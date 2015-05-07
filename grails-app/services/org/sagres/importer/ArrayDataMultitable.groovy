/*
  ArrayDataMultitable.groovy

  Array data separated into several tables
*/

package org.sagres.importer;


//*****************************************************************************


class ArrayDataMultitableInfo
{                                                     //ArrayDataMultitableInfo
//-----------------------------------------------------------------------------

    List< ArrayDataSampleTableInfo > sampleTables = [];

//-----------------------------------------------------------------------------
}                                                     //ArrayDataMultitableInfo


//=============================================================================


class ArrayDataSampleTableInfo
extends TextTableInfo
{                                                    //ArrayDataSampleTableInfo
//-----------------------------------------------------------------------------

    ArrayDataSample sample;
    Integer probeIdColumn;
    Integer signalColumn;
    Integer pValColumn;
    Integer callColumn;

//-----------------------------------------------------------------------------
}                                                    //ArrayDataSampleTableInfo


//*****************************************************************************


class ArrayDataSampleTableDatum
extends ArrayDataDatum
{                                                   //ArrayDataSampleTableDatum
//-----------------------------------------------------------------------------

    String probeId;

//-----------------------------------------------------------------------------
}                                                   //ArrayDataSampleTableDatum


//*****************************************************************************


class ArrayDataMultitable
{                                                         //ArrayDataMultitable
//-----------------------------------------------------------------------------

    static
    ArrayDataSampleTableDatum parseRow( String[] fields,
                                        ArrayDataSampleTableInfo tableInfo )
    {
        ArrayDataSampleTableDatum datum = new ArrayDataSampleTableDatum();
        datum.probeId =
                Importer.abbreviateProbeId( fields[ tableInfo.probeIdColumn ] );
        if ( tableInfo.signalColumn )
        {
            datum.signal =
                    Importer.parseDouble( fields[ tableInfo.signalColumn ] );
        }
        if ( tableInfo.pValColumn )
        {
            datum.pVal =
                    Importer.parseDouble( fields[ tableInfo.pValColumn ] );
        }
        if ( tableInfo.callColumn )
        {
            datum.call = fields[ tableInfo.callColumn ];
        }
        return datum;
    }

//=============================================================================

    static
    Closure fixupDatum = { ArrayDataSampleTableDatum datum ->
        ArrayDataImporter.convertCallToPVal( datum );
    }

//-----------------------------------------------------------------------------
}                                                         //ArrayDataMultitable


//*****************************************************************************


class ArrayDataMultitableValidator
extends ArrayDataValidator
{                                                //ArrayDataMultitableValidator
//-----------------------------------------------------------------------------

    void init( ArrayDataMultitableInfo tableInfo,
               Map< String, Boolean > chipProbes,
               List< String > warnings )
    {
        m_tableInfo = tableInfo;
        m_chipProbes = chipProbes;
        m_warnings = warnings;
    }

//=============================================================================

    void beforeTables( )
    {
        start( );
    }

//=============================================================================

    void validateRow( ArrayDataSampleTableInfo tableInfo,
                      String[] fields, int lineNum )
    {
        if ( checkFieldCount( tableInfo, fields, lineNum ) == false )
        {
            return;
        }

        ArrayDataSampleTableDatum datum =
                ArrayDataMultitable.parseRow( fields, tableInfo );

        checkProbeId( datum.probeId );

        if ( checkDatum( datum, datum.probeId, lineNum ) == false )
        {
            ++m_numProbeRowErrors;
        }
    }

//=============================================================================

    void afterTables( )
    {
        int maxRows = m_tableInfo.sampleTables*.numRows.max();
        finish( m_tableInfo.sampleTables.size(), maxRows );
    }

//=============================================================================

    ArrayDataMultitableInfo m_tableInfo;
    
//-----------------------------------------------------------------------------
}                                                //ArrayDataMultitableValidator


//*****************************************************************************


class ArrayDataMultitableBulkLoadBuilder
extends FileBuilderFromTextMultitable
{                                          //ArrayDataMultitableBulkLoadBuilder
//-----------------------------------------------------------------------------

    ArrayDataMultitableBulkLoadBuilder( String bulkLoadSpec,
                                        Closure fixupDatum = null )
    {
        super( bulkLoadSpec );
        m_fixupDatum = fixupDatum;
    }

//=============================================================================

    @Override
    void processRow( TextTableInfo tableInfo,
                     String[] fields, int lineNum )
    {
        ArrayDataSampleTableInfo sampleTable =
                (ArrayDataSampleTableInfo) tableInfo;
        assert( sampleTable );
        if ( fields.size() != tableInfo.numColumns )
        {
            return;
        }
        ArrayDataSampleTableDatum datum =
                ArrayDataMultitable.parseRow( fields, sampleTable );
        if ( m_fixupDatum )
        {
            m_fixupDatum( datum );
        }
        ArrayDataImporter.writeBulkLoadLine( m_writer, datum.probeId,
                                             sampleTable.sample, datum );
        if ( datum.signal != null )
        {
            sampleTable.sample.signalTotal += datum.signal;
        }
    }

//=============================================================================

    protected Closure m_fixupDatum;

//-----------------------------------------------------------------------------
}                                          //ArrayDataMultitableBulkLoadBuilder


//*****************************************************************************
