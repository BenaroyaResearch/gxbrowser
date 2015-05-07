package common.chipInfo

class ChipDataController
{
	def scaffold = true

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[chipDataInstanceList: ChipData.list(params), chipDataInstanceTotal: ChipData.count()]
	}

	def create = {
		def chipDataInstance = new ChipData()
		chipDataInstance.properties = params
		return [chipDataInstance: chipDataInstance]
	}

	def save = {
		def chipDataInstance = new ChipData(params)
		if (chipDataInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'chipData.label', default: 'ChipData'), chipDataInstance.id])}"
			redirect(action: "show", id: chipDataInstance.id)
		}
		else
		{
			render(view: "create", model: [chipDataInstance: chipDataInstance])
		}
	}

	def show = {
		def chipDataInstance = ChipData.get(params.id)
		if (!chipDataInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[chipDataInstance: chipDataInstance]
		}
	}

	def edit = {
		def chipDataInstance = ChipData.get(params.id)
		if (!chipDataInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [chipDataInstance: chipDataInstance]
		}
	}

	def update = {
		def chipDataInstance = ChipData.get(params.id)
		if (chipDataInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (chipDataInstance.version > version)
				{

					chipDataInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'chipData.label', default: 'ChipData')] as Object[], "Another user has updated this ChipData while you were editing")
					render(view: "edit", model: [chipDataInstance: chipDataInstance])
					return
				}
			}
			chipDataInstance.properties = params
			if (!chipDataInstance.hasErrors() && chipDataInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'chipData.label', default: 'ChipData'), chipDataInstance.id])}"
				redirect(action: "show", id: chipDataInstance.id)
			}
			else
			{
				render(view: "edit", model: [chipDataInstance: chipDataInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def chipDataInstance = ChipData.get(params.id)
		if (chipDataInstance)
		{
			try
			{
				chipDataInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipData.label', default: 'ChipData'), params.id])}"
			redirect(action: "list")
		}
	}
}
