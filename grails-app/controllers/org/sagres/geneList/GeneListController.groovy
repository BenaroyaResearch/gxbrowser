package org.sagres.geneList

import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.sagres.importer.TextTable
import org.sagres.importer.TextTableSeparator
import common.GeneInfo
import groovy.sql.Sql
import common.SecUser
import common.SecRole
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class GeneListController {

  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

  def dataSource
  def mongoDataService
  def springSecurityService



  def index = {
    redirect(action: "list", params: params)
  }

  def list = {
	def editable = false
	def user
	if (loggedIn)
	{
		  user = springSecurityService.currentUser
		  editable = user && user.authorities.contains(SecRole.findByAuthority('ROLE_GENELISTS'))
	}
    params.max = Math.min(params.max ? params.int('max') : 10, 100)
    [geneListInstanceList: GeneList.findAll(), geneListInstanceTotal: GeneList.count(), user: user, editable: editable]
  }

  def create = {
	def editable = false
	def user
	if (loggedIn)
	{
		user = springSecurityService.currentUser
		editable = user && user.authorities.contains(SecRole.findByAuthority('ROLE_GENELISTS'))
	}
  
    def geneListInstance = new GeneList()
    geneListInstance.properties = params
    return [geneListInstance: geneListInstance, user: user, editable: editable]
  }

  def save = {
    CommonsMultipartFile geneListFile = params.geneListFile
    def listFile = cacheTempFile(geneListFile)
    def geneList = new GeneList(params)
    if (geneList.save()) {
      if (listFile) {
        def unimportedGenes = []
        listFile.eachLine { text, line ->
          List<String> values = TextTable.splitRow(text, TextTableSeparator.TSV)
          if (!text.startsWith("#")) {
            def geneId = 0
			def symbol = values[0]
			
			// Is the first value an integer, then assume it's a geneId
			try {
					geneId = Long.parseLong(values[0])
			} catch (Exception e) {
					// Wasn't a number
			}
			
			// If we have more than two values, the third one is a symbol, otherwise assume the first one is a symbol.
			if (values.size() > 2) {
				symbol = values[2]
			}

			if (geneId > 0) {
				if (GeneInfo.findByGeneID((int)geneId)) {
					new GeneListDetail(geneList:geneList, geneId:geneId).save()
				}
				else if (values.size() > 2)
				{
					def geneInfo = GeneInfo.findBySymbol(symbol)
					if (geneInfo)
					{
						new GeneListDetail(geneList:geneList, geneId:geneInfo.geneID.toLong()).save()
					}
					else
					{
						unimportedGenes.push("geneId: ${geneId}, symbol: ${symbol}")
					}
				}
				else
				{
					unimportedGenes.push("geneId: ${geneId}, symbol: 'unknown'")
				}
			} else {
              	def geneInfo = GeneInfo.findBySymbol(symbol)
				if (geneInfo)
				{
					new GeneListDetail(geneList:geneList, geneId:geneInfo.geneID.toLong()).save()
				}
				else
				{
					unimportedGenes.push("geneId: ${geneId}, symbol: ${symbol}")
				}
            }
          }
        }
        listFile.delete()
        // only proceed with saving module generation if successful import occurs
        flash.message = "Your gene list ${geneList.name} was successfully created. The following genes were not imported. ${unimportedGenes.join(";")}"
        redirect(action: "show", id:geneList.id)
      }
    }
    else
    {
      render(view: "create", model: [geneListInstance: geneList])
    }
  }

  private def File cacheTempFile(CommonsMultipartFile f)
  {
    if (f) {
      File tempVersionFile = new File(grailsApplication.config.fileUpload.baseDir.toString(), "temp/${f.originalFilename}".toString())
      if (!tempVersionFile.exists()) {
        tempVersionFile.mkdirs()
      }
      f.transferTo(tempVersionFile)
      return tempVersionFile
    }
  }

  def cancel = {
    redirect(action: "list")
  }

  def show = {
    def geneListInstance = GeneList.get(params.id)
    if (!geneListInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
      redirect(action: "list")
    }
    else {
      def geneListDetails = []
      Sql sql = Sql.newInstance(dataSource)
      def query = """SELECT gi.symbol 'symbol', gi.geneid 'geneId', gi.name 'name' FROM gene_list_detail d
        LEFT JOIN gene_info gi ON d.gene_id = gi.geneid
        WHERE d.gene_list_id = ${geneListInstance.id}
        ORDER BY gi.symbol""".toString()
      sql.eachRow(query) {
        geneListDetails.push([symbol:it.symbol, geneId:it.geneId, name:it.name])
      }
      sql.close()
      [geneListInstance: geneListInstance, geneListDetails:geneListDetails]
    }
  }

  def edit = {
	def editable = false
	def user
	if (loggedIn)
	 {
		  user = springSecurityService.currentUser
		  editable = user && user.authorities.contains(SecRole.findByAuthority('ROLE_GENELISTS'))
	 }
    def geneListInstance = GeneList.get(params.id)
    if (!geneListInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
      redirect(action: "list")
    }
    else {
      return [geneListInstance: geneListInstance, user: user, editable: editable, params: params]
    }
  }

  def update = {
	def editable = false
	def user
	if (loggedIn)
	{
		user = springSecurityService.currentUser
		editable = user && user.authorities.contains(SecRole.findByAuthority('ROLE_GENELISTS'))
	}
  
    def geneListInstance = GeneList.get(params.id)
    if (geneListInstance) {
      if (params.version) {
        def version = params.version.toLong()
        if (geneListInstance.version > version) {

          geneListInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'geneList.label', default: 'GeneList')] as Object[], "Another user has updated this GeneList while you were editing")
          render(view: "edit", model: [geneListInstance: geneListInstance, user: user, editable: editable, params: params])
          return
        }
      }
      geneListInstance.properties = params
      if (!geneListInstance.hasErrors() && geneListInstance.save(flush: true)) {
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'geneList.label', default: 'GeneList'), geneListInstance.name])}"
        redirect(action: "show", id: geneListInstance.id)
      }
      else {
        render(view: "edit", model: [geneListInstance: geneListInstance, user: user, editable: editable, params : params])
      }
    }
    else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
      redirect(action: "list")
    }
  }

  def delete = {
    def geneListInstance = GeneList.get(params.id)
    if (geneListInstance) {
      try {
        geneListInstance.delete(flush: true)
        flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
        redirect(action: "list")
      }
      catch (org.springframework.dao.DataIntegrityViolationException e) {
        flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
        redirect(action: "show", id: params.id)
      }
    }
    else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'geneList.label', default: 'GeneList'), params.id])}"
      redirect(action: "list")
    }
  }
}
