/*
  AnnotationTable.groovy

  Chip (platform) annotation table in tabular form
*/

package org.sagres.importer;

import common.chipInfo.ChipType;


//*****************************************************************************


class AnnotationTableInfo
extends TextTableInfo
{                                                         //AnnotationTableInfo
//-----------------------------------------------------------------------------

    List< AnnotationColumnInfo > columns = [];
    int probeIdColumnIndex;

//-----------------------------------------------------------------------------
}                                                         //AnnotationTableInfo


//*****************************************************************************


class AnnotationTable
{                                                             //AnnotationTable
//-----------------------------------------------------------------------------

    private
    static
    void getHeaderInfo( String line, AnnotationTableInfo tableInfo,
                        ChipType chipType )
    {
        String[] fields = TextTable.splitRow( line, tableInfo.separator );
        tableInfo.numColumns = fields.size();
        tableInfo.probeIdColumnIndex = -1;
        fields.eachWithIndex { field, i ->
            AnnotationColumnInfo col = new AnnotationColumnInfo();
            tableInfo.columns.add( col );
            col.name = field.toLowerCase();
            col.name = col.name.replaceAll( /[ ()]/, "_" );
            if ( col.name == 'id' )
                col.name = 'probe_id';
            if ( col.name == chipType.probeListColumn )
            {
                if ( tableInfo.probeIdColumnIndex >= 0 )
                {
                    String msg = "Probe ID column (" +
                            chipType.probeListColumn +
                            ") repeated at columns " +
                            tableInfo.probeIdColumnIndex + " and " + i;
                    throw new ImportException( msg );
                }
                tableInfo.probeIdColumnIndex = i;
            }
            col.maxLength = 0;
        }
        if ( tableInfo.probeIdColumnIndex < 0 )
        {
            String msg = "Probe ID column (" + chipType.probeListColumn +
                    ") not found";
            throw new ImportException( msg );
        }
    }

//-----------------------------------------------------------------------------

    static
    void getDataRowInfo( String line, int lineNum,
                         AnnotationTableInfo tableInfo )
    {
        String[] fields = TextTable.splitRow( line, tableInfo.separator );
        if ( fields.size() != tableInfo.numColumns )
        {
            String msg = "Line " + lineNum + " has " + fields.size() +
                    " fields. Expecting " + tableInfo.numColumns;
            throw new ImportException( msg );
        }
        fields.eachWithIndex { field, i ->
            if ( field.size() > tableInfo.columns[ i ].maxLength )
            {
                tableInfo.columns[ i ].maxLength = field.size();
            }
        }
    }

//-----------------------------------------------------------------------------
}                                                             //AnnotationTable


//*****************************************************************************


class AnnotationTableBulkLoadBuilder
extends FileBuilderFromTextTable
{                                              //AnnotationTableBulkLoadBuilder
//-----------------------------------------------------------------------------

    AnnotationTableBulkLoadBuilder( String bulkLoadSpec,
                                    AnnotationTableInfo tableInfo )
    {
        super( bulkLoadSpec );
        m_tableInfo = tableInfo;
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            return;
        }
        fields[ m_tableInfo.probeIdColumnIndex ] = Importer.abbreviateProbeId(
            fields[ m_tableInfo.probeIdColumnIndex ] );
        m_writer.write( fields.join( "\t" ) + "\n" );
    }

//=============================================================================

    protected AnnotationTableInfo m_tableInfo;

//-----------------------------------------------------------------------------
}                                              //AnnotationTableBulkLoadBuilder


//*****************************************************************************
