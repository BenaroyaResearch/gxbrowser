package org.sagres

import com.mongodb.WriteResult

class AnnotationController {

  def mongoDataService
  def sampleSetService

  /**
   * Return a list of available annotations/clinical data/variables for a dataset
   */
  def show = {
    long datasetId = params.long("id")

    Map annotations = [:]
    Map headers = mongoDataService.findOne("sampleSet", [ sampleSetId:datasetId ], [ "spreadsheet","labkeySubject","labkeyClinical","labkeyAncillaryData" ])
    headers.each { String key, h ->
      List collectionAnnotations = []
      if (key != "_id") {
        h.header.each { String k, Map v ->
          v.key = "${key}.header.${k}"
          collectionAnnotations.push(v)
        }
        collectionAnnotations.sort { it.order }
        String collection = key.startsWith("labkey") ? "TrialShare - ${cleanName(key.substring(6))}".toString() : key.capitalize()
        annotations.put(collection, collectionAnnotations)
      }
    }

    def labData = mongoDataService.findOne("sampleSet", [ sampleSetId:datasetId ], [ "labkeyLabResults.tests", "labkeyFlow.tests" ] )
    [ "labkeyLabResults":"TrialShare - Lab Results", "labkeyFlow":"TrialShare - Flow" ].each { String collection, String title ->
      if (labData?.get(collection)?.tests) {
        List collectionAnnotations = []
        labData[collection].tests.each { String key, Map testInfo ->
          testInfo.key = "${collection}.tests.${key}"
          collectionAnnotations.push(testInfo)
        }
        collectionAnnotations.sort { it.order }
        annotations.put(title, collectionAnnotations)
      }
    }

    List tabs = []
    Map varToTab = [:]
    def mongoTabs = mongoDataService.findOne("sampleSetTabs", [ sampleSetId:datasetId ], [ "tabs" ])?.tabs
    if (mongoTabs)
    {
      mongoTabs.each {
        String tabName = it.name
        if (!tabs.contains(tabName)) {
          tabs.push(tabName)
        }
        it.fields.each {
          varToTab.put("spreadsheet.header.${it.value.substring(7)}".toString(), tabName)
        }
      }
    }

    return [ annotations:annotations, tabs:tabs, varToTab:varToTab ]
  }

  def updateSetting = {
    long datasetId = params.long("id")
    String annotation = params.annotation
    String value = params.value

    if (datasetId && annotation && value) {
      Map update = [:]
      update.put(annotation, value)
      mongoDataService.update("sampleSet", [ sampleSetId:datasetId ], update)
    }

    render ""
  }

  def removeSetting = {
    long datasetId = params.long("id")
    String annotation = params.annotation
    if (datasetId && annotation) {
      mongoDataService.remove("sampleSet", [ sampleSetId:datasetId ], annotation)
    }

    render ""
  }

  private String cleanName(String name)
  {
    return name.replaceAll(/[A-Z][a-z]/, { " ${it}".toString() }).replaceAll("\\s+", " ").trim()
  }

  def updateOrder = {
    long datasetId = params.long("id")
    String annotation = params.annotation
    if (datasetId && annotation) {
      String[] aSplit = annotation.split("\\.")
      String collection = aSplit[0]
      String testHeader = aSplit[1]
      Map mongoOrder = mongoDataService.findOne("sampleSet", [ sampleSetId:datasetId ], [ annotation ])
      int prevOrder = mongoOrder?.get(collection)?.get(aSplit[1])?.get(aSplit[2])?.order
      int newOrder = params.int("order")
      int start = prevOrder > newOrder ? newOrder : prevOrder + 1
      int end = prevOrder > newOrder ? prevOrder : newOrder + 1
      int increment = prevOrder > newOrder ? 1 : -1

      Map headers = mongoDataService.findOne("sampleSet", [ sampleSetId:datasetId ], [ collection ])?.get(collection)?.get(testHeader)
      headers.each { String field, Map fInfo ->
        if (fInfo.order >= start && fInfo.order < end) {
          println fInfo.order
          String annot = "${collection}.${testHeader}.${field}.order"
          Map inc = [:]
          inc.put(annot, fInfo.order+increment)
          mongoDataService.update("sampleSet", [ sampleSetId:datasetId ], inc)
        }
      }
      Map curInc = [:]
      curInc.put(annotation, newOrder)
      mongoDataService.update("sampleSet", [ sampleSetId:datasetId ], curInc)
    }

    render ""
  }

  def populateSpreadsheetSettings = {
    List sampleSetIds = mongoDataService.find("sample", null, [ "sampleSetId" ], [ "sampleSetId":1 ], -1).unique { it.sampleSetId }
    sampleSetIds.each {
      long id = it.sampleSetId
      if (id) {
        sampleSetService.populateSampleSetSpreadsheet(id)
      }
    }
    render "Success!"
  }

}
