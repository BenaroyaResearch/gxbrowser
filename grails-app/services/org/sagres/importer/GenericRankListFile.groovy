/*
  GenericRankListFile.groovy

  Routines for importing rank-list files based on sorting by a value.
  NOTE: Descending sort is the default because it gives higher values lower
  rank numbers, and lower ranks signify greater importance.
*/

package org.sagres.importer;

import org.sagres.rankList.RankList;


//*****************************************************************************


class GenericRankListFile
{                                                         //GenericRankListFile
//-----------------------------------------------------------------------------

    static
    void getTableInfo( String fileSpec, RankListTableInfo tableInfo )
    {
        tableInfo.numRows = 0;
        new File( fileSpec ).eachLine { line, lineNum ->
            if ( (lineNum > tableInfo.firstRow) && (line.size() > 0) )
            { //tableInfo.firstRow is 0-based, but lineNum is 1-based
                if ( lineNum == tableInfo.firstRow + 1 )
                {
                    String[] fields =
                            TextTable.splitRow( line, tableInfo.separator );
                    tableInfo.numColumns = fields.size();
                }
                ++tableInfo.numRows;
            }
        }
        validateTableInfo( tableInfo );
    }

//-----------------------------------------------------------------------------

    private
    static
    void validateTableInfo( RankListTableInfo tableInfo )
    {
        if ( (tableInfo.probeIdColumn == null) ||
             (tableInfo.probeIdColumn < 0) ||
             (tableInfo.probeIdColumn >= tableInfo.numColumns) )
        {
            throw new ImportException( "Invalid probe ID column: " +
                                       tableInfo.probeIdColumn );
        }
        if ( (tableInfo.valueColumn == null) ||
             (tableInfo.valueColumn < 0) ||
             (tableInfo.valueColumn >= tableInfo.numColumns) )
        {
            throw new ImportException( "Invalid value column: " +
                                       tableInfo.valueColumn );
        }
    }

//-----------------------------------------------------------------------------
}                                                         //GenericRankListFile


//*****************************************************************************


class GenericRankListBulkLoadBuilder
extends FileBuilderFromTextTable
{                                              //GenericRankListBulkLoadBuilder
//-----------------------------------------------------------------------------

    GenericRankListBulkLoadBuilder( String bulkLoadSpec,
                                    RankListTableInfo tableInfo,
                                    RankList rankList,
                                    boolean descending = true )
    {
        super( bulkLoadSpec );
        m_tableInfo = tableInfo;
        m_rankList = rankList;
        m_descending = descending;
    }

//=============================================================================

    @Override
    void beforeTable( )
    {
        super.beforeTable( );
        m_data = [];
    }

//=============================================================================

    @Override
    void processRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            return;
        }
        RankListDatum datum = RankListTable.parseRow( fields, m_tableInfo );
        m_data.add( datum );
    }

//=============================================================================

    @Override
    void afterTable( )
    {
        if ( m_descending )
        {
            m_data.sort { a, b -> b.value <=> a.value }
        }
        else
        {
            m_data.sort { a, b -> a.value <=> b.value }
        }
        m_data.eachWithIndex { datum, index ->
            datum.rank = index + 1; //1-based ranks
            RankListImporter.writeBulkLoadLine( m_writer, m_rankList, datum );
        }
        m_data = null;
        super.afterTable( );
    }

//=============================================================================

    RankListTableInfo m_tableInfo;
    RankList m_rankList;
    List< RankListDatum > m_data;
    boolean m_descending;

//-----------------------------------------------------------------------------
}                                              //GenericRankListBulkLoadBuilder


//*****************************************************************************
