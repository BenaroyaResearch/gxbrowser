package org.sagres.sampleSet

import grails.converters.JSON
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class DatasetGroupDetailController {

  static scaffold = DatasetGroupDetail
  def mongoDataService
  def springSecurityService



  def setHexColor = {
    def groupDetail = DatasetGroupDetail.get(params.id)
    groupDetail.setHexColor(params.color)
    groupDetail.save(flush:true)

    render [[hexColor:groupDetail.hexColor]] as JSON

    return null
  }

  def groupDetailInfo = {
    def groupDetail = DatasetGroupDetail.get(params.id)
    if (params.includeDetails)
    {
      render ([groupDetail] as JSON)
    }
    else
    {
      render ([[id: groupDetail.id, displayOrder: groupDetail.displayOrder, hexColor: groupDetail.hexColor, sampleId: groupDetail.sample.id, classType: 'datasetGroupDetail']] as JSON)
    }
    return null
  }

  def getSampleInfo = {
    def groupDetail = DatasetGroupDetail.get(params.id)
    def sampleSet = groupDetail.group.groupSet.sampleSet
    def sampleInfo = [sampleId:groupDetail.sample.id]
    def sampleValues = mongoDataService.getValuesForSample(sampleSet.id, groupDetail.sample.id)
    if (sampleValues)
    {
      def fields = mongoDataService.getSampleSetFields(sampleSet.id)
      def valueMap = [:]
      sampleValues.each { key, value ->
        if (fields.containsKey(key))
        {
          valueMap.put(fields.get(key).displayName, value)
        }
      }
      sampleInfo.put("sampleRow", valueMap)
    }
    if (groupDetail.sample)
    {
      def arrayData = groupDetail.sample
      def arrayDataMap = [:]
      arrayDataMap.put("Barcode", arrayData.barcode)
      arrayDataMap.put("Chip Type", arrayData.chip.chipType.name)
      sampleInfo.put("arrayData", arrayDataMap)
    }
    render ([sample:sampleInfo] as JSON)
    return null
  }

}
