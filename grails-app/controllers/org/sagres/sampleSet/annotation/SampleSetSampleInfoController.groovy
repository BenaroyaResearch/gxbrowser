package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.component.FileTag
import org.sagres.sampleSet.component.LookupList
import org.sagres.sampleSet.component.LookupListDetail
import common.chipInfo.Species
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class SampleSetSampleInfoController {

  static scaffold = SampleSetSampleInfo

  def converterService
  def springSecurityService
  def mongoDataService



  def create = {
    def sampleSetSampleInfoInstance = new SampleSetSampleInfo()
    sampleSetSampleInfoInstance.properties = params
    return [sampleSetSampleInfoInstance: sampleSetSampleInfoInstance]
  }

  def save = {
    def sampleSetSampleInfoInstance = new SampleSetSampleInfo(params)
    if (sampleSetSampleInfoInstance.save(flush: true)) {
      flash.message = "${message(code: 'default.created.message', args: [message(code: 'sampleSetSampleInfo.label', default: 'SampleSetSampleInfo'), sampleSetSampleInfoInstance.id])}"
      redirect(action: "show", id: sampleSetSampleInfoInstance.id)
    }
    else {
      render(view: "create", model: [sampleSetSampleInfoInstance: sampleSetSampleInfoInstance])
    }
  }

  def upload = {
    def sampleSetSampleInfoInstance = SampleSetSampleInfo.get(params.id)

    def tag = FileTag.findByTag(grailsApplication.config.sampleInfo.spreadsheet.tag)
    if (!tag) {
      println "Tag not found"
      tag = new FileTag(tag: grailsApplication.config.sampleInfo.spreadsheet.tag)
      tag.save()
    }

    // upload spreadsheet file
//    def webRootDir = servletContext.getRealPath("/")
//    def dir = new File(webRootDir, "/${grailsApplication.config.files.directory}/${sampleSetSampleInfoInstance.sampleSet.id}/${tag.tag}")
    def dir = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSetSampleInfoInstance.sampleSet.id}/${tag.tag}")
    dir.mkdirs()
    def file = new File(dir, request.getHeader("X-File-Name"))
    def inputStream = new BufferedInputStream(request.inputStream);
    def outputStream = new BufferedOutputStream(new FileOutputStream(file))
    int c
    while ((c = inputStream.read()) != -1) {
      outputStream.write(c)
    }
    outputStream.close()

    def ext = file.name.substring(file.name.lastIndexOf('.')+1)
    def header
    if (ext == "csv")
    {
      header = file.readLines()[1].trim()
    }
    else
    {
//      InputStream inStream = new FileInputStream(file)

    }

    def spreadsheet = sampleSetSampleInfoInstance.sampleSetSpreadsheet ?: new SampleSetFile(tag: tag, sampleSet: sampleSetSampleInfoInstance.sampleSet)
    spreadsheet.filename = file.name
    spreadsheet.extension = ext
    spreadsheet.description = "Sample Set Spreadsheet"
    if (header)
    {
      spreadsheet.header = header
    }
    spreadsheet.save()

    sampleSetSampleInfoInstance.sampleSetSpreadsheet = spreadsheet
    sampleSetSampleInfoInstance.save()
  }

  def setOtherDetail = {
    def lookupListInstance = LookupList.get(params.lookupListId)
    def property = lookupListInstance.name.encodeAsCamelCase()
    if (!lookupListInstance.lookupDetails?.collect { detail -> detail.name }.contains(params.value)) {
      def newValue = new LookupListDetail(name: params.value)
      lookupListInstance.addToLookupDetails(newValue)
      if (lookupListInstance.save(flush: true)) {
        def sampleSetSampleInfoInstance = SampleSetSampleInfo.get(params.id)
        sampleSetSampleInfoInstance.setProperty(property, newValue)
        sampleSetSampleInfoInstance.save(flush: true)
        render newValue.id
      }
    }
    else {
      render "duplicate"
    }
  }

  def setSampleSources = {
    def sampleSetSampleInfo = SampleSetSampleInfo.get(params.id)
    if (params.selected && params.sampleSourceId)
    {
      def selected = params.boolean("selected")
      def sampleSourceId = params.long("sampleSourceId")
      def sampleSource = LookupListDetail.get(sampleSourceId)
      if (sampleSource)
      {
        if (selected)
        {
          if (!sampleSetSampleInfo.sampleSources.contains(sampleSource))
          {
            sampleSetSampleInfo.addToSampleSources(sampleSource)
          }
        }
        else
        {
          sampleSetSampleInfo.removeFromSampleSources(sampleSource)
        }
        sampleSetSampleInfo.save()
      }
	  render ''
    }
    else if (params.otherSampleSource)
    {
      def otherSampleSource = params.otherSampleSource
      def lookupList = LookupList.findByName("Sample Source")
      def sampleSource = LookupListDetail.findByLookupListAndName(lookupList, otherSampleSource)
      if (!sampleSource)
      {
        sampleSource = new LookupListDetail(lookupList: lookupList, name: otherSampleSource).save(flush:true)
      }
      sampleSetSampleInfo.addToSampleSources(sampleSource)
      sampleSetSampleInfo.save()

      render sampleSource.id
    }
  }

  def setter = {
    def sampleSetSampleInfoInstance = SampleSetSampleInfo.get(params.id)
    if (params.value == "") {
      sampleSetSampleInfoInstance.setProperty(params.property, null)
      sampleSetSampleInfoInstance.save()
      render "<span class='hint'>${grailsApplication.config.hint.default.text}</span>"
    }
    else {
      def value = params.value
      if (params.clean)
      {
        value = converterService.docToHTML(value)
      }
      if (params.property == 'numberOfSamples') {
        try
        {
          sampleSetSampleInfoInstance.setProperty(params.property, Integer.parseInt(value))
        }
        catch (NumberFormatException e)
        {
          render "error"
          return null
        }
      }
      else if (params.property == 'species')
      {
        if (params.long("value"))
        {
          sampleSetSampleInfoInstance.species = Species.get(params.long("value"))
        }
        else
        {
          sampleSetSampleInfoInstance.species = null
        }
      }
      else {
        if (params.boolean("isSelect"))
        {
          sampleSetSampleInfoInstance.setProperty(params.property, LookupListDetail.get(params.long("value")))
        }
        else
        {
          sampleSetSampleInfoInstance.setProperty(params.property, value)
        }
      }
      sampleSetSampleInfoInstance.save()
      if (params.paragraph) {
        value = value.encodeAsParagraph()
      }
      render value
    }
  }

  def getter = {
    def sampleSetSampleInfo = SampleSetSampleInfo.get(params.id)
    def value = sampleSetSampleInfo.getPersistentValue(params.property)
    if (!value) {
      render ''
    }
    else {
      render value
    }
  }

}
