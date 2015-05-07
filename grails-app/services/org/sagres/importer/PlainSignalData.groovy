/*
  PlainSignalData.groovy

  Functions for importing gene-expression data from a file in the
  Plain Signal Data format:
  Tab-delimited text table (TSV, actually, so quoted values are allowed).
  One-line header, one line of data per probe (gene, exon, etc.).
  First column is probe ID.
  All subsequent columns are signal data and the headings of these columns
  are taken as-is as sample IDs (labels/barcodes).
  Signal values are decimal numbers; nulls are represented by empty strings.
  Detection p-values are all set to null.
*/

package org.sagres.importer;


//*****************************************************************************


class PlainSignalData
{                                                             //PlainSignalData
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataTableInfo tableInfo,
        List< ArrayDataSample > samples ->

            tableInfo.separator = TextTableSeparator.TSV;
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( lineNum == 1 )
                {
                    processHeader( line, tableInfo, samples );
                    tableInfo.firstRow = lineNum;
                    tableInfo.numRows = 0;
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
    void processHeader( String header,
                        ArrayDataTableInfo tableInfo,
                        List< ArrayDataSample > samples )
    {
        String[] columnNames =
                TextTable.splitRow( header, tableInfo.separator );
        tableInfo.numColumns = columnNames.size();
        if ( tableInfo.numColumns == 0 )
        { //This should be impossible at this point, but just in case...
            throw new ImportException(
                "Bad file format: No column names found in header" );
        }

        tableInfo.probeIdColumn = 0;
        tableInfo.sampleColsList = [];
        for ( int i = 1; i < tableInfo.numColumns; ++i )
        {
            String sampleLabel = columnNames[ i ];
            ArrayDataSample sample =
                    new ArrayDataSample( label: sampleLabel );
            samples.add( sample );
            ArrayDataSampleColumns sampleCols =
                    new ArrayDataSampleColumns( signalColumn: i,
                                                numColumns: 1 );
            tableInfo.sampleColsList.add( sampleCols );
        }
    }

//-----------------------------------------------------------------------------
}                                                             //PlainSignalData


//*****************************************************************************
