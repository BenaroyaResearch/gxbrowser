/*
  ArrayDataExporter.groovy

  Routines for exporting microarray and similar data
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class ArrayDataExporter
{                                                           //ArrayDataExporter
//-----------------------------------------------------------------------------

    static
    void writeHeader( PrintWriter writer,
                      List< ArrayDataSample > samples,
                      TextTableSeparator separator = TextTableSeparator.TAB,
                      boolean writeIds = true )
    {
        List< String > fields = [ "ProbeID" ];
        samples.each { sample ->
            String s =
                   (writeIds && sample.id)  ?  "X" + sample.id  :  sample.label;
            fields.add( s + ".AVG_Signal" );
            fields.add( s + ".Detection Pval" );
        }
        writer.print( TextTable.joinRow( fields, separator, false ) );
        //each subsequent line will begin with a new-line
    }

//=============================================================================

    static
    void writeDataRow( PrintWriter writer,
                       ArrayDataTableRowData data,
                       ChipType chipType,
                       TextTableSeparator separator = TextTableSeparator.TAB )
    {
        List< String > fields =
                [ Importer.expandProbeId( data.probeId, chipType ) ];
        data.sampleData.each { datum ->
            fields.add( (datum.signal != null) ? datum.signal : "" );
            fields.add( (datum.pVal != null) ? datum.pVal : "" );
        }
        writer.print( "\n" );
        writer.print( TextTable.joinRow( fields, separator, false ) );
    }

//-----------------------------------------------------------------------------
}                                                           //ArrayDataExporter


//*****************************************************************************


class ArrayDataTableExporter
extends FileBuilderFromTextTable
{                                                      //ArrayDataTableExporter
//-----------------------------------------------------------------------------

    ArrayDataTableExporter( String outFileSpec,
                            List< ArrayDataSample > samples,
                            ArrayDataTableInfo tableInfo,
                            ChipType chipType,
                            TextTableSeparator separator = TextTableSeparator.TAB,
                            boolean writeIds = true )
    {
        super( outFileSpec );
        m_samples = samples;
        m_tableInfo = tableInfo;
        m_chipType = chipType;
        m_separator = separator;
        m_writeIds = writeIds;
    }

//=============================================================================

    @Override
    void beforeTable( )
    {
        super.beforeTable( );
        ArrayDataExporter.writeHeader( m_writer, m_samples,
                                       m_separator, m_writeIds );
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        ArrayDataTableRowData rowData =
                ArrayDataTable.parseRow( fields, m_tableInfo );
        ArrayDataExporter.writeDataRow( m_writer, rowData, m_chipType,
                                        m_separator );
    }

//=============================================================================

    String m_outFileSpec;
    List< ArrayDataSample > m_samples;
    ArrayDataTableInfo m_tableInfo;
    ChipType m_chipType;
    TextTableSeparator m_separator;
    boolean m_writeIds;
    
//-----------------------------------------------------------------------------
}                                                      //ArrayDataTableExporter


//*****************************************************************************


class ArrayDataMultitableExporter
extends FileBuilderFromTextMultitable
{                                                 //ArrayDataMultitableExporter
//-----------------------------------------------------------------------------

    ArrayDataMultitableExporter( String outFileSpec,
                                 ArrayDataMultitableInfo tableInfo,
                                 ChipType chipType,
                                 TextTableSeparator separator = TextTableSeparator.TAB,
                                 boolean writeIds = true )
    {
        super( outFileSpec );
        m_tableInfo = tableInfo;
        m_chipType = chipType;
        m_separator = separator;
        m_writeIds = writeIds;
        m_probeIds = new TreeSet< String >();
        m_arrayData = [:];
        tableInfo.sampleTables.each { sampleTable ->
            m_arrayData[ sampleTable.sample ] = [:];
        }
    }

//=============================================================================

    @Override
    void beforeTables( )
    {
        super.beforeTables( );
        List< ArrayDataSample > samples = m_tableInfo.sampleTables*.sample;
        ArrayDataExporter.writeHeader( m_writer, samples,
                                       m_separator, m_writeIds );
    }


//=============================================================================

    @Override
    void processRow( TextTableInfo tableInfo, String[] fields, int lineNum )
    {
        ArrayDataSampleTableInfo sampleTableInfo =
                (ArrayDataSampleTableInfo) tableInfo;
        assert( sampleTableInfo );
        ArrayDataSampleTableDatum probeDatum =
                ArrayDataMultitable.parseRow( fields, sampleTableInfo );
        m_probeIds.add( probeDatum.probeId );
        ArrayDataDatum datum = new ArrayDataDatum( signal: probeDatum.signal,
                                                   pVal: probeDatum.pVal,
                                                   call: probeDatum.call );
        m_arrayData[ sampleTableInfo.sample ][ probeDatum.probeId ] = datum;
    }

//=============================================================================

    @Override
    void afterTables( )
    {
        List< ArrayDataSample > samples = m_tableInfo.sampleTables*.sample;
        for ( String probeId: m_probeIds )
        {
            ArrayDataTableRowData rowData =
                    new ArrayDataTableRowData( probeId: probeId,
                                               sampleData: [] );
            for ( ArrayDataSample sample: samples )
            {
                ArrayDataDatum datum = m_arrayData[ sample ][ probeId ];
                rowData.sampleData.add( datum );
            }
            ArrayDataExporter.writeDataRow( m_writer, rowData, m_chipType,
                                            m_separator );
        }
        super.afterTables( );
    }

//=============================================================================

    ArrayDataMultitableInfo m_tableInfo;
    ChipType m_chipType;
    TextTableSeparator m_separator;
    boolean m_writeIds;
    Set< String > m_probeIds;
    Map< ArrayDataSample, Map< String, ArrayDataDatum > > m_arrayData;

//-----------------------------------------------------------------------------
}                                                 //ArrayDataMultitableExporter


//*****************************************************************************
