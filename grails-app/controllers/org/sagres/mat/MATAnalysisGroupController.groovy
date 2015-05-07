package org.sagres.mat

class MATAnalysisGroupController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [MATAnalysisGroupInstanceList: MATAnalysisGroup.list(params), MATAnalysisGroupInstanceTotal: MATAnalysisGroup.count()]
    }

    def create = {
        def MATAnalysisGroupInstance = new MATAnalysisGroup()
        MATAnalysisGroupInstance.properties = params
        return [MATAnalysisGroupInstance: MATAnalysisGroupInstance]
    }

    def save = {
        def MATAnalysisGroupInstance = new MATAnalysisGroup(params)
        if (MATAnalysisGroupInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), MATAnalysisGroupInstance.id])}"
            redirect(action: "show", id: MATAnalysisGroupInstance.id)
        }
        else {
            render(view: "create", model: [MATAnalysisGroupInstance: MATAnalysisGroupInstance])
        }
    }

    def show = {
        def MATAnalysisGroupInstance = MATAnalysisGroup.get(params.id)
        if (!MATAnalysisGroupInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
            [MATAnalysisGroupInstance: MATAnalysisGroupInstance]
        }
    }

    def edit = {
        def MATAnalysisGroupInstance = MATAnalysisGroup.get(params.id)
        if (!MATAnalysisGroupInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [MATAnalysisGroupInstance: MATAnalysisGroupInstance]
        }
    }

    def update = {
        def MATAnalysisGroupInstance = MATAnalysisGroup.get(params.id)
        if (MATAnalysisGroupInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (MATAnalysisGroupInstance.version > version) {
                    
                    MATAnalysisGroupInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup')] as Object[], "Another user has updated this MATAnalysisGroup while you were editing")
                    render(view: "edit", model: [MATAnalysisGroupInstance: MATAnalysisGroupInstance])
                    return
                }
            }
            MATAnalysisGroupInstance.properties = params
            if (!MATAnalysisGroupInstance.hasErrors() && MATAnalysisGroupInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), MATAnalysisGroupInstance.id])}"
                redirect(action: "show", id: MATAnalysisGroupInstance.id)
            }
            else {
                render(view: "edit", model: [MATAnalysisGroupInstance: MATAnalysisGroupInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def MATAnalysisGroupInstance = MATAnalysisGroup.get(params.id)
        if (MATAnalysisGroupInstance) {
            try {
                MATAnalysisGroupInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroup.label', default: 'MATAnalysisGroup'), params.id])}"
            redirect(action: "list")
        }
    }
}
