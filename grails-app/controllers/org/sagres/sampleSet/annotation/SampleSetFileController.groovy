package org.sagres.sampleSet.annotation

import com.mongodb.BasicDBObject
import com.mongodb.DBObject
import grails.converters.JSON
import org.apache.commons.io.FileUtils
import org.apache.commons.io.FilenameUtils
import org.sagres.sampleSet.DatasetGroup
import org.sagres.sampleSet.MongoDataService
import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.component.FileTag
import org.sagres.util.mongo.MongoConnector
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class SampleSetFileController {

  def mongoDataService
  def sampleSetService
  def samplesFileImportService
  def springSecurityService
  def grailsApplication



  def download = {
    String dir = null, filename = null
    boolean isSoft = false, isChipFile = false
    def chipType = null
    if (params.signalFile && params.sampleSetId) {
      long ssId = params.long("sampleSetId")
      if (SampleSet.exists(ssId)) {
        dir = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${ssId}")
        isChipFile = true
        filename = params.signalFile
        isSoft = filename.matches(/^((GDS)|(GSE)).*((_family)|(_full)|(series_matrix))(\.soft)*$/)
        if (isSoft) {
          if (filename.endsWith("series_matrix")) {
            filename += ".txt"
          } else if (filename.endsWith("_family") || filename.endsWith("_full")) {
            filename += ".soft"
          }
        } else {
          if (!filename.matches(/.*\.txt$/)) {
            filename += ".txt"
          }
        }
        chipType = SampleSet.findById(ssId).chipType
      }
    } else {
      def sampleSetFile = SampleSetFile.get(params.id)
      dir = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSetFile.sampleSet.id}")
      isChipFile = sampleSetFile.tag?.tag == "Chip Files"
      isSoft = sampleSetFile.extension == "soft"
      chipType = sampleSetService.getChipType(sampleSetFile.sampleSet.id)
      filename = sampleSetFile.filename
    }
    if (isChipFile)
    {
      if (isSoft)
      {
        dir = "${grailsApplication.config.bgSubstractedFiles.baseDir}/soft"
      }
      else
      {
        dir = "${grailsApplication.config.bgSubstractedFiles.baseDir}/${chipType.importDirectoryName}"
      }
    }
    def file = new File(dir, filename)
    if (file.exists()) {
      response.setContentType("application/octet-stream")
      response.setHeader("Content-Disposition", "attachment; filename=\"${file.name}\"") // Firefox requires quoted file.name to handle spaces in download
      response.outputStream << file.bytes
    }
    return null
  }

  def checkTag = {
    def tagName = params.tag
    if (tagName && !FileTag.findByTag(tagName))
    {
      new FileTag(tag: tagName).save(flush:true)
    }
    return null
  }

  def upload = {
    def sampleSetId = params.id
    def incomingFile = params["files[]"]
    if (sampleSetId && incomingFile)
    {
      def sampleSet = SampleSet.get(sampleSetId)
      def tagName = params.tag ?: request.getHeader("tag")
      def tag = tagName ? FileTag.findByTag(tagName) : null
      def description = params.description ?: request.getHeader("description")
      def inFilename = incomingFile?.fileItem?.fileName
      def baseName = FilenameUtils.getName(inFilename) ?: ''
      def dot = baseName.lastIndexOf('.')
      def extension = dot > 0 ? baseName.substring(dot + 1) : ''

      if (!tag && tagName)
      {
        tag = new FileTag(tag: tagName)
        tag.save(flush:true)
      }

  //    if (tag)
  //    {
  //      def existingFile = sampleSet.sampleSetFiles.find {
  //        if (it.tag.tag == tagName && it.filename == baseName)
  //        {
  //          return it
  //        }
  //      }
        def existingFile = sampleSet.sampleSetFiles.find {
          if (it.filename == baseName)
          {
            return it
          }
        }
        if (existingFile)
        {
          versionizeFile(existingFile)
          existingFile.description = description
          existingFile.fileVersion++
          existingFile.save()
        }

        // make base directories
        def dir = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSetId}")
        dir.mkdirs()

        // upload spreadsheet file
        def file = new File(dir, baseName)
        incomingFile.transferTo(file)

        if (tagName == grailsApplication.config.sampleInfo.spreadsheet.tag)
        {
          saveSpreadsheetFile(sampleSet, file, tag, extension)
        }
        else
        {
          if (!existingFile)
          {
            def sampleSetFile = new SampleSetFile(filename: baseName, extension: extension, description: description, sampleSet: sampleSet)
            if (tag)
            {
              sampleSetFile.tag = tag
            }
            sampleSetFile.save()
          }
        }

        render(contentType: 'text/html') {
          [[name:baseName]] as JSON
        }
    }
//    }

    return null
  }

  private def versionizeFile = { SampleSetFile sampleSetFile ->
    def dir = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSetFile.sampleSet.id}")
    def file = new File(dir, sampleSetFile.filename)
    def versionizedName = "${sampleSetFile.filename}_v${sampleSetFile.fileVersion}"
    if (file.exists()) {
      FileUtils.copyFile(file, new File(dir, versionizedName))
    }
  }

  private def saveSpreadsheetFile = { SampleSet sampleSet, File file, FileTag tag, extension ->
    def header
    def groups = getDefaultGroups(sampleSet)
    def collection = MongoConnector.getInstance().getCollection()
    if (extension == "csv")
    {
      header = samplesFileImportService.importFile(collection, groups, sampleSet.id, file, true)
    }
    else
    {
      header = samplesFileImportService.importExcelFile(collection, groups, sampleSet.id, file, true)
    }
    groups.each { it.save() }

    def sampleSetSampleInfoInstance = sampleSet.sampleSetSampleInfo
    def spreadsheet = sampleSetSampleInfoInstance.sampleSetSpreadsheet ?: new SampleSetFile(tag: tag, sampleSet: sampleSet)
    spreadsheet.filename = file.name
    spreadsheet.extension = extension
    spreadsheet.description = "Sample Set Spreadsheet"
    if (header)
    {
      spreadsheet.header = header.join('\t')
    }
    spreadsheet.save()

    sampleSetSampleInfoInstance.sampleSetSpreadsheet = spreadsheet
    sampleSetSampleInfoInstance.numberOfSamples = collection.count(new BasicDBObject("sampleSetId", sampleSet.id))
    sampleSetSampleInfoInstance.save()
    sampleSet.save()

    sampleSetService.populateSampleSetSpreadsheet(sampleSet.id)
  }

  private List<DatasetGroup> getDefaultGroups(SampleSet sampleSet)
  {
    def groups = []
    if (sampleSet.groupSets)
    {
      sampleSet.groupSets.each { groupSet ->
        def group = groupSet.groups.find { group -> group.name == grailsApplication.config.group.default }
        if (!group)
        {
          def lastDisplayOrder = groupSet.groups.displayOrder.max() + 1
          group = new DatasetGroup(name: grailsApplication.config.group.default, displayOrder: lastDisplayOrder)
          groupSet.addToGroups(group)
        }
        groups.add(group)
      }
    }
    return groups
  }

  def spreadsheetData = {
    def sortedSamples = [], origFileCount = 0
    def echo = params.sEcho ? Integer.parseInt(params.sEcho) : 0
    if (params.id)
    {
      def sampleSetId = Long.parseLong(params.id)
      def columns = mongoDataService.getSampleFieldKeys()[0..4]
      def sortCol = params.iSortCol_0 ? Integer.parseInt(params.iSortCol_0) : 0
      def sortDir = params.sSortDir_0 == 'desc' ? MongoDataService.DESCENDING : MongoDataService.ASCENDING

      if (sortCol == 0)
      {
        sortedSamples = mongoDataService.getSamplesInSampleSet(sampleSetId, "sampleId", sortDir, 5)
      }
      else
      {
        // need to fix empty cells problem
        def sortKey = columns.get(sortCol-1)
        sortedSamples = mongoDataService.getSamplesInSampleSet(sampleSetId, "values.${sortKey}", sortDir, 5)
      }
      sortedSamples = sortedSamples.collect { DBObject s ->
        def values = s.get("values")
        def sampleValues = ["","","","","",""]
        sampleValues[0] = s.get("sampleId")
        if (values)
        {
          columns.eachWithIndex { key, i ->
            sampleValues.set(i+1, values.get(key))
          }
        }
        return sampleValues
      }
      origFileCount = sortedSamples.size()
    }
    render ([sEcho: echo, iTotalRecords: origFileCount, iTotalDisplayRecords: sortedSamples.size(), aaData: sortedSamples] as JSON)
    return null
  }

}
