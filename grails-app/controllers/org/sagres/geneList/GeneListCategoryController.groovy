package org.sagres.geneList

class GeneListCategoryController {
	
	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	  def dataSource
	
	  def index = {
		redirect(action: "list", params: params)
	  }
	
	  def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[geneListCategoryInstanceList: GeneListCategory.list(params), geneListCategoryInstanceTotal: GeneListCategory.count()]
	  }
	
	  def create = {
		def geneListCategoryInstance = new GeneList()
		geneListCategoryInstance.properties = params
		return [geneListCategoryInstance: geneListCategoryInstance]
	  }
	  
	  def save = {
		  def geneListCategoryInstance = new GeneListCategory(params)
		  if (geneListCategoryInstance.save(flush: true)) {
			  flash.message = "${message(code: 'default.created.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), geneListCategoryInstance.name])}"
			  redirect(action: "show", id: geneListCategoryInstance.id)
		  }
		  else {
			  render(view: "create", model: [geneListCategoryInstance: geneListCategoryInstance])
		  }
	  }
  
	  def show = {
		  def geneListCategoryInstance = GeneListCategory.get(params.id)
		  if (!geneListCategoryInstance) {
			  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
			  redirect(action: "list")
		  }
		  else {
			  [geneListCategoryInstance: geneListCategoryInstance]
		  }
	  }
  
	  def edit = {
		  def geneListCategoryInstance = GeneListCategory.get(params.id)
		  if (!geneListCategoryInstance) {
			  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
			  redirect(action: "list")
		  }
		  else {
			  return [geneListCategoryInstance: geneListCategoryInstance]
		  }
	  }
  
	  def update = {
		  def geneListCategoryInstance = GeneListCategory.get(params.id)
		  if (geneListCategoryInstance) {
			  if (params.version) {
				  def version = params.version.toLong()
				  if (geneListCategoryInstance.version > version) {
					  
					  geneListCategoryInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'geneListCategory.label', default: 'GeneListCategory')] as Object[], "Another user has updated this GeneListCategory while you were editing")
					  render(view: "edit", model: [geneListCategoryInstance: geneListCategoryInstance])
					  return
				  }
			  }
			  geneListCategoryInstance.properties = params
			  if (!geneListCategoryInstance.hasErrors() && geneListCategoryInstance.save(flush: true)) {
				  flash.message = "${message(code: 'default.updated.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), geneListCategoryInstance.name])}"
				  redirect(action: "show", id: geneListCategoryInstance.id)
			  }
			  else {
				  render(view: "edit", model: [geneListCategoryInstance: geneListCategoryInstance])
			  }
		  }
		  else {
			  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
			  redirect(action: "list")
		  }
	  }
  
	  def delete = {
		  def geneListCategoryInstance = GeneListCategory.get(params.id)
		  if (geneListCategoryInstance) {
			  try {
				  geneListCategoryInstance.delete(flush: true)
				  flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
				  redirect(action: "list")
			  }
			  catch (org.springframework.dao.DataIntegrityViolationException e) {
				  flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
				  redirect(action: "show", id: params.id)
			  }
		  }
		  else {
			  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneListCategory.label', default: 'GeneListCategory'), params.id])}"
			  redirect(action: "list")
		  }
	  }
	  
	  def geneListCategorySelected = {
		  //println "params: " + params
		  def geneLists = []
		  if (params.id != "null") {
		  	  geneLists = GeneListCategory.findById(params.id).geneLists
		  }
		  // this creates a whole <select></select> group - push it into the named div with the jquery.ajax 'success' function
		  render g.select(optionKey: 'id', optionValue: 'name', from: geneLists, class: 'large', id: 'geneListSelect',
			  			name: "geneListSelect", noSelection: [null:'-select a gene list-'], onchange: "changeGeneList();") 
	  }
	
}
