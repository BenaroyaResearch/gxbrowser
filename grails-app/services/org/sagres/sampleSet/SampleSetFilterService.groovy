package org.sagres.sampleSet

import java.awt.geom.Arc2D.Double;

import groovy.sql.Sql
import common.ListItemList
import common.ListItem
import common.SecUser

import org.springframework.web.context.request.RequestContextHolder
import org.sagres.sampleSet.annotation.SampleSetSampleInfo
import org.sagres.sampleSet.annotation.SampleSetAdminInfo
import org.sagres.sampleSet.annotation.SampleSetAnnotation
import common.chipInfo.ChipsLoaded
import common.chipInfo.ChipType
import org.sagres.sampleSet.SampleSetLink
import org.sagres.sampleSet.component.FileTag
import org.sagres.sampleSet.annotation.SampleSetPlatformInfo
import common.chipInfo.Species
import org.sagres.sampleSet.component.LookupListDetail
import common.chipInfo.GenomicDataSource
import common.SecRole

class SampleSetFilterService {

  private def tg2Datatypes = ["Benaroya", "Baylor"]
  private def static filterTypes = ["sampleSetSearch","platform","species","disease","sampleSource","institution", "principalInvestigator", "analyst", "dateSearch"]

  def sampleSetService //injected
  def tg2QueryService //injected
  def springSecurityService //injected
  def mongoDataService //injected
  def grailsApplication
  def dataSource; //injected
  def session = RequestContextHolder.currentRequestAttributes().getSession()

  def List buildSampleSetList(Sql sql, List sampleSets, isInternal) {
    def sampleSetList = []
	SecUser user = springSecurityService.currentUser
    sampleSets?.each { SampleSet ss ->
      def genomicDs = ss.genomicDataSource
      def numSamples = ss.sampleSetSampleInfo?.numberOfSamples ?: 0
//	  	println "sample Set: " + ss.id
//	  	println "roles to check: " + sampleSetRoles.findResults { it.sampleSetId == ss.id ? it.roleId : null } + " for: " + ss.id
		  
//        def rolesToCheck = SampleSetRole.findAllBySampleSetId(ss.id)
//        if (rolesToCheck && rolesToCheck.size() > 0) {
//            rolesToCheck.each {   cRole ->
//                def reqRole = SecRole.findById(cRole.roleId)
//
//                if (!user?.authorities?.authority?.contains(reqRole.authority)) {
//                    println "User (${user?.username}) does not have approrpiate authority to view sample set ${ss.id}"
//                } else {
//                    if (isInternal && genomicDs?.name in tg2Datatypes) {
//                        sampleSetList.push([sampleSet: ss, tg2Info: tg2QueryService.getSampleSetInfo(ss.id, sql), genomicDs: genomicDs, numSamples: numSamples])
//                    }
//                    else {
//                        sampleSetList.push([sampleSet: ss, genomicDs: genomicDs, numSamples: numSamples])
//                    }
//
//                }
//            }
//        } else {
//            if (isInternal && genomicDs?.name in tg2Datatypes) {
//                sampleSetList.push([sampleSet: ss, tg2Info: tg2QueryService.getSampleSetInfo(ss.id, sql), genomicDs: genomicDs, numSamples: numSamples])
//            }
//            else {
//                sampleSetList.push([sampleSet: ss, genomicDs: genomicDs, numSamples: numSamples])
//            }
//        }
	      if (isInternal && genomicDs?.name in tg2Datatypes) {
			  sampleSetList.push([sampleSet: ss, tg2Info: tg2QueryService.getSampleSetInfo(ss.id, sql), genomicDs: genomicDs, numSamples: numSamples])
          } else {
              sampleSetList.push([sampleSet: ss, genomicDs: genomicDs, numSamples: numSamples])
          }
    }

	
    return sampleSetList
  }

  def List buildStatusSetList(Sql sql, List sampleSets, isInternal) {
	  def sampleSetList = []
	  sampleSets?.each { SampleSet ss ->
		def genomicDs  = ss.genomicDataSource
		def numSamples = ss.noSamples ?: 0
		
		// Find the end date of the most parental sampleSet (walk up the parent tree).
		
		def dateEnded
		for (sss in ss) {
			def origss = sss
			//
			while (sss.parentSampleSet && sss.id != sss.parentSampleSet.id) {
				// println "going deeper for: " + origss.id + " and it's parent: " + ss.id + " and its parent " +ss.parentSampleSet.id
				sss = sss.parentSampleSet
			}
	  
			def parentChip = ChipsLoaded.findBySampleSet(sss)
			if (parentChip) {
			  dateEnded = parentChip.dateEnded
			} else {
				dateEnded = 0
			}
		}
  	  
		def analyses = 0
		// Count the number of analyses done.
		def analysesQuery = """SELECT a.id 'aId', a.data_set_groups 'groupSetId', a.dataset_name 'name', a.run_date 'runDate'
			FROM analysis_summary AS s
			LEFT JOIN analysis AS a ON s.analysis_id = a.id
			LEFT JOIN sample_set AS ss ON a.sample_set_id = ss.id
			LEFT JOIN dataset_group_set as dgs on a.data_set_groups = dgs.id
			  WHERE ss.id = ${ss.id}
			  AND s.results_loaded_field IS NOT NULL
				AND a.flag_delete IS NULL
				AND s.analysis_complete_time IS NOT NULL
				AND a.sample_set_id >= 0 AND a.data_set_groups >= 0
				AND ss.marked_for_delete IS NULL"""
			// ORDER BY a.run_date DESC, a.dataset_name ASC"""
  
		sql.eachRow(analysesQuery) {
			analyses++
		}
		
		def statusDetails = []
  
		if (ss.sampleSetAdminInfo == null) {
			println "DB integrity error: Admin Info null for sampleSet: " + ss.name + " (" + ss.id + ")"
		}
		
		def statusAnalyst = (ss.sampleSetAdminInfo?.analyst) ? 1 : 0
		def statusAdmin = (ss.sampleSetAdminInfo?.principleInvestigator) ? 1 : 0
		statusAdmin += (ss.sampleSetAdminInfo?.institution) ? 1 : 0
		
		if (statusAdmin > 0)
		{
			statusAdmin /= 2
		}

		def sName = ss.name
		def fName
		def statusName
		for (cf in FileTag.findByTag('Chip Files')) {
			fName = ss.sampleSetFiles?.find{it.tag == cf}?.filename
			statusName = (!(sName.endsWith(".txt") || sName.endsWith(".tsv") || sName.endsWith(".soft") || fName?.equals(sName))) ? 1 : 0
		}

		if (ss.sampleSetAnnotation == null) {
			println "DB integrity error: Annotation null for sampleSet: " + ss.name + " (" + ss.id + ")"
		}

		def statusDesign = (ss.sampleSetAnnotation?.purpose) ? 1 : 0
		statusDesign += (ss.sampleSetAnnotation?.hypothesis) ? 1 : 0
		statusDesign += (ss.sampleSetAnnotation?.experimentalDesign) ? 1 : 0
		statusDesign += (ss.sampleSetAnnotation?.experimentalVariables) ? 1 : 0
		statusDesign += (ss.sampleSetAnnotation?.controls) ? 1 : 0
		
		if (statusDesign > 0)
		{
			statusDesign /= 5
		}

		if (ss.sampleSetSampleInfo == null) {
			println "DB integrity error: sample Info null for sampleSet: " + ss.name + " (" + ss.id + ")"
		}

		def statusInfo = (ss.sampleSetSampleInfo?.treatmentProtocol) ? 1 : 0
		statusInfo += (ss.sampleSetSampleInfo?.growthProtocol) ? 1 : 0
		statusInfo += (ss.sampleSetSampleInfo?.extractionProtocol) ? 1 : 0
		statusInfo += (ss.sampleSetSampleInfo?.storageConditions) ? 1 : 0
		statusInfo += (ss.sampleSetSampleInfo?.sampleSources) ? 1 : 0
		
		if (statusInfo > 0)
		{
			statusInfo /= 5
		}
		
		def statusFile = (ss.sampleSetFiles?.find{it.tag != 5}?.filename) ? 1 : 0 // are there any files other than signal
		
		def statusGroup = (ss.groupSets?.size() > 1) ? 1 : 0	// Is there more than one groupset?
		if (statusGroup == 0) {									// If not, then at least does this single groupSet have multiple groups?
			statusGroup = (ss.groupSets?.find{max:1}?.groups?.size() > 1) ? 1 : 0
		}
		
		// println "SampleSet: " + ss.id + " - group Sets: " + ss.groupSets?.name + " count: " + ss.groupSets?.size()
		
		// def statusRank = (ss.defaultRankList?.numProbes) ? 1 : 0
		def statusRank = (ss.groupSets?.defaultRankList?.numProbes) ? 1 : 0
		
		// println "SampleSet: " + ss.id + " - group Sets RankLists: " + ss.groupSets?.defaultRankList?.numProbes
		
		def statusAnalysis = 0
		for (ml in SampleSetLink.findByName('Modules')) {
			statusAnalysis = (ss.links?.find{it.linkType == ml}?.dataUrl) ? 1 : 0
			// println "ssid: " + ss.id + " moduleLink: " + ml + " analysis: " + statusAnalysis
		}
		def statusBrowser = (ss.gxbPublished) ? 1 : 0
		
		def fractionComplete = statusAnalyst + statusName + statusDesign + statusInfo + statusAdmin +
							statusFile + statusGroup + statusRank + statusAnalysis + statusBrowser
							
		if (fractionComplete > 0)
		{
			fractionComplete /= 10
		}
		
		if (ss.approved == 1)
		{
			fractionComplete = 1.0
		}
	  
		if (ss.fractionComplete != fractionComplete) {
			ss.fractionComplete = fractionComplete
		}
  
		
		statusDetails.push([dateEnded: dateEnded, analyses: analyses, statusAnalyst: statusAnalyst, statusName: statusName, statusDesign: statusDesign,
							  statusInfo: statusInfo, statusAdmin : statusAdmin, statusFile : statusFile,
							statusGroup: statusGroup, statusRank: statusRank, statusAnalysis : statusAnalysis,
							statusBrowser : statusBrowser, fractionComplete: fractionComplete])
		
			
		if (isInternal && genomicDs?.name in tg2Datatypes)
		{
		  sampleSetList.push([sampleSet: ss, tg2Info: tg2QueryService.getSampleSetInfo(ss.id), genomicDs: genomicDs, numSamples: numSamples,
			  statusDetails : statusDetails])
		}
		else
		{
		  sampleSetList.push([sampleSet: ss, genomicDs: genomicDs, numSamples: numSamples,
			  statusDetails : statusDetails])
		}
		
	  }
	  
	  return sampleSetList
	}
  
  def Map getFilterLists(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false) {
	  def start, roles, filters, top, bottom, ssCount = 0
      SecUser user = springSecurityService.currentUser
	  def username = "anonymous"
	  if (user) {
		  username = user?.username
	  }
      List<SampleSet> filteredSampleSets = new ArrayList<SampleSet>()
	  def allowed = []
	  def disallowed = []
	  try {
		  if (session?.logTiming) {
			  start = System.currentTimeMillis()
		  }
	  } catch (IllegalStateException ise) {
	  }
	  
	  // Grab all sampleSet roles, test user roles, and remove sampleSets as necessary
	  def sampleSetRole = SampleSetRole.findAll()
	  sampleSetRole?.each {
		  ssCount++
		  def reqRole = SecRole.findById(it.roleId)
		  if (!user?.authorities?.authority?.contains(reqRole.authority)) {
			disallowed.add(it.sampleSetId)
		  }	else {
		  	allowed.add(it.sampleSetId)
		  }	  
	  }

	  def remove = disallowed.unique() - allowed.unique(); 
	  // remove each sample set that was disallowed without having an allowed role. (e.g. disjunctive test)
	  remove?.each {
		  // print " Disallowed: " + it + " is in allowed? : " + allowed
//		  if (!allowed?.unique().contains(it)) {
		  println "${username} user does not have necessary authority to view sample set ${it}"
		  sampleSets.remove(SampleSet.findById(it))
//		  }
	  }
	  
//      sampleSets.each {
	  //		  ssCount++
//          def rolesToCheck = SampleSetRole.findAllBySampleSetId(it.id)
//          if (rolesToCheck && rolesToCheck.size() > 0) {
//			  println "required rolecheck " + it.id
//              rolesToCheck.each {   cRole ->
//                  def reqRole = SecRole.findById(cRole.roleId)
//
//                  if (!user?.authorities?.authority?.contains(reqRole.authority)) {
//                      println "User ${username} does not have necessary authority to view sample set ${it.id}"
//                  } else {
//                      filteredSampleSets.add(it)
//                  }
//              }
//          } else {
//              filteredSampleSets.add(it)
//          }
//      }

	  try {
		  if (session?.getAttribute("logTiming")) {
			  roles = System.currentTimeMillis()
			  println "SampleSet role check for: " + ssCount + " took: " + (roles - start) + "ms"
		  }
	  } catch (IllegalStateException ise) {
	  }

	def setsRequiringLogin = null
    def platforms = platformList(sql, sampleSets, countAnalysis, setsRequiringLogin)
    def species = speciesList(sql, sampleSets, countAnalysis, setsRequiringLogin)
    def diseases = diseaseList(sql, sampleSets, countAnalysis, setsRequiringLogin)
    def sampleSources = sampleSourceList(sql, sampleSets, countAnalysis, setsRequiringLogin)
    def institutions = institutionList(sql, sampleSets, countAnalysis, setsRequiringLogin)
	def principalInvestigators = principalInvestigatorList(sql, sampleSets, countAnalysis, setsRequiringLogin)
	//Not a checkbox just return the UI features
	def significantGenes = significantGeneList(sql, sampleSets, countAnalysis, setsRequiringLogin)
	//Not a checkbox just return the UI features
	def analysts = analystList(sql, sampleSets, countAnalysis, setsRequiringLogin)

	try {
		if (session?.getAttribute("logTiming")) {
			filters = System.currentTimeMillis()
			println "SampleSet post filters: " + (filters - roles) + "ms"
		}
	} catch (IllegalStateException ise) {
	}

	return [platforms: platforms, species: species, diseases: diseases, sampleSources: sampleSources, institutions: institutions,
			principalInvestigators: principalInvestigators, analysts: analysts, significantGenes: significantGenes]
  }

  def ListItemList platformList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetQuery = sampleSets ? "AND ss.id IN (${sampleSets.id.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
    def query = """SELECT p.platform_option, COUNT(p.platform_option) 'nItems', -1 'itemId'
      FROM sample_set_platform_info p
      JOIN sample_set ss ON ss.id = p.sample_set_id"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
    query += """ WHERE ss.marked_for_delete IS NULL
      ${analysisWhere}
      ${sampleSetQuery}
      ${privateSets}
      AND p.platform_option IS NOT NULL
      GROUP BY p.platform_option"""
    return createReturnList(sql, query, "platform_option", [uiLabel: "Platform", paramName: "platformOption"])
  }

  def ListItemList speciesList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetQuery = sampleSets ? "AND si.sample_set_id IN (${sampleSets.id.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
    def query = """SELECT s.latin 'species', s.id 'itemId', COUNT(s.latin) 'nItems'
      FROM sample_set_sample_info AS si
      JOIN sample_set ss ON ss.id = si.sample_set_id"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
    query += """ JOIN species AS s ON s.id = si.species_id
      WHERE ss.marked_for_delete IS NULL
      ${analysisWhere}
      ${sampleSetQuery}
      ${privateSets}
      GROUP BY s.latin"""
    return createReturnList(sql, query, "species", [uiLabel: "Species", paramName: "species"])
  }

  def ListItemList diseaseList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetQuery = sampleSets ? "AND si.sample_set_id IN (${sampleSets.id.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
    def query = """SELECT ll.name 'disease', ll.id 'itemId', COUNT(ll.name) 'nItems'
      FROM sample_set_sample_info AS si
      JOIN sample_set ss ON ss.id = si.sample_set_id"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
    query += """ JOIN lookup_list_detail AS ll ON ll.id = si.disease_id
      WHERE ss.marked_for_delete IS NULL
      ${analysisWhere}
      ${sampleSetQuery}
      ${privateSets}
      GROUP BY ll.name"""
    return createReturnList(sql, query, "disease", [uiLabel: "Disease", paramName: "disease"])
  }

  def ListItemList sampleSourceList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetSampleInfos = sampleSets?.sampleSetSampleInfo?.collect { SampleSetSampleInfo info -> info?.id } // data integrity issue - #370 doesn't have a SSSI record.
    def sampleSetQuery = sampleSetSampleInfos ? "AND sample_set_sample_info_sample_sources_id IN (${sampleSetSampleInfos.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
    def query = """SELECT ll.name 'sample_source', ll.id 'itemId', COUNT(ll.name) 'nItems'
      FROM sample_set_sample_info_lookup_list_detail AS si
      JOIN sample_set_sample_info ssi ON si.sample_set_sample_info_sample_sources_id = ssi.id
	    JOIN sample_set ss ON ss.id = ssi.sample_set_id"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
    query += """ JOIN lookup_list_detail AS ll ON ll.id = si.lookup_list_detail_id
      WHERE ss.marked_for_delete IS NULL
      ${analysisWhere}
      ${sampleSetQuery}
      ${privateSets}
      GROUP BY ll.name"""
    return createReturnList(sql, query, "sample_source", [uiLabel: "Sample Source", paramName: "sampleSources"])
  }

  def ListItemList institutionList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetQuery = sampleSets ? "AND ss.id IN (${sampleSets.id.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
    def query = """SELECT gds.display_name 'institution', gds.id 'itemId', COUNT(gds.id) 'nItems'
      FROM sample_set AS ss"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
    query += """ JOIN genomic_data_source gds ON gds.id = ss.genomic_data_source_id
      WHERE ss.marked_for_delete IS NULL
      ${analysisWhere}
      ${sampleSetQuery}
      ${privateSets}
      GROUP BY gds.id"""
//    def altQuery = """SELECT gds.name, COUNT(DISTINCT s.sample_set_id)
//      FROM dataset_group_set s, dataset_group g, dataset_group_detail d, array_data a, chips_loaded cl, genomic_data_source gds
//		  ${sampleSetQuery}
//			  AND g.group_set_id = s.id
//		    AND d.group_id = g.id
//		    AND a.id = d.sample_id
//			  AND cl.id = a.chip_id
//        AND gds.id = cl.genomic_data_source_id
//        AND cl.load_status_id = 3
//        GROUP BY gds.name"""
    return createReturnList(sql, query, "institution", [uiLabel: "Institution", paramName: "institution"])
  }

  def ListItemList principalInvestigatorList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
    def sampleSetAdminInfos = sampleSets?.sampleSetAdminInfo?.collect { SampleSetAdminInfo info -> info?.id } // data integrity issue - #370 doesn't have a SSSI record.
	  def sampleSetQuery = sampleSetAdminInfos ? "AND ssai.sample_set_id IN (${sampleSets.id.join(",")})" : ""
    def privateSets = privateDs ? "AND ss.genomic_data_source_id NOT IN (${privateDs.join(',')})" : ""
	  def query = """SELECT DISTINCT(ssai.principle_investigator) 'principal_investigator', ssai.id 'itemId',
	  	COUNT(principle_investigator) 'nItems'
	  	FROM sample_set_admin_info AS ssai
	  	JOIN sample_set ss ON ss.id = ssai.sample_set_id"""
    def analysisWhere = ""
    if (countAnalysis) {
      query += """ JOIN analysis a ON ss.id = a.sample_set_id
                   JOIN analysis_summary asum ON asum.analysis_id = a.id """
      analysisWhere = """AND asum.analysis_complete_time IS NOT NULL
                         AND a.flag_delete IS NULL
                         AND a.data_set_groups >= 0
                         AND asum.results_loaded_field IS NOT NULL"""
    }
	  query += """ WHERE ss.marked_for_delete IS NULL
	    ${analysisWhere}
	  	${sampleSetQuery}
	  	${privateSets}
		  AND ssai.principle_investigator IS NOT NULL
	  	AND ssai.principle_investigator != ""
		  GROUP BY principle_investigator"""
	  // println query;
	  return createReturnList(sql, query, "principal_investigator", [uiLabel: "Principal Investigator", paramName: "principalInvestigator"])
  }
  
  def ListItemList analystList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
	  return ([uiLabel: "Analyst", paramName: "analyst"])
  }

  def ListItemList significantGeneList(Sql sql, List<SampleSet> sampleSets, boolean countAnalysis = false, List privateDs = null) {
	  return ([uiLabel: "Significant Genes", paramName: "significantGenes"])
  }
  
  private def ListItemList createReturnList(Sql sql, String query, String fieldName, Map listProperties)
  {
//	def top, bottom
    def retList = new ListItemList(listProperties)
//	if (session?.getAttribute("logTiming")) {
//		top = System.currentTimeMillis()
//	}
    sql.eachRow(query, { row ->
      retList.items.add(new ListItem(
        uiLabel: row.getProperty(fieldName),
        dbLabel: row.getProperty(fieldName),
        dbId: row.itemId,
        nItems: row.nItems))
    })
//	if (session?.getAttribute("logTiming")) {
//		bottom = System.currentTimeMillis()
//		println "sampleSet based query: " + query + "\ntook " + (bottom - top) + "ms"
//	}

    return retList
  }

  def void filter(List<SampleSet> sampleSets, Map params, List searchResults, Map checkedBoxes, Map dataResults)
  {
    def searchTerm = params.sampleSetSearch
    if (searchTerm && searchTerm.trim() != "")
    {
		searchResults.addAll(searchTerm)
		sampleSets.retainAll(textSearch(searchTerm))

    }
	
	def sliderMin = params.sliderMin
	def sliderMax = params.sliderMax
	if ((sliderMin && sliderMin.toInteger() > 0) || (sliderMax && sliderMax.toInteger() < 100))
	{
		// println "slider Filter: " + sliderMin + " max: " + sliderMax
		searchResults.addAll("between " + sliderMin + "% and " + sliderMax + "% complete")
		sampleSets.retainAll(applyFilterPercentComplete(sliderMin, sliderMax))
	}

	def dateSearch = params.dateSearch
	if (dateSearch)
	{
		// println "start Date: " + params.dateSearch
		//def jdateSearch = new Date(params.dateSearch)
		searchResults.addAll("sample sets loaded since " + dateSearch)
		sampleSets.retainAll(applyFilterDateSearch(dateSearch))
	}

	def analystTerm = params.analystSearch
	if (analystTerm && analystTerm?.trim() != "")
	{
		searchResults.addAll(analystTerm) // hack, show them only the first one.
		sampleSets.retainAll(applyFilterAnalyst(analystTerm))
	}

	def significantGeneTerm = params.significantGeneSearch
	def foldChange = params.foldChangeMin
	if (significantGeneTerm && significantGeneTerm?.trim() != "")
	{
		searchResults.addAll(significantGeneTerm + " " + foldChange + " fold expression change")
		sampleSets.retainAll(applyFilterSignificantGenes(significantGeneTerm, foldChange, dataResults))
	}
	
    if (params.platform)
    {
      def platforms = params.platform instanceof String ? [params.platform] : params.platform.collect { it }
      checkedBoxes.put("platform", platforms)
      searchResults.addAll(platforms)

      sampleSets.retainAll(applyFilterPlatform(platforms))
    }
    if (params.species)
    {
      def species = filterKeys(params, "species", checkedBoxes)
      def speciesList = Species.findAllByIdInList(species)
      searchResults.addAll(speciesList.latin)

      sampleSets.retainAll(applyFilterSpecies(speciesList))
    }
    if (params.disease)
    {
      def diseases = filterKeys(params, "disease", checkedBoxes)
      def diseaseList = LookupListDetail.findAllByIdInList(diseases)
      searchResults.addAll(diseaseList.name)

      sampleSets.retainAll(applyFilterDisease(diseaseList))
    }
    if (params.sampleSource)
    {
      def sampleSources = filterKeys(params, "sampleSource", checkedBoxes)
      def sampleSourceList = LookupListDetail.findAllByIdInList(sampleSources)
      searchResults.addAll(sampleSourceList.name)
      sampleSets.retainAll(applyFilterSampleSource(sampleSets, sampleSources))
    }
    if (params.institution)
    {
      def institutions = filterKeys(params, "institution", checkedBoxes)
      def genomicDataSources = GenomicDataSource.findAllByIdInList(institutions)
      searchResults.addAll(genomicDataSources.name)

      sampleSets.retainAll(applyFilterInstitution(genomicDataSources))
    }
    if (params.principalInvestigator)
    {
      def principalInvestigators = filterKeys(params, "principalInvestigator", checkedBoxes)
        def principalInvestigatorList = SampleSetAdminInfo.findAllByIdInList(principalInvestigators)
        searchResults.addAll(principalInvestigatorList.principleInvestigator)

        sampleSets.retainAll(applyFilterPrincipalInvestigator(principalInvestigatorList))
    }
	
  }

  private def List filterKeys(Map params, String key, Map checkedBoxes)
  {
    def requirements = params.get(key) instanceof String ? [Long.parseLong(params.get(key))] : params.get(key).collect { Long.parseLong(it) }
    def checkedKeys = params.get(key) instanceof String ? [Integer.parseInt(params.get(key))] : params.get(key).collect { Integer.parseInt(it) }
    checkedBoxes.put(key, checkedKeys)
    return requirements
  }

  def List<SampleSet> textSearch(String searchTerm)
  {
    return SampleSet.findAllByNameIlikeOrDescriptionIlike("%${searchTerm}%", "%${searchTerm}%")
  }
  
  def List<SampleSet> applyFilterPlatform(List<String> platforms)
  {
    def platformSets = []
    SampleSetPlatformInfo.findAllByPlatformOptionInListAndSampleSetIsNotNull(platforms).each {
      try {
        platformSets.add(it.sampleSet)
      } catch (Exception e) {
      }
    }
    return platformSets
  }

  def List<SampleSet> applyFilterSpecies(List<Species> species)
  {
    def speciesSets = []
    SampleSetSampleInfo.findAllBySpeciesInListAndSampleSetIsNotNull(species).each {
      try {
        speciesSets.add(it.sampleSet)
      } catch (Exception e) {
      }
    }
    return speciesSets
  }

  def List<SampleSet> applyFilterDisease(List<LookupListDetail> diseases)
  {
    return SampleSetSampleInfo.findAllByDiseaseInListAndSampleSetIsNotNull(diseases).sampleSet
  }

  def List<SampleSet> applyFilterSampleSource(List<SampleSet> sampleSets, List<Long> sampleSources)
  {
    def where = ""
    sampleSources.each {
      if (where == "")
        where += "WHERE ${it} in elements(ssi.sampleSources)"
      else
        where += " OR ${it} in elements(ssi.sampleSources)"
    }
    def sampleInfoSources = SampleSetSampleInfo.findAll("from SampleSetSampleInfo as ssi ${where}")
    return sampleInfoSources.sampleSet
  }

  def List<SampleSet> applyFilterInstitution(List<GenomicDataSource> institutions)
  {
    return SampleSet.findAllByGenomicDataSourceInListAndMarkedForDeleteIsNull(institutions)
//    def institutionSets = []
//    ChipsLoaded.findAllByGenomicDataSourceInListAndSampleSetIsNotNull(institutions).each {
//      try {
//        institutionSets.add(it.sampleSet)
//      } catch (Exception e) {
//      }
//    }
//    return institutionSets
  }

  def List<SampleSet> applyFilterPrincipalInvestigator(List<SampleSetAdminInfo> pIs)
  {
	  return SampleSetAdminInfo.findAllByPrincipleInvestigatorInListAndSampleSetIsNotNull(pIs.principleInvestigator).sampleSet
  }
  
  def List<SampleSet> applyFilterAnalyst(String analyst)
  {
	  return SampleSetAdminInfo.findAllByAnalystAndSampleSetIsNotNull(analyst).sampleSet
  }

  def List<SampleSet> applyFilterDateSearch(String dateSearched)
  {
	  // println "Comparing dateSearched: " + dateSearched
	  // String sdateSearched = '2011-12-01'
	  def dateTimeFormat = new java.text.SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
	  def jdateSearched = dateTimeFormat.parse("${dateSearched} 00:00:00");
	  def chipsLoadedSources = ChipsLoaded.findAll("FROM ChipsLoaded WHERE dateEnded >= ?", [jdateSearched] ).sampleSet
	  // println "results: " + chipsLoadedSources.size()
	  
	  return chipsLoadedSources
  }

  def List<SampleSet> applyFilterPercentComplete(String percentLower, String percentUpper)
  {
	  double jfractionLower = (double) Double.parseDouble(percentLower) / 100
	  double jfractionUpper = (double) Double.parseDouble(percentUpper) / 100
	// println "jfractionLower: " + jfractionLower + " jfractionUpper: " + jfractionUpper
	// def sampleSets = SampleSet.findAll("FROM SampleSet ss WHERE ss.percentComplete <= ?", [jpercentComplete])
	def sampleSets = SampleSet.findAllByFractionCompleteBetweenAndMarkedForDeleteIsNull(jfractionLower, jfractionUpper)
	// println "results: " + sampleSets.size()
	return sampleSets
  
  }
  
  def List<SampleSet> applyFilterSignificantGenes(String symbol, String foldChange, Map results)
  {
	  def probes = []
	  def interestingSampleSets = []
	  Sql sql = Sql.newInstance(dataSource)

	  //long startTime = System.currentTimeMillis()
	  int ccount = 0
	  int pcount = 0
	  
	  // Doing the comparison in log2 space.
	  def log2FoldChange = Math.log(foldChange.toDouble().doubleValue())/Math.log(2)
	  
//	  def geneQuery = """SELECT id AS Id, geneid AS geneId FROM gene_info WHERE symbol = ${symbol}"""
//	  def geneId = sql.firstRow(geneQuery)
	  
	  // Get the list of chiptypes and their source fields
	  def chips = ChipType.findAll()
	  chips?.each {
		def tableName    = it.probeListTable
		def probeColumn  = it.probeListColumn 
	  	def symbolColumn = it.symbolColumn

		// Get the list of probes
		if (tableName != null && probeColumn != null && symbolColumn != null) {
			def symbolQuery = """SELECT ${probeColumn} AS probeColumn, ${symbolColumn} AS symbolColumn FROM ${tableName} WHERE ${symbolColumn} = '${symbol}'""".toString()
			// println "query: " + symbolQuery
			sql.eachRow(symbolQuery) {
				if(!probes.contains(it.probeColumn)) {
					probes.push(it.probeColumn)
					pcount++
				}
			}
			ccount++
		}
	  }
	  //println "${pcount} probes for ${ccount} chips done @ ${System.currentTimeMillis() - startTime} ms"
   	  //println "the probes for " + symbol + " are: " + probes

	  // Get the list of samplesets based on the ranklists where 0.5 > foldChange > 2.0 (do it in log space).
	  // def samplesQuery = """SELECT probe_id AS probeId, FORMAT(value, 2) AS value, sample_set_id as ssId, ss.name AS name, rl.description AS description from rank_list_detail as rld, rank_list as rl, sample_set as ss where rl.rank_list_type_id = 3 AND rl.id = rld.rank_list_id AND rl.sample_set_id = ss.id AND probe_id IN ('""" + probes.join("', '") + """') AND ABS(value) > 2"""
//	  def samplesQuery = """SELECT probe_id AS probeId, FORMAT(value, 2) AS value, sample_set_id as ssId, rl.id AS rlId, REPLACE(rl.description, "PALO ", "") AS description FROM rank_list_detail as rld, rank_list as rl WHERE rl.rank_list_type_id = 3 AND rl.id = rld.rank_list_id AND probe_id IN ('""" + probes.join("', '") + """') AND ABS(LOG2(value)) > """ + log2FoldChange + " AND description LIKE 'PALO%' GROUP BY sample_set_id"
//
//  	  //println "query is: " + samplesQuery, 
//	  sql.eachRow(samplesQuery) {
//		interestingSampleSets.push(it.ssId)
//		println "sampleSet: " + it.ssId + " probeId: " + it.probeId + " name: " + it.description
//		results.put(it.ssId, [rlid: it.rlId, rlname: it.description, probeId: it.probeId, value: it.value]);
//	  }
	  probes.each {
		  def samplesQuery = """SELECT probe_id AS probeId, FORMAT(value, 2) AS value, sample_set_id as ssId, rl.id AS rlId, REPLACE(rl.description, "PALO ", "") AS description FROM rank_list_detail as rld, rank_list as rl WHERE rl.rank_list_type_id = 3 AND rl.id = rld.rank_list_id AND probe_id = '""" + it + """' AND ABS(LOG2(value)) > """ + log2FoldChange + """ AND description LIKE 'PALO%' GROUP BY sample_set_id"""
		  sql.eachRow(samplesQuery) {
		  	interestingSampleSets.push(it.ssId)
			//println "sampleSet: " + it.ssId + " probeId: " + it.probeId + " name: " + it.description
		  	results.put(it.ssId, [rlid: it.rlId, rlname: it.description, probeId: it.probeId, value: it.value]);
		  }
	  }
	  //println "samples done @ ${System.currentTimeMillis() - startTime} ms"
	  def sampleSets = SampleSet.findAllByIdInList(interestingSampleSets)
	  // println "these are the sets: " + sampleSets

	  sql.close()
	  
	  return sampleSets
	  
  }
  
  def saveFilter(String filterName, Map queryParams)
  {
    def username = "*"
    if (springSecurityService.isLoggedIn())
    {
      username = springSecurityService.getCurrentUser()?.username ?: "*"
    }
    def mongoValues = [:]
    queryParams?.each { String key, value ->
      if (key in filterTypes)
        mongoValues.put("query.${key}".toString(), value)
    }
    mongoDataService.remove("savedfilters", [user:username, name:filterName], "query")
    mongoDataService.update("savedfilters", [user:username, name:filterName], mongoValues)
  }
}
