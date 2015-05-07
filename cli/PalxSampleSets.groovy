#!/usr/bin/groovy

import groovy.sql.Sql;
import java.sql.SQLException;

String defaultDbHost = "localhost";
String altDbHost = "srvdm";
String defaultDb = "dm3";
String defaultDbUser = "browser";
String defaultDbPassword = "bibliome";

String defaultBaseUrl = "http://localhost:8080/dm3"

CliBuilder cli = new CliBuilder( usage: "PalxSampleSets.groovy [options]" );
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
cli.w( longOpt: "baseUrl",
	   required: false,
	   args: 1,
	   argName: "baseUrl",
	   "Base URL for DM3 Web service" );
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

def sql =
       Sql.newInstance( "jdbc:mysql://${dbHost}/${db}?useServerPrepStmts=false",
                        dbUser, dbPassword,
                        "com.mysql.jdbc.Driver");
if ( sql == null )
{
    println( "Unable to get Sql connection with data source ${dataSource}" );
    return;
}

String baseUrl = clArgs.w ?: defaultBaseUrl;


List< Integer > sampleSetsWithoutPalx = getSampleSetsWithoutPalx( sql );

sampleSetsWithoutPalx.each { ssId ->
    generatePalx( ssId, baseUrl );
}

//=============================================================================

List< Integer > getSampleSetsWithoutPalx( Sql sql )
{
    try
    {
        String query =
                "SELECT id FROM sample_set" +
                "  WHERE default_palx_id IS NULL";
        def rows = sql.rows( query );
        return rows*.id;
    }
    catch ( SQLException exc )
    {
        println( "SQL exception: \n" + exc );
        return [];
    }
}

//=============================================================================

void generatePalx( long ssId, String baseUrl )
{
    println( "Processing sample set " + ssId );
    URL url = new URL( baseUrl + "/sampleSet/calcPalx?sampleSetId=" + ssId );
    String response = url.getText();
    println( response );
}


//*****************************************************************************
