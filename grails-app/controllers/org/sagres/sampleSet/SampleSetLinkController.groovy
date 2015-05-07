package org.sagres.sampleSet

import org.springframework.dao.DataIntegrityViolationException

class SampleSetLinkController {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def index = {
    redirect(action: "list", params: params)
  }

  def cancel = {
    redirect(action: "list")
  }

  def list = {
    params.max = Math.min(params.max ? params.int('max') : 10, 100)
    [sampleSetLinkInstanceList: SampleSetLink.list(params), sampleSetLinkInstanceTotal: SampleSetLink.count()]
  }

  def create = {
    [sampleSetLinkInstance: new SampleSetLink(params)]
  }

  def save = {
    def sampleSetLinkInstance = new SampleSetLink(params)
    if (!sampleSetLinkInstance.save(flush: true)) {
      render(view: "create", model: [sampleSetLinkInstance: sampleSetLinkInstance])
      return
    }

    flash.message = message(code: 'default.created.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), sampleSetLinkInstance.id])
    redirect(action: "show", id: sampleSetLinkInstance.id)
  }

  def show = {
    def sampleSetLinkInstance = SampleSetLink.get(params.id)
    if (!sampleSetLinkInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "list")
      return
    }

    [sampleSetLinkInstance: sampleSetLinkInstance]
  }

  def edit = {
    def sampleSetLinkInstance = SampleSetLink.get(params.id)
    if (!sampleSetLinkInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "list")
      return
    }

    [sampleSetLinkInstance: sampleSetLinkInstance]
  }

  def update = {
    def sampleSetLinkInstance = SampleSetLink.get(params.id)
    if (!sampleSetLinkInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "list")
      return
    }

    if (params.version) {
      def version = params.version.toLong()
      if (sampleSetLinkInstance.version > version) {
        sampleSetLinkInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
          [message(code: 'sampleSetLink.label', default: 'SampleSetLink')] as Object[],
          "Another user has updated this SampleSetLink while you were editing")
        render(view: "edit", model: [sampleSetLinkInstance: sampleSetLinkInstance])
        return
      }
    }

    if (!params.visible)
    {
      params.visible = 0
    }
    sampleSetLinkInstance.properties = params

    if (!sampleSetLinkInstance.save(flush: true)) {
      render(view: "edit", model: [sampleSetLinkInstance: sampleSetLinkInstance])
      return
    }

    flash.message = message(code: 'default.updated.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), sampleSetLinkInstance.id])
    redirect(action: "show", id: sampleSetLinkInstance.id)
  }

  def delete = {
    def sampleSetLinkInstance = SampleSetLink.get(params.id)
    if (!sampleSetLinkInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "list")
      return
    }

    try {
      sampleSetLinkInstance.delete(flush: true)
      flash.message = message(code: 'default.deleted.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "list")
    }
    catch (DataIntegrityViolationException e) {
      flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'sampleSetLink.label', default: 'SampleSetLink'), params.id])
      redirect(action: "show", id: params.id)
    }
  }
}
