package org.sagres.mat

import grails.converters.JSON
import groovy.sql.Sql
import org.sagres.sampleSet.DatasetGroupSet
import org.sagres.sampleSet.SampleSet
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import common.SecRole
import org.sagres.stats.StatsService
import cern.colt.matrix.DoubleMatrix2D
import java.util.regex.Pattern
import common.chipInfo.ChipType
import org.sagres.sampleSet.SampleSetRole

class AnalysisController {


  def matDataService
  def matConfigService
  def dm3Service
  def springSecurityService
  def matResultsService
  def sampleSetService
  def matPlotService
  def mongoDataService
  def chartingDataService
  def mailService
  def dataSource //injected
  def grailsApplication

  static String separator = System.getProperty("line.separator")

  def beforeInterceptor = {
    //def grailsApplication = new DefaultGrailsApplication()
    def retVal = true
    SecUser user = springSecurityService.currentUser
    try {
      if (params.id != null) {

          def forwardURI =  request.getAttribute("javax.servlet.forward.request_uri")

          session.setAttribute("loginTarget", forwardURI)
          params.put("loginTarget", forwardURI)
          params.loginTarget = forwardURI

        Analysis analysis = Analysis.findByIdAndFlagDeleteIsNull(params.id)
        def sampleSetId = analysis?.sampleSetId
         if (sampleSetId) {

          def rolesToCheck = SampleSetRole.findAllBySampleSetId(sampleSetId)
//          if (rolesToCheck && rolesToCheck.size() > 0) {
//              println "sampleset ${params.id}  - has a required role."

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
                      println "User not logged in/ User does not have appropiate authority - redirecting to login"
                      flash.message = "Please Login to view this sample set"
                      redirect(controller: 'login', action: 'auth')
                      return false
                  }
              }
//          }
         }
      }	
    } catch (Exception nfe) {
      //Exception formating params.id when null
      println "${new Date()} : Exception in filter ${nfe.toString()}"
    }

    if (!loggedIn && actionName.equalsIgnoreCase("list")) {
      def newparams = [:]
      newparams.lastVisitedPage = "/" + controllerName + "/" + actionName
      flash.message = "Please login to view the list of analyses"
      redirect(controller: 'login', action: 'auth', params: newparams)
      retVal = false
    }

    return retVal
  }


  def analysisRunComplete = {
    def analysisId = params.id
    def completeFlagFile = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}scriptComplete"
    File analysisCompleteFlag = new File(completeFlagFile)
    def analysisStatus = [:]
    if (analysisCompleteFlag.exists()) {
      analysisStatus.put("status", "Complete")
    } else {
      analysisStatus.put("status", "Running")
    }
    render analysisStatus as JSON
  }

  def chipSupportsMultipleVersions = {
    def support = [:]
    try {
      def chipId = params.chipId
      ChipType ct
      if (chipId!= null && !chipId.equals("-1")) {
        long ci = Long.parseLong(chipId)
        ct = ChipType.get(ci)
      }
      if (ct != null && ct.moduleGen3MappingId != null && ct.moduleGen3MappingId > 0 ) {
        support.put("supported", Boolean.TRUE)
      } else {
        support.put("supported", Boolean.FALSE)
      }
      println "Supported - " + support
    } catch (Exception ex) {
      println "exception ${ex}"
    }
    render support as JSON
  }

  def uploadSignalData = {

  }

  def fileSep = System.getProperty("file.separator")

  def cleanupDatasetName(String datasetName) {
    StringBuilder sb = new StringBuilder()
    for (char c: datasetName?.toCharArray()) {
      if (((c >= 'a' && c <='z') || (c >= 'A' && c <= 'Z')) || ((c >= '0' && c <= '9') || (c == ' '))) {
        sb.append(c)
      }
    }
    return sb.toString()
  }



  def index = {
    redirect(action: "list", params: params)
  }


  def list = {
    params.max = Math.min(params.max ? params.int('max') : 10, 100)
    def urlPrefix = "${matConfigService.MATLink}/"
    def withResults = matDataService.getJobsForListView(params)
    def noResults = matDataService.getJobsForListView(params, false)
    def user = springSecurityService.currentUser

    def matAdmin = user.authorities.contains(SecRole.findByAuthority('ROLE_MATADMIN'))

    [analysisInstanceList: Analysis.findAllByFlagDeleteIsNull(),
        analysisInstanceTotal: Analysis.count(), jobsWithResults: withResults, jobsWithoutResults: noResults, urlPrefix:urlPrefix, user:user, matAdmin: matAdmin]
  }

  def updateDisplayName = {
    long analysisId = params.long("analysisId")
    String newName = params.text?.trim()
    Analysis a = Analysis.findById(analysisId)
    if (a && !newName?.isAllWhitespace()) {
      a.displayName = newName
      a.save(flush:true)
    }
    render (a.displayName ?: a.datasetName)
  }

  def create = {
    def analysisInstance = new Analysis()
    def errorFields = []
    analysisInstance.properties = params
    analysisInstance.displayName = params.datasetName
    def sampleGroupId = params.dataSetGroups
    def sampleSetId = params.expressionDataFile
    def forceCreate = params.force


    if (sampleGroupId != null && sampleSetId != null && forceCreate == null) {
      //set Version File
      //def versionId =  dm3Service.versionIdForSampleSet(sampleSetId)
      def chipId = dm3Service.getChipIdForSampleSet(sampleSetId)
      params.chipId = chipId
      forward( controller: "analysis", action:"save", params: params)

    } else {
      def dbDataSets = dm3Service.availableSampleSets
      def availableChips = dm3Service.getChipTypesAvailableForAnalysis()
      return [dbDataSets:dbDataSets, analysisInstance:analysisInstance,
          availableChips: availableChips, sampleSetId: sampleSetId, sampleGroupId: sampleGroupId, reload: forceCreate?:false, errorFields: errorFields]
    }
  }

  def save = {
    def datasetName = cleanupDatasetName(params.datasetName)
    params.datasetName = datasetName
    params.displayName = datasetName
    def errorFields =[]
    if (!datasetName) {
      errorFields.add('datasetName')
    }
    if (params.foldCut != null) {
      params.zscoreCut = params.foldCut
    }
    def runDate = new Date();
    def sampleGroupId = params.dataSetGroups
    def sampleSetId = params.expressionDataFile
    def chipId = params.chipId
    if (chipId == null ) {
      chipId = dm3Service.getChipIdForSampleSet(sampleSetId)
    }
    if (params.modGeneration == "3") {
      params.moduleVersion = dm3Service.getGen3VersionIdForChipType(chipId)
    } else {
      params.moduleVersion = dm3Service.getVersionIdForChipType(chipId)
    }
	if ( (params.moduleVersion == null || params.moduleVersion < 1) &&
		 params.modGeneration != 3 )
	{ //If we gen 2 moduleVersion not available, try 3.
      params.moduleVersion = dm3Service.getGen3VersionIdForChipType(chipId)
	  if ( params.moduleVersion != null )
	  {
		  params.modGeneration = 3;
	  }
	}
    if (params.moduleVersion == null || params.moduleVersion <1) {
      println "Unable to lookup moduleVersion - : ${params.moduleVersion} chipId : ${chipId} "
    }
	
	params.platformType = SampleSet.findById(sampleSetId)?.sampleSetPlatformInfo?.platform
	
    def analysisInstance = new Analysis(params)
    analysisInstance.runDate = runDate;
    //analysisInstance.datasetName=datasetName
    def expressionFile = null
    def designFile = null

    try {
      expressionFile = request.getFile("expressionFile")
      designFile = request.getFile("designFile")
    } catch (Exception ex) {
      println "Exception trying to get file from request ${ex.toString()}"
    }
    def user = springSecurityService.currentUser
    analysisInstance.user = user?.username
    def skipSave = false
    if (expressionFile == null || expressionFile.empty) {
      if (sampleSetId.toString().equalsIgnoreCase("-1")) {
        analysisInstance.errors.rejectValue('expressionDataFile', 'Select a sample set')
        errorFields.add('expressionDataFile')
        skipSave = true
      }
      if (sampleGroupId.toString().equalsIgnoreCase("-1")) {
        errorFields.add('sampleGroupId')
        analysisInstance.errors.rejectValue('dataSetGroups', 'Select a group')
        skipSave = true
      }
    }
    if (!skipSave && (analysisInstance.save(flush: true))) {
		SampleSet sampleSet = SampleSet.get( sampleSetId );
		if ( sampleSet?.chipType?.technology?.name == "Focused Array" )
		{
			matDataService.createAnalysisFolder(analysisInstance.id);
		}
		else
		{
			matDataService.createAnalysisFolder(analysisInstance.id, analysisInstance.datasetName)
		}
      def version = Version.findById(params.moduleVersion)
      if (version == null ) {
        println "Version is null for ${params.moduleVersion}"
      }
      matDataService.writeOutDataFiles(analysisInstance.id, version)
      matDataService.writeOutScriptFiles(analysisInstance.id)
      analysisInstance.setModuleFunctionFile(version.getFunctionFileName())
      analysisInstance.setModuleVersionFile(version.getVersionFileName())
      if (expressionFile == null || expressionFile.empty) {
        analysisInstance.setExpressionDataFile("expression.tsv")
        analysisInstance.setDesignDataFile("designFile.csv")
        analysisInstance.sampleSetId = Integer.parseInt(sampleSetId)
        analysisInstance.save()
        matDataService.writeParameterFile(analysisInstance)
        //Call web service to write out file
        // Need to move this to part of the run script portion
        //String dataDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisInstance.id}${fileSep}Data${fileSep}${datasetName.replaceAll(" ", "_")}"
        //				dm3Service.requestExpressionFile(sampleId, "${dataDir}${fileSep}${sampleName}" )
        def groupSet = dm3Service.createNewMATAnalysisGroupSet(analysisInstance.id, sampleGroupId)
        redirect(controller: 'MATAnalysisGroupSet', action: 'edit', params:[id: groupSet?.id, analysisId: analysisInstance.id, sampleSetId: sampleSetId])
      } else {
        matDataService.writeOutUploadedFile(analysisInstance.id, expressionFile, analysisInstance.datasetName)
        analysisInstance.setExpressionDataFile(expressionFile.originalFilename)
        analysisInstance.setDesignDataFile(designFile.originalFilename)
        matDataService.writeOutUploadedFile(analysisInstance.id, designFile, analysisInstance.datasetName)
        matDataService.writeParameterFile(analysisInstance)
        AnalysisSummary analysisSummary = new AnalysisSummary()
        analysisSummary.analysisId = analysisInstance.id
        analysisSummary.setAnalysisStartTime(new Date())
        analysisSummary.save()
        matDataService.runScript(matDataService.createCommandLine(analysisInstance.id),analysisInstance.id ,
            {
              if (user != null && grailsApplication.config.send.email.on) {
                mailService.sendMail {
                  to "${user.email}"
                  from "Chaussabel Lab BioIT <softdevteam@benaroyaresearch.org>"
                  subject "MAT run complete"
                  html "Your Module Analysis Tool (MAT) run has completed. You can view the results at " +
                      "${grailsApplication.config.grails.serverURL}/analysis/show/${analysisInstance.id}."
                }
              }
            })
        analysisInstance.save(flush: true)
        flash.message = "${message(code: 'default.created.message', args: [message(code: 'analysis.label', default: 'Analysis'), analysisInstance.id])}"
        redirect(action: "notifyJobStarted", id: analysisInstance.id)
      }
    } else {
      def dbDataSets = dm3Service.availableSampleSets
      def availableChips = dm3Service.getChipTypesAvailableForAnalysis()
      def versionFiles = Version.getAll()
      render(view: "create", model: [analysisInstance: analysisInstance, dbDataSets:dbDataSets,
          versionFiles: versionFiles, availableChips: availableChips, errorFields : errorFields])
    }
  }

  def notifyJobStarted = {
    def analysisInstance = Analysis.get(params.id)
    if (!analysisInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
      redirect(action: "list")
    }
    else {
      [analysisInstance: analysisInstance]
    }
  }

  def show = {
	  
	if (!params.controller) { params.controller = controllerName}
	if (!params.action) { params.action = actionName}

	def currentUser = springSecurityService.currentUser
	def currentUsername = currentUser?.username
	
    def analysisInstance = Analysis.findByIdAndFlagDeleteIsNull(params.id)

    if (!analysisInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
      redirect(action: "list")
    }
    else {
      def versionName = Version.findById(analysisInstance.moduleVersion)?.versionName
      def modGen = versionName ? ModuleGeneration.findByVersionName(versionName)?.generation : -1

      def sampleSet = SampleSet.findByIdAndMarkedForDeleteIsNull(analysisInstance.sampleSetId)
      String sampleSetName = sampleSet?.name
      def groupsName = DatasetGroupSet.findById(analysisInstance.dataSetGroups)?.name
//	  def title = analysisInstance.displayName ?: analysisInstance.datasetName.replaceAll(/[A-Z][a-z]/, { " ${it}".toString() }).replaceAll("\\s+", " ").trim()
      def title = analysisInstance.displayName ?: analysisInstance.datasetName

      def csvFiles = matDataService.getResultLinks(analysisInstance, "csv")
      def urlPrefix = "${matConfigService.MATLink}/${analysisInstance.id}/"
      def dsName = analysisInstance.datasetName.replaceAll(" ","_")

      def expression = "${urlPrefix}/Data/${dsName}/${analysisInstance.expressionDataFile}"
      def design = "${urlPrefix}/Data/${dsName}/${analysisInstance.designDataFile}"
      csvFiles.put(expression,"Microarray Data")
      csvFiles.put(design,"Design Data")

      def gsaPlotname = "${dsName}_Gene_Set_Analysis(_.*)+\\.csv".toString()
      Pattern p = Pattern.compile(gsaPlotname, Pattern.CASE_INSENSITIVE)
      def hasGSA = mongoDataService.exists("analysis_results", [ analysisId:analysisInstance.id, filename:p ], null)
      if (!hasGSA) {
        def csvFile = matDataService.getFile(analysisInstance, gsaPlotname)
        if (csvFile) {
          hasGSA = true
        }
      }
      def plsFilename = "${dsName}_Probe_Level_Statistics(_.*)+\\.csv".toString()
      Pattern plsP = Pattern.compile(plsFilename, Pattern.CASE_INSENSITIVE)
      def hasProbeLvlStats = mongoDataService.exists("analysis_results", [ analysisId:analysisInstance.id, filename:plsP ], null)
      if (!hasProbeLvlStats) {
        def csvFile = matDataService.getFile(analysisInstance, plsFilename)
        if (csvFile) {
          hasProbeLvlStats = true
        }
      }
      boolean isFocusedArray = sampleSet?.chipType?.technology?.name == "Focused Array"

      int control = 0
      def matWizard = MATWizard.findByAnalysisId(analysisInstance.id)
      if (matWizard) {
        control = analysisInstance.runDate.before(Date.parse("yyyy-MM-dd", "2012-03-30")) ? 1 : 0
      }
      def dsGroups = matPlotService.analysisGroups(analysisInstance, false)
      def groups = [:], cases = [], controls = []
      dsGroups.each { String key, Map gInfo ->
        groups.put(key, gInfo.color)
        if (gInfo.groupNum == control) {
          controls.push(key)
        } else {
          cases.push(key)
        }
      }

      def sampleSetFields = sampleSet ? [] : null
      if (sampleSet)
      {
        def spreadsheetData = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSet.id ], [ "spreadsheet" ])
        if (spreadsheetData?.spreadsheet?.header) {
          spreadsheetData.spreadsheet.header.each { String k, v ->
            if (v.overlay_visible == "show" && v.datatype == "string") {
              sampleSetFields.push([ key:"values.${k}".toString(), displayName:v.displayName ])
            }
          }
        }
        if (sampleSetFields.size() > 0) {
          sampleSetFields.add(0, [ key:"sampleId", displayName:"Array Data ID" ])
        }
      }
//      def sampleSetFields = sampleSet ? mongoDataService.getSampleSetFieldKeys(sampleSet.id, true) : null
//      if (sampleSetFields) {
//        sampleSetFields.add(0, [label:"sampleId", header:"Array Data ID"])
//      }
      def analyses = Analysis.findAllBySampleSetIdAndDataSetGroups(analysisInstance.sampleSetId,analysisInstance.dataSetGroups)
      analyses.remove(analysisInstance)
      def relatedAnalyses = analyses?.collect {
        return [id:it.id, name:it.datasetName]
      }
	  
	  //def matAdminRole = SecRole.findByAuthority('ROLE_MATADMIN')

	  def editable = false
	  if (currentUsername)
	  {
		  if (currentUsername == analysisInstance.user || currentUser?.authorities.contains(SecRole.findByAuthority('ROLE_MATADMIN')))
		  {
			  editable = true
		  }
	  }
	  
      def hasValidSampleSet = sampleSet != null

      return [analysisInstance:analysisInstance, title:title, sampleSetName:sampleSetName, groupsName:groupsName, groups:groups, modGen:modGen, editable:editable,
          hasGSA:hasGSA, csvFiles:csvFiles, relatedAnalyses:relatedAnalyses, hasValidSampleSet:hasValidSampleSet,
          sampleSetFields:sampleSetFields, cases:cases, controls:controls, hasProbeLvlStats:hasProbeLvlStats, isFocusedArray:isFocusedArray]
    }
  }

  def overlayOptions = {
    long sampleSetId = params.long("sampleSetId")
    if (sampleSetId && SampleSet.exists(sampleSetId)) {
      Map result = chartingDataService.overlayOptions(sampleSetId)
      if (result?.categorical) {
        render result.categorical as JSON
      }
    }
    render ""
  }

  def scatterSortOptions = {
	  List <Analysis> analyses = []
	  def numericalFeatures = []
	  List results = []
	  long metaCatId = params.long("metaCatId")
	  MetaCat metaCat = MetaCat.findById(metaCatId)
	  metaCat.analyses.sort({ Analysis a, Analysis b -> a.id <=> b.id }).each {	analyses.push(Analysis.get(it.id)) }
	  analyses.eachWithIndex { analysis, idx ->
		  // overlayOptions already scans for whether this should be shown via overlay_visible:show
		Map features = chartingDataService.overlayOptions(analysis.sampleSetId)
	    if (features?.numerical) {
			numericalFeatures += features.numerical
		}
	  }

	  def seen = [:]
	  numericalFeatures.each {
		  if (!seen[it.displayName]) {
			  results.push(it)
			  seen[it.displayName] = true;
		  } 
	  }
	  //println "got numerical features: " + myresults
	  
	  if (results) {
		  render results as JSON
	  }
	  render ""
	}
  
  def correlationOptions = {
    long analysisId = params.long("id")
    Analysis analysis = Analysis.findById(analysisId)
    if (analysis) {
      if (SampleSet.exists(analysis.sampleSetId)) {
        long startTime = System.currentTimeMillis()
        Map matrix = matPlotService.prepareMatrix(analysis)
//        println "matrix complete @ ${System.currentTimeMillis() - startTime} ms"
        Map overlays = chartingDataService.overlayOptions(analysis.sampleSetId)
//        println "overlays complete @ ${System.currentTimeMillis() - startTime} ms"
        DoubleMatrix2D data = matrix.matrix
        List<String> samples = matrix.samples
        List<String> modules = matrix.modules

        List threads = []
        Map message = [:]
        List sortedCategoricalCorrelations = []
        List sortedNumericalCorrelations = []

        overlays.categorical.each { Map o ->
//          def t = Thread.start {
          String field = "${o.collection}_${o.datatype}_${o.key}".toString()
          def mongoCor = mongoDataService.findOne("correlations", [analysisId:analysisId, field:field], ["correlation"])
          Map correlation = [:]
          if (mongoCor) {
            correlation = mongoCor.correlation
          } else {
            correlation = matPlotService.calcCorrelation(data, analysis.sampleSetId, samples, modules, field, 10d)
            if (correlation) {
              mongoDataService.update("correlations", [analysisId:analysisId, field:field], [correlation:correlation])
            }
          }
          if (correlation) {
            double score = StatsService.quantile(correlation.values().collect { Map corrRslt -> Math.abs(corrRslt.statistic) }, 0.75)
            sortedCategoricalCorrelations.push([field:field, displayName:o.displayName, order:o.order, score:score])
          }
//          }
//          threads.push(t)
        }
//        threads*.join()
        try
        {
          sortedCategoricalCorrelations.sort { a, b ->
            a.order <=> b.order // was a.score <=> b.score
          }
        }
        catch (Exception e)
        {
          message = [error:true, message:"Problem with retrieving correlation variables."]
          render message as JSON
        }


        overlays.numerical.each { Map o ->
//          def t = Thread.start {
          String field = "${o.collection}_${o.datatype}_${o.key}".toString()
          def mongoCor = mongoDataService.findOne("correlations", [analysisId:analysisId, field:field], ["correlation"])
          Map correlation = [:]
          if (mongoCor) {
            correlation = mongoCor.correlation
          } else {
            correlation = matPlotService.calcCorrelation(data, analysis.sampleSetId, samples, modules, field, 10d)
            if (correlation) {
              mongoDataService.update("correlations", [analysisId:analysisId, field:field], [correlation:correlation])
            }
          }
          if (correlation) {
            double score = StatsService.quantile(correlation.values().collect { Map corrRslt -> Math.abs(corrRslt.statistic) }, 0.75)
            sortedNumericalCorrelations.push([field:field, displayName:o.displayName, order:o.order, score:score])
          }
//          }
//          threads.push(t)
        }
//        threads*.join()
        try
        {
          sortedNumericalCorrelations.sort { a, b ->
            a.order <=> b.order // was a.score <=> b.score
          }
        }
        catch (Exception e)
        {
          message = [error:true, message:"Problem with retrieving correlation variables."]
          render message as JSON
        }
        //            println "done @ ${System.currentTimeMillis() - startTime} ms"
        List correlations = [[displayName: "Categorical:", order:-1]]
        correlations.addAll(sortedCategoricalCorrelations)
        correlations.push([displayName: "Continuous:", order:-1])
        correlations.addAll(sortedNumericalCorrelations)
        render correlations  as JSON
      }
    }
    render ""
  }

  def matAnalysis = {
    redirect(action: "show", params: params)
  }

  def edit = {
    def analysisInstance = Analysis.get(params.id)
    if (!analysisInstance) {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
      redirect(action: "list")
    }
    else {
      return [analysisInstance: analysisInstance]
    }
  }

  def update = {
    def analysisInstance = Analysis.get(params.id)
    if (analysisInstance) {
      if (params.version) {
        def version = params.version.toLong()
        if (analysisInstance.version > version) {

          analysisInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'analysis.label', default: 'Analysis')] as Object[], "Another user has updated this Analysis while you were editing")
          render(view: "edit", model: [analysisInstance: analysisInstance])
          return
        }
      }
      analysisInstance.properties = params
      if (!analysisInstance.hasErrors() && analysisInstance.save(flush: true)) {
        flash.message = "${message(code: 'default.updated.message', args: [message(code: 'analysis.label', default: 'Analysis'), analysisInstance.id])}"
        redirect(action: "show", id: analysisInstance.id)
      }
      else {
        render(view: "edit", model: [analysisInstance: analysisInstance])
      }
    }
    else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
      redirect(action: "list")
    }
  }

  def delete = {
    def analysisInstance = Analysis.get(params.id)
    if (analysisInstance) {
      try {
        analysisInstance.delete(flush: true)
        flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
        redirect(action: "list")
      }
      catch (org.springframework.dao.DataIntegrityViolationException e) {
        flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
        redirect(action: "show", id: params.id)
      }
    }
    else {
      flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'analysis.label', default: 'Analysis'), params.id])}"
      redirect(action: "list")
    }
  }

  	// This is now handled in Bootstrap, Scott Presnell 1/29/2014
//  def upgrade() {
//	  Sql db = Sql.newInstance(dataSource)
//	  def upQuery = "UPDATE analysis SET platform_type = 'Microarray' WHERE platform_type = '' OR platform_type = NULL"
//	  db.execute(upQuery)
//	  render(template:"/upgrade", model:[table:"Analysis", result: 'succeeded'])
//  }

  def markForDeletion = {
    Analysis analysis = Analysis.findById(params.id)

    def deleteFlag = "TRUE".toString()

    def deleteState = params.int("deleteState")
    def colspan  = params.int("colspan")

    if (deleteState == 0) {
      deleteFlag = "NULL".toString()
    } else {
      deleteFlag = "'TRUE'".toString()
    }

    // println "markForDeletion id: " + params.id + " deleteState: " + params.deleteState + " deleteFlag: " + deleteFlag

    if (analysis)
    {
      def returnMsg = [message:"", error:false]

      try
      {
        Sql sql = Sql.newInstance(dataSource)
        sql.execute("UPDATE analysis SET flag_delete=${deleteFlag} WHERE id=${analysis.id}".toString())
        sql.close()
        if (deleteState == 1) {
          returnMsg.message = "<td colspan=\"${colspan}\">${analysis.datasetName} has been successfully marked for deletion</td><td style=\"text-align: center;\"><button class=\"ui-icon-undo-cross\" title=\"Restore\" onclick=\"javascript:markAnalysisForDeletion(this, ${analysis.id}, 0, ${colspan});\"/></td>"
        } else {
          returnMsg.message = "<td colspan=\"${colspan + 1}\">${analysis.datasetName} Restored</td>"
        }
      }
      catch (Exception e)
      {
        returnMsg.message = "There was an error in marking this analysis for deletion."
        returnMsg.error = true
      }
      render returnMsg as JSON
    }
  }

  def startThumbnailGeneration = {
    def analysis = Analysis.get(params.id)
    if (analysis) {
      def proc = Thread.start {
        matDataService.processResults(params.id)
      }
    }
    render ([analysisId:params.id, status:'ok'] as JSON)
  }

  def getAvailableGroupSets = {
    def sampleId = params.sampleId
    def groupSets = dm3Service.getAvailableGroupSets(sampleId)
    render ([groups:groupSets] as JSON)
  }

  def getGroupsInSet = {
    def groupId = params.groupId
    def groups = dm3Service.getGroupsInSet(groupId)
    render ([groups:groups] as JSON)
  }


  def listImages = {
    def parseTags = params.list('tag')
    def analysisId = params.analysisId

    def results = [:]
    def tags = [:]
    tags.put('tag1','3')
    tags.put('tag2','2')
    results.put("TAGS",tags)
    def images = []
    def image = [:]
    image['url'] = "testUrl"
    image['thumbnail'] = "testThumb"
    image['desc'] = "new Description"
    images.add(image)
    results.put('images',images)
    render results as JSON

  }


  def plotFile = {
    def analysis = params.id ? Analysis.get(params.long("id")) : null
    String plotName = params.plotName
    String sampleField = params.sampleField == "null" ? "sampleBarcode" : params.sampleField
    if (analysis && plotName)
    {
      String[] plotType = plotName.split("_")
      if (plotType[1] == "gsa" || plotType[1] == "focusedarray")
      {
        def cachedPlot = mongoDataService.findOne("matfiles", [analysisId:analysis.id, plotName:plotName], ["sampleIds","csvHeader","csv"])
        if (cachedPlot)
        {
          String header = "${cachedPlot.csvHeader}"
          if (cachedPlot.sampleIds)
          {
            if (sampleField)
            {
              Map sampleIdToLabel = [:]
              def result = mongoDataService.getColumnValuesAndSampleId(analysis.sampleSetId, sampleField)
              result.each { Map s ->
                if (s[sampleField] && !((String)s[sampleField]).isAllWhitespace())
                {
                  sampleIdToLabel.put(s.sampleId.toString(), s[sampleField])
                } else {
                  sampleIdToLabel.put(s.sampleId.toString(), s.sampleId)
                }
              }
              cachedPlot.sampleIds.each {
                header += ",\"${sampleIdToLabel.get(it)}\""
              }
            }
            else
            {
              cachedPlot.sampleIds.each {
                header += ",\"${it}\""
              }
            }
          }
          def filename = "${analysis.datasetName?.replaceAll(" ","_") ?: analysis.id}_${plotName}.csv"
          def output = "${header}${separator}${cachedPlot.csv}".getBytes()
          response.setContentType("application/octet-stream")
          response.setHeader("Content-Disposition", "attachment; filename=${filename}")
          response.outputStream << output
        }
      }
      else if (plotType[0] == "group")
      {
        double fdr = params.double("floor") ?: 0d
        boolean isPie = plotType[3] == "piechart"
        boolean top = plotType[2] == "top"
        def csv = matPlotService.exportGroupCsv(analysis, top, isPie, fdr)
        def filename = "${analysis.datasetName?.replaceAll(" ","_") ?: analysis.id}_${plotName}.csv"
        def output = csv.getBytes()
        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=${filename}")
        response.outputStream << output
      }
      else
      {
        boolean showRowSpots = params.showRowSpots != null ? params.boolean("showRowSpots") : true
        boolean showControls = params.showControls != null ? params.boolean("showControls") : true
        float floor = params.float("floor") ?: 0f
        boolean isPie = plotType[2] == "piechart"
        boolean isDiff = plotType[2] == "spot"
        def byCols = plotType[3] == "samples" ? "samples" : null
        def byRows = plotType[4] == "modules" ? "modules" : null
        boolean annotatedOnly = plotType[1] == "annotated"
        int moduleCount = plotType[1] == "top" ? 62 : -1

        def csv = matPlotService.exportCsv(analysis, plotName, annotatedOnly, isPie, isDiff,
            showRowSpots, showControls, floor, sampleField, moduleCount, byRows, byCols)

        def filename = "${analysis.datasetName?.replaceAll(" ","_") ?: analysis.id}_${plotName}.csv"
        def output = csv.getBytes()
        response.setContentType("application/octet-stream")
        response.setHeader("Content-Disposition", "attachment; filename=${filename}")
        response.outputStream << output
      }
    }
  }

  def modulePlot = {
    def analysis = params.id ? Analysis.get(params.long("id")) : null
    String plotName = params.plotName
    boolean showRowSpots = params.boolean("showRowSpots")
    boolean showControls = params.boolean("showControls")
    double maxFoldChange = params.double("maxFoldChange") ?: 2.0
    if (analysis && plotName)
    {
      // [group or ind][analysis_type/clustering][num_modules][display_type][col_cluster][row_cluster]
      String[] plotType = plotName.split("_")
      def cachedPlot = null//mongoDataService.findOne("matplots", [analysisId:analysis.id, plotName:searchName], ["jsonplot"])
      if (cachedPlot)
      {
        render cachedPlot.jsonplot
      }
      else
      {
        def plot = null
        if (plotType[0] == "group")
        {
          def moduleCount = plotType[2] == "top" ? 62 : 260
          double fdr = params.double("floor") ?: analysis.fdr
          if (plotType[1] == "gsa")
          {
            // Gene Set Analysis - Module Group Comparison
            plot = matPlotService.gsaPlot(analysis, plotName, moduleCount, fdr)
            plot.description = "Gene set analysis (GSA). Significance or intensity noted by color hue."
          }
          else if (plotType[1] == "difference")
          {
            if (plotType[3] == "piechart")
            {
              // Module Group Comparison file
              plot  = matPlotService.modPiePlot(analysis, plotName, moduleCount, fdr)
              plot.description = "Differential expression analysis performed using filters on fold change and difference. Percent with fold change up and/or down noted by color."
            }
            else if (plotType[3] == "spot")
            {
              // Module Group Comparison file
              plot  = matPlotService.modPlot(analysis, plotName, moduleCount, fdr)
              plot.description = "Differential expression analysis performed using filters on fold change and difference. Significance or intensity noted by color hue."
            }
          }
          else if (plotType[1] == "focusedarray")
          {
            plot = matPlotService.faPlot(analysis, plotName, maxFoldChange)
            plot.description = "Focused array plot with fold changes"
          }
        }
        else if (plotType[0] == "individual")
        {
          float floor = params.float("floor") ?: 0f
          String sampleField = params.sampleField == "null" ? "sampleBarcode" : params.sampleField
          boolean isPie = plotType[2] == "piechart"
          boolean isDiff = plotType[2] == "spot"
          def byCols = plotType[3] == "samples" ? "samples" : null
          def byRows = plotType[4] == "modules" ? "modules" : null
          if (plotType[1] == "all")
          {
            plot = matPlotService.individualModulePlot(analysis, plotName, false, isPie, isDiff, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, byRows, byCols)
//            plot = matPlotService.individualModulePlot(analysis, plotName, false, false, isPie, isDiff, floor, sampleField, byRows, byCols, showRowSpots)
          }
          else if (plotType[1] == "annotated")
          {
            plot = matPlotService.individualModulePlot(analysis, plotName, true, isPie, isDiff, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, byRows, byCols)
//            plot = matPlotService.individualModulePlot(analysis, plotName, false, true, isPie, isDiff, floor, sampleField, byRows, byCols, showRowSpots)
          }
          else if (plotType[1] == "top")
          {
            plot = matPlotService.individualModulePlot(analysis, plotName, false, isPie, isDiff, showRowSpots, showControls, floor, maxFoldChange, sampleField, 62, byRows, byCols)
          }
        }
        if (plot) {
          render plot as JSON
        }
      }
    }
    render ""
  }

  def moduleInfo = {
    def analysis = params.id ? Analysis.get(params.long("id")) : null
    String plotName = params.plotName
    if (analysis && plotName)
    {
      SampleSet ss = SampleSet.findById(analysis.sampleSetId)
      boolean isFocusedArray = ss.chipType?.technology?.name == "Focused Array"

      def plot = [:]
      String[] plotType = plotName.split("_")
      if (plotType[0] == "group")
      {
        String moduleCountDesc = plotType[2] == "top" ? "Showing the most general modules created in the first six rounds of algorithm" : "Showing all modules"
        if (plotType[1] == "gsa")
        {
          plot.title = "Gene Set Analysis - ${plotType[2].capitalize()} Modules"
          plot.description = "Gene set analysis (GSA). Significance or intensity noted by color hue. ${moduleCountDesc}."
        }
        else if (plotType[1] == "focusedarray")
        {
          plot.title = "TFA Fold Change Spot Chart - All Modules"
          plot.description = "Differential expression analysis performed using fold change comparing average of cases to average of controls.  Color hue represents fold change intensity where red indicates up-regulation and blue indicates down-regulation."
        }
        else if (plotType[1] == "difference")
        {
          if (plotType[3] == "piechart")
          {
            plot.title = "Percent Difference Pie Chart - ${plotType[2].capitalize()} Modules"
            plot.description = "Differential expression analysis carried out via linear models for microarray data (LIMMA). Percent with fold change up and/or down noted by color. ${moduleCountDesc}."
          }
          else if (plotType[3] == "spot")
          {
            plot.title = "Percent Difference Spot Chart - ${plotType[2].capitalize()} Modules"
            plot.description = "Differential expression analysis carried out via linear models for microarray data (LIMMA). Color hue denotes percent of probes significantly up-  or downregulated (red and blue respectively). ${moduleCountDesc}."
          }
        }
      }
      else if (plotType[0] == "individual")
      {
        String displayType = plotType[2] == "piechart" ? "Pie Chart" : "Spot Chart"
        String displayTypeDesc = plotType[2] == "piechart" ? "Percent with fold change up and/or down noted by color" : "Color hue denotes percent of probes significantly up-  or downregulated (red and blue respectively)"
        boolean cluster = plotType[3] == "samples" || plotType[4] == "modules"
        String clusterTitle = cluster ? " Clustered" : ""
        String clusterDesc = cluster ? " By " : ""
        if (cluster)
        {
          clusterDesc += plotType[3] == "samples" ? "samples" : ""
          clusterDesc += plotType[3] == "samples" && plotType[4] == "modules" ? " and modules" : plotType[4] == "modules" ? "modules" : ""
          clusterDesc += "."
        }
        if (isFocusedArray)
        {
          String moduleCountTitle = plotType[1] == "all" ? "All Modules" : "Annotated Only"
          String moduleCountDesc = plotType[1] == "all" ? "Showing all modules" : "Showing only those modules with a defined gene ontology."
          plot.title = "TFA Fold Change Spot Chart${clusterTitle} - ${moduleCountTitle}"
          plot.description = "Differential expression analysis performed using fold change comparing an individual to average of controls.  Color hue represents fold change intensity where red indicates up-regulation and blue indicates down-regulation. ${displayTypeDesc}.${clusterDesc} ${moduleCountDesc}."
        }
        else if (plotType[1] == "all")
        {
          plot.title = "Percent Difference${clusterTitle} ${displayType} - All Modules"
          plot.description = "Differential expression analysis performed using filters on fold change and difference. ${displayTypeDesc}.${clusterDesc} Showing all modules."
        }
        else if (plotType[1] == "annotated")
        {
          plot.title = "Percent Difference${clusterTitle} ${displayType} - Annotated Only"
          plot.description = "Differential expression analysis performed using filters on fold change and difference. ${displayTypeDesc}.${clusterDesc} Showing only those modules with a defined gene ontology."
        }
        else if (plotType[1] == "top")
        {
          plot.title = "Percent Difference${clusterTitle} ${displayType} - Top Modules"
          plot.description = "Differential expression analysis performed using filters on fold change and difference. ${displayTypeDesc}.${clusterDesc} Showing the most general modules created in the first six rounds of algorithm."
        }
      }
      if (plot) {
        render plot as JSON
      }
    }
    render ""
  }

  def getVersionIdForSampleSet = {
    def sampleSetId = params.sampleId
    def version = dm3Service.versionIdForSampleSet(sampleSetId)
    def res = [:]
    res.put('versionId', version)
    render res as JSON
  }


  def getChipIdForSampleSet = {
    def sampleSetId = params.sampleId
    def version = dm3Service.getChipIdForSampleSet(sampleSetId)
    def res = [:]
    res.put('versionId', version)
    render res as JSON
  }



  def datasetNames = {
    def term = params.term
    if (term && term != "")
    {
      String queryStartsWith = """SELECT id, dataset_name FROM analysis WHERE dataset_name LIKE '${term}%' ORDER BY dataset_name LIMIT 10""".toString()
      def ids = [], names = []
      Sql sql = Sql.newInstance(dataSource)
      try {
        sql.eachRow(queryStartsWith) { row ->
          names.add([text: row.dataset_name, url: "/analysis/show/${row.id}"])
          ids.push(row.id)
        }
      } catch (Exception e) {
        e.printStackTrace()
      }
      String query = """SELECT id, dataset_name FROM analysis WHERE dataset_name LIKE '%${term}%' ORDER BY dataset_name LIMIT 10""".toString()
      sql.eachRow(query) { row ->
        if (!ids.contains(row.id) && ids.size() < 10)
        {
          names.add([text: row.dataset_name, url: "/analysis/show/${row.id}"])
        }
      }
      sql.close()
      render names as JSON
    }
  }

  def sampleLabels = {
    long sampleSetId = params.long("sampleSetId")
    def field = params.field
    if (sampleSetId && field)
    {
      Map sampleIdToLabel = [:]
      List result = mongoDataService.getColumnValuesAndSampleId(sampleSetId, field)
      result.each {
        sampleIdToLabel.put(it.sampleId, it[field])
      }
      render sampleIdToLabel as JSON
    }
  }

  def correlation = {
	  
	if (!params.controller) { params.controller = controllerName}
	if (!params.action) { params.action = actionName}
  
    long analysisId = params.long("id")
    Analysis analysis = Analysis.findByIdAndFlagDeleteIsNull(analysisId)

    def sampleSet = SampleSet.findById(analysis.sampleSetId)
    String sampleSetName = sampleSet?.name
    def groupsName = DatasetGroupSet.findById(analysis.dataSetGroups)?.name
//	def title = analysis.displayName ?: analysis.datasetName.replaceAll(/[A-Z][a-z]/, { " ${it}".toString() }).replaceAll("\\s+", " ").trim()
    def title = analysis.displayName ?: analysis.datasetName
	params.useMTC = params.useMTC ?: true
	
    return [params: params, analysis:analysis, sampleSetName:sampleSetName, groupsName:groupsName, title:title]
  }

  def correlationFile = {
    long analysisId = params.long("analysisId")
    Analysis analysis = Analysis.findByIdAndFlagDeleteIsNull(analysisId)
    String field = params.field
    String title = params.title ?: field
    String moduleCount = params.moduleCount ?: "All"
    boolean clusterRow = params.clusterRow ? params.boolean("clusterRow") : false
    boolean clusterCol = params.clusterCol ? params.boolean("clusterCol") : false
    double floor = params.double("floor") ?: 10d
    double lFilter = params.double("lFilter") ?: 0d
    double uFilter = params.double("uFilter") ?: 0d
    if (analysis && field)
    {
      Map fields = [:]
      String[] titles = title.split(",")
      field.split(",").eachWithIndex { String f, int i ->
        fields.put(f, titles[i])
      }
      String csv = matPlotService.exportCorrelationCsv(analysis, fields, floor, lFilter, uFilter, moduleCount, clusterRow, clusterCol)
      def filename = "${analysis.id}_correlations.csv"
      def output = csv.getBytes()
      response.setContentType("application/octet-stream")
      response.setHeader("Content-Disposition", "attachment; filename=${filename}")
      response.outputStream << output
    }
  }

  def getCorrelation = {
    long analysisId = params.long("analysisId")
    Analysis analysis = Analysis.findByIdAndFlagDeleteIsNull(analysisId)
    String field = params.field
    String title = params.title ?: field
    String moduleCount = params.moduleCount ?: "All"
    boolean clusterRow = params.clusterRow ? params.boolean("clusterRow") : false
    boolean clusterCol = params.clusterCol ? params.boolean("clusterCol") : false
    double floor = params.double("floor") ?: 10d
    double lFilter = params.double("lFilter") ?: 0d
    double uFilter = params.double("uFilter") ?: 0d
    double pVallFilter = params.double("plFilter") ?: 0d
    double pValuFilter = params.double("puFilter") ?: 0d
    boolean histogram = params.histogram ? params.boolean("histogram") : false
    boolean useMTC = params.useMTC ? params.boolean("useMTC") : true
    if (analysis && field)
    {
      Map fields = [:]
      String[] titles = title.split(",")
      field.split(",").eachWithIndex { String f, int i ->
        fields.put(f, titles[i])
      }
      def values = null
      if (fields.size() > 1 || params.mode == "multi")
      {
        values = matPlotService.multiCorrelationPlots(analysis, fields, floor, lFilter, uFilter,pVallFilter, pValuFilter, moduleCount, clusterRow, clusterCol, useMTC)
      }
      else
      {
        if (histogram)
        {
          values = matPlotService.correlationHistogram(analysis, field, floor)
        }
        else
        {
          values = matPlotService.multiCorrelationPlots(analysis, fields, floor, lFilter, uFilter,pVallFilter, pValuFilter, moduleCount, clusterRow, clusterCol, useMTC)
//          values = matPlotService.correlationPlot(analysis, field, floor, lFilter, uFilter, moduleCount)
        }
      }
      if (values) {
        render values as JSON
      }
    }
    render ""
  }

  def getCorrelationScatterPlot = {
    long analysisId = params.long("id")
    String field = params.field
    String module = params.module
    if (analysisId && field && module)
    {
      Analysis a = Analysis.findByIdAndFlagDeleteIsNull(analysisId)
      def plot = matPlotService.corScatterPlot(a, module, field)
      if (plot) {
        render plot as JSON
      }
    }
    render ""
  }

  def matCompare = {
  }

  def moduleAnalysesPlot = {
	  long modGenId = params.long("id")
	  ModuleGeneration modGen = ModuleGeneration.findById(modGenId)
    if (modGen) {
      boolean showRowSpots = params.showRowSpots ? params.boolean("showRowSpots") : true
      float floor = params.float("floor") ?: 0f
      boolean annotatedOnly = params.moduleCount ? params.moduleCount == "annotated": false
      boolean isPie = params.pie ? params.boolean("pie") : false
      boolean clusterRows = params.clusterRows ? params.boolean("clusterRows") : false
      boolean clusterCols = params.clusterCols ? params.boolean("clusterCols") : false
      def plot = matPlotService.moduleAnalysesPlot(modGen, annotatedOnly, isPie, showRowSpots, floor, clusterRows, clusterCols)
      if (plot) {
        render plot as JSON
      }
    }
    render ""
  }

  def metaCompare = {
	  if (!params.controller) { params.controller = controllerName}
	  if (!params.action) { params.action = actionName}
	
	  def metaCatInstance = MetaCat.findById(params.id)
	  if (!metaCatInstance) {
		  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'metacat.label', default: 'MetaCat'), params.id])}"
		  redirect(action: "list")
	  }
	  else
	  {
		  def title = metaCatInstance.displayName
	  
		  return [metaCatInstance:metaCatInstance, title:title]
	  }
  }

  def metaCatPlot = {
	  long metaCatId = params.long("id")
	  String plotName = params.plotName
	  String[] plotType = plotName.split("_")
	  boolean showRowSpots = params.showRowSpots ? params.boolean("showRowSpots") : true
	  boolean annotatedOnly = params.moduleCount ? params.moduleCount == "annotated": false
	  boolean clusterRows = params.clusterRows ? params.boolean("clusterRows") : false
	  boolean clusterCols = params.clusterCols ? params.boolean("clusterCols") : false
	  def myModules = params.customModules ?: []
	  List<String> customModules = []
	  if (myModules instanceof String) {
		customModules = [myModules]
	  } else {
	  	customModules = myModules
	  }
	  boolean byCols = plotType[4] == "samples" ? true : false
	  boolean byRows = plotType[5] == "modules" ? true : false
	  int colLevel = params.int("colLevel") ?: 0
	  double maxFoldChange = params.double("maxFoldChange") ?: 2.0
	  //boolean isPie = params.pie ? params.boolean("pie") : false
	  boolean isPie = plotType[3] == "piechart"
	  boolean isDiff = plotType[3] == "spot"
	  def moduleCount = plotType[2] == "top" ? 62 : 260
	  boolean showControls = false // params.boolean("showControls")
	  float floor = params.float("floor") ?: 0f
	  def plot = []
	  //println "colLevel: " + colLevel
	  //println "plotname: " + plotName
	  //println "customModules: " + customModules
	  MetaCat metaCat = MetaCat.findById(metaCatId)
	  if (metaCat) {
		  if (plotType[0] == "group") {
			  if (plotType[2] == "all")
			  {
				  plot = matPlotService.groupMetaCatPlot(metaCat, false, isPie, showRowSpots, floor, -1, byRows, byCols)
			  }
			  else if (plotType[2] == "annotated")
			  {
				  plot = matPlotService.groupMetaCatPlot(metaCat, true, isPie, showRowSpots, floor, -1, byRows, byCols)
			  }
			  else if (plotType[2] == "top")
			  {
				  plot = matPlotService.groupMetaCatPlot(metaCat, false, isPie, showRowSpots, floor, 62, byRows, byCols)
			  }
		  }
		  else if (plotType[0] == "individual")
		  {
			String sampleField = params.sampleField ?: "sampleBarcode"
			if (plotType[2] == "all")
			{
			  plot = matPlotService.individualMetaCatPlot(metaCat, plotName, false, isPie, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, [], byRows, byCols, colLevel)
			}
			else if (plotType[2] == "annotated")
			{
			  plot = matPlotService.individualMetaCatPlot(metaCat, plotName, true, isPie, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, [], byRows, byCols, colLevel)
			}
			else if (plotType[2] == "top")
			{
				plot = matPlotService.individualMetaCatPlot(metaCat, plotName, false, isPie, showRowSpots, showControls, floor, maxFoldChange, sampleField, 62, [], byRows, byCols, colLevel)
			}
			else if (plotType[2] == "custom") 
			{
				plot = matPlotService.individualMetaCatPlot(metaCat, plotName, false, isPie, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, customModules, byRows, byCols, colLevel)
			}
		 }
  
		 if (plot) {
			  render plot as JSON
		  }
	  }
	  render ""
	}
  
  def metaScatter = {
	  if (!params.controller) { params.controller = controllerName}
	  if (!params.action) { params.action = actionName}
	
	  def metaCatInstance = MetaCat.findById(params.id)
	  if (!metaCatInstance) {
		  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'metacat.label', default: 'MetaCat'), params.id])}"
		  redirect(action: "list")
	  }
	  else
	  {
		  def title = metaCatInstance.displayName
	  
		  return [metaCatInstance:metaCatInstance, title:title]
	  }
  }
  
  def scatterAnalysesPlot = {
	  def plot = []
	  long metaCatId = params.long("id")
	  MetaCat metaCat = MetaCat.findById(metaCatId)
	  String plotName = params.plotName ?: "none"
	  String[] plotType = plotName.split("_")
	  boolean showRowSpots = params.showRowSpots ? params.boolean("showRowSpots") : true
	  boolean showZeroValues = params.showZeroValues ? params.boolean("showZeroValues") : false
	  boolean annotatedOnly = params.moduleCount ? params.moduleCount == "annotated": false
	  boolean clusterRows = params.clusterRows ? params.boolean("clusterRows") : false
	  boolean clusterCols = params.clusterCols ? params.boolean("clusterCols") : false
	  boolean byCols = plotType[4] == "samples" ? true : false
	  String byRows = plotType[5]
	  boolean isSigned = plotType[5] == "signed" ? true : false
	  boolean isPie = plotType[3] == "piechart"
	  boolean isDiff = plotType[3] == "spot"
	  def moduleCount = plotType[2] == "top" ? 62 : 260
	  boolean showControls = false // params.boolean("showControls")
	  float floor = params.float("floor") ?: 0f
	  int colLevel = params.int("colLevel") ?: 0
	  double maxFoldChange = params.double("maxFoldChange") ?: 2.0
	  def myModules = params.customModules ?: []
	  List<String> customModules = []
	  if (myModules instanceof String) {
		customModules = myModules.split(/\,/)
	  } else {
	  	customModules = myModules
	  }
	  //println "plotname: " + plotName
	  //println "customModules: " + customModules
	  String sampleField = params.sampleField ?: "sampleBarcode"
	  if (metaCat) {
		  if (plotType[2] == "all")
		  {
			plot = matPlotService.individualMetaCatScatter(metaCat, plotName, false, isSigned, showZeroValues, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, [], byRows, byCols, colLevel)
		  }
		  else if (plotType[2] == "annotated")
		  {
			plot = matPlotService.individualMetaCatScatter(metaCat, plotName, true, isSigned, showZeroValues, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, [], byRows, byCols, colLevel)
		  }
		  else if (plotType[2] == "top")
		  {
			  plot = matPlotService.individualMetaCatScatter(metaCat, plotName, false, isSigned, showZeroValues, showRowSpots, showControls, floor, maxFoldChange, sampleField, 62, [], byRows, byCols, colLevel)
		  }
		  else if (plotType[2] == "custom")
		  {
			  plot = matPlotService.individualMetaCatScatter(metaCat, plotName, false, isSigned, showZeroValues, showRowSpots, showControls, floor, maxFoldChange, sampleField, -1, customModules, byRows, byCols, colLevel)
		  }
	
		  //model = matPlotService.individualMetaCatScatter(metaCat, plotName, false, false, false, false, showControls, floor, maxFoldChange, sampleField, -1, [], false, false, colLevel)
		  
		  if (plot) {
			  render plot as JSON
		  }
	  }
	  render ""
  }

    def runArchiveMATCsv = {
    if (loggedIn)
    {
      SecUser user = springSecurityService.currentUser
      if (user.authorities.contains(SecRole.findByAuthority('ROLE_ADMIN'))) {
        matDataService.archiveCsvFiles()
      }
    }
    redirect(action:"list")
  }

  def runArchiveMATDesignCsv = {
    if (loggedIn)
    {
      SecUser user = springSecurityService.currentUser
      if (user.authorities.contains(SecRole.findByAuthority('ROLE_ADMIN'))) {
        matDataService.archiveDesignCsvFiles()
      }
    }
    redirect(action:"list")
  }

  // Ajax call to this action allows toggling the published bit in Analysis
  def togglePublished = {
    if (params.matId) {
//			println "togglePublished params: " + params
      def mySet = Analysis.get(params.long("matId"))
      mySet.matPublished = params.int("state")

      mySet.save()

      mySet.errors?.allErrors?.each{
        println "Analysis save error: " + it
      };
    }
    // Must return something.
    render(text: 'OK')
  }
  
  // Ajax call to this action allows toggling the published bit in Analysis
  def toggleMetacat = {
	if (params.matId) {
			println "toggleMetaCat params: " + params
	  def mySet = Analysis.get(params.long("matId"))
	  mySet.metacatPublished = params.int("state")

	  mySet.save()

	  mySet.errors?.allErrors?.each{
		println "Analysis save error: " + it
	  };
	}
	// Must return something.
	render(text: 'OK')
  }

}
