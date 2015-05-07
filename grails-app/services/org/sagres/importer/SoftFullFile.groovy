/*
  SoftFullFile.groovy

  Routines for importing Soft "full" (GDS) microarray data files.
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class SoftFullFile
{                                                                //SoftFullFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataTableInfo tableInfo,
        List< ArrayDataSample > samples ->

            tableInfo.separator = TextTableSeparator.TAB;
            int state = 0; //0=pre-data, 1=header, 2=data, 3=post-data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "!dataset_table_begin" ) )
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
                    if ( line.startsWith( "!dataset_table_end" ) )
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
        return SoftFile.getChipType( "!dataset_platform", "^DATASET",
                                     fileSpec );
    }

//=============================================================================

    static
    ArrayDataAnnotations getAnnotations( String fileSpec )
    {
        ArrayDataAnnotations annots = new ArrayDataAnnotations();
        boolean processingSubset = false;
        KeyValue subsetKeyVal;
        List< String > sampleLabels;

        new File( fileSpec ).eachLine { line, lineNum ->
            if ( line.startsWith( "!dataset_" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                if ( annots.series.containsKey( kv.key ) == false )
                {
                    annots.series[ kv.key ] = [ kv.value ];
                }
                else if ( annots.series[ kv.key ].contains( kv.value ) == false )
                {
                    annots.series[ kv.key ].add( kv.value );
                }
            }
            else if ( line.startsWith( "^" ) )
            {
                if ( processingSubset )
                {
                    sampleLabels.each { label ->
                        ArrayDataSampleAnnotations sample =
                                annots.samples.find { it.label == label }
                        if ( sample == null )
                        {
                            sample = new ArrayDataSampleAnnotations();
                            annots.samples.add( sample );
                            sample.label = label;
                        }
                        sample.annotations[ subsetKeyVal.key ] =
                                subsetKeyVal.value;
                    }
                }
                if ( line.startsWith( "^SUBSET" ) )
                {
                    subsetKeyVal = new KeyValue();
                    sampleLabels = [];
                    processingSubset = true;
                }
                else
                {
                    processingSubset = false;
                }
            }
            else if ( line.startsWith( "!subset_type" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                subsetKeyVal.key = kv.value;
            }
            else if ( line.startsWith( "!subset_description" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                subsetKeyVal.value = kv.value;
            }
            else if ( line.startsWith( "!subset_sample_id" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                sampleLabels = kv.value.split( "," );
            }
        }
        return annots;
    }

//-----------------------------------------------------------------------------
}                                                                //SoftFullFile


//*****************************************************************************
