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

def cli = new CliBuilder( usage: "SoftExp.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.f( longOpt: "filespec",
	   required: false,
	   args: 1,
	   argName: "softFileSpec",
       "Soft expression data file spec" );
cli.d( longOpt: "directory",
	   required: true,
	   args: 1,
	   argName: "directory",
	   "Directory of Soft files" );
cli.o( longOpt: "outputDirectory",
	   required: false,
	   args: 1,
	   argName: "outputDirectory",
	   "Directory of output files" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}


//makeMetadataOnlyFiles( clArgs );
//inspectFamilyFiles( clArgs );
inspectFullFiles( clArgs );

//=============================================================================

boolean makeMetadataOnlyFiles( def args )
{
	boolean verbose = (args.v == true);

	String inputDirName = args.d;
	File inputDir = new File( inputDirName );
	String outputDirName = args.o;
	if ( outputDirName[ -1 ] != "/" )
		outputDirName += "/";
	File outputDir = new File( outputDirName );
	if ( outputDir.exists() == false )
		outputDir.mkdirs( );
	inputDir.eachFile { inFile ->
		if ( inFile.name.startsWith( "GSE" ) ||
			 inFile.name.startsWith( "GDS" ) )
		{
			File outFile = new File( outputDirName + inFile.name );
			outFile.withWriter { writer ->
				inFile.eachLine { line ->
					if ( line.startsWith( "^" ) ||
						 line.startsWith( "!" ) ||
						 line.startsWith( "#" ) )
					writer.writeLine( line );
				}
			}
		}
	}
}

//=============================================================================

boolean inspectFamilyFiles( def args )
{
	boolean verbose = (args.v == true);

	def fileAnnotMap = new TreeMap();
    def fileSampleAnnotMap = new TreeMap();

	String directoryName = args.d;
	File directory = new File( directoryName );
	directory.eachFileMatch( ~/GSE.*_family.soft/ ) { file ->
		fileAnnotMap[ file.name ] = getFileSeriesAnnotations( file );
        fileSampleAnnotMap[ file.name ] = getFileSampleAnnotations( file );
		addSampleAnnotsToMap( fileAnnotMap, fileSampleAnnotMap );
	}

	Map annotAnalysis = analyzeFamilyAnnotations( fileAnnotMap, fileSampleAnnotMap );

	displayFamilyAnnotationInfo( annotAnalysis );
}

//-----------------------------------------------------------------------------

def getFileSeriesAnnotations( File file )
{
	def annots = [:];
	int lineNum = 0;
	file.eachLine { line ->
		++lineNum;
		if ( line.startsWith( "!Series_" ) )
		{
			def parts = line.split( "=" );
			if ( parts.size() < 2 )
			{
				println( "< 2 parts on line " + lineNum + " of " + file.name );
				return false;
			}
			String key = parts[0].trim();
			String value = parts[1..-1].join( "=" ).trim();
			if ( annots.containsKey( key ) == false )
			{
				annots[ key ] = [ value ];
			}
			else if ( annots[ key ].contains( value ) == false )
			{
                if ( (key == "!Series_summary") ||
                     (key == "!Series_overall_design") )
                {   //accumulate as paragraphs
                    annots[ key ][ 0 ] += "\n" + value;
                }
                else
                {   //accumulate as list
                    annots[ key ].add( value );
                }
			}
		}
	}
	return annots;
}

//.............................................................................

def getFileSampleAnnotations( File file )
{
	def annots = [:];
    String sampleId;
    def sampleAnnots;
	int lineNum = 0;
	file.eachLine { line ->
		++lineNum;
        if ( line.startsWith( "^SAMPLE" ) )
        {
			def parts = line.split( "=" );
			if ( parts.size() < 2 )
			{
				println( "< 2 parts on line " + lineNum + " of " + file.name );
				return false;
			}
            sampleId = parts[1].trim();
            sampleAnnots = new TreeMap();
            annots[ sampleId ] = sampleAnnots;
        }
        else if ( line.startsWith( "!Sample" ) )
        {
			def parts = line.split( "=" );
			if ( parts.size() < 2 )
			{
				println( "< 2 parts on line " + lineNum + " of " + file.name );
				return false;
			}
			String key = parts[0].trim();
			String value = parts[1..-1].join( "=" ).trim();
			if ( sampleAnnots.containsKey( key ) == false )
			{
				sampleAnnots[ key ] = [ value ];
			}
			else if ( sampleAnnots[ key ].contains( value ) == false )
			{
                if ( (key == "!Sample_data_processing") ||
                     (key == "!Sample_description") ||
                     (key == "!Sample_extract_protocol_ch1") ||
                     (key == "!Sample_growth_protocol_ch1") ||
                     (key == "!Sample_hyb_protocol") ||
                     (key == "!Sample_label_protocol_ch1") ||
                     (key == "!Sample_scan_protocol") ||
                     (key == "!Sample_treatment_protocol_ch1")
				   )
                {   //accumulate as paragraphs
                    sampleAnnots[ key ][ 0 ] += "\n" + value;
                }
                else
                {   //accumulate as list
					sampleAnnots[ key ].add( value );
                }
			}
        }
	}
	return annots;
}

//.............................................................................

void addSampleAnnotsToMap( Map fileAnnots, Map fileSampleAnnots )
{
    fileSampleAnnots.each { file, sampleAnnots ->
		def seriesAnnots = fileAnnots[ file ];
        sampleAnnots.each { sample, annots ->
            annots.each { key, vals ->
				if ( seriesAnnots.containsKey( key ) == false )
					seriesAnnots[ key ] = [];
				vals.each { val ->
					if ( seriesAnnots[ key ].contains( val ) == false )
					{
						seriesAnnots[ key ].add( val );
					}
				}
			}
		}
	}
}

//-----------------------------------------------------------------------------

Map analyzeFamilyAnnotations( Map fileAnnotMap, Map fileSampleAnnotMap )
{
	def annotAnalysis = new TreeMap();

	fileAnnotMap.each { file, annots ->
		annots.each { key, vals ->
            if ( annotAnalysis.containsKey( key ) == false )
                annotAnalysis[ key ] = [:];
			def info = annotAnalysis[ key ];

            if ( info.containsKey( "Values" ) == false )
                info[ "Values" ] = [];
            info[ "Values" ].addAll( vals );

			int numVals = vals.size();
            if ( info.containsKey( "NumValsPerSeries" ) == false )
                info[ "NumValsPerSeries" ] = new TreeMap< Integer, Integer >();
            def numValsPerSeries = info[ "NumValsPerSeries" ];
			if ( numValsPerSeries.containsKey( numVals ) == false )
				numValsPerSeries[ numVals ] = 1;
			else
				++numValsPerSeries[ numVals ];

            if ( key.startsWith( "!Sample_contact_" ) )
            {
                String contactField = key[ "!Sample_contact_".size()..-1 ];
                String seriesContactKey = "!Series_contact_" + contactField;
                def seriesVals = annots[ seriesContactKey ];
                if ( (seriesVals == null) || (seriesVals.size() != 1) ||
                     (seriesVals[ 0 ] != vals[ 0 ]) )
                {
                    if ( info.containsKey( "SeriesSampleMismatch" ) == false )
                        info[ "SeriesSampleMismatch" ] = 1;
                    else
                        ++info[ "SeriesSampleMismatch" ];
                }
            }

			if ( (key == "!Sample_data_processing") ||
				 (key == "!Sample_extract_protocol_ch1") ||
				 (key == "!Sample_growth_protocol_ch1") ||
				 (key == "!Sample_hyb_protocol") ||
				 (key == "!Sample_label_protocol_ch1") ||
				 (key == "!Sample_scan_protocol") ||
				 (key == "!Sample_treatment_protocol_ch1")
			   )
			{
				if ( numVals > 1 )
				{
					if ( info.containsKey( "MultiValFiles" ) == false )
						info[ "MultiValFiles" ] = [];
					info[ "MultiValFiles" ].add( file );
				}
			}
		}
	}

    fileSampleAnnotMap.each { file, sampleAnnots ->
        sampleAnnots.each { sample, annots ->
            annots.each { key, vals ->
                assert( annotAnalysis.containsKey( key ) );
                def info = annotAnalysis[ key ];

                int numVals = vals.size();
                if ( info.containsKey( "NumValsPerSample" ) == false )
                    info[ "NumValsPerSample" ] =
                            new TreeMap< Integer, Integer >();
                def numValsPerSample = info[ "NumValsPerSample" ];
                if ( numValsPerSample.containsKey( numVals ) == false )
                    numValsPerSample[ numVals ] = 1;
                else
                    ++numValsPerSample[ numVals ];
            }
        }
    }

	int numFiles = fileAnnotMap.size();
	annotAnalysis.each { key, info ->
        info[ "Values" ].unique( );
        info[ "Values" ].sort { x, y ->
            (x.size() != y.size()) ? x.size() <=> y.size() : x <=> y }

		def numValsPerSeries = info[ "NumValsPerSeries" ];
		int total = 0;
		numValsPerSeries.each { size, count ->
			total += count;
		}
		int numZero = numFiles - total;
		if ( numZero > 0 )
			numValsPerSeries[ 0 ] = numZero;
	}

	return annotAnalysis;
}

//-----------------------------------------------------------------------------

void displayFamilyAnnotationInfo( Map annotAnalysis )
{
	annotAnalysis.each { key, info ->
        println( );
		println( key + ": " );
		displayNumValsPer( info[ "NumValsPerSeries" ], "Series" );
		displayNumValsPer( info[ "NumValsPerSample" ], "Sample" );
        displaySeriesSampleMismatches( info[ "SeriesSampleMismatch" ] );
        displaySmallestAndLargestValues( info[ "Values" ] );
		displayMultiValFiles( info[ "MultiValFiles" ] );
	}
}

//.............................................................................

void displaySeriesSampleMismatches( Integer numMismatches )
{
    if ( numMismatches != null )
    {
        println( "    Mismatches between series and sample: " + numMismatches );
    }
}

//.............................................................................

void displayMultiValFiles( List fileNames )
{
	if ( fileNames )
	{
		println( "    Multi-value files: " + fileNames.join( ", " ) );
	}
}

//=============================================================================

boolean inspectFullFiles( def args )
{
	boolean verbose = (args.v == true);

	def fileAnnotMap = new TreeMap();

	String directoryName = args.d;
	File directory = new File( directoryName );
	directory.eachFileMatch( ~/GDS.*_full.soft/ ) { file ->
		fileAnnotMap[ file.name ] = getFileDatasetAnnotations( file );
	}

	Map annotAnalysis = analyzeFullAnnotations( fileAnnotMap );

	displayFullAnnotationInfo( annotAnalysis );
}

//-----------------------------------------------------------------------------

Map getFileDatasetAnnotations( File file )
{
	def annots = [:];
	int lineNum = 0;
	file.eachLine { line ->
		++lineNum;
		if ( line.startsWith( "!dataset_" ) &&
		   (line.startsWith( "!dataset_table_" ) == false) )
		{
			def parts = line.split( "=" );
			if ( parts.size() < 2 )
			{
				println( "< 2 parts on line " + lineNum + " of " + file.name );
				return false;
			}
			String key = parts[0].trim();
			String value = parts[1..-1].join( "=" ).trim();
			if ( annots.containsKey( key ) == false )
			{
				annots[ key ] = [ value ];
			}
			else if ( annots[ key ].contains( value ) == false )
			{
                //accumulate as list
				annots[ key ].add( value );
			}
		}
	}
	return annots;
}

//-----------------------------------------------------------------------------

Map analyzeFullAnnotations( Map fileAnnotMap )
{
	def annotAnalysis = new TreeMap();

	fileAnnotMap.each { file, annots ->
		annots.each { key, vals ->
            if ( annotAnalysis.containsKey( key ) == false )
                annotAnalysis[ key ] = [:];
			def info = annotAnalysis[ key ];

            if ( info.containsKey( "Values" ) == false )
                info[ "Values" ] = [];
            info[ "Values" ].addAll( vals );

			int numVals = vals.size();
            if ( info.containsKey( "NumValsPerDataset" ) == false )
                info[ "NumValsPerDataset" ] = new TreeMap< Integer, Integer >();
            def numValsPerDataset = info[ "NumValsPerDataset" ];
			if ( numValsPerDataset.containsKey( numVals ) == false )
				numValsPerDataset[ numVals ] = 1;
			else
				++numValsPerDataset[ numVals ];
		}
	}

	int numFiles = fileAnnotMap.size();
	annotAnalysis.each { key, info ->
        info[ "Values" ].unique( );
        info[ "Values" ].sort { x, y ->
            (x.size() != y.size()) ? x.size() <=> y.size() : x <=> y }

		def numValsPerDataset = info[ "NumValsPerDataset" ];
		int total = 0;
		numValsPerDataset.each { size, count ->
			total += count;
		}
		int numZero = numFiles - total;
		if ( numZero > 0 )
			numValsPerDataset[ 0 ] = numZero;
	}

	return annotAnalysis;
}

//-----------------------------------------------------------------------------

void displayFullAnnotationInfo( Map annotAnalysis )
{
	annotAnalysis.each { key, info ->
        println( );
		println( key + ": " );
		displayNumValsPer( info[ "NumValsPerDataset" ], "Dataset" );
        displaySmallestAndLargestValues( info[ "Values" ] );
	}
}

//=============================================================================

void displayNumValsPer( Map sizes, String setName )
{
	if ( sizes )
	{
		print( "    Num vals per " + setName + ":" );
		def sizeVals = sizes.keySet().toList();
		if ( sizeVals.size() <= 6 )
		{
			for ( Integer s : sizeVals )
			{
				print( "  " + s + ": " + sizes[ s ] );
			}
		}
		else
		{
			for ( int i = 0; i < 5; ++i )
			{
				Integer s = sizeVals[ i ];
				print( "  " + s + ": " + sizes[ s ] );
			}
			print( "  ..." );
			for ( int i = -2; i < 0; ++i )
			{
				Integer s = sizeVals[ i ];
				print( "  " + s + ": " + sizes[ s ] );
			}
		}
		print( "\n" );
	}
}

//.............................................................................

void displaySmallestAndLargestValues( List values )
{
    int numVals = values.size();
    println( "    Values (" + numVals + ")" );
    if ( numVals <= 4 )
    {
        for ( String val : values )
        {
            println( '        "' + val + '"' );
        }
    }
    else
    {
        for ( int i = 0; i < 2; ++i )
        {
            println( '        "' + values[ i ] + '"' );
        }
        println( '        ...' );
        if ( numVals > 7 )
        {
            for ( int i = (numVals/2 - 1); i <= (numVals/2 + 1); ++i )
            {
                println( '        "' + values[ i ] + '"' );
            }
            println( '        ...' );
        }
        for ( int i = -2; i < 0; ++i )
        {
            println( '        "' + values[ i ] + '"' );
        }
    }
}

