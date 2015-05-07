/*
  RankListTable.groovy

  Rank-list data in tabular form.
*/

package org.sagres.importer;

import org.sagres.rankList.RankList;


//*****************************************************************************


class RankListTableInfo
extends TextTableInfo
{                                                           //RankListTableInfo
//-----------------------------------------------------------------------------

    Integer probeIdColumn;
    Integer rankColumn;
    Integer valueColumn;
    Integer pValColumn;
    
//-----------------------------------------------------------------------------
}                                                           //RankListTableInfo


//*****************************************************************************


class RankListTable
{                                                               //RankListTable
//-----------------------------------------------------------------------------

    static
    RankListDatum parseRow( String[] fields, RankListTableInfo tableInfo )
    {
        RankListDatum datum = new RankListDatum();
        if ( tableInfo.probeIdColumn != null )
        {
            datum.probeId =
                Importer.abbreviateProbeId( fields[ tableInfo.probeIdColumn ] );
        }
        if ( tableInfo.rankColumn != null )
        {
            datum.rank =
                    Importer.parseInteger( fields[ tableInfo.rankColumn ] );
        }
        if ( tableInfo.valueColumn != null )
        {
            datum.value =
                    Importer.parseDouble( fields[ tableInfo.valueColumn ] );
        }
        if ( tableInfo.pValColumn != null )
        {
            datum.pVal =
                    Importer.parseDouble( fields[ tableInfo.pValColumn ] );
        }
        return datum;
    }

//-----------------------------------------------------------------------------
}                                                               //RankListTable


//*****************************************************************************


class RankListTableValidator
{                                                      //RankListTableValidator
//-----------------------------------------------------------------------------

    void init( RankListTableInfo tableInfo,
               List< String > warnings )
    {
        m_tableInfo = tableInfo;
        m_warnings = warnings;
    }

//=============================================================================

    void beforeTable( )
    {
        m_errors = [];
    }

//=============================================================================

    void validateRow( String[] fields, int lineNum )
    {
        if ( fields.size() != m_tableInfo.numColumns )
        {
            String msg = "Line " + lineNum + " has " + fields.size() +
                    " fields. Expecting " + m_tableInfo.numColumns;
            m_errors.add( msg );
            return;
        }

        RankListDatum datum = RankListTable.parseRow( fields, m_tableInfo );
        if ( datum.probeId == null )
        {
            String msg = "No probe ID on line " + lineNum;
            m_errors.add( msg );
        }
        if ( (datum.rank == null) && (m_tableInfo.rankColumn != null) )
        {
            String msg = "No rank for probe ID " + datum.probeId +
                    " on line " + lineNum;
            if ( requireRank )
            {
                m_errors.add( msg );
            }
            else
            {
                m_warnings.add( msg );
            }
        }
        if ( (datum.value == null) && (m_tableInfo.valueColumn != null) )
        {
            String msg = "No value for probe ID " + datum.probeId +
                    " on line " + lineNum;
            if ( requireValue )
            {
                m_errors.add( msg );
            }
            else
            {
                m_warnings.add( msg );
            }
        }
        if ( (datum.pVal == null) && (m_tableInfo.pValColumn != null) )
        {
            String msg = "No p-value for probe ID " + datum.probeId +
                    " on line " + lineNum;
            if ( requirePVal )
            {
                m_errors.add( msg );
            }
            else
            {
                m_warnings.add( msg );
            }
        }
    }

//=============================================================================

    void afterTable( )
    {
        if ( m_errors.size() > 0 )
        {
            throw new ImportException( m_errors );
        }
    }

//=============================================================================

    protected boolean requireRank = true;
    protected boolean requireValue = false;
    protected boolean requirePVal = false;
    RankListTableInfo m_tableInfo;
    List< String > m_errors;
    List< String > m_warnings;

//-----------------------------------------------------------------------------
}                                                      //RankListTableValidator


//*****************************************************************************


class RankListTableBulkLoadBuilder
extends FileBuilderFromTextTable
{                                                //RankListTableBulkLoadBuilder
//-----------------------------------------------------------------------------

    RankListTableBulkLoadBuilder( String bulkLoadSpec,
                                  RankListTableInfo tableInfo,
                                  RankList rankList )
    {
        super( bulkLoadSpec );
        m_tableInfo = tableInfo;
        m_rankList = rankList;
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
        RankListImporter.writeBulkLoadLine( m_writer, m_rankList, datum );
    }

//=============================================================================

    RankListTableInfo m_tableInfo;
    RankList m_rankList;

//-----------------------------------------------------------------------------
}                                                //RankListTableBulkLoadBuilder


//*****************************************************************************
