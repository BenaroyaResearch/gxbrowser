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
	   argName: "backSubFileName",
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

def importer = new BackgroundSubtractedDataImporter( importerArgs, sql );
importer.importData( );


//*****************************************************************************


private
class BackgroundSubtractedDataImporter
{
//-----------------------------------------------------------------------------

    BackgroundSubtractedDataImporter( def args, Sql sql )
    {
        m_args = args;
        m_sql = sql;
    }

//=============================================================================

    boolean importData( )
    {
        int numProbeErrorsThreshold = 0;

		if ( populateStatuses( ) &&
             processSpec( m_args.inputFileSpec ) &&
             queryChipType( ) &&
             queryGenomicDataSource( ) &&
             createChipsLoadedRecord( ) &&
             readHeader( ) &&
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
    void setStatus( Status status, fields )
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
	boolean processSpec( inputFileSpec )
	{
        //Expects a spec of the form
        // baseDir/toBeImported/{chipType}/{filename}
		String[] specParts = inputFileSpec.split( "/" );
		int numParts = specParts.size();
		if ( numParts < 4 )
		{
            reportError( "Unable to parse '${m_args.inputFileSpec}'" );
			return false;
		}
		m_fileName = specParts[ -1 ];
		m_chipTypeDirName = specParts[ -2 ];
		String midDir = specParts[ -3 ];
        String baseDir = specParts[ 0..-4 ].join( "/" );
		String[] nameParts = m_fileName.split( /\./ );
		String baseName = (nameParts.size() < 2)  ?  m_fileName  :
				nameParts[ 0..-2 ].join( "." );

        m_bulkLoadFileSpec = [ baseDir, midDir, m_chipTypeDirName,
							   baseName + "_bulkLoad.txt" ].join( "/" );
        m_inputArchiveSpec = [ baseDir, "importedFiles", m_chipTypeDirName,
                               m_fileName ].join( "/" );
        m_bulkLoadArchiveSpec = [ baseDir, "importedFiles", m_chipTypeDirName,
                                  baseName + "_bulkLoad.txt" ].join( "/" );
        m_errorFileSpec = [ baseDir, "errors", m_fileName ].join( "/" );
        m_errorLogSpec = [ baseDir, "errors",
						   baseName + "_errors.txt" ].join( "/" );
        
		return true;
	}

//=============================================================================

    private
    boolean queryChipType( )
    {
        try
        {
            String query = """SELECT id, probe_list_table, probe_list_column
                              FROM chip_type WHERE import_directory_name = ?""";
            def chipType = m_sql.firstRow( query, [ m_chipTypeDirName ] );
            if ( chipType == null )
            {
                reportError( "No chip_type with import_directory_name " +
                             "'${m_chipTypeDirName}'" );
                return;
            }
            m_chipTypeId = chipType.id;
            m_probeListTable = chipType.probe_list_table;
            m_probeListColumn = chipType.probe_list_column;

            if ( ! m_probeListTable )
            {
                reportError( "No probeListTable listed for chip type " +
                             "'${m_chipTypeDirName}'" );
                return false;
            }
            if ( ! m_probeListColumn )
            {
                reportError( "No probeListColumn listed for chip type" +
                             "'${m_chipTypeDirName}'" );
                return false;
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
    boolean readHeader( )
    {
        m_headerLine = 1;
        String headerString;
        try
        {
            def inputFile =
                    new BufferedReader( new FileReader( m_args.inputFileSpec ) );
            headerString = inputFile.readLine( );
            while ( (headerString != null) && (headerString.size() == 0) )
            {
                ++m_headerLine;
                headerString = inputFile.readLine( );
            }
            inputFile.close( );
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open input file" );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading input" );
            return false;
        }
        if ( headerString == null )
        {
            reportError( "Bad file format: No non-blank line found" );
            return false;
        }
		
		if ( parseColumnHeader( headerString ) == false )
		{
			return false;
		}

        if ( verifyBarcodes( ) == false )
        {
            return false;
        }

        return true;
    }

//.............................................................................

	private
	boolean parseColumnHeader( String headerString )
	{
        String[] columnNames = headerString.split( "\t", -1 );
        m_numColumns = columnNames.size();
        if ( m_numColumns == 0 )
        { //This should be impossible at this point, but just in case...
            reportError( "Bad file format: No column names found" );
            return false;
        }

        if ( (probeIdNames.find { it == columnNames[ 0 ].toUpperCase() })
			 == null )
        {
            String msg = "Bad file format: first column (probe ID) name is '" +
                    columnNames[ 0 ] + "'";
            reportError( msg );
            return false;
        }

        m_barcodes = [];
        String lastBarcode = "";
        def barcodeInfo = [:];
        for ( int i = 0; i < m_numColumns; ++i )
        {
            Matcher m = (columnNames[ i ] =~ sampleColumnNamePattern);
            if ( m )
            {
                if ( (m.count != 1) || (m[ 0 ].size() != 3) )
                {   //This would be an error in our code
                    String msg = "Error evaluating regular expression on '" +
                            columnNames[ i ] + "' (column " + i + ")";
                    reportError( msg );
                    return false;
                }
                String barcode = m[ 0 ][ 1 ];
                if ( barcode != lastBarcode )
                {
                    if ( lastBarcode != "" )
                    {
                        barcodeInfo.lastColumn = i - 1;
                        m_barcodes.add( barcodeInfo );
                        barcodeInfo = [:];
                    }
                    for ( bc in m_barcodes )
                    {
                        if ( bc.barcode == barcode )
                        {
                            String msg = "Barcode " + barcode + "listed twice" +
                                    " at " + bc.firstColumn + "-" +
                                    bc.lastColumn + " and at " + i
                            reportError( msg );
                            return false;
                        }
                    }
                    barcodeInfo.barcode = lastBarcode = barcode;
                    barcodeInfo.firstColumn = i;
                }
                String dataTypeName = m[ 0 ][ 2 ];
                if ( (signalColumnNames.find { it == dataTypeName }) != null )
                {
					if ( barcodeInfo.signalColumn != null )
					{
						String msg = "Multiple signal columns (" +
								barcodeInfo.signalColumn +
								" and " + i + ") for barcode " +
								barcodeInfo.barcode
						reportError( msg );
						return false;
					}
					barcodeInfo.signalColumn = i;
                }
                if ( (detectionColumnNames.find { it == dataTypeName }) != null )
                {
					if ( barcodeInfo.detectionColumn != null )
					{
						String msg = "Multiple detection columns (" +
								barcodeInfo.detectionColumn +
								" and " + i + ") for barcode " +
								barcodeInfo.barcode
						reportError( msg );
						return false;
					}
					barcodeInfo.detectionColumn = i;
                }
            }
            else
            {
                if ( lastBarcode != "" )
                {
                    barcodeInfo.lastColumn = i - 1;
                    m_barcodes.add( barcodeInfo );
                    lastBarcode = "";
                    barcodeInfo = [:];
                }
            }
        }
        if ( lastBarcode != "" )
        {
            barcodeInfo.lastColumn = i - 1;
            m_barcodes.add( barcodeInfo );
            lastBarcode = "";
            barcodeInfo = [:];
        }
		return true;
	}

//.............................................................................

    private
    boolean verifyBarcodes( )
    {
        if ( m_barcodes.size() == 0 )
        {
            reportError( "No sample barcodes" );
            return false;
        }
        int colsPerBarcode = -1;
        int signalColOffset = -1;
        int detectionColOffset = -1;
        for ( bc in m_barcodes )
        {
            if ( bc.barcode == null )
            {   //This would be an error in our code
                String msg = "No barcode for columns " +
                        bc.firstColumn + "-" + bc.lastColumn;
                reportError( msg );
                return false;
            }
            if ( bc.firstColumn == null )
            {   //This would be an error in our code
                reportError( "No first column for barcode " + bc.barcode );
                return false;
            }
            if ( bc.lastColumn == null )
            {   //This would be an error in our code
                reportError( "No last column for barcode " + bc.barcode );
                return false;
            }
            int colsInBarcode = bc.lastColumn - bc.firstColumn + 1;
            if ( ! (colsInBarcode > 0) )
            {   //This would be an error in our code
                String msg = "Last col (" + bc.lastColumn + ") < first col (" +
                        bc.firstColumn + ") for barcode " + bc.barcode;
                reportError( msg );
                return false;
            }
            if ( colsPerBarcode == -1 )
            {
                colsPerBarcode = colsInBarcode;
            }
            else if ( colsInBarcode != colsPerBarcode )
            {
                String msg = "Barcode " + barcode + " has " +
                        colsInBarcode + " columns; other barcodes had " +
                        colsPerBarcode;
                reportError( msg );
                return false;
            }
            if ( bc.signalColumn == null )
            {
                reportError( "No signal column for barcode " + bc.barcode );
                return false;
            }
            if ( bc.detectionColumn == null )
            {
                reportError( "No detection column for barcode " + bc.barcode );
                return false;
            }
            if ( (bc.signalColumn < bc.firstColumn)
                 || (bc.signalColumn > bc.lastColumn) )
            {   //This would be an error in our code
                String msg = "Signal column (" + bc.signalColumn +
                        " not between " + bc.firstColumn +
                        " and " + bc.lastColumn;
                reportError( msg );
                return false;
            }
            if ( (bc.detectionColumn < bc.firstColumn)
                 || (bc.detectionColumn > bc.lastColumn) )
            {   //This would be an error in our code
                String msg = "Detection column (" + bc.detectionColumn +
                        " not between " + bc.firstColumn +
                        " and " + bc.lastColumn;
                reportError( msg );
                return false;
            }
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
        def bulkLoadFieldSeparator = "\t"; //default for MySQL LOAD DATA

		double negativeSignalErrorThreshold = 0.0;
		double negativeSignalWarningThreshold = 0.15;

        def inputFile;
        try
        {
            inputFile = new BufferedReader(
                new FileReader( m_args.inputFileSpec ) );
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open input file" );
            return false;
        }

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
            m_errorList = [];
            m_numProbeErrors = 0;
            m_invalidProbes = [];
            m_numNegativeSignals = 0;
			for ( bc in m_barcodes )
			{
				bc.signalTotal = 0.0;
			}
        }
        m_numProbes = 0;

        try
        {
            m_headerLine.times
            {   //skip past header
                inputFile.readLine( );
            }

            int rowNum = m_headerLine;
            while ( true )
            {
                String probeRow = inputFile.readLine( );
                if ( probeRow == null )
                    break;
                ++rowNum;

                String[] fields = probeRow.split( "\t", -1 );
                if ( fields.size() != m_numColumns )
                {
                    if ( checkData )
                    {
                        String err = "Wrong number of fields (" +
                                fields.size() + ", expected " +
                                m_numColumns + ") in row " + rowNum;
                        if ( m_numColumns > 0 )
                        {
                            err += ". Probe ID=" + fields[ 0 ];
                        }
                        m_errorList.add( err );
                        ++m_numProbeErrors;
                    }
                    continue;
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
                    continue;
                }

                boolean probeError = false;
                for ( bc in m_barcodes )
                {
                    String field = fields[ bc.signalColumn ];
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
					double signal = rawSignal.doubleValue();

                    field = fields[ bc.detectionColumn ];
                    pos.setIndex( 0 );
                    Number rawDetection =
							format.parse( field.toUpperCase(), pos );
                    if ( (rawDetection == null) ||
						 (pos.getIndex() < field.size()) )
                    {
                        if ( checkData )
                        {
                            String msg = "Invalid detection for sample " +
                                    bc.barcode + " for probe " + probeId +
									": " + field;
                            m_errorList.add( msg );
                        }
                        continue;
                    }
					double detection = rawDetection.doubleValue();
					if ( (detection < 0.0) || (detection > 1.0) )
                    {
                        if ( checkData )
                        {
                            String msg = "Invalid detection for sample " +
                                    bc.barcode + " for probe " + probeId +
									": " + field;
                            m_errorList.add( msg );
                            probeError = true;
                        }
                        continue;
                    }

                    if ( checkData )
                    {
						bc.signalTotal += signal;
						if ( signal < 0.0 )
							++m_numNegativeSignals;
                    }

                    if ( createBulkLoadFile )
                    {
                        bulkLoadFile.print( bc.arrayDataId );
                        bulkLoadFile.print( bulkLoadFieldSeparator );
                        bulkLoadFile.print( probeId );
                        bulkLoadFile.print( bulkLoadFieldSeparator );
                        bulkLoadFile.print( signal );
                        bulkLoadFile.print( bulkLoadFieldSeparator );
                        bulkLoadFile.print( detection );
                        bulkLoadFile.print( "\n" );
                    }
                }
                if ( checkData && probeError )
                {
					++m_numProbeErrors;
                }
                ++m_numProbes;
            }

            if ( checkData )
            {
				double totalValidSamples = m_numProbes * m_barcodes.size();
                double negFraction = (totalValidSamples > 0)  ?
				m_numNegativeSignals / totalValidSamples  :  0;
                String msg =  m_numNegativeSignals +
						" negative signals found (" +
                        negFraction.round( 3 ) + " of all valid sample probes)";
				if ( negFraction <= negativeSignalErrorThreshold )
				{
					reportError( msg );
					return false;
				}
				if ( negFraction <= negativeSignalWarningThreshold )
				{
					m_errorList.add( msg );
				}
            }
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading input file" );
            return false;
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
				   [ no_probes: m_numProbes, no_samples: m_barcodes.size() ] );
		try
		{
			String command =
					"LOAD DATA LOCAL INFILE '" + bulkLoadFile + "'" +
					" INTO TABLE array_data_detail" +
					" (array_data_id, affy_id, signal, detection)";
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

    private String m_genomicDataSourceName = "Benaroya"; //Fixed, for now

    private final String[] probeIdNames = [ "PROBE_ID", "PROBEID" ];
    private final Pattern sampleColumnNamePattern = ~/^(\d+[^\.]*)\.(.+)$/;
    private final String[] signalColumnNames =
            [ "AVG_Signal", "signal" ];
    private final String[] detectionColumnNames =
            [ "Detection Pval", "detection" ];

	private static def ms_statuses = [:];

    private def m_args;
    private Sql m_sql;

	private String m_chipTypeDirName;
	private String m_fileName;

    private String m_bulkLoadFileSpec;
    private String m_inputArchiveSpec;
    private String m_bulkLoadArchiveSpec;
    private String m_errorFileSpec;
    private String m_errorLogSpec;

    private int m_chipTypeId;
    private String m_probeListTable;
    private String m_probeListColumn;

    private int m_genomicDataSourceId;
    private int m_chipsLoadedId = 0;

    private int m_headerLine;
    private int m_numColumns;
    private def m_barcodes;

    private def m_validProbes;

    private def m_errorList;
    private int m_numProbeErrors;
    private def m_invalidProbes;
    private int m_numProbes;
    private int m_numNegativeSignals;

	private def m_signalSubtotals;

//-----------------------------------------------------------------------------
}


//*****************************************************************************
