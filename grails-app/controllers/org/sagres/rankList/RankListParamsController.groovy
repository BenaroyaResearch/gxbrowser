package org.sagres.rankList

import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class RankListParamsController {

	def dm3Service
	def springSecurityService
    def mongoDataService

    static allowedMethods = [save: "POST", update: "POST", delete: "POST"]



    def index = {
        redirect(action: "list", params: params)
    }

    def list = {
        params.max = Math.min(params.max ? params.int('max') : 10, 100)
        [rankListParamsInstanceList: RankListParams.list(params), rankListParamsInstanceTotal: RankListParams.count()]
    }

    def create = {
        def rankListParamsInstance = new RankListParams()
			  def dbSampleSets = dm3Service.availableSampleSets
        rankListParamsInstance.properties = params
        return [rankListParamsInstance: rankListParamsInstance, sampleSets: dbSampleSets]
    }

		def selectRankListComparison = {
			def rankListParamsInstance = RankListParams.findById(params.id)
			  def groupsInSet = dm3Service.getGroupsInSetForRanking(rankListParamsInstance.sampleSetGroupSetId)
			  return [id: params.id, groups:groupsInSet, rankListParamsInstance: rankListParamsInstance]
		}

	  def runRankList = {
			def rankListParamsInstance = RankListParams.findById(params.id)
			def user = springSecurityService.currentUser
			rankListParamsInstance.comparisons.clear()
			def sets = dm3Service.getAvailableSampleSets()
			def setName = sets.get(rankListParamsInstance.sampleSetId)
			rankListParamsInstance.sampleSetName=setName
			rankListParamsInstance.save()
			int count = 0
			params.each {
				String name = it.key
				String value = it.value
				if (name.startsWith("comp_") && value.equalsIgnoreCase("on")) {
					def parts = name.split('_')
					def group1 = parts[1]
					def group1Name = dm3Service.getGroupNameFromGroupId(group1)
					def group2 = parts[2]
					def group2Name = dm3Service.getGroupNameFromGroupId(group2)

					def rlc = new RankListComparison(groupOneId: group1, groupOneName: group1Name, groupTwoId: group2, groupTwoName: group2Name)
					rankListParamsInstance.addToComparisons(rlc)
					count++;
				}
			}
			if (rankListParamsInstance.save(flush: true)) {
				dm3Service.runRankList(params.id, user)
				flash.message = "${message(code: 'default.created.message', args: [message(code: 'RankListparams.label', default: 'RankListParams'), rankListParamsInstance.id])}"
				redirect(action: "show", id: rankListParamsInstance.id)
			} else {
				render(view: "create", model: [rankListParamsInstance: rankListParamsInstance])
			}
		}

    def saveSampleSelection = {
        def rankListParamsInstance = new RankListParams(params)
				Date runDate = new Date();
				rankListParamsInstance.runDate=runDate
				def user = springSecurityService.currentUser
				rankListParamsInstance.userName = user?.username
        if (rankListParamsInstance.save(flush: true)) {
            redirect(action: "selectRankListComparison", id: rankListParamsInstance.id, sampleSetGroupSetId: rankListParamsInstance.sampleSetGroupSetId, rankListParamsInstance:rankListParamsInstance)
        }
        else {
            render(view: "create", model: [rankListParamsInstance: rankListParamsInstance])
        }
    }

    def show = {
        def rankListParamsInstance = RankListParams.get(params.id)
        if (!rankListParamsInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
            redirect(action: "list")
        }
        else {
            [rankListParamsInstance: rankListParamsInstance]
        }
    }

    def edit = {
        def rankListParamsInstance = RankListParams.get(params.id)
        if (!rankListParamsInstance) {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
            redirect(action: "list")
        }
        else {
            return [rankListParamsInstance: rankListParamsInstance]
        }
    }

    def update = {
        def rankListParamsInstance = RankListParams.get(params.id)
        if (rankListParamsInstance) {
            if (params.version) {
                def version = params.version.toLong()
                if (rankListParamsInstance.version > version) {
                    
                    rankListParamsInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'rankListParams.label', default: 'RankListParams')] as Object[], "Another user has updated this RankListParams while you were editing")
                    render(view: "edit", model: [rankListParamsInstance: rankListParamsInstance])
                    return
                }
            }
            rankListParamsInstance.properties = params
            if (!rankListParamsInstance.hasErrors() && rankListParamsInstance.save(flush: true)) {
                flash.message = "${message(code: 'default.updated.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), rankListParamsInstance.id])}"
                redirect(action: "show", id: rankListParamsInstance.id)
            }
            else {
                render(view: "edit", model: [rankListParamsInstance: rankListParamsInstance])
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
            redirect(action: "list")
        }
    }

    def delete = {
        def rankListParamsInstance = RankListParams.get(params.id)
        if (rankListParamsInstance) {
            try {
                rankListParamsInstance.delete(flush: true)
                flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
                redirect(action: "list")
            }
            catch (org.springframework.dao.DataIntegrityViolationException e) {
                flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
                redirect(action: "show", id: params.id)
            }
        }
        else {
            flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'rankListParams.label', default: 'RankListParams'), params.id])}"
            redirect(action: "list")
        }
    }
}
