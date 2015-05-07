/*
  SoftFamilyFile.groovy

  Routines for importing Soft "family" (GSE) microarray data files.
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class SoftFamilyFile
{                                                              //SoftFamilyFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataMultitableInfo tableInfo,
        Map< String, String > sampleLabelMap = null ->

            int state = 0; //0=outside samples, 1=sample metadata
                           // 2=column names, 3=data
            ArrayDataSampleTableInfo sampleTable;
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "^SAMPLE" ) )
                    {
                        sampleTable = new ArrayDataSampleTableInfo();
                        tableInfo.sampleTables.add( sampleTable );
                        sampleTable.separator = TextTableSeparator.TAB;
                        KeyValue keyVal = KeyValue.split( line, "=" );
                        String label = keyVal.value;
                        if ( label == "" )
                        {
                            String msg = "No sample label on line " + lineNum;
                            throw new ImportException( msg );
                        }
                        if ( sampleLabelMap )
                        {
                            label = sampleLabelMap[ label ];
                            for ( ArrayDataSampleTableInfo adsti :
                                          tableInfo.sampleTables )
                            {
                                if ( adsti.sample?.label == label )
                                {
                                    sampleTable.sample = adsti.sample;
                                }
                            }
                        }
                        if ( sampleTable.sample == null )
                        {
                            sampleTable.sample = new ArrayDataSample();
                            sampleTable.sample.label = label;
                        }
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    if ( line.startsWith( "!sample_table_begin" ) )
                    {
                        state = 2;
                    }
                }
                else if ( state == 2 )
                {
                    String[] colNames =
                            TextTable.splitRow( line, sampleTable.separator );
                    colNames.eachWithIndex { colName, i ->
                        switch ( colName.toUpperCase() )
                        {
                        case "ID_REF":
                            sampleTable.probeIdColumn = i;
                            break;
                        case "VALUE":
                            sampleTable.signalColumn = i;
                            break;
                        case "DETECTION":
                            sampleTable.pValColumn = i;
                            break;
                        case "ABS_CALL":
                            sampleTable.callColumn = i;
                            break;
                        }
                    }
                    if ( sampleTable.probeIdColumn == null )
                    {
                        String msg = "No probe ID column for sample " +
                                sampleTable.sample.label;
                        throw new ImportException( msg );
                    }
                    if ( sampleTable.signalColumn == null )
                    {
                        String msg = "No signal column for sample " +
                                sampleTable.sample.label;
                        throw new ImportException( msg );
                    }
                    sampleTable.numColumns = colNames.size();
                    sampleTable.firstRow = lineNum;
                    sampleTable.numRows = 0;
                    state = 3;
                }
                else if ( state == 3 )
                {
                    if ( line.startsWith( "!sample_table_end" ) )
                    {
                        state = 0;
                    }
                    else
                    {
                        ++sampleTable.numRows;
                    }
                }
            }
    }

//=============================================================================

    static
    ChipType getChipType( String fileSpec )
    {
        Map chipInfo = SoftFile.getChipTypes( "!Series_platform_id", "^SERIES",
                                              fileSpec );
        if ( chipInfo.unsupportedGplIds.size() > 0 )
        {
            throw new ImportException( "Unsupported GPL platform:" +
                                       chipInfo.unsupportedGplIds[ 0 ] );
        }
        if ( chipInfo.chipTypes.size() == 1 )
        {
            return chipInfo.chipTypes[ 0 ];
        }
        //Currently only the combination U133A + U133B is supported.
        else if ( (chipInfo.chipTypes.size() == 2) &&
             (chipInfo.chipTypes[ 0 ].name == "HG-U133A") &&
             (chipInfo.chipTypes[ 1 ].name == "HG-U133B") )
        {
            return ChipType.findByName( "HG-U133A+U133B" );
        }
        else
        {
            throw new ImportException( "Unsupported platform combination" );
        }
    }

//=============================================================================

    static
    ArrayDataAnnotations getAnnotations( String fileSpec,
                                  Map< String, String > sampleLabelMap = null )
    {
        ArrayDataAnnotations annots = new ArrayDataAnnotations();
        ArrayDataSampleAnnotations sample;

        new File( fileSpec ).eachLine { line, lineNum ->
            if ( line.startsWith( "!Series_" ) )
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
            else if ( line.startsWith( "^SAMPLE" ) )
            {
                sample = null;
                KeyValue kv = KeyValue.split( line, "=" );
                String label = kv.value;
                if ( label == "" )
                {
                    String msg = "No sample ID on line " + lineNum;
                    throw new ImportException( msg );
                }
                if ( sampleLabelMap )
                {
                    label = sampleLabelMap[ label ];
                    for ( ArrayDataSampleAnnotations adsa : annots.samples )
                    {
                        if ( adsa.label == label )
                        {
                            sample = adsa;
                        }
                    }
                }
                if ( sample == null )
                {
                    sample = new ArrayDataSampleAnnotations( label: label );
                    annots.samples.add( sample );
                }
            }
            else if ( line.startsWith( "!Sample_title" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                sample.annotations[ kv.key ] = kv.value;
            }
            else if ( line.startsWith( "!Sample_characteristics" ) )
            {
                KeyValue kv = KeyValue.split( line, "=" );
                if ( kv.value == "" )
                {
                    String msg = "No value for " + kv.key +
                            " on line " + lineNum;
                    throw new ImportException( msg );
                }
                kv = KeyValue.split( kv.value, ":" );
                if ( kv.value == "" )
                {
                    kv.value = "True";
                }
                sample.annotations[ kv.key ] = kv.value;
            }
            else if ( line.startsWith( "!Sample_" ) )
            {
                //Although given at the sample level, most of these annotations
                // say more about the series as a whole, most of the time.
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
            else if ( line.startsWith( "#VALUE" ) )
            {
                //This sometimes indicates normalization, etc.
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
        }
        return annots;
    }

//=============================================================================

    
//-----------------------------------------------------------------------------
}                                                              //SoftFamilyFile


//*****************************************************************************
