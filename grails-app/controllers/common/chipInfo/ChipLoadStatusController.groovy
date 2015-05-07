package common.chipInfo

class ChipLoadStatusController {

	def scaffold = true

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [chipLoadStatusInstanceList: ChipLoadStatus.list(params), chipLoadStatusInstanceTotal: ChipLoadStatus.count()]
    }

    def create = {
        def chipLoadStatusInstance = new ChipLoadStatus()
        chipLoadStatusInstance.properties = params
        return [chipLoadStatusInstance: chipLoadStatusInstance]
    }

    def save = {
        def chipLoadStatusInstance = new ChipLoadStatus(params)
        if (chipLoadStatusInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), chipLoadStatusInstance.id])}"
            redirect(action: "show", id: chipLoadStatusInstance.id)
        }
        else {
            render(view: "create", model: [chipLoadStatusInstance: chipLoadStatusInstance])
        }
    }

    def show = {
        def chipLoadStatusInstance = ChipLoadStatus.get(params.id)
        if (!chipLoadStatusInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
            redirect(action: "list")
        }
        else {
            [chipLoadStatusInstance: chipLoadStatusInstance]
        }
    }

    def edit = {
        def chipLoadStatusInstance = ChipLoadStatus.get(params.id)
        if (!chipLoadStatusInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [chipLoadStatusInstance: chipLoadStatusInstance]
        }
    }

    def update = {
        def chipLoadStatusInstance = ChipLoadStatus.get(params.id)
        if (chipLoadStatusInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (chipLoadStatusInstance.version > version) {
                    
                    chipLoadStatusInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus')] as Object[], "Another user has updated this ChipLoadStatus while you were editing")
                    render(view: "edit", model: [chipLoadStatusInstance: chipLoadStatusInstance])
                    return
                }
            }
            chipLoadStatusInstance.properties = params
            if (!chipLoadStatusInstance.hasErrors() && chipLoadStatusInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), chipLoadStatusInstance.id])}"
                redirect(action: "show", id: chipLoadStatusInstance.id)
            }
            else {
                render(view: "edit", model: [chipLoadStatusInstance: chipLoadStatusInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def chipLoadStatusInstance = ChipLoadStatus.get(params.id)
        if (chipLoadStatusInstance) {
            try {
                chipLoadStatusInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipLoadStatus.label', default: 'ChipLoadStatus'), params.id])}"
            redirect(action: "list")
        }
    }
}
