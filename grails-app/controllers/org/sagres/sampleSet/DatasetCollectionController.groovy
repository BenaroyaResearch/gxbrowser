package org.sagres.sampleSet

import grails.converters.JSON
import groovy.sql.Sql
import groovy.sql.GroovyRowResult
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class DatasetCollectionController {

  def dataSource
  def datasetCollectionService
  def mongoDataService
  def springSecurityService



  def index = {
    redirect(action:"list")
  }

  def list = {
    return [collections:datasetCollectionService.getCollections(null)]
  }

  def collectionSampleSets = {
    def collectionName = params.name
    if (collectionName)
    {
      def ssIds = datasetCollectionService.getSampleSetsForCollection(collectionName)
      def sampleSets = [:]
      Sql sql = Sql.newInstance(dataSource)
      def nameQuery = """SELECT ss.id 'id', ss.name 'name', p.platform_option 'platform' FROM sample_set AS ss
        LEFT JOIN sample_set_platform_info AS p ON ss.id = p.sample_set_id
        WHERE ss.id IN (${ssIds.join(",")})
        """.toString()
      def speciesDiseaseQuery = """SELECT i.sample_set_id 'id', s.latin 'species', ll.name 'disease' FROM sample_set_sample_info AS i
        LEFT JOIN species AS s ON i.species_id = s.id
        LEFT JOIN lookup_list_detail AS ll ON i.disease_id = ll.id
        WHERE i.sample_set_id IN (${ssIds.join(",")})""".toString()
      sql.rows(nameQuery).each { GroovyRowResult row ->
        sampleSets.put(row.id, [name:row.name, platform:row.platform])
      }
      sql.rows(speciesDiseaseQuery).each { GroovyRowResult row ->
        sampleSets.get(row.id).put("species", row.species)
        sampleSets.get(row.id).put("disease", row.disease)
      }
      sql.close()
      render sampleSets as JSON
    }
  }

  def collections = {
    def term = params.term
    render datasetCollectionService.getCollections(term).keySet() as JSON
  }

  def saveCollection = {
    def collectionName = params.collectionName
    datasetCollectionService.saveCollection(collectionName, null)
  }

  def addToCollection = {
    def sampleSetId = params.long("id")
    def collectionName = params.name
    datasetCollectionService.addToCollection(collectionName, sampleSetId, true)
  }

  def removeFromCollection = {
    def sampleSetId = params.long("id")
    def collectionName = params.name
    datasetCollectionService.removeFromCollection(collectionName, sampleSetId)
    render "removed"
  }

}
