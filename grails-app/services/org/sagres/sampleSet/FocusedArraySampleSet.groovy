/*
  FocusedArraySampleSet.groovy

  Routines specific to focused-array data.
*/

package org.sagres.sampleSet;

import groovy.sql.Sql;
import java.sql.SQLException;
import groovy.sql.GroovyRowResult;
import groovy.util.ConfigObject;
import org.sagres.util.SagresException;
import org.sagres.util.FileSys;
import org.sagres.util.OS;
import org.sagres.sampleSet.SampleSetService;
import common.chipInfo.ChipType;
import common.chipInfo.ChipData;
import org.sagres.importer.ImportService;
import org.sagres.importer.TextTable;
import org.sagres.importer.TextTableSeparator;


//*****************************************************************************


class FocusedArraySampleSet
{                                                       //FocusedArraySampleSet
//-----------------------------------------------------------------------------

	FocusedArraySampleSet( SampleSetService sampleSetService,
						   Sql sql,
						   ConfigObject appConfig,
						   ImportService importService )
	{
		m_sampleSetService = sampleSetService;
		m_sql = sql;
		m_appConfig = appConfig;
		m_importService = importService;
	}

//=============================================================================

	Map computeFoldChanges( SampleSet sampleSet, Float palx, Float floor )
	{
		Map result = [ success: false, message: "" ];
		try
		{
			FocusedArrayFoldChangeParams params =
					new FocusedArrayFoldChangeParams( sampleSet: sampleSet,
													  palx: palx,
													  floor: floor );
			params.save( flush: true );

			String baseDir = m_appConfig.focusedArrayFoldChange.baseDir +
					params.id + '/';
			String dataDir = baseDir + "Data/";
			FileSys.makeDirIfNeeded( dataDir );
			String paramsDir = baseDir + "Parameters/";
			FileSys.makeDirIfNeeded( paramsDir );
			String scriptsSrcDir =
					m_appConfig.focusedArrayFoldChange.baseDir + "scripts/";
			String scriptsCopyDir = baseDir + "scripts/";
			FileSys.makeDirIfNeeded( scriptsCopyDir );
			String resultsDir = baseDir + "Results/";
			FileSys.makeDirIfNeeded( resultsDir );

			generateDataFilesForFC( sampleSet, dataDir );
			writeParamsFile( paramsDir + "parameters.csv",
							 [ PALX: palx, floor: floor ] );
			copyScripts( scriptsSrcDir, scriptsCopyDir );

			String scriptName = "NewDataDesign_CalculateFCfromCt.R";
			runFoldChangeScript( scriptsSrcDir + scriptName,
								 baseDir );
		
			importFoldChangeResults( sampleSet, params.id,
									 resultsDir + "FC.csv" );

			sampleSet.defaultFoldChangeParams = params;
			sampleSet.save( flush: true );

			result.success = true;
		}
		catch ( SagresException exc )
		{
			result.message = exc.message;
		}
		return result;
	}

//=============================================================================

	private
	boolean generateDataFilesForFC( SampleSet sampleSet, String directory )
	{
		String sampleIdsTable = makeSampleIdsTempTable( sampleSet );
		
		generateDataFile( sampleIdsTable,
						  directory + "dataFile.csv" );
		generateAssayDesignFile( sampleSet, sampleIdsTable,
								 directory + "assayDesignFile.csv" );
		generateSampleDesignFile( sampleIdsTable,
								  directory + "sampleDesignFile.csv" );

		dropTable( sampleIdsTable );
		return true;
	}

//-----------------------------------------------------------------------------

	private
	String makeSampleIdsTempTable( SampleSet sampleSet )
	{
		String sqlStatement;
		try
		{
			String tmpTableName = "tmp_samples";
			sqlStatement =
					"CREATE TEMPORARY TABLE " + tmpTableName + "\n" +
					"  (id BIGINT PRIMARY KEY)";
			m_sql.execute( sqlStatement );
			
			List< Long > sampleIds =
				   m_sampleSetService.getArrayDataIdsForSampleSet( sampleSet.id,
																   m_sql );
			for ( Long sampleId : sampleIds )
			{
				sqlStatement =
						"INSERT INTO " + tmpTableName + "\n" +
						"  (`id`) \n" +
						"  VALUES (" + sampleId + ")";
				m_sql.execute( sqlStatement );
			}
			return tmpTableName;
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"SQL: " + sqlStatement + "\n" +
					"Message: " + exc.message;
			throw new SagresException( msg );
		}
	}

//-----------------------------------------------------------------------------

    private
    void dropTable( String tableName )
    {
		String sqlStatement;
		try
		{
			sqlStatement =
					"DROP TABLE IF EXISTS " + tableName;
			m_sql.execute( sqlStatement );
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"SQL: " + sqlStatement + "\n" +
					"Message: " + exc.message;
			throw new SagresException( msg );
		}
    }

//-----------------------------------------------------------------------------

	private
	void generateDataFile( String sampleIdsTable,
						   String fileSpec )
	{
		String query;
		try
		{
			TextTableSeparator sep = TextTableSeparator.CSV;

			PrintWriter dataFile = new PrintWriter( fileSpec );
			List< String > fields =
					[ "SampleID", "AssayID",
					  "Ct.Value", "Ct.Quality", "Ct.Call" ];
			dataFile.println( TextTable.joinRow( fields, sep, true ) );

			query =
					"SELECT ad.barcode, fasad.assay_code," +
					" fasad.ct_value, fasad.ct_quality, fasad.ct_call \n" +
					"  FROM array_data AS ad \n" +
					"  INNER JOIN focused_array_sample_assay_data AS fasad \n" +
					"    ON ad.id = fasad.array_data_id \n" +
					"  INNER JOIN " + sampleIdsTable + " AS ids \n" +
					"    ON ids.id = ad.id";
			List< GroovyRowResult > rows = m_sql.rows( query );
			for ( GroovyRowResult row : rows )
			{
				List< String > values =
						[ row.barcode, row.assay_code,
						  row.ct_value, row.ct_quality, row.ct_call ];
				dataFile.println( TextTable.joinRow( values, sep, true ) );
			}
			dataFile.flush( );
			dataFile.close( );
		}
		catch ( IOException exc )
		{
			String msg = "Error writing " + fileSpec + "\n" + exc.message;
			throw new SagresException( msg );
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"Query: " + query + "\n" +
					"Message: " + exc.message;
			println( msg );
		}
	}

//-----------------------------------------------------------------------------

	private
	void generateAssayDesignFile( SampleSet sampleSet,
								  String sampleIdsTable,
								  String fileSpec )
	{
		String sqlStatement;
		try
		{
			TextTableSeparator sep = TextTableSeparator.CSV;

			PrintWriter designFile = new PrintWriter( fileSpec );
			List< String > fields =
					[ "AssayID", "Target", "Type" ];
			designFile.println( TextTable.joinRow( fields, sep, true ) );

			//All samples will have records for all assays, so just get one
			sqlStatement =
					"SELECT id FROM " + sampleIdsTable;
			Long sampleId = m_sql.firstRow( sqlStatement ).id;

			sqlStatement =
					"CREATE TEMPORARY TABLE tmp_housekeeping \n" +
					"  ( target VARCHAR( 255 ), \n" +
					"    flag VARCHAR( 255 ) )";
			m_sql.execute( sqlStatement );

			sqlStatement =
					"INSERT INTO tmp_housekeeping \n" +
					"  ( target, flag ) \n" +
					"  SELECT target, 'HK' \n" +
					"    FROM focused_array_housekeeping \n" +
					"    WHERE chips_loaded_id = " + sampleSet.chipsLoaded.id;
			List inserted = m_sql.executeInsert( sqlStatement );
			if ( inserted.size() == 0 )
			{
				sqlStatement =
						"INSERT INTO tmp_housekeeping \n" +
						"  ( target, flag ) \n" +
						"  SELECT target, 'HK' \n" +
						"    FROM focused_array_housekeeping \n" +
						"    WHERE chip_type_id = " + sampleSet.chipType.id;
				inserted = m_sql.executeInsert( sqlStatement );
			}

			sqlStatement =
					"SELECT fasad.assay_code, fasad.target, hk.flag \n" +
					"  FROM focused_array_sample_assay_data AS fasad \n" +
					"  LEFT JOIN tmp_housekeeping AS hk \n" +
					"    ON hk.target = fasad.target \n" +
					"  WHERE fasad.array_data_id = " + sampleId;
			List< GroovyRowResult > rows = m_sql.rows( sqlStatement );

			for ( GroovyRowResult row : rows )
			{
				List< String > values =
						[ row.assay_code, row.target, (row.flag ?: "NonHK") ];
				designFile.println( TextTable.joinRow( values, sep, true ) );
			}
			designFile.flush( );
			designFile.close( );
			dropTable( "tmp_housekeeping" );
		}
		catch ( IOException exc )
		{
			String msg = "Error writing " + fileSpec + "\n" + exc.message;
			throw new SagresException( msg );
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"SQL: " + sqlStatement + "\n" +
					"Message: " + exc.message;
			println( msg );
		}
	}

//-----------------------------------------------------------------------------

	private
	void generateSampleDesignFile( String sampleIdsTable,
								   String fileSpec )
	{
		String sqlStatement;
		try
		{
			TextTableSeparator sep = TextTableSeparator.CSV;

			PrintWriter designFile = new PrintWriter( fileSpec );
			List< String > fields =
					[ "SampleID", "SampleName", "Type" ];
			designFile.println( TextTable.joinRow( fields, sep, true ) );

			sqlStatement =
					"SELECT ad.barcode, ad.sample_name, ad.sample_type \n" +
					"  FROM array_data AS ad \n" +
					"  INNER JOIN " + sampleIdsTable + " AS ids \n" +
					"    ON ids.id = ad.id";
			List< GroovyRowResult > rows = m_sql.rows( sqlStatement );
			for ( GroovyRowResult row : rows )
			{
				List< String > values =
						[ row.barcode, row.sample_name, row.sample_type ];
				designFile.println( TextTable.joinRow( values, sep, true ) );
			}
			designFile.flush( );
			designFile.close( );
		}
		catch ( IOException exc )
		{
			String msg = "Error writing " + fileSpec + "\n" + exc.message;
			throw new SagresException( msg );
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"SQL: " + sqlStatement + "\n" +
					"Message: " + exc.message;
			println( msg );
		}
	}

//=============================================================================

	private
	void writeParamsFile( String fileSpec, Map params )
	{
		try
		{
			PrintWriter paramsFile = new PrintWriter( fileSpec );
			paramsFile.println( "Key,Value" );
			paramsFile.println( "PALX," +
								(params.PALX != null ? params.PALX :"NA") );
			paramsFile.println( "floor," +
								(params.floor != null ? params.floor : "NA") );
			paramsFile.flush( );
			paramsFile.close( );
		}
		catch ( IOException exc )
		{
			String msg = "Error writing " + fileSpec + "\n" + exc.message;
			throw new SagresException( msg );
		}
	}

//=============================================================================

	private
	void copyScripts( String scriptsSrcDir, String scriptsDestDir )
	{
		FileSys.copyDirectory( scriptsSrcDir, scriptsDestDir, true );
	}

//=============================================================================

	private
	void runFoldChangeScript( String scriptSpec, String workingDir )
	{
		try
		{
			Map results = [:];
			boolean rslt =
					OS.execSysCommand( m_appConfig.mat.R.executable,
									   [ '--vanilla', scriptSpec, workingDir ],
									   results, 1 );
			if ( results.error )
			{
				PrintWriter errorsFile =
						new PrintWriter( workingDir + "Results/errors.txt" );
				errorsFile.write( results.error );
				errorsFile.flush( );
				errorsFile.close( );
			}
			if ( results.output )
			{
				PrintWriter messagesFile =
						new PrintWriter( workingDir + "Results/messages.txt" );
				messagesFile.write( results.output );
				messagesFile.flush( );
				messagesFile.close( );
			}
			if ( rslt == false )
			{
				throw new SagresException( "Error executing R scripts: " +
										   results.error );
			}
		}
		catch ( IOException exc )
		{
			String msg = "IO error: " + exc.message;
			throw new SagresException( msg );
		}
	}

//=============================================================================

	private
	void importFoldChangeResults( SampleSet sampleSet, Long paramsId,
								  String resultsSpec )
	{
		Map importRslt =
				m_importService.importFocusedArrayFoldChangeResults(
					sampleSet, paramsId, resultsSpec,
					this, m_sampleSetService, m_sql );
		if ( importRslt.success == false )
		{
			throw new SagresException( importRslt.message );
		}
	}

//=============================================================================

	Map getAssayTargetMap( SampleSet sampleSet )
	{
		Map assayTargetMap = [:];
		String sqlStatement;
		try
		{
			List< Long > sampleIds =
				   m_sampleSetService.getArrayDataIdsForSampleSet( sampleSet.id,
																   m_sql );
			Long sampleId = sampleIds[ 0 ];

			sqlStatement =
					"SELECT assay_code, target \n" +
					"  FROM focused_array_sample_assay_data \n" +
					"  WHERE array_data_id = " + sampleId;
			List< GroovyRowResult > rows = m_sql.rows( sqlStatement );

			for ( GroovyRowResult row : rows )
			{
				assayTargetMap[ (row.assay_code) ] = row.target;
			}
		}
		catch ( SQLException exc )
		{
			String msg = "SQL error.\n" +
					"SQL: " + sqlStatement + "\n" +
					"Message: " + exc.message;
			throw new SagresException( msg );
		}
		return assayTargetMap;
	}
	
//=============================================================================

	private SampleSetService m_sampleSetService;
	private Sql m_sql;
	private ConfigObject m_appConfig;
	private ImportService m_importService;

//-----------------------------------------------------------------------------
}                                                       //FocusedArraySampleSet


//*****************************************************************************
