package org.sagres.mat

import org.springframework.dao.DataIntegrityViolationException
import common.SecRole
import common.SecUser

class MetaCatController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	
	def springSecurityService //injected
	def matPlotService

    def index() {
        redirect(action: "list", params: params)
    }

    def list(Integer max) {
        params.max = Math.min(max ?: 10, 100)
        [metaCatInstanceList: MetaCat.list(params), metaCatInstanceTotal: MetaCat.count()]
    }

    def create() {
		SecUser currentUser = springSecurityService.currentUser
		def analysisInstanceList  = Analysis.createCriteria().list()
		{
			and
			{
				eq('metacatPublished', 1)
				isNull('flagDelete')
			}
		}

        [metaCatInstance: new MetaCat(params), analysisInstanceList: analysisInstanceList, currentUser: currentUser]
    }

    def save() {
		SecUser currentUser = springSecurityService.currentUser
        def metaCatInstance = new MetaCat(params)
		metaCatInstance.user = currentUser;
        if (!metaCatInstance.save(flush: true)) {
            render(view: "create", model: [metaCatInstance: metaCatInstance])
            return
        }

		metaCatInstance.analyses = []
		params.each {
			if (it.key.startsWith("analysis_"))
			  metaCatInstance.analyses << Analysis.get((it.key - "analysis_") as Integer)
		}

		println "${metaCatInstance.analyses.size()} analyses for metaCat instance ${metaCatInstance.id}"
		
		if (metaCatInstance.analyses.size() > 0)
		{
			def counts = matPlotService.getSamplesCount(metaCatInstance)
			metaCatInstance.noSamples = counts.totalSamples
			metaCatInstance.noCases = counts.totalCases
		}
		else
		{
			metaCatInstance.noSamples = 0
			metaCatInstance.noCases = 0
		}
		
        flash.message = message(code: 'default.created.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), metaCatInstance.id])
        redirect(action: "show", id: metaCatInstance.id)
    }

    def show(Long id) {
		SecUser currentUser = springSecurityService.currentUser
        def metaCatInstance = MetaCat.get(id)
        if (!metaCatInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "list")
            return
        }

        [metaCatInstance: metaCatInstance, currentUser: currentUser]
    }

    def edit(Long id) {
		SecUser currentUser = springSecurityService.currentUser
        def metaCatInstance = MetaCat.get(id)
        if (!metaCatInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "list")
            return
        }

		def analysisInstanceList  = Analysis.createCriteria().list()
		{
			and
			{
				eq('metacatPublished', 1)
				isNull('flagDelete')
			}
		}

        [metaCatInstance: metaCatInstance, analysisInstanceList: analysisInstanceList, currentUser: currentUser]
    }

    def update(Long id, Long version) {
        def metaCatInstance = MetaCat.get(id)
        if (!metaCatInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "list")
            return
        }

        if (version != null) {
            if (metaCatInstance.version > version) {
                metaCatInstance.errors.rejectValue("version", "default.optimistic.locking.failure",
                          [message(code: 'metaCat.label', default: 'MetaCat')] as Object[],
                          "Another user has updated this MetaCat while you were editing")
                render(view: "edit", model: [metaCatInstance: metaCatInstance])
                return
            }
        }

		metaCatInstance.analyses = []
		params.each {
			if (it.key.startsWith("analysis_"))
			  metaCatInstance.analyses << Analysis.get((it.key - "analysis_") as Integer)
		}

		println "${metaCatInstance.analyses.size()} analyses for metaCat instance ${metaCatInstance.id}"
		
		if (metaCatInstance.analyses.size() > 0)
		{
			def counts = matPlotService.getSamplesCount(metaCatInstance)
			metaCatInstance.noSamples = counts.totalSamples
			metaCatInstance.noCases = counts.totalCases
		}
		else
		{
			metaCatInstance.noSamples = 0
			metaCatInstance.noCases = 0
		}

        metaCatInstance.properties = params
		
        if (!metaCatInstance.save(flush: true)) {
            render(view: "edit", model: [metaCatInstance: metaCatInstance])
            return
        }

		def metaCatSaved = MetaCat.get(id)
		
        flash.message = message(code: 'default.updated.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), metaCatInstance.id])
        redirect(action: "show", id: metaCatInstance.id)
    }

    def delete(Long id) {
        def metaCatInstance = MetaCat.get(id)
        if (!metaCatInstance) {
            flash.message = message(code: 'default.not.found.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "list")
            return
        }

        try {
            metaCatInstance.delete(flush: true)
            flash.message = message(code: 'default.deleted.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "list")
        }
        catch (DataIntegrityViolationException e) {
            flash.message = message(code: 'default.not.deleted.message', args: [message(code: 'metaCat.label', default: 'MetaCat'), id])
            redirect(action: "show", id: id)
        }
    }
}
