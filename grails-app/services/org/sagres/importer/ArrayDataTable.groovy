/*
  ArrayDataTable.groovy

  Array data in tabular form.
*/

package org.sagres.importer;

import java.util.regex.Matcher;


//*****************************************************************************


class ArrayDataTableInfo
extends TextTableInfo
{                                                          //ArrayDataTableInfo
//-----------------------------------------------------------------------------

    Integer probeIdColumn;
    List< ArrayDataSampleColumns > sampleColsList = [];
    Integer numProbes;

//-----------------------------------------------------------------------------
}                                                          //ArrayDataTableInfo


//=============================================================================


class ArrayDataSampleColumns
{                                                      //ArrayDataSampleColumns
//-----------------------------------------------------------------------------

    Integer signalColumn;
    Integer pValColumn;
    Integer callColumn;
    Integer numColumns;
    
//-----------------------------------------------------------------------------
}                                                      //ArrayDataSampleColumns


//*****************************************************************************


class ArrayDataTableRowData
{                                                       //ArrayDataTableRowData
//-----------------------------------------------------------------------------

    String probeId;
    List< ArrayDataDatum > sampleData = [];

//-----------------------------------------------------------------------------
}                                                       //ArrayDataTableRowData


//*****************************************************************************


class ArrayDataTable
{                                                              //ArrayDataTable
//-----------------------------------------------------------------------------

    static
    void parseHeader( String header, ArrayDataTableInfo tableInfo,
                      List< ArrayDataSample > samples,
                      String probeIdPattern, String samplePattern,
                      String signalPattern = null, String pValPattern = null,
                      String callPattern = null )
    {
        String[] columnNames =
                TextTable.splitRow( header, tableInfo.separator );
        tableInfo.numColumns = columnNames.size();
        if ( tableInfo.numColumns == 0 )
        { //This should be impossible at this point, but just in case...
            throw new ImportException(
                "Bad file format: No column names found" );
        }

        tableInfo.sampleColsList = [];
        String lastSampleLabel = "";
        ArrayDataSample sample;
        ArrayDataSampleColumns sampleCols;
        for ( int i = 0; i < tableInfo.numColumns; ++i )
        {
            String colName = columnNames[ i ];
            if ( ((probeIdPattern != null) && (colName =~ probeIdPattern))
                 || (i == 0) )
            {
                if ( tableInfo.probeIdColumn == null )
                {
                    tableInfo.probeIdColumn = i;
                }
                else
                {
                    println( "Two possible probe ID columns (" +
                             tableInfo.probeIdColumn + " and " + i +
                             "). Using the first." );
                }
            }
            else
            {
                Matcher m = (colName =~ samplePattern);
                if ( m )
                {
                    if ( (m.count != 1) || (m[ 0 ].size() < 2) )
                    {   //This would be an error in our regex
                        String msg = "Error evaluating regex on '" +
                                columnNames[ i ] + "' (column " + i + ")";
                        throw new ImportException( msg );
                    }
                    String sampleLabel = m[ 0 ][ 1 ];

                    if ( sampleLabel != lastSampleLabel )
                    {
                        sample = null;
                        sampleCols = null;
                        for ( int j = 0; j < samples.size(); ++j )
                        {
                            if ( samples[ j ].label == sampleLabel )
                            {
                                sample = samples[ j ];
                                sampleCols = tableInfo.sampleColsList[ j ];
                                break;
                            }
                        }
                        if ( sample == null )
                        {
                            sample = new ArrayDataSample( );
                            samples.add( sample );
                            sampleCols = new ArrayDataSampleColumns( );
                            tableInfo.sampleColsList.add( sampleCols );
                            sample.label = lastSampleLabel = sampleLabel;
                            sampleCols.numColumns = 0;
                        }
                    }
                    ++sampleCols.numColumns;

                    if ( signalPattern && (colName =~ signalPattern) )
                    {
                        if ( sampleCols.signalColumn != null )
                        {
                            String msg = "Multiple signal columns (" +
                                    sampleCols.signalColumn +
                                    " and " + i + ") for sample " +
                                    sample.label
                            throw new ImportException( msg );
                        }
                        sampleCols.signalColumn = i;
                    }
                    else if ( pValPattern && (colName =~ pValPattern) )
                    {
                        if ( sampleCols.pValColumn != null )
                        {
                            String msg = "Multiple pVal columns (" +
                                    sampleCols.pValColumn +
                                    " and " + i + ") for sample " +
                                    sample.label
                            throw new ImportException( msg );
                        }
                        sampleCols.pValColumn = i;
                    }
                    else if ( callPattern && (colName =~ callPattern) )
                    {
                        if ( sampleCols.callColumn != null )
                        {
                            String msg = "Multiple call columns (" +
                                    sampleCols.callColumn +
                                    " and " + i + ") for sample " +
                                    sample.label
                            throw new ImportException( msg );
                        }
                        sampleCols.callColumn = i;
                    }

                }
            }
        }

        if ( tableInfo.probeIdColumn == null )
        {
            throw new ImportException( "No Probe ID column found" );
        }
    }

//=============================================================================

    static
    ArrayDataTableRowData parseRow( String[] fields,
                                    ArrayDataTableInfo tableInfo )
    {
        ArrayDataTableRowData rowData = new ArrayDataTableRowData();

        rowData.probeId =
                Importer.abbreviateProbeId( fields[ tableInfo.probeIdColumn ] );

        tableInfo.sampleColsList.each { cols ->
            ArrayDataDatum datum = new ArrayDataDatum( );
            if ( cols.signalColumn )
            {
                datum.signal =
                        Importer.parseDouble( fields[ cols.signalColumn ] );
            }
            if ( cols.pValColumn )
            {
                datum.pVal =
                        Importer.parseDouble( fields[ cols.pValColumn ] );
            }
            if ( cols.callColumn )
            {
                datum.call = fields[ cols.callColumn ];
            }
            rowData.sampleData.add( datum );
        }
        return rowData;
    }
    
//=============================================================================

    
//-----------------------------------------------------------------------------
}                                                              //ArrayDataTable


//*****************************************************************************


class ArrayDataTableFormatValidator
{                                               //ArrayDataTableFormatValidator
//-----------------------------------------------------------------------------

    void validateTableInfo( ArrayDataTableInfo tableInfo )
    {
        if ( tableInfo.sampleColsList.size() == 0 )
        {
            throw new ImportException( "No samples listed" );
        }
        int colsPerSample = -1;
        for ( ArrayDataSampleColumns cols : tableInfo.sampleColsList )
        {
            if ( (cols.signalColumn == null) && signalColumnRequired )
            {
                throw new ImportException( "No signal column for sample " +
                                           cols.sampleName );
            }
            if ( (cols.pValColumn == null) && pValColumnRequired )
            {
                throw new ImportException( "No pVal column for sample " +
                                           cols.sampleName );
            }
            if ( (cols.pValColumn == null) && (cols.callColumn == null) &&
                 detectionColumnRequired )
            {
                throw new ImportException( "No detection column for sample " +
                                           cols.sampleName );
            }
            if ( uniformNumColumnsRequired )
            {
                if ( colsPerSample == -1 )
                {
                    colsPerSample = cols.numColumns;
                }
                else if ( cols.numColumns != colsPerSample )
                {
                    String msg = "Sample " + cols.sampleName + " has " +
                            cols.numColumns + " columns; other samples had " +
                            colsPerSample;
                    throw new ImportException( msg );
                }
            }
        }
    }

//=============================================================================

    protected boolean signalColumnRequired = true;
    protected boolean pValColumnRequired = false;
    protected boolean detectionColumnRequired = false; //p-val or call flag
    protected boolean uniformNumColumnsRequired = true;

//-----------------------------------------------------------------------------
}                                               //ArrayDataTableFormatValidator


//*****************************************************************************


class ArrayDataTableValidator
extends ArrayDataValidator
{                                                     //ArrayDataTableValidator
//-----------------------------------------------------------------------------

    void init( ArrayDataTableInfo tableInfo,
               Map< String, Boolean > chipProbes,
               List< String > warnings )
    {
        m_tableInfo = tableInfo;
        m_chipProbes = chipProbes;
        m_warnings = warnings;
    }

//=============================================================================

    void beforeTable( )
    {
        start( );
    }

//=============================================================================

    void validateRow( String[] fields, int lineNum )
    {
        if ( checkFieldCount( m_tableInfo, fields, lineNum ) == false )
        {
            return;
        }

        ArrayDataTableRowData rowData =
                ArrayDataTable.parseRow( fields, m_tableInfo );
        assert( rowData.sampleData.size() ==
                m_tableInfo.sampleColsList.size() );

        checkProbeId( rowData.probeId );

        boolean probeError = false;
        for ( ArrayDataDatum datum : rowData.sampleData )
        {
            if ( checkDatum( datum, rowData.probeId, lineNum ) == false )
            {
                probeError = true;
            }
        }
        if ( probeError )
        {
            ++m_numProbeRowErrors;
        }
    }

//=============================================================================

    void afterTable( )
    {
        finish( m_tableInfo.sampleColsList.size(), m_tableInfo.numRows );
    }

//=============================================================================

    protected ArrayDataTableInfo m_tableInfo;
    
//-----------------------------------------------------------------------------
}                                                     //ArrayDataTableValidator


//*****************************************************************************


class ArrayDataTableBulkLoadBuilder
extends FileBuilderFromTextTable
{                                               //ArrayDataTableBulkLoadBuilder
//-----------------------------------------------------------------------------

    ArrayDataTableBulkLoadBuilder( String bulkLoadSpec,
                                   ArrayDataTableInfo tableInfo,
                                   List< ArrayDataSample > samples,
                                   Closure fixupRowData = null )
    {
        super( bulkLoadSpec );
        m_tableInfo = tableInfo;
        m_samples = samples;
        m_fixupRowData = fixupRowData;
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            return;
        }
        ArrayDataTableRowData rowData =
                ArrayDataTable.parseRow( fields, m_tableInfo );
        if ( m_fixupRowData )
        {
            m_fixupRowData( rowData );
        }
        for ( int i = 0; i < m_samples.size(); ++i )
        {
            ArrayDataSample sample = m_samples[ i ];
            ArrayDataDatum datum = rowData.sampleData[ i ];
            ArrayDataImporter.writeBulkLoadLine( m_writer, rowData.probeId,
                                                 sample, datum );
            if ( datum.signal )
            {
                sample.signalTotal += datum.signal;
            }
        }
    }

//=============================================================================

    private ArrayDataTableInfo m_tableInfo;
    private List< ArrayDataSample > m_samples;
    private Closure m_fixupRowData;

//-----------------------------------------------------------------------------
}                                               //ArrayDataTableBulkLoadBuilder


//*****************************************************************************
