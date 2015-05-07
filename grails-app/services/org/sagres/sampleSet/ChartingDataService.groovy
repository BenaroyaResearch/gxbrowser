package org.sagres.sampleSet

import common.chipInfo.ChipType
import common.chipInfo.ChipsLoaded
import common.ArrayData
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import java.awt.Color
import org.springframework.web.context.request.RequestContextHolder
import org.sagres.stats.StatsService
import org.sagres.rankList.RankList
import org.sagres.rankList.RankListType
import java.util.regex.Pattern
import java.util.regex.Matcher
import org.sagres.labkey.LabkeyReport

class ChartingDataService {

  def dataSource
  def grailsApplication
  def sampleSetService
  def mongoDataService
  def session = RequestContextHolder.currentRequestAttributes().getSession()

//  def getProbeData(String probeId, boolean rgbColors)
//  {
//    Sql sql = Sql.newInstance(dataSource)
//    def dataQuery = "SELECT a.array_data_id, a.signal " +
//      "from array_data_detail a " +
//      "where a.affy_id = '${probeId}' " +
//      "ORDER BY a.array_data_id LIMIT 100"
//
//    def samples = [], signals = []
//    sql.rows(dataQuery).each {
//      samples.push(it.array_data_id)
//      signals.push(it.signal)
//    }
//
//    sql.close()
//
//    return [name: probeId, data: signals, samples: samples]
//  }

  def getGroupSetProbeData(long groupSetId)
  {
    Sql sql = Sql.newInstance(dataSource)

    Map tables = getProbeTables(groupSetId)
    String signalDataTable = tables.signalDataTable
    String probeIdColumn = tables.probeIdColumn

    def data = [:]
    def samplesQuery = """SELECT DISTINCT a.array_data_id
      FROM ${signalDataTable} a, dataset_group_detail d, dataset_group g
      WHERE g.group_set_id = ${groupSetId}
      AND d.group_id = g.id
      AND a.array_data_id = d.sample_id"""
    def samples = sql.rows(samplesQuery.toString()).collect {
      it.array_data_id
    }

    def probeQuery = """SELECT DISTINCT a.${probeIdColumn}
      FROM ${signalDataTable} a, dataset_group_detail d, dataset_group g
      WHERE g.group_set_id = ${groupSetId}
      AND d.group_id = g.id
      AND a.array_data_id = d.sample_id"""
    sql.rows(probeQuery.toString()).each {
      data.put(it[probeIdColumn], new float[samples.size()])
    }

    def query = """SELECT a.array_data_id AS sampleId, a.${probeIdColumn} AS probeId, a.signal AS probeSignal
      FROM ${signalDataTable} a, dataset_group_detail d, dataset_group g
      WHERE g.group_set_id = ${groupSetId}
      AND d.group_id = g.id
      AND a.array_data_id = d.sample_id"""
    sql.rows(query.toString()).each {
      int col = samples.indexOf(it.sampleId)
      data.get(it.probeId)[col] = it.probeSignal
    }

    sql.close()

    return [data: data, samples: samples]
  }

  def getSampleSetDomain(long groupSetId, String probeId, float floor)
  {
    if (probeId && !probeId.trim().isEmpty())
    {
      Map tables = getProbeTables(groupSetId)
      String signalDataTable = tables.signalDataTable
      String probeIdColumn = tables.probeIdColumn

      Sql sql = Sql.newInstance(dataSource)
      def minMaxQuery = "SELECT MAX(a.signal) 'maxSignal', MIN(a.signal) 'minSignal', COUNT(a.signal) 'n' " +
        "FROM ${signalDataTable} a, dataset_group_detail d, dataset_group g " +
        "WHERE g.group_set_id = ${groupSetId} AND d.group_id = g.id " +
        "AND a.array_data_id = d.sample_id " +
        "AND ${probeIdColumn} = '${probeId}'"
      GroovyRowResult row = sql.firstRow(minMaxQuery)

      def n = row.get("n")
      def min = 0, max = 0
      if (n > 0)
      {
        min = row.get("minSignal")
        min = floor ? Math.max(min, floor) : min
        max = row.get("maxSignal")
      }

      sql.close()
      return [min: min, max:max, n:n]
    }
    return null
  }

	def getDynamicSignalData(long sampleSetID, String probeID, Map groupData)
	{
    SampleSet ss = SampleSet.findById(sampleSetID)
    if (ss) {
      Map tables = getProbeTables(ss)

      def sql = new Sql(dataSource)
      def retMap = [:]
      groupData.each { it  ->
                String sampleList = ""
                def gd = it.value
                gd.samples.each { sampleID ->
                  if (sampleList.size() > 0)
                    sampleList += ","
                  sampleList += "'" + sampleID + "'"
                }
                GString query = """select `signal` from ${tables.normalizedDataTable}
                  where array_data_id in (${sampleList})
                  and ${tables.probeIdColumn} = '${probeID}'"""
                def groupInfo = [:]
                groupInfo["samples"] = gd.samples
                groupInfo["groupId"] = -1
                groupInfo["name"] = it.key
                def signalData = []
                sql.eachRow(query.toString()) { row ->
                  signalData.push(row.signal)
                }
                groupInfo["data"] = signalData
                retMap.put(it.key, groupInfo)

              }

      return retMap
    }
    return null
	}

  def getSampleSetData(long sampleSetId, long groupSetId, String probeId, float floor, int limit, boolean rgbColors)
  {
    def sampleSet = SampleSet.get(sampleSetId)
    def groupSet = DatasetGroupSet.findBySampleSetAndId(sampleSet, groupSetId)

    def groupsData = [:]
    groupSet.groups.each {
      groupsData.put(it.id, getGroupData(sampleSet, it.id, probeId, floor, limit, rgbColors))
    }

    return groupsData
  }

  def getGroupData(SampleSet sampleSet, long groupId, String probeId, float floor, int limit, boolean rgbColors)
  {
    def group = DatasetGroup.get(groupId)
    def sampleLimit = limit ?: grailsApplication.config.samples.maxNumDisplay
    def defaultColor = group.hexColor ?: grailsApplication.config.groups.defaultColor
    def probe = probeId ?: "1653355"

    Sql sql = Sql.newInstance(dataSource)
    def sampleQuery = "SELECT sample_id, hex_color FROM dataset_group_detail " +
                      "WHERE group_id=${group.id} " +
                      "ORDER BY display_order, sample_id ASC"
    if (limit > 0)
    {
      sampleQuery += " LIMIT ${sampleLimit}"
    }
    def data = [], samples = [], colors = []

	  // samples and color query
    sql.rows(sampleQuery).each {
      samples.push(it.sample_id)
      colors.push(it.hex_color ?: defaultColor)
    }

    while (colors.size() < 10)
    {
      colors.add(defaultColor)
    }

    if (rgbColors)
    {
      colors = convertToRgb(colors)
    }

	  // determine which table has the signal data
	  ChipType chip = ChipsLoaded.findBySampleSet(sampleSet)?.chipType
    if (chip == null && sampleSet.parentSampleSet)
    {
      chip = ChipsLoaded.findBySampleSet(sampleSet.parentSampleSet)?.chipType
    }
//	  ChipType chipType = chip.get(chip.chipType.id)
//    ChipType chip = sampleSetService.getChipType(sampleSet.id)
    def sampleSetCheck = "AND a.sample_set_id = ${sampleSet.parentSampleSet?.id ?: sampleSet.id} "

    Map tables = getProbeTables(sampleSet)
//    def signalDataTable = chip.technology.id == 2 ? "array_data_detail_tmm_normalized" : "array_data_detail_quantile_normalized"
    if (chip.id == 27)
    {
//      signalDataTable = "array_data_detail"
      sampleSetCheck = ""
    }
	if (sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7)
	{
//		signalDataTable = "array_data_detail"
		sampleSetCheck = ""
	}
	if ( chip.technology.name == "Focused Array" )
	{
		sampleSetCheck = "AND a.focused_array_fold_change_params_id = " +
				sampleSet.defaultFoldChangeParams?.id + " ";
	}
    // data query
    def max = -1.0
//    def query = "SELECT a.signal FROM array_data_detail a, dataset_group_detail d " +
    def query = "SELECT ${tables.signalColumn} FROM ${tables.normalizedDataTable} a, dataset_group_detail d " +
      "WHERE d.group_id = ${group.id} " +
      "AND a.array_data_id = d.sample_id " +
      "AND a.${tables.probeIdColumn} = '${probe}' " +
      sampleSetCheck +
      "ORDER BY d.display_order, d.sample_id ASC"
    if (limit > 0)
    {
      query += " LIMIT ${sampleLimit}"
    }
    sql.eachRow(query.toString()) {
	    try
	    {
//        floor = floor ? floor : 10
		    def signal = it.signal < floor ? floor : it.signal
		    def sig = (int)Math.round(signal * 100)
		    signal = sig / 100
//        def signal = (it.signal == null) || (it.signal < 0) ? floor : Math.max(floor, Math.log(it.signal))
//        signal = Math.max(floor, Math.log(signal))
//		    def signal = it.signal ? Math.max(floor, Math.log(signal)) : floor
//        def signal = it.signal ? Math.log10(it.signal) : floor
	      data.push(signal)
        max = Math.max(max, signal)
	    }
	    catch (Exception e)
	    {
		    data.push(floor)
        max = Math.max(max, floor)
	    }
    }
    sql.close()

    def defaultSignalType = sampleSet.defaultSignalDisplayType
    def rawSignalType = sampleSet.rawSignalType

    if (rawSignalType.id == 2 || rawSignalType.id == 7)
    {
      if (defaultSignalType == null || defaultSignalType.id == 1 || defaultSignalType.id == 6)
      {
        data = data.collect {
          return Math.pow(2, it)
        }
      }
    }
    else if (rawSignalType?.id == 1 && defaultSignalType?.id == 2)
    {
      data = data.collect {
        return Math.log(it) / Math.log(2)
      }
    }

    return [samples: samples, groupId: groupId, data: data, colors: colors, groupName: group.name, groupColor: group.hexColor]
  }

  private def convertToRgb(List hexColors)
  {
    return hexColors.collect {
      Color c = Color.decode(it)
      "rgb(${c.red},${c.green},${c.blue})"
    }
  }

  def getNormalizedData(int groupId)
  {
  }

  	//TODO: this could be replaced with chip_probe_symbol
	def String getProbeIdForCrossProject(long sampleSetID, String geneSymbol, String origProbeId)
	{
		if (geneSymbol == null)
			return null

		String szNewProbeID = null
		def queryString = """SELECT c.probe_list_table, c.probe_list_column, c.symbol_column
			FROM sample_set a, chips_loaded b, chip_type c
			WHERE (a.id = b.sample_set_id or a.parent_sample_set_id = b.sample_set_id) AND b.chip_type_id = c.id
			AND a.id = ${sampleSetID}"""

		Sql sql = Sql.newInstance(dataSource)
		def row = sql.firstRow(queryString.toString())
		if (row != null)
		{
			def probeSelect = """SELECT ${row.probe_list_column} 'probeID' FROM ${row.probe_list_table} WHERE ${row.symbol_column} = '${geneSymbol}'"""
			sql.eachRow(probeSelect.toString()) {
				// use the original probeID if we have it.
				if (it.probeId == origProbeId)
				{
					szNewProbeID = it.probeID
				}
				// Otherwise use the first probe.
				if (szNewProbeID == null)
				{
					szNewProbeID = it.probeID
				}
			}
		}

		return szNewProbeID
	}

  def getGroupSetProbeData(Sql sql, long rankListId, long groupSetId, String probeId, String geneSymbol, String signalDataTable, double floor, String sortField, String varType, boolean rankListExposed)
  {
	String first = ""
	String second = ""
	
	def start, groupSets, probeData // timing
	try {
		if (session.getAttribute("logTiming")) {
			start = System.currentTimeMillis()
		}
	} catch (IllegalStateException ise) {
	}
	
	def rankList = RankList.get(rankListId)
	def rankListType
	if (rankList != null && rankListExposed) {
		Pattern groupPattern = Pattern.compile("([\\w]+)\\s([\\w\\s\\-\\+\\/\\=\\.]+)\\svs\\s([\\w\\s\\-\\+\\/\\=\\.]+)")
		Matcher groupMatcher = groupPattern.matcher(rankList.description)

		if (groupMatcher.find()) {
			first = groupMatcher.group(2)
			second = groupMatcher.group(3)
		}
		rankListType = RankListType.get(rankList.rankListTypeId)
	}
	
    def groupSet = DatasetGroupSet.get(groupSetId)

    if (groupSet && probeId != "")
    {
	  def sampleSet = groupSet.sampleSet
      def chartStep = 100
      if (sampleSet.rawSignalType?.id == 2)
      {
        chartStep = 15
      }
	  if (sampleSet.defaultSignalDisplayType?.id == 6 || sampleSet.defaultSignalDisplayType?.id == 1)
	  {
		  chartStep = 2
	  }
	  
      def groups = []
      double max = 0.0

	  // FIXME: If gene symbol is not null we are probably in the cross project browser and we should update the probe ID that we
	  // are looking for, a known bug right now is that we are taking the first probe returned should there be more than
	  // one probe mapped to the symbol ...  still need to figure out how to handle this properly
	  // Update: check if the current probe id is a member of the returned set.  If so, use it, assuming it corresponds to the same sequence
	  if (geneSymbol != null)
	  {
		  probeId = getProbeIdForCrossProject(sampleSet.id, geneSymbol, probeId)
	  }

      List sortedSampleIds = null
      if (sortField != "none") {
        String[] fieldKeys = sortField.split("_",2)
        String collection = fieldKeys[0]

        LabkeyReport theReport = LabkeyReport.findBySampleSetIdAndCategory(sampleSet.id, collection)
        String sampleColumn = null
        if (theReport?.sampleSetColumn && theReport?.reportColumn)
        {
          sampleColumn = "values.${theReport.sampleSetColumn}".toString()
        }

        Map sorter = [:]
        if (collection.startsWith("labkey")) {
          if (sampleColumn) {
//          String hKey = fieldKeys[1]
//          if (collection == "labkeyLabResults") {
//            hKey = "result_in_original_units"
//          } else if (collection == "labkeyFlow") {
//            hKey = "flow_population"
//          }

//          String field = "${collection}.header.${hKey}".toString()
//          Map header = mongoDataService.findOne("sampleSet", [ sampleSetId:groupSet.sampleSet.id ], [ field ])
//          boolean isNumber = header[collection].header[hKey].datatype == "number"

          List pids = []
          Map sampleToPids = [:]
//          List result = mongoDataService.find("sample", [ sampleSetId:groupSet.sampleSet.id ], [ "sampleId", "values.labkeyid", "values.visitnum" ], [ "sampleId":1 ], -1)
          List result = mongoDataService.find("sample", [ sampleSetId:sampleSet.id ], [ "sampleId", sampleColumn ], [ "sampleId":1 ], -1)
          if (result) {
//            def visitNumMap = ["-1":"-1 (archived)", "-2":"-2", "-2.0":"-2", "3":"H3", "M6":"M6", "9":"H9", "6":"H6"]
            result.each {
              String theId = it.values[sampleColumn]
              sampleToPids.put(theId, it.sampleId)
              pids.push(theId)
//              String vStr = it.values.visitnum instanceof Double ? ((Double)it.values.visitnum).toInteger().toString() : it.values.visitnum.toString()
//              def visit = visitNumMap.get(vStr)
//              if (visit) {
//                String labkeyId = it.values.labkeyid
//                if (sampleToPids.containsKey(labkeyId)) {
//                  sampleToPids.get(labkeyId).put(it.sampleId, visit)
//                } else {
//                  Map sampleInfo = [:]
//                  sampleInfo.put(it.sampleId, visit)
//                  sampleToPids.put(labkeyId, sampleInfo)
//                  pids.push(labkeyId)
//                }
//              }
            }

            if (collection in [ "labkeyLabResults", "labkeyFlow" ])
            {
              String testKey = collection == "labkeyLabResults" ? "lbtest" : "flow_population"
              String testValue = collection == "labkeyLabResults" ? "lborres" : "flow_value"

              Map mongoKey = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSet.id ], [ "${collection}.tests.${fieldKeys[1]}" ])?.get(collection)?.tests?.get(fieldKeys[1])
              String testName = mongoKey?.name ?: fieldKeys[1].replaceAll("_"," ").capitalize()

//              Map testQuery = [participant_id:['$in':pids]]
              Map testQuery = [:]
              testQuery.put(theReport.reportColumn, ['$in':pids])
              testQuery.put(testKey, testName)//fieldKeys[1].replaceAll("_"," "))
              testQuery.put(testValue, [ '$ne':null, '$ne':'' ])
//              List queryFields = [ "participant_id", "visit", testKey, testValue ]
              List queryFields = [ theReport.reportColumn, testKey, testValue ]
              List data = mongoDataService.find(collection, testQuery, queryFields, null, -1)
              if (data)
              {
                sortedSampleIds = []
                data.sort() {
                  def value = it[testValue]
                  if (value instanceof String) {
                    return Double.parseDouble(value)
                  }
                  return value
                }
                data.each {
                  if (sampleToPids.containsKey(it[theReport.reportColumn]))
                  {
                    sortedSampleIds.push(sampleToPids.get(it[theReport.reportColumn]))
                  }
//                  String pVisit = it.visit
//                  Map sampleIds = sampleToPids.get(it.participant_id)
//                  sampleIds.each { long sId, String sVisit ->
//                    if (pVisit?.endsWith(sVisit)) {
//                      sortedSampleIds.push(sId)
//                    }
//                  }
                }
              }
            } else {
              Map theQuery = [:]
              theQuery.put(theReport.reportColumn, ['$in':pids])
              if (varType == "numerical")
              {
                sortedSampleIds = []
//                List data = mongoDataService.find(collection, [ participant_id:[ '$in':pids ] ], [ "participant_id", fieldKeys[1] ], null, -1)
                List data = mongoDataService.find(collection, theQuery, [ theReport.reportColumn, fieldKeys[1] ], null, -1)
                if (data) {
                  data.sort {
                    if (it[fieldKeys[1]] instanceof String) {
                      return Double.parseDouble(it[fieldKeys[1]])
                    }
                    return it[fieldKeys[1]]
                  }
                  data.each {
                    if (sampleToPids.containsKey(it[theReport.reportColumn])) {
                      sortedSampleIds.push(sampleToPids.get(it[theReport.reportColumn]))
                    }
//                    if (sampleToPids.containsKey(it.participant_id)) {
//                      sortedSampleIds.addAll(sampleToPids.get(it.participant_id).keySet())
//                    }
                  }
                }
              }
              else
              {
                sorter.put(fieldKeys[1], 1)
                sortedSampleIds = []
//                List data = mongoDataService.find(collection, [ participant_id:[ '$in':pids ] ], [ "participant_id" ], sorter, -1)
                List data = mongoDataService.find(collection, theQuery, [ theReport.reportColumn ], sorter, -1)
                if (data) {
                  data.each {
                    if (sampleToPids.containsKey(it[theReport.reportColumn])) {
                      sortedSampleIds.push(sampleToPids.get(it[theReport.reportColumn]))
                    }
//                    if (sampleToPids.containsKey(it.participant_id)) {
//                      sortedSampleIds.addAll(sampleToPids.get(it.participant_id).keySet())
//                    }
                  }
                }
              }
            }
          }
          }
        } else {
          if (varType == "numerical") {
            String valuesField = fieldKeys[1].split("\\.")[1]
            List nResults = mongoDataService.find(collection, [ sampleSetId:sampleSet.id ], [ "sampleId", fieldKeys[1] ], null, -1)
            if (nResults) {
              nResults.sort { Map s ->
                def sValue = s.values[valuesField]
                if (sValue instanceof String) {
                  if (sValue.isNumber()) {
                    return Double.parseDouble(sValue)
                  } else {
                    return null
                  }
                }
                return sValue
              }
              sortedSampleIds = nResults.collect { (long) it.sampleId }
            }
          } else {
            sorter.put(fieldKeys[1], 1)
            sortedSampleIds = mongoDataService.find(collection, [ sampleSetId:sampleSet.id ], [ "sampleId" ], sorter, -1)?.collect {
              (long) it.sampleId
            }
          }
        }
      }

	  def sampleSetId = sampleSet.parentSampleSet?.id ?: sampleSet.id

	  def sampleIdToBarcode = [:]
	  mongoDataService.find("sample", [ sampleSetId:sampleSetId ], [ "sampleId", "sampleBarcode" ], null, -1).each {
		sampleIdToBarcode.put((long)it.sampleId, it.sampleBarcode)
	  }
	  def hitInfinity = false // track floating point exceptions.
	  
	  Map tables = getProbeTables(sampleSet)
	  
	  try {
		  if (session.getAttribute("logTiming")) {
			  groupSets = System.currentTimeMillis()
			  println "groupSetStart: " + (groupSets - start) + "ms"
		  }
	  } catch (IllegalStateException ise) {
	  }

      groupSet.groups.sort { it.displayOrder }.each {
        if (!it.groupDetails.isEmpty())
        {
          Map groupData = getGroupProbeData(sql, sampleSet, it.id, probeId, signalDataTable, sampleIdToBarcode, tables, floor)
          if (groupData) {
			hitInfinity = groupData.hitInfinity || hitInfinity
            max = Math.max(max, groupData.data.max)
            if (sortedSampleIds) {
              groupData.points.sort {
                int idx = sortedSampleIds.indexOf((long)it.data.id)
                if (idx < 0) {
                  return sortedSampleIds.size()
                }
                return idx
              }.eachWithIndex { Map p, int i ->
                p.x = i
              }
            }

			if (rankList != null) {
				groupData.rankListDescription = rankList.description
			}
			if (rankListType != null) {
				groupData.rankListAbbrev = rankListType.abbrev
				groupData.rankListType = rankListType.description
			}
			groupData.up = groupData.down = false;
			if (it.name.equals(first)) {
			  groupData.up = true;
			} else if (it.name.equals(second)) {
				groupData.down = true;
			}
			// println "name: " + it.name + " groupData.up: " + groupData.up + " groupData.down: " + groupData.down
            groups.push(groupData)
          }
        }
      }
	  
	  try {
		  if (session.getAttribute("logTiming")) {
			  probeData = System.currentTimeMillis()
			  println "probeData: " + (probeData - groupSets) + "ms"
		  }
	  } catch (IllegalStateException ise) {
	  }

      int n = (int)(max / chartStep)
			max = (n + 1) * chartStep
      return [hitInfinity: hitInfinity, groups:groups, max:max]
    }
  }

  def Map getGroupProbeData(Sql sql, def sampleSet, long groupId, String probeId, String signalDataTable, Map sampleIdToBarcode, Map tables, double floor)
  {
	def hitInfinity = false
    def group = DatasetGroup.get(groupId)
    if (group && probeId != "")
    {
      //def sampleSet = group.groupSet.sampleSet
      def rawSignalType = sampleSet.rawSignalType

      if (rawSignalType.id == 2 || rawSignalType.id == 7) {
        floor = Math.log(floor) / Math.log(2)
      }

//      if (tables.isFocused) {
//        floor = 0
//      }
      def probeIdCol = signalDataTable == "array_data_detail_tmm_normalized" ? "probe_id" : "affy_id"

	  def sampleSetId = sampleSet.parentSampleSet?.id ?: sampleSet.id
	  
      def sampleSetCheck = ""
      if (signalDataTable != "array_data_detail" && !tables.isFocused) {
        sampleSetCheck = "AND s.sample_set_id = ${sampleSetId}"
      }
	  def top, bottom // timing
	  
      def query = """SELECT s.array_data_id 'sampleId', s.${tables.signalColumn} 'probeSignal' FROM dataset_group_detail g
        JOIN ${signalDataTable} s ON g.sample_id = s.array_data_id
        WHERE g.group_id = ${groupId}
        AND s.${probeIdCol} = '${probeId}'
        ${sampleSetCheck}""".toString()
//        ORDER BY g.sample_id""".toString()

	  try {
		if (session.getAttribute("logTiming")) {
		  println "groupQuery: " + query
		  top = System.currentTimeMillis()
		}
	  } catch (IllegalStateException ise) {
	  }


      List<Double> qValues = new ArrayList<Double>()
      def points = []
      int x = 0
      sql.eachRow(query) {
        def signal = tables.isFocused || tables.isRnaSeq || it.probeSignal > floor ? it.probeSignal : floor
        points.push([x:x, y:signal, data:[id:it.sampleId, barcode:sampleIdToBarcode.get((long)it.sampleId)]])
        qValues.push(signal)
        x++
      }
      qValues.sort()

      if (qValues.isEmpty())
        return null

      // convert to signals to default display type
      def defaultSignalType = sampleSet.defaultSignalDisplayType
      if (rawSignalType.id == 2 || rawSignalType.id == 7)
      {
        if (defaultSignalType == null || defaultSignalType.id == 1 || defaultSignalType.id == 6)
        {
          points.each { Map m ->
            m.y = Math.pow(2, m.y)
			if (m.y == Double.POSITIVE_INFINITY) {
				hitInfinity = true;
			}
  
          }
          qValues = qValues.collect {
            return Math.pow(2, it)
          }
        }
      }
      else if (rawSignalType?.id == 1 && defaultSignalType?.id == 2)
      {
        points.each { Map m ->
          m.y = Math.log(m.y) / Math.log(2)
		  if (m.y == Double.POSITIVE_INFINITY) {
			  hitInfinity = true;
		  }
        }
        qValues = qValues.collect {
          return Math.log(it) / Math.log(2)
        }
      }
	  
	  try {
		  if (session.getAttribute("logTiming")) {
			  bottom = System.currentTimeMillis()
			  println "loop " + x  + " probes: " + (bottom - top) + "ms"
		  }
	  } catch (IllegalStateException ise) {
	  }

      // then do quartile calculations
      double firstQuantile = StatsService.quantile(qValues, 0.25, true)
      double secondQuantile = StatsService.quantile(qValues, 0.5, true)
      double thirdQuantile = StatsService.quantile(qValues, 0.75, true)
      double max = qValues.max()
      double min = qValues.min()

      def color = group.hexColor ?: grailsApplication.config.groups.defaultColor
      return [hitInfinity: hitInfinity, label:group.name, color:color, points:points, data:[id:groupId, groupSet:group.groupSet.id, max:max, min:min, quartiles:[first:firstQuantile, second:secondQuantile, third:thirdQuantile]]]
    }
  }

  Map getProbeTables(long groupSetId)
  {
    SampleSet sampleSet = DatasetGroupSet.findById(groupSetId)?.sampleSet
    if (sampleSet) {
      return getProbeTables(sampleSet)
    }
    return null
  }

  Map getProbeTables(SampleSet sampleSet)
  {
	ChipsLoaded chipLoaded = ChipsLoaded.findBySampleSet(sampleSet)
	if (chipLoaded == null && sampleSet.parentSampleSet) {
		chipLoaded = ChipsLoaded.findBySampleSet(sampleSet.parentSampleSet)
	}
    ChipType chip = chipLoaded?.chipType
	ArrayData firstArray = ArrayData.findByChip(chipLoaded)

    boolean isFocused = false, isRnaSeq = false
    def signalDataTable = "array_data_detail"
	def normalizedDataTable = "array_data_detail_quantile_normalized"
	def probeIdColumn = "affy_id"
    def signalColumn = "`signal`"
    if (sampleSet.rawSignalType?.id == 6 || sampleSet.rawSignalType?.id == 7) {
      normalizedDataTable = "array_data_detail"
	// FIXME: generate a constant at bootstrap to check against.
    } else if (chip.technology.name == "Focused Array") {
      signalDataTable = "focused_array_sample_assay_data"
      normalizedDataTable = "focused_array_fold_changes"
      probeIdColumn = "target"
      signalColumn = "fc"
      isFocused = true
	// FIXME: generate a constant at bootstrap to check against.
    } else if (chip.technology.name == "RNA-Seq") {
	  // if we're looking at raw data, use the quantile normalized table, else use tmm normalized table.
	  if (sampleSet.defaultSignalDisplayType?.id != 1) {
		normalizedDataTable = "array_data_detail_tmm_normalized"
	  }
	  isRnaSeq = true
    }

    return [ chip:chip, signalDataTable:signalDataTable, probeIdColumn:probeIdColumn, signalColumn:signalColumn,
        	  normalizedDataTable:normalizedDataTable, isFocused:isFocused, isRnaSeq:isRnaSeq,, chipTypeId:chip.id, firstArrayId: firstArray.id]
  }

  def overlayOptions(long sampleSetId)
  {
    def categoricalOverlays = [], numericalOverlays = []

    List<String> reports = []
    LabkeyReport.findAllBySampleSetIdAndEnabledNotEqual(sampleSetId,false)?.each {
      if (it.sampleSetColumn && it.reportColumn) {
        reports.push(it.category.trim())
      }
    }

    List<String> simpleLabkeys = ["labkeySubject","labkeyClinical","labkeyAncillaryData"]
    simpleLabkeys.retainAll(reports)
    List<String> resultsLabkeys = [ "labkeyLabResults", "labkeyFlow" ]
    resultsLabkeys.retainAll(reports)

    simpleLabkeys.each { collection ->
      def result = mongoDataService.findOne("sampleSet", [sampleSetId:sampleSetId], ["${collection}.header".toString()])
      if (result?.get(collection)?.header) {
        def header = result.get(collection).header
        header.remove("participant_id")
        header.each { k, v ->
          if (v.overlay_visible == "show") {
            v.key = k
            v.collection = collection
            if (v.datatype == "string" && v.numUnique <= 20) {
              categoricalOverlays.push(v)
            } else if (v.datatype == "number") {
              numericalOverlays.push(v)
            }
          }
        }
      }
    }
    // labkey lab results
    resultsLabkeys.each { String collection ->
      String testData = "${collection}.tests".toString()
      Map cQuery = [ sampleSetId:sampleSetId ]
      cQuery.put(testData, [ '$exists':true ])
      def labData = mongoDataService.findOne("sampleSet", cQuery, [ testData ])
      if (labData) {
        labData.get(collection).tests.each { String key, Map testInfo ->
          if (testInfo.overlay_visible == "show") {
            String displayName = testInfo.displayName
            numericalOverlays.push([ displayName:displayName, key:key, collection:collection,
                datatype:testInfo.datatype, order:testInfo.order ])
          }
        }
      }
    }

    // query sample info
    def spreadsheetData = mongoDataService.findOne("sampleSet", [ sampleSetId:sampleSetId ], [ "spreadsheet" ])
    if (spreadsheetData?.spreadsheet?.header) {
      spreadsheetData.spreadsheet.header.each { String k, v ->
        if (v.overlay_visible == "show") {
          v.key = "values.${k}".toString()
          v.collection = "sample"
          if (v.datatype == "string" && v.numUnique <= 20) {
            categoricalOverlays.push(v)
          } else if (v.datatype == "number") {
            numericalOverlays.push(v)
          }
        }
      }
    }

    categoricalOverlays.sort { it.displayName }
    numericalOverlays.sort { it.displayName }

    return [categorical:categoricalOverlays, numerical:numericalOverlays]
  }

}
