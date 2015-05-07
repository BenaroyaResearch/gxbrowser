#!/usr/bin/groovy
/*
  GetSoftPlatform.groovy

  Script for determining the platform(s) for a Soft expression data file.
*/
package org.sagres.dm

import groovy.sql.Sql;
import java.sql.SQLException;


//*****************************************************************************

String defaultDbHost = "localhost";
String altDbHost = "srvdev";
String defaultDb = "dm3";
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
cli.f( longOpt: "file",
	   required: true,
	   args: 1,
	   argName: "softFileName",
       "Soft file name" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

String dbHost = clArgs.h ?: defaultDbHost;
String db = clArgs.d ?: defaultDb;
String dbUser = clArgs.u ?: defaultDbUser;
String dbPassword = clArgs.p ?: defaultDbPassword;

Sql sql =
       Sql.newInstance( "jdbc:mysql://${dbHost}/${db}?useServerPrepStmts=false",
                        dbUser, dbPassword,
                        "com.mysql.jdbc.Driver");
if ( sql == null )
{
    println( "Unable to get Sql connection with data source ${dataSource}" );
    return;
}

getSoftPlatformInfo( clArgs.f, sql );

//=============================================================================

void getSoftPlatformInfo( String fileSpec, Sql sql )
{
    List< String > gplIds = getGplIds( fileSpec );
    if ( gplIds == null )
        return;
    gplIds.each { gplId ->
        print( gplId + ":" );
        Map info = getPlatformInfo( sql, gplId );
        if ( info == null )
        {
            println( " (Unknown)" );
        }
        else
        {
            println ( " " + info.manufacturer +
                      " " + info.model +
                      " " + (info.version  ?  "v" + info.version  :  "") );
        }
    }
}

//=============================================================================

List< String > getGplIds( String fileSpec )
{
    if ( fileSpec =~ /.*GSE\d*_family[^\/]*/ )
    {
        return getValues( fileSpec, "!Series_platform_id", "^SERIES" );
    }
    else if ( fileSpec =~ /.GDS\d*_full[^\/]*/ )
    {
        return getValues( fileSpec, "!dataset_platform", "^DATASET" );
    }
    else
    {
        println( "Filename does not match any expected type" );
        return null;
    }
}

//=============================================================================

List< String > getValues( String fileSpec, String key, String section )
{
    try
    {
        List< String > values = [];
        def inputFile = new BufferedReader(
            new FileReader( fileSpec ) );
        int state = 0; //1=in series section, 2=past series section
        int lineNum = 0;
        String line = inputFile.readLine( );
        String keyRegExp = /^/ + key + /\s*=/;
        while ( (line != null) && (state < 2) )
        {
            ++lineNum;
            if ( (state == 0) && line.startsWith( section ) )
            {
                state = 1;
            }
            else if ( state == 1 )
            {
                if ( line.startsWith( "^" ) &&
                     (line.startsWith( section ) == false) )
                {
                    state = 2;
                }
                else if ( line =~ keyRegExp )
                {
                    def parts = splitInTwo( line, "=" );
                    if ( parts.size() < 2 )
                    {
                        reportError( "No value for " + key +
                                     " on line " + lineNum );
                        return null;
                    }
                    values.add( parts[ 1 ] );
                }
            }

            line = inputFile.readLine( );
        }
        return values;
    }
    catch ( FileNotFoundException exc )
    {
        reportError( "Unable to open input file" );
        return null;
    }
    catch ( IOException exc )
    {
        reportError( "I/O error occurred reading input file" );
        return null;
    }
}

//-----------------------------------------------------------------------------

List< String > splitInTwo( String string, String separator )
{
    List< String > parts = [];
    List< String > rawParts = string.split( separator, -1 );
    if ( rawParts.size() > 0 )
    {
        parts.add( rawParts[ 0 ].trim() );
    }
    if ( rawParts.size() > 1 )
    {
        parts.add( rawParts[ 1..-1 ].join( separator ).trim() );
    }
    return parts;
}

//=============================================================================

def getPlatformInfo( Sql sql, String gplId )
{
    try
    {
        String query =
                "SELECT cd.manufacturer, cd.model, cd.chip_version" +
                "  FROM gpl_chip_type AS gct" +
                "  JOIN chip_type AS ct ON ct.id = gct.chip_type_id" +
                "  JOIN chip_data AS cd ON cd.id = ct.chip_data_id" +
                "  WHERE gct.gpl_id = ?";
        def row = sql.firstRow( query, [ gplId ] );
        if ( row == null )
            return null;
        Map platformInfo = [:];
        platformInfo.manufacturer = row.manufacturer;
        platformInfo.model = row.model;
        platformInfo.version = row.chip_version;
        return platformInfo;
    }
    catch ( SQLException exc )
    {
        println( "SQL error occurred reading gpl_chip_type:\n" +
                     exc.message );
        return null;
    }
}

//*****************************************************************************
