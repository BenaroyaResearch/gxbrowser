#!/usr/bin/groovy
package org.sagres.dm

import groovy.sql.Sql;
import java.sql.SQLException;
import java.io.FileReader;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.ParsePosition;


//*****************************************************************************

String defaultDbHost = "localhost";
String altDbHost = "srvdmdev";
String defaultDb = "dm_dev";
String defaultDbUser = "browser";
String defaultDbPassword = "bibliome";

def cli = new CliBuilder( usage: "BackSub.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.f( longOpt: "filename",
	   required: true,
	   args: 1,
	   argName: "softFileName",
       "Background-subtracted expression data file name" );
cli.h( longOpt: "host",
	   required: false,
	   args: 1,
	   argName: "dbHost",
       "Database host [default: ${defaultDbHost}" +
	   (altDbHost ? ", alternative: ${altDbHost}" : "") + "]" );
cli.d( longOpt: "database",
	   required: false,
	   args: 1,
	   argName: "dbName",
       "Database [default: ${defaultDb}]" );
cli.u( longOpt: "user",
	   required: (! defaultDbUser),
	   args: 1,
	   argName: "dbUser",
       "Database user name" +
	   (defaultDbUser ? " [default: ${defaultDbUser}]" : "") );
cli.p( longOpt: "password",
	   required: (! defaultDbPassword),
	   args: 1,
	   argName: "dbPassword",
       "Database user name" +
	   (defaultDbPassword ? " [default: ${defaultDbPassword}]" : "") );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

def importerArgs = [:];
importerArgs.inputFileSpec = clArgs.f;
importerArgs.verbose = (clArgs.v == true);

String dbHost = clArgs.h ?: defaultDbHost;
String db = clArgs.d ?: defaultDb;
String dbUser = clArgs.u ?: defaultDbUser;
String dbPassword = clArgs.p ?: defaultDbPassword;

def sql =
       Sql.newInstance( "jdbc:mysql://${dbHost}/${db}?useServerPrepStmts=false",
                        dbUser, dbPassword,
                        "com.mysql.jdbc.Driver");
if ( sql == null )
{
    println( "Unable to get Sql connection with data source ${dataSource}" );
    return;
}

def importer = new SoftExpressionDataImporter( importerArgs, sql );
importer.importData( );


//*****************************************************************************


class SoftExpressionDataImporter
{                                                  //SoftExpressionDataImporter
//-----------------------------------------------------------------------------

    SoftExpressionDataImporter( def args, Sql sql )
    {
        m_args = args;
        m_sql = sql;
    }

//=============================================================================

    boolean importData( )
    {
        int numProbeErrorsThreshold = 0;

		if ( processSpec( m_args.inputFileSpec ) &&
			 readHeader( ) &&
			 populateStatuses( ) &&
			 queryChipType( ) &&
			 queryGenomicDataSource( ) &&
			 createChipsLoadedRecord( ) &&
			 loadValidProbes( ) &&
			 processProbeData( true, false ) &&
			 (m_numProbeErrors <= numProbeErrorsThreshold) &&
			 addArrayDataRecords( ) &&
			 processProbeData( false, true ) &&
			 runBulkLoad( m_bulkLoadFileSpec ) )
		{
			moveFile( m_args.inputFileSpec, m_inputArchiveSpec );
			moveFile( m_bulkLoadFileSpec, m_bulkLoadArchiveSpec );
			if ( m_numProbeErrors > 0 )
			{
				reportErrorList( false );
			}
			setStatus( Status.COMPLETE, [ date_ended: new Date() ] );
			return true;
		}
		else
		{
			moveFile( m_args.inputFileSpec, m_errorFileSpec );
			deleteFile( m_bulkLoadFileSpec, true );
			if ( m_numProbeErrors > 0 )
			{
				reportErrorList( true );
			}
			return false;
		}
    }

//=============================================================================

    private
    void reportError( String message )
    {
        if ( m_chipsLoadedId > 0 )
        {
            setStatus( Status.ERROR,
					   [ notes: message, date_ended: new Date() ] );
        }
        else
        {
            println message;
        }
    }

//-----------------------------------------------------------------------------

    private
    void setStatus( Status status, def fields )
    {
        if ( status == Status.ERROR )
        {
            println status.toString() +
					(fields?.notes ? (": " + fields.notes) : "");
        }
        else
        {
            println status.toString() +
					(fields?.notes ? (": " + fields.notes) : "");
        }

        try
        {
			String command = "UPDATE chips_loaded";
			command += " SET load_status_id = ?";
			def fieldVals = [];
			fieldVals += ms_statuses[ status ];

			def updateableFields = [ "notes", "no_probes", "no_samples",
									 "date_ended" ];
			updateableFields.each
			{
				if ( fields[ it ] )
				{
					command += ", " + it + " = ?";
					fieldVals += fields[ it ];
				}
			}
			command += " WHERE id = ?";
			fieldVals += m_chipsLoadedId;
            m_sql.execute( command, fieldVals );
            if ( m_sql.updateCount != 1 )
            {
				String msg = "Unable to update chips_loaded\n" +
						" Command was:\n" + command;
				println msg;
            }
        }
        catch ( SQLException exc )
        {
            println "SQL error updating load_status_id in chips_loaded:";
			println exc.message;
        }
    }

//=============================================================================

	private
	boolean processSpec( String inputFileSpec )
	{
        //Expects a spec of the form
        // baseDir/toBeImported/{subdir}/{filename}
		String[] specParts = inputFileSpec.split( "/" );
		int numParts = specParts.size();
		if ( numParts < 4 )
		{
            reportError( "Unable to parse '${inputFileSpec}'" );
			return false;
		}
		m_fileName = specParts[ -1 ];
		String subDir = specParts[ -2 ];
		String midDir = specParts[ -3 ];
        String baseDir = specParts[ 0..-4 ].join( "/" );
		String[] nameParts = m_fileName.split( /\./ );
		String baseName = (nameParts.size() < 2)  ?  m_fileName  :
				nameParts[ 0..-2 ].join( "." );

        m_bulkLoadFileSpec = [ baseDir, midDir, subDir,
							   baseName + "_bulkLoad.txt" ].join( "/" );
        m_inputArchiveSpec = [ baseDir, "importedFiles", subDir,
                               m_fileName ].join( "/" );
        m_bulkLoadArchiveSpec = [ baseDir, "importedFiles", subDir,
                                  baseName + "_bulkLoad.txt" ].join( "/" );
        m_errorFileSpec = [ baseDir, "errors", m_fileName ].join( "/" );
        m_errorLogSpec = [ baseDir, "errors",
						   baseName + "_errors.txt" ].join( "/" );
        
		return true;
	}

//=============================================================================

    private
    boolean readHeader( )
    {
        def platformIdKeys = //lower-cased
                [ "^platform",
                  "!sample_platform_id",
                  "!series_platform_id",
                  "!dataset_platform",
                  "!annotation_platform"
                ];

		m_format = Format.UNKNOWN;
		m_gplId = "";
        try
        {
            def inputFile = new BufferedReader(
                new FileReader( m_args.inputFileSpec ) );
			String line = inputFile.readLine( );
            while( (line != null) && (m_format == Format.UNKNOWN) )
            {
				if ( line.startsWith( "!dataset_table_begin" ) )
				{
					m_format = Format.DATASET_TABLE;
                    if ( m_args.verbose )
                    {
                        println( "Format: dataset_table" );
                    }
				}
				else if ( line.startsWith( "!sample_table_begin" ) )
				{
					m_format = Format.SAMPLE_TABLE;
                    if ( m_args.verbose )
                    {
                        println( "Format: sample_table" );
                    }
				}
                else if ( m_gplId == "" )
				{
					for ( String key : platformIdKeys )
					{
						if ( line.toLowerCase().startsWith( key ) )
						{
							def parts = line.split( "=" );
							if ( parts.size() < 2 )
								break;
							String platformId = parts[1].trim();
							if ( platformId.startsWith( "GPL" ) )
							{
								m_gplId = platformId;
								break;
							}
						}
					}
				}
				line = inputFile.readLine( );
            }
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open input file" );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading input file" );
            return false;
        }
		if ( m_format == Format.UNKNOWN )
		{
			reportError( "Unknown format" );
			return false;
		}
		if ( m_gplId == "" )
		{
			reportError( "Platform ID not determined" );
			return false;
		}

        return true;
    }

//=============================================================================

	private
	boolean populateStatuses( )
	{
        if ( ms_statuses.size() == Status.values().size() )
            return true;
		def statusNames = [ "Creating Samples", "Importing Signal Data",
							"Complete", "Errors - Samples NOT Created",
							"Errors - Samples Created" ];
		for ( status in Status.values() )
		{
			String statusName = statusNames[ status.ordinal() ];
            try
            {
                String query = "SELECT id FROM chip_load_status WHERE name = ?";
                def chipLoadStatus = m_sql.firstRow( query, [ statusName ] );
                if ( chipLoadStatus == null )
                {
                    reportError( "No chip_load_status with name '" +
                                 statusName + "'" );
                    return false;
                }

                ms_statuses[ status ] = chipLoadStatus.id;
            }
            catch ( SQLException exc )
            {
                reportError( "SQL error occurred reading chip_load_status:\n" +
							 exc.message );
				return false;
            }
		}
		return true;
	}

//=============================================================================

    private
    boolean queryChipType( )
    {
        try
        {
			String query = """SELECT chip_type_id FROM gpl_chip_type
                              WHERE gpl_id = ?""";
			def gplChipType = m_sql.firstRow( query, [ m_gplId ] );
			if ( gplChipType == null )
			{
				reportError( "No gpl_chip_type with gpl_id " + m_gplId );
				return false;
			}
            m_chipTypeId = gplChipType.chip_type_id;
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred reading gpl_chip_type:\n" +
						 exc.message );
            return false;
        }

        try
        {
            String query = "SELECT probe_list_table, probe_list_column, name" +
					" FROM chip_type" +
					" WHERE id = ?";
            def chipType = m_sql.firstRow( query, [ m_chipTypeId ] );
            if ( chipType == null )
            {
                reportError( "No chip_type with id " + m_chipTypeId );
                return false;
            }
            m_probeListTable = chipType.probe_list_table;
            m_probeListColumn = chipType.probe_list_column;

            if ( ! m_probeListTable )
            {
                reportError( "No probeListTable listed for chip_type " +
                             m_chipTypeId );
                return false;
            }
            if ( ! m_probeListColumn )
            {
                reportError( "No probeListColumn listed for chip_type" +
                             m_chipTypeId );
                return false;
            }

			if ( m_args.verbose )
			{
				println( "Chip name: ${chipType.name}" );
			}
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred reading chip_type:\n" +
						 exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean queryGenomicDataSource( )
    {
        try
        {
            String query = """SELECT id FROM genomic_data_source
                              WHERE name = ?""";
            def gds = m_sql.firstRow( query, [ m_genomicDataSourceName ] );
            if ( gds == null )
            {
                reportError( "No genomic_data_source with name "
                             + "'${m_genomicDataSourceName}'" );
                return false;
            }
            m_genomicDataSourceId = gds.id;
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred reading genomic_data_source:\n" +
						 exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean createChipsLoadedRecord( )
    {
        try
        {
            String command = """INSERT INTO chips_loaded 
                                ( version, chip_type_id,
                                  genomic_data_source_id, date_started,
                                  filename, load_status_id, 
                                  no_probes, no_samples )
                                VALUES( ?, ?, ?, ?, ?, ?, ?, ? )""";
            def now = new Date();
            def key = m_sql.executeInsert( command,
                                           [ 0,
                                             m_chipTypeId,
                                             m_genomicDataSourceId,
                                             now,
                                             m_fileName,
                                             ms_statuses[ Status.STARTING ],
                                             0,
                                             0 ] );
            if ( (key == null) || (key.size() < 1) || (key[0].size() < 1) )
            {
                reportError( "Unable to insert into  chips_loaded with" +
                             ", chip_type_id=" + m_chipTypeId +
                             ", genomic_data_source_id=" + genomicDataSource.id +
                             ", date_started=" + now +
                             ", filename='" + m_fileName + "'" +
                             ", load_status_id=" + ms_statuses[ Status.STARTING ] +
                             ", no_probes=" + 0 +
                             ", no_samples=" + 0 );
                return false;
            }

            m_chipsLoadedId = key[0][0];
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred inserting into chips_loaded:\n" +
						 exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean loadValidProbes( )
    {
        m_validProbes = new HashSet();
        try
        {
            def stmnt = "SELECT " + m_probeListColumn +
                    " FROM " + m_probeListTable +
                    " WHERE ((" + m_probeListColumn + " IS NOT NULL)" +
                    " AND (" + m_probeListColumn + " <> ''))";
            m_sql.eachRow( stmnt )
            { row ->
                m_validProbes.add( row[ 0 ] );
            }
            if ( m_args.verbose )
            {
                println( "Valid probe list size: ${m_validProbes.size()}" );
            }
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred reading column " +
                       m_probeListColumn +
						 " in table " + m_probeListTable + ":\n" +
						 exc.message );
            return false;
        }

        if ( ! m_validProbes )
        {
            reportError( "No valid probes listed in column " +
                         m_probeListColumn + " in table " + m_probeListTable );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean addArrayDataRecords( )
    {
        try
        {
            for( bc in m_barcodes )
            {
				double averageSignal =
					  (m_numProbes > 0) ? bc.signalTotal / m_numProbes : 0.0;
                String command = "INSERT INTO array_data" +
						" ( version, barcode, chip_id, average_signal )" +
                        " VALUES( ?, ?, ?, ? )";
                def keys = m_sql.executeInsert( command,
												[ 0,
												  bc.barcode,
												  m_chipsLoadedId,
												  averageSignal ] );
                if ( (keys == null) ||
                     (keys.size() < 1) || (keys[0].size() < 1) )
                {
                    reportError( "Unable to insert into chips_loaded with" +
                                 " barcode=" + bc.barcode );
                    return false;
                }

                bc.arrayDataId = keys[0][0];
            }
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred inserting into array_data:\n" +
						 exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean processProbeData( boolean checkData, boolean createBulkLoadFile )
    {
        if ( checkData )
        {
            m_numProbes = 0;
            m_errorList = [];
            m_numProbeErrors = 0;
            m_invalidProbes = [];
        }

		if ( m_format == Format.DATASET_TABLE )
			return processDataSetTable( checkData, createBulkLoadFile );
		else if ( m_format == Format.SAMPLE_TABLE )
		    return processSampleTables( checkData, createBulkLoadFile );
		else
			return false;
    }

//-----------------------------------------------------------------------------

	private
	boolean processDataSetTable( boolean checkData,
								 boolean createBulkLoadFile )
	{
        try
        {
            def inputFile = new BufferedReader(
                new FileReader( m_args.inputFileSpec ) );

			def bulkLoadFile;
			if ( createBulkLoadFile )
			{
				try
				{
					bulkLoadFile = new PrintWriter( m_bulkLoadFileSpec );
				}
				catch ( FileNotFoundException exc )
				{
					reportError( "Unable to create " + m_bulkLoadFileSpec );
					return false;
				}
			}

            int section = 0; //0=metadata header, 1=column names, 2=data
            int lineNum = 0;
			String line = inputFile.readLine( );
            while( line != null )
            {
                ++lineNum;
                if ( line.startsWith( "!dataset_table_begin" ) )
                {
                    if ( m_args.verbose )
                    {
                        println( "dataset_table_begin" );
                    }
                    if ( checkData && (section != 0) )
                    {
                        reportError( "Encountered dataset_table_begin," +
                                     " but not in metadata header:" +
                                     " line " + lineNum );
                        return false;
                    }
                    section = 1;
                }
                else if ( line.startsWith( "!dataset_table_end" ) )
                {
                    if ( m_args.verbose )
                    {
                        println( "dataset_table_end" );
                    }
                    if ( checkData && (section != 2) )
                    {
                        reportError( "Encountered dataset_table_end," +
                                     " but not in dataset table section:" +
                                     " line " + lineNum );
                        return false;
                    }
                    section = 0;
                }
				else if ( section == 1 )
				{
                    if ( checkData )
                    {
                        if ( readDataSetColumnNames( line ) == false )
                        {
                            return false;
                        }
                    }
                    section = 2;
				}
				else if ( section == 2 )
				{
                    processDataSetRow( line, lineNum, checkData, bulkLoadFile );
				}
				line = inputFile.readLine( );
			}
            if ( checkData && m_args.verbose )
            {
                println( "${lineNum} lines read" );
                println( "${m_barcodes.size()} barcodes (samples)" );
                println( "${m_numProbes} probes" );
                println( "${m_numProbeErrors} probes with errors" );
            }
		}
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open input file" );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading input file" );
            return false;
        }
		return true;
	}

//.............................................................................

    private
    boolean readDataSetColumnNames( String line )
    {
        String[] columnNames = line.split( "\t", -1 );
        m_numColumns = columnNames.size();
        if ( m_numColumns == 0 )
        {
            reportError( "Bad file format: No column names found" );
            return false;
        }
        if ( columnNames[ 0 ].toUpperCase() != "ID_REF" )
        {
            String msg = "Bad file format: first column (ID_REF) name is '" +
                    columnNames[ 0 ] + "'";
            reportError( msg );
            return false;
        }

        m_barcodes = [];
        columnNames.eachWithIndex
        { name, i ->
            if ( name.startsWith( "GSM" ) )
            {
                def barcodeInfo = [ barcode: name,
                                    column: i,
                                    signalTotal: 0.0
                                  ];
                m_barcodes.add( barcodeInfo );
            }
        }
        if ( m_barcodes.size() == 0 )
        {
            reportError( "No sample barcodes" );
            return false;
        }
        if ( m_args.verbose )
        {
            println( "${m_barcodes.size()} barcodes" );
        }
        return true;
    }

//.............................................................................

    private
    boolean processDataSetRow( String row, int lineNum,
                               boolean checkData, def bulkLoadFile )
    {
        String[] fields = row.split( "\t", -1 );
        if ( fields.size() != m_numColumns )
        {
            if ( checkData )
            {
                String err = "Wrong number of fields (" + fields.size() +
                        ", expected " + m_numColumns + ") in line " + lineNum;
                if ( fields.size() > 0 )
                {
                    err += ". Probe ID=" + fields[ 0 ];
                }
                m_errorList.add( err );
                ++m_numProbeErrors;
            }
            return false;
        }

        String probeId = fields[ 0 ];
        boolean probeValid = false;
        if ( m_validProbes.contains( probeId ) )
        {
            probeValid = true;
        }	
        if ( probeId.startsWith( "ILMN_" ) )
        {
            probeId = probeId.substring( "ILMN_".size() );
        }
        if ( (probeValid == false) && m_validProbes.contains( probeId ) )
        {
            probeValid = true;
        }
        if ( probeValid == false )
        {
            if ( checkData )
            {
                m_errorList.add( "Invalid probe ID: " + probeId );
                m_invalidProbes.add( probeId );
                ++m_numProbeErrors;
            }
            return false;
        }

        boolean probeError = false;
        for ( bc in m_barcodes )
        {
            Double signal = null;
            String field = fields[ bc.column ];
            if ( field != "null" )
            {
                def format = new DecimalFormat();
                def pos = new ParsePosition( 0 );
                Number rawSignal = format.parse( field.toUpperCase(), pos );
                if ( (rawSignal == null) ||
                     (pos.getIndex() < field.size()) )
                {
                    if ( checkData )
                    {
                        String msg = "Invalid signal for sample " +
                                bc.barcode + " for probe " + probeId +
                                ": " + field;
                        m_errorList.add( msg );
                        probeError = true;
                    }
                    continue;
                }
                signal = rawSignal.doubleValue();
                if ( checkData )
                {
                    bc.signalTotal += signal;
                }
            }

            if ( bulkLoadFile )
            {
                bulkLoadFile.print( bc.arrayDataId );
                bulkLoadFile.print( bulkLoadFieldSeparator );
                bulkLoadFile.print( probeId );
                bulkLoadFile.print( bulkLoadFieldSeparator );
                bulkLoadFile.print( (signal != null) ? signal : "NULL" );
                bulkLoadFile.print( "\n" );
            }
        }
        if ( checkData )
        {
            ++m_numProbes;
            if ( probeError )
            {
                ++m_numProbeErrors;
            }
        }

        return true;
    }

//-----------------------------------------------------------------------------

	private
	boolean processSampleTables( boolean checkData,
								 boolean createBulkLoadFile )
	{
        try
        {
            def inputFile = new BufferedReader(
                new FileReader( m_args.inputFileSpec ) );

			def bulkLoadFile;
			if ( createBulkLoadFile )
			{
				try
				{
					bulkLoadFile = new PrintWriter( m_bulkLoadFileSpec );
				}
				catch ( FileNotFoundException exc )
				{
					reportError( "Unable to create " + m_bulkLoadFileSpec );
					return false;
				}
			}

            if ( checkData )
            {
                m_barcodes = [];
                m_numProbes = 0;
                m_maxNumProbes = 0;
                m_signalTotal = 0.0;
            }

            int section = 0; //0=metadata header, 1=sample metadata,
                             // 2=column names, 3=data
            int lineNum = 0;
            m_barcode = "";
			String line = inputFile.readLine( );
            while( line != null )
            {
                ++lineNum;
				if ( line.startsWith( "^SAMPLE" ) )
                {
                    if ( checkData && (section != 0) )
                    {
                        reportError( "Encountered ^SAMPLE but not in" +
                                     " metadata header:" +
                                     " line " + lineNum );
                        return false;
                    }
                    int sampleSubIdx = line.indexOf( "GSM" );
                    if ( checkData && (sampleSubIdx < 0) )
                    {
                        reportError( "No sample barcode (GSM) on line " +
                                     lineNum );
                        return false;
                    }
                    m_barcode = line.substring( sampleSubIdx );
                    if ( m_args.verbose )
                    {
                        println( "SAMPLE: " + m_barcode );
                    }
                    if ( createBulkLoadFile )
                    {
                        for ( def bc : m_barcodes )
                        {
                            if ( bc.barcode == m_barcode )
                            {
                                m_arrayDataId = bc.arrayDataId;
                            }
                        }
                    }
                    section = 1;
                }
                else if ( line.startsWith( "!sample_table_begin" ) )
                {
                    if ( m_args.verbose )
                    {
                        println( "sample_table_begin" );
                    }
                    if ( checkData )
                    {
                        if ( section != 1 )
                        {
                            reportError( "Encountered !sample_table_begin" +
                                         " but not in sample metadata:" +
                                         " line " + lineNum );
                            return false;
                        }
                        m_numProbes = 0;
                    }
                    section = 2;
                }
                else if ( line.startsWith( "!sample_table_end" ) )
                {
                    if ( m_args.verbose )
                    {
                        println( "sample_table_end" );
                    }
                    if ( checkData )
                    {
                        if ( section != 3 )
                        {
                            reportError( "Encountered !sample_table_end" +
                                         " but not in data:" +
                                         " line " + lineNum );
                            return false;
                        }
                        def barcodeInfo = [ barcode: m_barcode,
                                            signalTotal: m_signalTotal ];
                        m_barcodes.add( barcodeInfo );
                        m_barcode = "";
                        m_signalTotal = 0.0;
                        if ( m_numProbes > m_maxNumProbes )
                        {
                            m_maxNumProbes = m_numProbes;
                        }
                        if ( m_args.verbose )
                        {
                            println( "${m_numProbes} probes" );
                        }
                    }
                    section = 0;
                }
                else if ( section == 2 )
                {
                    if ( checkData )
                    {
                        String[] columnNames = line.split( "\t", -1 );
                        m_numColumns = columnNames.size();
                        if ( m_numColumns < 2 )
                        {
                            reportError( "Bad file format: only " +
                                         m_numColumns +
                                         " columns in sample table" );
                            return false;
                        }
                        if ( columnNames[ 0 ] != "ID_REF" )
                        {
                            String msg = "Bad file format: first column (ID_REF)" +
                                    " name is '" + columnNames[ 0 ] + "'";
                            reportError( msg );
                            return false;
                        }
                        if ( columnNames[ 1 ] != "VALUE" )
                        {
                            String msg = "Bad file format: second column (VALUE)" +
                                    " name is '" + columnNames[ 1 ] + "'";
                            reportError( msg );
                            return false;
                        }
                    }
                    section = 3;
                }
                else if ( section == 3 )
                {
                    processSampleRow( line, lineNum, checkData, bulkLoadFile );
                }
				line = inputFile.readLine( );
			}
            if ( checkData )
            {
                m_numProbes = m_maxNumProbes;
                if ( m_args.verbose )
                {
                    println( "${lineNum} lines read" );
                    println( "${m_barcodes.size()} barcodes (samples)" );
                    println( "${m_numProbes} probes" );
                    println( "${m_numProbeErrors} probes with errors" );
                }
            }
		}
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open input file" );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading input file" );
            return false;
        }
		return true;
	}

//.............................................................................

    private
    boolean processSampleRow( String row, int lineNum,
                              boolean checkData, def bulkLoadFile )
    {
        String[] fields = row.split( "\t", -1 );
        if ( fields.size() != m_numColumns )
        {
            if ( checkData )
            {
                String err = "Wrong number of fields (" + fields.size() +
                        ", expected " + m_numColumns + ") in line " + lineNum;
                if ( fields.size() > 0 )
                {
                    err += ". Probe ID=" + fields[ 0 ];
                }
                m_errorList.add( err );
                ++m_numProbeErrors;
            }
            return false;
        }

        String probeId = fields[ 0 ];
        boolean probeValid = false;
        if ( m_validProbes.contains( probeId ) )
        {
            probeValid = true;
        }	
        if ( probeId.startsWith( "ILMN_" ) )
        {
            probeId = probeId.substring( "ILMN_".size() );
        }
        if ( (probeValid == false) && m_validProbes.contains( probeId ) )
        {
            probeValid = true;
        }
        if ( probeValid == false )
        {
            if ( checkData )
            {
                m_errorList.add( "Invalid probe ID: " + probeId );
                m_invalidProbes.add( probeId );
                ++m_numProbeErrors;
            }
            return false;
        }

        boolean probeError = false;

        Double signal = null;
        String field = fields[ 1 ];
        if ( field != "null" )
        {
            def format = new DecimalFormat();
            def pos = new ParsePosition( 0 );
            Number rawSignal = format.parse( field.toUpperCase(), pos );
            if ( (rawSignal == null) || (pos.getIndex() < field.size()) )
            {
                if ( checkData )
                {
                    String msg = "Invalid signal for sample " +
                            m_barcode + " for probe " + probeId + ": " + field +
                            " (line " + lineNum + ")";
                    m_errorList.add( msg );
                    probeError = true;
                }
            }
            signal = rawSignal.doubleValue();
            if ( checkData )
            {
                m_signalTotal += signal;
            }
        }

        if ( bulkLoadFile )
        {
            bulkLoadFile.print( m_arrayDataId );
            bulkLoadFile.print( bulkLoadFieldSeparator );
            bulkLoadFile.print( probeId );
            bulkLoadFile.print( bulkLoadFieldSeparator );
            bulkLoadFile.print( (signal != null) ? signal : "NULL" );
            bulkLoadFile.print( "\n" );
        }
        if ( checkData )
        {
            ++m_numProbes;
            if ( probeError )
            {
                ++m_numProbeErrors;
            }
        }

        return true;
    }

//=============================================================================

    private
    void reportErrorList( boolean asError )
    {
        String msg = "Errors on " + m_numProbeErrors + " probes.\n";
        int errmax = Math.min( m_errorList.size() - 1, 9 );
        msg += m_errorList[ 0..errmax ].join( "\n" );
		if ( asError )
		{
			setStatus( Status.ERROR, [ notes: msg, date_ended: new Date() ] );
		}
		else
		{
			setStatus( Status.WARNING, [ notes: msg ] );
		}

        def errorLog;
        try
        {
            errorLog = new PrintWriter( m_errorLogSpec );
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to create " + m_errorLogSpec );
            return;
        }
        errorLog.print( "Errors on " + m_numProbeErrors + " probes.\n" );
        for ( err in m_errorList )
        {
            errorLog.print( err );
            errorLog.print( "\n" );
        }
        errorLog.close( );
    }

//=============================================================================

    private
    boolean runBulkLoad( String bulkLoadFile )
    {
		setStatus( Status.IMPORTING,
				   [ no_probes: m_numProbes,
					 no_samples: m_barcodes.size() ] );
		try
		{
			String command =
					"LOAD DATA LOCAL INFILE '" + bulkLoadFile + "'" +
					" INTO TABLE array_data_detail" +
					" (array_data_id, affy_id, signal)";
			m_sql.execute( command );
            if ( m_sql.updateCount <= 0 )
			{
				reportError( "Unable to load " + bulkLoadFile +
							 " into array_data_detail" );
				return false;
			}
		}
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred loading " + bulkLoadFile +
						 " into array_data_detail:\n" +
						 exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean moveFile( String origSpec, String newSpec )
    {
        def origFile = new File( origSpec );
        def newFile = new File( newSpec );
        File newParentPath = newFile.getParentFile();
        if ( (newParentPath.exists() == false) &&
             (newParentPath.mkdirs( ) == false) )
        {
            reportError( "Unable to mkdir '${newParentPath}'" );
            return false;
        }
        if ( origFile.renameTo( newFile ) == false )
        {
            reportError( "Unable to move '${origSpec}' to '${newSpec}'" );
            return false;
        }
        return true;
    }

//-----------------------------------------------------------------------------

	private
	boolean deleteFile( String fileSpec, boolean okIfNonexistent )
	{
		def file = new File( fileSpec );
        if ( (file.exists() == false) && okIfNonexistent )
        {
            return true;
        }
		if ( file.delete( ) == false )
        {
            reportError( "Unable to delete '${fileSpec}'" );
            return false;
        }
        return true;
	}

//=============================================================================

	private enum Status { STARTING, IMPORTING, COMPLETE, ERROR, WARNING };
	private enum Format { DATASET_TABLE, SAMPLE_TABLE, UNKNOWN };

    private String m_genomicDataSourceName = "GEO"; //Fixed, for now!!!
	private String bulkLoadFieldSeparator = "\t"; //default for MySQL LOAD DATA

    private def m_args;
    private Sql m_sql;

	private String m_fileName;
    private String m_bulkLoadFileSpec;
    private String m_inputArchiveSpec;
    private String m_bulkLoadArchiveSpec;
    private String m_errorFileSpec;
	private String m_errorLogSpec;

	private static def ms_statuses = [:];

    private String m_gplId;
    private int m_chipTypeId;
    private String m_probeListTable;
    private String m_probeListColumn;

    private int m_genomicDataSourceId;
    private int m_chipsLoadedId = 0;

	private Format m_format = Format.UNKNOWN;
    private int m_numColumns;
    private def m_barcodes;

    private def m_validProbes;

    private int m_numProbes;
    private int m_maxNumProbes;
    private def m_errorList;
    private int m_numProbeErrors;
    private def m_invalidProbes;
    private String m_barcode;
    private double m_signalTotal;
    private int m_arrayDataId;

//-----------------------------------------------------------------------------
}                                                  //SoftExpressionDataImporter


//*****************************************************************************
