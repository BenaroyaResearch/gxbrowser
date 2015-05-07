#!/usr/bin/groovy
/*
  ExpAnnot.groovy

  Script for importing gene expression probe annotation files.
*/
package org.sagres.dm

import java.util.regex.Pattern;
import groovy.sql.Sql;
import java.sql.SQLException;


//*****************************************************************************

String defaultDbHost = "localhost";
String altDbHost = "srvdmdev";
String defaultDb = "dm_dev";
String defaultDbUser = "browser";
String defaultDbPassword = "bibliome";

def cli = new CliBuilder( usage: "ExpMan.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
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
cli.c( longOpt: "chip",
	   required: true,
	   args: 1,
	   argName: "chipName",
       "Chip name (as given in chip_type table)" );
cli.a( longOpt: "annotation",
	   required: true,
	   args: 1,
	   argName: "annotationFileName",
       "Annotation file name" );
cli.t( longOpt: "table",
	   required: true,
	   args: 1,
	   argName: "probeDefsTableName",
       "Name of new DB table for probe defs" );
cli.f( longOpt: "column",
	   required: false,
	   args: 1,
	   argName: "probeDefsColumnName",
       "Name of Probe ID column (field) in new table [default depends on manufacturer]" );
cli.r( longOpt: "replaceTable",
       "Replace probe defs table if it already exists" );
cli.l( longOpt: "listColumns",
       "Just list columns" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

def importerArgs = [:];
importerArgs.annotationFileSpec = clArgs.a;
importerArgs.chipName = clArgs.c;
importerArgs.probeDefsTableName = clArgs.t;
importerArgs.probeIdColumnName = clArgs.f ? clArgs.f.toLowerCase() : "";
importerArgs.verbose = (clArgs.v == true);
importerArgs.justGetColumns = (clArgs.l == true);
importerArgs.replaceTableIfExists = (clArgs.r == true);

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

def importer = new ExpressionAnnotationImporter( importerArgs, sql );
importer.importProbes( );


//*****************************************************************************


private
class ExpressionAnnotationImporter
{                                                //ExpressionAnnotationImporter
//-----------------------------------------------------------------------------

    ExpressionAnnotationImporter( def args, Sql sql )
    {
        m_args = args;
        m_sql = sql;
    }

//=============================================================================

	boolean importProbes( )
	{
		if ( ! getChipManufacturer( ) )
			return false;
        if ( ! extractProbesSection( ) )
            return false;
        if ( ! createProbeDefsTable( ) )
            return false;
        if ( ! loadProbeDefs( ) )
            return false;
        if ( ! setChipTypeTableAndColumn( ) )
            return false;
		if ( ! deleteProbesSectionFile( ) )
			return false;
		return true;
	}

//=============================================================================

	private
	void reportError( String message )
	{
		println( message );
	}

//=============================================================================

	private
	boolean getChipManufacturer( )
	{
        try
        {
            String query = "SELECT cd.manufacturer" +
                    " FROM chip_data AS cd JOIN chip_type AS ct" +
                    " WHERE cd.id = ct.chip_data_id AND ct.name = ?";
            def chipType = m_sql.firstRow( query, [ m_args.chipName ] );
            if ( chipType == null )
            {
                reportError( "No chip_type with name " +
                             "'${m_args.chipName}'" );
                return;
            }
            m_manufacturer = chipType.manufacturer;
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error occurred reading chip_type:\n" +
						 exc.message );
            return false;
        }
		if ( ! setProbeIdColumnName( ) )
			return false;
        return true;
	}

//-----------------------------------------------------------------------------

	private
	boolean setProbeIdColumnName( )
	{
		m_probeIdColumnName = m_args.probeIdColumnName;
		if ( ! m_probeIdColumnName )
		{
			if ( m_manufacturer == "Affymetrix" )
			{
				m_probeIdColumnName = "probe_set_id";
			}
			if ( m_manufacturer == "Illumina" )
			{
				m_probeIdColumnName = "probe_id";
			}
			if ( m_manufacturer == "Phalanx" )
			{
				m_probeIdColumnName = "phalanx_id";
			}
		}
		if ( ! m_probeIdColumnName )
			return false;
		return true;
	}

//=============================================================================

    //N.B.: This reads the Columns section (at the end of the annotation,
    // at least in HT12 V4), but that seems to be less useful than the first
    // line of the Probes section, as it does not match the number of fields
    // seen in the probes records.
	private
	boolean getIlluminaColumnList( )
	{
		def file = new File( m_args.annotationFileSpec );
		if ( file == null )
		{
			reportError( "Unable to open " + m_args.annotationFileSpec );
			return false;
		}
		boolean readingColumnLines = false;
        Pattern sectionStartPattern = ~/^\[.+\]/; //starts with "[something]"
        try
        {
            file.eachLine
            { line ->
                if ( readingColumnLines == false )
                {
                    if ( line.startsWith( "[Columns]" ) )
                    {
                        readingColumnLines = true;
                    }
                }
                else
                {
                    if ( sectionStartPattern.matcher( line ) )
                    {
                        throw GOOD_BREAK;
                    }
                    else
                    {
                        def fields = line.split( "\t", -1 );
                        if ( fields.size() < 2 )
                        {
                            reportError( "Column def line has only " +
                                         fields.size() + " fields" );
                            throw BAD_BREAK;
                        }
                        if ( (fields[ 1 ] == "probe")
                             || (fields[ 1 ] == "all") )
                        {
                            m_columns.add( [ name: fields[ 0 ], size: 0 ] );
                        }
                    }
                }
            }
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open " + m_args.annotationFileSpec );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading " +
                         m_args.annotationFileSpec );
            return false;
        }
        catch ( Exception e )
        {
            if ( e != GOOD_BREAK )
            {
                if ( e != BAD_BREAK )
                {
                    throw e;
                }
                return false;
            }
        }
		return true;
	}

//=============================================================================

	private
	boolean extractProbesSection( )
    {
		boolean affymetrix = (m_manufacturer == "Affymetrix");
		boolean illumina = (m_manufacturer == "Illumina");
        boolean phalanx = (m_manufacturer == "Phalanx");
		if ( (! affymetrix) && (! illumina) && (! phalanx) )
		{
			reportError( "Unknown manufacturer" );
			return false;
		}
		def inFile = new File( m_args.annotationFileSpec );
		if ( inFile == null )
		{
			reportError( "Unable to open " + m_args.annotationFileSpec );
			return false;
		}
        m_probesFileSpec = m_args.annotationFileSpec + "_probes";
        def outFile = new File( m_probesFileSpec );
        try
        {
            outFile.withWriter
            { writer ->
                boolean readingProbeLines = (affymetrix || phalanx);
                boolean readingHeader = true;
                Pattern illuminaSectionStartPattern = ~/^\[.+\]/;
                inFile.eachLine
                { line, lineNum ->
                    if ( readingProbeLines == false )
                    {
						if ( illumina &&
							 line.startsWith( "[Probes]" ) )
                        {
                            readingProbeLines = true;
                        }
                    }
                    else
                    {
						if ( affymetrix &&
							 line.startsWith( "#" ) )
						{
							return; //continue to next line
						}
                        if ( illumina &&
							 illuminaSectionStartPattern.matcher( line ) )
                        {
                            throw GOOD_BREAK; //end of Probes section
                        }
                        def fields;
						if ( affymetrix )
						{
							fields = splitCsvRow( line );
						}
						if ( illumina || phalanx )
						{
							fields = line.split( "\t", -1 );
						}
                        if ( readingHeader )
                        {
                            readingHeader = false;
                            for ( String field : fields )
                            {
                                String name = field.toLowerCase();
                                name = name.replaceAll( /[ ()]/, "_" );
                                m_columns.add( [ name: name, size: 0 ] );
                            }
                            m_probeIdColumnIndex = m_columns.findIndexOf
                            {
                                it.name == m_probeIdColumnName;
                            }
                            if ( (m_probeIdColumnIndex < 0)
								 && (m_args.justGetColumns == false) )
                            {
                                println( m_probeIdColumnName +
                                         " not found in column names" );
                                throw BAD_BREAK;
                            }
                        }
                        else
                        {
                            if ( fields.size() != m_columns.size() )
                            {
                                reportError( "Probe line " + lineNum + " has " +
                                             fields.size() + " fields" );
                                for ( int i = 0;
                                      i < Math.max( fields.size(),
                                                    m_columns.size() );
                                      ++i )
                                {
                                    println( "    " +
                                             (i < m_columns.size() ?
                                              m_columns[ i ].name : "   ") +
                                             ": " +
                                             (i < fields.size() ?
                                              fields[ i ] : "") );
                                }
                                throw BAD_BREAK;
                            }
                            if ( m_args.justGetColumns == false )
                            {
								if ( illumina )
								{
									fields[ m_probeIdColumnIndex ] =
											fields[ m_probeIdColumnIndex ].
									                replace( "ILMN_", "" );
								}
                                writer.writeLine( fields.join( "\t" ) );
                            }

                            fields.eachWithIndex
                            { field, i ->
                                if ( field.size() > m_columns[ i ].size )
                                {
                                    m_columns[ i ].size = field.size();
                                }
                            }
                        }
                    }
                }
            }
        }
        catch ( FileNotFoundException exc )
        {
            reportError( "Unable to open " + m_args.annotationFileSpec );
            return false;
        }
        catch ( IOException exc )
        {
            reportError( "I/O error occurred reading " +
                         m_args.annotationFileSpec );
            return false;
        }
        catch ( Exception e )
        {
            if ( e != GOOD_BREAK )
            {
                if ( e != BAD_BREAK )
                {
                    throw e;
                }
                return false;
            }
        }
        if ( m_args.verbose || m_args.justGetColumns )
        {
            println( "Columns: " );
            for ( def column : m_columns )
            {
                println( "   " + column.name +
                         " [" + column.size + "]" );
            }
            println( );
            println( "Using column " + m_probeIdColumnIndex +
                     " (" + m_columns[ m_probeIdColumnIndex ].name +
                     ") for Probe ID" );
            println( );
            if ( m_args.justGetColumns )
            {
                return false; //not error, but no more processing desired
            }
        }
        return true;
    }

//-----------------------------------------------------------------------------

	private
	def splitCsvRow( String row )
	{
		def parts = [];
		String curPart = "";
		boolean inQuotes = false;
		int i = 0;
		while ( i < row.size() )
		{
			if ( row[ i ] == '"' )
			{
				if ( inQuotes && (i < row.size() - 1) && (row[ i + 1 ] == '"') )
				{
					//doubled quotes: keep the second
					++i;
					curPart += row[ i ];
					++i;
				}
				else
				{
					inQuotes = ! inQuotes;
					++i;
				}
			}
			else if ( (row[ i ] == ',') && (inQuotes == false) )
			{
				parts.add( curPart );
				curPart = "";
				++i;
			}
			else
			{
				curPart += row[ i ];
				++i;
			}
		}
		parts.add( curPart );
		return parts;
	}

//=============================================================================

    private
    boolean createProbeDefsTable( )
    {
        if ( m_args.replaceTableIfExists )
        {
            try
            {
                String command = "DROP TABLE IF EXISTS " +
                        m_args.probeDefsTableName;
                if ( m_args.verbose )
                {
                    println( "SQL command:" );
                    println( command );
                    println( );
                }
                m_sql.execute( command );
            }
            catch ( SQLException exc )
            {
                reportError( "SQL error dropping " + m_args.probeDefsTableName +
                             ":\n" + exc.message );
                return false;
            }
        }

        try
        {
            String command = "CREATE TABLE " + m_args.probeDefsTableName +
                    " ( " +
                    "id INT NOT NULL PRIMARY KEY AUTO_INCREMENT";
            for ( def column : m_columns )
            {
                command += ", `" + column.name + "` ";
                if ( column.size < 250 )
                {
                    command += "VARCHAR( 255 )";
                }
                else if ( column.size < 5000 )
                {
					int colsize = 500 * (((column.size + 600) / 500) as int);
                    command += "VARCHAR( $colsize )";
                }
                else if ( column.size < 65000 )
                {
                    command += "TEXT";
                }
                else
                {
                    command += "MEDIUMTEXT"; //16M should be plenty!
                }
            }
            command += " )";
            if ( m_args.verbose )
            {
                println( "SQL command:" );
                println( command );
                println( );
            }
            
            m_sql.execute( command );
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error creating " + m_args.probeDefsTableName +
                         ":\n" + exc.message );
            return false;
        }
        return true;
    }
    
//=============================================================================

    private
    boolean loadProbeDefs( )
    {
        try
        {
            def columns = m_columns*.name.collect { "`" + it + "`" }
            String command = "LOAD DATA LOCAL INFILE '" +
                    m_probesFileSpec + "'" +
                    " INTO TABLE " + m_args.probeDefsTableName +
                    " ( " + columns.join( ", " ) + " )";
            if ( m_args.verbose )
            {
                println( "SQL command:" );
                println( command );
                println( );
            }
            m_sql.execute( command );
            if ( m_sql.updateCount <= 0 )
			{
				reportError( "Unable to load " + m_probesFileSpec +
							 " into " + m_args.probeDefsTableName );
				return false;
			}
        }
        catch ( SQLException exc )
        {
            reportError( "SQL error loading " + m_probesFileSpec +
                         " into " + m_args.probeDefsTableName +
                         ":\n" + exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

    private
    boolean setChipTypeTableAndColumn( )
    {
        String chipTypeTableName = "chip_type";
        try
        {
            String command = "UPDATE " + chipTypeTableName +
                    " SET probe_list_table = ?, " +
                    " probe_list_column = ?" +
                    " WHERE name = ?";
            def sqlArgs = [ m_args.probeDefsTableName,
                            m_probeIdColumnName,
                            m_args.chipName ];
            if ( m_args.verbose )
            {
                println( "SQL command:" );
                println( command );
                println( "    args: " + sqlArgs );
                println( );
            }
            m_sql.execute( command, sqlArgs );
            if ( m_sql.updateCount != 1 )
            {
                reportError( "Unable to update " + chipTypeTableName );
                return false;
            }
        }        
        catch ( SQLException exc )
        {
            reportError( "SQL error updating " + chipTypeTableName +
                         ":\n" + exc.message );
            return false;
        }
        return true;
    }

//=============================================================================

	private
	boolean deleteProbesSectionFile( )
	{
		def file = new File( m_probesFileSpec );
		return file.delete( );
	}

//=============================================================================

    private def m_args;
    private Sql m_sql;
	private String m_manufacturer;
    private def m_columns = [];
	private String m_probeIdColumnName;
    private String m_probesFileSpec;
    private int m_probeIdColumnIndex = -1;
    private static BAD_BREAK = new Exception(); //To break out of loops calling
    private static GOOD_BREAK = new Exception(); //closures.

//-----------------------------------------------------------------------------
}                                                //ExpressionAnnotationImporter


//*****************************************************************************
