/*
  GenericChipAnnotationFile.groovy

  Functions for importing a typical microarray chip annotation file
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class GenericChipAnnotationFile
{                                                   //GenericChipAnnotationFile
//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        AnnotationTableInfo tableInfo,
        ChipType chipType ->
            int state = 0; //0=pre-data, 1=data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "#" ) == false )
                    {
                        AnnotationTable.getHeaderInfo( line, tableInfo,
                                                       chipType );
                        tableInfo.firstRow = lineNum;
                        tableInfo.numRows = 0;
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    ++tableInfo.numRows;
                    AnnotationTable.getDataRowInfo( line, lineNum, tableInfo );
                }
            }
    }

//-----------------------------------------------------------------------------
}                                                   //GenericChipAnnotationFile


//*****************************************************************************
