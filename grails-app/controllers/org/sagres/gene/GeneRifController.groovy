package org.sagres.gene

import common.GeneInfo

class GeneRifController
{

	def scaffold = true

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[geneRifInstanceList: GeneRif.list(params), geneRifInstanceTotal: GeneRif.count()]
	}

	def create = {
		def geneRifInstance = new GeneRif()
		geneRifInstance.properties = params
		return [geneRifInstance: geneRifInstance]
	}

	def save = {
		def geneRifInstance = new GeneRif(params)
		if (geneRifInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), geneRifInstance.id])}"
			redirect(action: "show", id: geneRifInstance.id)
		}
		else
		{
			render(view: "create", model: [geneRifInstance: geneRifInstance])
		}
	}

	def show = {
		def geneRifInstance = GeneRif.get(params.id)
		if (!geneRifInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[geneRifInstance: geneRifInstance]
		}
	}

	def edit = {
		def geneRifInstance = GeneRif.get(params.id)
		if (!geneRifInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [geneRifInstance: geneRifInstance]
		}
	}

	def update = {
		def geneRifInstance = GeneRif.get(params.id)
		if (geneRifInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (geneRifInstance.version > version)
				{

					geneRifInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'geneRif.label', default: 'GeneRif')] as Object[], "Another user has updated this GeneRif while you were editing")
					render(view: "edit", model: [geneRifInstance: geneRifInstance])
					return
				}
			}
			geneRifInstance.properties = params
			if (!geneRifInstance.hasErrors() && geneRifInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), geneRifInstance.id])}"
				redirect(action: "show", id: geneRifInstance.id)
			}
			else
			{
				render(view: "edit", model: [geneRifInstance: geneRifInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def geneRifInstance = GeneRif.get(params.id)
		if (geneRifInstance)
		{
			try
			{
				geneRifInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneRif.label', default: 'GeneRif'), params.id])}"
			redirect(action: "list")
		}
	}

	def upload =
	{
		render(view: "upload")
	}

	def importFile =
	{
		def theFile = request.getFile("theFile")
		if (theFile && theFile.empty == false)
		{
			println "parse and load the file"
			// move the file into the uploads directory (how could we upload directly to it?
			def fileName = theFile.getOriginalFilename()
			println fileName

			def newFilePath = grailsApplication.config.ncbiFiles.baseDir + "/" + fileName
			theFile.transferTo(new File(newFilePath))

			importAndParseFile(newFilePath)
		}
		else
		{
			println "no file to parse"
		}
	}

	def importGeneRIF =
	{
		def nImported = importAndParseFile(grailsApplication.config.ncbiFiles.baseDir + "/generifs_basic")
		return [ nImported: nImported ]
	}

	def importAndParseFile(String fullPath)
	{
		if (fullPath == null)
			fullPath = grailsApplication.config.ncbiFiles.baseDir + "/generifs_basic"

		println "import and parse: " + fullPath
		def importFile = new File(fullPath)
		int nRows = 0
		int nImported = 0

		try
		{
			importFile.eachLine { line ->
				if (nRows++ > 0)
				{
					def rowItems = line.tokenize('\t')
					if (rowItems[0] == "9606")
					{
						try
						{
							def geneRIF = new GeneRif()
							geneRIF.geneID = rowItems[1].toInteger()
							geneRIF.pubmedID = rowItems[2].toString()
							geneRIF.timeLastUpdated = rowItems[3].toString()
//							geneRIF.geneRIF = rowItems[4].toString()
							geneRIF.save()
							nImported++
						}
						catch (Exception e)
						{
							println e.getMessage()
							println e.getStackTrace()
							println "stop here"
						}
					}
				}

/*
				if (nImported > 10)
					throw new Exception("break");
*/
			}
		}
		catch (Exception e) { println e.getMessage() }

		return nImported
	}

}
