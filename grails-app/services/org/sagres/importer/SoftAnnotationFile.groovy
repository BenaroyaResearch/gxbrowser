/*
  SoftAnnotationFile.groovy

  Routines for importing Soft GEO platform (GPL) annotation files.
*/

package org.sagres.importer;

import common.chipInfo.ChipType;
import common.chipInfo.GplChipType;


//*****************************************************************************


class SoftAnnotationFile
{                                                          //SoftAnnotationFile
//-----------------------------------------------------------------------------

    static
    ChipType getChipType( String fileSpec )
    {
        //First, look in file contents.
        Map chipInfo = SoftFile.getChipTypes( "!Annotation_platform",
                                              "^Annotation", fileSpec );
        if ( chipInfo.chipTypes.size() == 1 )
        {
            return chipInfo.chipTypes[ 0 ];
        }
        //Second, try based on file name.
        FileSpecParts specParts = Importer.parseFileSpec( fileSpec );
        String[] nameParts = specParts.name.split( '-' );
        String gplId = nameParts[ 0 ];
        GplChipType gplChipType = GplChipType.findByGplId( gplId );
        if ( gplChipType == null )
        {
            throw new ImportException( "Unlisted platform: " + gplId );
        }
        return gplChipType.chipType;
    }

//-----------------------------------------------------------------------------

    static
    Closure getTableInfo = {
        String fileSpec,
        AnnotationTableInfo tableInfo,
        ChipType chipType ->
            tableInfo.separator = TextTableSeparator.TAB;
            int state = 0; //0=pre-header, 1=header, 2=data, 3=post-data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line == "!platform_table_begin" )
                    {
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    AnnotationTable.getHeaderInfo( line, tableInfo,
                                                   chipType );
                    tableInfo.firstRow = lineNum;
                    tableInfo.numRows = 0;
                    state = 2;
                }
                else if ( state == 2 )
                {
                    if ( line == "!platform_table_end" )
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
}                                                          //SoftAnnotationFile


//*****************************************************************************
