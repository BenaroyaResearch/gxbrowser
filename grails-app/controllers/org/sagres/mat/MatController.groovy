package org.sagres.mat

import org.sagres.sampleSet.SampleSet
import groovy.sql.Sql
import common.chipInfo.ChipsLoaded
import groovy.sql.GroovyRowResult
import grails.converters.JSON
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import common.chipInfo.GenomicDataSource
import org.sagres.sampleSet.SampleSetRole
import common.SecRole

class MatController {

  def dataSource //injected
  def sampleSetFilterService //injected
  def sampleSetService
  def matPlotService
  def grailsApplication

  def mongoDataService
  def springSecurityService



  def index = {
    redirect(action: "list")
  }

  def list = {
    Sql sql = Sql.newInstance(dataSource)

    List<SampleSet> sampleSets = matSampleSets(sql, params)

    def model = [:]
    def filters = sampleSetFilterService.getFilterLists(sql, sampleSets, true)
    model.putAll(filters)

    def searchTerm = params.sampleSetSearch
    if (searchTerm && searchTerm.trim() != "")
    {
      String query = searchQuery(searchTerm)
      Set<SampleSet> querySampleSets = []
      sql.eachRow(query) {
        querySampleSets.add(SampleSet.findById(Analysis.findById(it.id).sampleSetId))
      }
      sampleSets.retainAll(querySampleSets)
      params.searchResults = params.sampleSetSearch
    }
    params.filterPanelShow = "true"
    def sampleSetIds = params.searchResults || params.sampleSetId || params.dataSetGroups ? sampleSets.id : null

    def sampleSetInfo = sampleSetInfo(sql, sampleSets)
    def matInfo = matInfo(sql, sampleSetIds)
    model.putAll([sampleSetInfo: sampleSetInfo, matInfo: matInfo, params:params])
    sql.close()

    return model
  }

  def filteredSampleSets = {

    Sql sql = Sql.newInstance(dataSource)


    List<SampleSet> sampleSets = matSampleSets(sql, params)

    def model = [:]
    def filters = sampleSetFilterService.getFilterLists(sql, sampleSets, true)
    model.putAll(filters)

    def searchTerm = params.sampleSetSearch
    if (searchTerm && searchTerm.trim() != "")
    {
      sampleSets.retainAll(sampleSetFilterService.textSearch(searchTerm))
      params.searchResults = params.sampleSetSearch
    }

    def searchResults = [], checkedBoxes = [:], dataResults = [:]
    sampleSetFilterService.filter(sampleSets, params, searchResults, checkedBoxes, dataResults)

    def to = Math.min(5, searchResults.size())
    params.searchResults = searchResults[0..<to]

    def sampleSetInfo = sampleSetInfo(sql, sampleSets)
    def matInfo = matInfo(sql, sampleSets.id)

    sql.close()

    model.putAll([sampleSetInfo: sampleSetInfo, matInfo: matInfo, params: params, checkedBoxes: checkedBoxes])

    render(view: "list", model: model)
  }

  private List<SampleSet> matSampleSets(Sql sql, Map params) {
      SecUser user = springSecurityService.currentUser
    def sampleSet = "a.sample_set_id"
    sampleSet += params.sampleSetId ? " = ${params.sampleSetId}" : " >= 0"
    def dsGroups = "a.data_set_groups"
    dsGroups += params.dataSetGroups ? " = ${params.dataSetGroups}" : " >= 0"
		def loggedIn = springSecurityService.isLoggedIn()

		def setsRequiringLogin = grailsApplication.config.genomic_datasource_names_requiring_login

    def matSampleSetQuery = """SELECT a.sample_set_id 'ssid', COUNT(a.id) 'numAnalysis'
      FROM analysis AS a
	    JOIN analysis_summary AS s ON s.analysis_id = a.id
      WHERE s.results_loaded_field IS NOT NULL
      AND ${sampleSet}
      AND ${dsGroups}
      AND a.flag_delete IS NULL
	  AND a.mat_published = 1
      GROUP BY a.sample_set_id""".toString()
    def sampleSets = []
    sql.eachRow(matSampleSetQuery, { row ->
      if (row.ssid >= 0)
      {
          def ss = SampleSet.get(row.ssid)
          def rolesToCheck = SampleSetRole.findAllBySampleSetId(row.ssid)
          if (rolesToCheck && rolesToCheck.size() > 0) {

              rolesToCheck.each {   cRole ->
                  def reqRole = SecRole.findById(cRole.roleId)
                  if (!user?.authorities?.authority?.contains(reqRole.authority)) {
                      println "User not logged in/ User does not have approrpiate authority - redirecting to login"
                  } else {
                      sampleSets.push(ss)
                  }
              }
          } else {
              sampleSets.push(ss)
          }
      }
    })
    return sampleSets
  }

  private def Map sampleSetInfo(Sql sql, List<SampleSet> sampleSets)
  {
    def sampleSetInfo = [:]
    sampleSets?.each { SampleSet ss ->
      // get genomic datasource
      def genomicDs = ss.genomicDataSource
      // get number of samples
      def numSamples = ss.noSamples ?: 0
      sampleSetInfo.put(ss.id, [platform: ss.sampleSetPlatformInfo.platformOption, genomicDs: genomicDs, numSamples: numSamples])
    }
    return sampleSetInfo
  }

  private def List matInfo(Sql sql, List<Long> sampleSetIds) {
    if (sampleSetIds != null && sampleSetIds.isEmpty())
    {
      return []
    }
    else
    {
        SecUser user = springSecurityService.currentUser


      // get MAT info
      def matInfo = [], lastSampleSetId = -1, datasetGroups = [:]
      def matQuery = """SELECT a.id, a.display_name 'display_name', a.dataset_name 'analysis_name', a.run_date, ss.id 'sample_set_id', ss.name 'sample_set_name', d.id 'group_set_id', d.name 'group_set_name', a.mod_generation, s.analysis_complete_time
        FROM analysis_summary AS s
        LEFT JOIN analysis AS a ON s.analysis_id = a.id
        LEFT JOIN sample_set AS ss ON a.sample_set_id = ss.id
        LEFT JOIN dataset_group_set AS d ON a.data_set_groups = d.id
        WHERE s.results_loaded_field IS NOT NULL
        AND a.flag_delete IS NULL
	  	AND a.mat_published = 1
        AND a.sample_set_id >= 0 AND a.data_set_groups >= 0
        AND ss.marked_for_delete IS NULL
        AND s.analysis_complete_time IS NOT NULL
        ORDER BY ss.id ASC, d.id ASC, s.analysis_complete_time DESC""".toString()
      sql.rows(matQuery).collect { GroovyRowResult row ->

        def sampleSetId = row.sample_set_id
        row.analysis_name = row.display_name ?: row.analysis_name.replaceAll(/[A-Z][a-z]/, { " ${it}".toString() }).replaceAll("\\s+", " ").trim()
        if (!sampleSetIds || (sampleSetIds && sampleSetIds.contains(sampleSetId)))
        {
                  if (sampleSetId) {
                      def rolesToCheck = SampleSetRole.findAllBySampleSetId(sampleSetId)
                      if (rolesToCheck && rolesToCheck.size() > 0) {
                          rolesToCheck.each {   cRole ->
                              def reqRole = SecRole.findById(cRole.roleId)
                              if (!user?.authorities?.authority?.contains(reqRole.authority)) {
                                  println "User not logged in/ User does not have approrpiate authority - redirecting to login"
                              } else {
                                  matInfo.push(row)
                              }
                          }
                      } else {
                          matInfo.push(row)
                      }
                  } else {
                      matInfo.push(row)

                  }
        }
      }
      return matInfo
    }
  }

    def titles = {
        SecUser user = springSecurityService.currentUser

        if (params.term != "") {
            String query = searchQuery(params.term)
            List titles = []
            Sql sql = Sql.newInstance(dataSource)
            sql.eachRow(query) { row ->
                def sampleSetId = row.sample_set_id
                if (sampleSetId) {

                    def rolesToCheck = SampleSetRole.findAllBySampleSetId(sampleSetId)
                    if (rolesToCheck && rolesToCheck.size() > 0) {

                        rolesToCheck.each {   cRole ->
                            def reqRole = SecRole.findById(cRole.roleId)
                            if (!user?.authorities?.authority?.contains(reqRole.authority)) {
                                println "User not logged in/ User does not have approrpiate authority - redirecting to login"
                            } else {
                                titles.push([text: "${row.g_name} :: ${row.a_name}", url: "/analysis/show/${row.id}"])
                            }
                        }
                    }
                } else {
                    titles.push([text: "${row.g_name} :: ${row.a_name}", url: "/analysis/show/${row.id}"])
                }

                //titles.push([text: "${row.g_name} :: ${row.a_name}", url: "/analysis/show/${row.id}"])
            }
            sql.close()
            render titles as JSON
        }
    }

  private String searchQuery(String searchTerm) {

    String queryStartsWith = """SELECT a.id, a.dataset_name 'a_name', d.name 'g_name', ss.id 'sample_set_id'
        FROM analysis AS a
        JOIN analysis_summary AS s ON s.analysis_id = a.id
        LEFT JOIN sample_set AS ss ON ss.id = a.sample_set_id
        LEFT JOIN dataset_group_set AS d ON d.id = a.data_set_groups
        WHERE s.results_loaded_field IS NOT NULL
        AND a.sample_set_id >= 0
        AND a.data_set_groups >= 0
        AND (a.display_name LIKE '%${searchTerm}%' OR a.dataset_name LIKE '%${searchTerm}%' OR ss.name LIKE '%${searchTerm}%')
        AND s.analysis_complete_time IS NOT NULL
        AND ss.marked_for_delete IS NULL
        AND a.flag_delete IS NULL
        LIMIT 10""".toString()

    return queryStartsWith
  }

}
