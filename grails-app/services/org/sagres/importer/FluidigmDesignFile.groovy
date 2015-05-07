/*
  FluidigmDesignFile.groovy

  Routines for importing focused-array layout design files.
  NOTES:
  1. These routines expect tab-delimited text files. The design data will
     generally need to be exported as such from Excel spreadsheets.
*/

package org.sagres.importer;


//*****************************************************************************


class FluidigmDesignFile
{                                                          //FluidigmDesignFile
//-----------------------------------------------------------------------------

    static
    Map getChipTypeInfo( )
    {
        return [
            probeListTable: "fluidigm",
            probeListColumn: "target",
            symbolColumn: "gene_symbol",
            refSeqColumn: "design_refseq",
            synonymColumn: "gene_aliases"
        ];
    }

//-----------------------------------------------------------------------------

    static
    List getDbTableColumns( )
    {
        return [
            "target",
            "assay_id",
            "assay_name",
            "fp",
            "rp",
            "design_refseq",
            "blast_hits",
            "gene_symbol",
            "gene_aliases",
            "gene_full_name",
            "go_function",
            "go_process",
            "go_component"
        ];
    }

//=============================================================================

    //These routines are general enough that they may be applicable to
    // most focused-array design formats, but I don't know that yet.

    static
    Closure getTableInfo = {
        String fileSpec,
        FocusedArrayDesignTableInfo tableInfo,
        List< String > dbTableColumns ->
            tableInfo.separator = TextTableSeparator.TSV;
            int state = 0; //0=pre-data, 1=data
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( state == 0 )
                {
                    if ( line.startsWith( "#" ) == false )
                    {
                        getColumnInfo( line, tableInfo, dbTableColumns );
                        tableInfo.firstRow = lineNum;
                        tableInfo.numRows = 0;
                        state = 1;
                    }
                }
                else if ( state == 1 )
                {
                    ++tableInfo.numRows;
                }
            }
    }

//-----------------------------------------------------------------------------

    static
    void getColumnInfo( String line,
                        FocusedArrayDesignTableInfo tableInfo,
                        List< String > dbTableColumns )
    {
        String[] columnNames = TextTable.splitRow( line, tableInfo.separator );
        tableInfo.numColumns = columnNames.size();
        tableInfo.columns = [];
        for ( int i = 0; i < columnNames.size(); ++i )
        {
            String name = columnNames[ i ];
            name = name.toLowerCase();
            name = name.replaceAll( /[ ()]/, "_" );
            if ( dbTableColumns.contains( name ) )
            {
                FocusedArrayDesignFileColumnInfo colInfo =
                        new FocusedArrayDesignFileColumnInfo( name: name,
                                                              index: i );
                tableInfo.columns.add( colInfo );
            }
        }
    }

//-----------------------------------------------------------------------------
}                                                          //FluidigmDesignFile


//*****************************************************************************
