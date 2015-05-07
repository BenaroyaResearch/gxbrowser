package org.sagres.sampleSet;


import com.mongodb.BasicDBObject
import common.ArrayData
import common.chipInfo.ChipsLoaded
import groovy.sql.Sql
import java.sql.SQLException
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication;
import org.sagres.sampleSet.SampleSetLink
import org.sagres.sampleSet.SampleSetLinkDetail
import org.sagres.sampleSet.annotation.SampleSetAdminInfo
import org.sagres.sampleSet.annotation.SampleSetAnnotation
import org.sagres.sampleSet.annotation.SampleSetPlatformInfo
import org.sagres.sampleSet.annotation.SampleSetSampleInfo
import org.sagres.sampleSet.component.SampleSetOverviewComponent
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefault
import org.sagres.sampleSet.component.SampleSetOverviewComponentDefaultSet
import org.sagres.util.mongo.MongoConnector
import org.sagres.sampleSet.annotation.SampleSetFile
import org.sagres.sampleSet.component.FileTag
import org.sagres.importer.ImportService;
import org.sagres.importer.Importer;
import org.apache.commons.io.FileUtils
import groovy.sql.GroovyRowResult
import common.chipInfo.RawSignalDataType
import common.chipInfo.GenomicDataSource
import common.chipInfo.ChipType
import common.chipInfo.ChipData
import org.sagres.rankList.RankListType
import org.sagres.rankList.RankList
import org.sagres.sampleSet.component.LookupListDetail;
import org.sagres.importer.TextTable;
import org.sagres.importer.TextTableSeparator;
import org.sagres.importer.ArrayDataImporter;
import org.sagres.sampleSet.MongoDataService;
import org.sagres.mat.MatConfigService


//*****************************************************************************

class SampleSetService
{                                                            //SampleSetService
//-----------------------------------------------------------------------------

  static transactional = true
  def dataSource; //injected
    def mongoDataService; //injected
    //Injection doesn't work reliably, so:
    def grailsApplication = new DefaultGrailsApplication( );


//=============================================================================

	def getGEOSeries(SampleSet sampleSet)
	{
		Map result = [:]

		def linkType = SampleSetLink.findByName('GEO')
		def linkData = SampleSetLinkDetail.findByLinkTypeAndSampleSet(linkType, sampleSet)
		if (linkData?.dataUrl) {
			result.put('dataUrl', linkData.dataUrl)
			def lastSlashIndex = linkData.dataUrl.lastIndexOf('=')
			def seriesId = linkData.dataUrl.substring(lastSlashIndex+1)
			result.put('seriesId', seriesId)
	    }
		return result
	}

	def getPMIDData(SampleSet sampleSet)
	{
		Map result = [:]

		def linkType = SampleSetLink.findByName('PMID')
		def linkData = SampleSetLinkDetail.findByLinkTypeAndSampleSet(linkType, sampleSet)
		if (linkData?.dataUrl) {
			result.put('dataUrl', linkData.dataUrl)
			def lastSlashIndex = linkData.dataUrl.lastIndexOf('/')
			def pmid = linkData.dataUrl.substring(lastSlashIndex+1)
			result.put('pmId', pmid)
		}
		return result
	}

  int createSampleSetFromChipID( long chipId, boolean cleanFirst = false )
  {
    def ChipsLoaded chip = ChipsLoaded.get( chipId )
    assert chip != null
    if (chip == null)
      return 0

    if ( cleanFirst )
    {
      def oldSampleSet = chip.sampleSet
      oldSampleSet.delete()
    }

    // get the array_data info
    def arrayDataList = ArrayData.findAllByChip(chip)
    assert arrayDataList.size() == chip.noSamples

    def sampleSet = createSampleSet( arrayDataList, chip.filename, chip.filename, chip )
	sampleSet.save( flush: true );

    // now save the link back to the chip
    chip.sampleSet = sampleSet
    chip.save( flush: true )

    return sampleSet.id
  }

  int createSampleSetFromSamples( List<ArrayData> arrayDataList, String name, String description )
  {
    if (arrayDataList)
    {
      def chip = arrayDataList.get(0).chip
      def sampleSet = createSampleSet(arrayDataList, name, description, chip)

      return sampleSet.id
    }
    return 0
  }

  int duplicateSampleSetFromSamples( List<ArrayData> arrayDataList, SampleSet parentSampleSet, String name, String description, List options)
  {
    if (arrayDataList)
    {
      def chip = arrayDataList.get(0).chip
      def sampleSet = createSampleSet(arrayDataList, name, description, chip, parentSampleSet, options)
      return sampleSet.id
    }
    return 0
  }

  private String getPlatformFromChip( ChipsLoaded chip )
  {
    def chipData = chip.chipType.chipData
    def platformOption = "${chipData.manufacturer} ${chipData.model}"
    platformOption += chipData.chipVersion ? " v${chipData.chipVersion}" : ""
    return platformOption
  }

  private SampleSet createSampleSet( List arrayDataList, String name, String description, ChipsLoaded chip, SampleSet parentSampleSet = null, List options = null )
  {
    def allSamplesDatasetGroup = new DatasetGroup()
    allSamplesDatasetGroup.name = grailsApplication.config.sampleSets.allSamples.groupName
    allSamplesDatasetGroup.hexColor = grailsApplication.config.sampleSets.allSamples.defaultColor
    arrayDataList.each { sample ->
      def gd = new DatasetGroupDetail()
      gd.sample = sample
      allSamplesDatasetGroup.addToGroupDetails(gd)
    }

    def datasetGroupSet = new DatasetGroupSet()
    datasetGroupSet.name = grailsApplication.config.sampleSets.allSamples.groupName
    datasetGroupSet.addToGroups(allSamplesDatasetGroup)

    def sampleSet = new SampleSet()
    sampleSet.name = name
    sampleSet.description = description
    sampleSet.status = grailsApplication.config.status.default.text
    sampleSet.studyType = grailsApplication.config.studyType.default.text
    sampleSet.rawSignalType = RawSignalDataType.findByName("raw_signal")
    if (parentSampleSet) {
      sampleSet.parentSampleSet = parentSampleSet.parentSampleSet ?: parentSampleSet
    }
    sampleSet.addToGroupSets(datasetGroupSet)
    sampleSet.chipsLoaded = chip
    sampleSet.chipType = chip.chipType
    sampleSet.genomicDataSource = chip.genomicDataSource
    sampleSet.noSamples = chip.noSamples

    if (sampleSet.save( flush:true )) {
      sampleSet.defaultGroupSet = datasetGroupSet

      // copy over the clinical datasource
      if (parentSampleSet && options.contains("copyAnnotations"))
      {
        sampleSet.rawSignalType = parentSampleSet.rawSignalType
        sampleSet.defaultSignalDisplayType = parentSampleSet.defaultSignalDisplayType
        sampleSet.clinicalDataSource = parentSampleSet.clinicalDataSource
        sampleSet.clinicalDatasourceDataUrl = parentSampleSet.clinicalDatasourceDataUrl

        if (parentSampleSet.visibleSecRoles)
        {
          parentSampleSet.visibleSecRoles.each {
            sampleSet.addToVisibleSecRoles(it)
          }
        }
      }

      sampleSet.save( flush: true )

      // create a placeholder SampleSetAnnotation
      def sampleSetAnnotation = new SampleSetAnnotation()
      if (parentSampleSet && options.contains("copyAnnotations"))
      {
        sampleSetAnnotation.properties = parentSampleSet.sampleSetAnnotation.properties
      }
      sampleSetAnnotation.sampleSet = sampleSet
      sampleSetAnnotation.save( flush: true )

      // create a placeholder SampleSetSampleInfo
      def sampleSetSampleInfo = new SampleSetSampleInfo()
      if (parentSampleSet && options.contains("copyAnnotations"))
      {
        sampleSetSampleInfo.properties = parentSampleSet.sampleSetSampleInfo.properties
        sampleSetSampleInfo.sampleSources = new HashSet<LookupListDetail>(parentSampleSet.sampleSetSampleInfo.sampleSources)
      }
      if (parentSampleSet && options.contains("copyFiles"))
      {
        def spreadsheet = parentSampleSet.sampleSetSampleInfo.sampleSetSpreadsheet
        if (spreadsheet)
        {
          def parentFile = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${parentSampleSet.id}/${spreadsheet.filename}")
          def subsetFile = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSet.id}/${spreadsheet.filename}")
          FileUtils.copyFile(parentFile, subsetFile, true)
          sampleSetSampleInfo.sampleSetSpreadsheet = new SampleSetFile(sampleSet: sampleSet, filename: spreadsheet.filename, extension: spreadsheet.extension, tag: FileTag.findByTag("Sample Set Spreadsheet"), header: spreadsheet.header).save( flush: true )
        }
      }
      else
      {
        sampleSetSampleInfo.sampleSetSpreadsheet = null
      }
      sampleSetSampleInfo.sampleSet = sampleSet
      sampleSetSampleInfo.numberOfSamples = arrayDataList.size()
      sampleSetSampleInfo.save( flush: true )

      // want to always copy over chip file or do we need to create a new chip file?
//      String bgFile = chip.filename
//      def extension = bgFile.substring(bgFile.lastIndexOf(".")+1)
//      new SampleSetFile(sampleSet: sampleSet, description: "Expression Data File", filename: bgFile, extension: extension.toLowerCase(), tag: FileTag.findByTag("Chip Files")).save( flush: true )
      if (parentSampleSet && options.contains("copyFiles"))
      {
        parentSampleSet.sampleSetFiles.each {
          if (!it.tag || (it.tag.tag != "Chip Files" && it.tag.tag != "Sample Set Spreadsheet"))
          {
            def parentFile = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${parentSampleSet.id}/${it.filename}")
            def subsetFile = new File("${grailsApplication.config.fileUpload.baseDir}/ds/${sampleSet.id}/${it.filename}")
            FileUtils.copyFile(parentFile, subsetFile, true)
            new SampleSetFile(sampleSet: sampleSet, filename: it.filename, extension: it.extension, tag: it.tag, header: it.header).save( flush: true )
          }
        }
      }

      def sampleSetPlatformInfo = new SampleSetPlatformInfo()
      sampleSetPlatformInfo.sampleSet = sampleSet
      sampleSetPlatformInfo.platform = "Microarray"
      sampleSetPlatformInfo.platformOption = getPlatformFromChip(chip).trim()
      sampleSetPlatformInfo.save( flush: true )

      def sampleSetAdminInfo = new SampleSetAdminInfo()
      if (parentSampleSet && options.contains("copyAnnotations"))
      {
        sampleSetAdminInfo.properties = parentSampleSet.sampleSetAdminInfo.properties
      }
      sampleSetAdminInfo.sampleSet = sampleSet
      sampleSetAdminInfo.save( flush: true )

      // copy over default components for this sample set
      def ssDefComponentSet = SampleSetOverviewComponentDefaultSet.findByName(grailsApplication.config.sampleSet.overviewComponentsSet.default.name)
      if (ssDefComponentSet) {
        def defComponents = SampleSetOverviewComponentDefault.findAllByDefaultSetId(ssDefComponentSet.id)
        defComponents.each {
          def ssComponent = new SampleSetOverviewComponent(sampleSetId: sampleSet.id, componentId: it.componentId, displayOrder: it.displayOrder)
          ssComponent.save( flush: true )
        }
      }

	  // create a default ranklist for the newly created sample set.
	  // def rresult = generateDefaultRankList(sampleSet.id, null);
	  
	  // Copy values document from sampleSetId collection
      def collection = MongoConnector.getInstance().getCollection()

      arrayDataList.each { sample ->
        BasicDBObject row = new BasicDBObject("sampleSetId", sampleSet.id)
        row.put("sampleId", sample.id)
        row.put("sampleBarcode", sample.barcode)
        if (parentSampleSet && options.contains("copyAnnotations"))
        {
          BasicDBObject query = new BasicDBObject("sampleSetId", parentSampleSet.id)
          query.put("sampleId", sample.id)
          def values = collection.findOne(query)?.get("values")
          if (values)
          {
            row.put("values", values)
          }
        }
        collection.insert(row)
      }

	  // Copy spreadsheet document from sampleSet, and tabs document from sampleSetTabs collections
	  if (parentSampleSet && options.contains("copyAnnotations"))
	  {
		  // Copy over the spreadsheet information, used in tracking/tabs, overlay and correlation display.
		  def ssCollection = MongoConnector.getInstance().getCollection("sampleSet");
		  BasicDBObject ssrow = new BasicDBObject("sampleSetId", sampleSet.id)
		  BasicDBObject query = new BasicDBObject("sampleSetId", parentSampleSet.id)
		  def spreadsheet = ssCollection.findOne(query)?.get("spreadsheet")
		  if (spreadsheet)
		  {
			  	ssrow.put("spreadsheet", spreadsheet)
		  }
		  ssCollection.insert(ssrow)
		  
		  // Copy over tab information, used to create/populate geneBrowser tracking tabs.
		  def sstCollection = MongoConnector.getInstance().getCollection("sampleSetTabs");
		  BasicDBObject sstrow = new BasicDBObject("sampleSetId", sampleSet.id)
		  BasicDBObject tquery = new BasicDBObject("sampleSetId", parentSampleSet.id)
		  def tabs = sstCollection.findOne(tquery)?.get("tabs")
		  if (tabs)
		  {
				  sstrow.put("tabs", tabs)
		  }
		  sstCollection.insert(sstrow)
	  }

    }
    else
    {
        println( "sampleSet not saved:" );
        sampleSet.errors.each { err -> println( err ) }
    }

    return sampleSet
  }
  
    boolean exportSampleSetToFiles( SampleSet sampleSet, String directory,
                                    def dataSource,
                                    def matConfigService )
    {
        Sql sql = Sql.newInstance( dataSource );
        SampleSetArchive archive =
                new SampleSetArchive( this,
                                      sql,
                                      mongoDataService,
                                      grailsApplication.config,
                                      matConfigService );
        return archive.exportToFiles( sampleSet, directory );
    }

    boolean importSampleSetFromFiles( String directory,
                                      def dataSource,
                                      def matConfigService )
    {
        Sql sql = Sql.newInstance( dataSource );
        SampleSetArchive archive =
                new SampleSetArchive( this,
                                      sql,
                                      mongoDataService,
                                      grailsApplication.config,
                                      matConfigService );
        return archive.importFromFiles( directory );
    }

    boolean exportSampleSetToTarball( SampleSet sampleSet, String fileSpec,
                                      def dataSource,
                                      def matConfigService )
    {
        Sql sql = Sql.newInstance( dataSource );
        SampleSetArchive archive =
                new SampleSetArchive( this,
                                      sql,
                                      mongoDataService,
                                      grailsApplication.config,
                                      matConfigService );
        return archive.exportToTarball( sampleSet, fileSpec );
    }

    boolean importSampleSetFromTarball( String fileSpec,
                                        def dataSource,
                                        def matConfigService )
    {
        Sql sql = Sql.newInstance( dataSource );
        SampleSetArchive archive =
                new SampleSetArchive( this,
                                      sql,
                                      mongoDataService,
                                      grailsApplication.config,
                                      matConfigService );
        return archive.importFromTarball( fileSpec );
    }

    boolean archiveAndDeleteSampleSet( SampleSet sampleSet, String archiveSpec,
                                       def dataSource, def matConfigService )
    {
        Sql sql = Sql.newInstance( dataSource );
        SampleSetArchive archive =
                new SampleSetArchive( this,
                                      sql,
                                      mongoDataService,
                                      grailsApplication.config,
                                      matConfigService );
		if (archiveSpec) {				  	
			boolean exportRslt = archive.exportToTarball(sampleSet, archiveSpec)
			if ( exportRslt == false )
			{
					return false
			}
		}
        return archive.deleteAllData( sampleSet );
    }

	boolean deleteRankLists(SampleSet sampleSet, def type)
	{
		Sql sql = Sql.newInstance(dataSource)
		def query = "DELETE FROM rank_list_detail WHERE rank_list_id = ?".toString()
		def rankLists = null
		
		if (type == 'sampleSet') {
			rankLists = RankList.findAllBySampleSetId(sampleSet.id)
		} else if (type == 'user') {
			def rangeType = RankListType.findByAbbrev('range')
			rankLists = RankList.findAllBySampleSetIdAndRankListTypeNotEqual(sampleSet.id, rangeType)
		} else if (type == 'marked' && sampleSet == null) {
			rankLists = RankList.findAllByMarkedForDeleteIsNotNull()	
		}

		rankLists.each {
			println "deleting rank list: " + it.id + " description: " + it.description
			try {
				sql.execute(query, it.id)
			} catch (Error e) {
				println "unable to delete rank_list_detail for ranklist: " + it.id
			}
			it.delete()
		}
		return true;
	}

  def GenomicDataSource getDatasetType(long sampleSetId)
  {
      SampleSet sampleSet = SampleSet.get( sampleSetId );
      if ( sampleSet?.genomicDataSource )
      {
          return sampleSet.genomicDataSource;
      }

	  try
	  {
		  Sql sql = Sql.newInstance(dataSource)
		  def query = "SELECT a.chip_id FROM dataset_group_set s, dataset_group g, dataset_group_detail d, array_data a " +
		    "WHERE s.sample_set_id = ${sampleSetId} " +
		    "AND g.group_set_id = s.id " +
		    "AND d.group_id = g.id " +
		    "AND a.id = d.sample_id " +
		    "LIMIT 1"
		  def result = (GroovyRowResult)sql.firstRow(query)
		  def chipId = result ? result.get("chip_id") : null
		  if (chipId)
		  {
        return ChipsLoaded.get(chipId).genomicDataSource
		  }
      sql.close()
	  }
	  catch (Exception e)
	  {
		  println e.getMessage()
		  println e.getStackTrace()
	  }
    return null
  }

    ChipsLoaded getChipsLoaded( long sampleSetId )
    {
      SampleSet sampleSet = SampleSet.get( sampleSetId );
      if ( sampleSet?.chipsLoaded )
      {
          return sampleSet.chipsLoaded;
      }

        try
        {
            Sql sql = Sql.newInstance(dataSource)
            def query = "SELECT a.chip_id FROM dataset_group_set s, dataset_group g, dataset_group_detail d, array_data a " +
                    "WHERE s.sample_set_id = ${sampleSetId} " +
                    "AND g.group_set_id = s.id " +
                    "AND d.group_id = g.id " +
                    "AND a.id = d.sample_id " +
                    "LIMIT 1"
            def result = (GroovyRowResult)sql.firstRow(query)
            def chipId = result ? result.get("chip_id") : null
            if (chipId)
            {
                return ChipsLoaded.get(chipId);
            }
            sql.close()
        }
        catch (Exception e)
        {
            println e.getMessage()
            println e.getStackTrace()
        }
        return null
    }

  def ChipType getChipType(long sampleSetId)
  {
      ChipsLoaded chipsLoaded = getChipsLoaded( sampleSetId );
      return chipsLoaded?.chipType;
  }

  def int getNumberOfSamples(long groupSetId, Sql sql)
  {
//    Sql sql = Sql.newInstance( dataSource )
    def query = "SELECT COUNT(d.id) AS sampleCount FROM dataset_group_set AS s " +
      "JOIN dataset_group AS g ON g.group_set_id = s.id " +
      "JOIN dataset_group_detail AS d ON d.group_id = g.id " +
      "WHERE s.id = ${groupSetId}"
    GroovyRowResult result = sql.firstRow(query)
    def numSamples = result.get("sampleCount")
//    sql.close()
    return numSamples
  }

  def Map<String,Long> getBarcodeToArrayDataId( long sampleSetId,
												Sql sql = null )
  {
    def barcodeToArrayDataId = [:]
    sql = sql ?: Sql.newInstance(dataSource)
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

  def Map getGroupSetRankLists(long sampleSetId, long groupSetId)
  {
    Sql sql = Sql.newInstance(dataSource)
    def sampleSet = SampleSet.get(sampleSetId)
    def groupSets = groupSetId == -1 ? sampleSet.groupSets : [DatasetGroupSet.get(groupSetId)]
    def groupSetRankLists = [:]
    groupSets.each { groupSet ->
	  // Pick up the default/range ranklist too, it's identified by the ssid and the RankListType.
	  def gRankLists = RankList.findAllBySampleSetIdAndRankListTypeAndMarkedForDeleteIsNull(sampleSetId, RankListType.findByAbbrev("range")) 	
      gRankLists += RankList.findAllByGroupSetIdAndMarkedForDeleteIsNull(groupSet.id)
      if (gRankLists)
      {
        groupSetRankLists.put(groupSet.id, gRankLists)
      }
      else
      {
        def query = """SELECT rd.rank_list_id 'id' FROM rank_list rl
          JOIN rank_list_detail rd ON rl.id = rd.rank_list_id
          WHERE sample_set_id = ${sampleSet.id}
		  AND rl.marked_for_delete IS NULL
          AND rank = 1
          AND value IS NOT NULL""".toString()
        def rankListOrder = []
        sql.eachRow(query) {
          rankListOrder.push(it.id)
        }
        def ranklistDescriptions = []
        def rlDescQuery = """SELECT c.group_one_name 'g1', c.group_two_name 'g2' FROM rank_list_comparison c
        WHERE c.group_one_id IN (SELECT g.id FROM dataset_group g WHERE g.group_set_id = ${groupSet.id})
        OR c.group_two_id in (SELECT g.id FROM dataset_group g WHERE g.group_set_id = ${groupSet.id})""".toString()
        sql.eachRow(rlDescQuery) { r ->
          ranklistDescriptions.push("All ${r.g1} vs ${r.g2}".toString())
          ranklistDescriptions.push("PALO ${r.g1} vs ${r.g2}".toString())
        }
        def ranklists = []
        if (sampleSet.defaultRankList)
        {
          ranklists.push(sampleSet.defaultRankList)
        }
        if (!ranklistDescriptions.isEmpty())
        {
          def c = RankList.createCriteria()
          ranklists.addAll(c.listDistinct {
            eq("sampleSetId", (int)sampleSet.id)
            and {
              'in'("description",ranklistDescriptions)
            }
          })
        }
        ranklists.retainAll { rankListOrder.contains(it.id) }
        ranklists.sort { rankListOrder.indexOf(it.id) }
        groupSetRankLists.put(groupSet.id, ranklists)
      }
    }
    sql.close()
    return groupSetRankLists
  }

  void populateSampleSetSpreadsheet( long id,
                                     MongoDataService mongoDataSvce = null )
  {
    Map fields = [:]
    int order = 0
    if ( mongoDataService == null )
    {
        if ( mongoDataSvce != null )
        {
            mongoDataService = mongoDataSvce;
        }
        else
        {
            println( "SampleSetService: MongoDataService is null" );
            return;
        }
    }
    List fieldKeys = mongoDataService.getSampleSetFieldKeys(id, true)
    fieldKeys.each { Map fk ->
      String key = fk.label
      String displayName = fk.header
      List values = mongoDataService.getColumnValues(id, key)
      values.removeAll {
        if (it instanceof String) {
          return it.trim().isAllWhitespace()
        }
        return false
      }
      int isNumber = 0, isDate = 0, isString = 0
      values.each {
        if (it instanceof String) {
          if (!it.isNumber()) {
            isString++
          } else {
            isNumber++
          }
        } else if (it instanceof Date) {
          isDate++
        } else if (it instanceof Number) {
          isNumber++
        }
      }
      String datatype = "string"
      if (isString == 0 && !(isNumber > 0 && isDate > 0)) {
        if (isNumber > 0) {
          datatype = "number"
        } else if (isDate > 0) {
          datatype = "timepoint"
        }
      }
      if (values.size() > 0) {
        String mongoKey = key.substring(key.indexOf(".")+1)
        fields.put(mongoKey, [ order:order, displayName:displayName, numUnique:values.size(), datatype:datatype, ssat_visible:"show", overlay_visible:"show" ])
        order++
      }
    }
    if (!fields.isEmpty()) {
      mongoDataService.insert("sampleSet", [ sampleSetId:id ], [ spreadsheet:[ header:fields ] ])
    }
  }

//=============================================================================

  private
  void reportError( String message )
  {
    println( message );
  }

//-----------------------------------------------------------------------------

  List getArrayDataIdsForSampleSet( long sampleSetId, Sql sql )
  {
    try
    {
      String query = "SELECT id FROM `dataset_group_set`" +
        " WHERE `sample_set_id` = ?";
      def row = sql.firstRow( query, [ sampleSetId ] );
      if ( row == null )
      {
        reportError( "No dataset_group_set with sample_set_id " +
          sampleSetId );
        return [];
      }

      query = "SELECT dgd.sample_id AS sample_id" +
              " FROM `dataset_group_detail` AS dgd" +
              " JOIN `dataset_group` AS dg" +
              " ON dg.id = dgd.group_id" +
              " WHERE dg.group_set_id = ?" +
              " ORDER BY dgd.sample_id";
      def rows = sql.rows( query, [ row.id ] );
      if ( rows == [] )
      {
        reportError( "No dataset_group_details with group_set_id " +
          row.id );
        return [];
      }
      return rows*.sample_id;
    }
    catch ( SQLException exc )
    {
      reportError( "SQL error occurred:\n" + exc.message );
      return [];
    }
  }

//=============================================================================

  boolean quantileNormalizeSignals( long sampleSetId, Sql sql )
  {
    sql = sql ?: Sql.newInstance( dataSource );
    if ( sql == null )
    {
      log.error( "Unable to get Sql connection with data source" );
      return false
    }

    String normalizedFileSpec = "/tmp/Normalized" + sampleSetId;
    PrintWriter normalizedFile;
    try
    {
      normalizedFile = new PrintWriter( normalizedFileSpec );
    }
    catch ( FileNotFoundException exc )
    {
      reportError( "Unable to create " + normalizedFileSpec );
      return false;
    }

    def arrayDataIds = getArrayDataIdsForSampleSet( sampleSetId, sql );
    int maxProbes = 0;
    try
    {
      for ( int arrayDataId : arrayDataIds )
      {
        String query =
        "SELECT `affy_id`, `signal`, `detection`" +
          " FROM `array_data_detail`" +
          " WHERE `array_data_id` = ?" +
          " ORDER BY `signal`";
        def rows = sql.rows( query, [ arrayDataId ] );
        int numProbes = rows.size();
        if ( numProbes > maxProbes )
        maxProbes = numProbes;
        for ( int i = 0; i < numProbes; ++i )
        {
          writeNormalizedFileRow( normalizedFile,
            sampleSetId,
            arrayDataId,
            rows[ i ].affy_id,
            rows[ i ].signal,
            rows[ i ].detection,
            i );
        }
      }
      normalizedFile.flush( );
      normalizedFile.close( );

      String command =
      "LOAD DATA LOCAL INFILE '" + normalizedFileSpec + "'" +
        " INTO TABLE `array_data_detail_quantile_normalized`" +
        " ( `sample_set_id`, `array_data_id`, `affy_id`," +
        " `signal`, `detection`, `signal_rank` )";
      sql.execute( command );

      File normFile = new File( normalizedFileSpec );
      normFile.delete( );

      command =
        "CREATE TEMPORARY TABLE `tmp_signal_avg`" +
          "  ( `signal_rank` INT PRIMARY KEY," +
          "    `avg_signal` DOUBLE )";
      sql.execute( command );

      command =
        "INSERT INTO `tmp_signal_avg`" +
          "  ( `signal_rank`, `avg_signal` )" +
          "  SELECT `signal_rank`, AVG( `signal` )" +
          "    FROM `array_data_detail_quantile_normalized`" +
          "    WHERE `sample_set_id` = " + sampleSetId +
          "    GROUP BY `signal_rank`";
      sql.execute( command );

      command =
        "UPDATE `array_data_detail_quantile_normalized`" +
          "  SET `signal` = " +
          "    ( SELECT `avg_signal`" +
          "        FROM `tmp_signal_avg` AS t" +
          "        WHERE t.signal_rank = array_data_detail_quantile_normalized.signal_rank )" +
          "  WHERE `sample_set_id` = " + sampleSetId;
      sql.execute( command );

      command = "DROP TABLE `tmp_signal_avg`";
      sql.execute( command );
    }
    catch ( SQLException exc )
    {
      reportError( "SQL error occurred:\n" + exc.message );
      return false;
    }
    return true;
  }

//-----------------------------------------------------------------------------

  private
  boolean writeNormalizedFileRow( PrintWriter file,
                                  long sampleSetId, int arrayDataId,
                                  String probeId, Double signal,
                                  Double detection, int signalRank )
  {
    file.print( sampleSetId );
    file.print( "\t" );
    file.print( arrayDataId );
    file.print( "\t" );
    file.print( probeId );
    file.print( "\t" );
    file.print( (signal != null) ? signal : "\\N" );
    file.print( "\t" );
    file.print( (detection != null) ? detection : "\\N" );
    file.print( "\t" );
    file.print( signalRank );
    file.print( "\n" );
    return true;
  }

//=============================================================================

	boolean generateDefaultRankList( long sampleSetId,
									 Sql sql = null )
	{
		try
		{
			if (dataSource == null) {
				println("dataSource is null")
				log.error("dataSource is null")
			}
			
            sql = sql ?: Sql.newInstance( dataSource );
            if ( sql == null )
            {
                log.error( "Unable to get Sql connection with data source" );
                return false;
            }

			SampleSet sampleSet = SampleSet.get( sampleSetId );
			List arrayDataIds = getArrayDataIdsForSampleSet( sampleSetId, sql );

			// Test for fold change, and use array_data_detail as necessary.
			String signalDataTable = "array_data_detail_quantile_normalized"
			String rangeMath       = " (MAX(`signal`) - MIN(`signal`)) AS `range`,";
			def rawSignalType = sampleSet?.rawSignalType?.id
			// #6 is fold change, #7 is log2 fold change.
			if (rawSignalType == 6 || rawSignalType == 7)
			{
				//println "Fold Change Dataset default rank list creation: " + sampleSetId
				signalDataTable = "array_data_detail"
				if (rawSignalType == 6)
				{
					// convert signal to log2 for comparison of up and down regulated on an equal footing
					rangeMath = " (LOG2(MAX(`signal`)) - LOG2(MIN(`signal`))) AS `range`,";
				}
			}
			
			String query = "SELECT `affy_id` AS `probe_id`," +
					rangeMath +
					" MIN(`detection`) AS `min_detection`" +
					"  FROM " + signalDataTable +
					"  WHERE `array_data_id` IN (" +
					arrayDataIds.join( "," ) + ") " +
					"  GROUP BY `probe_id`" +
					"  ORDER BY `range` DESC";
			List rows = sql.rows( query );
			rows.removeAll { row -> row.min_detection > 0.1 }

			long rankListTypeId = RankListType.findByAbbrev( "range" ).id;
			String command =
					"INSERT INTO rank_list" +
                    "( `version`, `file_loaded_id`, `sample_set_id`," +
                    "`rank_list_type_id`, `description`, `num_probes` )" +
                    " VALUES ( ?, ?, ?, ?, ?, ? );";
            def values = [ 0, 0, sampleSetId,
                           rankListTypeId, "PALO All Samples", rows.size() ];
            def key = sql.executeInsert( command, values );
            if ( (key == null) || (key.size() < 1) || (key[0].size() < 1) )
            {
                reportError( "Unable to run SQL command:\n" + command +
                             "\n with values:\n" + values );
                return false;
            }
            long rankListId = key[0][0];
            RankList rankList = RankList.get( rankListId );

			String rankListFileSpec = "/tmp/RankList" + sampleSetId;
			PrintWriter rankListWriter;
			try
			{
				rankListWriter = new PrintWriter( rankListFileSpec );
			}
			catch ( FileNotFoundException exc )
			{
				reportError( "Unable to create " + rankListFileSpec );
				return false;
			}

			rows.eachWithIndex { row, index ->
                List rowData = [ 0, //version
                                 rankListId,
                                 row.probe_id,
                                 (index + 1),
                                 row.range,
                                 null //no p-value
                               ];
                Importer.writeBulkLoadLine( rankListWriter, rowData );
			}
			rankListWriter.flush( );
			rankListWriter.close( );

            Importer.loadDataToDb( rankListFileSpec, "rank_list_detail",
                                   [ "version",
                                     "rank_list_id",
                                     "probe_id",
                                     "rank",
                                     "value",
                                     "fdr_p_value"
                                   ],
                                   sql );

			File rankListFile = new File( rankListFileSpec );
			rankListFile.delete();

//            SampleSet sampleSet = SampleSet.get( sampleSetId );
            sampleSet.defaultRankList = rankList;
            sampleSet.defaultGroupSet.defaultRankList = rankList;
            sampleSet.save( flush: true );
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred:\n" + exc.message );
            return false;
        }
		return true;
	}

//=============================================================================

	//Returns the ID of the sampleSetPalx record (found or created);
	// 0 on failure.

	int calcPalx( long sampleSetId, Sql sql = null,
                  Double detectionPVal = null, Double fractionPresent = null )
	{
		SampleSet sampleSet = SampleSet.get( sampleSetId );
		detectionPVal = detectionPVal ?:
                grailsApplication.config.palx.defaultDetectionPVal;
		fractionPresent = fractionPresent ?:
                grailsApplication.config.palx.defaultFractionPresent;

		sql = sql ?: Sql.newInstance( dataSource );
		if ( sql == null )
		{
			log.error( "Unable to get Sql connection with data source" );
			return 0;
		}

		try
		{
            String query =
                    "SELECT id FROM sample_set_palx" +
                    "  WHERE sample_set_id = ?" +
                    "    AND detectionpval = ?" +
                    "    AND fraction_present = ?";
            def row = sql.firstRow( query, [ sampleSetId,
                                             detectionPVal, fractionPresent ] );
            if ( row )
            {
                return row.id;
            }
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred:\n" + exc.message );
            return 0;
        }

		SampleSetPalx sampleSetPalx =
				new SampleSetPalx( sampleSet: sampleSet,
								   detectionPVal: detectionPVal,
								   fractionPresent: fractionPresent );
		sampleSetPalx.save( flush: true );
        sampleSetPalx = SampleSetPalx.get( sampleSetPalx.id );

		List arrayDataIds = getArrayDataIdsForSampleSet( sampleSetId, sql );
		int minCount = Math.max( (int) Math.ceil( fractionPresent *
												  arrayDataIds.size() ),  1 );
		
		try
		{
			String command =
					"INSERT INTO sample_set_palx_detail" +
					"  ( sample_set_palx_id, probe_id )" +
					"  SELECT " + sampleSetPalx.id + ", probe_id" +
					"    FROM " +
					"     (SELECT affy_id AS probe_id, COUNT(*) AS cnt" +
					"        FROM array_data_detail" +
					"        WHERE (detection < " + detectionPVal +
					"            OR detection IS NULL)" +
					"          AND array_data_id IN" +
					"            (" + arrayDataIds.join( ", " ) + ")" +
					"        GROUP BY probe_id) AS add1" +
					"    WHERE cnt >= " + minCount;
			sql.execute( command );

            sampleSet.defaultPalx = sampleSetPalx;
            sampleSet.save( flush: true );
		}
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred:\n" + exc.message );
            return 0;
        }
		return sampleSetPalx.id;
	}

//=============================================================================

    boolean createTransformedSampleSet( long origSampleSetId,
                                        Map args,
										Sql sql = null )
    {
		try
		{
            sql = sql ?: Sql.newInstance( dataSource );
            if ( sql == null )
            {
                log.error( "Unable to get Sql connection" );
                return false;
            }

			String bulkLoadFileSpec = "/tmp/Transformed" + origSampleSetId;
			PrintWriter writer;
			try
			{
				writer = new PrintWriter( bulkLoadFileSpec );
			}
			catch ( FileNotFoundException exc )
			{
				reportError( "Unable to create " + bulkLoadFileSpec );
				return false;
			}

            List origArrayDataIds =
                    getArrayDataIdsForSampleSet( origSampleSetId, sql );
            List origArrayData = origArrayDataIds.collect { id ->
                ArrayData.get( id );
            }
            List newArrayDataIds = origArrayData.collect { origArrayDatum ->
                ArrayData arrayData =
                        new ArrayData( barcode: origArrayDatum.barcode,
                                       chip: origArrayDatum.chipId );
                arrayData.save( flush: true );
            }

            String query =
                    "SELECT affy_id, signal, detection" +
                    "  FROM array_data_detail" +
                    "  WHERE array_data_id = ?";

            Closure transform = { x -> x }
            if ( args.containsKey( "transform" ) )
            {
                switch ( args[ "transform" ] )
                {
                case "exp2":
                    transform = { x -> Math.pow( 2.0d, x ) }
                    break;
                case "log2":
                    double ln2 = Math.log( 2.0d );
                    transform = { x -> Math.log( x ) / ln2 }
                    break;
                }
            }
            for ( int i = 0; i < origArrayDataIds.size(); ++i )
            {
                int origArrayDataId = origArrayDataIds[ i ];
                int newArrayDataId = newArrayDataIds[ i ];
                List rows = sql.rows( query, [ origArrayDataId ] );
                rows.each { row ->
                    Double newSignal = transform( row.signal );
                    ArrayDataImporter.writeBulkLoadLine( writer,
                                                         newArrayDataId,
                                                         row.affy_id,
                                                         newSignal,
                                                         row.detection );
                }
            }
            writer.flush( );
            writer.close( );

            ArrayDataImporter.loadDataToDb( bulkLoadSpec, sql );

			File bulkLoadFile = new File( bulkLoadFileSpec );
			bulkLoadFile.delete();

            List newArrayData = newArrayDataIds.collect { id ->
                ArrayData.get( id );
            }

            SampleSet origSampleSet = SampleSet.get( origSampleSetId );
            String newName = origSampleSet.name + " (Transformed)"; //!!!
            String newDescription = origSampleSet.description;
            int newSampleSetId =
                    duplicateSampleSetFromSamples( newArrayData, origSampleSet,
                                                   newName, newDescription,
                                                   [ "copyAnnotations",
                                                     "copyFiles" ] );
            if ( newSampleSetId == 0 )
            {
                return false;
            }
            boolean rslt = quantileNormalizeSignals( newSampleSetId, sql );
            if ( rslt == false )
            {
                return false;
            }

            rslt = generateDefaultRankList( newSampleSetId, sql );
            if ( rslt == false )
            {
                return false;
            }
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred:\n" + exc.message );
            return false;
        }
		return true;
    }

//=============================================================================

    boolean execSysCommand( String command,
                            int verbosity = 1, Map results = null )
    {
        boolean success = false;
        try
        {
            if ( verbosity > 1 )
            {
                println( "Executing: " + command );
            }
            Process process = command.execute( );
            int returnValue = process.waitFor( );
            success = (returnValue == 0);
            String output = process.getText();
            String error = process.getErr().getText();
            if ( success )
            {
                if ( verbosity > 2 )
                {
                    println( output );
                }
            }
            else
            {
                if ( verbosity > 0 )
                {
                    println( error );
                }
                if ( verbosity > 1 )
                {
                    println( output );
                }
            }
            if ( results != null )
            {
                results.returnValue = returnValue;
                results.output = output;
                results.error = error;
            }
        }
        catch( IOException exc )
        {
            success = false;
            if ( verbosity > 0 )
            {
                println( "Exception: " + exc.message );
            }
            if ( results )
            {
                results.error = exc.message;
            }
        }
        return success;
    }

//.............................................................................

    boolean linkFile( String srcSpec, String destSpec )
    {
        if ( makeDirIfNeeded( destSpec ) == false )
            return false;
        File destFile = new File( destSpec );
        if ( destFile.exists() )
            destFile.delete();
        String command = "ln -s ${srcSpec} ${destSpec}";
        return execSysCommand( command );
    }

//-----------------------------------------------------------------------------

    boolean makeDirIfNeeded( String fileSpec )
    {
        File parentPath = (new File( fileSpec )).getParentFile();
        if ( parentPath.exists() )
            return true;
        return parentPath.mkdirs();
    }

//-----------------------------------------------------------------------------

    boolean generateOrLinkFile( String destSpec,
                                String cachedSpec,
                                Closure generateFile )
    {
        File cachedFile = new File( cachedSpec );
        if ( cachedFile.exists() == false )
        {
            boolean rslt = generateFile( cachedSpec );
            if ( rslt == false )
                return false;
        }
        return linkFile( cachedSpec, destSpec );
    }

//-----------------------------------------------------------------------------

	boolean generateArrayDataDetailTSV( long sampleSetId,
										String tsvFileSpec,
										boolean quantileNormalized = false,
										boolean useBarcodes = false,
										Sql sql = null )
	{
        Closure generateFile = { fileSpec ->
            String command;
            try
            {
                sql = sql ?: Sql.newInstance( dataSource );
                if ( sql == null )
                {
                    String msg =
                            "Unable to get Sql connection with data source";
                    log.error( msg );
                    return false;
                }

                String sourceTable = (quantileNormalized ?
                                      "array_data_detail_quantile_normalized" :
                                      "array_data_detail");
                def arrayDataIds =
                        getArrayDataIdsForSampleSet( sampleSetId, sql );
                if ( arrayDataIds.size() == 0 )
                {
                    String msg = "No array data IDs for sample " + sampleSetId;
                    reportError( msg );
                    return false;
                }

                def rows;

                command = "DROP TABLE IF EXISTS tmp_ads";
                sql.execute( command );

                command =
                        "CREATE TEMPORARY TABLE tmp_ads" +
                        "  ( id INTEGER PRIMARY KEY )";
                sql.execute( command );
                arrayDataIds.each { id ->
                    command = "INSERT INTO tmp_ads ( id ) VALUE( ${id} )";
                    sql.execute( command );
                }

                List barcodes;
                if ( useBarcodes )
                {
                    command =
                            "SELECT ad.barcode AS `barcode`" +
                            "  FROM array_data AS ad" +
                            "  INNER JOIN tmp_ads AS tads ON tads.id = ad.id" +
                            "  ORDER BY ad.id";
                    rows = sql.rows( command );
                    barcodes = rows*.barcode;
                    if ( barcodes.size() != arrayDataIds.size() )
                    {
                        reportError( "Number of barcodes (" + barcodes.size() +
                                     ") != number of samples (" +
                                     arrayDataIds.size() + ")" );
                        return false;
                    }
                }

                command = "SELECT DISTINCT det.affy_id AS `affy_id`" +
                        "  FROM " + sourceTable + " AS det" +
                        "  INNER JOIN tmp_ads AS tads" +
                        "    ON tads.id = det.array_data_id" +
                        "  ORDER BY `affy_id`";
                rows = sql.rows( command );
                def probeIds = rows*.affy_id;

                ChipType chipType = getChipType( sampleSetId );
                ChipData chipData = chipType.chipData;
                String manufacturer = chipData.manufacturer;
                String probePrefix =
                        (manufacturer == "Illumina") ? "ILMN_" : "";

                if ( makeDirIfNeeded( fileSpec ) == false )
                {
                    String msg = "Unable to make directory for " + fileSpec;
                    reportError( msg );
                    return false;
                }
                PrintWriter tsvFile = new PrintWriter( fileSpec );
		
                tsvFile.print( "ProbeID" );

                if ( useBarcodes )
                {
                    for ( String barcode : barcodes )
                    {
                        tsvFile.print( "\t" );
                        tsvFile.print( barcode + ".AVG_Signal" );
                        tsvFile.print( "\t" );
                        tsvFile.print( barcode + ".Detection Pval" );
                    }
                }
                else
                {
                    for ( long arrayDataId : arrayDataIds )
                    {
                        tsvFile.print( "\t" );
                        tsvFile.print( "X" + arrayDataId + ".AVG_Signal" );
                        tsvFile.print( "\t" );
                        tsvFile.print( "X" + arrayDataId + ".Detection Pval" );
                    }
                }

                for ( String probeId : probeIds )
                {
                    command = "SELECT det.array_data_id AS `array_data_id`," +
                            "    det.signal AS `signal`," +
                            "    det.detection AS `detection`" +
                            "  FROM " + sourceTable + " AS det" +
                            "  INNER JOIN tmp_ads AS tads" +
                            "    ON tads.id = det.array_data_id" +
                            "  WHERE det.affy_id = '" + probeId + "'" +
                            "  ORDER BY det.array_data_id";
                    rows = sql.rows( command );

                    tsvFile.print( "\n" );
                    tsvFile.print( probePrefix + probeId );

                    int j = 0;
                    for ( int i = 0; i < arrayDataIds.size(); ++i )
                    {
                        if ( rows[ j ].array_data_id == arrayDataIds[ i ] )
                        {
                            Double signal = rows[ j ].signal;
                            Double detection = rows[ j ].detection;
                            ++j;
                            tsvFile.print( "\t" );
                            tsvFile.print( (signal != null) ? signal : "" );
                            tsvFile.print( "\t" );
                            tsvFile.print( (detection != null) ? detection : "" );
                        }
                        else
                        {
                            tsvFile.print( "\t" );
                            tsvFile.print( "\t" );
                        }
                    }
                }

                command = "DROP TABLE tmp_ads";
                sql.execute( command );

                tsvFile.flush( );
                tsvFile.close( );

                return true;
            }
            catch ( SQLException exc )
            {
                String msg = "SQL error.\n" +
                        "Command: " + command + "\n" +
                        "Message: " + exc.message;
                reportError( msg );
                return false;
            }
        }

        String baseDir =
                grailsApplication?.config?.fileOutput?.baseDir ?: "/tmp/";
        String cachedSpec = baseDir + "expressionTsv/" + sampleSetId + "/" +
                "data" +
                (quantileNormalized ? "_qn" : "") +
                (useBarcodes ? "_bc" : "") +
                ".tsv";
        return generateOrLinkFile( tsvFileSpec, cachedSpec, generateFile );
	}

//-----------------------------------------------------------------------------

	boolean generateFocusedArrayExpressionCSV( SampleSet sampleSet,
											   String csvFileSpec,
											   Sql sql = null )
	{
        Closure generateFile = { fileSpec ->
            String command;
            try
            {
                sql = sql ?: Sql.newInstance( dataSource );
                if ( sql == null )
                {
                    String msg =
                            "Unable to get Sql connection with data source";
                    log.error( msg );
                    return false;
                }

                List< Long > sampleIds =
                        getArrayDataIdsForSampleSet( sampleSet.id, sql );
                if ( sampleIds.size() == 0 )
                {
                    String msg = "No array data IDs for sample " + sampleSet.id;
                    reportError( msg );
                    return false;
                }
				List< Long > excludedSampleIds = [];
				for ( Long id : sampleIds )
				{
					ArrayData sample = ArrayData.get( id );
					if ( (sample.sampleType != "NonRef") )
					{
						excludedSampleIds.add( id );
					}
				}
				sampleIds.removeAll( excludedSampleIds );

                command = "DROP TABLE IF EXISTS tmp_sampids";
                sql.execute( command );
                command =
                        "CREATE TEMPORARY TABLE tmp_sampids" +
                        "  ( id INTEGER PRIMARY KEY )";
                sql.execute( command );
                for ( Long id : sampleIds )
				{
					command = "INSERT INTO tmp_sampids ( id )" +
							" VALUE( ${id} )";
					sql.execute( command );
                }

                command =
						"SELECT DISTINCT fc.target" +
                        "  FROM focused_array_fold_changes AS fc" +
                        "  INNER JOIN tmp_sampids AS ids" +
                        "    ON ids.id = fc.array_data_id" +
                        "  ORDER BY fc.target";
                List< GroovyRowResult > rows = sql.rows( command );
                List< String > assayTargets = rows*.target;

				command =
						"SELECT target FROM focused_array_housekeeping" +
						"  WHERE chips_loaded_id = " + sampleSet.chipsLoaded.id;
                rows = sql.rows( command );
                List< String > housekeeping = rows*.target;
				if ( housekeeping.size() == 0 )
				{
					command =
							"SELECT target FROM focused_array_housekeeping" +
							"  WHERE chip_type_id = " + sampleSet.chipType.id;
					rows = sql.rows( command );
					housekeeping = rows*.target;
				}
				assayTargets.removeAll( housekeeping );

				Map< String, Long > targetAssayMap = [:];
                ChipType chipType = getChipType( sampleSet.id );
				command =
						"SELECT id, " +
						chipType.probeListColumn + " AS target" +
						"  FROM " + chipType.probeListTable +
						"  WHERE chip_type_id=" + chipType.id;
				rows = sql.rows( command );
				for ( GroovyRowResult row : rows )
				{
					targetAssayMap[ row.target ] = row.id;
				}

                if ( makeDirIfNeeded( fileSpec ) == false )
                {
                    String msg = "Unable to make directory for " + fileSpec;
                    reportError( msg );
                    return false;
                }
                PrintWriter csvFile = new PrintWriter( fileSpec );
				TextTableSeparator sep = TextTableSeparator.CSV;

				List< String > fields = [ "" ];
				for ( Long sampleId : sampleIds )
				{
					fields.add( "S" + sampleId );
				}
				csvFile.println( TextTable.joinRow( fields, sep, true ) );

				for ( String target : assayTargets )
				{
					command =
							"SELECT expr.array_data_id AS `sample_id`," +
							"    expr.fc AS `fc`" +
							"  FROM focused_array_fold_changes AS expr" +
							"  INNER JOIN tmp_sampids AS sids" +
							"    ON sids.id = expr.array_data_id" +
							"  WHERE expr.target='" + target + "'" +
							"  ORDER BY expr.array_data_id";
                    rows = sql.rows( command );
					fields = [ "A" + targetAssayMap[ (target) ] ];
					int j = 0;
					for ( Long sampleId : sampleIds )
					{
						if ( rows[ j ].sample_id == sampleId )
						{
							Float fc = rows[ j ].fc;
							++j;
							fields.add( fc );
						}
						else
						{
							fields.add( null );
						}
					}
					csvFile.println( TextTable.joinRow( fields, sep, true ) );
				}

                command = "DROP TABLE tmp_sampids";
                sql.execute( command );

                csvFile.flush( );
                csvFile.close( );

                return true;
            }
            catch ( SQLException exc )
            {
                String msg = "SQL error.\n" +
                        "Command: " + command + "\n" +
                        "Message: " + exc.message;
                reportError( msg );
                return false;
            }
        }

        String baseDir =
                grailsApplication?.config?.fileOutput?.baseDir ?: "/tmp/";
        String cachedSpec = baseDir + "expressionTsv/" + sampleSet.id + "/" +
                "data" + ".csv";
        return generateOrLinkFile( csvFileSpec, cachedSpec, generateFile );
	}

//=============================================================================

    Map getSampleSetStats( long sampleSetId, Sql sql = null )
    {
        try
        {
            sql = sql ?: Sql.newInstance( dataSource );
            if ( sql == null )
            {
                log.error( "Unable to get Sql connection with data source" );
                return false;
            }
            def arrayDataIds = getArrayDataIdsForSampleSet( sampleSetId, sql );
            if ( arrayDataIds.size() == 0 )
            {
                reportError( "No array data IDs for sample " + sampleSetId );
                return false;
            }
            String inADIds = " IN ( " + arrayDataIds.join( ", " ) + " )";

            String query =
                    "SELECT MIN(signal) AS min, MAX(signal) AS max" +
                    "  FROM array_data_detail" +
                    "  WHERE array_data_id IN " +
                    "  ( " + arrayDataIds.join( ", " ) + " )";
            def row = sql.firstRow( query );
            def stats =
                    [ min: row.min,
                      max: row.max
                    ];
            return stats;
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred:\n" + exc.message );
            return [:];
        }
    }

//=============================================================================

	def List getRankLists(SampleSet sampleSet)
	{
		def retList = []
		try
		{
			def szSql = "select id, description from rank_list where sample_set_id = " + sampleSet.id
			def sql = new Sql(dataSource)
			sql.eachRow(szSql) { row ->
				def item = [ "key": row.id, "value": row.description ]
				retList << item
			}
			if (retList.size() == 0)
			{
				retList << [ "key": -1, "value": "No Rank List Loaded"]
			}
			else
			{
				retList << [ "key": -1, "value": "Use Default"]
			}
		}
		catch (Exception e)
		{
			println e.getMessage()
			e.printStackTrace()
		}
		return retList
	}

//=============================================================================

	Map computeFocusedArrayFoldChanges( SampleSet sampleSet,
										Float palx = 0.1,
										Float floor = null,
										Sql sql = null,
										ImportService importService = null
									  )
	{
		sql = sql ?: new Sql( dataSource );
		importService = importService ?: new ImportService( );
		FocusedArraySampleSet fass =
				new FocusedArraySampleSet( this, sql,
										   grailsApplication.config,
										   importService );
		Map fcRslt = fass.computeFoldChanges( sampleSet, palx, floor );
		return fcRslt;
	}

//-----------------------------------------------------------------------------
}                                                            //SampleSetService
