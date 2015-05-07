/*
  RnaSeqTable.groovy

  Functions for importing RNA Seq gene counts.
*/

package org.sagres.importer;

import org.sagres.sampleSet.SampleSet;


//*****************************************************************************


class RnaSeqTable
{                                                                 //RnaSeqTable
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataTableInfo tableInfo,
        List< ArrayDataSample > samples ->

            tableInfo.skipRow = skipRow;
            boolean inHeader = true;
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( inHeader )
                {
                    if ( line.size() > 0 )
                    {
                        processHeader( line, tableInfo, samples );
                        tableInfo.firstRow = lineNum;
                        tableInfo.numRows = 0;
                        inHeader = false;
                    }
                }
                else
                {
                    ++tableInfo.numRows;
                }
            }
    }

//-----------------------------------------------------------------------------

    private
    static
    void processHeader( String header, ArrayDataTableInfo tableInfo,
                        List< ArrayDataSample > samples )
    {
        if ( header.contains( "\t" ) )
        {
            tableInfo.separator = TextTableSeparator.TAB;
        }
        else
        {
            tableInfo.separator = TextTableSeparator.SPACE;
        }
        ArrayDataTable.parseHeader( header, tableInfo, samples,
                                    /(?i)^GENE\w*$/,
                                    /^(\w+)$/,
                                    /^(\w+)$/,
                                    null );
        validateTableInfo( tableInfo );
    }

//-----------------------------------------------------------------------------

    private
    static
    void validateTableInfo( ArrayDataTableInfo tableInfo )
    {
        if ( tableInfo.probeIdColumn != 0 )
        {
            String msg = "Bad file format: first column is not gene ID";
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
    Closure skipRow = { String row, String[] fields ->
        return RnaSeqImporter.bogusProbes.contains( fields[ 0 ] );
    }
    
//-----------------------------------------------------------------------------
}                                                                 //RnaSeqTable


//*****************************************************************************


class RnaSeqTableTmmNormalizedBulkLoadBuilder
extends FileBuilderFromTextTable
{                                     //RnaSeqTableTmmNormalizedBulkLoadBuilder
//-----------------------------------------------------------------------------

    RnaSeqTableTmmNormalizedBulkLoadBuilder( String bulkLoadSpec,
                                             SampleSet sampleSet,
                                             ArrayDataTableInfo tableInfo,
                                             List< ArrayDataSample > samples )
    {
        super( bulkLoadSpec );
        m_sampleSet = sampleSet;
        m_tableInfo = tableInfo;
        m_samples = samples;
    }

//=============================================================================

    void processRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            return;
        }
        ArrayDataTableRowData rowData =
                ArrayDataTable.parseRow( fields, m_tableInfo );
        for ( int i = 0; i < m_samples.size(); ++i )
        {
            ArrayDataSample sample = m_samples[ i ];
            ArrayDataDatum datum = rowData.sampleData[ i ];
            RnaSeqImporter.writeTmmNormalizedBulkLoadLine( m_writer,
                                                           m_sampleSet,
                                                           rowData.probeId,
                                                           sample, datum );
        }
    }

//=============================================================================

    protected SampleSet m_sampleSet;
    protected ArrayDataTableInfo m_tableInfo;
    protected List< ArrayDataSample > m_samples;

//-----------------------------------------------------------------------------
}                                     //RnaSeqTableTmmNormalizedBulkLoadBuilder


//*****************************************************************************
