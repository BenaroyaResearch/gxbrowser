package org.sagres.sampleSet

import grails.converters.JSON

class SampleSetTabConfiguratorController {

  private static final String TAB_COLLECTION = "sampleSetTabs"
  private def tg2Datatypes = ["Benaroya", "Baylor"]
  def sampleSetService //injected
  def mongoDataService //injected
  def geneQueryService //injected
  def tg2QueryService //injected

  def create = {
    SampleSet sampleSet = SampleSet.get(params.id)

    Map<String,String> mongoDatafields = mongoDataService.sampleSetFields(params.long("id"))
    Set<String> mongoDatafieldTypes = new HashSet<String>(mongoDatafields?.values()).unique()

    def externalFields = [:]
    externalFields.put("gene", geneQueryService.geneSummaryFields())
    def isTg2 = tg2Datatypes.contains(sampleSetService.getDatasetType(sampleSet.id)?.name)
    if (isTg2) {
      externalFields.put("ben_tg2", tg2QueryService.tg2Fields())
    }

    // get existing configuration
    def existingFields = [:], query = [:]
    query.put("sampleSetId", sampleSet.id)
    def tabs = mongoDataService.find(TAB_COLLECTION, query, ["tabs"], null, 0)
    if (tabs)
    {
      tabs[0]?.tabs?.sort { a, b ->
        a.order <=> b.order
      }.each { tab ->
        String tabName = tab.name
        def tabFields = tab.fields
        tabFields.each {
          String id = it.value
          String displayName = id.substring(id.lastIndexOf(".")+1)
          if (displayName.length() > 0)
          {
            String db = it.externalDb
            if (db)
            {
              externalFields.get(db).remove(displayName)
            }
            else
            {
              mongoDatafields.remove(displayName)
            }
            it.displayName = displayName.encodeAsHumanize()
          }
        }
        existingFields.put(tabName, tabFields)
      }
    }

    return [sampleSet: sampleSet, internalFields: mongoDatafields, internalFieldTypes: mongoDatafieldTypes,
      externalFields: externalFields, existingFields: existingFields]
  }

  def save = {
    def sampleSetId = params.long("id")
    if (sampleSetId)
    {
      def tabs = [:], order = params.list("order[]").collect { return it.substring(4) }
      params.keySet().each { String k ->
        if (params.get(k).size() > 0)
        {
          if (k.startsWith("tab-"))
          {
            int brackets = k.lastIndexOf("[]")
            tabs.put(k.substring(4,brackets), params.get(k))
          }
        }
      }
      if (tabs.size() > 0)
      {
        def tabKeyValues = []
        tabs.each { key, value ->
          def tabValues = []
          if (value instanceof String)
          {
            tabValues.push(parseField(value))
          }
          else
          {
            value.each {
              tabValues.push(parseField(it))
            }
          }
          tabKeyValues.push([name: key, fields:tabValues, order:order.indexOf(key)])
        }

        def query = [:], values = [:]
        query.put("sampleSetId", sampleSetId)
        values.put("tabs", tabKeyValues)
        mongoDataService.insert(TAB_COLLECTION, query, values)
      }
      else
      {
        mongoDataService.remove(TAB_COLLECTION, ["sampleSetId": sampleSetId], "tabs")
      }
    }
    return
  }

  private def parseField(String value) {
    int dot = value.indexOf(".")
    def db = value.substring(0, dot)
    def val = [:]
    val.put("value", value.substring(dot+1))
    if (db != "internal") {
      val.put("externalDb", db)
    }
    return val
  }

}
