/*
  RankListImporter.groovy

  Routines for importing rank lists
*/

package org.sagres.importer;

import groovy.sql.Sql;
import org.sagres.rankList.RankList;
import org.sagres.rankList.RankListType;
import org.sagres.sampleSet.SampleSet;
import org.sagres.sampleSet.DatasetGroupSet;
import org.sagres.FilesLoaded;


//*****************************************************************************


class RankListInfo
{                                                                //RankListInfo
//-----------------------------------------------------------------------------

    Long sampleSetId;
    Long groupSetId;
    RankListType rankListType;
    String description;

//-----------------------------------------------------------------------------
}                                                                //RankListInfo


//*****************************************************************************


class RankListDatum
{                                                               //RankListDatum
//-----------------------------------------------------------------------------

    String probeId;
    Integer rank;
    Double value;
    Double pVal;

//-----------------------------------------------------------------------------
}                                                               //RankListDatum


//*****************************************************************************


class RankListImporter
{                                                            //RankListImporter
//-----------------------------------------------------------------------------

    static
    RankList createRankList( FilesLoaded fileLoaded,
                             RankListInfo rankListInfo,
                             Integer numProbes = null )
    {
        RankList rankList =
                new RankList( fileLoaded: fileLoaded,
                              sampleSetId: rankListInfo.sampleSetId,
                              groupSetId: rankListInfo.groupSetId,
                              rankListType: rankListInfo.rankListType,
                              description: rankListInfo.description,
                              numProbes: numProbes );
        if ( rankList.save( flush: true ) == null )
        {
            List< String > errors = [ "Error saving RankList record:" ];
            rankList.errors.each { err ->
                errors.add( err );
            }
            throw new ImportException( errors );
        }
        return rankList;
    }

//=============================================================================

    static
    void writeBulkLoadLine( PrintWriter writer, RankList rankList,
                            RankListDatum datum )
    {
        Importer.writeBulkLoadLine( writer, [ rankList.id, datum.probeId,
                                              datum.rank, datum.value,
                                              datum.pVal ] );
    }

//-----------------------------------------------------------------------------

    static
    void loadDataToDb( String bulkLoadSpec, Sql sql )
    {
        Importer.loadDataToDb( bulkLoadSpec, "rank_list_detail",
                               [ "rank_list_id", "probe_id",
                                 "rank", "value", "fdr_p_value" ],
                               sql );
    }

//-----------------------------------------------------------------------------
}                                                            //RankListImporter


//*****************************************************************************
