/*
  IlluminaChipAnnotationFile.groovy

  Functions for importing an Illumina microarray chip annotation file
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class IlluminaChipAnnotationFile
{                                                  //IlluminaChipAnnotationFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        AnnotationTableInfo tableInfo,
        ChipType chipType ->
            tableInfo.separator = TextTableSeparator.TAB;
            int state = 0; //0=pre-data, 1=header, 2=data, 3=post-data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "[Probes]" ) )
                    {
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    AnnotationTable.getHeaderInfo( line, tableInfo, chipType );
                    tableInfo.firstRow = lineNum;
                    tableInfo.numRows = 0;
                    state = 2;
                }
                else if ( state == 2 )
                {
                    if ( line =~ /^\[.+\]/ )
                    {
                        state = 3;
                    }
                    else
                    {
                        ++tableInfo.numRows;
                        AnnotationTable.getDataRowInfo( line, lineNum,
                                                        tableInfo );
                    }
                }
            }
    }

//-----------------------------------------------------------------------------
}                                                  //IlluminaChipAnnotationFile


//*****************************************************************************
