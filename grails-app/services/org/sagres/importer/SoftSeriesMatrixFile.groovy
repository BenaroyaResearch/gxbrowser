/*
  SoftSeriesMatrixFile.groovy

  Routines for importing Soft "series matrix" (GSE) microarray data files.
*/

package org.sagres.importer;

import java.util.regex.Matcher;
import common.chipInfo.ChipType;
import common.chipInfo.GplChipType;


//*****************************************************************************


class SoftSeriesMatrixFile
{                                                        //SoftSeriesMatrixFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataTableInfo tableInfo,
        List< ArrayDataSample > samples ->

            tableInfo.separator = TextTableSeparator.TSV;
            int state = 0; //0=pre-data, 1=header, 2=data, 3=post-data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "!series_matrix_table_begin" ) )
                    {
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    processHeader( line, tableInfo, samples );
                    tableInfo.firstRow = lineNum;
                    tableInfo.numRows = 0;
                    state = 2;
                }
                else if ( state == 2 )
                {
                    if ( line.startsWith( "!series_matrix_table_end" ) )
                    {
                        state = 3;
                    }
                    else
                    {
                        ++tableInfo.numRows;
                    }
                }
            }
    }

//-----------------------------------------------------------------------------

    private
    static
    void processHeader( String header, ArrayDataTableInfo tableInfo,
                        List< ArrayDataSample > samples )
    {
        ArrayDataTable.parseHeader( header, tableInfo, samples,
                                    /(?i)^ID_REF$/,
                                    /^(GSM.+)$/,
                                    /^GSM.+$/,
                                    null );
    }

//-----------------------------------------------------------------------------

    private
    static
    void validateTableInfo( ArrayDataTableInfo tableInfo )
    {
        if ( tableInfo.probeIdColumn != 0 )
        {
            String msg = "Bad file format: first column is not probe ID";
            throw new ImportException( msg );
        }
        int colsPerSample = -1;
        tableInfo.sampleColsList.each { cols ->
            if ( cols.signalColumn == null )
            {
                throw new ImportException( "No signal column for sample " +
                                           cols.sampleName );
            }
            if ( colsPerSample == -1 )
            {
                colsPerSample = cols.numColumns;
            }
            else if ( cols.numColumns != colsPerSample )
            {  //expecting same number of columns for all samples
                String msg = "Sample " + cols.sampleName + " has " +
                        cols.numColumns + " columns; other samples had " +
                        colsPerSample;
                throw new ImportException( msg );
            }
        }
    }

//=============================================================================

    static
    ChipType getChipType( String fileSpec )
    {
        Matcher m = (fileSpec =~ /.*(GPL\d+)[^\/]*/);
        String gplId;
        if ( m && (m.count == 1) && (m[ 0 ].size() >= 2) )
        {
            gplId = m[ 0 ][ 1 ];
        }
        else
        {
            //If the platform isn't given in the file name, there should be
            // just one platform for the series and it will be given internally.
            gplId = getSeriesPlatformId( fileSpec );
        }
        GplChipType gplChipType = GplChipType.findByGplId( gplId );
        if ( gplChipType == null )
        {
            throw new ImportException( "Unsupported platform: " + gplId );
        }
        return gplChipType.chipType;
    }

//.............................................................................

    private
    static
    String getSeriesPlatformId( String fileSpec )
    {
        String gplId;
        try
        {
            def inputFile = new BufferedReader(
                new FileReader( fileSpec ) );
            String line = inputFile.readLine( );
            int lineNum = 0;
            while ( line != null )
            {
                ++lineNum;
                if ( line.startsWith( "!Series_platform_id" ) )
                {
                    String[] fields =
                            TextTable.splitRow( line, TextTableSeparator.TSV );
                    if ( fields.size() != 2 )
                    {
                        String msg = "No value listed for Series_platform_id";
                        throw new ImportException( msg );
                    }
                    if ( gplId )
                    {
                        String msg = "Multiple Series platform IDs" +
                                " (" + gplId + " and " + fields[ 1 ] + ")";
                        throw new ImportException( msg );
                    }
                    gplId = fields[ 1 ];
                }
                line = inputFile.readLine( );
            }
            if ( gplId == null )
            {
                String msg = "No Series_platform_id found";
                throw new ImportException( msg );
            }
        }
        catch ( FileNotFoundException exc )
        {
            String msg = "Unable to open " + fileSpec;
            throw new ImportException( msg );
        }
        catch ( IOException exc )
        {
            String msg = "I/O error occurred reading " + fileSpec;
            throw new ImportException( msg );
        }
        assert( gplId );
        return gplId;
    }

//=============================================================================

    static
    ArrayDataAnnotations getAnnotations( String fileSpec )
    {
        ArrayDataAnnotations annots = new ArrayDataAnnotations();
        new File( fileSpec ).eachLine { line, lineNum ->
            if ( line.startsWith( "!Series_" ) )
            {
                String[] fields =
                        TextTable.splitRow( line, TextTableSeparator.TSV );
                if ( fields.size() == 2 )
                {
                    String key = fields[ 0 ];
                    String value = fields[ 1 ];
                    if ( annots.series.containsKey( key ) == false )
                    {
                        annots.series[ key ] = [ value ];
                    }
                    else if ( annots.series[ key ].contains( value ) == false )
                    {
                        annots.series[ key ].add( value );
                    }
                }
                //else throw exception?
            }
            else if ( line.startsWith( "!Sample_geo_accession" ) )
            {
                String[] fields =
                        TextTable.splitRow( line, TextTableSeparator.TSV );
                setOrCheckAnnotsSamplesSize( annots, fields.size() - 1 );
                for ( int i = 1; i < fields.size(); ++i )
                {
                    annots.samples[ i - 1 ].label = fields[ i ];
                }
            }
            else if ( line.startsWith( "!Sample_title" ) )
            {
                String[] fields =
                        TextTable.splitRow( line, TextTableSeparator.TSV );
                setOrCheckAnnotsSamplesSize( annots, fields.size() - 1 );
                for ( int i = 1; i < fields.size(); ++i )
                {
                    annots.samples[ i - 1 ].annotations[ fields[ 0 ] ] =
                            fields[ i ];
                }
            }
            else if ( line.startsWith( "!Sample_characteristics" ) )
            {
                String[] fields =
                        TextTable.splitRow( line, TextTableSeparator.TSV );
                setOrCheckAnnotsSamplesSize( annots, fields.size() - 1 );
                for ( int i = 1; i < fields.size(); ++i )
                {
                    KeyValue kv = KeyValue.split( fields[ i ], ":" );
                    annots.samples[ i - 1 ].annotations[ kv.key ] = kv.value;
                }
            }
            else if ( line.startsWith( "!Sample_" ) )
            {
                //Although given at the sample level, most of these annotations
                // say more about the series as a whole, most of the time.
                String[] fields =
                        TextTable.splitRow( line, TextTableSeparator.TSV );
                String key = fields[ 0 ];
                for ( int i = 1; i < fields.size(); ++i )
                {
                    String value = fields[ i ];
                    if ( annots.series.containsKey( key ) == false )
                    {
                        annots.series[ key ] = [ value ];
                    }
                    else if ( annots.series[ key ].contains( value ) == false )
                    {
                        annots.series[ key ].add( value );
                    }
                }
            }
        }
        return annots;
    }

//.............................................................................

    private
    static
    void setOrCheckAnnotsSamplesSize( ArrayDataAnnotations annots,
                                      int numSamples )
    {
        if ( annots.samples.size() == 0 )
        {
            numSamples.times {
                annots.samples.add( new ArrayDataSampleAnnotations() );
            }
        }
        else
        {
            if ( annots.samples.size() != numSamples )
            {
                String msg = "Sample annotation size mismatch";
                throw new ImportException( msg );
            }
        }
    }

//-----------------------------------------------------------------------------
}                                                        //SoftSeriesMatrixFile


//*****************************************************************************
