/*
  RnaSeqImporter.groovy

  Routines for importing RNA Seq data.
*/

package org.sagres.importer

import groovy.sql.Sql;
import java.sql.SQLException;
import org.sagres.sampleSet.SampleSet;


//*****************************************************************************


class RnaSeqSample
{                                                                //RnaSeqSample
//-----------------------------------------------------------------------------

    Integer id;
    String label; //a.k.a. barcode

//-----------------------------------------------------------------------------
}                                                                //RnaSeqSample


//*****************************************************************************


class RnaSeqDatum
{                                                                 //RnaSeqDatum
//-----------------------------------------------------------------------------

    Double count;

//-----------------------------------------------------------------------------
}                                                                 //RnaSeqDatum


//*****************************************************************************


class RnaSeqImporter
{                                                              //RnaSeqImporter
//-----------------------------------------------------------------------------

    static
    Closure isProbeIdListed = {
        String probeId,
        Map< String, Boolean > chipProbes ->

            if ( bogusProbes.contains( probeId ) )
                return true;
            return chipProbes.containsKey( probeId );
    }

//=============================================================================

    static
    void writeRawCountBulkLoadLine( PrintWriter writer, String probeId,
                                    ArrayDataSample sample, ArrayDataDatum datum )
    {
        Importer.writeBulkLoadLine( writer, [ sample.id, probeId,
                                              datum.signal, null ] );
    }

//-----------------------------------------------------------------------------

    static
    void loadRawCountDataToDb( String bulkLoadSpec, Sql sql )
    {
        Importer.loadDataToDb( bulkLoadSpec, "array_data_detail",
                               [ "array_data_id", "affy_id",
                                 "signal", "detection" ],
                               sql );
    }

//=============================================================================

    static
    void writeTmmNormalizedBulkLoadLine( PrintWriter writer,
                                         SampleSet sampleSet,
                                         String probeId,
                                         ArrayDataSample sample,
                                         ArrayDataDatum datum )
    {
        Importer.writeBulkLoadLine( writer, [ sampleSet.id, sample.id,
                                              probeId, datum.signal ] );
    }

//-----------------------------------------------------------------------------

    static
    void loadTmmNormalizedDataToDb( String bulkLoadSpec, Sql sql )
    {
        Importer.loadDataToDb( bulkLoadSpec, "array_data_detail_tmm_normalized",
                               [ "sample_set_id", "array_data_id",
                                 "probe_id", "signal" ],
                               sql );
    }

//=============================================================================

    static List< String > bogusProbes =
            [ "no_feature", "ambiguous", "too_low_aQual",
              "not_aligned", "alignment_not_unique" ];

//-----------------------------------------------------------------------------
}                                                              //RnaSeqImporter


//*****************************************************************************
