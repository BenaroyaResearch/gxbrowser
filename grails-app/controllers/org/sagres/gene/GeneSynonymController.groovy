package org.sagres.gene

import groovy.sql.Sql
import common.GeneInfo

class GeneSynonymController
{
	def scaffold = true

	// inject the main datasource
	def dataSource

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[geneSynonymInstanceList: GeneSynonym.list(params), geneSynonymInstanceTotal: GeneSynonym.count()]
	}

	def create = {
		def geneSynonymInstance = new GeneSynonym()
		geneSynonymInstance.properties = params
		return [geneSynonymInstance: geneSynonymInstance]
	}

	def save = {
		def geneSynonymInstance = new GeneSynonym(params)
		if (geneSynonymInstance.save(flush: true))
		{
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), geneSynonymInstance.id])}"
			redirect(action: "show", id: geneSynonymInstance.id)
		}
		else
		{
			render(view: "create", model: [geneSynonymInstance: geneSynonymInstance])
		}
	}

	def show = {
		def geneSynonymInstance = GeneSynonym.get(params.id)
		if (!geneSynonymInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[geneSynonymInstance: geneSynonymInstance]
		}
	}

	def edit = {
		def geneSynonymInstance = GeneSynonym.get(params.id)
		if (!geneSynonymInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			return [geneSynonymInstance: geneSynonymInstance]
		}
	}

	def update = {
		def geneSynonymInstance = GeneSynonym.get(params.id)
		if (geneSynonymInstance)
		{
			if (params.version)
			{
				def version = params.version.toLong()
				if (geneSynonymInstance.version > version)
				{

					geneSynonymInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'geneSynonym.label', default: 'GeneSynonym')] as Object[], "Another user has updated this GeneSynonym while you were editing")
					render(view: "edit", model: [geneSynonymInstance: geneSynonymInstance])
					return
				}
			}
			geneSynonymInstance.properties = params
			if (!geneSynonymInstance.hasErrors() && geneSynonymInstance.save(flush: true))
			{
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), geneSynonymInstance.id])}"
				redirect(action: "show", id: geneSynonymInstance.id)
			}
			else
			{
				render(view: "edit", model: [geneSynonymInstance: geneSynonymInstance])
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
			redirect(action: "list")
		}
	}

	def delete = {
		def geneSynonymInstance = GeneSynonym.get(params.id)
		if (geneSynonymInstance)
		{
			try
			{
				geneSynonymInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e)
			{
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneSynonym.label', default: 'GeneSynonym'), params.id])}"
			redirect(action: "list")
		}
	}

	/** this will refresh the entire list, by deleting its contents and reparsing the sysnonym info from gene_info */
	def refreshSynonymList =
	{
		def sql = new Sql(dataSource)
		sql.execute("delete from gene_synonym")

//		def geneList = GeneInfo.findA
		GeneInfo.findAll().each { gene ->
			def synonymList = gene.synonyms
			synonymList.tokenize('|').each { synonym ->
				def geneSynonym = new GeneSynonym()
				geneSynonym.gene = gene
				geneSynonym.synonym = synonym
				geneSynonym.save()
			}
		}
	}
}
