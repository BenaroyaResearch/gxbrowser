package common

import org.springframework.dao.DataIntegrityViolationException

class SecRoleController {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def index = {
    redirect(action: "list", params: params)
  }

  def list = {
    return [secRoleInstanceList: SecRole.list()]
  }

  def cancel = {
    redirect(action: "list")
  }

  def create = {
    [secRoleInstance: new SecRole(params)]
  }

  def save = {
    def secRoleInstance = new SecRole(params)
    if (!secRoleInstance.save(flush: true)) {
      render(view: "create", model: [secRoleInstance: secRoleInstance])
      return
    }

    flash.message = message(code: 'default.created.message', args: [message(code: 'secRole.label', default: 'SecRole'), secRoleInstance.id])
    redirect(action: "show", id: secRoleInstance.id)
  }

  def show = {
    def secRoleInstance = SecRole.get(params.id)
    if (!secRoleInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])
      redirect(action: "list")
      return
    }

    [secRoleInstance: secRoleInstance]
  }

  def delete = {
    def secRoleInstance = SecRole.get(params.id)
    if (!secRoleInstance) {
      flash.message = message(code: 'default.not.found.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])
      redirect(action: "list")
      return
    }

    try {
      secRoleInstance.delete(flush: true)
      flash.message = message(code: 'default.deleted.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])
      redirect(action: "list")
    }
    catch (DataIntegrityViolationException e) {
      flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'secRole.label', default: 'SecRole'), params.id])
      redirect(action: "show", id: params.id)
    }
  }
}
