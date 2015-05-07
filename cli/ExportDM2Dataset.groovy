#!/usr/bin/groovy

import groovy.sql.Sql;
import groovy.sql.GroovyRowResult;
import java.sql.SQLException;

String defaultDbHost = "srvdm";
String defaultDb = "dm2";
String defaultDbUser = "browser";
String defaultDbPassword = "bibliome";
String defaultDirectory = "./";

CliBuilder cli = new CliBuilder( usage: "ExportDM2Dataset.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.s( longOpt: "dataset",
	   required: true,
	   args: 1,
	   argName: "dataset",
       "Dataset ID" );
cli.h( longOpt: "host",
	   required: (! defaultDbHost),
	   args: 1,
	   argName: "dbHost",
       "Database host [default: ${defaultDbHost}" );
cli.d( longOpt: "database",
	   required: (! defaultDb),
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
cli.f( longOpt: "folder",
       required: (! defaultDirectory),
       args: 1,
       argName: "directory",
       "Directory" +
	   (defaultDirectory ? " [default: ${defaultDirectory}]" : "") );
cli.n( longOpt: "noExpression",
       required: false,
       args: 0,
       "No expression data" );

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

String directory = clArgs.f ?: defaultDirectory;
if ( directory[ -1 ] != "/" )
    directory += "/";

int dataset = Integer.parseInt( clArgs.s );
boolean noExpression = clArgs.n ? true : false;

String summaryFileSpec = directory + "dm2_" + dataset + "_summary.txt";
String designFileSpec = directory + "dm2_" + dataset + "_design.csv";
String expressionFileSpec = directory + "dm2_" + dataset + "_expression.tsv";

Map datasetSummary = getDatasetSummary( dataset, sql );
if ( datasetSummary == null )
    return;
Map sampleData = getSampleData( dataset, datasetSummary, sql );
if ( sampleData == null )
    return;
writeDatasetSummary( datasetSummary, summaryFileSpec );
writeSampleDataCsv( sampleData, designFileSpec );
if ( ! noExpression )
    writeExpressionData( sampleData, sql, expressionFileSpec );

//*****************************************************************************

Map getDatasetSummary( int dataset, Sql sql )
{
    try
    {
        String query =
                "SELECT d.official_name AS official_name," +
                "       d.description AS description," +
                "       d.date_created AS date_created," +
                "       d.file_name AS file_name," +
                "       ct.name AS chip_type" +
                "  FROM datasets AS d" +
                "  LEFT JOIN chip_types AS ct" +
                "    ON ct.chip_id = d.chip_type_id" +
                "  WHERE d.dataset_id = ?";
        GroovyRowResult record = sql.firstRow( query, [ dataset ] );
        Map summary =
                [
                    name: record.official_name,
                    description: record.description,
                    chipType: record.chip_type,
                    date: record.date_created,
                    filename: record.file_name
                ];
        return summary;
    }
    catch ( SQLException exc )
    {
        println( "SQL exception: \n" + exc );
        return null;
    }
}

//-----------------------------------------------------------------------------

void writeDatasetSummary( Map datasetSummary, String fileSpec )
{
    new File( fileSpec ).withWriter { writer ->
        writer.println( "Name: " + datasetSummary.name );
        writer.println( "Description: " + datasetSummary.description );
        writer.println( "Chip type: " + datasetSummary.chipType );
        writer.println( "Date: " + datasetSummary.date );
        writer.println( "Filename: " + datasetSummary.filename );
    }
}

//=============================================================================

Map getSampleData( int dataset, Map datasetSummary, Sql sql )
{
    try
    {
        String query =
                "SELECT dgsd.array_data_id AS array_data_id," +
                "       ad.file_name AS file_name," +
                "       dgs.name AS group_set," +
                "       dg.name AS `group`," +
                "       ct.name AS chip_type" +
                "  FROM dataset_group_set_details AS dgsd" +
                "  LEFT JOIN array_data AS ad" +
                "    ON ad.array_data_id = dgsd.array_data_id" +
                "  LEFT JOIN dataset_group_sets AS dgs" +
                "    ON dgs.group_set_id = dgsd.group_set_id" +
                "      AND dgs.dataset_id = dgsd.dataset_id" +
                "  LEFT JOIN dataset_groups AS dg" +
                "    ON dg.group_id = dgsd.group_id" +
                "      AND dg.dataset_id = dgsd.dataset_id" +
                "  LEFT JOIN chip_types AS ct" +
                "    ON ct.chip_id = ad.chip_type_id" +
                "  WHERE dgsd.dataset_id = ?";
        List< GroovyRowResult > records = sql.rows( query, [ dataset ] );

        Set< Integer > sampleIdSet = new TreeSet< Integer >();
        Set< String > attributeNames = new HashSet< String >();
        Map< Integer, Map > attributes = [:];
        for ( GroovyRowResult record : records )
        {
            Integer sampleId = record.array_data_id;
            String name = record.file_name;
            String attributeName = record.group_set;
            String attributeValue = record.group;
            String sampleChipType = record.chip_type;

            sampleIdSet.add( sampleId );
            if ( attributeName != "All Samples" )
                attributeNames.add( attributeName );
            if ( attributes.containsKey( sampleId ) == false )
            {
                attributes[ sampleId ] = [ "name": name ];
            }
            else
            {
                assert( name == attributes[ sampleId ][ "name" ] );
            }
            if ( attributeName != "All Samples" )
                attributes[ sampleId ][ attributeName ] = attributeValue;
            assert( sampleChipType == datasetSummary.chipType );
        }
        Map sampleData =
                [
                    sampleIds: sampleIdSet.toArray(),
                    attributeNames: attributeNames.toArray(),
                    attributes: attributes,
                ];
        return sampleData;
    }
    catch ( SQLException exc )
    {
        println( "SQL exception: \n" + exc );
        return null;
    }
}

//-----------------------------------------------------------------------------

void writeSampleDataCsv( Map sampleData, String fileSpec )
{
    List< List< String > > sampleDataTable = [];
    List< String > header = [];
    header.add( "Sample" );
    for ( String attributeName : sampleData.attributeNames )
    {
        header.add( attributeName );
    }
    sampleDataTable.add( header );
    for ( Integer sampleId : sampleData.sampleIds )
    {
        List< String > row = [];
        Map sampleDatum = sampleData.attributes[ sampleId ];
        row.add( sampleDatum[ "name" ] );
        for ( String attributeName : sampleData.attributeNames )
        {
            row.add( sampleDatum[ attributeName ] ?: "" );
        }
        sampleDataTable.add( row );
    }

    new File( fileSpec ).withWriter { writer ->
        for ( List< String > row : sampleDataTable )
        {
            row = row.collect { '"' + it + '"' }
            String rowString = row.join( ", " );
            writer.println( rowString );
        }
    }
}

//=============================================================================

void writeExpressionData( Map sampleData, Sql sql, String fileSpec )
{
    try
    {
        String command = "DROP TABLE IF EXISTS tmp_ads";
        sql.execute( command );
        command =
                "CREATE TEMPORARY TABLE tmp_ads" +
                "  ( id INTEGER PRIMARY KEY )";
        sql.execute( command );
        sampleData.sampleIds.each { sampleId ->
            command = "INSERT INTO tmp_ads ( id ) VALUE( ${sampleId} )";
            sql.execute( command );
        }
        
        command =
                "SELECT DISTINCT det.affy_id AS `probe_id`" +
                "  FROM array_data_detail AS det" +
                "  INNER JOIN tmp_ads AS tads" +
                "    ON tads.id = det.array_data_id" +
                "  ORDER BY det.affy_id";
        List< GroovyRowResult > records = sql.rows( command );
        List< String > probeIds = records*.probe_id;

        new File( fileSpec ).withWriter { writer ->
            writer.print( "ProbeID" );
            sampleData.sampleIds.each { sampleId ->
                String sampleName = sampleData.attributes[ sampleId ][ "name" ];
                writer.print( "\t" );
                writer.print( sampleName + ".AVG_Signal" );
                writer.print( "\t" );
                writer.print( sampleName + ".Detection Pval" );
            }

            for ( String probeId : probeIds )
            {
                command =
                        "SELECT det.array_data_id AS `sample_id`," +
                        "    det.signal AS `signal`," +
                        "    det.detection AS `detection`" +
                        "  FROM array_data_detail AS det" +
                        "  INNER JOIN tmp_ads AS tads" +
                        "    ON tads.id = det.array_data_id" +
                        "  WHERE det.affy_id = '" + probeId + "'" +
                        "  ORDER BY det.array_data_id";
                records = sql.rows( command );

                writer.print( "\n" );
                writer.print( probeId );

                int j = 0;
                for ( int i = 0; i < sampleData.sampleIds.size(); ++i )
                {
                    if ( records[ j ].sample_id == sampleData.sampleIds[ i ] )
                    {
                        Double signal = records[ j ].signal;
                        Double detection = records[ j ].detection;
                        detection = 1.0d - detection; //invert for DM3 use
                        ++j;
                        writer.print( "\t" );
                        writer.print( (signal != null) ? signal : "" );
                        writer.print( "\t" );
                        writer.print( (detection != null) ? detection : "" );
                    }
                    else
                    {
                        writer.print( "\t" );
                        writer.print( "\t" );
                    }
                }
            }
        }

        command = "DROP TABLE IF EXISTS tmp_ads";
        sql.execute( command );
    }
    catch ( SQLException exc )
    {
        println( "SQL exception: \n" + exc );
        return null;
    }
}

//*****************************************************************************
