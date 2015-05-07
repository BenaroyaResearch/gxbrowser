package org.sagres.charts

import common.SecUser
import common.chipInfo.ChipType
import grails.converters.JSON
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.sagres.sampleSet.DatasetGroup
import org.sagres.sampleSet.DatasetGroupDetail
import org.sagres.sampleSet.DatasetGroupSet
import org.sagres.sampleSet.SampleSet
import org.sagres.stats.StatsService
import org.sagres.labkey.LabkeyReport
import common.chipInfo.ChipsLoaded

class ChartsController {

  def dataSource
  def chartingDataService
	def mongoDataService
  def tg2QueryService
  def geneQueryService
	def labkeyReportService

  // Save images and copy to clipboard for canvas and svg
  def chartToImageService

  def springSecurityService



	def defaultColors = ["#0073e7","#f4000a","#ffbb1c","#00c6c0","#f10743","#2dab00","#0055ac","#b60009","#db9800",
                       "#008a86","#a10a32","#1d6e00","#3b9dff","#ff4951","#ffc849","#7dede9","#dc517b","#94e378"]
  def colorSchemes = [["#016b69","#6b2601","#817a19","#1d426b","#741d32","#30392b","#321f44","#016b2d","#4b4b4b","#bd6911",
                       "#dc5253","#5275dc","#494c81","#60412a","#4f7d6d","#6e6971","#6d7b63","#dc5270","#5f5f5f","#2b538b"],
                      ["#aea95c","#6cc9b4","#b48185","#75b868","#998e61","#c0d493","#8bb5c4","#c1dccf","#a78f6a","#849d3d",
                       "#6b70ad","#a14e64","#64918e","#8f8f8f","#3f3f3f","#7e87aa","#584183","#896454","#573647","#7186a3"],
                      ["#ee1c24","#00913f","#fff200","#007fda","#662d91","#9e005d","#a85b00","#ff276d","#1c00a6","#949494",
                       "#0fbcb1","#ff5a95","#00056a","#ff6621","#699f49","#50ff7e","#608fff","#00ae72","#92e700","#575757"],
                      ["#9b9b9b","#49407d","#c1728d","#7e6141","#773e61","#6b577b","#4f7e9e","#b6af59","#3a6f50","#a35054",
                       "#525252","#8d9468","#356f5c","#b19383","#323b2b","#75a385","#886750","#3e4380","#6b4b5c","#6e6e6e"],
                      ["#cfc7a4","#5a9e94","#005275","#002344","#a38650","#625439","#572c70","#3b6a6c","#f3c94a","#d53f72",
                       "#262c48","#52435c","#be4529","#ffb793","#485e88","#79859e","#6bb946","#534741","#0a2b8e","#21986d"],
                      ["#7cd7ff","#ffd91c","#94190d","#fd6300","#451327","#1f3640","#0085bf","#006746","#3fcaa4","#8f7bc5",
                       "#682300","#0021bc","#6cd1e5","#0279ff","#90cbae","#da932c","#ff6237","#ff778f","#952e4c","#499a77"]]

  def copyToClipboard = {
    byte[] image
    if (params.img)
    {
      image = chartToImageService.getClipboardImage(params.img, "img")
    }
    else if (params.svg)
    {
      image = chartToImageService.getClipboardImage(params.svg, "svg")
    }
    if (image)
    {
      response.setContentType("application/octet-stream")
      response.outputStream << image
    }
  }

  def saveImg = {
    if (params.img)
    {
      render chartToImageService.saveImg(params.img)
    }
    else if (params.svg)
    {
      render chartToImageService.saveSVG(params.svg)
    }
  }

  def downloadImg = {
    if (params.id)
    {
      chartToImageService.downloadImg(params.id, response, params.filename)
    }
  }

  def deleteImg = {
    if (params.id)
    {
      chartToImageService.deleteTempImg(params.id)
    }
  }
  // end (save and copy to clipboard)

  def show = { }

  def getDatasetGroups = {
    def sampleSet = SampleSet.get(params.sampleSetId)
    def groupSets = DatasetGroupSet.findAllBySampleSet(sampleSet)
    render (groupSets as JSON)
  }

  def getProbeIds = {
    long groupSetId = params.long("groupSetId")
    Map tables = chartingDataService.getProbeTables(groupSetId)

    if (tables) {
      Sql sql = Sql.newInstance(dataSource)
      def probes = []
      def probeQuery = """SELECT DISTINCT a.${tables.probeIdColumn} FROM ${tables.signalDataTable} a, dataset_group_detail d, dataset_group g
          WHERE g.group_set_id = ${groupSetId}
          AND d.group_id = g.id
          AND a.array_data_id = d.sample_id
          ORDER BY a.${tables.probeIdColumn}
		  LIMIT 10"""
      sql.rows(probeQuery.toString()).each {
        probes.push(it[tables.probeIdColumn])
      }
      render (probes as JSON)
    }
    return null
  }

  def getProbeId = {
    long sampleSetId = params.long("sampleSetId")
    params.sampleSetID = sampleSetId
    params.limit = 1
//	  def geneSymbol = params.symbol ? params.symbol : "CD4"
    def geneSymbol = geneQueryService.runGeneQuery(params)?.pop().symbol ?: "CD4"

    Sql sql = Sql.newInstance(dataSource)
    DatasetGroupSet ds = SampleSet.get(sampleSetId).groupSets?.iterator().next()
    DatasetGroup g = ds.groups?.iterator().next()
    DatasetGroupDetail d = g.groupDetails?.iterator().next()
    ChipType ct = d.getSample().chip.chipType
    def table = ct.getProbeListTable()
    def col = ct.getProbeListColumn()
    def symbolColumn = ct.getSymbolColumn()
    if (table == "probe_xref" && col.startsWith("illumina_v2")) {
      table = "illumina_human6_v2"
      col = "probe_id"
    }
    def symbolCol = symbolColumn ?: "symbol"
    def probeIdQuery = "SELECT ${col} FROM ${table} WHERE ${symbolCol} = '${geneSymbol}'".toString()
    def probeId = ((GroovyRowResult)sql.firstRow(probeIdQuery)).get(col)

    render probeId
    return probeId
  }

	def getHistogramDataAsOneGroup =
	{
		def sampleSetId = Long.parseLong(params.sampleSetId)
		def groupSetID = Long.parseLong(params.groupSetId)
		def probeId = params.probeId
		def chartType = params.chartType
		if (params.grouping == null)
			params.grouping = "default_grouping"


		def sampleSet = SampleSet.get(sampleSetId)
		if (groupSetID == null)
		{
			sampleSet?.groupSets?.findAll {
				groupSetID = it.id
			}
		}

		// get the correct floor value from the sample set (if log2 transform set to 0)
		def dFloor = 10.0
		def chartStep = 100
		// kludge for feng for now
//		if (sampleSetId == 33)
		if (sampleSet.rawSignalType?.id == 2)
		{
			dFloor = 0.0
			chartStep = 15
		}

		if (sampleSet.defaultSignalDisplayType?.id == 6)
		{
			dFloor = 0.0
			chartStep = 5
		}

		def groupList = null

		if (params.grouping != 'default_grouping')
		{
			// kludge for now, need to review with Kelly
//			def key = params.grouping.split("\\.")[1]
			groupList = mongoDataService.getSamplesForDynamicGroups(sampleSetId, params.grouping)
			groupList = chartingDataService.getDynamicSignalData(sampleSetId, probeId, groupList)
		}



//    { groupId: [samples: samples, groupId: groupId, sortedData: sortedData, colors: (colors as JSON)] }

		def allData = [:]
		if (groupSetID > 0)
		{

			// kludge, use the latest defined sample set view as the default ... needs to be replaced by the default sample set view
			def result

			if (groupList == null)
				result = chartingDataService.getSampleSetData(sampleSetId, groupSetID, probeId, dFloor, -1, (chartType == "canvasXpress"))
			else
				result = groupList
			switch(chartType) {
			  case "canvasXpress":
			    // { x: { key: [values] }, y: { vars: [variables], smps: [samples], sortedData: [[sortedData]] } }
			    result.each { groupId, Map groupData ->
			      def y = [vars: ["Group_${groupId}"], smps: groupData.samples.collect { it.toString() }, data: [groupData.data]]
			      def gData = [data: [y:y], colors: groupData.colors]
			      allData.put(groupId, gData)
			    }
			    break;
			  case "d3":
			    break;
			  case "highcharts":
			    // series: [ { name: "name", sortedData: [] }, { name: "name", sortedData: [] } ]
	        def max = -1d, groupNum = 1, numPoints = 0
				  def dataArray = [], colors = [], categories = [], signalData = []
	        result.eachWithIndex { groupId, Map groupData, nGroup ->
		        def groupName = groupData.name ? groupData.name : DatasetGroup.get(groupId).name

//	          def groupName = DatasetGroup.get(groupId).name
	            groupData.data.eachWithIndex { val, i ->
	              if (dataArray.size() > i)
	              {
	                dataArray[i].data.push([y: val, color: (groupData.colors ? groupData.colors[i] : defaultColors[nGroup%20]), sid: groupData.samples[i]])
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
	                newData.push([y: val, color: (groupData.colors ? groupData.colors[i] : defaultColors[nGroup%20]), sid: groupData.samples[i]])
	                dataArray.push([data: newData])
	              }
                max = Math.max(max, val)
                numPoints++
	            }
	          def groupSize = groupData.data.size()
	          while (groupSize < dataArray.size())
	          {
	            dataArray[groupSize].data.push(null)
	            groupSize++
	          }
	          categories.push("${groupName}")
/*
		        if (groupData.sortedData && groupData.sortedData.size > 0)
	            max = Math.max(max, groupData.sortedData.max())
*/
	          groupNum++
			    }
				  int n = (int)(max / chartStep)
				  max = (n + 1) * chartStep
	        allData = [data: dataArray, max: max, categories: categories, numPoints: numPoints, numGroups: result.keySet().size()]
			    break;
			}
		}


		render (allData as JSON)
	}

  def getHistogramDataAsGroups = {
    def sampleSetId = Long.parseLong(params.sampleSetId)
    def groupSetId = Long.parseLong(params.groupSetId)
    def probeId = params.probeId
    def chartType = params.chartType
    def sampleSet = SampleSet.get(sampleSetId)

//    { groupId: [samples: samples, groupId: groupId, sortedData: sortedData, colors: (colors as JSON)] }

    // get the correct floor value from the sample set (if log2 transform set to 0)
		def dFloor = 10.0
		def chartStep = 100
		if (sampleSet.rawSignalType?.id == 2)
		{
			dFloor = 0.0
			chartStep = 15
		}
		if (sampleSet.defaultSignalDisplayType?.id == 6)
		{
			dFloor = 0.0
			chartStep = 5
		}

    def result = chartingDataService.getSampleSetData(sampleSetId, groupSetId, probeId, dFloor, 10, (chartType == "canvasXpress"))

    def allData = [:]
    switch(chartType) {
      case "canvasXpress":
        // { x: { key: [values] }, y: { vars: [variables], smps: [samples], sortedData: [[sortedData]] } }
        result.each { groupId, Map groupData ->
          def y = [vars: ["Group_${groupId}"], smps: groupData.samples.collect { it.toString() }, data: [groupData.data]]
          def gData = [data: [y:y], colors: groupData.colors]
          allData.put(groupId, gData)
        }
        break;
      case "d3":
        break;
      case "highcharts":
        // series: [ { name: "name", sortedData: [] }, { name: "name", sortedData: [] } ]
        def allMax = -1.00
	      result.each { groupId, Map groupData ->
          def gData = [data: [[name: groupId, data: groupData.data]], colors: groupData.colors, max: groupData.data.max()]
          allMax = groupData.data.max() ? Math.max(allMax, groupData.data.max()) : allMax
          allData.put(groupId, gData)
        }
        int nItems = (int)(allMax / chartStep)
		    allMax = (nItems + 1) * chartStep
        allData.each { key, value ->
          value.max = allMax
        }
        break;
    }

    render (allData as JSON)
  }

  def getBoxplotDataAsGroups = {
    def sampleSetId = Long.parseLong(params.sampleSetId)
    def groupSetId = Long.parseLong(params.groupSetId)
    def probeId = params.probeId
    def chartType = params.chartType

    if (params.grouping == null)
			params.grouping = "default_grouping"

    def sampleSet = SampleSet.get(sampleSetId)
		if (groupSetId == null)
		{
			sampleSet?.groupSets?.findAll {
				groupSetId = it.id
			}
		}

    // get the correct floor value from the sample set (if log2 transform set to 0)
		def dFloor = 10.0
		def chartStep = 100
		if (sampleSet.rawSignalType?.id == 2)
		{
			dFloor = 0.0
			chartStep = 15
		}
		if (sampleSet.defaultSignalDisplayType?.id == 6)
		{
			dFloor = 0.0
			chartStep = 5
		}
		
    def groupList = null
    if (params.grouping != 'default_grouping')
		{
			// kludge for now, need to review with Kelly
			groupList = mongoDataService.getSamplesForDynamicGroups(sampleSetId, params.grouping)
			groupList = chartingDataService.getDynamicSignalData(sampleSetId, probeId, groupList)
		}

    def result = null
    if (groupList == null)
				result = chartingDataService.getSampleSetData(sampleSetId, groupSetId, probeId, dFloor, -1, (chartType == "canvasXpress"))
			else
				result = groupList

//    def result = chartingDataService.getSampleSetData(sampleSetId, groupSetId, probeId, 10.0, -1, (chartType == "canvasXpress"))
    def minMax = chartingDataService.getSampleSetDomain(groupSetId, probeId, 10.0)

    def allData = [:]

    if (probeId && !probeId.trim().isEmpty())
    {
//      Date now = Calendar.getInstance().getTime()
      def min = 0.0, max = 0.0
      switch(chartType) {
//        case "highcharts":
//          def boxData = []
//          result.each { groupId, Map groupData ->
//            ArrayList sampleData = groupData.data.sort()
//            boxData.add(calculateQuantiles(now.getTime(), sampleData))
//            now.setMonth(now.month+1)
//          }
//          allData.put("data", boxData)
//          break;
//        case "canvasXpress":
//          // { x: { key: [values] }, y: { vars: [variables], smps: [samples], sortedData: [[sortedData]] } }
//          result.each { groupId, Map groupData ->
//            def grouping = groupData.samples.collect { "Probe_${probeId}" }
//            def x = [Probe: grouping]
//            def y = [vars: ["Group_${groupId}"], smps: groupData.samples, data: [groupData.data]]
//            allData.put(groupId, [data: [x:x, y:y]])
//          }
//          break;
        case "d3":
          def plotData = [], labels = [], colors = [], samples = []
          def n = 0
          result.each { groupId, Map groupData ->
            def groupName = groupData.name ? groupData.name : groupData.groupName

            plotData.push(groupData.data)
            samples.push(groupData.samples)
  //          def data = [groupData.data.sort()]
  //          def gData = [data: data, minMax: minMax]
            labels.push(groupName)
            def color = groupData.groupColor
            if (!color)
            {
              color = defaultColors[n%20]
              n++
            }
            colors.push(color)
            min = Math.min(min, minMax.min)
            max = Math.max(max, minMax.max)
  //          allData.put(groupId, gData)
          }
          allData.put("samples", samples)
          allData.put("labels", labels)
          allData.put("colors", colors)
          allData.put("data", plotData)
          break;
      }
      int nItems = (int)(max / chartStep)
		  max = (nItems + 1) * chartStep

      allData.put("min", Math.floor(min))
      allData.put("max", Math.ceil(max))
    }

    render (allData as JSON)
  }

  private
  def List<Float> calculateQuantiles(long index, ArrayList sortedData)
  {
    int n = sortedData.size();

    float min, q1, median, q3, max
		if (n == 1)
		{
			min = q1 = median = q3 = max = sortedData[0]
		}
		else if (n == 2)
		{
			min = q1 = sortedData[0]
			median = (sortedData[0] + sortedData[1]) / 2;
			max = q3 = sortedData[1]
		}
		else
		{
      min = sortedData.min()
      max = sortedData.max()

			boolean even = isEven(n)
			double q1Pos, q3Pos
			double medianPos = n * 0.5
			int medianPosFloor = ((Double)Math.floor(medianPos)).intValue()
      q1Pos = medianPosFloor * 0.5
      q3Pos = (medianPosFloor + n) * 0.5
			int q1PosInt = ((Double)q1Pos).intValue()
      int q3PosInt = ((Double)q3Pos).intValue()
			if (even)
			{
				median = (sortedData[((Double)medianPos).intValue()-1] + sortedData[((Double)medianPos).intValue()]) / 2
				if (isEven(Math.ceil(medianPos)))
				{
					q1 = (sortedData[q1PosInt - 1] + sortedData[q1PosInt]) / 2
          q3 = (sortedData[q3PosInt - 1] + sortedData[q3PosInt]) / 2
				}
				else
				{
					q1 = sortedData[((Double)(Math.floor(q1Pos))).intValue()]
					q3 = sortedData[((Double)(Math.floor(q3Pos))).intValue()]
				}
			}
			else
			{
				median = sortedData[medianPosFloor]
				if (isEven(medianPosFloor))
				{
					q1 = sortedData[q1PosInt]
					q3 = sortedData[q3PosInt]
				}
				else
				{
					q1 = (sortedData[q1PosInt] + sortedData[q1PosInt + 1]) / 2
					q3 = (sortedData[q3PosInt - 1] + sortedData[q3PosInt]) / 2
				}
 			}
		}

		return [index, min, q1, q3, max]
	}

	def boolean isEven(double v)
	{
		return (v % 2) == 0
	}

  def getLineChartData = {
    def groupSetId = Long.parseLong(params.groupSetId)
    def chartType = params.chartType

    def result = chartingDataService.getGroupSetProbeData(groupSetId)

    def allData = []
    switch(chartType) {
      case "canvasXpress":
        // { x: { key: [values] }, y: { vars: [variables], smps: [samples], sortedData: [[sortedData]] } }
        def vars = [], data = []
        result.data.each { probeId, signals ->
          vars.push(probeId)
          data.push(signals)
        }

        def y = [vars: vars, smps: result.samples, data: data]
        allData.push([y:y])
        break;
      case "highcharts":
        result.data.each { probeId, signals ->
          allData.push([name: probeId, data: signals])
        }
        break;
    }
    render (allData as JSON)
  }

  def highchartsBipolarGraphs = {
  }

  def canvasXPressBipolarGraphs = {
    def sampleSetId = Long.parseLong(params.sampleSetId)
    def groupSetId = Long.parseLong(params.groupSetId)
    def probeId = params.probeId

    // returns a hash of [groupId:groupDataArray]
    def output = chartingDataService.getSampleSetData(sampleSetId, groupSetId, probeId, 10.0, 10, false)
  }

//  def bipolarGraph = {
//      def vars = []
//      def moduleRun = ModuleRun.findByName('Run1')
//      def sortedData = Module.findAllByModuleRun(moduleRun).collect {
//        vars.push(it.name)
//        return [it.negPercent, it.posPercent]
//      }
//      def smps = ['negPercent','posPercent']
//      def y = [vars: vars, smps: smps, sortedData: sortedData] as JSON
//
//      return [y:y, x:([Percentages:smps] as JSON)]
//    }

  def getTg2Histogram = {
    def sampleSetId = Long.parseLong(params.sampleSetId)
    def dataKeys = params.dataKeys ? params.dataKeys.split(",") : null
    def color = params.color ?: grailsApplication.config.groups.defaultColor
    def result = tg2QueryService.getHistogramData(sampleSetId, dataKeys, color)
    if (result)
    {
      render (result as JSON)
    }
    else
    {
      render null
    }
  }

	def groupSetHistogram = {
		double dFloor = 10.0
		long sampleSetId = params.long("sampleSetId")
		long groupSetId = 0;
		if (params.groupSetId.toString().startsWith("labkey") || params.groupSetId.toString().startsWith("trialshare_group")) {
			groupSetId = SampleSet.get(sampleSetId).defaultGroupSet?.id
		} else {
			groupSetId = params.long("groupSetId")
		}
		long rankListId = params.long("rankListId")
		boolean rankListExposed = (params.rankListVisibility == "hidden") ? false : true
		String probeId = params.probeId
		def lk_max
		String geneSymbol = params.geneSymbol
		String signalDataTable = params.signalDataTable
		String sort = params.sort ?: "none"
		String varType = params.varType ?: "none"

		def sampleSet = SampleSet.get(sampleSetId)
		if (sampleSet.defaultSignalDisplayType?.id == 6) {
			dFloor = 0.0
		}
		if (probeId != "") {
			Sql sql = Sql.newInstance(dataSource)
			def data = chartingDataService.getGroupSetProbeData(sql, rankListId, groupSetId, probeId, geneSymbol, signalDataTable, dFloor, sort, varType, rankListExposed)
			if (params.groupSetId?.toString()?.startsWith("labkey")) {
				//Handle Cohorts
				def points = [:]
				 lk_max = data.max
				def rldesc
				def rlabbrev
				def rltype
				data.groups.each {	group ->
					rldesc = group.rankListDescription
					rlabbrev = group.rankListAbbrev
					rlabbrev = group.rankListType
					group.points.each { point ->
						def val = point.y
						def id = point.data.id
						def barcode = point.data.barcode
						def pointData = [:]
						pointData.val = val
						pointData.id = id
						pointData.barcode = barcode
						points.put(id.toString(), pointData)
					}
				}
				def labkeyIdMapping = labkeyReportService.getLabkeyIdMapping()
				def cohortData = [:]
				def cohorts = labkeyReportService.loadLabkeyCohorts()
				def cohortLongs = [:]
				cohorts.each {	labkeyId, cohortName ->
					def gxbid = labkeyIdMapping.get(labkeyId)
                    if (!cohortName) {
                        cohortName = "Other"
                    }
					//println "$labkeyId  ${cohortName} $gxbid  ${points.size()}  ${points.containsKey(gxbid.toString())}"
					if (gxbid) {
						def pointData = points.get(gxbid.toString())
						if (!cohortData.containsKey(cohortName)) {
							cohortData.put(cohortName, [:])
							cohortLongs.put(cohortName, new ArrayList<Double>())
						}
						cohortData.get(cohortName).put(gxbid, pointData)
						cohortLongs.get(cohortName).push(pointData?.val)
					} else {
						//println "No GXB mapping for labkey id $labkeyId"
					}
				}
				data = [:]
				def groups = []
				int position = 0;
				cohortData.each { cohortlabel, gxbPoints ->
					List<Double> cLongs = cohortLongs.get(cohortlabel)
					def group = [:]
					def stats = [:]
					def quarts = [:]
					def pts = []
					group['label'] = cohortlabel
					group['color'] = defaultColors[groups.size()]
                    try {
					    quarts['first'] = StatsService.quantile(cLongs, 0.25, true)
					    quarts['second'] = StatsService.quantile(cLongs, 0.5, true)
					    quarts['third'] = StatsService.quantile(cLongs, 0.75, true)
                    } catch (Exception ex) {
                        quarts['first'] = 0
                        quarts['second'] = 0
                        quarts['third'] = 0
                        println "Exception using StatsService ${ex.toString()}"
                        //ex.printStackTrace()
                    }
					double amax = cLongs.max()
					double amin = cLongs.min()
					stats['id'] = 3213
					stats['groupSet'] = 33
					stats['max'] = amax
					stats['min'] = amin
					stats['quartiles'] = quarts

					gxbPoints.eachWithIndex { id, lpoint, index ->
						def thisPoint = [:]
						thisPoint.x = index
						thisPoint.y = lpoint?.val
						def pointData = [:]
						pointData.id = lpoint?.id
						pointData.barcode = lpoint?.barcode
						thisPoint.data = pointData
						pts.add(thisPoint)
					}
					group.points = pts
					group.data = stats
					group['rankListDescription'] = rldesc
					group['rankListAbbrev'] = rlabbrev
					group['rankListType'] = rltype
					groups.push(group)

				}
				data.groups = groups
				data.max = lk_max

			}
			if (params.groupSetId.toString().startsWith("trialshare_group")) {
			//Handle Groups
				def tsGroupId = params.groupSetId.split("\\.")[1]
				def groupLabel
				def lkgroups = labkeyReportService.loadLabkeyGroups()
				lkgroups.each {  g->
					if (g.id?.toString().equalsIgnoreCase(tsGroupId.toString())) {
						groupLabel = g.label
					}
				}
				def groupIds = labkeyReportService.loadLabkeyGroupMembers(groupLabel)
				def points = [:]
				 lk_max = data.max
				def rldesc
				def rlabbrev
				def rltype
				data.groups.each {	group ->
					rldesc = group.rankListDescription
					rlabbrev = group.rankListAbbrev
					rlabbrev = group.rankListType
					group.points.each { point ->
						def val = point.y
						def id = point.data.id
						def barcode = point.data.barcode
						def pointData = [:]
						pointData.val = val
						pointData.id = id
						pointData.barcode = barcode
						points.put(id.toString(), pointData)
					}
				}
				def labkeyIdMapping = labkeyReportService.getLabkeyIdMapping()
				def ssidsInGroup = []
				groupIds.each {  labkeyId ->
					ssidsInGroup.add(labkeyIdMapping.get(labkeyId).toString())
				}
				def groupData = [:]
				def otherLabel = "Other"
				groupData.put(otherLabel, [:])
				groupData.put(groupLabel, [:])
				def groupLongs = [:]
				groupLongs.put(otherLabel, new ArrayList<Double>())
				groupLongs.put(groupLabel, new ArrayList<Double>())

				points.each { key, value ->
					def localgroup = otherLabel
					if (ssidsInGroup.contains(key.toString())) {
						localgroup = groupLabel
					}
					groupData.get(localgroup).put(key, value)
					groupLongs.get(localgroup).push(value?.val)
				}
				data = [:]
				def groups = []
				int position = 0;
				groupData.each { glabel, gxbPoints ->
					List<Double> cLongs = groupLongs.get(glabel)
					def group = [:]
					def stats = [:]
					def quarts = [:]
					def pts = []
					group['label'] = glabel
					group['color'] = defaultColors[groups.size()]
                    try {

					quarts['first'] = StatsService.quantile(cLongs, 0.25, true)
					quarts['second'] = StatsService.quantile(cLongs, 0.5, true)
					quarts['third'] = StatsService.quantile(cLongs, 0.75, true)
                    }  catch (Exception ex) {
                        println "Exception using StatsService ${ex.toString()}"
                        quarts['first'] = 0
                        quarts['second'] = 0
                        quarts['third'] = 0

                    }
					double amax = cLongs.max()
					double amin = cLongs.min()
					stats['id'] = 3213
					stats['groupSet'] = 33
					stats['max'] = amax
					stats['min'] = amin
					stats['quartiles'] = quarts

					gxbPoints.eachWithIndex { id, lpoint, index ->
						def thisPoint = [:]
						thisPoint.x = index
						thisPoint.y = lpoint.val
						def pointData = [:]
						pointData.id = lpoint.id
						pointData.barcode = lpoint.barcode
						thisPoint.data = pointData
						pts.add(thisPoint)
					}
					group.points = pts
					group.data = stats
					group['rankListDescription'] = rldesc
					group['rankListAbbrev'] = rlabbrev
					group['rankListType'] = rltype
					groups.push(group)

				}
				data.groups = groups
				data.max = lk_max

			}
			data.probeId = probeId
			sql.close()

			if (!data.hitInfinity) {
				try {
					render data as JSON
				} catch (Exception e) {
					println "rendering as JSON failed: " + e.toString()
					println "check data transform (e.g. mis annotated data as log2 transformed)"
				}
			} else {
				def err = [error: [message: '<h4 style=\'color: red;\'>Underflow or overflow in transformed signal data for sampleSet ' + sampleSetId + '</h4>']]
				render err as JSON
			}
		}
		render ""
	}

	def groupSetOverlay = {
    long sampleSetId = params.long("sampleSetId")
    String field = params.field
    Integer colorSchemeIdx = params.colorScheme && params.colorScheme != "null" ? Integer.parseInt(params.colorScheme) : null
    if (sampleSetId && field)
    {
      def colorScheme = colorSchemeIdx != null ? colorSchemes[colorSchemeIdx.intValue()] : ["#f00"]
      def keys = field.split("_",2)

      boolean isLabkey = keys[0].startsWith("labkey")
      LabkeyReport theReport = isLabkey ? LabkeyReport.findBySampleSetIdAndCategory(sampleSetId, keys[0]) : null
      String sampleColumn = null
      Map sampleIds = [:]
      if (theReport?.sampleSetColumn && theReport?.reportColumn)
      {
        sampleColumn = isLabkey ? "values.${theReport.sampleSetColumn}".toString() : ""
        List queryResult = isLabkey ? mongoDataService.find("sample", [ sampleSetId:sampleSetId ], [ "sampleId", sampleColumn ], null, -1) : null
        queryResult?.each { Map sValues ->
          def sid = sValues.values?.get(theReport.sampleSetColumn)
          if (sid) { sampleIds.put(sid, sValues.sampleId) }
        }
      }

      if (keys[0] in [ "labkeyLabResults", "labkeyFlow" ])
      {
//        def visitNumMap = ["-1":"-1 (archived)", "-2":"-2", "-2.0":"-2", "3":"H3", "M6":"M6", "9":"H9", "6":"H6"]
//        List queryResult = mongoDataService.find("sample", [sampleSetId:sampleSetId], ["sampleId","values.labkeyid","values.visitnum"], null, -1)
//        List queryResult = mongoDataService.find("sample", [ sampleSetId:sampleSetId ], [ "sampleId", sampleColumn ], null, -1)
//        Map sampleIds = [:]
//        queryResult.each { Map sValues ->
//          def sid = sValues.values?.get(sampleColumn)
//          if (sid) { sampleIds.put(sid, sValues.sampleId) }
//        }
//        Map labkeyToSampleInfo = [:]
//        queryResult.each {
//          String vStr = it.values.visitnum instanceof Double ? ((Double)it.values.visitnum).toInteger().toString() : it.values.visitnum.toString()
//          def visit = visitNumMap.get(vStr)
//          if (visit) {

//            String labkeyId = it.values.labkeyid
//            if (labkeyToSampleInfo.containsKey(labkeyId)) {
//              labkeyToSampleInfo.get(labkeyId).put(it.sampleId, visit)
//            } else {
//              Map sampleInfo = [:]
//              sampleInfo.put(it.sampleId, visit)
//              labkeyToSampleInfo.put(labkeyId, sampleInfo)
//            }
//          }
//          labkeyToSampleInfo.put(it.values.labkeyid.toString(), [ sampleId:it.sampleId, visit:visitNumMap.get(vStr) ])
//        }
//        Map labkeyToSampleInfo = [:]
//        queryResult.each {
//          def labkeyId = it.values.labkeyid
//          def visit = visitNumMap.get(it.values.visitnum.toString())
//          if (labkeyToSampleInfo.containsKey(labkeyId)) {
//            labkeyToSampleInfo.get(labkeyId).put(it.sampleId, visit)
//          } else {
//            Map sampleInfo = [:]
//            sampleInfo.put(it.sampleId, visit)
//            labkeyToSampleInfo.put(labkeyId, sampleInfo)
//          }
//        }
        if (theReport.sampleSetColumn && theReport.reportColumn)
        {
          Map mongoInfo = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "${keys[0]}.tests.${keys[1]}", "${keys[0]}.config" ])
          Map mongoKey = mongoInfo?.get(keys[0])?.tests?.get(keys[1])
          String testName = mongoKey?.name ?: keys[1].replaceAll("_"," ").capitalize()
          String displayName = mongoKey?.displayName ?: keys[1].replaceAll("_"," ").capitalize()

          String testKey = keys[0] == "labkeyLabResults" ? "lbtest" : "flow_population"
          String testValue = keys[0] == "labkeyLabResults" ? "lborres" : "flow_value"
  //        Map testQuery = [participant_id:['$in':labkeyToSampleInfo.keySet().asList()]]
          Map testQuery = [:]
          testQuery.put(theReport.reportColumn, ['$in':sampleIds.keySet().toList()])
          testQuery.put(testKey, testName)
          List result = mongoDataService.find(keys[0], testQuery, [ theReport.reportColumn, testKey, testValue ], null, -1)
  //        List result = mongoDataService.find(keys[0], testQuery, [ "participant_id","visit",testKey,testValue ], null, -1)
          def data = []
          result.each { Map m ->
  //          if (m[testKey] == testName)
  //          {
  //            def sampleInfo = labkeyToSampleInfo.get(m.participant_id)
  //            if (sampleInfo) {
  //              sampleInfo.each { long sampleId, String visit ->
  //                if (m.visit?.endsWith(visit) && isValidNumber(m[testValue])) {
  //                  data.push([ sampleId:sampleId, "${keys[1]}":m[testValue] ])
  //                }
  //              }
  //            }
            def sampleId = sampleIds.get(m[theReport.reportColumn])
            if (isValidNumber(m[testValue])) {
              data.push([ sampleId:sampleId, "${keys[1]}":m[testValue] ])
            }
  //            println "${m.participant_id}, ${sampleInfo.sampleId} - ${sampleInfo.visit} <=> ${m.visit}"
  //            if (sampleInfo?.visit)
  //            {
  //              if (m.visit?.endsWith(sampleInfo.visit))
  //              {
  //                data.push([sampleId:sampleInfo.sampleId, "${keys[1]}":m[testValue]])
  //              }
  //            }
  //          }
          }
          def model = [key:keys[1], categories:[:], data:data, displayName:displayName]
          render model as JSON
        }
      }
      else if (field.startsWith("labkey"))
      {
        List result = null
        if (theReport.sampleSetColumn && theReport.reportColumn)
        {
          Map header = mongoDataService.findOne("sampleSet", [sampleSetId:sampleSetId], ["${keys[0]}.header.${keys[1]}".toString()])
          Map fieldInfo = header?.get(keys[0]).header.get(keys[1])
          String displayName = fieldInfo?.displayName
          String datatype = fieldInfo?.datatype ?: "string"
  //        List sampleIds = mongoDataService.getColumnValuesAndSampleId(sampleSetId, "values.labkeyid")
  //        List sampleIds = mongoDataService.getColumnValuesAndSampleId(sampleSetId, sampleColumn)
  //        Map labkeyToSample = [:]
  //        sampleIds.each {
  //          def labkeyId = it["values.labkeyid"]
  //          if (labkeyToSample.containsKey(labkeyId)) {
  //            labkeyToSample.get(labkeyId).push(it.sampleId)
  //          } else {
  //            labkeyToSample.put(labkeyId, [it.sampleId])
  //          }
  //        }
  //        List result = mongoDataService.find(keys[0], [participant_id:['$in':labkeyToSample.keySet().asList()]], ["participant_id",keys[1]], null, -1)
          Map query = [:]
          query.put(theReport.reportColumn, ['$in':sampleIds.keySet().asList()])
          result = mongoDataService.find(keys[0], query, [ theReport.reportColumn,keys[1] ], null, -1)
          int i = 0
          def categories = [:]
          def samplesToRemove = []
          def resultsToAdd = []
          result.each { Map m ->
            def sampleId = sampleIds.get(m[theReport.reportColumn])//labkeyToSample.get(m.participant_id)
            if (sampleId) {
              sampleId.eachWithIndex { long sId, int idx ->
                def val = m[keys[1]]
                boolean invalidValue = false
                if (datatype == "number" && val instanceof String) {
                  if (!val.isNumber()) {
                    samplesToRemove.push(m)
                    invalidValue = true
                  }
                }
                if (!invalidValue) {
                  Map cloneResult = m.clone()
                  cloneResult.sampleId = sId
                  resultsToAdd.push(cloneResult)
                  if (datatype == "string")
                  {
                    boolean skip = false
                    if (val instanceof String && ((String)val).trim().isAllWhitespace())
                    {
                      skip = true
                    }
                    if (!skip && !categories.containsKey(val)) {
                      categories.put(val, colorScheme[i])
                      i++
                    }
                  }
                }
              }
            }
            else
            {
              samplesToRemove.add(m)
            }
          }
          result.removeAll(samplesToRemove)
          result.addAll(resultsToAdd)
          def model = [key:keys[1], categories:categories, data:result, displayName:displayName]
          render model as JSON
        }
      }
      else
      {
        String fieldKey = keys[1].substring(7)
        Map fieldKeyInfo =  mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "spreadsheet.header.${fieldKey}".toString() ])
        String datatype = fieldKeyInfo?.spreadsheet?.header?.get(fieldKey)?.datatype ?: "string"

        def categories = [:]
        if (datatype == "string") {
          def values = mongoDataService.getColumnValues(sampleSetId, keys[1])
          values.eachWithIndex { v, i ->
            if (v && v != "null") {
              if (v instanceof Double && v.intValue() == v) {
                categories.put(v.intValue(), colorScheme[i])
              } else if (v instanceof Date) {
                categories.put(String.format("%tF", v), colorScheme[i])
              } else {
                if (((String)v).trim() != "")
                {
                  categories.put(v, colorScheme[i])
                }
              }
            }
          }
        }
        def result = mongoDataService.getColumnValuesAndSampleId(sampleSetId, keys[1])
        if (result)
        {
          def data = []
          result.each {
            def v = it[keys[1]]
            if (v && v != "null") {
              if (v instanceof String) {
                if (datatype == "number") {
                  if (v.isInteger()) {
                    data.push([ sampleId:it.sampleId, "${keys[1]}":v.toInteger() ])
                  } else if (v.isDouble()) {
                    data.push([ sampleId:it.sampleId, "${keys[1]}":v.toDouble() ])
                  }
                } else if (datatype == "string") {
                  data.push(it)
                }
              }
              else
              {
                data.push(it)
              }
            }
          }
          def fieldInfo = mongoDataService.findOne("sampleField", [key:keys[1].substring(7)], null)
          def model = [key:keys[1], categories:categories, data:data, displayName:fieldInfo?.displayName?.encodeAsHumanize()]

          render model as JSON
        }
      }
    }
    render ""
  }

  private boolean isValidNumber(def value)
  {
    if (value instanceof String) {
      return value.isNumber()
    } else {
      return (value instanceof Number)
    }
  }

}
