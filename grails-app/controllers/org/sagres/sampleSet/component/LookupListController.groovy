package org.sagres.sampleSet.component

import grails.converters.JSON
import org.sagres.sampleSet.annotation.SampleSetPlatformInfo
import org.sagres.sampleSet.annotation.SampleSetSampleInfo

class LookupListController {
  static scaffold = LookupList

  def list(Integer max) {
	  //params.max = Math.min(max ?: 10, 100)
	  [lookupListInstanceList: LookupList.list(params), lookupListInstanceTotal: LookupList.count()]
  }

  def save = {
    def lookupList = new LookupList(params)
    if (lookupList.save(flush: true)) {
      flash.message = "${lookupList.name} created. You can now add more details."
      redirect(action: "show", id: lookupList.id)
    }
    else {
      render(view: "create", model: [lookupList: lookupList])
    }
  }

  def show = {
    def typeMap = [:]
    LookupList.list().collect { lookupList -> typeMap.put(lookupList.type, lookupList.type) }

    def lookupList = LookupList.get(params.id)
    if (!lookupList) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'lookupList.label', default: 'LookupList'), params.id])}"
      redirect(action: "list")
    }
    else
    {
//      def counts = [:]
//      lookupList.lookupDetails.each { detail ->
//        def criteria, detailCount
//        switch (lookupList.type)
//        {
//          case "Sample Info":
//            criteria = SampleSetSampleInfo.createCriteria()
//            detailCount = criteria.count {
//              eq(lookupList.name.encodeAsCamelCase(), detail.name)
//            }
//            break
//          case "Platform Info":
//            criteria = SampleSetPlatformInfo.createCriteria()
//            detailCount = criteria.count {
//              eq("platformOption", detail.name)
//            }
//            break
//        }
//        counts.put(detail.name,detailCount)
//      }
      if (params.error)
      {
        lookupList.errors.reject(params.error, null, "Error! There is problem with the option you entered.")
      }
      [lookupList: lookupList, typeMap: typeMap]//, counts:counts]
    }
  }

  def deleteDetail = {
    def lookupList = LookupList.get(params.id)
    def lookupListDetail = LookupListDetail.get(params.lookupDetailId)
    if (lookupListDetail) {
      lookupListDetail.delete()
    }
    redirect(action: "show", id: lookupList.id)
  }

  def addDetail = {
    if (params.name == "")
    {
      redirect(action: "show", params:[id: params.id, error: "org.sagres.mat.LookupListDetail.blank"])
      return null
    }
    def lookupList = LookupList.get(params.id)
    if (!lookupList.lookupDetails?.collect { detail -> detail.name }.contains(params.name)) {
      lookupList.addToLookupDetails(new LookupListDetail(name: params.name))
      if (lookupList.save(flush: true)) {
        redirect(action: "show", id: lookupList.id)
      }
    }
    else {
      redirect(action: "show", params:[id: params.id, error: "org.sagres.sampleSet.component.LookupListDetail.name.unique"])
      return null
    }
  }

  def getDetails = {
    def lookupList = LookupList.findByName(params.name)
    if (lookupList)
    {
      render (lookupList.lookupDetails.collect { detail -> detail.name } as JSON)
    }
  }

  def getter = {
    def lookupList = LookupList.get(params.id)
    def value = lookupList.getPersistentValue(params.property)
    if (value)
    {
      render lookupList.getPersistentValue(params.property)
    }
    else
    {
      render ''
    }
  }

  def setter = {
    if (params.value == "") {
      render "<span class='hint'>${grailsApplication.config.hint.default.text}</span>"
    }
    else {
      def value = params.value
      def lookupList = LookupList.get(params.id)
      lookupList.setProperty(params.property, value)
      lookupList.save(flush: true)
      if (params.paragraph) {
        value = value.encodeAsParagraph()
      }
      render value
    }
  }

}
