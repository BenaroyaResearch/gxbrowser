/*
  FocusedArrayData.groovy

  Routines for importing focused-array expression data
*/

package org.sagres.importer;

import groovy.sql.Sql;
import java.sql.SQLException;
import groovy.sql.GroovyRowResult;
import common.chipInfo.ChipType;
import common.chipInfo.ChipsLoaded;
import common.chipInfo.FluidigmChipLoaded;
import org.sagres.sampleSet.SampleSet;
import org.sagres.sampleSet.SampleSetService;
import org.sagres.sampleSet.FocusedArraySampleSet;


//*****************************************************************************


class FocusedArraySampleInfo
{                                                      //FocusedArraySampleInfo
//-----------------------------------------------------------------------------

	Long id;
    String plate;
    String well;
    String name;
    String type;
    Float concentration;

//-----------------------------------------------------------------------------
}                                                      //FocusedArraySampleInfo


//*****************************************************************************


class FocusedArrayAssayInfo
{                                                       //FocusedArrayAssayInfo
//-----------------------------------------------------------------------------

    String plate;
    String well;
    String target;

//-----------------------------------------------------------------------------
}                                                       //FocusedArrayAssayInfo


//*****************************************************************************


class FocusedArraySampleAssayDatum
{                                                //FocusedArraySampleAssayDatum
//-----------------------------------------------------------------------------

	FocusedArraySampleInfo sampleInfo;
	FocusedArrayAssayInfo assayInfo;
	Float ctValue;
	Float ctQuality;
	String ctCall;
	String ctThreshold;
	Float tmInRange;
	Float tmOutRange;
	Float tmPeakRatio;
	
//-----------------------------------------------------------------------------
}                                                //FocusedArraySampleAssayDatum


//*****************************************************************************


class FoldChangeTableInfo
extends TextTableInfo
{                                                         //FoldChangeTableInfo
//-----------------------------------------------------------------------------

	String[] sampleCodes;

//-----------------------------------------------------------------------------
}                                                         //FoldChangeTableInfo


//*****************************************************************************


class FocusedArrayData
{                                                            //FocusedArrayData
//-----------------------------------------------------------------------------

    static
    Map< String, Boolean > getChipTargets( ChipType chipType, Sql sql )
    {
        assert( sql );
        Map< String, Boolean > chipTargets = new HashMap< String, Boolean >();
        String query;
        try
        {
            query = "SELECT " + chipType.probeListColumn +
                    " FROM " + chipType.probeListTable +
                    " WHERE ((chip_type_id = " + chipType.id + ")" +
					" AND (" + chipType.probeListColumn + " IS NOT NULL)" +
                    " AND (" + chipType.probeListColumn + " <> ''))";
            sql.eachRow( query ) { row ->
                chipTargets[ row[ 0 ] ] = false;
            }
            if ( chipTargets.size() == 0 )
            {
                String msg = "No targets listed in column " +
                        chipType.probeListColumn +
                        " of table " + chipType.probeListTable;
                throw new ImportException( msg );
            }
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Query: " + query + "\n" +
                    "Message: " + exc.message
            throw new ImportException( msg );
        }
        return chipTargets;
    }

//=============================================================================

	static
	def createAuxLogRecord( ChipsLoaded logRecord,
							String variant, Map data,
							Sql sql )
	{
        assert( sql );
        String command;
        try
        {
			if ( variant == "Fluidigm" )
			{
				command =
						"INSERT INTO fluidigm_chip_loaded" +
						"  ( `version`," +
						" `chips_loaded_id`," +
						" `chip_run_info`," +
						" `application_version`," +
						" `application_build`," +
						" `export_type`," +
						" `quality_threshold`, " +
						" `baseline_correction_method`," +
						" `ct_threshold_method` )" +
						"  VALUES ( ?, ?, ?, ?, ?, ?, ?, ?, ? )";
				Float qualityThreshold =
						Importer.parseFloat( data[ "Quality Threshold" ] );
				List< List > ids =
						sql.executeInsert(
							command,
							[ 0,
							  logRecord.id,
							  data[ "Chip Run Info" ],
							  data[ "Application Version" ],
							  data[ "Application Build" ],
							  data[ "Export Type" ],
							  qualityThreshold,
							  data[ "Baseline Correction Method" ],
							  data[ "Ct Threshold Method" ]
							] );
				return FluidigmChipLoaded.get( ids[ 0 ][ 0 ] );
			}
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message
            throw new ImportException( msg );
        }
	}

//=============================================================================

    static
    void writeBulkLoadLine( PrintWriter writer,
							FocusedArraySampleAssayDatum datum )
    {
		String assay_code = "A" + datum.assayInfo.plate +
				"_A" + datum.assayInfo.well;
        Importer.writeBulkLoadLine( writer,
									[ datum.sampleInfo.id, assay_code,
									  datum.assayInfo.target,
									  datum.ctValue, datum.ctQuality,
									  datum.ctCall, datum.ctThreshold,
									  datum.tmInRange, datum.tmOutRange,
									  datum.tmPeakRatio ] );
    }

//-----------------------------------------------------------------------------

    static
    void loadDataToDb( String bulkLoadSpec, Sql sql )
    {
        Importer.loadDataToDb( bulkLoadSpec, "focused_array_sample_assay_data",
                               [ "array_data_id", "assay_code", "target",
                                 "ct_value", "ct_quality", "ct_call",
								 "ct_threshold", "tm_in_range", "tm_out_range",
								 "tm_peak_ratio" ],
                               sql );
    }

//=============================================================================

	static
	void loadHousekeepingGenesFile( String fileSpec,
									ChipType chipType,
									Sql sql )
	{
		String command;
        try
        {
			new File( fileSpec ).eachLine { line ->
				if ( line.size() > 0 )
				{
					command =
							"INSERT INTO focused_array_housekeeping \n" +
							"  ( chip_type_id, target ) \n" +
							"  VALUES ( " + chipType.id + ", " +
							Importer.quoteForSql( line ) + " )";
					sql.execute( command );
				}
			}
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
	}

//.............................................................................

	static
	void loadHousekeepingGenesFile( String fileSpec,
									ChipsLoaded chipsLoaded,
									Sql sql )
	{
		String command;
        try
        {
			new File( fileSpec ).eachLine { line ->
				if ( line.size() > 0 )
				{
					command =
							"INSERT INTO focused_array_housekeeping \n" +
							"  ( chips_loaded_id, target ) \n" +
							"  VALUES ( " + chipsLoaded.id + ", " +
							Importer.quoteForSql( line ) + " )";
					sql.execute( command );
				}
			}
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
	}

//=============================================================================

	static
	void loadReferenceSamplesFile( String fileSpec,
								   ChipType chipType,
								   Sql sql )
	{
		String command;
        try
        {
			new File( fileSpec ).eachLine { line ->
				if ( line.size() > 0 )
				{
					command =
							"INSERT INTO focused_array_reference_samples \n" +
							"  ( chip_type_id, sample_name ) \n" +
							"  VALUES ( " + chipType.id + ", " +
							Importer.quoteForSql( line ) + " )";
					sql.execute( command );
				}
			}
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
	}

//-----------------------------------------------------------------------------

	static
	void loadReferenceSamplesFile( String fileSpec,
								   ChipsLoaded chipsLoaded,
								   Sql sql )
	{
		List< String > referenceSamples = [];
		new File( fileSpec ).eachLine { line ->
			if ( line.size() > 0 )
			{
				referenceSamples.add( line );
			}
		}
		setReferenceSamples( chipsLoaded, referenceSamples, sql );
	}

//.............................................................................

	static
	void setReferenceSamples( ChipsLoaded chipsLoaded,
							  Sql sql )
	{
		String query;
        try
        {
			ChipType chipType = chipsLoaded.chipType;
			query =
					"SELECT sample_name \n" +
					"  FROM focused_array_reference_samples \n" +
					"  WHERE chip_type_id = " + chipType.id;
			List< GroovyRowResult > rows = sql.rows( query );
			List< String > referenceSamples = rows*.sample_name;
			setReferenceSamples( chipsLoaded, referenceSamples, sql );
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Query: " + query + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
	}

//.............................................................................

	static
	void setReferenceSamples( ChipsLoaded chipsLoaded,
							  List< String > referenceSamples,
							  Sql sql )
	{
		String command;
        try
        {
			String listString = "( " +
					referenceSamples.collect(
						{
							Importer.quoteForSql( it )
						}
					).join( ", " ) + " )";

			command =
					"UPDATE array_data \n" +
					"  SET sample_type='NTC' \n" +
					"  WHERE chip_id=" + chipsLoaded.id + "\n" +
					"    AND orig_sample_type='NTC'";
			sql.execute( command );

			command =
					"UPDATE array_data \n" +
					"  SET sample_type='Ref' \n" +
					"  WHERE chip_id=" + chipsLoaded.id + "\n" +
					"    AND sample_name IN " + listString;
			sql.execute( command );

			command =
					"UPDATE array_data \n" +
					"  SET sample_type='NonRef' \n" +
					"  WHERE chip_id=" + chipsLoaded.id + "\n" +
					"    AND sample_type IS NULL";
			sql.execute( command );
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
	}

//=============================================================================

	static
	Map importFoldChangeResults( SampleSet sampleSet,
								 Long foldChangeParamsId,
								 String fcFileSpec,
								 FocusedArraySampleSet focusedArraySampleSet,
								 SampleSetService sampleSetService,
								 Sql sql )
	{
		Map result = [ success: false, message: "" ];
		try
		{
			Map assayTargetMap =
					focusedArraySampleSet.getAssayTargetMap( sampleSet );
			Map barcodeSampleIdMap =
					sampleSetService.getBarcodeToArrayDataId( sampleSet.id, sql );

			FoldChangeTableInfo tableInfo =
					getFoldChangeTableInfo( fcFileSpec );

			FileSpecParts specParts = Importer.parseFileSpec( fcFileSpec );
			String bulkLoadSpec =
					specParts.path + "/" + specParts.name + "_bulkload.tsv";
			PrintWriter bulkLoadFile = new PrintWriter( bulkLoadSpec );

			Closure processRow = { fields, lineNum ->
				String assayCode = fields[ 0 ];
				String target = assayTargetMap[ (assayCode) ];
				for ( int i = 1; i < fields.size(); ++i )
				{
					String sampleCode = tableInfo.sampleCodes[ i ];
					Long sampleId = barcodeSampleIdMap[ (sampleCode) ];
					Float fc = Importer.parseFloat( fields[ i ] );

					List data = [ foldChangeParamsId, sampleId,
								  assayCode, target, fc ];
					Importer.writeBulkLoadLine( bulkLoadFile, data );
				}
			}

			TextTable.process( fcFileSpec, tableInfo, processRow );

			bulkLoadFile.flush( );
			bulkLoadFile.close( );

			List< String > fields =
					[ "focused_array_fold_change_params_id", "array_data_id",
					  "assay_code", "target", "fc" ];
			Importer.loadDataToDb( bulkLoadSpec, "focused_array_fold_changes",
								   fields, sql );

			result.success = true;
        }
        catch ( ImportException exc )
        {
            result.message =
                    (exc.messages  ?  exc.messages[ 0 ]  :  "Import error" );
			println ( result.message );
            // exc.printStackTrace( ); //Just for debugging
        }
        catch ( Throwable exc )
        {
            result.message = exc.message;
			println ( result.message );
        }
		return result;
	}

//-----------------------------------------------------------------------------

	static
	FoldChangeTableInfo getFoldChangeTableInfo( String fcFileSpec )
	{
		FoldChangeTableInfo tableInfo = new FoldChangeTableInfo( );
		tableInfo.separator = TextTableSeparator.CSV;
		tableInfo.firstRow = 1;
		tableInfo.numRows = 0;
		new File( fcFileSpec ).eachLine { line, lineNum ->
			if ( lineNum == 1 )
			{
				tableInfo.sampleCodes =
						TextTable.splitRow( line, tableInfo.separator );
				tableInfo.numColumns = tableInfo.sampleCodes.size();
			}
			else
			{
				++tableInfo.numRows;
			}
		}
		return tableInfo;
	}

//=============================================================================

	static
	void validateSampleInfoListConsistency(
		List< List< FocusedArraySampleInfo > > sampleInfoLists,
		List< String > warnings )
	{
		List< String > samplePropNames =
				[ "name", "type", "concentration" ];
		validateListConsistency( "Sample", sampleInfoLists, samplePropNames,
								 warnings );
	}

//-----------------------------------------------------------------------------

	static
	void validateAssayInfoListConsistency(
		List< List< FocusedArrayAssayInfo > > assayInfoLists,
		List< String > warnings )
	{
		List< String > assayPropNames =
				[ "target" ];
		validateListConsistency( "Assay", assayInfoLists, assayPropNames,
								 warnings );
	}

//-----------------------------------------------------------------------------

	static
	void validateListConsistency( String itemType,
								  List< List > lists,
								  List< String > propNames,
								  List< String > warnings )
	{
		Map< Map, Map > itemsMap = [:];
		for ( List itemList : lists )
		{
			for ( def item : itemList )
			{
				Map codeMap = [ plate: item.plate,
								well: item.well ];
				if ( itemsMap.containsKey( codeMap ) )
				{
					Map props = itemsMap[ (codeMap) ];
					for ( String propName : propNames )
					{
						if ( item[ (propName) ] !=
							 props[ (propName) ] )
						{
							String msg =
									itemType + " {plate:" + item.plate +
									"well: " + item.well + "}" +
									" has ${propName} " + props[ (propName) ] +
									" and " + item[ (propName) ] +
									" in different files";
							throw new ImportException( msg );
						}
					}
				}
				else
				{
					Map props = [:];
					for ( String propName : propNames )
					{
						props[ (propName) ] = item[ (propName) ];
					}
					itemsMap[ (codeMap) ] = props;
				}
			}
		}
	}

//=============================================================================

	static
	void validateAssaysAgainstDesignTargets(
		List< List< FocusedArrayAssayInfo > > assayInfoLists,
		Map< String, Boolean > designTargets,
		List< String > warnings )
	{
		final int unknownTargetThreshold = 5;
		Set< String > unknownTargets = new HashSet< String >();
		for ( List< FocusedArrayAssayInfo > assayInfoList : assayInfoLists )
		{
			for ( FocusedArrayAssayInfo assayInfo : assayInfoList )
			{
				String target = assayInfo.target;
				if ( designTargets.containsKey( target ) )
				{
					designTargets[ (target) ] = true;
				}
				else
				{
					unknownTargets.add( target );
				}
			}
		}
		List< String > errors = [];
		for ( String unknownTarget : unknownTargets )
		{
			errors.add( "Unknown assay target: " + unknownTarget );
		}
		if ( errors.size() > unknownTargetThreshold )
		{
			throw new ImportException( errors );
		}
		else
		{
			warnings.addAll( errors );
		}
	}

//=============================================================================


    static
    void cleanup( boolean succeeded,
				  AuxFileSpecs auxSpecs, List< FileSpecParts > specPartsList,
                  List< ArrayDataSample > samples, Long sampleSetId,
                  Sql sql )
    {
        Importer.cleanup( succeeded, auxSpecs, specPartsList );
        if ( succeeded )
        {
            return;
        }
		//The rest is a partial rollback due to failure.
		// Partial because we leave the chips_loaded record intact,
		// with the error report, and because I may have forgotten something.
        String command;
        try
        {
			command = "DELETE FROM focused_array_sample_assay_data" +
					"  WHERE array_data_id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

			command = "DELETE FROM focused_array_fold_changes" +
					"  WHERE array_data_id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

			command = "DELETE FROM array_data" +
                    "  WHERE id = ?";
			for ( ArrayDataSample sample : samples )
			{
				if ( sample.id )
					sql.execute( command, [ sample.id ] );
			}

            if ( sampleSetId )
            {
                SampleSet sampleSet = SampleSet.get( sampleSetId );
                if ( sampleSet )
                {
                    sampleSet.delete( flush: true );
                }
			}
        }
        catch ( SQLException exc )
        {
            String msg = "SQL error.\n" +
                    "Command: " + command + "\n" +
                    "Message: " + exc.message;
            throw new ImportException( msg );
        }
    }
	
//-----------------------------------------------------------------------------
}                                                            //FocusedArrayData


//*****************************************************************************


class FocusedArrayDataValidator
{                                                   //FocusedArrayDataValidator
//-----------------------------------------------------------------------------

	protected
	void start( )
	{
        m_errors = [];
	}

//=============================================================================

    protected
    boolean checkFieldCount( TextTableInfo tableInfo,
                             String[] fields, int lineNum )
    {
        if ( fields.size() != tableInfo.numColumns )
        {
            String msg = "Line " + lineNum + " has " + fields.size() +
                    " fields. Expecting " + tableInfo.numColumns;
            m_errors.add( msg );
            return false;
        }
        return true;
    }

//=============================================================================

    protected
    void finish( )
	{
        if ( m_errors.size() > 0 )
        {
            throw new ImportException( m_errors );
        }
    }

//=============================================================================

    protected List< String > m_errors;
    protected List< String > m_warnings;

//-----------------------------------------------------------------------------
}                                                   //FocusedArrayDataValidator


//*****************************************************************************
