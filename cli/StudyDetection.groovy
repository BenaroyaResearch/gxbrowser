#!/usr/bin/groovy
package org.sagres.dm

import java.util.regex.Pattern;
import java.util.regex.Matcher;
import java.text.DecimalFormat;
import java.text.ParsePosition;


//*****************************************************************************

def cli = new CliBuilder( usage: "SoftExp.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.d( longOpt: "directory",
	   required: true,
	   args: 1,
	   argName: "directory",
	   "Directory of Soft files" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}


//inspectFamilyColumnDefs( clArgs );
inspectDetectionValues( clArgs );

//=============================================================================

void inspectFamilyColumnDefs( def args )
{
	boolean verbose = (args.v == true);

    Map fileColDefsMap = [:];

	String directoryName = args.d;
	File directory = new File( directoryName );
	directory.eachFileMatch( ~/GSE.*_family.soft/ ) { file ->
		fileColDefsMap[ file.name ] = getFamilyFileColDefs( file );
	}

    displayFileColDefsMap( fileColDefsMap );
}

//-----------------------------------------------------------------------------

Map getFamilyFileColDefs( File file )
{
    def colDefs = [:];
    int lineNum = 0;
    boolean inSamples = false;
	file.eachLine { line ->
		++lineNum;
        if ( line.startsWith( "^SAMPLE" ) )
        {
            inSamples = true;
        }
        else if ( inSamples && line.startsWith( "#" ) )
        {
			def parts = line.split( "=", -1 );
			if ( parts.size() < 2 )
			{
				println( "< 2 parts on line " + lineNum + " of " + file.name );
				return false;
			}
			String key = parts[0].trim();
			String value = parts[1..-1].join( "=" ).trim();
			if ( colDefs.containsKey( key ) == false )
			{
				colDefs[ key ] = [ value ];
			}
			else if ( colDefs[ key ].contains( value ) == false )
			{
                colDefs[ key ].add( value );
            }
        }
	}
	return colDefs;
}

//-----------------------------------------------------------------------------

void displayFileColDefsMap( Map fileColDefsMap )
{
	fileColDefsMap.each { fileName, colDefs ->
        println( fileName );
		colDefs.each { key, vals ->
            println( "    " + key + ":" );
            vals.each { val ->
                println( "        " + val );
            }
        }
        println( );
    }
}

//=============================================================================

void inspectDetectionValues( def args )
{
	boolean verbose = (args.v == true);

	String directoryName = args.d;
	File directory = new File( directoryName );
	directory.eachFileMatch( ~/GSE.*_family.soft/ ) { file ->
		Map detectionInfo = collectDetectionInfo( file );
        displayDetectionInfo( file.name, detectionInfo );
	}
}

//-----------------------------------------------------------------------------

Map collectDetectionInfo( File file )
{
    Map detectionInfo = [:];
    int lineNum = 0;
    int state = 0; //1=at table head, 2=in data
    int idRefCol = -1;
    int valueCol = -1;
    int detectionCol = -1;
    int absCallCol = -1;
    DecimalFormat format = new DecimalFormat();
    ParsePosition pos = new ParsePosition( 0 );

	file.eachLine { line ->
		++lineNum;
        if ( line.startsWith( "!sample_table_begin" ) )
        {
            state = 1;
        }
        else if ( state == 1 )
        {
            List fields = line.split( "\t");
            for ( int i = 0; i < fields.size(); ++i )
            {
                if ( fields[ i ].toUpperCase() == "ID_REF" )
                {
                    idRefCol = i;
                }
                else if ( fields[ i ].toUpperCase() == "VALUE" )
                {
                    valueCol = i;
                }
                else if ( fields[ i ].toUpperCase().startsWith( "DETECTION" ) )
                {
                    detectionCol = i;
                }
                else if ( fields[ i ].toUpperCase() == "ABS_CALL" )
                {
                    absCallCol = i;
                }
            }
            assert( idRefCol >= 0 );
            assert( valueCol >= 0 );
            state = 2;
        }
        else if ( state == 2 )
        {
            if ( line.startsWith( "!sample_table_end" ) )
            {
                state = 0;
            }
            else if ( (detectionCol >= 0) || (absCallCol >= 0) )
            {
                List fields = line.split( "\t", -1 );
                Double detection;
                String absCall;
                if ( detectionCol >= 0 )
                {
                    String field = fields[ detectionCol ];
                    pos.setIndex( 0 );
                    Number rawDetection =
                            format.parse( field.toUpperCase(), pos );
                    assert( (rawDetection != null) &&
                            (pos.getIndex() == field.size()) );
                    detection = rawDetection.doubleValue();
                }
                if ( absCallCol >= 0 )
                {
                    absCall = fields[ absCallCol ];
                }
                
                if ( detection != null )
                {
                    if ( detectionInfo.containsKey( "detFreqs" ) == false )
                    {
                        detectionInfo[ "detFreqs" ] = new TreeMap();
                        for ( int i = 0; i < 10; ++i )
                        {
                            detectionInfo[ "detFreqs" ][ i ] = 0;
                        }
                    }
                    assert( detection >= 0.0 );
                    assert( detection <= 1.0 );
                    int cell = Math.min( (int)(10.0d * detection), 9 );
                    ++detectionInfo[ "detFreqs" ][ cell ];
                }
                if ( absCall != null )
                {
                    if ( detectionInfo.containsKey( "calls" ) == false )
                    {
                        detectionInfo[ "calls" ] = [:];
                    }
                    if ( detectionInfo[ "calls" ].containsKey( absCall ) ==
                         false )
                    {
                        detectionInfo[ "calls" ][ absCall ] = [:];
                        detectionInfo[ "calls" ][ absCall ][ "freq" ] = 0;
                    }
                    ++detectionInfo[ "calls" ][ absCall ][ "freq" ];
                    if ( detection != null )
                    {
                        if ( detectionInfo[ "calls" ][ absCall ].containsKey( "min" ) == false )
                        {
                            detectionInfo[ "calls" ][ absCall ][ "min" ] =
                                    detection;
                            detectionInfo[ "calls" ][ absCall ][ "max" ] =
                                    detection;
                        }
                        else
                        {
                            if ( detection < detectionInfo[ "calls" ][ absCall ][ "min" ] )
                            {
                                detectionInfo[ "calls" ][ absCall ][ "min" ] =
                                        detection;
                            }
                            if ( detection > detectionInfo[ "calls" ][ absCall ][ "max" ] )
                            {
                                detectionInfo[ "calls" ][ absCall ][ "max" ] =
                                        detection;
                            }
                        }
                    }
                }
            }
        }
    }
    return detectionInfo;
}

//-----------------------------------------------------------------------------

void displayDetectionInfo( String fileName, Map detectionInfo )
{
    if ( (detectionInfo.containsKey( "detFreqs" ) == false) &&
         (detectionInfo.containsKey( "calls" ) == false) )
    {
        return;
    }
    println( fileName );
    if ( detectionInfo.containsKey( "detFreqs" ) )
    {
        println( "    Detection frequencies" );
        for ( int i = 0; i < 10; ++i )
        {
            println( "        0." + i + ": " +
                     detectionInfo[ "detFreqs" ][ i ] );
        }
    }
    if ( detectionInfo.containsKey( "calls" ) )
    {
        println( "    Abs Calls" );
        detectionInfo[ "calls" ].each { call, callInfo ->
            print( "        " + call + ":" );
            print( " freq: " + callInfo[ "freq" ] );
            if ( callInfo.containsKey( "min" ) )
            {
                print( " min: " + callInfo[ "min" ] );
                print( " max: " + callInfo[ "max" ] );
            }
            println();
        }
    }
    println();
}

//*****************************************************************************
