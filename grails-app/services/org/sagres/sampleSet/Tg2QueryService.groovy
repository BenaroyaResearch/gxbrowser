package org.sagres.sampleSet

import groovy.sql.Sql
import groovy.sql.GroovyRowResult
import grails.converters.JSON
import common.ArrayData

class Tg2QueryService {

  def dataSource

  /**
   * update the external sample ID column in array data for all samples in the sample set
   * @param sampleSetID
   */
  def updateTg2IDs(long sampleSetID)
  {
    def resultsMap = [:]
    GString sampleSetQuery = """select d.id, d.barcode from dataset_group_set a, dataset_group b, dataset_group_detail c, array_data d
							where sample_set_id = ${sampleSetID} and a.id = b.group_set_id and b.id = c.group_id and c.sample_id = d.id"""

    String szChipList = "";
    def sampleMap = [:]
    def scratchMap = [:]
    def sql = new Sql(dataSource)
    sql.eachRow(sampleSetQuery.toString()) { row ->
      String [] tokens = row.barcode.split("_")
      if (scratchMap[tokens[0]] == null)
      {
        scratchMap.put(tokens[0], 1)
        szChipList += szChipList.length() > 0 ? ",${tokens[0]}" : "${tokens[0]}"
      }
      sampleMap.put(row.barcode, row.id)
    }

    resultsMap.put("noSamples", sampleMap.keySet().size())

    if (szChipList != "")
    {

      // now get the sample IDs from TG2 and update array_data
      String tg2Query = "select a.chip_barcode, b.position, b.sample_id from ben_tg2.microarray_chip a, ben_tg2.microarray_chip_position b " +
        "where a.chip_barcode in (${szChipList}) and a.id = b.microarray_chip_id"

      sql.eachRow(tg2Query) { row->
        def barcode = row.chip_barcode + "_" + (char)(row.position + 64)
        def arrayDataID = sampleMap.get(barcode)
        GString updateSql = """update array_data set externalDB = 'ben_tg2', externalid = '${row.sample_id}' where id = ${arrayDataID}"""
        sql.execute(updateSql.toString())
        sampleMap.remove(barcode)
      }
      resultsMap.put("noSamplesNotProcessed", sampleMap.keySet().size())
      resultsMap.put("badBarcodes", sampleMap.keySet())
    }

    return resultsMap
  }

  def getSampleDisplayInfo(sampleID)
  {
    GString tg2Query = """select b.sample_id, sample_name, collection_date, collection_method, culture_conditions, incubation_time, received_date,
			b.delivery_method, donor_id, study_group, c.definition 'cellPopulation', d.definition 'tissueType', b.gender, b.sample_content_sample_notes 'notes',
			s.definition 'species', outside_id, age, stimulus, physical_state, race, ethnicity, source_institution, isolation_method
			from ben_tg2.sample b
			left join ben_tg2.cell_population c on b.sample_content_cell_population_id = c.id
			left join ben_tg2.tissue_type d on b.sample_content_tissue_type_id = d.id
			left join ben_tg2.species s ON b.species_id = s.id
			where b.id = ${sampleID}"""

    def tg2Info = [:]
    def tg2Data = []
    def sql = new Sql(dataSource)
    sql.eachRow(tg2Query.toString()) { row ->
      tg2Info.put("sample ID", row.sample_id)
      tg2Info.put("sample Name", row.sample_name)
      def gender = null
      if (row.gender == "M")
        gender = "Male"
      else if (row.gender == "F")
        gender = "Female"
      tg2Data << [ "key": "Gender", "value": gender ]
      tg2Data << [ "key": "Collection Date", "value": row.collection_date ]
      tg2Data << [ "key": "Received Date", "value": row.received_date ]
      tg2Data << [ "key": "Collection Method", "value": row.collection_method ]
      tg2Data << [ "key": "Cell Population", "value": row.cellPopulation]
      tg2Data << [ "key": "Tissue Type", "value": row.tissueType]
      tg2Data << [ "key": "Incubation Time", "value": row.incubation_time ]
      tg2Data << [ "key": "Culture Conditions", "value": row.culture_conditions ]
      tg2Data << [ "key": "Donor ID", "value": row.donor_id]
      tg2Data << [ "key": "Study Group", "value": row.study_group]
      tg2Data << [ "key": "TG2 Notes", "value": row.notes]
      tg2Data << [ "key": "Species", "value": row.species]
      tg2Data << [ "key": "Outside ID", "value": row.outside_id]
      tg2Data << [ "key": "Age", "value": row.age]
      tg2Data << [ "key": "Stimulus", "value": row.stimulus]
      tg2Data << [ "key": "Physical State", "value": row.physical_state]
      tg2Data << [ "key": "Race", "value": row.race]
      tg2Data << [ "key": "Ethnicity", "value": row.ethnicity]
      tg2Data << [ "key": "Source Institution", "value": row.source_institution]
      tg2Data << [ "key": "Isolation Method", "value": row.isolation_method]
    }
    tg2Info.put("data", tg2Data)
    return tg2Info
  }

  def sampleInfo(long sampleId)
  {
    GString tg2Query = """select b.sample_id, sample_name, collection_date, received_date,
			c.definition 'cellPopulation', d.definition 'tissueType', b.gender, b.sample_content_sample_notes 'notes',
			s.definition 'species', age
			from ben_tg2.sample b
			left join ben_tg2.cell_population c on b.sample_content_cell_population_id = c.id
			left join ben_tg2.tissue_type d on b.sample_content_tissue_type_id = d.id
			left join ben_tg2.species s ON b.species_id = s.id
			where b.id = ${sampleId}"""

    def tg2Data = [:]
    def sql = new Sql(dataSource)
    sql.eachRow(tg2Query.toString()) { row ->
      tg2Data.put("Sample ID", row.sample_id)
      tg2Data.put("Sample Name", row.sample_name)
      def gender = "Unknown"
      if (row.gender == "M") {
        gender = "Male"
      } else if (row.gender == "F") {
        gender = "Female"
      }
      tg2Data.put("Gender", gender)
      tg2Data.put("Collection Date", row.collection_date)
      tg2Data.put("Received Date", row.received_date)
      tg2Data.put("Cell Population", row.cellPopulation)
      tg2Data.put("Tissue Type", row.tissueType)
      tg2Data.put("TG2 Notes", row.notes)
      tg2Data.put("Species", row.species)
      tg2Data.put("Age", row.age)
    }
    return tg2Data
  }

  def List tg2Fields()
  {
    Sql sql = Sql.newInstance(dataSource)
    def sampleId = sql.firstRow("SELECT id FROM ben_tg2.sample LIMIT 1").id
    sql.close()
    def data = getSampleDisplayInfo(sampleId).data
    def fields = ["sample_name"]
    data.each { fields.push(it.key.encodeAsKeyCase()) }
    return fields
  }


  def Map<Long,List> getTg2Ids(long sampleSetId)
  {
    def sampleNameToArrayDataIds = [:]
    def barcodeToArrayDataId = getBarcodes(sampleSetId)

    Sql sql = Sql.newInstance(dataSource)
    barcodeToArrayDataId.each { String key, value ->
			def splitPoint
			try {
      splitPoint = key.indexOf("_")
      def position = (int)key.charAt(splitPoint+1) - 64
      def barcode = splitPoint > 0 ? key.substring(0,splitPoint) : key
      def tg2Query = "SELECT b.id, c.sample_name " +
        "FROM ben_tg2.microarray_chip a, ben_tg2.microarray_chip_position b, ben_tg2.sample c " +
        "WHERE a.chip_barcode = '${barcode}' " +
        "AND b.position = ${position} " +
        "AND a.id = b.microarray_chip_id " +
        "AND b.sample_id = c.id"
      sql.rows(tg2Query).each { GroovyRowResult r ->
        def sampleName = ((String)r.sample_name).trim()
        if (sampleNameToArrayDataIds.containsKey(sampleName))
        {
          ((List)sampleNameToArrayDataIds.get(sampleName)).add(["sampleId":value, "chipId":r.id]);
        }
        else
        {
          sampleNameToArrayDataIds.put(sampleName, [["sampleId":value, "chipId":r.id]])
        }
      }
			} catch (Exception ex) {
				println "Exception getting TG2 ID's - sampleSetId ${sampleSetId} $key:$value:$splitPoint"
				println "${ex.toString()}"
				throw ex
			}
    }
    sql.close()

    return sampleNameToArrayDataIds
  }

  def Map<String,String> getTg2SampleIds(Collection<String> sampleNames) {
    def sampleNameToTg2SampleId = [:]
    Sql sql = Sql.newInstance(dataSource)
    sampleNames.each {
      def sampleName = it.trim()
      def tg2Query = "SELECT a.sample_id " +
        "FROM ben_tg2.sample a " +
        "WHERE UPPER(TRIM(sample_name)) = UPPER('${sampleName}')"
      def result = (GroovyRowResult)sql.firstRow(tg2Query)
      if (result)
      {
        sampleNameToTg2SampleId.put(sampleName, result.sample_id)
      }
    }
    sql.close()

    return sampleNameToTg2SampleId
  }

  def List getTg2QualityData(long sampleSetId) {
    def arrayDataIdToTg2QualityData = []
    def barcodeToArrayDataId = getBarcodes(sampleSetId)

    Sql sql = Sql.newInstance(dataSource)
    barcodeToArrayDataId.each { String key, value ->
      arrayDataIdToTg2QualityData.push(getTg2QualityDataForSample(sql, key))
    }
    sql.close()

    return arrayDataIdToTg2QualityData
  }

  def Map getTg2QualityDataForSample(Sql sql, String barcodeKey) {
    def splitPoint = barcodeKey.indexOf("_")
    def position = null, barcode = null
    if (splitPoint > 0)
    {
      position = (int)barcodeKey.charAt(splitPoint+1) - 64
      barcode = barcodeKey.substring(0,splitPoint)
    }
    else
    {
      position = -1
      barcode = ""
    }
    def values = [:]
    def tg2Query = "SELECT chip_id, background, biotin, a.date_created, experiment_start_date, genes001, genes005, gp95, housekeeping, noise, c.sample_id, c.sample_name " +
      "FROM ben_tg2.microarray_chip a, ben_tg2.microarray_chip_position b, ben_tg2.sample c " +
      "WHERE a.chip_barcode = '${barcode}' " +
      "AND b.position = ${position} " +
      "AND a.id = b.microarray_chip_id " +
      "AND b.sample_id = c.id"
    GroovyRowResult r = sql.firstRow(tg2Query)
    if (r) {
      values = r.subMap(["sample_id", "sample_name", "date_created", "experiment_start_date", "background", "biotin", "genes001", "genes005", "gp95", "housekeeping", "noise"])
      values.put("barcode", "${barcodeKey}")
    }
    return values
  }

  def Map<Long,Map> getTg2SampleData(long sampleSetId) {
    def arrayDataIdToTg2SamplesData = [:]
    def barcodeToArrayDataId = getBarcodes(sampleSetId)

    Sql sql = Sql.newInstance(dataSource)
    barcodeToArrayDataId.each { String key, value ->
      arrayDataIdToTg2SamplesData.put(value,getTg2SampleData(sql,key))
    }
    sql.close()

    return arrayDataIdToTg2SamplesData
  }

  def Map getTg2SampleData(Sql sql, String barcodeKey)
  {
    def splitPoint = barcodeKey.indexOf("_")
    def position
    def barcode
    if (splitPoint > 0)
    {
      position = (int)barcodeKey.charAt(splitPoint+1) - 64
      barcode = barcodeKey.substring(0,splitPoint)
    }
    else
    {
      position = -1
      barcode = ''
    }
    def tg2Query = "SELECT b.id, chip_id, c.sample_id, c.sample_name, c.received_date, c.collection_date, c.collection_method, c.delivery_method, c.study_group, " +
      "c.donor_id, s.definition 'species', c.age, c.outside_id, c.stimulus, c.physical_state, c.gender, c.race, c.ethnicity, c.culture_conditions, " +
      "c.incubation_time, c.source_institution, c.isolation_method, d.definition 'cell_population', e.definition 'tissue_type', c.sample_content_sample_notes 'tg2_notes' " +
      "FROM ben_tg2.microarray_chip a " +
      "JOIN ben_tg2.microarray_chip_position b ON a.id = b.microarray_chip_id " +
      "JOIN ben_tg2.sample c ON b.sample_id = c.id " +
      "LEFT JOIN ben_tg2.species s ON c.species_id = s.id " +
      "LEFT JOIN ben_tg2.cell_population d ON c.sample_content_cell_population_id = d.id " +
      "LEFT JOIN ben_tg2.tissue_type e ON c.sample_content_tissue_type_id = e.id " +
      "WHERE a.chip_barcode = '${barcode}' AND b.position=${position}"
    def values = [:]
    GroovyRowResult r = sql.firstRow(tg2Query)
    if (r)
    {
      values = r.subMap(["sample_id", "sample_name", "received_date", "collection_date", "collection_method", "delivery_method", "study_group",
        "donor_id", "species", "age", "outside_id", "stimulus", "physical_state", "gender", "race", "ethnicity", "culture_conditions",
        "incubation_time", "source_institution", "isolation_method", "cell_population", "tissue_type", "tg2_notes"])
      values.put("barcode", "${barcodeKey}")
    }
    return values
  }

  private Map<String,Long> getBarcodes(long sampleSetId) {
    def barcodeToArrayDataId = [:]

    Sql sql = Sql.newInstance(dataSource)
    def query = "SELECT a.id, a.barcode FROM dataset_group_set s, dataset_group g, dataset_group_detail d, array_data a " +
      "WHERE s.sample_set_id = ${sampleSetId} " +
      "AND g.group_set_id = s.id " +
      "AND d.group_id = g.id " +
      "AND a.id = d.sample_id " +
      "GROUP BY a.id"
    sql.rows(query).each { GroovyRowResult r ->
      barcodeToArrayDataId.put(r.get("barcode"), r.get("id"))
    }
    sql.close()

    return barcodeToArrayDataId
  }

  def Map<String,String> getSampleSetInfo(long sampleSetId)
  {
    def sampleSetInfo = [:]
    def sampleSet = SampleSet.get(sampleSetId)
    if (sampleSet.defaultGroupSet)
    {
      def groupDetails = sampleSet.defaultGroupSet.groups.groupDetails.flatten()
      Sql sql = Sql.newInstance(dataSource)
      try
      {
        groupDetails.find { DatasetGroupDetail d ->
          if (d.sample.externalDB == "ben_tg2")
          {
            def tg2Query = "SELECT project_title, experiment_start_date " +
              "FROM ben_tg2.microarray_chip a " +
              "JOIN ben_tg2.microarray_chip_position b ON a.id = b.microarray_chip_id " +
              "LEFT JOIN ben_tg2.project p ON a.project_id = p.id " +
              "WHERE b.id = ${d.sample.externalID}"
            GroovyRowResult r = sql.firstRow(tg2Query)
            if (r)
            {
              if (r.project_title) { sampleSetInfo.put("project", r.project_title) }
              if (r.experiment_start_date) { sampleSetInfo.put("run_date", r.experiment_start_date) }
              return (sampleSetInfo.keySet() == 2)
            }
          }
          return false
        }
      }
      catch (Exception e)
      {
        println e.getMessage()
        println e.getStackTrace()
      }

      def microArrayIds = []
      groupDetails.each { DatasetGroupDetail d ->
        if (d.sample.externalDB == "ben_tg2")
        {
          microArrayIds.push(d.sample.externalID)
        }
      }
      if (microArrayIds.size() > 0)
      {
        try
        {
          def cellPopulation = []
          def tg2Query = "SELECT d.definition " +
            "FROM ben_tg2.microarray_chip_position a " +
            "LEFT JOIN ben_tg2.sample c ON a.sample_id = c.id " +
            "LEFT JOIN ben_tg2.cell_population d ON c.sample_content_cell_population_id = d.id " +
            "WHERE a.id IN (${microArrayIds.join(",")})"
//			    println tg2Query
          sql.rows(tg2Query).each { GroovyRowResult r ->
            if (r)
            {
              if (r.definition) {
                cellPopulation.push(r.definition)
              }
            }
          }
          if (!cellPopulation.isEmpty())
          {
            sampleSetInfo.put("cell_population", cellPopulation.unique().join(","))
          }
        }
        catch (Exception e)
        {
          println e.getMessage()
          println e.getStackTrace()
        }
      }
      sql.close()
    }
    return sampleSetInfo
  }

  def Map getHistogramData(long sampleSetId, String[] dataKeys, String color)
  {
    def output
    if (dataKeys)
    {
      def barcodeToArrayDataId = getBarcodes(sampleSetId)

      def dataKeyToColor = ["#31114c","#83285b","#4d5b45","#c0b928","#7fdad8","#186977","#8d530f"]
      def points = [:]
      dataKeys.sort().each {
        points.put(it, [])
      }

      def maxPoint = -1.0
      Sql sql = Sql.newInstance(dataSource)
      barcodeToArrayDataId.each { String key, value ->
        def barcodeParts = []
        barcodeParts = key.tokenize("_")
        if (barcodeParts.size() > 1)
        {
          try
          {
            def position = ((int)barcodeParts[1][0]) - 64
            def tg2Query = "SELECT c.sample_id, " + dataKeys.join(",") +
              " FROM ben_tg2.microarray_chip a, ben_tg2.microarray_chip_position b, ben_tg2.sample c " +
              "WHERE a.chip_barcode = '${barcodeParts[0]}' " +
              "AND b.position = ${position} " +
              "AND a.id = b.microarray_chip_id " +
              "AND b.sample_id = c.id"
            def r = (GroovyRowResult)sql.firstRow(tg2Query)
            if (r)
            {
              dataKeys.eachWithIndex { k, i ->
                def val = r.get(k)
                if (val)
                {
                  points.get(k).push([name: r.get("sample_id"), y: val]);
                }
                maxPoint = Math.max(maxPoint, val)
              }
            }
          }
          catch (Exception e)
          {
            println e.getMessage()
          }
        }
      }
      sql.close()

      if (points.size() > 0)
      {
        def allPoints = [], colorIdx = 0, numPoints = 0
        points.each { key, data ->
          if (data.size() > 0)
          {
            allPoints.push([name: key, data: data, color: dataKeyToColor[colorIdx]])
//            maxPoint = Math.max(maxPoint, data.max())
            numPoints = Math.max(numPoints, data.size())
            colorIdx++
          }
        }
        output = [data: allPoints, max: maxPoint, numPoints: numPoints, numGroups: points.keySet().size(), numKeys: dataKeys.length]
      }
    }

    return output
  }

  def projectListAsJson =
  {
    def szProjectSql = """select id, project_title 'title', date_created from ben_tg2.project"""
    def projectQuery = new Sql(dataSource)
    def sampleCountQuery = new Sql(dataSource)
    def projectList = []
    projectQuery.eachRow(szProjectSql.toString()) { projectRow ->
      def szSampleCountSql = """select c.project_id 'projectID', b.microarray_chip_id 'chipID', count(distinct a.id) 'noSamples'
				from ben_tg2.sample a
				left join ben_tg2.microarray_chip_position b on b.sample_id = a.id
				left join ben_tg2.microarray_chip c on c.id = b.microarray_chip_id
				where a.project_id = ${projectRow.id}
				group by c.project_id, b.microarray_chip_id"""

      def nSamples = 0
      def nExtraChips = 0
      def nProjectChips = 0
      sampleCountQuery.eachRow(szSampleCountSql.toString()) { sampleCountRow ->
        if (sampleCountRow.projectID != null)
        {
          if (sampleCountRow.projectID == projectRow.id)
            nProjectChips++
          else
            nExtraChips++
        }
        nSamples += sampleCountRow.noSamples
      }
      projectList << [ projectRow.id, projectRow.title, projectRow.date_created, nSamples, nProjectChips, nExtraChips, -1 ]
    }
    return projectList
  }
  
  def tg2ProjectSamplesList =
  {
	def szProjectSql = """select id, project_title AS 'title', project_background AS 'hypothesis', project_methods AS 'design', project_goals AS 'purpose', date_created from ben_tg2.project"""
    def projectQuery = new Sql(dataSource)
    def sampleCountQuery = new Sql(dataSource)
    def projectList = []
    projectQuery.eachRow(szProjectSql.toString()) { projectRow ->
      def szSampleCountSql = """select c.project_id 'projectID', b.microarray_chip_id 'chipID', count(distinct a.id) 'noSamples'
				from ben_tg2.sample a
				left join ben_tg2.microarray_chip_position b on b.sample_id = a.id
				left join ben_tg2.microarray_chip c on c.id = b.microarray_chip_id
				where a.project_id = ${projectRow.id}
				group by c.project_id, b.microarray_chip_id"""

      def nSamples = 0
      def nExtraChips = 0
      def nProjectChips = 0
      sampleCountQuery.eachRow(szSampleCountSql.toString()) { sampleCountRow ->
        if (sampleCountRow.projectID != null)
        {
          if (sampleCountRow.projectID == projectRow.id)
            nProjectChips++
          else
            nExtraChips++
        }
        nSamples += sampleCountRow.noSamples
      }
      projectList.push([ id: projectRow.id, title: projectRow.title, hypothesis: projectRow.hypothesis, design: projectRow.design, purpose: projectRow.purpose, date: projectRow.date_created, nSamples: nSamples, nChips: nProjectChips])
    }
    return projectList
  }
  
  def tg2ProjectList =
  {
	def szProjectSql = """select id, project_title AS 'title', project_background AS 'hypothesis', project_methods AS 'design', project_goals AS 'purpose', date_created from ben_tg2.project"""
    def projectQuery = new Sql(dataSource)

    def projectList = []
    projectQuery.eachRow(szProjectSql.toString()) { projectRow ->
      projectList.push([ id: projectRow.id, title: projectRow.title, hypothesis: projectRow.hypothesis, design: projectRow.design, purpose: projectRow.purpose, date: projectRow.date_created])
    }
    return projectList
  }

}
