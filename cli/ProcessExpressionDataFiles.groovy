#!/usr/bin/groovy
package org.sagres.dm


//*****************************************************************************

String defaultBaseDir = "/var/local/dm/fileUploads/expressionData/toBeImported";
String defaultBaseUrl = "http://localhost:8080/dm3"

CliBuilder cli = new CliBuilder( usage: "SoftExp.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.d( longOpt: "baseDir",
	   required: false,
	   args: 1,
	   argName: "baseDir",
	   "Base directory of expression data files to be imported" );
cli.u( longOpt: "baseUrl",
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

Map args = [:];
args.verbose = (clArgs.v == true);
args.baseDirName = args.d ?: defaultBaseDir;
args.baseUrl = clArgs.u ?: defaultBaseUrl;

processExpressionDataFiles( args );

//=============================================================================

void processExpressionDataFiles( Map args )
{
	File baseDir = new File( args.baseDirName );
    processDirectory( baseDir, args.baseUrl );
}

//=============================================================================

void processDirectory( File dir, String baseUrl )
{
    dir.eachFile { file ->
        if ( file.isFile() )
        {
            String fileSpec = file.canonicalPath;
            fileSpec = URLEncoder.encode( fileSpec );
            URL url;
            if ( dir.name == "soft" )
            {
                url = new URL( baseUrl + "/chipsLoaded/importSoftFile" +
                               "?fileSpec=" + fileSpec );
            }
            else
            {
                url = new URL( baseUrl + "/chipsLoaded/importExpressionData" +
                               "?fileSpec=" + fileSpec );
            }
            println( "URL: " + url );
            String response = url.getText();
            println( "Response: " + response );
        }
        else if ( file.isDirectory() )
        {
            processDirectory( file, baseUrl ); //recurse
        }
    }
}

//*****************************************************************************
