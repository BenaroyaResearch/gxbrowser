package org.sagres.sampleSet

import grails.converters.JSON
import org.codehaus.groovy.grails.commons.GrailsClassUtils
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class DatasetGroupController {

  static scaffold = DatasetGroup
  def chartingDataService
  def mongoDataService
  def springSecurityService



  def create = {
    def groupSet = DatasetGroupSet.get(params.id)
    def displayOrder = groupSet.groups.displayOrder.max() + 1

    groupSet.addToGroups(new DatasetGroup(name: params.name, displayOrder: displayOrder, datasetGroupSet: groupSet))
    groupSet.save()

    def errors
    if (groupSet.errors.getFieldErrorCount() > 0)
    {
      errors = [errors:groupSet.errors.getFieldError().codes[3], name: params.name]
    }

    redirect(controller:'datasetGroupSet', action:'view', id: groupSet.id, params: errors)

    return null
  }

  def listGroupDetails = {
    def group = DatasetGroup.get(params.id)
    render group.groupDetails.collect { detail -> [id:detail.id, sampleId:detail.sample.id] } as JSON
    return [datasetGroupDetails:group.groupDetails]
  }

  def groupInfo = {
    def group = DatasetGroup.get(params.id)
    if (params.includeDetails)
    {
      render ([group] as JSON)
    }
    else
    {
      def color = group.hexColor ?: grailsApplication.config.group.color.default
      render ([[id: group.id, name: group.name, displayOrder: group.displayOrder, hexColor: color, classType: 'datasetGroup']] as JSON)
    }
    return null
  }

  def setHexColor = {
    def group = DatasetGroup.get(params.id)
    group.setHexColor(params.color)
    group.save(flush:true)

    render ([[hexColor:group.hexColor]] as JSON)

    return null
  }

  def moveGroup = {
    def group = DatasetGroup.get(params.id)
    def totalGroups = DatasetGroup.countByGroupSet(group.groupSet)
    def newPosition = params.newPosition ? Integer.parseInt(params.newPosition) : DatasetGroup.get(params.reference).displayOrder
    switch (params.moveType) {
      case 'before':
        if (newPosition > group.displayOrder)
        {
          newPosition--
        }
        break;
      case 'after':
        if (newPosition < group.displayOrder)
        {
          newPosition++
        }
        break;
    }
    if (newPosition > totalGroups)
    {
      newPosition = totalGroups
    }
    if (newPosition < 0)
    {
      newPosition = 0
    }
    if (group.displayOrder != newPosition)
    {
      def gc = DatasetGroup.createCriteria()
      if (newPosition > group.displayOrder)
      {
        gc.list {
          eq('groupSet', group.groupSet)
          and {
            between('displayOrder', group.displayOrder+1, newPosition)
          }
        }?.each { it.displayOrder--; it.save() }
      }
      else
      {
        gc.list {
          eq('groupSet', group.groupSet)
          and {
            between('displayOrder', newPosition, group.displayOrder-1)
          }
        }?.each { it.displayOrder++; it.save() }
      }
      group.setDisplayOrder(newPosition)
      group.save()
    }
    render ([[newPosition:newPosition]] as JSON)
    return null
  }

  def moveSample = {
    def groupDetail = DatasetGroupDetail.get(params.id)
    def newGroup = DatasetGroup.get(params.newGroup)

    // don't care about display order for now
    groupDetail.group = newGroup
    groupDetail.save()
  }

  def setter = {
    def datatype = GrailsClassUtils.getPropertyType(DatasetGroup.class, params.property)
    def group = DatasetGroup.get(params.id)
    def value
    switch (datatype) {
      case Integer.class:
        value = Integer.parseInt(params.value)
        break
      case Long.class:
        value = Long.parseLong(params.value)
        break
      default:
        value = params.value.trim()
    }
    if (value == "")
    {
      render "error"
    }
    else
    {
      group.setProperty(params.property, value)
      if (group.save(flush:true))
      {
        render ([newValue:value] as JSON)
      }
    }
    return null
  }

  def getData = {
    def library = params.chartLibrary

    def dFloor = params.float("floor")
		def sampleSet = DatasetGroup.get(params.long("id")).groupSet.sampleSet
		if (sampleSet.rawSignalType?.id == 2)
		{
			dFloor = 0.0
		}
		if (sampleSet.defaultSignalDisplayType?.id == 6)
		{
			dFloor = 0.0
		}
		def output = chartingDataService.getGroupData(sampleSet, params.long("id"), params.probeId, dFloor, 10, (library == 'canvasXpress'))
    def data
    if (library == "canvasXpress")
    {
      def y = [:]
      y.put('vars', ["group${params.id}"])
      y.put('smps', output["samples"])
      y.put('data', [output["data"]])
      data = ["y":y]
    }
    else
    {
      data = output["data"]
    }
    render ([data: data, colors: output["colors"], max: output["max"]] as JSON)
    return null
  }

}
