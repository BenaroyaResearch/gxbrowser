package org.sagres.sampleSet

import common.ArrayData
import grails.converters.JSON
import common.SecUser
import common.SecRole
import org.sagres.rankList.RankList
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import common.chipInfo.ChipType
import common.chipInfo.ChipsLoaded
import common.chipInfo.GenomicDataSource

class DatasetGroupSetController {

	static scaffold = DatasetGroupSet

	def sampleSetService
	def mongoDataService
	def tg2QueryService
	def springSecurityService
	def labkeyReportService
	def chartingDataService
	def grailsApplication

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {

		def datasetGroupSetList = [];

		def groupList = DatasetGroupSet.findAll("FROM DatasetGroupSet AS dgs LEFT JOIN dgs.sampleSet AS ss")

		groupList.each { gl ->
			datasetGroupSetList.push([datasetGroupSet: gl[0], sampleSet: gl[1]])
		}

		def model = [datasetGroupSetList: datasetGroupSetList, params: params]

		return model
	}

	def show = {
		def datasetGroupSet
		def sampleSet
		def datasetGroupSetInstance = [];

		def groupList = DatasetGroupSet.findAll("FROM DatasetGroupSet AS dgs LEFT JOIN dgs.sampleSet AS ss WHERE dgs.id=${params.id}")

		groupList.each { gl ->
			datasetGroupSet = gl[0]
			sampleSet = gl[1]
		}

		def model = [datasetGroupSetInstance: [datasetGroupSet: datasetGroupSet, sampleSet: sampleSet], params: params]

		return model
	}

	def create = {
		def sampleSet = SampleSet.get(params.id)
		def groupSet = new DatasetGroupSet(name: params.name, sampleSet: sampleSet)
		if (groupSet.save(flush: true)) {
			def defaultGroup = new DatasetGroup(name: grailsApplication.config.group.default, displayOrder: 1, groupSet: groupSet)
			defaultGroup.save(flush: true)

			// find all samples that have array data
			def samples = mongoDataService.getSampleIdsInSampleSet(sampleSet.id)
			if (samples) {
				def criteria = ArrayData.createCriteria()
				criteria.list {
					'in'("id", samples)
				}?.each {
					defaultGroup.addToGroupDetails(new DatasetGroupDetail(sample: it))
				}
				defaultGroup.save(flush: true)
			}

			groupSet.addToGroups(defaultGroup)

			groupSet.save(flush: true)

			flash.message = "A new group set, ${params.name}, has been created"
			redirect(action: "view", id: groupSet.id)
		}
		else {
			redirect(controller: "sampleSet", action: "show", id: sampleSet.id, params: [errors: groupSet.errors.fieldErrors.collect { it.codes[3] }])
		}
		return null
	}

	def createGroup = {
		def sampleSet = SampleSet.get(params.id)
		def groupSet = new DatasetGroupSet(name: params.name, sampleSet: sampleSet)
		def defaultGroup = new DatasetGroup(name: grailsApplication.config.group.default, displayOrder: 1, groupSet: groupSet)

		// find all samples that have array data
		def samples = mongoDataService.getSampleIdsInSampleSet(sampleSet.id)
		if (samples) {
			def criteria = ArrayData.createCriteria()
			criteria.list {
				'in'("id", samples)
			}?.each {
				defaultGroup.addToGroupDetails(new DatasetGroupDetail(sample: it))
			}
		}

		groupSet.addToGroups(defaultGroup)
		return [sampleSet: sampleSet, groupSet: groupSet]

	}

	def hasLabkeyData(def sampleSetId) {
		return sampleSetId == 33l;//TODO Fix correctly
	}

	def view = {
		def editable = false
		if (loggedIn) {
			SecUser user = springSecurityService.currentUser
			editable = user && user.authorities.contains(SecRole.findByAuthority('ROLE_USER'))
		}

		def lkgroups
		def groupSet = DatasetGroupSet.get(params.id)
		if (params.errors) {
			groupSet.errors.reject(params.errors, [params.name].toArray(), "Error! There is problem with the option you entered.")
		}

		def datasetType = sampleSetService.getDatasetType(groupSet.sampleSet.id)?.name
		if (datasetType in grailsApplication.config.genomicSource.tg2Datatypes) {
			updateMongoDbWithTg2(["sampleSetId": groupSet.sampleSet.id])
		}
		def headers = mongoDataService.getSampleSetFieldKeys(groupSet.sampleSet.id, true)

		if (hasLabkeyData(groupSet.sampleSet.id)) {
			headers.push([label: "labkey.cohort", header: "Cohorts"])
			lkgroups = labkeyReportService.loadTrialShareGroups()
			lkgroups.each { key, value ->
				headers.push([label: "trialshare_group.${key}", header: value])
			}
		}
		def tg2headers = mongoDataService.getTg2Fields(groupSet.sampleSet.id, true)
		headers.addAll(tg2headers)

		def sampleValueCount = 0
		def sampleValues
		if (headers) {
			sampleValues = mongoDataService.getColumnValues(groupSet.sampleSet.id, headers[0].label)
			if (sampleValues) {
				sampleValueCount = sampleValues.size()
				if (sampleValues.size() > 5) {
					sampleValues = sampleValues[0..4]
				}
			}
		}

    Map tables = chartingDataService.getProbeTables(groupSet.sampleSet)
//    def signalDataTable = tables.normalizedDataTable
//		ChipType chip = ChipsLoaded.findBySampleSet(groupSet.sampleSet)?.chipType
//		if (chip == null && groupSet.sampleSet.parentSampleSet) {
//			chip = ChipsLoaded.findBySampleSet(groupSet.sampleSet.parentSampleSet)?.chipType
//		}
//		def signalDataTable = chip.technology.id == 2 ? "array_data_detail_tmm_normalized" : "array_data_detail_quantile_normalized"
//		if (chip.id == 27) {
//			signalDataTable = "array_data_detail"
//		}

//		if (groupSet.sampleSet.rawSignalType.id == 6 || groupSet.sampleSet.rawSignalType.id == 7) {
//			signalDataTable = "array_data_detail"
//		}
		def totalGroups = DatasetGroup.countByGroupSet(groupSet)
		return [groupSet: groupSet, headers: headers, sampleValues: sampleValues, totalGroups: totalGroups,
			sampleValueCount: sampleValueCount, editable: editable, signalDataTable: tables?.normalizedDataTable]
	}

	def updateMongoDbWithTg2 = { attr ->
		def tg2SampleData = tg2QueryService.getTg2SampleData(attr.sampleSetId)
		tg2SampleData.each { long sampleId, Map values ->
			mongoDataService.updateTg2Sample(attr.sampleSetId, sampleId, values)
		}
	}

	def addToGroup = {
		def groupSet = DatasetGroupSet.get(params.id)
		if (params.samples) {
			def selectedSamples = params.samples.split(',').collect { Long.parseLong(it) }
			def group = DatasetGroup.get(params.receiveGroup)
			def groupDetails = DatasetGroupDetail.findAllByIdInList(selectedSamples)
			groupDetails.each { DatasetGroupDetail groupDetail ->
				groupDetail.group = group
			}
			groupSet.save()
			flash.message = "${selectedSamples.size()} samples were moved to ${group.name}."
		}
		else {
			flash.message = "No samples were selected."
		}

		render params.id
	}

	def deleteGroup = {
		def group = DatasetGroup.get(params.id)
		def groupSet = DatasetGroupSet.get(group.groupSet.id)
		if (group.groupDetails) {
			groupSet.errors.reject("org.sagres.mat.DatasetGroupSet.details.not.empty", null, "Cannot delete a non-empty group!")
		}
		else {
			DatasetGroup.findAllByGroupSetAndDisplayOrderGreaterThan(groupSet, group.displayOrder).each { it.displayOrder--; it.save() }
			group.delete()
			flash.message = "${group.name} has been deleted."
		}
		redirect(action: "view", id: groupSet.id)
	}

	def sampleValues = {
		def groupSet = DatasetGroupSet.get(params.id)
		def sampleValues = mongoDataService.getColumnValues(groupSet.sampleSet.id, params.header)
		def sampleValueCount = sampleValues.size()
		if (sampleValues.size() > 5) {
			sampleValues = sampleValues[0..4]
		}
		render([sampleValues: sampleValues, sampleValueCount: sampleValueCount] as JSON)
		return null
	}

	def createGroups = {
		def groupSet = DatasetGroupSet.get(params.id)
		def group = DatasetGroup.get(params.splitGroupId)
		def sampleSetid = groupSet.sampleSet.id
		def namePrefix = params.namingConvention == 'Appending column name to group name' ? "${group.name}-" : ""

		def uniqueGroups = mongoDataService.getColumnValues(sampleSetid, group.groupDetails.sample.id, params.header)

		// make room for new groups - increment displayOrder
		def increment = uniqueGroups.size() - 1
		DatasetGroup.findAllByGroupSetAndDisplayOrderGreaterThan(groupSet, group.displayOrder).each {
			it.displayOrder += increment; it.save()
		}

		// now change the name of the current group and create the additional new groups
		Map<String, DatasetGroup> valToGroup = new HashMap<String, DatasetGroup>()
		uniqueGroups.eachWithIndex { val, i ->
			val = val ?: "none"
			if (i == 0) {
				// don't really need to delete current group, just change name
				// and move it's group details to any additional new groups
				group.name = namePrefix + val
				valToGroup.put(val, group)
			}
			else {
				def newGroup = new DatasetGroup(name: namePrefix + val, displayOrder: group.displayOrder + i)
				groupSet.addToGroups(newGroup)
				valToGroup.put(val, newGroup)
			}
		}

		// start adding group details
		def column = params.header
		group.groupDetails.each {
			def v = mongoDataService.getValueForColumn(sampleSetid, it.sample.id, column)
			it.group = valToGroup.get(v)
		}

		groupSet.save()

		redirect(action: "view", id: params.id)
		return null
	}

	def createGroupsFromAll = {
		def groupSet = DatasetGroupSet.get(params.id)

		// delete all groups
		DatasetGroup.findAllByGroupSet(groupSet).each {
			groupSet.removeFromGroups(it)
			it.delete()
		}
		groupSet.save(flush: true)

		def sampleFields = mongoDataService.getSampleFields()
		def headerKey = params.header
		def header = headerKey.split("\\.")
		def externalGroupDefinition = false
		def tsGroupId = -1
		//Check for labkey
		def humanizedHeader
		if (header[0] == "values") {
			if (sampleFields.containsKey(header[1])) {
				humanizedHeader = sampleFields.get(header[1]).displayName
			}
			else {
				humanizedHeader = header[1]
			}
		}
		else if (header[0] == "tg2") {
			humanizedHeader = header[1].encodeAsHumanize()
		}
		else if (header[0] == "labkey") {
			externalGroupDefinition = true
			humanizedHeader = "Cohort"
		}
		else if (header[0] == "trialshare_group") {
			externalGroupDefinition = true
			humanizedHeader = "TrialShare Group ${header[1]}"
			tsGroupId = header[1]
		}
		else {
			humanizedHeader = header[0].encodeAsHumanize()
		}

		// now create new groups and add group details
		def displayOrder = 1
		Map<String, DatasetGroup> uniqueGroups = new HashMap<String, DatasetGroup>()
		if (!externalGroupDefinition) {

			mongoDataService.getColumnValuesAndSampleId(groupSet.sampleSet.id, headerKey).each { Map m ->
				def groupName = m.get(headerKey)
				if (!groupName || groupName == "null") {
					groupName = "none"
				}
				if (!uniqueGroups.containsKey(groupName)) {
					def newGroup = new DatasetGroup(groupSet: groupSet, name: "${humanizedHeader}-${groupName}", displayOrder: displayOrder)
					newGroup.save()
//        groupSet.addToGroups(newGroup)
					uniqueGroups.put(groupName, newGroup)
					displayOrder++
				}
				def arrayData = ArrayData.get(m.sampleId)
				if (arrayData) {
					uniqueGroups.get(groupName).addToGroupDetails(new DatasetGroupDetail(sample: arrayData))
				}
			}
		} else {
			def labkeyIdMapping = labkeyReportService.getLabkeyIdMapping()
			if (tsGroupId == -1) {

				//Handle Labkey cohorts
				def cohorts = labkeyReportService.loadLabkeyCohorts()
				cohorts.each {labkeyId, cohortName ->
					def groupName = cohortName
					if (!groupName || groupName.length() == 0) {
						groupName = "Not in a cohort"
					}

					if (!uniqueGroups.containsKey(groupName)) {
						def newGroup = new DatasetGroup(groupSet: groupSet, name: "${humanizedHeader}-${groupName}", displayOrder: displayOrder)
						newGroup.save()
						uniqueGroups.put(groupName, newGroup)
						displayOrder++
					}
					def arrayData = ArrayData.get(labkeyIdMapping.get(labkeyId))
					//	println "labkeyIdMapping.get(labkeyId): ${labkeyIdMapping.get(labkeyId)} - ${arrayData}"
					if (arrayData) {
						uniqueGroups.get(groupName).addToGroupDetails(new DatasetGroupDetail(sample: arrayData))
					}
				}
			} else {
				//Handle Labkey Group
				def groups = labkeyReportService.loadLabkeyGroups()
				def groupLabel
				groups.each {  g->
					if (g.id?.toString().equalsIgnoreCase(tsGroupId.toString())) {
						groupLabel = g.label
					}
				}
				//def groupLabel = groups.get(tsGroupId)
				def group = labkeyReportService.loadLabkeyGroupMembers(groupLabel)
				def sampleSetIdsInGroup = []
				group.each {  labkeyId ->
					sampleSetIdsInGroup.add(labkeyIdMapping.get(labkeyId).toString())
				}

				def allSamples = mongoDataService.getSamplesInSampleSet(groupSet.sampleSet.id)
				if (!uniqueGroups.containsKey(groupLabel)) {
					def newGroup = new DatasetGroup(groupSet: groupSet, name: groupLabel, displayOrder: displayOrder)
					newGroup.save()
					uniqueGroups.put(groupLabel, newGroup)
					displayOrder++
				}
				def notInGroupName = "Other"
				if (!uniqueGroups.containsKey(notInGroupName)) {
					def newGroup = new DatasetGroup(groupSet: groupSet, name: notInGroupName, displayOrder: displayOrder)
					newGroup.save()
					uniqueGroups.put(notInGroupName, newGroup)
					displayOrder++
				}

				allSamples.each {   sampleId ->
					def arrayData = ArrayData.get(sampleId.sampleId)
					def ssid = sampleId.sampleId.toString()
					def groupName = notInGroupName
					if (sampleSetIdsInGroup.contains(ssid)) {
						groupName = groupLabel
					}
					if (arrayData) {
						uniqueGroups.get(groupName).addToGroupDetails(new DatasetGroupDetail(sample: arrayData))
					}
				}
			}


		}

//    groupSet.save()

		redirect(action: "view", id: groupSet.id)
	}

	def resetGroups = {
		def groupSet = DatasetGroupSet.get(params.id)

		// delete all groups
		DatasetGroup.findAllByGroupSet(groupSet).each {
			groupSet.removeFromGroups(it)
			it.delete()
		}
		groupSet.save(flush: true)

		def ungroup = new DatasetGroup(name: grailsApplication.config.group.default, displayOrder: 1)
		groupSet.addToGroups(ungroup)
		def samples = mongoDataService.getSampleIdsInSampleSet(groupSet.sampleSet.id)
		if (samples) {
			def criteria = ArrayData.createCriteria()
			criteria.list {
				'in'("id", samples)
			}?.each {
				ungroup.addToGroupDetails(new DatasetGroupDetail(sample: it))
			}
			ungroup.save(flush: true)
		}

		groupSet.save()

		redirect(action: "view", id: params.id)
	}

	def createSubset = {
		def sampleSet = DatasetGroupSet.get(params.id)?.sampleSet
		def group = DatasetGroup.get(params.groupId)
		def newDatasetName = "${sampleSet.name} - ${group.name}"
		int version = 1
		while (SampleSet.findByName(newDatasetName)) {
			newDatasetName = "${sampleSet.name} - ${group.name}_${version}"
			version++
		}
		def options = new ArrayList()
		if (params.options) {
			options.addAll(params.options)
		}
		def description = newDatasetName
		if (options.contains("copyAnnotations")) {
			description = sampleSet?.description
		}
		def sampleSetId = sampleSetService.duplicateSampleSetFromSamples(group.groupDetails.sample, sampleSet,
			newDatasetName, description, options)
		redirect(controller: "sampleSet", action: "show", id: sampleSetId)
	}

	def setDefaultRankList = {
		def success = false
		def groupSet = DatasetGroupSet.get(params.long("id"))
		def rankList = RankList.get(params.long("rankListId"))
		if (groupSet && rankList) {
			groupSet.defaultRankList = rankList
			groupSet.save()
			success = true
		}
		render( success ? "OK" : "Fail" )
	}

	def getGroups = {
		def groupSetId = params.id
		if (groupSetId?.toString().startsWith("labkey") || groupSetId?.toString().startsWith("trialshare_group")) {
			def groups = []
			render groups as JSON
		} else {

			def groupSet = DatasetGroupSet.get(params.long("id"))
			if (groupSet) {
				def groups = groupSet.groups.collect {
					[id: it.id, name: it.name]
				}
				render groups as JSON
			}

		}
	}

}
