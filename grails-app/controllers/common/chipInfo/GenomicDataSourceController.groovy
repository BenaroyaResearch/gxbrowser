package common.chipInfo

class GenomicDataSourceController {

	def scaffold = true

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [genomicDataSourceInstanceList: GenomicDataSource.list(params), genomicDataSourceInstanceTotal: GenomicDataSource.count()]
    }

    def create = {
        def genomicDataSourceInstance = new GenomicDataSource()
        genomicDataSourceInstance.properties = params
        return [genomicDataSourceInstance: genomicDataSourceInstance]
    }

    def save = {
        def genomicDataSourceInstance = new GenomicDataSource(params)
        if (genomicDataSourceInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), genomicDataSourceInstance.id])}"
            redirect(action: "show", id: genomicDataSourceInstance.id)
        }
        else {
            render(view: "create", model: [genomicDataSourceInstance: genomicDataSourceInstance])
        }
    }

    def show = {
        def genomicDataSourceInstance = GenomicDataSource.get(params.id)
        if (!genomicDataSourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
            redirect(action: "list")
        }
        else {
            [genomicDataSourceInstance: genomicDataSourceInstance]
        }
    }

    def edit = {
        def genomicDataSourceInstance = GenomicDataSource.get(params.id)
        if (!genomicDataSourceInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [genomicDataSourceInstance: genomicDataSourceInstance]
        }
    }

    def update = {
        def genomicDataSourceInstance = GenomicDataSource.get(params.id)
        if (genomicDataSourceInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (genomicDataSourceInstance.version > version) {
                    
                    genomicDataSourceInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'genomicDataSource.label', default: 'GenomicDataSource')] as Object[], "Another user has updated this GenomicDataSource while you were editing")
                    render(view: "edit", model: [genomicDataSourceInstance: genomicDataSourceInstance])
                    return
                }
            }
            genomicDataSourceInstance.properties = params
            if (!genomicDataSourceInstance.hasErrors() && genomicDataSourceInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), genomicDataSourceInstance.id])}"
                redirect(action: "show", id: genomicDataSourceInstance.id)
            }
            else {
                render(view: "edit", model: [genomicDataSourceInstance: genomicDataSourceInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def genomicDataSourceInstance = GenomicDataSource.get(params.id)
        if (genomicDataSourceInstance) {
            try {
                genomicDataSourceInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'genomicDataSource.label', default: 'GenomicDataSource'), params.id])}"
            redirect(action: "list")
        }
    }
}
