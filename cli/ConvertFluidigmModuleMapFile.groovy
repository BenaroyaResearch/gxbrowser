#!/usr/bin/groovy
/*
  ConvertFluidigmModuleMapFile.groovy

  Script for converting module map file to two-column format.
  The input file has five fields. The first is the "Fluidigm gene" (the Target).
  The fourth is the Gen 3 module. We keep these, module first.
*/
package org.sagres.dm

def cli = new CliBuilder( usage: "ExpMan.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.i( longOpt: "inputFile",
	   required: true,
	   args: 1,
	   argName: "inputFileName",
       "Input file name" );
cli.o( longOpt: "outputFile",
	   required: true,
	   args: 1,
	   argName: "outputFileName",
       "Output file name" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

convertModuleMapFile( clArgs.i, clArgs.o );

//*****************************************************************************

void convertModuleMapFile( String inputFileName, String outputFileName )
{
	PrintWriter writer = new PrintWriter( outputFileName );
	new File( inputFileName ).eachLine { line, lineNum ->
		if ( (lineNum > 1) && (line.size() > 0) )
		{
			String[] fields = line.split( ',' );
			assert( fields.size() >= 4 );
			writer.println( fields[ 3 ] + ',' + fields[ 0 ] );
		}
	}
	writer.flush( );
	writer.close( );
}


//*****************************************************************************

