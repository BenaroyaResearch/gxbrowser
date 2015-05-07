package common.chipInfo

class ChipTypeController
{
	def scaffold = true

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[chipTypeInstanceList: ChipType.list(params), chipTypeInstanceTotal: ChipType.count()]
	}

	def create = {
		def chipTypeInstance = new ChipType()
		chipTypeInstance.properties = params
		return [chipTypeInstance: chipTypeInstance]
	}

	def save = {
		def chipTypeInstance = new ChipType(params)
		if (chipTypeInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'chipType.label', default: 'ChipType'), chipTypeInstance.id])}"
			redirect(action: "show", id: chipTypeInstance.id)
		}
		else
		{
			render(view: "create", model: [chipTypeInstance: chipTypeInstance])
		}
	}

	def show = {
		def chipTypeInstance = ChipType.get(params.id)
		if (!chipTypeInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[chipTypeInstance: chipTypeInstance]
		}
	}

	def edit = {
		def chipTypeInstance = ChipType.get(params.id)
		if (!chipTypeInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [chipTypeInstance: chipTypeInstance]
		}
	}

	def update = {
		def chipTypeInstance = ChipType.get(params.id)
		if (chipTypeInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (chipTypeInstance.version > version)
				{

					chipTypeInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'chipType.label', default: 'ChipType')] as Object[], "Another user has updated this ChipType while you were editing")
					render(view: "edit", model: [chipTypeInstance: chipTypeInstance])
					return
				}
			}
			chipTypeInstance.properties = params
			if (!chipTypeInstance.hasErrors() && chipTypeInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'chipType.label', default: 'ChipType'), chipTypeInstance.id])}"
				redirect(action: "show", id: chipTypeInstance.id)
			}
			else
			{
				render(view: "edit", model: [chipTypeInstance: chipTypeInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def chipTypeInstance = ChipType.get(params.id)
		if (chipTypeInstance)
		{
			try
			{
				chipTypeInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipType.label', default: 'ChipType'), params.id])}"
			redirect(action: "list")
		}
	}
}
