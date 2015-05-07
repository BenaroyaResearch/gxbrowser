/*
  GeneralExpressionData.groovy

  Functions for importing gene-expression data from a file in the
  Plain Signal Data format:
  Tab-delimited text table (TSV, actually, so quoted values are allowed).
  One-line header, one line of data per probe (gene, exon, etc.).
  First column is probe ID.
  Column headings for signal values consist of the sample IDs (labels/barcodes),
  followed by a period and a string containing "signal" (case-insensitive)
  (and no periods).
  Signal values are decimal numbers; nulls are represented by empty strings.
  Columns with detection p-values are optional. The column headings consist of
  the sample ID followed by a period and a string containing "detection"
  (again case-insensitive, and again no periods in this string).
  Detection p-values are decimal numbers; nulls are represented by
  empty strings.
  Columns with "call flags" are also optional. The column headings consist of
  the sample ID followed by a period and a string containing "call" (again
  case-insensitive, and again with no periods in this string).
  Detection call flags contain the characters "P", "M", or "A", for "present",
  "marginal", or "absent".
  Any columns which do not have these suffixes are permitted but ignored.
*/

package org.sagres.importer;


//*****************************************************************************


class GeneralExpressionData
{                                                       //GeneralExpressionData
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        ArrayDataTableInfo tableInfo,
        List< ArrayDataSample > samples ->

            tableInfo.separator = TextTableSeparator.TSV;
            tableInfo.skipRow = { String row, String[] fields = null ->
                return (row.size() == 0);
            }
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( lineNum == 1 )
                {
                    processHeader( line, tableInfo, samples );
                    tableInfo.firstRow = lineNum;
                    tableInfo.numRows = 0;
                    tableInfo.numProbes = 0;
                }
                else
                {
                    ++tableInfo.numRows;
                    if ( tableInfo.skipRow( line ) == false )
                    {
                        ++tableInfo.numProbes;
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
                                    null,
                                    /^(.*)\.([^\.]+)$/,
                                    /(?i)^(.*)\.[^\.]*signal[^\.]*$/,
                                    /(?i)^(.*)\.[^\.]*detection[^\.]*$/,
                                    /(?i)^(.*)\.[^\.]*call[^\.]*$/ );
    }

//-----------------------------------------------------------------------------
}                                                       //GeneralExpressionData


//*****************************************************************************
