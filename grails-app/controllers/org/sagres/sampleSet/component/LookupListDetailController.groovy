package org.sagres.sampleSet.component

class LookupListDetailController {
  static scaffold = LookupListDetail

  def save = {
    def lookupListDetailInstance = new LookupListDetail(params)
    if (lookupListDetailInstance.save(flush: true)) {
      flash.message = "${message(code: 'default.created.message', args: [message(code: 'lookupListDetail.label', default: 'LookupListDetail'), lookupListDetailInstance.id])}"
      redirect(action: "show", id: lookupListDetailInstance.id)
    }
    else {
      render(view: "create", model: [lookupListDetailInstance: lookupListDetailInstance])
    }
  }

  def setter = {
    def lookupListDetailInstance = LookupListDetail.get(params.id)
    def value = params.value
    if (!lookupListDetailInstance.lookupList.lookupDetails?.collect { detail -> detail.name }.contains(value))
    {
      lookupListDetailInstance.name = value
      lookupListDetailInstance.save(flush: true)
    }
    render lookupListDetailInstance.name
  }

  def getter = {
    def lookupListDetailInstance = LookupListDetail.get(params.id)
    render lookupListDetailInstance.name
  }

}
