package org.sagres.common

import groovy.sql.Sql
import org.sagres.sampleSet.SampleSet
import org.sagres.mat.Analysis
import org.sagres.mat.ModuleGeneration
import org.sagres.mat.Version
import org.sagres.rankList.RankList
import org.springframework.web.context.request.RequestContextHolder
import common.chipInfo.ChipType
import common.ArrayData
import org.sagres.mat.Module

class GeneQueryService
{

  static transactional = true
  def dataSource
  def sampleSetService
  def chartingDataService
  def grailsApplication
  def session = RequestContextHolder.currentRequestAttributes().getSession()

  def getDefaultQuery(sql, params)
  {
	  def start = System.currentTimeMillis()
	  def setup
	  def sampleSetID = params.long("sampleSetID")
	  
//	def chipTypeID = params.long("chipTypeID")
//
//    String szSql = "SELECT DISTINCT f.symbolColumn, f.symbol_column, f.probe_list_table, f.entrez_gene_column, f.id as 'chip_type_id' FROM chip_type f"
//	String chipsetWhere = ""
//	// Get the columns from the chipType domain object for this sample set (or the default one).
//	if (chipTypeID != null && chipTypeID > 0)
//	{
//		chipsetWhere = " WHERE id = ${chipTypeID}"
//	}
//	else if (sampleSetID != null && sampleSetID > 0)
//    {
////      szSql = """SELECT DISTINCT f.probe_list_column, f.symbol_column, f.probe_list_table, f.entrez_gene_column, d.id 'arrayDataId'
////		FROM dataset_group_set a, dataset_group b, dataset_group_detail c, array_data d, chips_loaded e, chip_type f
////		WHERE a.sample_set_id = ${sampleSetID} AND a.id = b.group_set_id AND b.id = c.group_id
////		and c.sample_id = d.id AND d.chip_id = e.id AND e.chip_type_id = f.id limit 1"""
//		chipsetWhere = ", sample_set a WHERE a.id = ${sampleSetID} AND a.chip_type_id = f.id"
//    }
//    else
//    {
//		def defaultChipType = grailsApplication.config.sampleSets.defaultChipType
//		chipsetWhere = " WHERE id = ${defaultChipType}"
//    }
//	szSql = szSql + chipsetWhere 
	
    def szGeneQuery = null
    try
    {
	  SampleSet ss = SampleSet.findById(sampleSetID)
	  Map tables = chartingDataService.getProbeTables(ss)
  
      //def row = sql.firstRow(szSql)
      String probeListTable = tables.chip.probeListTable
      String probeListColumn = tables.chip.probeListColumn
      String symbolColumn = tables.chip.symbolColumn
      String geneIdColumn = tables.chip.entrezGeneColumn
	  //String chip_type_id = tables.chip.id
	  //String arrayDataId = row.arrayDataId  // Only used if asking for module based set, and did not have rank list.
	  long arrayDataId = tables.firstArrayId
      if (probeListTable == "probe_xref" && probeListColumn.startsWith("illumina_v2")) {
        probeListTable = "illumina_human6_v2"
        probeListColumn = "probe_id"
        symbolColumn = "symbol"
        geneIdColumn = "entrez_gene_id"
      }

      String geneId = geneIdColumn ? "${geneIdColumn} 'alt_geneid'" : "${symbolColumn} 'alt_geneid'"

      // SampleSet ss = SampleSet.findById(sampleSetID)
	  // Map tables = chartingDataService.getProbeTables(ss)
      String focusedParamId = tables.isFocused ? "AND s.focused_array_fold_change_params_id = ${ss.defaultFoldChangeParams.id}" : ""
      //String chipTypeQuery = tables.isFocused ? "AND a.chip_type_id =  ${tables.chip.id} " : ""
//      String affyId = "affy_id"
//      if (params.signalDataTable == "array_data_detail_tmm_normalized") {
//        affyId = "probe_id"
//      } else if (params.signalDataTable == "focused_array_fold_changes") {
//        affyId = "target"
//      }

      def rankListId = -1
      if (params.rankList && params.rankList != "null" && params.rankList != "" && params.rankList != "-1")
      {
        rankListId = params.rankList.toInteger()
      }

      def geneListId = params.long("geneListId") ?: -1
      boolean hasGeneList = geneListId != -1
      String geneListJoin = hasGeneList ? "JOIN gene_list_detail g ON g.gene_id = gi.geneid" : ""
      String geneListWhere = hasGeneList ? "g.gene_list_id = ${geneListId} AND " : ""

	  try {
		  if (session?.getAttribute("logTiming")) {
			  setup = System.currentTimeMillis()
			  println "tableSetup: " + (setup - start) + "ms"
		  }
	  } catch (IllegalStateException ise) {
		  // println "session threw exception " + ise.toString()
	  }
  
	  //
	  // If coming in from analysis/show with module name, and module versionName - we're looking for only the genes/probes in a module.
	  //
      if (params.module)
      {
        // this is for the GXB link from MAT
        def hasRankList = rankListId != -1
		def rankValue = hasRankList ? "a.value" : "-1"
		def pValue    = hasRankList ? "a.fdr_p_value" : "-1"
        def rankJoin = hasRankList ? """JOIN rank_list_detail a ON a.probe_id = md.probe_id
          JOIN ${probeListTable} b ON a.probe_id = b.${probeListColumn}""" : ""
        def arrayDataDetailJoin = hasRankList ? "" : "JOIN ${tables.signalDataTable} s ON s.${tables.probeIdColumn} = md.probe_id"
		def arrayDataDetailWhere = hasRankList ? "" : "s.array_data_id = ${arrayDataId} ${focusedParamId} AND "
        def versionName = params.modVersionName
        if (params.analysis) {
          versionName = Version.get(Analysis.get(params.analysis)?.moduleVersion).versionName
        }
        def modGen = ModuleGeneration.findByVersionName(versionName).id


        szGeneQuery = """SELECT gi.symbol 'symbol', gi.geneid 'geneid', gi.geneid 'alt_geneid', md.probe_id 'probe', gi.name 'name', ${rankValue} 'val', ${pValue} 'pvalue'
          FROM module m
          JOIN module_detail md ON m.id = md.module_id
          ${arrayDataDetailJoin}
          JOIN gene_info gi ON md.gene_symbol = gi.symbol
          ${rankJoin}
          ${geneListJoin}
          WHERE ${arrayDataDetailWhere}
          module_name = '${params.module}'
          AND module_generation_id = '${modGen}'"""
        if (params.queryString)
        {
          szGeneQuery += """ AND gi.symbol LIKE '${params.queryString}%'"""
        }
//        else
//        {
//          szGeneQuery += """ AND gi.symbol IS NOT NULL"""
//        }
        szGeneQuery += hasGeneList ? " AND g.gene_list_id = ${geneListId}" : ""
        szGeneQuery += hasRankList ? " AND a.rank_list_id = ${rankListId} ORDER BY a.rank ASC" : ""
      }
	  //
	  // Default for rank list is selected
	  //
      else if (rankListId != -1)
      {
        szGeneQuery = """SELECT a.probe_id 'probe', a.value 'val', a.fdr_p_value 'pvalue', s.${symbolColumn} 'symbol', gi.name 'name', gi.geneid 'geneid', s.${geneId}
			FROM rank_list_detail a
			JOIN ${probeListTable} s ON a.probe_id = s.${probeListColumn}
			JOIN gene_info gi ON s.${symbolColumn} = gi.symbol
			${geneListJoin}
			WHERE a.rank_list_id = ${rankListId} ${focusedParamId}"""
        if (hasGeneList) {
          szGeneQuery += """ AND g.gene_list_id = ${geneListId}"""
        }
        if (params.queryString)
        {
          szGeneQuery += """ AND s.${symbolColumn} LIKE '${params.queryString}%'"""
		  // AND s.${symbolColumn} != ''
		  
        }
        szGeneQuery += """ ORDER BY a.rank ASC"""
      }
	  //AND gi.symbol IS NOT NULL 
	  //
	  // no rank list selected but a query is entered
	  //
      else if (params.queryString)
      {
        szGeneQuery = """SELECT a.${probeListColumn} 'probe', a.${symbolColumn} 'symbol', gi.geneid 'geneid', gi.name 'name', -1 'val', -1 'pvalue', a.${geneId}
			FROM ${probeListTable} a
			JOIN ${tables.signalDataTable} s ON s.${tables.probeIdColumn} = a.${probeListColumn}
			JOIN gene_info gi ON a.${symbolColumn} = gi.symbol
			${geneListJoin}
			WHERE s.array_data_id = ${arrayDataId} ${focusedParamId} AND ${geneListWhere}
			a.${symbolColumn} LIKE '${params.queryString}%' 
			ORDER BY a.${symbolColumn}"""
      }
	  // WHERE s.array_data_id = ${arrayDataId} ${focusedParamId} AND ${geneListWhere}
	  //AND a.${symbolColumn} != ''
	  //
	  // Default for no rank list selected
	  //
      else
      {
        szGeneQuery = """SELECT a.${probeListColumn} 'probe', a.${symbolColumn} 'symbol', gi.geneid 'geneid', gi.name 'name', -1 'val', -1 'pvalue', a.${geneId}
			FROM ${probeListTable} a
			JOIN ${tables.signalDataTable} s ON s.${tables.probeIdColumn} = a.${probeListColumn}
			JOIN gene_info gi ON a.${symbolColumn} = gi.symbol
			${geneListJoin}
			WHERE s.array_data_id = ${arrayDataId} ${focusedParamId} """
		// possible gene list selected
		// WHERE s.array_data_id = ${arrayDataId} ${focusedParamId} """
		// AND a.${symbolColumn} != '' AND gi.symbol is NOT NULL
        if (hasGeneList) {
          szGeneQuery += """ AND g.gene_list_id = ${geneListId}"""
        }
        szGeneQuery += """ ORDER BY a.${symbolColumn}"""
      }

    }
    catch (Exception e)
    {
      println e.getMessage()
      println "failed on: getProbeTables"
    }

//    println szGeneQuery
    return szGeneQuery.toString()
  }

  def List runGeneQuery(params)
  {
    def retList = []

	def start = System.currentTimeMillis()
	def setup
	def end
    def sql = new Sql(dataSource)
    def szSql = getDefaultQuery(sql, params)

	def pageOffset = params.offset ? params.offset.toInteger() : 0
    def pageSize = params.limit ? params.limit.toInteger() : 250
	
	// Don't need to limit it if we're asking for the probes of a module - return them all.
	if (! params.module)
	{
		szSql += " LIMIT " + pageSize 
		if (pageOffset != 0)
		{
			szSql += " OFFSET " + pageOffset
		}
	}

	try {
		if (session?.getAttribute("logTiming")) {
			setup = System.currentTimeMillis()
			println "querySetup: " + (setup - start) + "ms"
			println szSql
		}
	} catch (IllegalStateException ise) {
		// println "session threw exception " + ise.toString()
	}

    def isFc = params.rankList ? RankList.findById(params.rankList)?.rankListType?.abbrev == "fc" : false
    def isDiff = params.rankList ? RankList.findById(params.rankList)?.rankListType?.abbrev == "diff" : false

    def anotherSql = new Sql(dataSource)
    anotherSql.eachRow(szSql)
    {
          def val = it.val
          def value = val != null ? ((Double)it.val).round(2) : null
          String upDown = "none"
          if (isFc) {
            upDown = value < 1 ? "down" : "up"
            value = value < 1 ? (1 / val).round(2) : value
          } else if (isDiff) {
            upDown = value < 0 ? "down" : "up"
            value = Math.abs(value)
          }
          //							def value = it.val ? it.val.round(new MathContext(2)) : -1
          retList << [ "geneid": it.geneid, "symbol" : it.symbol, "name" : it.name, "probe": it.probe, "val":value, "pvalue":it.pvalue, upDown:upDown, "alt_geneid":it.alt_geneid ]
    }

	try {
		if (session?.getAttribute("logTiming")) {
			end = System.currentTimeMillis()
			println "queryExecute: " + (end - setup) + "ms"
		}
	} catch (IllegalStateException ise) {
	}

    return retList
  }

  def queryGeneListHTML(params)
  {
    def geneList = runGeneQuery(params)

    def htmlReturn = ""
    if (geneList.size() > 0)
    {
      htmlReturn = "<div id='resultList'>"
      def bFirst = true
      geneList.each
          {
            if (it.name != null)
            {
              if (bFirst)
              {
                htmlReturn += "<div id='geneBox_${it.probe}' onclick=\"selectGene('${it.geneid}', '${it.symbol}', '${it.probe}'); return false;\" class='geneBox'>"
                bFirst = false
              }
              else
              {
                htmlReturn += "<div id='geneBox_${it.probe}' onclick=\"selectGene('${it.geneid}', '${it.symbol}', '${it.probe}'); return false;\" class='geneBox'>"
              }
              htmlReturn += "<span class='geneSymbol'>${it.symbol}</span><span class='probeLabel'> (${it.probe})</span><br/>"
              htmlReturn += "<div class='geneDetail'>${it.name}</div></div>"
            }
          }
      htmlReturn += "</div>"
    }
    return htmlReturn
  }

  def geneSummaryFields()
  {
    def url = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils/esummary.fcgi?db=gene&retMode=xml&id=7503";
    def xml = url.toURL().text
    def fullXml = new XmlParser().parseText(xml)
    def geneFields = fullXml.DocSum.Item.collect
        {
          it.attribute("Name")
        }
    return geneFields
  }

  def List geneSearch(Sql sql, params) {
    if (sql == null) {
      sql = Sql.newInstance(dataSource)
    }
    String term = params.term
    SampleSet ss = SampleSet.findById(params.sampleSetID)

    if (ss && term.size() > 1) {
	  Map tables = chartingDataService.getProbeTables(ss)
      String probeListTable = tables.chip.probeListTable
      String probeListColumn = tables.chip.probeListColumn
      String symbolColumn = tables.chip.symbolColumn
      if (probeListTable == "probe_xref" && probeListColumn.startsWith("illumina_v2")) {
        probeListTable = "illumina_human6_v2"
        probeListColumn = "probe_id"
        symbolColumn = "symbol"
      }
      //String firstArrayDataId = ArrayData.findByChip(ss.chipsLoaded).id
	  
//      String affyId = "affy_id"
//      if (params.signalDataTable == "array_data_detail_tmm_normalized") {
//        affyId = "probe_id"
//      } else if (params.signalDataTable == "focused_array_fold_changes") {
//        affyId = "target"
//      }

      // Gene list query
      def geneListId = params.long("geneListId") ?: -1
      boolean hasGeneList = geneListId != -1
      String geneListJoin = hasGeneList ? "JOIN gene_list_detail g ON g.gene_id = gi.geneid" : ""
      String geneListWhere = hasGeneList ? "g.gene_list_id = ${geneListId} AND " : ""

      // Module query
      String moduleJoin = "", moduleWhere = ""
      def moduleName = params.module
      def versionName = params.modVersionName
      if (moduleName)
      {
        if (params.analysis)
        {
          versionName = Version.get(Analysis.get(params.analysis)?.moduleVersion).versionName
        }
        def modGen = ModuleGeneration.findByVersionName(versionName).id
        def module = Module.findByModuleGenerationIdAndModuleName(modGen,moduleName)
        moduleJoin = "JOIN module_detail md ON b.${probeListColumn} = md.probe_id"
        moduleWhere = "md.module_id = ${module.id} AND"
      }
	  //           WHERE s.array_data_id = ${firstArrayDataId} AND
      String geneSearchQuery = """SELECT DISTINCT gi.symbol 'symbol', 'Gene Symbols' as 'list'
          FROM ${probeListTable} b
          JOIN ${tables.signalDataTable} s ON s.${tables.probeIdColumn} = b.${probeListColumn}
          ${moduleJoin}
          JOIN gene_info gi ON b.${symbolColumn} = gi.symbol
          ${geneListJoin}
		  WHERE ${geneListWhere} ${moduleWhere}
          gi.symbol LIKE '${term}%'
          LIMIT ${params.limit}""".toString()
		  
//	  println "query: " + geneSearchQuery
		  
//	List matchingGenes = []
//  sql.eachRow(geneSearchQuery) {
//      matchingGenes.push([ symbol:it.symbol, list:"Gene list" ])
//    }
	  List matchingGenes = sql.rows(geneSearchQuery)
	  //println "# matching genes: " + matchingGenes.size
      return matchingGenes
    }
	// no sample set
    return null
//    else
//    {
//      def symbolQuery = """SELECT DISTINCT symbol, geneid, name FROM gene_info
//        WHERE symbol LIKE '${params.term}%' ORDER BY symbol LIMIT 10""".toString()
//
//      def matchingGenes = []
//      sql.eachRow(symbolQuery) {
//        matchingGenes.push([gene:it.symbol, list:"Gene symbols"])
//      }
//
//      if (matchingGenes.size() < 10) {
//        def limit = 10 - matchingGenes.size()
//        def nameQuery = """SELECT DISTINCT symbol, geneid, name FROM gene_info
//          WHERE name LIKE '%${params.term}%' ORDER BY name LIMIT ${limit}""".toString()
//        sql.eachRow(nameQuery) {
//          matchingGenes.push([gene:"${it.symbol} - ${it.name}", list:"Gene names"])
//        }
//      }
//      return matchingGenes
//    }
  }

  def List geneList(long geneListId) {
    def geneList = []
    def sql = new Sql(dataSource)
    def query = "SELECT gene_id FROM gene_list_detail WHERE gene_list_id = ${geneListId}".toString()
    sql.eachRow(query) {
      geneList.push(it.gene_id)
    }
    sql.close()
    return geneList
  }

}
