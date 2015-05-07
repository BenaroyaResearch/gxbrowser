package 	org.sagres

import common.ArrayData
import common.SecUser
import common.SecRole
import common.chipInfo.ChipType
import common.chipInfo.ChipData
import common.chipInfo.ChipsLoaded
import grails.converters.JSON
import javax.sql.DataSource
import groovy.sql.Sql
import org.sagres.geneList.GeneList
import org.sagres.rankList.RankList
import org.sagres.rankList.RankListType
import org.sagres.sampleSet.DatasetGroup
import org.sagres.sampleSet.DatasetGroupSet
import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.SampleSetLink
import org.sagres.sampleSet.SampleSetLinkDetail
import org.sagres.sampleSet.component.FileTag
import org.sagres.sampleSet.component.OverviewComponent
import org.sagres.sampleSet.component.SampleSetOverviewComponent
import org.springframework.dao.DataAccessException;
import org.springframework.web.multipart.commons.CommonsMultipartFile
import org.apache.xmlbeans.impl.xb.xmlconfig.NamespaceList.Member2.Item;

import java.lang.ref.ReferenceQueue.Null;
import java.util.regex.Pattern
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.sagres.importer.TextTableSeparator
import org.sagres.sampleSet.annotation.SampleSetFile
import org.sagres.labkey.LabkeyReport
import org.sagres.sampleSet.SampleSetRole

class GeneBrowserController
{

  private def tg2Datatypes = ["Benaroya", "Baylor"]

  def grailsApplication
  def dataSource
  def geneQueryService
  def mongoDataService
  def chartingDataService
  def tg2QueryService
  def ncbiQueryService
  def sampleSetService
  def sampleSetFilterService //injected
  def springSecurityService
  def notesService
  def labkeyReportService



  final String DEFAULT_GROUPING = "default_grouping";

  def beforeInterceptor = {
    //def grailsApplication = new DefaultGrailsApplication()
    SecUser user = springSecurityService.currentUser

      /**
    def setsRequiringLogin = grailsApplication.config.genomic_datasource_names_requiring_login
    def fengRoleName = grailsApplication.config.dm3.feng.permissions
    try {
        def fengRole = SecRole.findByAuthority(grailsApplication.config.dm3.feng.permissions)


      if ( actionName.equalsIgnoreCase("show") && (setsRequiringLogin.containsKey(sampleSetService.getDatasetType(Long.parseLong(params.id))?.name)
          && !(user?.authorities?.authority?.contains(fengRole)   || user?.authorities?.authority?.contains(fengRoleName))) )     {
        flash.message = "Please Login to view this Sample Set in the Gene Expression Browser"
          def forwardURI =  request.getAttribute("javax.servlet.forward.request_uri")

          session.setAttribute("loginTarget", forwardURI)
          params.put("loginTarget", forwardURI)
          params.loginTarget = forwardURI
        redirect(controller: 'login', action: 'auth', params:  params)
        return false
      }
    } catch (NumberFormatException nfe) {
      //Exception formating params.id when null
    }
    **/
      //Generic Role check using SampleSetRole
      try {
          if (params.id) {
              //Check for existing Roles
              def rolesToCheck = SampleSetRole.findAllBySampleSetId(params.id)
//              if (rolesToCheck && rolesToCheck.size() > 0) {

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
						  flash.message = "Please Login to view this sample set"
						  redirect(controller: 'login', action: 'auth')
						  return false
                      }
                  }
//              }
          }
      } catch (Exception ex) {
          println "Exception checking role ${ex.toString()}"
      }

      return true
  }

  def index =
    {
      redirect(action: "list", params: params)
    }



  def getLabkeyIdMapping(def id) {
    Map labkeyIdToSampleId = [:]
    if (SampleSet.exists(id)) {
      List samples = mongoDataService.getSamples(id)
      samples.each { Map kv ->
        labkeyIdToSampleId.put(kv.kv.sampleId, kv.sampleId)
      }
    }
    return labkeyIdToSampleId
  }

  def list =
    {
	  def start, published, filtered, built 
      if (params.gxb != null)
      {
        redirect(action: "show", params: params)
      }

      Sql sql = Sql.newInstance(dataSource)
	  if (session.getAttribute("logTiming")) {
		  start = System.currentTimeMillis()
	  }
      def publishedSampleSets = SampleSet.findAllByGxbPublishedAndMarkedForDeleteIsNull(1)
	  if (session.getAttribute("logTiming")) {
		  published = System.currentTimeMillis()
		  println "published SampleSets: " + (published - start) + "ms"
	  }

      def filterList = sampleSetFilterService.getFilterLists(sql, publishedSampleSets)
	  if (session.getAttribute("logTiming")) {
		  filtered = System.currentTimeMillis()
		  println "filtered SampleSets: " + (filtered - published) + "ms"
	  }

      def searchTerm = params.sampleSetSearch
      if (searchTerm && searchTerm.trim() != "")
      {
        publishedSampleSets.retainAll(sampleSetFilterService.textSearch(searchTerm))
        params.searchResults = params.sampleSetSearch
      }
      params.filterPanelShow = "true"

      params.foldChangeMin = params?.foldChangeMin ?: "2.0"

      def isInternal = params.boolean("briInternal")

      def sampleSetList = sampleSetFilterService.buildSampleSetList(sql, publishedSampleSets, isInternal)
	  if (session.getAttribute("logTiming")) {
		  built = System.currentTimeMillis()
		  println "built SampleSets: " + (built - filtered) + "ms"
	  }

      def model = [sampleSetList: sampleSetList, params:params]
      model.putAll(filterList)
      sql.close()

      return model
    }

  def filteredSampleSets = {
    Sql sql = Sql.newInstance(dataSource)
    def publishedSampleSets = SampleSet.findAllByGxbPublishedAndMarkedForDeleteIsNull(1)
    def filterList = sampleSetFilterService.getFilterLists(sql, publishedSampleSets)
    def searchResults = [], checkedBoxes = [:], dataResults= [:]
    sampleSetFilterService.filter(publishedSampleSets, params, searchResults, checkedBoxes, dataResults)

    def to = Math.min(5, searchResults.size())
    params.searchResults = searchResults[0..<to]

    params.foldChangeMin = params?.foldChangeMin ?: "2.0"

    def isInternal = params.boolean("briInternal")

    def model = [:]
    model.putAll(filterList)
    def sampleSetList = sampleSetFilterService.buildSampleSetList(sql, publishedSampleSets, isInternal)
    model.putAll([sampleSetList: sampleSetList, params: params, checkedBoxes: checkedBoxes, dataResults: dataResults])
    sql.close()

    render(view: "list", model: model)
  }

  def show = {
    params.max = 100

    SampleSet sampleSet = null

    def sampleSetPlatformInfo = null
    def sampleSetOverviewComponents = []
//	def sampleSetLinkComponents = [:]
    def errorMsg = null
    def defaultGroupSetID = ""

    //    long start = System.currentTimeMillis()
    // check to see if this is a link (miniurl) being passed back in
    if (params.gxb != null)
    {
      //			println params.gxb
      MiniURL miniUrl = MiniURL.findByMiniURL(params.gxb)
      if (miniUrl != null)
      {
        // double check that we are looking at geneBrowser & show
        if (miniUrl.controller == "geneBrowser" && miniUrl.action == "show")
        {
          sampleSet = SampleSet.get(miniUrl.paramsID.toLong())
          params.id = sampleSet.id
          def argList = miniUrl.args.split("&")
          argList.each {
            if (it.length() > 2)
            {
              def item = it.split("=")
              params.put(item[0], (item.length > 1 ? item[1] : ""))
            }
          }
        }
        //				println miniUrl.args
      }
      //      println "creation of miniUrl - ${System.currentTimeMillis() - start} ms"
    }

    if (params.id && sampleSet == null)
    {
      sampleSet = SampleSet.findByIdAndMarkedForDeleteIsNull(params.long("id"))
	  // Move the restriction of not Chip Files into the controller, because sometimes FileTag is incomplete.
	  def chipFiles = FileTag.findByTag('Chip Files')
	  if (chipFiles) { // reduce the List only if FileTag 'Chip Files' are found.
		  sampleSet.sampleSetFiles = sampleSet?.sampleSetFiles.findAll { "from SampleSetFiles WHERE tag != ${chipFiles.id}" }
	  }
      sampleSetOverviewComponents = SampleSetOverviewComponent.findAllBySampleSetId(params.id, [sort: "displayOrder", order: "asc"])
      sampleSetPlatformInfo = sampleSet?.sampleSetPlatformInfo
    }
    //      println "getting study info - ${System.currentTimeMillis() - start} ms"

    if (sampleSet)
    {
      def isTg2 = false
      if (sampleSet)
      {
		  def qData = tg2QueryService.getTg2QualityData(sampleSet.id)
		  if (qData) {
			  isTg2 = !qData?.get(0)?.isEmpty()
		  }

        defaultGroupSetID = (params.defaultGroupSetId != null) ? params.defaultGroupSetId : sampleSet.defaultGroupSet?.id
      }
      else
      {
        errorMsg = "You must supply a sample set ID to browse"
      }

	  //@todo need to fix this to make it data driven
      def labKey = null
      if (sampleSet?.clinicalDataSource?.id == 1)
      {
        labKey = grailsApplication.config.dm3.labkey.authentication.url + "/project/Studies/ITN029ST/Study%20Data/begin.view?"
        //labKey = "https://accesstrial.immunetolerance.org/project/Studies/ITN029ST/Study%20Data/begin.view?"
      }

      // if subset, need to retrieve chip type differently
      ChipType chip = ChipsLoaded.findBySampleSet(sampleSet)?.chipType
      if (chip == null && sampleSet.parentSampleSet) {
        chip = ChipsLoaded.findBySampleSet(sampleSet.parentSampleSet)?.chipType
      }
	  // Default is to show the normalized values
      def signalDataTable = "array_data_detail_quantile_normalized"
      if (sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7) {
        signalDataTable = "array_data_detail"
      } else if (chip.technology.name == "RNA-Seq") {
	  	signalDataTable = "array_data_detail_tmm_normalized"
      } else if (chip.technology.name == "Focused Array") {
        signalDataTable = "focused_array_fold_changes"
      }

	  // Default lable is simply ...
      params.defYAxisLabel = "Expression Values"
	  if (chip.technology.name == "RNA-Seq")
	  {
		  if (sampleSet.defaultSignalDisplayType?.id != 1) {
			params.defYAxisLabel = "Normalized Counts"
		  } else {
			params.defYAxisLabel = "Raw Counts"
			signalDataTable = "array_data_detail"
		  }
	  }
	  else if (sampleSet.defaultSignalDisplayType?.id == 2)
	  {
		  params.defYAxisLabel = "Log\u2082 Expession Values"
	  }
	  else if (sampleSet.defaultSignalDisplayType?.id == 6)
      {
		  params.defYAxisLabel = "Fold Change"
      }
      // println "Default YLabel: " + params.defYAxisLabel

      //    println "getting chip info - ${System.currentTimeMillis() - start} ms"

      def moduleLink = null
      if (params.analysisId)
      {
        moduleLink = "${createLink(controller:"analysis", action:"show", id:params.analysisId)}"
      }
      else if (sampleSet.id == 33)
      {
        moduleLink =   "${createLink(controller: 'analysis', action: 'matAnalysis', id:'8')}"
      }
      else if (sampleSet.id == 24)
      {
        moduleLink = "${createLink(controller: 'analysis', action: 'matAnalysis', id:'9')}"
      }
      //    println "creating module link - ${System.currentTimeMillis() - start} ms"

      boolean hasLabkeyData = mongoDataService.exists("sampleSet", [sampleSetId:sampleSet.id, labkey:true], null)
      List tabs = mongoDataService.findOne("sampleSetTabs", [ sampleSetId:sampleSet.id ], ["tabs"])?.tabs
      tabs?.sort {
        it.order
      }
      boolean hasTabs = tabs != null

      //    long start = System.currentTimeMillis()
      def rankLists = null
      def defaultRankList = -1
      if (sampleSet.defaultGroupSet)
      {
        rankLists = sampleSetService.getGroupSetRankLists(sampleSet.id, sampleSet.defaultGroupSet?.id).get(sampleSet.defaultGroupSet?.id)
        defaultRankList = sampleSet.defaultGroupSet?.defaultRankList?.id ?: sampleSet.defaultRankList?.id
      }
      else if (sampleSet.defaultRankList)
      {
        rankLists = [sampleSet.defaultRankList]
        defaultRankList = sampleSet.defaultRankList?.id
      }
	  if (defaultRankList < 0) {
		  println "sample set: " + sampleSet.id + " has no default rank list"
	  }
      def currentRankList = params.long("rankList") ?: defaultRankList
      def currentRankListType = currentRankList != -1 ? RankList.get(currentRankList)?.rankListType?.abbrev : null
	  def currentDescription = currentRankList != -1 ? RankList.get(currentRankList)?.description : null
	  def currentRankListClass = null
	  if (currentDescription) {
		  def dList = currentDescription?.tokenize(' ')
		  if (dList) {
			  currentRankListClass = dList.get(0)
		  }
	  }
//	  println "rankLists: " + rankLists
//	  println "currentRL: " + currentRankList
//	  println "currentRLT: " + currentRankListType
//	  println "currentRLC: " + currentRankList
	  
      //    println "rank lists = ${System.currentTimeMillis() - start} ms"
      def groupSets = [:]
      sampleSet.groupSets.each {
        groupSets.put(it.id, it.name)
      }
      if (hasLabkeyData) {
        groupSets.put("labkey:cohort", "TrialShare Cohorts")
        def lkgroups = labkeyReportService.loadTrialShareGroups()
        lkgroups.each { key, value ->
          groupSets.put("trialshare_group.${key}", value)
        }
      }



      [ params: params, sampleSet: sampleSet, errorMsg: errorMsg, defaultGroupSetID: defaultGroupSetID,
          labKey: labKey, rankLists: rankLists, moduleLink: moduleLink, isTg2: isTg2, signalDataTable:signalDataTable,
          sampleSetOverviewComponents: sampleSetOverviewComponents, geneLists:GeneList.list(),
          hasLabkeyData:hasLabkeyData, defaultRankList:defaultRankList, currentRankList:currentRankList,
		  currentRankListType:currentRankListType, currentRankListClass:currentRankListClass,
          hasTabs:hasTabs, tabs:tabs, groupSets: groupSets.sort(), sampleSetPlatformInfo: sampleSetPlatformInfo]
    }
    else
    {
        println "Redirectig to list gb.show"
      redirect(action:"list")
    }
  }

  private def visibleTabs(long sampleSetId)
  {
    def query = [:], existingFields = [:]
    query.put("sampleSetId", sampleSetId)
    def tabs = mongoDataService.find("sampleSet", query, ["tabs"], null, -1)
    if (tabs)
    {
      tabs[0]?.tabs?.sort { a, b ->
        a.order <=> b.order
      }.each { tab ->
        def tabName = tab.name
        def tabFields = tab.fields
        tabFields.each {
          String id = it.value
          String displayName = id.substring(id.lastIndexOf(".")+1)
          if (displayName.length() > 0)
          {
            it.displayName = displayName.encodeAsHumanize()
          }
        }
        existingFields.put(tabName, tabFields)
      }
    }
    return existingFields
  }

  def displayExpressionHistogram =
    {
      def sampleSetId = Long.parseLong(params.sampleSetId)
      def groupSetID = Long.parseLong(params.groupSetId)
      def probeId = params.probeId
      def chartType = params.chartType
      def grouping = params.grouping;

      // load groups
      if (grouping == DEFAULT_GROUPING)
      {
        println "use supplied group set ID"
      }
      else
      {
        println grouping
      }

      def sampleSet = SampleSet.get(sampleSetId)
      if (groupSetID == null)
      {
        sampleSet?.groupSets?.findAll {
          groupSetID = it.id
        }
      }


      def allData = [:]
      if (groupSetID > 0)
      {
        // kludge, use the latest defined sample set view as the default ... needs to be replaced by the default sample set view

        def result = chartingDataService.getSampleSetData(sampleSetId, groupSetID, probeId, 10.0, -1, (chartType == "canvasXpress"))
        switch(chartType) {
          case "canvasXpress":
            // { x: { key: [values] }, y: { vars: [variables], smps: [samples], data: [[data]] } }
            result.each { groupId, Map groupData ->
              def y = [vars: ["Group_${groupId}"], smps: groupData.samples.collect { it.toString() }, data: [groupData.data]]
              def gData = [data: [y:y], colors: groupData.colors]
              allData.put(groupId, gData)
            }
            break;
          case "d3":
            break;
          case "highcharts":
            // series: [ { name: "name", data: [] }, { name: "name", data: [] } ]
            def max = -1, groupNum = 1
            def dataArray = [], colors = [], categories = [], signalData = []
            result.each { groupId, Map groupData ->
              def groupName = DatasetGroup.get(groupId).name
              groupData.data.eachWithIndex { val, i ->
                if (dataArray.size() > i)
                {
                  dataArray[i].data.push([y: val, color: groupData.colors[i], sid: groupData.samples[i]])
                }
                else
                {
                  def newData = []
                  def j = 1
                  while (j < groupNum)
                  {
                    newData.push(null)
                    j++
                  }
                  newData.push([y: val, color: groupData.colors[i], sid: groupData.samples[i]])
                  dataArray.push([data: newData])
                }
              }
              def groupSize = groupData.data.size()
              while (groupSize < dataArray.size())
              {
                dataArray[groupSize].data.push(null)
                groupSize++
              }
              categories.push("${groupName}")
              if (groupData.data && groupData.data.size > 0)
                max = Math.max(max, groupData.data.max())
              groupNum++
            }
            allData = [data: dataArray, max: max, categories: categories]
            break;
        }
      }


      render (allData as JSON)

    }

  def getGeneList =
    {
	  String username = springSecurityService.currentUser?.username
      def geneList = geneQueryService.runGeneQuery(params)
      if (params.boolean("returnNotes") == true) {
        List symbols = geneList.collect {
          "GENESYMBOL:${it.symbol}".toString()
        }
        Map counts = notesService.tagCounts([ '$and': [['tags':[ '$in':symbols]] , [ '$or': [['privacy':false], ['user':username]]]]])
        geneList.each {
          it.notes = counts.get("GENESYMBOL:${it.symbol}".toString()) ?: 0
        }
      }
      def jsonResults = [ gl:geneList, term:params.term, count: geneList.size() ]
      render jsonResults as JSON
    }

  def queryAllGenes = {
    Sql sql = Sql.newInstance(dataSource)
    def geneList = geneQueryService.geneSearch(sql, params)
    sql.close()
    if (geneList) {
      def jsonResults = [ gl:geneList, term:params.term ]
      render jsonResults as JSON
    }
    render ""
  }

  def queryGeneList =
    {
      def htmlReturn = geneQueryService.queryGeneListHTML(params)

      def jsonResults = [ "gl": htmlReturn ]
      render jsonResults as JSON
    }

  //@todo need a cleaner way of handling the different datasources, but for now we just have to know what can be displayed and from where
  def getSampleDisplayInfo =
    {
      def sampleSetId = params.long("dsid"), sampleId = params.long("sid")
      def getTg2 = params.boolean("getTg2")

      def jsonResults = [:]
      def sampleBarcode = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], ["sampleBarcode"])?.sampleBarcode
      jsonResults.put("sample.barcode", sampleBarcode ?: sampleId)

      Map tabs = sampleSetId ? visibleTabs(sampleSetId) : null
      if (tabs)
      {
        ArrayData sample = ArrayData.get(sampleId)
        if (sample)
        {
          def keysToTab = [:], tg2KeysToTab = [:]
          tabs.each { key, values ->
            key = key.encodeAsKeyCase()
            def emptyKeys = [:]
            values.each { Map v ->
              if (v.externalDb)
              {
                def db = v.externalDb
                // query external db
                if (db == "ben_tg2" && sample.externalDB == "ben_tg2")
                {
                  tg2KeysToTab.put(v.value, key)
                  emptyKeys.put(v.value, null)
                }
              }
              else
              {
                keysToTab.put(v.value, key)
                emptyKeys.put(v.value, null)
              }
            }
            jsonResults.put(key, emptyKeys)
          }
          if (tg2KeysToTab.size() > 0)
          {
            // need to fix this to return only valid keys
            def tg2 = tg2QueryService.getSampleDisplayInfo(sample.externalID)
            if (tg2)
            {
              def d = tg2.data
              d.push([key: "sample_id", value: tg2.get("sample ID")])
              d.push([key: "sample_name", value: tg2.get("sample Name")])
              d.each {
                def k = it.key.encodeAsKeyCase()
                def tab = tg2KeysToTab.get(k)
                if (tab)
                {
                  def v = it.value
                  if (v instanceof Date)
                  {
                    v = String.format("%tF", v)
                  }
                  ((Map)jsonResults.get(tab)).put("ben_tg2."+k, v)
                }
              }
            }
          }

          def query = ["sampleSetId": sampleSetId, "sampleId": sample.id]
          def mongoSampleData = mongoDataService.find(null, query, keysToTab.keySet().toList(), null, -1)
          if (mongoSampleData)
          {
            mongoSampleData[0].each { key, value ->
              recurseCollection(keysToTab, key, key, value, jsonResults)
            }
          }
        }
      }
      else
      {
        jsonResults.put("defTg2", [:])
        ArrayData sample = ArrayData.get(params.long("sid"))
        if (sample && getTg2)
        {
          // need to fix this to return only valid keys
          def tg2 = tg2QueryService.getSampleDisplayInfo(sample.externalID)
          if (tg2)
          {
            def d = tg2.data
//          d.add(0, [key: "sample_id", value: tg2.get("sample ID")])
            d.add(0, [key: "sample_name", value: tg2.get("sample Name")])
            d.each {
              def v = it.value
              if (v instanceof Date)
              {
                v = String.format("%tF", v)
              }
              ((Map)jsonResults.get("defTg2")).put(it.key.encodeAsKeyCase(), v)
            }
          }
        }
      }
      render jsonResults as JSON
    }

  def geoSampleInfo = {
	def sampleSetId = params.long("dsid"), sampleId = params.long("sid")
    def sampleBarcode = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], ["sampleBarcode"]).sampleBarcode
    def geoResults = ncbiQueryService.queryGEO(sampleBarcode)
    geoResults.detailLink = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=${sampleBarcode}"
    render geoResults as JSON
  }

  def querySampleSetInfo = {
	  def linkResults = [:]
	  def sampleSet = SampleSet.findById(params.long("ssid"))
	  if (sampleSet) {
		  def geoSeries = sampleSetService.getGEOSeries(sampleSet)
		  if (geoSeries) {
			  def geoRef = null 
			  try {
			  	geoRef = ncbiQueryService.queryGEOSeries(geoSeries.seriesId)
			  } catch (Exception e) {
			  }
		  	  if (geoRef != null) {
				linkResults.put('geoData', geoRef.accession + ": " + geoRef.title)
			  } else {
			  	linkResults.put('geoData', geoSeries.seriesId)
			  }
			  linkResults.put('geoUrl', geoSeries.dataUrl)
		  }
	  
		  def pubmedData = sampleSetService.getPMIDData(sampleSet)
		  if (pubmedData) {
			  def articles = []
			  try {
				articles = ncbiQueryService.getArticles([pubmedData.pmId.toString()], 1)
			  } catch (Exception e) {
		  	  }
		  	  if (articles?.size() > 0) {
				linkResults.put('pubmedData', articles.get(0).Title)
				linkResults.put('pubmedDOI', articles.get(0).DOI)
			  } else {
			  	linkResults.put('pubmedData', pubmedData.pmId)
			  }
			  linkResults.put('pubmedUrl', pubmedData.dataUrl)
		  }
		  if (linkResults) {
			  render linkResults as JSON
		  }
	  }
	  render ""
  }
  
  def qcSampleInfo = {
    def result = [:]
    if (params.dsid && params.sid) {
      def sampleSetId = params.long("dsid"), sampleId = params.long("sid")
      def sampleBarcode = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], ["sampleBarcode"])?.sampleBarcode
      Sql sql = Sql.newInstance(dataSource)
      def tg2QcInfo = tg2QueryService.getTg2QualityDataForSample(sql, sampleBarcode)
      def tg2SampleInfo = tg2QueryService.getTg2SampleData(sql, sampleBarcode)
      if (tg2QcInfo) {
        result.put("qcInfo", tg2QcInfo)
      }
      if (tg2SampleInfo) {
        result.put("sampleInfo", tg2SampleInfo)
      }
      sql.close();
    }
    if (result) {
      render result as JSON
    }
    render ""
  }

  def sampleGroupInfo = {
    def sampleSetId = params.long("dsid"), sampleId = params.long("sid")
    if (sampleSetId && sampleId)
    {
      Map out = [:], fieldToTab = [:]
      List tabs = mongoDataService.findOne("sampleSetTabs", [ sampleSetId:sampleSetId ], ["tabs"])?.tabs
      if (tabs) {
        tabs.each { Map tab ->
          tab.fields.each { Map field ->
            fieldToTab.put(field.value.substring(7), tab.name.encodeAsKeyCase())
          }
          out.put(tab.name.encodeAsKeyCase(), [ labels:[:], data:[:], numFields:0 ])
        }
      }
      def sampleBarcode = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], ["sampleBarcode"])?.sampleBarcode
      def result = mongoDataService.getValuesForSample(sampleSetId, sampleId)
      if (result) {
        def sampleFields = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "spreadsheet.header" ])?.spreadsheet?.header
//        def sampleFields = mongoDataService.find("sampleField", null, null, null, -1)
        def labels = [:]
        sampleFields.each { String fKey, Map fieldInfo ->
          fieldInfo.key = fKey
          if (tabs && fieldToTab.containsKey(fKey)) {
            Map tabStuff = out.get(fieldToTab.get(fKey))
            tabStuff.labels.put(fKey, fieldInfo)
          } else {
            labels.put(fKey, fieldInfo)
          }
//          if (tabs && fieldToTab.containsKey(it.key)) {
//            Map tabStuff = out.get(fieldToTab.get(it.key))
//            tabStuff.labels.put(it.key, it)
//          } else {
//            labels.put(it.key, it)
//          }
        }
        Map formattedResult = [:]
        if (sampleBarcode)
        {
          if (tabs && fieldToTab.containsKey("sampleBarcode")) {
            Map tabStuff = out.get(fieldToTab.get("sampleBarcode"))
            tabStuff.labels.put("sampleBarcode", [displayName:"Sample ID", datatype:"string"])
            tabStuff.data.put("sampleBarcode", sampleBarcode)
            tabStuff.numFields++
          } else {
            labels.put("sampleBarcode", [displayName:"Sample ID", datatype:"string"])
            formattedResult.put("sampleBarcode", sampleBarcode)
          }
        }
        result.each { key, value ->
          if (value)
          {
            if (value instanceof Date)
            {
              if (tabs && fieldToTab.containsKey(key)) {
                Map tabStuff = out.get(fieldToTab.get(key))
                tabStuff.data.put(key, String.format("%tF", value))
                tabStuff.numFields++
              } else {
                formattedResult.put(key, String.format("%tF", value))
              }
            }
            else
            {
              if (tabs && fieldToTab.containsKey(key)) {
                Map tabStuff = out.get(fieldToTab.get(key))
                tabStuff.data.put(key, value)
                tabStuff.numFields++
              } else {
                formattedResult.put(key, value)
              }
            }
          }
        }
        if (tabs == null)
        {
          def groupInfo = [ labels:labels, data:formattedResult, numFields:formattedResult.size() ]
          out.put("group", groupInfo)
        }
        render out as JSON
      }
    }
    render ""
  }

  def tg2SampleInfo = {
    def sampleSetId = params.long("dsid"), sampleId = params.long("sid")
    if (sampleSetId && sampleId)
    {
      ArrayData sample = ArrayData.get(sampleId)
      if (sample && sample.externalDB == "ben_tg2")
      {
        def result = tg2QueryService.sampleInfo(sample.externalID)
        if (result)
        {
          render result as JSON
        }
      }
    }
    render ""
  }

  def labkeySampleInfo = {
    long sampleSetId = params.long("dsid"), sampleId = params.long("sid")
    if (sampleSetId && sampleId)
    {
      List<String> sampleColumns = []
      Map<String,LabkeyReport> reportToColumns = [:]
      LabkeyReport.findAllBySampleSetIdAndEnabledNotEqual(sampleSetId,false).each {
        if (it.sampleSetColumn) {
          reportToColumns.put(it.category, it)
          sampleColumns.push("values.${it.sampleSetColumn}".toString())
        }
      }
      sampleColumns = sampleColumns.unique()

      if (sampleColumns)
      {
        if (!sampleColumns.contains("values.labkeyid")) {
          sampleColumns.push("values.labkeyid")
        }
        //      def visitNumMap = ["-1":"-1 (archived)", "-2":"-2", "-2.0":"-2", "3":"H3", "M6":"M6", "9":"H9", "6":"H6"]
        //      def pidResult = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], ["values.labkeyid","values.visitnum"])
        def pidResult = mongoDataService.findOne("sample", [sampleSetId:sampleSetId, sampleId:sampleId], sampleColumns)
        if (pidResult) {
          String pid = pidResult.get("values").labkeyid
          //        String vStr = pidResult.get("values").visitnum instanceof Double ? ((Double)pidResult.get("values").visitnum).toInteger().toString() : pidResult.get("values").visitnum.toString()
          //        def visit = visitNumMap.get(vStr)
          def labkeyInfo = [:]
          reportToColumns.each { String collection, LabkeyReport lkRep ->
            //        ["labkeySubject","labkeyClinical","labkeyLabResults","labkeyFlow","labkeyAncillaryData"].each { collection ->
            if (collection == "labkeyAncillaryData")
            {
              labkeyInfo.put(collection, [data:[["participant_id":pid]]])
            }
            else
            {
              Map header = mongoDataService.findOne("sampleSet", [sampleSetId:sampleSetId], ["${collection}.header".toString()])
              if (header?.get(collection)) {
                Map theMatcher = [:]
                LabkeyReport theReport = reportToColumns.get(collection)
                if (pidResult.get("values").containsKey(theReport.sampleSetColumn)) {
                  theMatcher.put(theReport.reportColumn, pidResult.get("values").get(theReport.sampleSetColumn))
                }
                //              Map query = [participant_id:pid]
                if (collection in ["labkeyLabResults", "labkeyFlow"]) {
                  //                Pattern visitPattern = Pattern.compile(".*${visit}\$".toString(), Pattern.CASE_INSENSITIVE);
                  //                query.put("visit", visitPattern)
                  if (collection == "labkeyFlow") {
                    Pattern flowPattern = Pattern.compile(".* % of total \\(tube \\d+\\)\$".toString(), Pattern.CASE_INSENSITIVE);
                    theMatcher.put("flow_population", flowPattern)
                  }
                }
                List returnFields = null
                if (collection == "labkeyFlow") {
                  returnFields = [ "flow_population", "flow_value" ]
                } else if (collection == "labkeyLabResults") {
                  returnFields = [ "lbtest","lbdt","lbdy","lborres","lbcbl","lbpcbl" ]
                }
                List result = mongoDataService.find(collection, theMatcher, returnFields, null, -1)
                Map labels = [:]

                if (header?.get(collection)?.header) {
                  if (collection in ["labkeyLabResults", "labkeyFlow"]) {
                    header.get(collection).header.keySet().retainAll(returnFields)
                  }
                  int order = 0
                  header.get(collection).header.sort { it.value.order }.each { String hKey, Map hInfo ->
                    hInfo.order = order
                    labels.put(hKey, hInfo)
                    order++
                  }
                }

                labkeyInfo.put(collection, [labels:labels, data:result, numFields:(labels.size()-1)])
              }
            }
          }
          if (!labkeyInfo.isEmpty())
          {
            render labkeyInfo as JSON
          }
        }
      }
    }
    render ""
  }

  def rankLists = {
	def sampleSetId = params.long("sampleSetId")
    def groupSetId = params.long("groupSetId")
    if ((sampleSetId && ! groupSetId) || groupSetId.toString().startsWith("labkey") || groupSetId.toString().startsWith("trialshare_group")) {
      def sampleSet = SampleSet.get(sampleSetId)
      groupSetId = sampleSet.defaultGroupSet?.id
    }
    if (sampleSetId && groupSetId)
    {
      def defaultRankList = DatasetGroupSet.get(groupSetId).defaultRankList
	  if (! defaultRankList) {
		  println "sample set: " + sampleSetId + " group set: " + groupSetId + " has no default rank list - attempting to use sampleset default/range"
		  defaultRankList = RankList.findBySampleSetIdAndRankListTypeAndMarkedForDeleteIsNull(sampleSetId, RankListType.findByAbbrev("range"))
	  }
      def ranklists = sampleSetService.getGroupSetRankLists(sampleSetId, groupSetId)
      if (ranklists) {
        def gsRanklists = []
        ranklists.get(groupSetId).each { RankList rl ->
          gsRanklists.push([id:rl.id, name:rl.name, description:rl.description, rankListType:rl.rankListType.abbrev])
        }
  
		def defaultRankListType = defaultRankList?.rankListType?.abbrev
		def defaultDescription = defaultRankList != null ? defaultRankList?.description : null
		def defaultRankListClass = null
		if (defaultDescription) {
			def dList = defaultDescription?.tokenize(' ')
			if (dList) {
				defaultRankListClass = dList.get(0)
			}
		}
//		println "rankLists: " + gsRanklists
//		println "defaultRL: " + defaultRankList
//		println "defaultRLT: " + defaultRankListType
//		println "defaultRLC: " + defaultRankList

        def result = [defaultRankList:defaultRankList?.id, defaultRankListType:defaultRankListType, defaultRankListClass:defaultRankListClass, ranklists:gsRanklists]
        render result as JSON
      }
      render ""
    }
  }

  def overlayOptions = {
    long sampleSetId = params.long("sampleSetId")
    def model = chartingDataService.overlayOptions(sampleSetId)
    if (!model.categorical.isEmpty() || !model.numerical.isEmpty()) {
      render model as JSON
    }
    render ""
  }

  def scaleInfo = {
	  def sampleSetId = params.long("sampleSetId")
	  def displayType = SampleSet.get(sampleSetId)?.defaultSignalDisplayType?.id
	  def htmlScale = "Signal";
	  def raphaelScale = "Expression Values";

	  if (displayType)
	  {	  	  
		  if (displayType == 2)
		  {
			  htmlScale = "Log<sub>2</sub> Signal";
			  raphaelScale = "Log\u2082 Expession Values"
		  }
		  else if (displayType == 6)
		  {
			  htmlScale = "Fold Change";
			  raphaelScale = "Fold Change"
		  }
		  else if (displayType == 7)
		  {
			  htmlScale = "Log<sub>2</sub> Fold Change";
			  raphaelScale = "Log\u2082 Fold Change"
		  }
	  }
	  
	  def result = [htmlScale: htmlScale, raphaelScale: raphaelScale]
	  render result as JSON
  }
  
  private def recurseCollection(Map keysToTab, String baseKey, String key, Object value, Map results)
  {
    if (value instanceof Map)
    {
      value.each { newKey, newValue ->
        recurseCollection(keysToTab, "${baseKey}.${newKey}", newKey, newValue, results)
      }
    }
    else
    {
      if (keysToTab.containsKey(baseKey))
      {
        def tab = keysToTab.get(baseKey)
        def val = value
        if (key == "gender")
        {
          if (value == "M")
            val = "Male"
          else if (value == "F")
            val = "Female"
        }
        if (val instanceof Date)
        {
          val = String.format("%tF", val)
        }
        ((Map)results.get(tab)).put(baseKey, val)
      }
    }
  }

  def displayGene =
    {
      def jsonResults = [:]

      def sampleSet = SampleSet.get(params.id)

      render jsonResults as JSON


    }

  def samplesFileImportService
  def importLabkeyData = {
    def labkeyTabs =  grailsApplication.config.dm3.labkeyTabs
    //["labkeySubject":"Subject","labkeyClinical":"Clinical","labkeyLabResults":"Lab Results","labkeyAncillaryData":"Ancillary Data","labkeyFlow":"Flow Data"]
    return [labkeyTabs:labkeyTabs]
  }

  def saveLabkeyData = {
    long sampleSetId = params.long("sampleSetId")
    String tab = params.labkeyTab
    CommonsMultipartFile labkeyExcelFile = params.labkeyFile
    def labkeyFile = cacheTempFile(labkeyExcelFile)
    if (labkeyFile.getName().endsWith(".csv"))
    {
      samplesFileImportService.importLabkeyCSVData(sampleSetId, labkeyFile, tab, TextTableSeparator.CSV)
    }
    else if (labkeyFile.getName().endsWith(".tsv"))
    {
      samplesFileImportService.importLabkeyCSVData(sampleSetId, labkeyFile, tab, TextTableSeparator.TSV)
    }
//    else
    //    {
    //      samplesFileImportService.importLabkeyData(sampleSetId, labkeyFile, tab)
    //    }
    labkeyFile.delete()
    redirect(action:"importLabkeyData")
  }

  def getGroupSets = {
    def sampleSet = SampleSet.get(params.long("sampleSetId"))
    if (sampleSet)
    {
      def groupSets = sampleSet.groupSets.collect {
        [name:it.name, id:it.id]
      }
      def result = [groupSets:groupSets.sort { it.id }, defaultGroupSet:sampleSet.defaultGroupSet.id]
      render result as JSON
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

  def crossProject = {
    def probeId = params.probeID
    def geneSymbol = params.geneSymbol
    def geneId = params.geneID

	def _mysqlDb      = grailsApplication.config.dataSource.database
	
    if (probeId && geneSymbol && geneId)
    {
      Sql sql = Sql.newInstance(dataSource)

	  def chipInfo = []
      def sampleSets = []

	  def start = System.currentTimeMillis()
	  def chipPrep
	
	  def sampleSetQuery
	  
	  def tableQuery = """SELECT COUNT(*) AS 'table' FROM information_schema.tables WHERE table_schema = '${_mysqlDb}' AND table_name = 'chip_probe_symbol'"""
	  def rowCount = sql.rows(tableQuery.toString())
	  
	  println "table chip_probe_symbol: " + rowCount?.get(0)?.get('table').asBoolean()
	  
	  if (rowCount?.get(0)?.get('table') > 0) { // does chip_probe_symbol exist?
	  
		  // Get the list of chiptypes and their source fields
		  def chips = ChipType.findAll()
		  chips?.each { 
			def tableId		 = it.id
//			def tableName    = it.probeListTable
//			def probeColumn  = it.probeListColumn
//			def symbolColumn = it.symbolColumn

			def cData = ChipData.findById(it.chipDataId)
//			def chipType = cData.name
//			def chipManufacturer = cData.manufacturer
			chipInfo.push(['id': tableId, 'name': cData.name, 'manufacturer': cData.manufacturer]);
		  }
		  //println "chipInfo: " + chipInfo.findAll{it.get('id') == 3}.get(0)

		  try {
			  if (session.getAttribute("logTiming")) {
				  chipPrep = System.currentTimeMillis()
				  println "chip info setup query: " + (chipPrep - start) + "ms"
			  }
		  } catch (IllegalStateException ise) {
		  }
	
		  // WARNING: Uses cooked table chip_probe_symbol
		  sampleSetQuery = """SELECT c.chip_type_id 'ctid', a.id 'ssid', a.name 'name', MIN(r.rank) 'rank'
			  FROM sample_set a
			  JOIN rank_list_detail r ON r.rank_list_id = a.default_rank_list_id
			  JOIN chip_probe_symbol c ON c.chip_type_id = a.chip_type_id
			  WHERE a.gxb_published = 1 AND a.marked_for_delete IS NULL AND
			  c.symbol = '${geneSymbol}' AND r.probe_id = c.probe_id GROUP BY a.id ORDER BY rank ASC"""
	  
	  	  sql.eachRow(sampleSetQuery.toString()) { result ->
			Map cInfo = chipInfo.findAll{it.get('id') == result.ctid}.get(0)
			def name = "${result.rank} ${result.name} (" + cInfo.get('manufacturer') + ": " + cInfo.get('name') + ")"
			sampleSets.push(['id': result.ssid, 'name': name, 'rank': result.rank])
		  }
			
//	  	sampleSets = sampleSets.sort { it.rank }
	  
	  } else {

	   	chipPrep = System.currentTimeMillis()

		sampleSetQuery = """SELECT a.id, a.name, d.manufacturer, d.name 'chipType'
        	FROM sample_set a, chips_loaded b, chip_type c, chip_data d
        	WHERE a.gxb_published = 1 AND a.marked_for_delete IS NULL
        	AND (a.id = b.sample_set_id OR a.parent_sample_set_id = b.sample_set_id)
        	AND b.chip_type_id = c.id AND c.chip_data_id = d.id ORDER BY a.id DESC"""

		sql.eachRow(sampleSetQuery.toString()) {
	  		def name = """${it.name} (${it.manufacturer}: ${it.chipType})"""
			sampleSets.push([id:it.id, name:name])
		}
	  }
	  	  
	  try {
		  if (session.getAttribute("logTiming")) {
			  def end = System.currentTimeMillis()
			  println "cross query:" + sampleSetQuery
			  println "cross timing: " + (end - chipPrep) + "ms for " + sampleSets.size() + " sample sets"
		  }
	  } catch (IllegalStateException ise) {
	  }

      boolean hasTg2 = false, hasLabkey = false, hasTabs = false
      long currentSampleSetId = params.long("id") ?: sampleSets?.get(0).id
      def sampleSetOverviewComponents
      List tabs
      if (currentSampleSetId)
      {
        hasTg2 = !tg2QueryService.getTg2QualityData(currentSampleSetId)?.get(0).isEmpty()
        hasLabkey = mongoDataService.exists("sampleSet", [sampleSetId:currentSampleSetId, labkey:true], null)
        sampleSetOverviewComponents = SampleSetOverviewComponent.findAllBySampleSetId(currentSampleSetId, [sort: "displayOrder", order: "asc"])
        tabs = mongoDataService.findOne("sampleSetTabs", [ sampleSetId:currentSampleSetId ], ["tabs"])?.tabs
        if (tabs) {
          tabs.each { Map t ->
            t.key = t.name.encodeAsKeyCase()
          }
        }
        hasTabs = tabs != null
      }

      SampleSet sampleSet = SampleSet.get(currentSampleSetId)
      if (!params.id)
      {
        params.id = sampleSet.id
      }
      ChipType chip = ChipsLoaded.findBySampleSet(sampleSet)?.chipType
      if (chip == null && sampleSet.parentSampleSet) {
        chip = ChipsLoaded.findBySampleSet(sampleSet.parentSampleSet)?.chipType
      }
      def signalDataTable = chip.technology.id == 2 ? "array_data_detail_tmm_normalized" : "array_data_detail_quantile_normalized"
      if (chip.id == 27)
      {
        signalDataTable = "array_data_detail"
      }
      if (sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7)
      {
        signalDataTable = "array_data_detail"
      }

      params.defYAxisLabel = "Expression Values"
	  if (sampleSet.defaultSignalDisplayType?.id == 2)
	  {
		  params.defYAxisLabel = "Log\u2082 Expression Values"
	  }
	  else if (sampleSet.defaultSignalDisplayType?.id == 6)
      {
		  params.defYAxisLabel = "Fold Change"
      }

      sql.close()

	  def groupSets = [:]
	  sampleSet.groupSets.each {
		groupSets.put(it.id, it.name)
	  }

	  def defaultGroupSetID = sampleSet.defaultGroupSet?.id
	  
      Map curParams = [ probeID:params.probeID, geneID:params.geneID, geneSymbol:params.geneSymbol ]

      return [params:params, sampleSets:sampleSets, sampleSet:sampleSet, defaultGroupSetID: defaultGroupSetID, groupSets: groupSets.sort(),
		  sampleSetOverviewComponents:sampleSetOverviewComponents,
          hasTg2:hasTg2, hasLabkey:hasLabkey, signalDataTable:signalDataTable, tabs:tabs, hasTabs:hasTabs]
    }
  }

  def getSignalTable = {
    long sampleSetId = params.long("sampleSetId")
    def sampleSet = SampleSet.get(sampleSetId)
    if (sampleSet) {
      ChipType chip = ChipsLoaded.findBySampleSet(sampleSet)?.chipType
      if (chip == null && sampleSet.parentSampleSet) {
        chip = ChipsLoaded.findBySampleSet(sampleSet.parentSampleSet)?.chipType
      }
      def signalDataTable = "array_data_detail_quantile_normalized"
      if (chip.id == 27 || sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7) {
        signalDataTable = "array_data_detail"
      } else if (chip.technology.name == "RNA-Seq") {
        signalDataTable = "array_data_detail_tmm_normalized"
      } else if (chip.technology.name == "Focused Array") {
        signalDataTable = "focused_array_fold_changes"
      }
//      def signalDataTable = "array_data_detail_quantile_normalized"
//      if (chip.id == 27)
//      {
//        signalDataTable = "array_data_detail"
//      }
//      if (sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7)
//      {
//        signalDataTable = "array_data_detail"
//      }
      def result = [signalDataTable:signalDataTable]
      render result as JSON
    }
    render ""
  }

  def getStudyInfo = {
    long sampleSetId = params.long("sampleSetId")
    if (sampleSetId)
    {
      SampleSet ss = SampleSet.get(sampleSetId)

      def studyInfo = [:]
      studyInfo.put("Title", ss.name)
      studyInfo.put("Description", ss.description.replaceAll(/<\/?(?i:td)(.|\n)*?>/,"").decodeHTML())

      def components = SampleSetOverviewComponent.findAllBySampleSetId(sampleSetId, [sort: "displayOrder", order: "asc"])
      components.each { ssComponent ->
        def overviewComponent = OverviewComponent.get(ssComponent.componentId)
        String value = ss.sampleSetAnnotation.getProperty(overviewComponent.annotationName)
        if (value && !value.isAllWhitespace())
        {
          studyInfo.put(overviewComponent.name, value.decodeHTML())
        }
      }
      render studyInfo as JSON
    }
    render ""
  }

  def checkTg2Labkey = {
    long sampleSetId = params.long("sampleSetId")
    if (sampleSetId)
    {
      boolean hasTg2 = !tg2QueryService.getTg2QualityData(sampleSetId)?.get(0).isEmpty()
      boolean hasLabkey = mongoDataService.exists("sampleSet", [sampleSetId:sampleSetId, labkey:true], null)
      List tabs = mongoDataService.findOne("sampleSetTabs", [ sampleSetId:sampleSetId ], ["tabs"])?.tabs
      if (tabs) {
        tabs.each { Map t ->
          t.key = t.name.encodeAsKeyCase()
        }
      }
      boolean hasTabs = tabs != null
      def checks = [hasTg2:hasTg2, hasLabkey:hasLabkey, hasTabs:hasTabs, tabs:tabs]
      render checks as JSON
    }
  }

  def sampleSetQuery = {
    //String queryStartsWith = """SELECT id, name FROM sample_set WHERE name LIKE '${params.term}%' AND gxb_published = 1 AND marked_for_delete IS NULL ORDER BY name""".toString()

    String queryStartsWith = """SELECT a.id, a.name AS name, d.manufacturer, d.name AS 'chipType'
	FROM sample_set a, chips_loaded b, chip_type c, chip_data d
	WHERE a.name LIKE '${params.term}%' AND a.gxb_published = 1 and a.marked_for_delete IS NULL
	and (a.id = b.sample_set_id or a.parent_sample_set_id = b.sample_set_id)
	and b.chip_type_id = c.id
	and c.chip_data_id = d.id
	ORDER BY a.id desc""".toString()

    //println "in sampleSetQuery"

    def ids = [], titles = []
    Sql sql = Sql.newInstance(dataSource)
    try {
      sql.eachRow(queryStartsWith) { row ->
        def name = """${row.name} (${row.manufacturer}: ${row.chipType})"""
        titles.add([id:row.id, text:name])
        ids.push(row.id)
        //println "first: " + name
      }
    } catch (Exception e) {
      e.printStackTrace()
    }

    if (params.term != "") {
      //String queryMatches = """SELECT id, name FROM sample_set WHERE name LIKE '%${params.term}%' OR description LIKE '%${params.term}%' AND gxb_published = 1 AND marked_for_delete IS NULL ORDER BY name""".toString()

      String queryMatches = """SELECT a.id, a.name AS name, d.manufacturer, d.name AS 'chipType'
			FROM sample_set a, chips_loaded b, chip_type c, chip_data d
			WHERE a.name LIKE '%${params.term}%' OR a.description LIKE '%${params.term}%' AND a.gxb_published = 1 and a.marked_for_delete IS NULL
			and (a.id = b.sample_set_id or a.parent_sample_set_id = b.sample_set_id)
			and b.chip_type_id = c.id
			and c.chip_data_id = d.id
			ORDER BY a.id desc""".toString()

      sql.eachRow(queryMatches) { row ->
        if (!ids.contains(row.id))
        {
          def name = """${row.name} (${row.manufacturer}: ${row.chipType})"""
          titles.add([id:row.id, text:name])
        }
      }
    }
    // println "sampleQuery: done"
    sql.close()
    render titles as JSON
  }

  def getAllSymbols = {
    if (params.term != "") {
      String query = """SELECT DISTINCT(symbol) FROM gene_info WHERE symbol LIKE '${params.term}%' ORDER BY symbol LIMIT 10"""
      def geneSymbols = []
      Sql sql = Sql.newInstance(dataSource)
      try {
        sql.eachRow(query) { row ->
          geneSymbols.add(text: row.symbol)
        }
      } catch (Exception e) {
        e.printStackTrace()
      }
      sql.close()
      render geneSymbols as JSON
    }
  }

}
