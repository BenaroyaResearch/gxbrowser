package common

import grails.converters.JSON
import groovy.sql.Sql
import javax.swing.text.html.HTMLDocument.HTMLReader
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub

class GeneInfoController
{
	def scaffold = true
	def dataSource
	def ncbiQueryService
	def geneQueryService
	def mongoDataService

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
//		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		params.max = 100
		params.pageTitle = "GXB: Gene Info"
		[ params: params ]
	}


	def queryGeneInfo =	{
		def geneInfo = ["Description":"--", "Summary":"--"]
		def pubmedInfo = ["numArticles":0]
		try {
			def genes = ncbiQueryService.queryGene(params.geneID)
			if (genes) {
				geneInfo.putAll(genes)
			}
		} catch (Exception e) {
			//e.printStackTrace()
			println "Exception calling ncbiQueryService (queryGene) ${e.toString()}"
			geneInfo.put("Description", GeneInfo.findByGeneID(params.geneID)?.name ?: "--")
		}
		try {
			//pubmedInfo.putAll(ncbiQueryService.searchGene(params.geneSymbol))
			List ids = ncbiQueryService.getGeneLinks(params.geneID)
			if (ids?.size() > 0) {
				pubmedInfo.numArticles = ids.size()
				List articles = ncbiQueryService.getArticles(ids, 25)
				if (articles) {
					pubmedInfo.articles = articles
				}
			}
		} catch (Exception e) {
			println "Exception calling ncbiQueryService (getGeneLinks+getArticles) ${e.toString()}"
		}
		def jsonResults = [ "entrez" : geneInfo, geneId:params.geneID, pubmed:pubmedInfo ]
		render jsonResults as JSON
	}

	def queryGeneLinks = {
		int limit = params.int("limit") ?: 0
		List ids = ncbiQueryService.getGeneLinks(params.geneID)
		Map model = [:]
		if (ids.size() > 0) {
			model.numArticles = ids.size()
			List articles = ncbiQueryService.getArticles(ids, limit)
			if (articles) {
				model.articles = articles
				render model as JSON
			}
		}
		render ""
	}

//  def searchGene = {
//    Map results = ncbiQueryService.searchGene(params.geneSymbol)
//    render results as JSON
//  }

	def queryGeneList =
	{
		def htmlReturn = geneQueryService.queryGeneListHTML(params)

		def jsonResults = [ "gl": htmlReturn ]
		render jsonResults as JSON
	}

	def create = {
		def geneInfoInstance = new GeneInfo()
		geneInfoInstance.properties = params
		return [geneInfoInstance: geneInfoInstance]
	}

	def save = {
		def geneInfoInstance = new GeneInfo(params)
		if (geneInfoInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), geneInfoInstance.id])}"
			redirect(action: "show", id: geneInfoInstance.id)
		}
		else
		{
			render(view: "create", model: [geneInfoInstance: geneInfoInstance])
		}
	}

	def show = {
		def geneInfoInstance = GeneInfo.get(params.id)
		if (!geneInfoInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[geneInfoInstance: geneInfoInstance]
		}
	}

	def edit = {
		def geneInfoInstance = GeneInfo.get(params.id)
		if (!geneInfoInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [geneInfoInstance: geneInfoInstance]
		}
	}

	def update = {
		def geneInfoInstance = GeneInfo.get(params.id)
		if (geneInfoInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (geneInfoInstance.version > version)
				{

					geneInfoInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'geneInfo.label', default: 'GeneInfo')] as Object[], "Another user has updated this GeneInfo while you were editing")
					render(view: "edit", model: [geneInfoInstance: geneInfoInstance])
					return
				}
			}
			geneInfoInstance.properties = params
			if (!geneInfoInstance.hasErrors() && geneInfoInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), geneInfoInstance.id])}"
				redirect(action: "show", id: geneInfoInstance.id)
			}
			else
			{
				render(view: "edit", model: [geneInfoInstance: geneInfoInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def geneInfoInstance = GeneInfo.get(params.id)
		if (geneInfoInstance)
		{
			try
			{
				geneInfoInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneInfo.label', default: 'GeneInfo'), params.id])}"
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

	def importGeneInfo =
	{
		def nImported = importAndParseFile(grailsApplication.config.ncbiFiles.baseDir + "/gene_info")
		return [ nImported: nImported ]
	}

	def importAndParseFile(String fullPath)
	{
		if (fullPath == null)
			fullPath = grailsApplication.config.ncbiFiles.baseDir + "/gene_info"

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
						def gene = new GeneInfo()
						gene.taxID = rowItems[0].toInteger()
						gene.geneID = rowItems[1].toInteger()
						gene.symbol = rowItems[2]
						gene.synonyms = rowItems[4]
						gene.dbXref = rowItems[5]
						gene.name = rowItems[8]
						gene.save(flush:true)
						nImported++
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
