package org.sagres.sampleSet

import common.ClinicalDataSource
import common.SecRole
import common.SecUser
import common.chipInfo.RawSignalDataType
import grails.converters.JSON
import groovy.sql.Sql
import org.sagres.sampleSet.annotation.*
import org.sagres.sampleSet.component.*
import org.sagres.mat.Analysis
import org.sagres.rankList.RankList
import org.sagres.rankList.RankListDetail
import org.apache.jasper.compiler.Node.ParamsAction;
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.sagres.importer.TextTable
import org.sagres.importer.TextTableSeparator

import org.springframework.security.core.context.SecurityContextHolder as SCH
import common.LabkeyAppTokenAuthentication
import common.SecUserSecRole
import common.chipInfo.ChipsLoaded
import common.chipInfo.ChipType
import common.chipInfo.GenomicDataSource
import org.sagres.mat.MatConfigService
import org.sagres.labkey.LabkeyReport

class SampleSetController {

//  static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	private def tg2Datatypes = ["Benaroya", "Baylor"]
	def mongoDataService
	def platformService
	def converterService
	def tg2QueryService
	def sampleSetFilterService //injected
	def springSecurityService //injected
	def sampleSetService; //injected
	def datasetCollectionService //injected
	def dataSource; //injected
	def dm3Service //injected
	def userDetailsService
	def grailsApplication
    def matConfigService
    def labkeyReportService

	def beforeInterceptor = {
		//def grailsApplication = new DefaultGrailsApplication()
		def retVal = true
		SecUser user = springSecurityService.currentUser


		def setsRequiringLogin = grailsApplication.config.genomic_datasource_names_requiring_login

        //Generic Role check using SampleSetRole
        try {

              if (params.id) {
                  //Check for existing Roles
                  def rolesToCheck = SampleSetRole.findAllBySampleSetId(params.id)
//                  if (rolesToCheck && rolesToCheck.size() > 0) {
//                      println "sampleset ${params.id}  - has a required role."

                      rolesToCheck.each {   cRole ->
                          def reqRole = SecRole.findById(cRole.roleId)
                          //println "Checking for ${reqRole.authority}"
						  if (!user?.authorities?.authority?.contains(reqRole.authority))
						  {
							  def lastVisitedInfo = [:]
							  lastVisitedInfo.action = actionName
							  lastVisitedInfo.controller = controllerName
							  lastVisitedInfo.params = params
							  session.setAttribute("sessionInfo", lastVisitedInfo)
							  println "User not logged in/ User does not have approrpiate authority - redirecting to login"
							  flash.message = "Please Login to view this sample set"
							  redirect(controller: 'login', action: 'auth')
							  retVal = false
//						  } else {
//						  	  println "User has role"
						  }
 //                     }
                  }
              }
        }   catch ( Exception ex ) {
            println "Exception checking role ${ex.toString()}"
        }

		return retVal
	}

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		
		Sql sql = Sql.newInstance(dataSource)
		
		if (grailsApplication?.config?.dm3?.labkey?.authentication?.url != null) {
			handleLabkeyUser()
		}
		
		def sampleSets = SampleSet.findAllByMarkedForDeleteIsNull()
		def filterList = sampleSetFilterService.getFilterLists(sql, sampleSets)
		def searchTerm = params.sampleSetSearch
		if (searchTerm && searchTerm.trim() != "") {
			sampleSets.retainAll(sampleSetFilterService.textSearch(searchTerm))
			params.searchResults = params.sampleSetSearch
		}
		params.filterPanelShow = "true"
		def isInternal = params.boolean("briInternal")

		def sampleSetList = sampleSetFilterService.buildSampleSetList(sql, sampleSets, isInternal)

		def model = [sampleSetList: sampleSetList, params: params]
		model.putAll(filterList)
		model.put("savedFilters", getSavedFilters())
		model.put("savedCollections", getSavedCollections())
		sql.close()

		datasetCollectionService.saveCollection("temp-collection", sampleSets.id)

		return model
	}

	def status = {
		if (grailsApplication?.config?.dm3?.labkey?.authentication?.url != null) {
			handleLabkeyUser()
		}
		def sampleSets = SampleSet.findAllByMarkedForDeleteIsNull()
		def searchTerm = params.sampleSetSearch
		if (searchTerm && searchTerm.trim() != "") {
			sampleSets.retainAll(sampleSetFilterService.textSearch(searchTerm))
			params.searchResults = params.sampleSetSearch
		}
		params.filterPanelShow = "true"
		def isInternal = params.boolean("briInternal")

		Sql sql = Sql.newInstance(dataSource)
		def sampleSetList = sampleSetFilterService.buildStatusSetList(sql, sampleSets, isInternal)
		def model = [sampleSetList: sampleSetList, params: params]
		model.putAll(sampleSetFilterService.getFilterLists(sql, null))
		model.put("savedFilters", getSavedFilters())
		model.put("savedCollections", getSavedCollections())
		sql.close()

		datasetCollectionService.saveCollection("temp-collection", sampleSets.id)

		return model
	}

	def filteredSampleSets = {
		Sql sql = Sql.newInstance(dataSource)
		def action = params.controllerAction ?: "list"
		def sampleSets = SampleSet.findAllByMarkedForDeleteIsNull()//getAll()
		def filterList = sampleSetFilterService.getFilterLists(sql, sampleSets)
		def searchResults = [], checkedBoxes = [:], dataResults = [:]
		sampleSetFilterService.filter(sampleSets, params, searchResults, checkedBoxes, dataResults)

		def to = Math.min(5, searchResults.size())
		params.searchResults = searchResults[0..<to]

		def isInternal = params.boolean("briInternal")

		def model = [:]
		model.putAll(filterList)
		def sampleSetList = sampleSetFilterService.buildSampleSetList(sql, sampleSets, isInternal)
		model.putAll([sampleSetList: sampleSetList, params: params, checkedBoxes: checkedBoxes, dataResults: dataResults])
		model.put("savedFilters", getSavedFilters())
		sql.close()
		datasetCollectionService.saveCollection("temp-collection", sampleSets.id)

		render(view: action, model: model)
	}
	
	def filteredStatusSets = {
		Sql sql = Sql.newInstance(dataSource)
		def action = params.controllerAction ?: "status"
		def sampleSets = SampleSet.findAllByMarkedForDeleteIsNull()//getAll()
		def searchResults = [], checkedBoxes = [:], dataResults = [:]
		sampleSetFilterService.filter(sampleSets, params, searchResults, checkedBoxes, dataResults)

		def to = Math.min(5, searchResults.size())
		params.searchResults = searchResults[0..<to]

		def isInternal = params.boolean("briInternal")

		def model = [:]
		model.putAll(sampleSetFilterService.getFilterLists(sql, null))
		def sampleSetList = sampleSetFilterService.buildStatusSetList(sql, sampleSets, isInternal)
		model.putAll([sampleSetList: sampleSetList, params: params, checkedBoxes: checkedBoxes, dataResults: dataResults])
		model.put("savedFilters", getSavedFilters())
		sql.close()
		datasetCollectionService.saveCollection("temp-collection", sampleSets.id)

		render(view: action, model: model)
	}

	private def List getSavedFilters() {
		def username = "*"
		if (springSecurityService.isLoggedIn()) {
			username = springSecurityService.currentUser?.username ?: "*"
			List filters = mongoDataService.find("savedfilters", [user: username], ["name"], null, 0)
			def savedFilters = filters?.collect {
				return it.name
			}
			savedFilters.remove("last-filter")
			return savedFilters
		}
	}

	def loadFilter = {
		def filterName = params.filterName
		if (filterName) {
			def username = "*"
			if (springSecurityService.isLoggedIn()) {
				username = springSecurityService.getCurrentUser()?.username ?: "*"
			}
			Map queryParams = mongoDataService.findOne("savedfilters", [user: username, name: filterName], ["query"]).query
			queryParams.put("filterPanelShow", true)

			redirect(action: "filteredSampleSets", params: queryParams)
		}
	}

	def saveFilter = {
		def filterName = params.filterName
		if (filterName) {
			sampleSetFilterService.saveFilter(filterName, params)
		}
	}

	private def List getSavedCollections() {
		return datasetCollectionService.getCollectionNames()
	}

	def updateArrayDataTable = {
		SampleSet.list().each {
			def dsType = sampleSetService.getDatasetType(it.id)?.name
			if (dsType == "Benaroya") {
				tg2QueryService.updateTg2IDs(it.id)
			}
		}
		redirect(action: "list", params: params)
	}

	def updateMongo = {
		SampleSet.list().each {
			mongoDataService.updateSamples(it.id)
		}
		redirect(action: "list", params: params)
	}

	def create = {
		def sampleSet = new SampleSet()
		sampleSet.properties = params
		return [sampleSet: sampleSet]
	}

	def save = {
		def sampleSet = new SampleSet(params)
		if (createSampleSetAndSave(sampleSet)) {
			flash.message = "A new sample set has been created. You can begin adding information about the study design by clicking on the text below each category."
			redirect(action: "show", id: sampleSet.id)
		}
		else {
			render(view: "create", model: [sampleSet: sampleSet])
		}
	}

	private boolean createSampleSetAndSave(SampleSet sampleSet) {
		sampleSet.description = sampleSet.description.trim()
		sampleSet.status = grailsApplication.config.status.default.text
		sampleSet.studyType = grailsApplication.config.studyType.default.text

		if (sampleSet.save(flush: true)) {
			// create a placeholder SampleSetAnnotation
			def sampleSetAnnotation = new SampleSetAnnotation()
			sampleSetAnnotation.sampleSet = sampleSet
			sampleSetAnnotation.save()

			// create a placeholder SampleSetSampleInfo
			def sampleSetSampleInfo = new SampleSetSampleInfo()
			sampleSetSampleInfo.sampleSet = sampleSet
			sampleSetSampleInfo.save()

			def sampleSetPlatformInfo = new SampleSetPlatformInfo()
			sampleSetPlatformInfo.sampleSet = sampleSet
			sampleSetPlatformInfo.save()

			def sampleSetAdminInfo = new SampleSetAdminInfo()
			sampleSetAdminInfo.sampleSet = sampleSet
			sampleSetAdminInfo.save()

			// copy over default components for this sample set
			def ssDefComponentSet = SampleSetOverviewComponentDefaultSet.findByName(grailsApplication.config.sampleSet.overviewComponentsSet.default.name)
			if (ssDefComponentSet) {
				def defComponents = SampleSetOverviewComponentDefault.findAllByDefaultSetId(ssDefComponentSet.id)
				defComponents.each {
					def ssComponent = new SampleSetOverviewComponent(sampleSetId: sampleSet.id, componentId: it.componentId, displayOrder: it.displayOrder)
					ssComponent.save()
				}
			}
			return true
		}

		return false
	}

	def setStatus = {
		def sampleSet = SampleSet.get(params.id)
		def status = LookupListDetail.findByLookupListAndName(LookupList.findByName('Status'), params.value)
		if (status) {
			sampleSet.status = params.value
			sampleSet.save()
		}
		render sampleSet.status
	}

	def setStudyType = {
		def sampleSet = SampleSet.get(params.id)
		def studyType = LookupListDetail.findByLookupListAndName(LookupList.findByName('Study Type'), params.value)
		if (studyType) {
			sampleSet.studyType = params.value
			sampleSet.save()
		}
		render sampleSet.studyType
	}

	def setter = {
		def value = params.value
		if (params.clean) {
			value = converterService.docToHTML(params.value, (params.property == "name"))
		}
		def sampleSet = SampleSet.get(params.id)
		if (value == "") {
			sampleSet.setProperty(params.property, null)
		}
		else {
			sampleSet.setProperty(params.property, value)
		}
		if (sampleSet.save(flush: true)) {
			if (params.paragraph) {
				value = value.encodeAsParagraph()
			}
			render value
			return value
		}
		else {
			render "error"
		}
		return null
	}

	def getter = {
		def sampleSet = SampleSet.get(params.id)
		if (sampleSet) {
			def value = sampleSet.getPersistentValue(params.property)
			if (!value) {
				render ''
			}
			else {
				render value
				return value
			}
		}
		return null
	}

	def sampleSetTags = {
		def sampleSet = SampleSet.get(params.id)
		render(getTags(sampleSet) as JSON)
		return null
	}

	private List getTags(SampleSet sampleSet) {
		def tags = ["All Files"] + SampleSetFile.withCriteria {
			eq("sampleSet", sampleSet)
			and {
				isNotNull("tag")
			}
			tag { }  // make sure the referenced tag exists....

		}.tag.unique() { a, b ->
			a.tag <=> b.tag
		}.sort {
			FileTag a, FileTag b -> a.tag.compareToIgnoreCase(b.tag)
		}.collect {
			it.tag
		}
		return tags
	}

	def files = {
		def sampleSet = SampleSet.get(params.id)
		def tag = params.fileTag ? FileTag.findByTag(params.fileTag) : null
		def files = tag ? SampleSetFile.findAllBySampleSetAndTag(sampleSet, tag) : sampleSet.sampleSetFiles
		if (files) {
			files = files.sort { SampleSetFile a, SampleSetFile b -> a.filename.compareToIgnoreCase(b.filename) }
		}
		render(files as JSON)
	}

	def cancel = {
		// return to list
		redirect(action: "list")
	}

	def show = {
		def editable = false
		if (loggedIn) {
			SecUser user = springSecurityService.currentUser
			editable = user && user.authorities.contains(SecRole.findByAuthority(grailsApplication.config.dm3.sampleset.editrole))
		}

		def sampleSet = SampleSet.findByIdAndMarkedForDeleteIsNull(params.long("id"))//get(params.id)
		if (sampleSet) {
			def statusMap = [:]
			LookupList.findByName('Status')?.lookupDetails.each { statusMap.put(it.name, it.name) }

			def studyTypeMap = [:]
			LookupList.findByName('Study Type')?.lookupDetails.collect { studyTypeMap.put(it.name, it.name) }


			def tg2QualityInfo, tg2SampleInfo, tg2QualityHeaders = ["sample_id", "sample_name", "barcode"],
			tg2SampleHeaders = ["sample_id", "sample_name", "barcode"]
			def datasetType = sampleSetService.getDatasetType(sampleSet.id)?.name
			def showTg2 = datasetType in tg2Datatypes
			def showAdmin = showTg2
			if (showTg2) {
				tg2QualityInfo = tg2QueryService.getTg2QualityData(sampleSet.id)
				tg2QualityInfo.each { Map values ->
					values.each { k, v ->
						if (v != null && !tg2QualityHeaders.contains(k)) {
							tg2QualityHeaders.push(k)
						}
					}
				}
				tg2QualityInfo.each { Map values ->
					values.keySet().retainAll(tg2QualityHeaders)
				}

				tg2SampleInfo = tg2QueryService.getTg2SampleData(sampleSet.id)
				tg2SampleInfo.each { long id, Map values ->
					values.each { k, v ->
						if (v != null && !tg2SampleHeaders.contains(k)) {
							tg2SampleHeaders.push(k)
						}
					}
				}
				tg2SampleInfo.each { long id, Map values ->
					values.keySet().retainAll(tg2SampleHeaders)
				}
			}

      Sql sql = Sql.newInstance(dataSource)

			def groupSetToAnalyses = [:]
			def query = """SELECT a.id 'aId', a.data_set_groups 'groupSetId', a.dataset_name 'name', a.run_date 'runDate', a.flag_delete 'flagDelete'
        FROM analysis_summary AS s
        LEFT JOIN analysis AS a ON a.id = s.analysis_id
        WHERE a.sample_set_id = ${sampleSet.id}
        AND s.analysis_complete_time IS NOT NULl
        AND a.data_set_groups != -1
        AND (a.flag_delete IS NULL OR a.flag_delete != 'TRUE')
        ORDER BY a.run_date DESC, a.dataset_name ASC"""
			//LIMIT 5
			sql.eachRow(query) {
				if (!groupSetToAnalyses.containsKey(it.groupSetId)) {
					groupSetToAnalyses.put(it.groupSetId, [])
				}
				groupSetToAnalyses.get(it.groupSetId).push([analysis: it.aId, name: it.name, runDate: it.runDate, flagDelete: it.flagDelete])
			}
      sql.close()

      def labkeyReports = LabkeyReport.findAllBySampleSetId(sampleSet.id)
      def labkeyHost = grailsApplication.config.dm3.labkey.authentication.url

      def associatedTSRole = -1
      def trialShareProjectInfo
      if (grailsApplication.config.dm3.authenticate.labkey
   //           && (user?.authorities?.authority?.contains('ROLE_POWER_USER') || user?.authorities?.authority?.contains('ROLE_ADMIN') )
      ) {
          trialShareProjectInfo = labkeyReportService.loadTrialShareRolesWithDescription()
          SampleSetRole ssRole = SampleSetRole.findBySampleSetId(params.id)
          associatedTSRole = ssRole?.roleId
      }
      List<String> categories = labkeyReports.collect { it.category }
      Map<String,List<Map<String,String>>> categoryToHeaders = [:]
      if (categories) {
        Map headers = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSet.id ], categories)
        headers.each { String key, h ->
          List categoryHeaders = []
          if (key != "_id") {
            h.header.each { String k, Map v ->
              categoryHeaders.push([ key:k, displayName:v.displayName ])
            }
          }
          categoryToHeaders.put(key, categoryHeaders)
        }
      }

			def groupSetToRankLists = editable ? sampleSetService.getGroupSetRankLists(sampleSet.id, -1) : null

			def tags = getTags(sampleSet)
			def files = sampleSet.sampleSetFiles
			def associatedChipId = dm3Service.versionIdForSampleSet(sampleSet.id)
			def gen3VersionId = dm3Service.getGen3VersionIdForChipType( sampleSet.chipType.id );
			def hasAssociatedChipId = (associatedChipId != null && associatedChipId > 0) ||
			((gen3VersionId != null) && (gen3VersionId > 0));

			def platformInfo = sampleSet.sampleSetPlatformInfo
			def platforms = LookupList.findByName('Genomic Platform').lookupDetails
			def platformOptions = platformService.getPlatformOptions(platformInfo?.platform)
			def libraryPrepOptions = platformService.getLibraryPrepOptions(platformInfo?.platform)

			def samples = mongoDataService.getSamples(sampleSet.id)
			def sampleHeaders = mongoDataService.getSampleSetFields(sampleSet.id)
			def previewSamples = mongoDataService.getSamplesInSampleSet(sampleSet.id, "sampleBarcode", MongoDataService.ASCENDING, 5)
			def fieldKeys = sampleHeaders.keySet()
			def previewHeaders = []
			if (fieldKeys) {
				fieldKeys.eachWithIndex { k, i ->
					if (i < 5) {
						previewHeaders.push([key: k, displayName: sampleHeaders.get(k).displayName])
					}
				}
			}

			def dataKeys = ['biotin', 'background', 'genes001', 'genes005', 'gp95', 'housekeeping', 'noise']
			dataKeys.retainAll(tg2QualityHeaders)

      def sampleSetOverviewComponents = SampleSetOverviewComponent.findAllBySampleSetId(params.id, [sort: "displayOrder", order: "asc"])
			if (!sampleSet || !sampleSetOverviewComponents) {
				flash.message = "${message(code: 'default.not.found.message', args: ['Sample Set', params.id])}"
				redirect(action: "list")
			}
			else {
				return [statusMap: statusMap, studyTypeMap: studyTypeMap,
					sampleSet: sampleSet, samples: samples, sampleHeaders: sampleHeaders,
					sampleSetOverviewComponents: sampleSetOverviewComponents,
					platforms: platforms, platformOptions: platformOptions, libraryPrepOptions: libraryPrepOptions,
					tags: tags, files: files,
					previewSamples: previewSamples, previewHeaders: previewHeaders,
					tg2QualityInfo: tg2QualityInfo, tg2SampleInfo: tg2SampleInfo,
					tg2QualityHeaders: tg2QualityHeaders, tg2SampleHeaders: tg2SampleHeaders,
					showTg2: showTg2, showAdmin: showAdmin, dataKeys: dataKeys, editable: editable,
					hasAssociatedChipId: hasAssociatedChipId, groupSetToAnalyses: groupSetToAnalyses,
					groupSetToRankLists: groupSetToRankLists, labkeyReports: labkeyReports, categoryToHeaders:categoryToHeaders,
            params: params, labkeyHost: labkeyHost, trialShareProjectInfo:trialShareProjectInfo, associatedTSRole:associatedTSRole]
			}
		} else {
			flash.message = "${message(code: 'default.not.found.message', args: ['Sample Set', params.id])}"
			redirect(action: "list")
		}
	}

	def getAllSamples = {
		if (params.id) {
			def samples = mongoDataService.getSamples(Long.parseLong(params.id))
			render(samples as JSON)
		}
		return null
	}

	def groups = {
		def sampleSet = SampleSet.get(params.id)
		if (params.errors) {
			if (params.errors instanceof Collection) {
				params.errors.each {
					sampleSet.errors.reject(it, null, "Error! There is problem with the option you entered.")
				}
			}
			else {
				sampleSet.errors.reject(params.errors, null, "Error! There is problem with the option you entered.")
			}
		}
		return [sampleSet: sampleSet]
	}

	def spreadsheetHeader = {
		def sampleSet = SampleSet.get(params.id)
		def spreadsheet = sampleSet?.sampleSetSampleInfo?.sampleSetSpreadsheet
		if (spreadsheet) {
			render spreadsheet.header
		}
		return null
	}

  def labkeyHeader = {
    long sampleSetId = params.long("id")
    String category = params.category
    if (sampleSetId && category)
    {
      Map result = [:]
      Map headers = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [params.category])
      headers.each { String key, h ->
        if (key == category) {
          List categoryHeaders = []
          h.header.each { String k, Map v ->
            categoryHeaders.push([ key:k, displayName:v.displayName ])
          }
          result.headers = categoryHeaders
        }
      }
      render result as JSON
    }
    render ''
  }

  def createReport = {
    long sampleSetId = params.long("id")
    LabkeyReport r = new LabkeyReport(params)
    r.sampleSetId = sampleSetId
    r.save(flush:true)
    redirect(action:"show", id:sampleSetId)
  }

  def saveReport = {
    long reportId = params.long("id")
    String sampleSetColumn = params.sampleSetColumn?.trim()
    String reportColumn = params.reportColumn?.trim()
    String name = params.name?.trim()
    String queryUrl = params.queryUrl?.trim()
    String category = params.category?.trim()
    boolean enabled = params.enabled ? params.boolean("enabled") : true

    LabkeyReport r = LabkeyReport.findById(reportId)
    if (r) {
      r.reportName = name
      r.reportURL = queryUrl
      r.category = category
      r.setEnabled(enabled)
      if (sampleSetColumn && sampleSetColumn != 'null') {
        r.sampleSetColumn = sampleSetColumn
      }
      if (reportColumn && reportColumn != 'null') {
        r.reportColumn = reportColumn
      }
      if (r.save(flush:true)) {
        render 'success'
      } else {
        render 'error'
      }
    }
    render ''
  }

  def deleteReport = {
    LabkeyReport r = LabkeyReport.findById(params.long("id"))
    if (r) {
      try {
        r.delete()
      } finally {
        Map result = [ success:true ]
        render result as JSON
      }
    }
    render ''
  }

  def saveGroupsColumn = {
    long sampleSetId = params.long("id")
    def sampleSet = SampleSet.findById(sampleSetId)
    if (sampleSet && params.groupsColumn)
    {
      sampleSet.itnCohortsIdColumn = params.groupsColumn
      if (sampleSet.save(flush:true)) {
        render 'success'
      } else {
        render 'error'
      }
    }
    render ''
  }

    def saveSampleSetRole = {
        long sampleSetId  = params.long("id")
        SampleSetRole sRole = SampleSetRole.findBySampleSetId(sampleSetId)?:new SampleSetRole(sampleSetId: sampleSetId)
        long roleId = params.long("roleId")
        if (roleId > 0) {
            sRole.roleId = roleId
        } else {
            sRole.delete()
        }
        if (sRole.save(flush: true)) {
            render 'success'
        } else {
            sRole.errors.each {
                println "Error saving ${it}"
            }
            render 'error'
        }
        render ''
    }

	def edit = {
		def sampleSetInstance = SampleSet.get(params.id)
		if (!sampleSetInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sampleSet.label', default: 'SampleSet'), params.id])}"
			redirect(action: "list")
		}
		else {
			return [sampleSetInstance: sampleSetInstance]
		}
	}

	def update = {
		def sampleSetInstance = SampleSet.get(params.id)
		if (sampleSetInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (sampleSetInstance.version > version) {

					sampleSetInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'sampleSet.label', default: 'SampleSet')] as Object[], "Another user has updated this SampleSet while you were editing")
					render(view: "edit", model: [sampleSetInstance: sampleSetInstance])
					return
				}
			}
			sampleSetInstance.properties = params
			if (!sampleSetInstance.hasErrors() && sampleSetInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'sampleSet.label', default: 'SampleSet'), sampleSetInstance.id])}"
				redirect(action: "show", id: sampleSetInstance.id)
			}
			else {
				render(view: "edit", model: [sampleSetInstance: sampleSetInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'sampleSet.label', default: 'SampleSet'), params.id])}"
			redirect(action: "list")
		}
	}

	def updateSample = {
		def sampleId = params.long("id")
		def sampleSetId = params.long("sampleSetId")
		def field = params.field

		def newValue = params.text
		if (params.date) {
			newValue = Date.parse("MM/dd/yyyy", params.date)
		}

		mongoDataService.updateSample(sampleSetId, sampleId, field, newValue)
	}

	def samplesEditor = {
		def sampleSetId = Long.parseLong(params.id)
		def samples = mongoDataService.getSamples(sampleSetId)
		def sampleHeaders = mongoDataService.getSampleSetFields(sampleSetId)
		render(template: "samplesEditorTemplate", model: [samples: samples, headers: sampleHeaders])
	}


	def previewSamples = {
		def sampleSetId = Long.parseLong(params.id)
		def previewHeaders = mongoDataService.getSampleSetFields(sampleSetId, 5)
		def sortKey = params.sortKey == "sampleBarcode" ? "sampleBarcode" : "values.${params.sortKey}"
		def previewSamples = mongoDataService.getSamplesInSampleSet(sampleSetId, sortKey, Integer.parseInt(params.sortDir), 5)
		def samples = previewSamples.collect { sample ->
			def s = []
			s.push(sample.sampleBarcode)
			if (sample.values) {
				previewHeaders.keySet().each {
					if (sample.values.containsKey(it)) {
						s.push(sample.values.get(it))
					}
					else {
						s.push(" ")
					}
				}
			}
			else {
				if (previewHeaders.keySet().size() > 0) {
					for (i in 0..previewHeaders.keySet().size() - 1) {
						s.push(" ")
					}
				}
			}
			return s
		}
		render([headers: previewHeaders, samples: samples] as JSON)
	}

	def setDefaultGroupSet = {
		if (params.id && params.groupSetId) {
			def sampleSet = SampleSet.get(params.id)
			def defaultGroupSet = DatasetGroupSet.get(params.groupSetId)
			if (defaultGroupSet) {
				sampleSet.defaultGroupSet = defaultGroupSet
				sampleSet.save()
			}
		}
	}

	/**
	 * create a sample set and group set with all samples from a background subtracted file
	 * @param chipID
	 * @param cleanFirst (if set delete the sample sets for the chipID first)
	 * @return the ID of the new sample set
	 *
	 * for dev/testing using chipID = 10
	 * http://localhost:8080/dm/sampleSet/createSampleSetFromChipID/?chipID=10
	 */
	def createSampleSetFromChipID =
	{
		boolean cleanFirst = (params.cleanFirst == "yes");
		return sampleSetService.createSampleSetFromChipID(params.chipID,
			cleanFirst);
	}

	def quantileNormalizeSignals =
	{
		int sampleSetId = Integer.parseInt(params.sampleSetId);
		Sql sql = Sql.newInstance(dataSource);
		boolean success =
		sampleSetService.quantileNormalizeSignals(sampleSetId, sql);
		render(success ? "OK" : "Fail");
	}

	def generateDefaultRankList =
	{
		int sampleSetId = Integer.parseInt(params.sampleSetId);
		Sql sql = Sql.newInstance(dataSource);
		boolean success =
		sampleSetService.generateDefaultRankList(sampleSetId, sql);
		render(success ? "OK" : "Fail");
	}

	def calcPalx =
	{
		int sampleSetId = Integer.parseInt(params.sampleSetId);
		Double detectionPVal =
		params.detectionPVal ? Double.parseDouble(params.detectionPVal) :
			grailsApplication.config.palx.defaultDetectionPVal;
		Double fractionPresent =
		params.fractionPresent ? Double.parseDouble(params.fractionPresent) :
			grailsApplication.config.palx.defaultFractionPresent;
		Sql sql = Sql.newInstance(dataSource);
		int ssPalxId =
		sampleSetService.calcPalx(sampleSetId, sql,
			detectionPVal, fractionPresent);
		render(ssPalxId ? "OK" : "Fail");
	}

	def createTransformedSampleSet =
	{
		int sampleSetId = Integer.parseInt(params.sampleSetId);
		Sql sql = Sql.newInstance(dataSource);
		boolean success = sampleSetService.createTransformedSampleSet(
			sampleSetId, params, sql);
		render(success ? "OK" : "Fail");
	}

	def makeArrayDataDetailTSVFile =
	{
		int sampleSetId = Integer.parseInt(params.sampleSetId);
		String tsvFileSpec = params.tsvFileSpec ?:
			"/tmp/SampleSet_" + params.sampleSetId + ".tsv";
		boolean quantileNormalized = (params.quantileNormalized == "true");
		Sql sql = Sql.newInstance(dataSource);
		boolean success = sampleSetService.generateArrayDataDetailTSV(
			sampleSetId, tsvFileSpec, quantileNormalized, false, sql);
		render(success ? "OK" : "Fail");
	}

	def getSampleSetStats =
	{
		long sampleSetId = Integer.parseInt(params.sampleSetId);
		Sql sql = Sql.newInstance(dataSource);
		Map stats = sampleSetService.getSampleSetStats(sampleSetId, sql);
		render(stats);
	}

	def updateTG2IDs =
	{
		def resultsMap = tg2QueryService.updateTg2IDs(params.long("id"))
		return [results: resultsMap, params: params]
	}

	/**
	 * removes all data for the sample set from the mongo DB and replaces it with sampleID and barcode
	 */
	def resetSampleSet =
	{
		//println "clearing id: " + params.id
		
		def sampleSet = SampleSet.get(params.id)
		sampleSet?.sampleSetSampleInfo?.sampleSetSpreadsheet = null

		def sampleSetID = params.long('id')
		mongoDataService.deleteSampleSet(sampleSetID)
		refreshSampleSet(sampleSetID)
		
		render(text: 'OK')
	}

	def refreshSampleSet(sampleSetID) {
		// get ids and sample_name to update
		def tg2Query = """select d.id, d.barcode, e.sample_name
				from dataset_group_set a, dataset_group b, dataset_group_detail c, array_data d, ben_tg2.sample e
				where sample_set_id = ${sampleSetID}
				and a.id = b.group_set_id
				and b.id = c.group_id
				and c.sample_id = d.id
				and d.externalid is not null
				and d.externalid = e.id"""

		def sql = new Sql(dataSource)
		def sampleList = []
		sql.eachRow(tg2Query.toString()) { row ->
			sampleList << ["id": row.id, "barcode": row.barcode, "name": row.sample_name]
		}

		sampleList.each() { sample ->
			def srSampleName = sample.name.getAt(0..9)
			def srQuery = """select b.* from sampleReg.SampleTracking a, sampleReg.dna_status_results b where Sample_ID like '${srSampleName}%' and a.VMRC_RNO = b.VMRC_RNO """
			mongoDataService.updateSampleEx(sampleSetID, sample.id, "sampleBarcode", sample.barcode)
			sql.eachRow(srQuery.toString()) { srData ->
				def srDataMap = srData.toRowResult()
				srDataMap.each() {
					if (it.value != null && it.key != "VMRC_RNO") {
						mongoDataService.updateSample(sampleSetID, sample.id, it.key, it.value)
					}
				}
			}
		}
	}

	def refreshSampleRegistryData =
	{
		def sampleSetID = params.long('id')
		refreshSampleSet(sampleSetID)
	}

	def publishToGxb = {
		if (params.id) {
			def sampleSet = SampleSet.get(params.id)
			def publish = params.boolean("publish") ? 1 : 0
			sampleSet.gxbPublished = publish
			sampleSet.save()
		}
		render(text: 'OK')
	}

	def rawSignalDatatype = {
		if (params.id) {
			def sampleSet = SampleSet.get(params.id)
			def signalType = params.long("signalType")
			def sType = RawSignalDataType.get(signalType)
//			if (sType) {
				sampleSet.rawSignalType = sType
				sampleSet.save()
//			}
		}
		render(text: 'OK')
	}

	def defaultSignalDatatype = {
		if (params.id) {
			def sampleSet = SampleSet.get(params.id)
			def signalType = params.long("signalType")
			def sType = RawSignalDataType.get(signalType)
//			if (sType) {
				sampleSet.defaultSignalDisplayType = sType
				sampleSet.save()
//			}
		}
		render(text: 'OK')
	}

	def clinicalDatasource = {
		if (params.id) {
			def sampleSet = SampleSet.get(params.id)
			def clinicalDatasource = params.long("clinicalDatasource")
			def cDatasource = ClinicalDataSource.get(clinicalDatasource)
			if (params.clinicalDatasource) {
				sampleSet.clinicalDataSource = cDatasource
				sampleSet.save()
			}
		}
		render(text: 'OK')
	}

	def setPrivacy = {
		long sampleSetId  = params.long("id")
		if (sampleSetId) {
			def sampleSet = SampleSet.get(params.id)
			SampleSetRole sRole = SampleSetRole.findBySampleSetId(sampleSetId)?:new SampleSetRole(sampleSetId: sampleSetId)
			def isPublic = params.boolean("isPublic") ?: false
			if (isPublic) {
				println "sampleset " + sampleSetId + " is now public"
				sampleSet.visibleSecRoles = null
				sRole.delete()
			}
			else
			{
				println "sampleset " + sampleSetId + " is now private"
				def role = SecRole.findByAuthority('ROLE_USER')
				sampleSet.addToVisibleSecRoles(role)
				sRole.roleId = role.id
				sRole.save(flush: true)
			}
			sampleSet.save(flush: true)
		}
		render(text: 'OK')
	}

	def setSampleSetLink = {
		if (params.id) {
			def sampleSet = SampleSet.get(params.id)
			def linkType = SampleSetLink.get(params.linkTypeId)
			def url = params.url ? params.url.trim() : null
			if (linkType) {
				if (sampleSet.links.linkType.contains(linkType)) {
					def link = sampleSet.links.find { it.linkType == linkType }
					if (url) {
						link.dataUrl = url
						link.save()
					}
					else {
						sampleSet.removeFromLinks(link)
						link.delete()
					}
				}
				else {
					if (url) {
						new SampleSetLinkDetail(sampleSet: sampleSet, linkType: linkType, dataUrl: url).save()
					}
				}
			}
		}
		render(text: 'OK')
	}

	def titles = {
		if (params.term != "") {
			def gxb = ""
			if (params.gxb) {
				gxb = "gxb_published = 1 AND"
			}

			SecUser user = springSecurityService.currentUser

			String queryStartsWith = """SELECT id, name FROM sample_set WHERE  ${gxb} name LIKE '${params.term}%' AND marked_for_delete IS NULL ORDER BY name LIMIT 10""".toString()
			def ids = [], titles = [], dirtyTitles = []
			Sql sql = Sql.newInstance(dataSource)
			try {
				sql.eachRow(queryStartsWith) { row ->
                    dirtyTitles.add([text: row.name, id: row.id, url: "/sampleSet/show/${row.id}", gxbUrl: "/geneBrowser/show/${row.id}"])
					ids.push(row.id)
				}
			} catch (Exception e) {
				e.printStackTrace()
			}
			String query = """SELECT id, name FROM sample_set WHERE  ${gxb} (name LIKE '%${params.term}%' OR description LIKE '%${params.term}%') AND marked_for_delete IS NULL ORDER BY name LIMIT 10""".toString()
			sql.eachRow(query) { row ->
				if (!ids.contains(row.id) && ids.size() < 10) {
                    dirtyTitles.add([text: row.name, id: row.id, url: "/sampleSet/show/${row.id}", gxbUrl: "/geneBrowser/show/${row.id}"])
				}
			}
			sql.close()
            dirtyTitles.each { row ->


            }

			render titles as JSON
		}
	}

	// Ajax call to this action allows sniffing out possible analysts (for filtering).
	def analysts = {
		if (params.term != "") {
			def gxb = ""
			if (params.gxb) {
				gxb = "gxb_published = 1 AND"
			}
			String query = """SELECT DISTINCT(analyst)
			  FROM sample_set_admin_info AS ssai
			  WHERE ssai.analyst LIKE '${params.term}%' ORDER BY ssai.analyst LIMIT 10"""
			def analysts = []
			Sql sql = Sql.newInstance(dataSource)
			try {
				sql.eachRow(query) { row ->
					analysts.add(text: row.analyst)
				}
			} catch (Exception e) {
				e.printStackTrace()
			}
			sql.close()
			render analysts as JSON
		}
	}

	// Ajax call to this action allows toggling the completed bit in SampleSet
	def toggleCompleted = {
		// println "toggleCompleted params: " + params
		if (params.ssId) {
			def mySet = SampleSet.get(params.ssId.toInteger())
			mySet.approved = params.state.toInteger()
			mySet.save()
		}
		// Must return something.
		render(text: 'OK')
	}

    def exportSpreadsheet = {
        long id = params.long("id")
        Long groupSetId = params.long("groupSetId")
        Long groupId = params.long("groupId")
        if (SampleSet.exists(id)) {
            List samples = mongoDataService.getSamples(id)

            boolean hasGroupSetInfo = false, hasGroupInfo = false
            Map sampleToGroup = [:]
            if (groupId && DatasetGroup.exists(groupId)) {
                DatasetGroup grp = DatasetGroup.findById(groupId)
                grp.groupDetails.each {
                    sampleToGroup.put(it.sample.id, grp.name)
                }
                hasGroupInfo = true
            } else if (groupSetId && DatasetGroupSet.exists(groupSetId)) {
                DatasetGroupSet groupSet = DatasetGroupSet.findById(groupSetId)
                groupSet.groups.each { DatasetGroup grp ->
                    grp.groupDetails.each {
                        sampleToGroup.put(it.sample.id, grp.name)
                    }
                }
                hasGroupSetInfo = true
            }

            StringBuilder sb = new StringBuilder()
            sb.append("\"Array Sample ID\"")
            if (hasGroupSetInfo) {
                sb.append(",\"Group Set\"")
            }
            sb.append(",\"Barcode\"")
            List order = []
            List keys = mongoDataService.getSampleSetFieldKeys(id, true)
            keys.each {
                order.push(it.label.split("\\.")[1])
                sb.append(",\"${it.header}\"")
            }
            sb.append("\r\n")

            samples.each { Map kv ->
                if (!hasGroupInfo || sampleToGroup.containsKey(kv.id)) {
                    sb.append("\"${kv.id}\"")
                    if (hasGroupSetInfo) {
                        sb.append(",\"${sampleToGroup.get(kv.id) ?: ''}\"")
                    }
                    sb.append(",\"${kv.sampleId}\"")
                    order.each {
                        def v = kv[it] ?: " "
                        sb.append(",\"${v}\"")
                    }
                    sb.append("\r\n")
                }
            }

            def groupName = hasGroupInfo ? "group${groupId}_" : ""
            def filename = "sampleset${id}_${groupName}sampleannotations.csv"
            def output = sb.toString().getBytes()
            response.setContentType("application/octet-stream")
            response.setHeader("Content-Disposition", "attachment; filename=${filename}")
            response.outputStream << output
        }
    }

	def markForDeletion = {
		long id = params.long("id")
		Map msg = [:]
		if (SampleSet.exists(id)) {
			Sql sql = Sql.newInstance(dataSource)
			def update = "UPDATE sample_set SET marked_for_delete=1 WHERE id=${id}"
			int count = sql.executeUpdate(update)
			sql.close()
			if (count == 1) {
				msg.error = false
				msg.message = "Sample Set ${id} was successfully marked for deletion."
			}
		} else {
			msg.error = true
		}
		render msg as JSON
	}

	def handleLabkeyUser() {
		try {
			SecUser currentUser = springSecurityService.currentUser
			def labkeyUser = SCH.context?.authentication
			if (labkeyUser != null && labkeyUser instanceof LabkeyAppTokenAuthentication) {
				if (!currentUser.username.equalsIgnoreCase(labkeyUser.name)) {
					//Need to
					def newSecUser
					try {
						newSecUser = userDetailsService.loadUserByUsername(labkeyUser.name)
						println "Loaded user"
					} catch (Exception ex) {
						println "Creating new user ${labkeyUser.name} : ${ex.message}"
						newSecUser = new SecUser(username: labkeyUser.name, email: labkeyUser.name, password: 'password', enabled: true, accountExpired: false).save(failOnError: true)
						def userRole = SecRole.findByAuthority('ROLE_USER')
						SecUserSecRole.create(newSecUser, userRole)
					}
					labkeyUser.setPrincipal(newSecUser)
				}
			}
		} catch (Exception ex) {
			println "Exception handling labkey user : ${ex.toString()}"
			ex.printStackTrace()
		}

	}


  def exportToFiles = {
      SampleSet sampleSet = SampleSet.get( params.long( "id" ) );
      String directory = grailsApplication.config.fileSampleSetExport.baseDir +
              params.id + "/";
      boolean success =
              sampleSetService.exportSampleSetToFiles( sampleSet, directory,
                                                       dataSource,
                                                       matConfigService );
      render( success ? "OK" : "Fail" );
  }

  def importFromFiles = {
      String directory = params.directory ?:
              grailsApplication.config.fileSampleSetExport.baseDir +
                  params.id + "/";
      boolean success =
              sampleSetService.importSampleSetFromFiles( directory,
                                                         dataSource,
                                                         matConfigService );
	render( success ? "OK" : "Fail" );
  }

  def exportToTarball = {
      SampleSet sampleSet = SampleSet.get( params.long( "id" ) );
      String filename = params.id + ".tgz";
      String fileSpec = grailsApplication.config.fileSampleSetExport.baseDir +
              filename;
      boolean success =
              sampleSetService.exportSampleSetToTarball( sampleSet, fileSpec,
                                                         dataSource,
                                                         matConfigService );
      if ( success )
      {
          byte[] fileData = new File( fileSpec ).getBytes();
          response.setContentType("application/octet-stream");
          response.setHeader("Content-Disposition",
                             "attachment; filename=${filename}");
          response.outputStream << fileData;
      }
      else
      {
          flash.message = "Export failed";
          log.debug( flash.message );
      }
  }

  def importFromTarball = {
      def mpFile = request.getFile( "sampleSetFile" );
      if ( mpFile.empty == false )
      {
          String filename = mpFile.getOriginalFilename();
          String fileSpec =
                  grailsApplication.config.fileSampleSetExport.baseDir +
                  filename;
          File dest = new File( fileSpec );
          mpFile.transferTo( dest );
          boolean success =
                  sampleSetService.importSampleSetFromTarball( fileSpec,
                                                               dataSource,
                                                               matConfigService );
          if ( success )
          {
              redirect( action: "list" );
          }
          else
          {
              flash.message = "Import failed";
              log.debug( flash.message );
          }
      }
      else
      {
          flash.message = "File was not uploaded";
          log.debug( flash.message );
      }
  }

	def archiveAndDelete = {
		def sampleSet = SampleSet.get(params.id)
		if ( sampleSet )
        {
			def isParent = (SampleSet.findAllByParentSampleSet(sampleSet) != null)
			if (! isParent) {
				String filename = params.id + ".tgz";
				String archiveSpec = grailsApplication.config.fileSampleSetExport.baseDir + filename;
				println "about to archive and delete " + filename + " to " + archiveSpec
				boolean success = sampleSetService.archiveAndDeleteSampleSet(sampleSet, archiveSpec, dataSource, matConfigService)
				println "back from archive and delete"
				render(success ? "OK" : "Fail")
			}
			else
			{
				render("SampleSet has children - archive/delete those first")
			}
        }
		else
        {
			render("No SampleSet")
		}
	}


	def deleteAllData = {
		def sampleSet = SampleSet.get(params.id)
		if (sampleSet)
		{
			def isParent = SampleSet.findAllByParentSampleSet(sampleSet)
			if (! isParent) {
				println "about to delete " + params.id
				boolean success = sampleSetService.archiveAndDeleteSampleSet(sampleSet, null, dataSource, matConfigService)
				println "back from delete"
				render(success ? "OK" : "Fail")
			}
			else
			{
				render("SampleSet has children - archive/delete those first")
			}
		}
		else
		{
			render("No SampleSet")
		}

    }

	def deleteUserRankLists = {
		def sampleSet = SampleSet.get(params.id)
		if (sampleSet)
		{
			boolean success = sampleSetService.deleteRankLists(sampleSet, 'user')
			//TODO: reset sampleSet ranklist to it's default.
			render( success ? "OK" : "Fail" )
		}
		else
		{
			render("No SampleSet")
		}
	
	}

	def deleteMarkedRankLists = {
		boolean success = sampleSetService.deleteRankLists(null, 'marked')
		render( success ? "OK" : "Fail" )
	}
	
	def getInfo = {
		def sampleSet = SampleSet.findByIdAndMarkedForDeleteIsNull(params.long("id"))//get(params.id)
		if (sampleSet) {
			def details = "<b>Title:</b> " + sampleSet.description
			if (sampleSet?.sampleSetAnnotation?.purpose) {
				details += "<b>Purpose:</b>" + sampleSet.sampleSetAnnotation.purpose
			}
			if (sampleSet?.sampleSetAnnotation?.hypothesis) {
				details += "<b>Hypothesis:</b>" + sampleSet.sampleSetAnnotation.hypothesis
			}
			if (sampleSet?.sampleSetAnnotation?.experimentalDesign) {
				details += "<b>Design:</b>" + sampleSet.sampleSetAnnotation.experimentalDesign
			}
			if (sampleSet?.sampleSetAnnotation?.experimentalVariables) {
				details += "<b>Variables:</b>" + sampleSet.sampleSetAnnotation.experimentalVariables
			}
			if (sampleSet?.sampleSetAnnotation?.controls) {
				details += "<b>Controls:</b>" + sampleSet.sampleSetAnnotation.controls
			}
		
			Map result = [content: details] 
			render result as JSON
		}
		else
		{
			render(text:"Fail")
		}
	}
}
