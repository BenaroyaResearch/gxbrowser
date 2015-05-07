/*
  FocusedArrayDesignFile.groovy

  Routines for importing focused-array layout design data.
*/

package org.sagres.importer;


//*****************************************************************************


class FocusedArrayDesignFileColumnInfo
{
    String name;
    int index;
}


//=============================================================================


class FocusedArrayDesignTableInfo
extends TextTableInfo
{
    List< FocusedArrayDesignFileColumnInfo > columns;
}


//*****************************************************************************


class FocusedArrayDesignFile
{                                                      //FocusedArrayDesignFile
//-----------------------------------------------------------------------------

//-----------------------------------------------------------------------------
}                                                      //FocusedArrayDesignFile


//*****************************************************************************


class FocusedArrayDesignBulkLoadBuilder
extends FileBuilderFromTextTable
{                                           //FocusedArrayDesignBulkLoadBuilder
//-----------------------------------------------------------------------------

    FocusedArrayDesignBulkLoadBuilder( long chipTypeId,
                                       String bulkLoadSpec,
                                       FocusedArrayDesignTableInfo tableInfo )
    {
        super( bulkLoadSpec );
        m_chipTypeId = chipTypeId.toString();
        m_tableInfo = tableInfo;
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            println( "fields.size (" + fields.size() +
                     ") != tableInfo.numColumns (" +
                     m_tableInfo.numColumns + ")" );
            return;
        }
        List< String > values = [];
        values.add( m_chipTypeId );
        for ( int i = 0; i < m_tableInfo.columns.size(); ++i )
        {
            String val = fields[ m_tableInfo.columns[ i ].index ];
            values.add( val );
        }
        m_writer.write( values.join( "\t" ) + "\n" );
    }

//=============================================================================

    protected String m_chipTypeId;
    protected FocusedArrayDesignTableInfo m_tableInfo;

//-----------------------------------------------------------------------------
}                                           //FocusedArrayDesignBulkLoadBuilder


//*****************************************************************************
