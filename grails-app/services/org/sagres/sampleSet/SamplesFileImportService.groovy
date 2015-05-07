package org.sagres.sampleSet

import com.mongodb.DBCollection
import org.sagres.util.excel.ExcelBuilder
import com.mongodb.BasicDBObject
import com.mongodb.DBCursor
import com.mongodb.DBObject
import org.apache.poi.ss.usermodel.Row
import common.ArrayData
import org.sagres.util.mongo.MongoConnector
import com.mongodb.BasicDBList
import groovy.sql.Sql
import groovy.sql.GroovyRowResult
import org.sagres.importer.TextTable
import org.sagres.importer.TextTableSeparator

class SamplesFileImportService {

  static final String DEFAULT_DELIMITER = ","
  static final String DEFAULT_INPUT_TYPE = "text"

  def dataSource
  def grailsApplication
  def tg2QueryService
  def sampleSetService
  def mongoDataService

  /**
   * Importing CSV file with default delimiter of comma (,)
   */
  def List<String> importFile(DBCollection collection, List<DatasetGroup> groups, long sampleSetId, File file, boolean header)
  {
    return importFile(collection, groups, sampleSetId, file, DEFAULT_DELIMITER, header)
  }

  /**
   * Importing CSV file with user defined
   */
  def List<String> importFile(DBCollection collection, List<DatasetGroup> groups, long sampleSetId, File file, String delimiter, boolean header)
  {
    def datasetType = sampleSetService.getDatasetType(sampleSetId)?.name
    def isTg2 = datasetType in ["Benaroya", "Baylor"]
    def barcodeToArrayDataId = sampleSetService.getBarcodeToArrayDataId(sampleSetId)
    def sampleNameToArrayDataId, tg2SampleIds
    if (isTg2)
    {
      sampleNameToArrayDataId = tg2QueryService.getTg2Ids(sampleSetId)
      tg2SampleIds = tg2QueryService.getTg2SampleIds(sampleNameToArrayDataId.keySet())
    }

    def keys
    List<String> displayNames = new ArrayList<String>()
    file.eachLine { line ->
      String[] values = TextTable.splitCsvRow(line)
      if (header && !displayNames)
      {
        values.eachWithIndex { String h, int hIdx ->
          if (hIdx > 0 && !h.isAllWhitespace())
          {
            displayNames.push(h)
          }
        }
        keys = createKeys(displayNames)
      }
      else
      {
        if (!displayNames)
        {
          displayNames = createHeader(values.length)
          keys = createKeys(displayNames)
        }

        def row = [:]
        if (values)
        {
          values.eachWithIndex { v, i ->
            if (i > 0 && i <= keys.size()) {
              row.put(keys[i-1], v)
            }
          }

          def sampleName = values[0].toString().trim()
          if (sampleName.length() > 0)
          {
            def arrayDataId
            if (isTg2 && sampleNameToArrayDataId.containsKey(sampleName))
            {
              arrayDataId = sampleNameToArrayDataId.get(sampleName)
              arrayDataId.each { Map tg2Values ->
                addSample(collection, sampleSetId, tg2Values.sampleId, sampleName, groups, row, tg2SampleIds.get(sampleName), tg2Values.chipId)
              }
            }
            else
            {
              arrayDataId = barcodeToArrayDataId.get(sampleName)
              if (arrayDataId)
              {
                addSample(collection, sampleSetId, arrayDataId, sampleName, groups, row)
              }
            }
          }
        }
      }
    }

    saveFields(displayNames)
    return displayNames
  }

  def List<String> importExcelFile(DBCollection collection, List<DatasetGroup> groups, long sampleSetId, File file, boolean header)
  {
    def datasetType = sampleSetService.getDatasetType(sampleSetId)?.name
    def isTg2 = datasetType in ["Benaroya", "Baylor"]
    def barcodeToArrayDataId = sampleSetService.getBarcodeToArrayDataId(sampleSetId)
    def sampleNameToArrayDataId, tg2SampleIds
    if (isTg2)
    {
      sampleNameToArrayDataId = tg2QueryService.getTg2Ids(sampleSetId)
      tg2SampleIds = tg2QueryService.getTg2SampleIds(sampleNameToArrayDataId.keySet())
    }

    def keys
    def displayNames
    new ExcelBuilder(file).eachLine {
      if (it.rowNum == 0)
      {
        displayNames = header ? parseHeaderRow(it) : createHeader(it.lastCellNum)
        keys = createKeys(displayNames)
      }
      else
      {
        def row = parseRow(it, keys)
        def sampleName = cell(0).toString().trim()
        if (sampleName.length() > 0)
        {
          def arrayDataId
          if (isTg2 && sampleNameToArrayDataId.containsKey(sampleName))
          {
            arrayDataId = sampleNameToArrayDataId.get(sampleName)
            arrayDataId.each { Map tg2Values ->
              addSample(collection, sampleSetId, tg2Values.sampleId, sampleName, groups, row, tg2SampleIds.get(sampleName), tg2Values.chipId)
            }
          }
          else
          {
            arrayDataId = barcodeToArrayDataId.get(sampleName)
            if (arrayDataId)
            {
              addSample(collection, sampleSetId, arrayDataId, sampleName, groups, row)
            }
          }
        }
      }
    }
    saveFields(displayNames)
    return displayNames
  }

  private def addSample(DBCollection collection, long sampleSetId, long sampleId, String sampleName, List<DatasetGroup> groups, Map row, String tg2SampleId = null, long tg2Id = -1)
  {
    BasicDBObject toFind = new BasicDBObject("sampleSetId", sampleSetId)
    toFind.put("sampleId", sampleId)
    DBCursor cur = collection.find(toFind)
    if (cur.hasNext())
    {
      DBObject obj = cur.next()
      BasicDBObject newValues = new BasicDBObject()
      newValues.append("sampleName", sampleName)
      if (tg2SampleId)
      {
        newValues.append("tg2SampleId", tg2SampleId)
      }
      if (tg2Id >= 0)
      {
        newValues.append("tg2chipPositionId", tg2Id)
      }
      row.keySet().each { key ->
        newValues.append("values.${key}", row.get(key))
      }
      BasicDBObject setValue = new BasicDBObject("\$set", newValues)
      collection.update(obj, setValue, true, true)
    }
    else
    {
      BasicDBObject r = new BasicDBObject("sampleSetId", sampleSetId)
      r.put("sampleId", sampleId)
      r.put("sampleName", sampleName)
      if (tg2SampleId)
      {
        r.put("tg2SampleId", tg2SampleId)
      }
      if (tg2Id)
      {
        r.put("tg2chipPositionId", tg2Id)
      }
      r.put("values", row)
      collection.insert(r)
      createGroupDetail(sampleId, groups)
    }
  }

  private def createGroupDetail(long sampleId, List<DatasetGroup> groups)
  {
    def arrayData = ArrayData.findById(sampleId)
    if (arrayData)
    {
      groups.each { group ->
        group.addToGroupDetails(new DatasetGroupDetail(sample: arrayData))
      }
    }
  }

  private def saveFields(List<String> fields)
  {
    def collection = MongoConnector.getInstance().getCollection(grailsApplication.config.mongo.sampleField.collection)
    def numFields = collection.getCount()
    fields.each {
      def key = it.encodeAsKeyCase()
      def query = new BasicDBObject("key", key)
      if (!collection.findOne(query))
      {
        def field = new BasicDBObject("key", key)
        field.put("displayName", it)
        field.put("type", "text")
        field.put("defaultDisplayOrder", numFields)
        collection.insert(field)
        numFields++
      }
    }
  }

  def importLabkeyData(long sampleSetId, File file, String collection)
  {
    List header = null, keys = null
    def rows = []
    def headerKeys = [:]
    new ExcelBuilder(file).eachLine { Row r ->
      if (r.rowNum == 0)
      {
        header = parseHeaderRow(r)
        keys = createKeys(header)
        keys.eachWithIndex { k, i ->
          headerKeys.put(k, [order:i, displayName:header[i]])
        }
      }
      else
      {
        def row = parseRow(r, keys)
        row.put("participant_id", cell(0).toString().trim())
        rows.push(row)
      }
    }
    if (!rows.isEmpty())
    {
      mongoDataService.update(collection, [sampleSetId:sampleSetId], [header:headerKeys, data:rows])
    }
  }

  def importLabkeyCSVData(long sampleSetId, File file, String collection, TextTableSeparator separator)
  {
    List header = null, keys = null
    def rows = []
    def headerKeys = [:]
    def headerToUniqueValues = [:]
    def firstFiveLines = []
    file.eachLine { line, i ->
      String[] values = TextTable.splitRow(line, separator)
      if (i == 1)
      {
        header = values.collect {
          if (it == "VST" || it == "participantVisitVisitLabel") {
            return "visit"
          } else {
            return it
          }
        }
				header[0] = "participant_id"
        keys = createKeys(header)
        keys.eachWithIndex { k, j ->
          headerKeys.put(k, [order:j, displayName:header[j].encodeAsHumanize()])
          headerToUniqueValues.put(k, new HashSet())
        }
      }
      else
      {
        def row = [:]
        values.eachWithIndex { v, idx ->
          def hKey = keys[idx]
          row.put(hKey, v)
          ((Set)headerToUniqueValues.get(hKey)).add(v)
        }
        rows.push(row)
        if (i < 6) {
          firstFiveLines.push(values)
        }
      }
    }

    headerToUniqueValues.each { k, Set<String> v ->
      Map hMap = headerKeys.get(k)
      hMap.put("numUnique", v.size())
      def isNumber = true
      v.each { String s ->
        if (!s.isAllWhitespace()) {
          isNumber = isNumber && s.trim().isNumber()
        }
      }
      def dType = isNumber && k != "participant_id" ? "number" : "string"
      hMap.put("datatype", dType)
      hMap.put("ssat_visible", "show")
      hMap.put("overlay_visible", "show")
    }
    if (!rows.isEmpty())
    {
      def data = [labkey:true]
      def searchKeys = ["participant_id"]
      if (collection in [ "labkeyLabResults", "labkeyFlow" ]) {
        searchKeys.push("visit")

        String testCat = collection == "labkeyLabResults" ? "lbtest" : "flow_population"

        if (headerToUniqueValues.containsKey(testCat))
        {
          int order = 0
          def tests = [:]
          headerToUniqueValues.get(testCat).each {
            tests.put(toKey(it), [ name:it, displayName:it, datatype:"number", ssat_visible:"show", overlay_visible:"show", order:order ])
            order++
          }
          searchKeys.push(testCat)

          data.put(collection, [ header:headerKeys, tests:tests ])
        }
        else
        {
          data.put(collection, [ header:headerKeys ])
        }
      } else {
        data.put(collection, [ header:headerKeys ])
      }
      mongoDataService.update("sampleSet", [sampleSetId:sampleSetId], data)
      mongoDataService.insertDocuments(collection, searchKeys, rows)
    }
  }

  private String toKey(String text)
  {
    return text.toLowerCase().replaceAll(/[^A-z0-9 \+\-%]/, "").replaceAll(/\s+/, "_")
  }

  private def List parseHeaderRow(Row row)
  {
    def rowArray = []
    for (int c = 1; c < row.lastCellNum; c++)
    {
      rowArray.add(row.getAt(c))
    }
    return rowArray
  }

  private def Map parseRow(Row row, keys)
  {
    def rowValues = [:]
    for (int c = 1; c < row.lastCellNum; c++)
    {
      if (keys[c-1])
      {
        rowValues.put(keys[c-1], row.getAt(c))
      }
    }
    return rowValues
  }

  private def List createHeader(int length)
  {
    def headers = []
    1.step(length, 1) { headers.push("Col${it}") }
    return headers
  }

  private def List createKeys(List<String> displayNames)
  {
    return displayNames.collect { String n -> n.toLowerCase().replaceAll("\\."," ").replaceAll("\\s+","_") }
  }

}
