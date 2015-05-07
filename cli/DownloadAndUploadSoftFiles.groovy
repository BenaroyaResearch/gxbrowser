#!/usr/bin/groovy
/*
  DownloadAndUploadSoftFiles.groovy

  Script that downloads Soft files from NCBI's GEO repository,
  gunzips them, and uploads them to our server for import into the DM3 database.
*/

package org.sagres.dm;


String defaultDirectory = ".";
String defaultDm3Host = "localhost";
String defaultDm3Port = "8080";

CliBuilder cli = new CliBuilder( usage: "DownloadAndUploadSoftFiles.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.s( longOpt: "gse",
       required: false,
       args: 1,
       argName: "gse",
       "GEO series (GSE) number" );
cli.c( longOpt: "gpl",
       required: false,
       args: 1,
       argName: "gpl",
       "GEO platform (GPL) number" );
cli.l( longOpt: "listfile",
       required: false,
       args: 1,
       argName: "listFile",
       "File with GEO series (GSE) numbers" );
cli.d( longOpt: "directory",
       required: false,
       args: 1,
       argName: "directory",
       "Directory for Soft file downloads" );
cli.h( longOpt: "dm3host",
       required: false,
       args: 1,
       argName: "dm3Host",
       "DM3 Web service host" );
cli.p( longOpt: "dm3port",
       required: false,
       args: 1,
       argName: "dm3Port",
       "DM3 Web service port" );
cli.u( longOpt: "dm3url",
	   required: false,
	   args: 1,
	   argName: "dm3Url",
	   "DM3 Web service URL" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}
if ( clArgs.help )
{
    cli.usage();
    return;
}

String gse = clArgs.s ?: null;
String gpl = clArgs.c ?: null;
String listFileSpec = clArgs.l ?: null;
String directory = clArgs.d ?: defaultDirectory;
if ( directory.endsWith( "/" ) == false )
{
    directory += "/";
}
String dm3Host = clArgs.h ?: defaultDm3Host;
String dm3Port = clArgs.p ?: defaultDm3Port;
String dm3Url = clArgs.u ?: "http://${dm3Host}:${dm3Port}/dm3";
boolean verbose = (clArgs.v == true);


if ( gse )
{
    if ( gpl == null )
    {
        downloadAndUploadFamilyFile( gse, directory, dm3Url, verbose );
    }
    else
    {
        downloadAndUploadSeriesMatrixFile( gse, gpl, directory, dm3Url,
                                           verbose );
    }
}
else if ( listFileSpec )
{
    downloadAndUploadFiles( listFileSpec, directory, dm3Url, verbose );
}
else
{
    println( "No files specified for processing" );
}

//*****************************************************************************


void downloadAndUploadFiles( String listFileSpec, String directory,
                             String dm3Url, boolean verbose )
{
    new File( listFileSpec ).eachLine { line ->
        if ( line.startsWith( "#" ) )
            return; //skip comments
        String[] fields = line.split( "[ \t]" );
        if ( fields.size() > 0 )
        {   
            String gse = fields[ 0 ];
            if ( fields.size() == 1 )
            {
                downloadAndUploadFamilyFile( gse, directory, dm3Url, verbose );
            }
            else
            {
                String gpl = fields[ 1 ];
                downloadAndUploadSeriesMatrixFile( gse, gpl, directory,
                                                   dm3Url, verbose );
            }
        }
    }
}

//-----------------------------------------------------------------------------

void downloadAndUploadFamilyFile( String gse, String directory, String dm3Url,
                                  boolean verbose )
{
    if ( gse.startsWith( "GSE" ) == false )
    {
        gse = "GSE" + gse;
    }
    String fileName = "${gse}_family.soft";
    String downloadUrl = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SOFT/by_series/" +
            "${gse}/${fileName}.gz";
    String fileSpec = directory + fileName;
    boolean rslt = downloadFile( downloadUrl, fileSpec, verbose );
    if ( rslt == false )
        return;
    rslt = uploadFile( dm3Url, fileSpec, "Family", verbose );
}

//.............................................................................

void downloadAndUploadSeriesMatrixFile( String gse, String gpl,
                                        String directory, String dm3Url,
                                        boolean verbose )
{
    if ( gse.startsWith( "GSE" ) == false )
    {
        gse = "GSE" + gse;
    }
    if ( gpl.startsWith( "GPL" ) == false )
    {
        gpl = "GPL" + gpl;
    }
    String fileName = "${gse}-${gpl}_series_matrix.txt";
    String downloadUrl = "ftp://ftp.ncbi.nih.gov/pub/geo/DATA/SeriesMatrix/" +
            "${gse}/${fileName}.gz";
    String fileSpec = directory + fileName;
    boolean rslt = downloadFile( downloadUrl, fileSpec, verbose );
    if ( rslt == false )
        return;
    rslt = uploadFile( dm3Url, fileSpec, "SeriesMatrix", verbose );
}

//-----------------------------------------------------------------------------

boolean downloadFile( String downloadUrl, String fileSpec, boolean verbose )
{
    if ( new File( fileSpec ).exists() )
        return true;
    String command = "curl -o ${fileSpec}.gz ${downloadUrl}";
    Map results = [:];
    int verbosity = verbose ? 2 : 1;
    boolean rslt = execSysCommand( command, verbosity, results );
    if ( rslt == false )
        return false;

    command = "gunzip ${fileSpec}.gz";
    results = [:];
    rslt = execSysCommand( command, verbosity, results );
    if ( rslt == false )
        return false;
    return true;
}

//-----------------------------------------------------------------------------

boolean uploadFile( String dm3Url, String fileSpec,
                    String type, boolean verbose )
{
    String url = dm3Url + "/chipsLoaded/batchUploadSoftFile";
    String command = 'curl -F file=@' + fileSpec +
            ' -F type=' + type + ' ' + url;
    Map results = [:];
    int verbosity = verbose ? 3 : 1;
    boolean rslt = execSysCommand( command, verbosity, results );
    if ( rslt == false )
        return false;
}

//=============================================================================

boolean execSysCommand( String command, int verbosity = 1, Map results = null )
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
                if ( verbosity == 1 )
                {
                    println( "Error executing: " + command );
                }
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


//*****************************************************************************
