/*
  ImportService.groovy

  Implementation details of ImportService
*/

package org.sagres.importer;

//import org.codehaus.groovy.grails.commons.GrailsApplication;
import groovy.util.ConfigObject;
import grails.plugin.mail.MailService;
import groovy.sql.Sql;
import java.sql.SQLException;
import org.sagres.sampleSet.SampleSetService;
import org.sagres.sampleSet.MongoDataService;
import common.chipInfo.ChipsLoaded;
import common.chipInfo.ChipData;
import common.chipInfo.ChipType;
import common.chipInfo.Technology;
import common.chipInfo.GenomicDataSource;
import common.chipInfo.FluidigmChipLoaded;
import common.ArrayData;
import org.sagres.sampleSet.SampleSet;
import org.sagres.FilesLoaded;
import org.sagres.rankList.RankList;
import org.sagres.rankList.RankListType;
//import org.codehaus.groovy.grails.web.mapping.LinkGenerator; //!!! 2.0


//*****************************************************************************


class ImportServiceImpl
{                                                           //ImportServiceImpl
//-----------------------------------------------------------------------------

    ImportServiceImpl( ImportService importService,
					   ConfigObject config,
                       SampleSetService sampleSetService )
    {
        assert( config );
        assert( sampleSetService );
		m_importService = importService;
        m_config = config;
        m_sampleSetService = sampleSetService;
    }

//-----------------------------------------------------------------------------

    void setSql( Sql sql )
    {
        m_sql = sql;
    }

//-----------------------------------------------------------------------------

    void setMongoDataService( MongoDataService mongoDataService )
    {
        m_mongoDataService = mongoDataService;
    }

//-----------------------------------------------------------------------------

    void setMailService( MailService mailService )
    {
        m_mailService = mailService;
    }

//=============================================================================

    Map importArrayDataTableFile( String fileSpec,
                                  Closure getChipType,
                                  GenomicDataSource genomicDataSource,
                                  Closure getTableInfo,
                                  ArrayDataTableFormatValidator formatValidator,
                                  ArrayDataTableValidator dataValidator,
                                  Closure getAnnotations,
                                  String emailRecipient = null,
                                  Closure fixupRowData = null )
    {
        Map result = [ success: false, message: "" ];
        ChipsLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< ArrayDataSample > samples = [];
        Long sampleSetId;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            if ( ArrayDataImporter.alreadyLoaded( specParts.name ) )
            {
                println( "Already imported" );
                return [ success: true,
                         message: "Already imported" ];
            }
            ChipType chipType = getChipType();
            logRecord = ArrayDataImporter.createLogRecord(
                specParts.name,
                [ genomicDataSource: genomicDataSource,
                  chipType: chipType,
                ] );

            ArrayDataTableInfo tableInfo = new ArrayDataTableInfo();
            getTableInfo( fileSpec, tableInfo, samples );
            formatValidator.validateTableInfo( tableInfo );

            assert( m_sql );
            Map< String, Boolean > chipProbes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipProbes, warnings );
            TextTable.process( fileSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );

            samples.each { sample ->
                ArrayData arrayData =
                        new ArrayData( barcode: sample.label,
                                       chip: logRecord );
                arrayData.save( flush: true );
                sample.id = arrayData.id;
            }

            ArrayDataTableBulkLoadBuilder builder =
                    new ArrayDataTableBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, tableInfo, samples,
                        fixupRowData );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            Integer numProbes = tableInfo.numProbes ?: tableInfo.numRows;

            samples.each { sample ->
                Double avgSignal;
                if ( numProbes > 0 )
                {
                    avgSignal = sample.signalTotal / numProbes;
                }
                ArrayData arrayData = ArrayData.get( sample.id );
                arrayData.averageSignal = avgSignal;
                arrayData.save( flush: true );
            }

            Importer.updateLogRecord( logRecord,
                                      ImportStatus.IMPORTING, 
                                      [ noSamples: samples.size(),
                                        noProbes: numProbes ]
                                    );

            ArrayDataImporter.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );

            sampleSetId =
                    ArrayDataImporter.createSampleSet( logRecord.id,
                                                       m_sampleSetService );
            result.sampleSet = SampleSet.get( sampleSetId );

            ArrayDataAnnotations annotations = getAnnotations( samples );
            annotations.saveToDb( result.sampleSet,
                                  m_mongoDataService, m_sampleSetService );

            ArrayDataImporter.quantileNormalize( sampleSetId,
                                                 m_sampleSetService, m_sql );
            ArrayDataImporter.generateDefaultRankList( sampleSetId,
                                                       m_sampleSetService,
                                                       m_sql );
            ArrayDataImporter.calcPalx( sampleSetId, m_sampleSetService, 
                                        m_sql, m_config );

            result.success = true;
            result.message = makeSampleSetMessage( sampleSetId, m_config );

            if ( warnings )
            {
                println( "Import completed with warnings" );
                result.status = ImportStatus.WARNING;
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                result.status = ImportStatus.COMPLETE;
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            result.status = ImportStatus.ERROR;
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                ArrayDataImporter.cleanup( result.success, auxSpecs,
                                           samples, sampleSetId, m_sql );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
		
		if (emailRecipient != "none") {
			try {
				Importer.sendEMail( m_mailService, m_config,
                            		[ fileSpec ], result.status, result.message,
									emailRecipient );
			} catch (Exception exc) {
				println "sendEMail failed: " + exc.message()
			}
		}
		
        return result;
    }

//-----------------------------------------------------------------------------

    Map importArrayDataMultitableFile( String fileSpec,
                                       Closure getChipType,
                                       GenomicDataSource genomicDataSource,
                                       Closure getTableInfo,
                                       ArrayDataMultitableValidator dataValidator,
                                       Closure getAnnotations,
                                       String emailRecipient = null,
                                       String sampleLabelMapSpec = null,
                                       Closure fixupDatum = null )
    {
        Map result = [ success: false, message: "" ];
        ChipsLoaded logRecord;
        AuxFileSpecs auxSpecs;
        Long sampleSetId;
        List< ArrayDataSample > samples;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            if ( ArrayDataImporter.alreadyLoaded( specParts.name ) )
            {
                println( "Already imported" );
                return [ success: true,
                         message: "Already imported" ];
            }
            ChipType chipType = getChipType();
            logRecord = ArrayDataImporter.createLogRecord(
                specParts.name,
                [ genomicDataSource: genomicDataSource,
                  chipType: chipType,
                ] );

            Map< String, String > sampleLabelMap;
            if ( sampleLabelMapSpec )
            {
                sampleLabelMap =
                        ArrayDataImporter.parseMatchedSamplesFile( 
                            sampleLabelMapSpec, TextTableSeparator.CSV );
            }

            ArrayDataMultitableInfo tableInfo = new ArrayDataMultitableInfo();
            getTableInfo( fileSpec, tableInfo, sampleLabelMap );

            assert( m_sql );
            Map< String, Boolean > chipProbes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipProbes, warnings );
            TextMultitable.process( fileSpec, tableInfo.sampleTables,
                                    dataValidator.&validateRow,
                                    dataValidator.&beforeTables,
                                    dataValidator.&afterTables );

            tableInfo.sampleTables.each { sampleTable ->
                ArrayData arrayData =
                        new ArrayData( barcode: sampleTable.sample.label,
                                       chip: logRecord );
                arrayData.save( flush: true );
                sampleTable.sample.id = arrayData.id;
            }

            ArrayDataMultitableBulkLoadBuilder builder =
                    new ArrayDataMultitableBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, fixupDatum );
            TextMultitable.process( fileSpec, tableInfo.sampleTables,
                                    builder.&processRow,
                                    builder.&beforeTables,
                                    builder.&afterTables );

            tableInfo.sampleTables.each { sampleTable ->
                double avgSignal;
                if ( sampleTable.numRows > 0 )
                {
                    avgSignal =
                           sampleTable.sample.signalTotal / sampleTable.numRows;
                }
                ArrayData arrayData = ArrayData.get( sampleTable.sample.id );
                arrayData.averageSignal = avgSignal;
                arrayData.save( flush: true );
            }

            int numSamples = tableInfo.sampleTables.size();
            int maxRows = tableInfo.sampleTables*.numRows.max();
            Importer.updateLogRecord( logRecord,
                                      ImportStatus.IMPORTING, 
                                      [ noSamples: numSamples,
                                        noProbes: maxRows ]
                                    );

            ArrayDataImporter.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );

            sampleSetId =
                    ArrayDataImporter.createSampleSet( logRecord.id,
                                                       m_sampleSetService );
            result.sampleSet = SampleSet.get( sampleSetId );
            samples = tableInfo.sampleTables*.sample;

            ArrayDataAnnotations annotations =
                    getAnnotations( samples, sampleLabelMap );
            annotations.saveToDb( result.sampleSet,
                                  m_mongoDataService, m_sampleSetService );

            ArrayDataImporter.quantileNormalize( sampleSetId,
                                                 m_sampleSetService, m_sql );
            ArrayDataImporter.generateDefaultRankList( sampleSetId,
                                                       m_sampleSetService,
                                                       m_sql );
            ArrayDataImporter.calcPalx( sampleSetId, m_sampleSetService, 
                                        m_sql, m_config );

            result.success = true;
            result.message = makeSampleSetMessage( sampleSetId, m_config );

            if ( warnings )
            {
                println( "Import completed with warnings" );
                result.status = ImportStatus.WARNING;
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ]);
            }
            else
            {
                println( "Import completed successfully" );
                result.status = ImportStatus.COMPLETE;
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            result.status = ImportStatus.ERROR;
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                ArrayDataImporter.cleanup( result.success, auxSpecs,
                                           samples, sampleSetId, m_sql );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
		
		if (emailRecipient != "none") {
			try {
				Importer.sendEMail( m_mailService, m_config,
                            		[ fileSpec ], result.status, result.message,
									emailRecipient );
			} catch (Exception exc) {
				println "sendEMail failed: " + exc.message()
			}
		}

        return result;
    }

//=============================================================================

    Map importRnaSeqGeneCountFiles( String rawCountSpec,
                                    String tmmNormalizedSpec,
                                    ChipType chipType,
                                    GenomicDataSource genomicDataSource,
                                    String emailRecipient = null )
    {
        Map result = [ success: false, message: "" ];
        ChipsLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< ArrayDataSample > samples = [];
        Long sampleSetId;
        List< String > warnings = [];

        Closure getTableInfo = RnaSeqTable.getTableInfo;
        ArrayDataTableValidator dataValidator = new ArrayDataTableValidator(
            requireKnownProbeId: false,
            isProbeIdListedFunc: RnaSeqImporter.isProbeIdListed );
        Closure fixupRowData = { }

        try
        {
            if ( new File( rawCountSpec ).exists() == false )
            {
                throw new ImportException( rawCountSpec + " does not exist" );
            }
            println( "Importing " + rawCountSpec + ". ChipType " + chipType );
            FileSpecParts specParts =
                    Importer.parseFileSpec( rawCountSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            if ( ArrayDataImporter.alreadyLoaded( specParts.name ) )
            {
                println( "Already imported" );
                return [ success: true,
                         message: "Already imported" ];
            }
            logRecord = ArrayDataImporter.createLogRecord(
                specParts.name,
                [ genomicDataSource: genomicDataSource,
                  chipType: chipType,
                ] );

            ArrayDataTableInfo tableInfo = new ArrayDataTableInfo();
            getTableInfo( rawCountSpec, tableInfo, samples );

            Map< String, Boolean > chipGenes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipGenes, warnings );
            TextTable.process( rawCountSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );

            samples.each { sample ->
                ArrayData arrayData =
                        new ArrayData( barcode: sample.label,
                                       chip: logRecord );
                arrayData.save( flush: true );
                sample.id = arrayData.id;
            }

            ArrayDataTableBulkLoadBuilder builder =
                    new ArrayDataTableBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, tableInfo, samples,
                        fixupRowData );
            TextTable.process( rawCountSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            Importer.updateLogRecord( logRecord,
                                      ImportStatus.IMPORTING, 
                                      [ noSamples: samples.size(),
                                        noProbes: tableInfo.numRows ]
                                    );

            ArrayDataImporter.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );

            sampleSetId =
                    ArrayDataImporter.createSampleSet( logRecord.id,
                                                       m_sampleSetService );
            SampleSet sampleSet = SampleSet.get( sampleSetId );
            result.sampleSet = sampleSet;

            result.success = true;
            result.message = makeSampleSetMessage( sampleSetId, m_config );

            if ( tmmNormalizedSpec )
            {
                Map rslt = importRnaSeqTmmNormalizedFile( tmmNormalizedSpec,
                                                          sampleSet,
                                                          chipType,
														  logRecord,
                                                          genomicDataSource );
                result.putAll( rslt );
            }

            if ( warnings )
            {
                println( "Import completed with warnings" );
                result.status = ImportStatus.WARNING;
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ]);
            }
            else
            {
                println( "Import completed successfully" );
                result.status = ImportStatus.COMPLETE;
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            result.status = ImportStatus.ERROR;
            println( Importer.combineMessages( exc.messages, 300, 4 ) );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                assert( logRecord );
                ArrayDataImporter.cleanup( result.success, auxSpecs,
                                           samples, sampleSetId, m_sql );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
		
		if (emailRecipient != "none") {
			try {
				Importer.sendEMail( m_mailService, m_config,
									[ rawCountSpec, tmmNormalizedSpec ],
									result.status, result.message,
									emailRecipient );
			} catch (Exception exc) {
				println "sendEMail failed: " + exc.message()
			}
		}

        return result;
    }

//-----------------------------------------------------------------------------

    Map importRnaSeqTmmNormalizedFile( String tmmNormalizedSpec,
                                       SampleSet sampleSet,
                                       ChipType chipType,
									   ChipsLoaded chipLoaded,
                                       GenomicDataSource genomicDataSource,
                                       String emailRecipient = null )
    {
        Map result = [ success: false, message: "" ];
        ChipsLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< ArrayDataSample > samples = [];
        List< String > warnings = [];

        Closure getTableInfo = RnaSeqTable.getTableInfo;
        ArrayDataTableValidator dataValidator = new ArrayDataTableValidator(
            requireKnownProbeId: false,
            isProbeIdListedFunc: RnaSeqImporter.isProbeIdListed );
        Closure fixupRowData = { }

        try
        {
            if ( new File( tmmNormalizedSpec ).exists() == false )
            {
                throw new ImportException( tmmNormalizedSpec +
                                           " does not exist" );
            }
            println( "Importing " + tmmNormalizedSpec +
                     ". ChipType " + chipType );
            FileSpecParts specParts =
                    Importer.parseFileSpec( tmmNormalizedSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            if ( ArrayDataImporter.alreadyLoaded( specParts.name ) )
            {
                println( "Already imported" );
                return [ success: true,
                         message: "Already imported" ];
            }
            logRecord = ArrayDataImporter.createLogRecord(
                specParts.name,
                [ genomicDataSource: genomicDataSource,
                  chipType: chipType,
                ] );

            ArrayDataTableInfo tableInfo = new ArrayDataTableInfo();
            getTableInfo( tmmNormalizedSpec, tableInfo, samples );

            Map< String, Boolean > chipGenes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipGenes, warnings );
            TextTable.process( tmmNormalizedSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );
            samples.each { sample ->
                ArrayData arrayData = ArrayData.findByBarcodeAndChip( sample.label, chipLoaded);
                assert( arrayData );
                sample.id = arrayData?.id;
            }

            RnaSeqTableTmmNormalizedBulkLoadBuilder builder =
                    new RnaSeqTableTmmNormalizedBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, sampleSet, tableInfo, samples );
            TextTable.process( tmmNormalizedSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            Importer.updateLogRecord( logRecord,
                                      ImportStatus.IMPORTING, 
                                      [ noSamples: samples.size(),
                                        noProbes: tableInfo.numRows ]
                                    );

            RnaSeqImporter.loadTmmNormalizedDataToDb( auxSpecs.bulkLoadSpec,
                                                      m_sql );

            result.success = true;
            result.message = makeSampleSetMessage( sampleSet.id, m_config );

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true,[ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                assert( logRecord );
                ArrayDataImporter.cleanup( result.success, auxSpecs,
                                           samples, sampleSet.id, m_sql );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//=============================================================================

    Map importFocusedArrayDataFile( List< String > fileSpecs,
									String housekeepingGenesFileSpec,
									String referenceSamplesFileSpec,
                                    ChipType chipType,
                                    GenomicDataSource genomicDataSource,
                                    Closure getTableInfo,
									Closure getRawDatum,
                                    String emailRecipient = null )
    {
        Map result = [ success: false, message: "" ];
        ChipsLoaded logRecord;
        def auxLogRecord;
		List< FileSpecParts > specPartsList = [];
        AuxFileSpecs auxSpecs;
        Long sampleSetId;
        List< ArrayDataSample > samples = [];
		HashMap< String, FocusedArraySampleInfo > sampleMap = [:];
        List< String > warnings = [];

        assert( m_sql );

        try
        {
			String variant = chipType.chipData.manufacturer;
            println( "Importing " + fileSpecs );
            for ( String fileSpec : fileSpecs )
            {
                FileSpecParts specParts =
                        Importer.parseFileSpec( fileSpec, "toBeImported" );
                specPartsList.add( specParts );
            }

			List< FocusedArrayDataTableInfo > tableInfoList = [];

			Map< String, Boolean > chipTargets;
			
            for ( int i = 0; i < fileSpecs.size(); ++i )
            {
                String fileSpec = fileSpecs[ i ];
                if ( new File( fileSpec ).exists() == false )
                {
                    throw new ImportException( fileSpec + " does not exist" );
                }
                FileSpecParts specParts = specPartsList[ i ];
                if ( i == 0 )
                {
                    if ( ArrayDataImporter.alreadyLoaded( specParts.name ) )
                    {
                        println( "Already imported" );
                        return [ success: true,
                                 message: "Already imported" ];
                    }
                    String additionalFiles =
                            specPartsList[ 1..-1 ]*.name.join( " : " );
                    logRecord = ArrayDataImporter.createLogRecord(
                        specParts.name,
                        [ genomicDataSource: genomicDataSource,
                          chipType: chipType,
                          additionalFiles: additionalFiles
                        ] );
					chipTargets =
							FocusedArrayData.getChipTargets( chipType, m_sql );

					auxSpecs = Importer.buildAuxFileSpecs( specParts );
                }

                FocusedArrayDataTableInfo tableInfo;
				if ( variant == "Fluidigm" )
				{
					tableInfo = new FluidigmArrayDataTableInfo();
				}
				getTableInfo( fileSpec, tableInfo ); //Also validates format
				tableInfoList.add( tableInfo );
                if ( i == 0 )
                {
					auxLogRecord = FocusedArrayData.createAuxLogRecord(
						logRecord, variant, tableInfo.headerInfo, m_sql );
				}
            }

			FocusedArrayData.validateSampleInfoListConsistency(
				tableInfoList*.samples, warnings );
			FocusedArrayData.validateAssayInfoListConsistency(
				tableInfoList*.assays, warnings );
			FocusedArrayData.validateAssaysAgainstDesignTargets(
				tableInfoList*.assays, chipTargets, warnings );

            for ( int i = 0; i < fileSpecs.size(); ++i )
            {
                String fileSpec = fileSpecs[ i ];
				FocusedArrayDataTableInfo tableInfo = tableInfoList[ i ];

				FocusedArrayDataTableValidator dataValidator =
						new FocusedArrayDataTableValidator( );
				dataValidator.init( tableInfo, getRawDatum, warnings );
				TextTable.process( fileSpec, tableInfo,
								   dataValidator.&validateRow,
								   dataValidator.&beforeTable,
								   dataValidator.&afterTable );
			}

            for ( int i = 0; i < fileSpecs.size(); ++i )
            {
                String fileSpec = fileSpecs[ i ];
				FocusedArrayDataTableInfo tableInfo = tableInfoList[ i ];

				for ( FocusedArraySampleInfo sampleInfo : tableInfo.samples )
				{
					String barcode = "S" + sampleInfo.plate +
							"_S" + sampleInfo.well;
					if ( sampleMap.containsKey( barcode ) )
					{
						sampleInfo.id = sampleMap[ (barcode) ].id;
					}
					else
					{
						ArrayData arrayData =
								new ArrayData( barcode: barcode,
											   chip: logRecord,
											   sampleName: sampleInfo.name,
											   origSampleType: sampleInfo.type,
											   sampleConcentration: sampleInfo.concentration );
						arrayData.save( flush: true );
						samples.add( arrayData );
						sampleInfo.id = arrayData.id;
						sampleMap[ barcode ] = sampleInfo;
					}
				}
			}

			FocusedArrayMultitableBulkLoadBuilder builder =
					new FocusedArrayMultitableBulkLoadBuilder(
						auxSpecs.bulkLoadSpec, getRawDatum );
			TextMultitable.process( fileSpecs, tableInfoList,
									builder.&processRow,
									builder.&beforeTables, builder.&afterTables );

            Integer numProbes = chipTargets.size();
            Importer.updateLogRecord( logRecord,
                                      ImportStatus.IMPORTING, 
                                      [ noSamples: sampleMap.size(),
                                        noProbes: numProbes ]
                                    );

            FocusedArrayData.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );

			if ( housekeepingGenesFileSpec )
			{
				FocusedArrayData.loadHousekeepingGenesFile(
					housekeepingGenesFileSpec, logRecord, m_sql );
			}
			if ( referenceSamplesFileSpec )
			{
				FocusedArrayData.loadReferenceSamplesFile(
					referenceSamplesFileSpec, logRecord, m_sql );
			}
			else
			{
				FocusedArrayData.setReferenceSamples( logRecord, m_sql );
			}

            sampleSetId =
                    ArrayDataImporter.createSampleSet( logRecord.id,
                                                       m_sampleSetService );
			SampleSet sampleSet = SampleSet.get( sampleSetId );
            result.sampleSet = sampleSet;

			Float defaultPalx = 0.1;
			Float defaultFloor = null;
			Map fcRslt =
					m_sampleSetService.computeFocusedArrayFoldChanges(
						sampleSet, defaultPalx, defaultFloor,
						m_sql, m_importService );
			if ( fcRslt.success == false )
			{
				throw new ImportException( fcRslt.message );
			}

            result.success = true;
            result.message = makeSampleSetMessage( sampleSetId, m_config );

            if ( warnings )
            {
                println( "Import completed with warnings" );
                result.status = ImportStatus.WARNING;
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                result.status = ImportStatus.COMPLETE;
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            result.status = ImportStatus.ERROR;
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
				if ( housekeepingGenesFileSpec )
				{
					specPartsList.add( Importer.parseFileSpec(
										   housekeepingGenesFileSpec,
										   "toBeImported" ) );
				}
				if ( referenceSamplesFileSpec )
				{
					specPartsList.add( Importer.parseFileSpec(
										   referenceSamplesFileSpec,
										   "toBeImported" ) );
				}
                FocusedArrayData.cleanup( result.success,
										  auxSpecs, specPartsList[ 1..-1 ],
										  samples, sampleSetId, m_sql );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
		
		if (emailRecipient != "none") {
			try {
				Importer.sendEMail( m_mailService, m_config,
                            		fileSpecs, result.status, result.message,
									emailRecipient );
			} catch (Exception exc) {
				println "sendEMail failed: " + exc.message()
			}
		}

        return result;
    }

//=============================================================================


    private
    String makeSampleSetMessage( long sampleSetId, ConfigObject config )
    {
        String message =
                "The new sample set may be viewed and annotated at: ";
        message += config.grails.serverURL + "/sampleSet/show/" + sampleSetId;
        /*!!!2.0
        LinkGenerator linkGenerator = new LinkGenerator( );
        message += linkGenerator.link( absolute: true,
                                       controller: "sampleSet",
                                       action: "show",
                                       id: sampleSetId );
        */
        return message;
    }

//=============================================================================

    Map validateAndExportArrayDataTableFile( String inFileSpec,
                                             String outDataFileSpec,
                                             String outSamplesFileSpec,
                                             Closure getChipType,
                                             Closure getTableInfo,
                                             ArrayDataTableFormatValidator formatValidator,
                                             ArrayDataTableValidator dataValidator,
                                             Closure getAnnotations )
    {
        Map result = [ success: false, message: "" ];
        List< ArrayDataSample > samples = [];
        List< String > warnings = [];

        try
        {
            if ( new File( inFileSpec ).exists() == false )
            {
                throw new ImportException( inFileSpec + " does not exist" );
            }
            println( "Processing " + inFileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( inFileSpec, "toBeImported" );
            ChipType chipType = getChipType();
					  result.chipType = chipType

            ArrayDataTableInfo tableInfo = new ArrayDataTableInfo();
            getTableInfo( inFileSpec, tableInfo, samples );
            formatValidator.validateTableInfo( tableInfo );

            assert( m_sql );
            Map< String, Boolean > chipProbes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipProbes, warnings );
            TextTable.process( inFileSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );

            ArrayDataTableExporter exporter =
                    new ArrayDataTableExporter( outDataFileSpec, samples,
                                                tableInfo, chipType );
            TextTable.process( inFileSpec, tableInfo,
                               exporter.&processRow,
                               exporter.&beforeTable, exporter.&afterTable );

            ArrayDataAnnotations annotations = getAnnotations( samples );
            annotations.exportSamplesTable( outSamplesFileSpec );

            result.success = true;
            result.message = "OK";
            result.numSamples = samples.size();
            result.numProbes = tableInfo.numRows;

            if ( warnings )
            {
                println( "Processing completed with warnings" );
                result.status = ImportStatus.WARNING;
                result.messages = warnings;
            }
            else
            {
                println( "Processing completed successfully" );
                result.status = ImportStatus.COMPLETE;
            }
        }
        catch ( ImportException exc )
        {
            println( "Processing failed" );
            result.status = ImportStatus.ERROR;
            println( Importer.combineMessages( exc.messages, 300, 4 ) );
            result.messages = exc.messages;
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Processing threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            result.message = exc.message;
        }
        return result;
    }

//-----------------------------------------------------------------------------

    Map validateAndExportArrayDataMultitableFile( String inFileSpec,
                                                  String outDataFileSpec,
                                                  String outSamplesFileSpec,
                                                  Closure getChipType,
                                                  Closure getTableInfo,
                                                  ArrayDataMultitableValidator dataValidator,
                                                  Closure getAnnotations,
                                                  Closure fixupDatum )
    {
        Map result = [ success: false, message: "" ];
        List< ArrayDataSample > samples = [];
        List< String > warnings = [];

        try
        {
            if ( new File( inFileSpec ).exists() == false )
            {
                throw new ImportException( inFileSpec + " does not exist" );
            }
            println( "Processing " + inFileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( inFileSpec, "toBeImported" );
            ChipType chipType = getChipType();

            ArrayDataMultitableInfo tableInfo = new ArrayDataMultitableInfo();
            getTableInfo( inFileSpec, tableInfo );

            assert( m_sql );
            Map< String, Boolean > chipProbes =
                    ArrayDataImporter.getChipProbes( chipType, m_sql );

            dataValidator.init( tableInfo, chipProbes, warnings );
            TextMultitable.process( inFileSpec, tableInfo.sampleTables,
                                    dataValidator.&validateRow,
                                    dataValidator.&beforeTables,
                                    dataValidator.&afterTables );

            ArrayDataMultitableExporter exporter =
                    new ArrayDataMultitableExporter( outDataFileSpec,
                                                     tableInfo, chipType );
            TextMultitable.process( inFileSpec, tableInfo.sampleTables,
                                    exporter.&processRow,
                                    exporter.&beforeTables, 
                                    exporter.&afterTables );

            samples = tableInfo.sampleTables*.sample;
            ArrayDataAnnotations annotations = getAnnotations( samples );
            annotations.exportSamplesTable( outSamplesFileSpec );

            result.success = true;
            result.message = "OK";
            result.numSamples = samples.size();
            result.numProbes = tableInfo.sampleTables*.numRows.max();

            if ( warnings )
            {
                println( "Processing completed with warnings" );
                result.status = ImportStatus.WARNING;
                result.messages = warnings;
            }
            else
            {
                println( "Processing completed successfully" );
                result.status = ImportStatus.COMPLETE;
            }
        }
        catch ( ImportException exc )
        {
            println( "Processing failed" );
            result.status = ImportStatus.ERROR;
            println( Importer.combineMessages( exc.messages, 300, 4 ) );
            result.messages = exc.messages;
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Processing threw an exception" );
            result.status = ImportStatus.ERROR;
            println( exc.message );
            exc.printStackTrace( );
            result.message = exc.message;
        }
        return result;
    }
    
//=============================================================================

    Map importAnnotationTableFile( String fileSpec,
                                   ChipType chipType,
                                   Closure getTableInfo,
                                   TextTableSeparator separator
                                       = TextTableSeparator.TAB )
    {
        Map result = [ success: false, message: "" ];
        FilesLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            logRecord = Importer.createFilesLoadedRecord( specParts.name );

            AnnotationTableInfo tableInfo = new AnnotationTableInfo();
            tableInfo.separator = separator;
            getTableInfo( fileSpec, tableInfo, chipType );

            AnnotationTableBulkLoadBuilder builder =
                    new AnnotationTableBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, tableInfo );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            AnnotationImporter.createAnnotationDbTable( chipType.probeListTable,
                                                        tableInfo.columns,
                                                        m_sql );

            Importer.updateLogRecord( logRecord, ImportStatus.IMPORTING );
            Importer.loadDataToDb( auxSpecs.bulkLoadSpec,
                                   chipType.probeListTable,
                                   tableInfo.columns*.name,
                                   m_sql );
            
            result.success = true;
            result.message = "OK";

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                Importer.cleanup( result.success, auxSpecs );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//-----------------------------------------------------------------------------

    Map importFocusedArrayDesignFile( String fileSpec,
									  String housekeepingGenesFileSpec,
									  String referenceSamplesFileSpec,
                                      String chipTypeName,
                                      ChipData chipData,
                                      Map chipTypeInfo,
                                      Closure getTableInfo,
                                      List< String > dbTableColumns )
    {
        Map result = [ success: false, message: "" ];
        FilesLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            logRecord = Importer.createFilesLoadedRecord( specParts.name );

            Technology technology = Technology.findByName( 'Focused Array' );
            ChipType chipType =
                    new ChipType( name: chipTypeName,
                                  chipData: chipData,
                                  technology: technology,
                                  importDirectoryName: "focused_array",
                                  probeListTable: chipTypeInfo.probeListTable,
                                  probeListColumn: chipTypeInfo.probeListColumn,
                                  symbolColumn: chipTypeInfo.symbolColumn,
                                  refSeqColumn: chipTypeInfo.refSeqColumn,
                                  sequenceColumn: chipTypeInfo.sequenceColumn,
                                  accessionNumberColumn: chipTypeInfo.accessionNumberColumn,
                                  synonymColumn: chipTypeInfo.synonymColumn,
                                  entrezGeneColumn: chipTypeInfo.entrezGeneColumn
                                );
            chipType.save( flush: true );

            FocusedArrayDesignTableInfo tableInfo =
                    new FocusedArrayDesignTableInfo();
            getTableInfo( fileSpec, tableInfo, dbTableColumns );

            FocusedArrayDesignBulkLoadBuilder builder =
                    new FocusedArrayDesignBulkLoadBuilder(
                        chipType.id, auxSpecs.bulkLoadSpec, tableInfo );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            List< String > colNames = [ "chip_type_id" ];
            colNames.addAll( tableInfo.columns*.name );
            Importer.updateLogRecord( logRecord, ImportStatus.IMPORTING );
            Importer.loadDataToDb( auxSpecs.bulkLoadSpec,
                                   chipTypeInfo.probeListTable,
                                   colNames,
                                   m_sql );

			FocusedArrayData.loadHousekeepingGenesFile(
				housekeepingGenesFileSpec, chipType, m_sql );
			FocusedArrayData.loadReferenceSamplesFile(
				referenceSamplesFileSpec, chipType, m_sql );

            result.success = true;
            result.message = "OK";

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
				List< FileSpecParts > specPartsList = [];
				specPartsList.add( Importer.parseFileSpec(
									   housekeepingGenesFileSpec,
									   "toBeImported" ) );
				specPartsList.add( Importer.parseFileSpec(
									   referenceSamplesFileSpec,
									   "toBeImported" ) );
                Importer.cleanup( result.success, auxSpecs, specPartsList );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//=============================================================================

    Map importStdRankList( String fileSpec,
                           Long sampleSetId = null,
                           Long groupSetId = null )
    {
        Map result = [ success: false, message: "" ];
        FilesLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            logRecord = Importer.createFilesLoadedRecord( specParts.name );
            RankListInfo rankListInfo = 
                    new RankListInfo( sampleSetId: sampleSetId,
                                      groupSetId: groupSetId );
            StdRankListFile.getRankListInfoFromFileName( specParts.name,
                                                         rankListInfo );
            RankListTableInfo tableInfo = new RankListTableInfo();
            StdRankListFile.getTableInfo( fileSpec, rankListInfo, tableInfo );
            RankListTableValidator dataValidator = new RankListTableValidator();
            dataValidator.init( tableInfo, warnings );
            TextTable.process( fileSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );
            RankList rankList =
                    RankListImporter.createRankList( logRecord, rankListInfo,
                                                     tableInfo.numRows );
            result.rankList = rankList;
            RankListTableBulkLoadBuilder builder =
                    new RankListTableBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, tableInfo, rankList );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );
            Importer.updateLogRecord( logRecord, ImportStatus.IMPORTING );
            RankListImporter.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );
            
            result.success = true;
            result.message = "OK";

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                Importer.cleanup( result.success, auxSpecs );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//-----------------------------------------------------------------------------

    Map importGenericRankList( String fileSpec,
                               Long sampleSetId, Long groupSetId,
                               String description,
                               TextTableSeparator separator,
                               int firstRow,
                               int probeIdColumn, int valueColumn,
                               boolean descending = true )
    {
        Map result = [ success: false, message: "" ];
        FilesLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< String > warnings = [];

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            logRecord = Importer.createFilesLoadedRecord( specParts.name );
            RankListType rankListType = RankListType.findByAbbrev( "other" );
            RankListInfo rankListInfo =
                    new RankListInfo( sampleSetId: sampleSetId,
                                      groupSetId: groupSetId,
                                      rankListType: rankListType,
                                      description: description );
            RankListTableInfo tableInfo =
                    new RankListTableInfo( separator: separator,
                                           firstRow: firstRow,
                                           probeIdColumn: probeIdColumn,
                                           valueColumn: valueColumn );
            GenericRankListFile.getTableInfo( fileSpec, tableInfo );
            RankListTableValidator dataValidator =
                    new RankListTableValidator( requireRank: false,
                                                requireValue: true );
            dataValidator.init( tableInfo, warnings );
            TextTable.process( fileSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );
            RankList rankList =
                    RankListImporter.createRankList( logRecord, rankListInfo,
                                                     tableInfo.numRows );
            result.rankList = rankList;
            GenericRankListBulkLoadBuilder builder =
                    new GenericRankListBulkLoadBuilder(
                        auxSpecs.bulkLoadSpec, tableInfo, rankList,
                        descending );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );
            Importer.updateLogRecord( logRecord, ImportStatus.IMPORTING );
            RankListImporter.loadDataToDb( auxSpecs.bulkLoadSpec, m_sql );
            
            result.success = true;
            result.message = "OK";

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true,[ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                Importer.cleanup( result.success, auxSpecs );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//=============================================================================

    Map importNcbiGeneFile( String fileSpec )
    {
        Map result = [ success: false, message: "" ];
        FilesLoaded logRecord;
        AuxFileSpecs auxSpecs;
        List< String > warnings = [];
        NcbiGeneDataValidator dataValidator = new NcbiGeneDataValidator();

        try
        {
            if ( new File( fileSpec ).exists() == false )
            {
                throw new ImportException( fileSpec + " does not exist" );
            }
            println( "Importing " + fileSpec );
            FileSpecParts specParts =
                    Importer.parseFileSpec( fileSpec, "toBeImported" );
            auxSpecs = Importer.buildAuxFileSpecs( specParts );
            logRecord = Importer.createFilesLoadedRecord( specParts.name );

            NcbiGeneDataTableInfo tableInfo =
                    NcbiGeneDataImporter.getTableInfo( specParts.name,
                                                       fileSpec );

            dataValidator.init( tableInfo, warnings );
            TextTable.process( fileSpec, tableInfo,
                               dataValidator.&validateRow,
                               dataValidator.&beforeTable,
                               dataValidator.&afterTable );

            NcbiGeneDataBulkLoadBuilder builder =
                    new NcbiGeneDataBulkLoadBuilder( auxSpecs.bulkLoadSpec,
                                                     tableInfo );
            TextTable.process( fileSpec, tableInfo,
                               builder.&processRow,
                               builder.&beforeTable, builder.&afterTable );

            Importer.updateLogRecord( logRecord, ImportStatus.IMPORTING );
            NcbiGeneDataImporter.loadDataToDb( auxSpecs.bulkLoadSpec,
                                               tableInfo, m_sql );
            
            result.success = true;
            result.message = "OK";

            if ( warnings )
            {
                println( "Import completed with warnings" );
                Importer.reportErrors( warnings, logRecord,
                                       auxSpecs.errorLogSpec, false, [ dateEnded: new Date() ] );
            }
            else
            {
                println( "Import completed successfully" );
                Importer.updateLogRecord( logRecord,
                                          ImportStatus.COMPLETE,
                                          [ dateEnded: new Date() ] );
            }
        }
        catch ( ImportException exc )
        {
            println( "Import failed" );
            Importer.reportErrors( exc.messages, logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            println( "Import threw an exception" );
            println( exc.message );
            exc.printStackTrace( );
            Importer.reportErrors( [ exc.message ], logRecord,
                                   auxSpecs?.errorLogSpec, true, [ dateEnded: new Date() ] );
            result.message = exc.message;
        }
        finally
        {
            try
            {
                Importer.cleanup( result.success, auxSpecs );
            }
            catch ( Exception exc )
            {
                println( "Import cleanup threw an exception" );
                println( exc.message );
                exc.printStackTrace( );
            }
        }
        return result;
    }

//=============================================================================

	private ImportService m_importService;
    private ConfigObject m_config;
    private MailService m_mailService;
    private SampleSetService m_sampleSetService;
    private Sql m_sql;
    private MongoDataService m_mongoDataService;

//-----------------------------------------------------------------------------
}                                                           //ImportServiceImpl


//*****************************************************************************
