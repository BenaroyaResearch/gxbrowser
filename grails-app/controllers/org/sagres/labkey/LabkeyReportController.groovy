package org.sagres.labkey

import grails.converters.JSON

class LabkeyReportController {

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
		def labkeyReportService

    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [labkeyReportInstanceList: LabkeyReport.list(params), labkeyReportInstanceTotal: LabkeyReport.count()]
    }

	def loadTrialShareGroups = {
		  def tsgroups = labkeyReportService.loadTrialShareGroups()
			render tsgroups
	}


    def create = {
        def labkeyReportInstance = new LabkeyReport()
				def sampleSets = labkeyReportService.availableSampleSets
        labkeyReportInstance.properties = params
        return [labkeyReportInstance: labkeyReportInstance, sampleSets: sampleSets]
    }

    def save = {
        def labkeyReportInstance = new LabkeyReport(params)
        if (labkeyReportInstance.save(flush: true)) {
            flash.message = "${message(code: 'default.created.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), labkeyReportInstance.id])}"
            redirect(action: "show", id: labkeyReportInstance.id)
        }
        else {
            render(view: "create", model: [labkeyReportInstance: labkeyReportInstance])
        }
    }

    def show = {
        def labkeyReportInstance = LabkeyReport.get(params.id)
        if (!labkeyReportInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
            redirect(action: "list")
        }
        else {
            [labkeyReportInstance: labkeyReportInstance]
        }
    }

    def edit = {
        def labkeyReportInstance = LabkeyReport.get(params.id)
        if (!labkeyReportInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [labkeyReportInstance: labkeyReportInstance]
        }
    }

    def update = {
        def labkeyReportInstance = LabkeyReport.get(params.id)
        if (labkeyReportInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (labkeyReportInstance.version > version) {
                    
                    labkeyReportInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'labkeyReport.label', default: 'LabkeyReport')] as Object[], "Another user has updated this LabkeyReport while you were editing")
                    render(view: "edit", model: [labkeyReportInstance: labkeyReportInstance])
                    return
                }
            }
            labkeyReportInstance.properties = params
            if (!labkeyReportInstance.hasErrors() && labkeyReportInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), labkeyReportInstance.id])}"
                redirect(action: "show", id: labkeyReportInstance.id)
            }
            else {
                render(view: "edit", model: [labkeyReportInstance: labkeyReportInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def labkeyReportInstance = LabkeyReport.get(params.id)
        if (labkeyReportInstance) {
            try {
                labkeyReportInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'labkeyReport.label', default: 'LabkeyReport'), params.id])}"
            redirect(action: "list")
        }
    }


		def runReports = {
			def results = [:]
			def reports = LabkeyReport.findAll()
				reports.each { LabkeyReport report ->
					if (report.enabled == null || report.enabled == true) {
						def statusCode = labkeyReportService.loadLabkeyReport(report.id)
						results.put(report.id, "${report.reportName} for sample set:${report.sampleSetId} : ${statusCode}")
						println "Completed gathering labkey report: ${report.id}"
					}
				}
			render results
		}

	def runReport = {
			def lkrepId = Long.parseLong(params.id)
			def statusCode = labkeyReportService.loadLabkeyReport(lkrepId)
			render "Status Code for request ${statusCode}"

		}
}
