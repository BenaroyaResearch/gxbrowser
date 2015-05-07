#!/usr/bin/groovy
/*
  MatchSamplesInSoftFile.groovy

  Finds samples with matching characteristics and creates a text file
  mapping sample labels with a new compound label.
  For series where the same sample was run on multiple platforms
  (commonly Affy U133A & U133B).
*/

import java.util.regex.Matcher;


//*****************************************************************************

def cli = new CliBuilder( usage: "SoftExp.groovy [options]" );
cli.help( longOpt: "help",
		  "Print this message" );
cli.s( longOpt: "softfile",
	   required: true,
	   args: 1,
	   argName: "softFileSpec",
       "Input Soft expression data file spec" );
cli.t( longOpt: "tablefile",
       required: true,
       args: 1,
       argName: "tableFileSpec",
       "Sample table file spec (CSV)" );
cli.p( longOpt: "problemsfile",
       required: true,
       args: 1,
       argName: "problemsFileSpec",
       "Error report file spec (TXT)" );
cli.i( longOpt: "matchontitle",
       "Match on sample title" );
cli.a( longOpt: "matchonannotations",
       "Match on sample annotations" );
cli.v( longOpt: "verbose",
       "Verbose output" );

def clArgs = cli.parse( args );
if ( ! clArgs )
{
	return;
}

String softFileSpec = clArgs.s;
String tableFileSpec = clArgs.t;
String problemsFileSpec = clArgs.p;
boolean matchOnTitle = (clArgs.i == true);
boolean matchOnAnnotations = (clArgs.a == true);
boolean verbose = (clArgs.v == true);

matchSamples( softFileSpec, tableFileSpec, problemsFileSpec,
              matchOnTitle, matchOnAnnotations, verbose );

enum Method
{
    RegularBySample, RegularByPlatform,
    MatchOnTitle, MatchOnAnnotations
};
    
//*****************************************************************************

void matchSamples( String softFileSpec, 
                   String tableFileSpec, String problemsFileSpec,
                   boolean matchOnTitle, boolean matchOnAnnotations,
                   boolean verbose )
{
    List< String > sampleLabels = [];
    Map< String, String > sampleTitles = [:];
    Map< String, String > samplePlatforms = [:];
    Map< String, Map > sampleAnnotations = [:];
    List< String > platforms = [];
    Map< String, Integer > platformCounts = [:];
    List< List< String > > sampleSetList = [];

    readSampleData( softFileSpec, sampleLabels, sampleTitles,
                    samplePlatforms, sampleAnnotations,
                    platforms, platformCounts,
                    verbose );
    Map countInfo = getPlatformCountInfo( platformCounts, verbose );
    printPlatformCounts( platforms, platformCounts, verbose );
    Method method = determineMethod( sampleLabels, samplePlatforms,
                                     platforms, countInfo,
                                     matchOnTitle, matchOnAnnotations,
                                     verbose );
    collectMatches( sampleSetList, method,
                    sampleLabels, sampleTitles, sampleAnnotations, countInfo,
                    verbose );
    writeSampleTable( tableFileSpec, sampleSetList, sampleTitles,
                      samplePlatforms, sampleAnnotations, verbose );
    writeAnnotationMismatches( problemsFileSpec,
                               sampleSetList, sampleAnnotations, verbose );
}

//=============================================================================

void readSampleData( String softFileSpec,
                     List< String > sampleLabels,
                     Map< String, String > sampleTitles,
                     Map< String, String > samplePlatforms,
                     Map< String, Map >sampleAnnotations,
                     List< String > platforms,
                     Map< String, Integer > platformCounts,
                     boolean verbose )
{
    String label;
    Map annotations;
    new File( softFileSpec ).eachLine { line, lineNum ->
        if ( line.startsWith( "^SAMPLE" ) )
        {
            KeyValue kv = KeyValue.split( line, "=" );
            label = kv.value;
            assert( label );
            sampleLabels.add( label );
            annotations = [:];
            sampleAnnotations[ (label) ] = annotations;
        }
        else if ( line.startsWith( "!Sample_title" ) )
        {
            KeyValue kv = KeyValue.split( line, "=" );
            sampleTitles[ (label) ] = kv.value;
        }
        else if ( line.startsWith( "!Sample_platform_id" ) )
        {
            KeyValue kv = KeyValue.split( line, "=" );
            String platform = kv.value;
            samplePlatforms[ (label) ] = platform;
            if ( platforms.contains( platform ) == false )
            {
                platforms.add( platform );
                assert( platformCounts.containsKey( platform ) == false );
                platformCounts[ (platform) ] = 0;
            }
            else
            {
                assert( platformCounts.containsKey( platform ) );
                ++platformCounts[ (platform) ];
            }
        }
        else if ( line.startsWith( "!Sample_characteristics" ) )
        {
            KeyValue kv = KeyValue.split( line, "=" );
            assert( kv.value );
            kv = KeyValue.split( kv.value, ":" );
            annotations[ (kv.key) ] = kv.value;
        }
        else if ( line.startsWith( "!Sample_description" ) )
        {
            KeyValue kv = KeyValue.split( line, "=" );
            annotations[ (kv.key) ] = kv.value;
        }
    }
}

//=============================================================================

Map getPlatformCountInfo( Map< String, Integer > platformCounts,
                          boolean verbose )
{
    Map countInfo =
            [
                regular: true,
                numPlatforms: platformCounts.size(),
                numSampleSets: 0
            ];
    platformCounts.each { platform, count ->
        if ( countInfo.numSampleSets == 0 )
        {
            countInfo.numSampleSets = count;
        }
        else if ( count != countInfo.numSampleSets )
        {
            countInfo.regular = false;
        }
    }
    return countInfo;
}

//-----------------------------------------------------------------------------

void printPlatformCounts( List< String > platforms,
                          Map< String, Integer > platformCounts,
                          boolean verbose )
{
    if ( verbose )
    {
        println( "Platforms:" );
        for ( String platform : platforms )
        {
            println( platform + ": " + platformCounts[ (platform) ] );
        }
    }
}

//=============================================================================

Method determineMethod( List< String > sampleLabels,
                        Map< String, String > samplePlatforms,
                        List< String > platforms,
                        Map countInfo,
                        boolean matchOnTitle, boolean matchOnAnnotations,
                        boolean verbose )
{
    if ( matchOnTitle )
    {
        return Method.MatchOnTitle;
    }
    if ( matchOnAnnotations )
    {
        return Method.MatchOnAnnotations;
    }

    if ( countInfo.regular )
    {
        if ( checkForSampleRegularity( sampleLabels, samplePlatforms,
                                       platforms, countInfo, true,
                                       verbose ) )
        {
            return Method.RegularBySample;
        }
        else if ( checkForSampleRegularity( sampleLabels, samplePlatforms,
                                            platforms, countInfo, false,
                                            verbose ) )
        {
            return Method.RegularByPlatform;
        }
    }
    else
    {
        return Method.MatchOnTitle;
    }
}

//-----------------------------------------------------------------------------

boolean checkForSampleRegularity( List< String > sampleLabels,
                                  Map< String, String > samplePlatforms,
                                  List< String > platforms,
                                  Map countInfo,
                                  boolean samplesTogether,
                                  boolean verbose )
{
    assert( platforms.size() == countInfo.numPlatforms );
    for ( int i = 0; i < sampleLabels.size(); ++i )
    {
        String platform = samplePlatforms[ sampleLabels[ i ] ];
        int j = samplesTogether  ?  (i % countInfo.numPlatforms)  :
                (int)(i / countInfo.numSampleSets);
        if ( platform != platforms[ j ] )
            return false;
    }
    if ( verbose )
    {
        if ( samplesTogether )
        {
            println( "Samples are regularly grouped together" );
        }
        else
        {
            println( "Platforms are regularly grouped together" );
        }
    }
    return true;
}

//=============================================================================

void collectMatches( List< List< String > > sampleSetList,
                     Method method,
                     List< String > sampleLabels,
                     Map< String, String > sampleTitles,
                     Map< String, Map > sampleAnnotations,
                     Map countInfo,
                     boolean verbose )
{
    switch ( method )
    {
    case Method.RegularBySample:
    case Method.RegularByPlatform:
        collectRegularMatches( sampleSetList, sampleLabels,
                               countInfo, method, verbose );
        break;
    case Method.MatchOnTitle:
        collectMatchesBasedOnTitles( sampleSetList, sampleLabels,
                                     sampleTitles, verbose );
        break;
    case Method.MatchOnAnnotations:
        collectMatchesBasedOnAnnotations( sampleSetList, sampleLabels,
                                          sampleAnnotations, verbose );
        break;
    }
}

//-----------------------------------------------------------------------------

void collectRegularMatches( List< List< String > > sampleSetList,
                            List< String > sampleLabels,
                            Map countInfo, Method method,
                            boolean verbose )
{
    for ( int s = 0; s < countInfo.numSampleSets; ++s )
    {
        List< String > labels = [];
        sampleSetList.add( labels );
        for ( int p = 0; p < countInfo.numPlatforms; ++p )
        {
            int i = (method == Method.RegularBySample)  ?
                    (s * countInfo.numPlatforms  +  p)  :
                    (p * countInfo.numSampleSets  +  s);
            String label = sampleLabels[ i ];
            labels.add( label );
        }
    }
}

//-----------------------------------------------------------------------------

void collectMatchesBasedOnTitles( List< List< String > > sampleSetList,
                                  List< String > sampleLabels,
                                  Map< String, String > sampleTitles,
                                  boolean verbose )
{
    List< String > titleRegexes = //Should load these from a file!!!
    [
        /(_?[AB])$/,
        /([ _]?U133[AB])/,
        /( ?[ABab])\.txt~/,
        /sys\d+([AB])/,
        /RET( )/
    ];
    Map< String, List< String > > titleLabelsMap = [:];
    for ( String label : sampleLabels )
    {
        String modTitle = modifyTitle( sampleTitles[ (label) ], titleRegexes );
        if ( verbose )
        {
            println( "orig title: " + sampleTitles[ (label) ] );
            println( "new title:  " + modTitle );
            println( );
        }
        if ( titleLabelsMap.containsKey( modTitle ) == false )
        {
            List< String > sampleSet = [ label ];
            sampleSetList.add( sampleSet );
            titleLabelsMap[ (modTitle) ] = sampleSet;
        }
        else
        {
            List< String > sampleSet = titleLabelsMap[ (modTitle) ];
            sampleSet.add( label );
        }
    }
}

//.............................................................................

String modifyTitle( String title, List< String > regexes )
{
    String modTitle = title;
    for ( String regex : regexes )
    {
        Matcher matcher = (modTitle =~ regex);
        if ( matcher )
        {
            int s = matcher.start( 1 );
            int e = matcher.end( 1 );
            if ( (s >= 0) && (e > 0) )
            {
                String t = "";
                if ( s > 0 )
                    t = title[0..(s-1)];
                if ( e < title.size() )
                    t += title[e..-1];
                modTitle = t;
            }
        }
    }
    return modTitle;
}

//-----------------------------------------------------------------------------

void collectMatchesBasedOnAnnotations( List< List< String > > sampleSetList,
                                       List< String > sampleLabels,
                                       Map< String, Map > sampleAnnotations,
                                       boolean verbose )
{
    Map< Map, List< String > > annotsLabelsMap = [:];
    for ( String label : sampleLabels )
    {
        Map annots = sampleAnnotations[ (label) ];
        if ( annotsLabelsMap.containsKey( annots ) == false )
        {
            List< String > sampleSet = [ label ];
            sampleSetList.add( sampleSet );
            annotsLabelsMap[ (annots) ] = sampleSet;
        }
        else
        {
            List< String > sampleSet = annotsLabelsMap[ (annots) ];
            sampleSet.add( label );
        }
    }
}

//=============================================================================

void writeSampleTable( String tableFileSpec,
                       List< List< String > > sampleSetList,
                       Map< String, String > sampleTitles,
                       Map< String, String > samplePlatforms,
                       Map< String, Map > sampleAnnotations,
                       boolean verbose )
{
    new File( tableFileSpec ).withPrintWriter { writer ->
        List< String > keyList = [];
        sampleAnnotations.each { label, annots ->
            annots.each { key, value ->
                if ( keyList.contains( key ) == false )
                {
                    keyList.add( key );
                }
            }
        }

        writer.print( '"Sample ID","Title","Platform"' );
        for ( String key : keyList )
        {
            writer.print( ',"' + key + '"' );
        }
        writer.println();

        for ( List< String > sampleSet : sampleSetList )
        {
            writer.println();
            for ( String label : sampleSet )
            {
                writer.print( '"' + label + '"' );
                writer.print( ',"' + sampleTitles[ (label) ] + '"' );
                writer.print( ',"' + samplePlatforms[ (label) ] + '"' );
                for ( String key : keyList )
                {
                    writer.print( ',"' +
                                  (sampleAnnotations[ (label) ][ (key) ] ?: '') +
                                  '"' );
                }
                writer.println();
            }
        }
    }
}

//=============================================================================

void writeAnnotationMismatches( String problemsFileSpec,
                                List< List< String > > sampleSetList,
                                Map< String, Map > sampleAnnotations,
                                boolean verbose )
{
    new File( problemsFileSpec ).withPrintWriter { writer ->
        for ( List< String > sampleSet : sampleSetList )
        {
            assert( sampleSet.size() > 0 );
            List< String > keys = [];
            for ( String label : sampleSet )
            {
                Map annotations = sampleAnnotations[ (label) ];
                annotations.each { key, value ->
                    if ( keys.contains( key ) == false )
                    {
                        keys.add( key );
                    }
                }
            }
            Set< String > badKeys = new HashSet< String >();
            String label0 = sampleSet[ 0 ];
            Map annotations0 = sampleAnnotations[ (label0) ];
            for ( int s = 1; s < sampleSet.size(); ++s )
            {
                String label1 = sampleSet[ s ];
                Map annotations1 = sampleAnnotations[ (label1) ];
                for ( String key : keys )
                {
                    if ( annotations0[ (key) ] != annotations1[ (key) ] )
                    {
                        badKeys.add( key );
                    }
                }
            }
            if ( badKeys.size() > 0 )
            {
                writer.println( sampleSet.join( " + " ) );
                writer.println( "  These match:" );
                for ( String key : keys )
                {
                    if ( badKeys.contains( key ) == false )
                    {
                        writer.println( "    " + key + ": " +
                                 sampleAnnotations[ sampleSet[ 0 ] ][ (key) ] );
                    }
                }
                writer.println( "  These don't match:" );
                for ( String key : keys )
                {
                    if ( badKeys.contains( key ) )
                    {
                        writer.println( "    " + key + ":" );
                        for ( String label : sampleSet )
                        {
                            writer.println( "      " + label + ": " +
                                     sampleAnnotations[ label ][ (key) ] );
                        }
                    }
                }
                writer.println( );
            }
        }
    }
}


//*****************************************************************************


class KeyValue
{                                                                    //KeyValue
//-----------------------------------------------------------------------------

    String key = "";
    String value = "";

//=============================================================================

    static
    KeyValue split( String string, String separator )
    {
        KeyValue keyVal = new KeyValue();
        List< String > rawParts = string.split( separator, -1 );
        if ( rawParts.size() > 0 )
        {
            keyVal.key = rawParts[ 0 ].trim();
        }
        if ( rawParts.size() > 1 )
        {
            keyVal.value = rawParts[ 1..-1 ].join( separator ).trim();
        }
        return keyVal;
    }

//-----------------------------------------------------------------------------
}                                                                    //KeyValue


//*****************************************************************************

