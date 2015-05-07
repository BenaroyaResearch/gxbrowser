/*
  StdRankListFile.groovy

  Functions for importing BRI standard rank-list files.
*/

package org.sagres.importer;

import org.sagres.rankList.RankListType;


//*****************************************************************************


class StdRankListFile
{                                                             //StdRankListFile
//-----------------------------------------------------------------------------

    static
    void getRankListInfoFromFileName( String fileName,
                                      RankListInfo rankListInfo )
    {
        //Uses BRI naming convention for rank-list files
        String[] parts = fileName.split( "_" );
        if ( parts.size() < 1 )
        {
            String msg = "Empty file name: " + fileName;
            throw new ImportException( msg );
        }
        if ( parts[ 0 ].startsWith( "ds" ) )
        {
            String ds = parts[ 0 ].substring( 2 );
            rankListInfo.sampleSetId = Importer.parseInteger( ds );
            parts = parts[ 1..-1 ];
        }
        if ( parts.size() < 1 )
        {
            String msg = "Invalid rank-list file name: " + fileName;
            throw new ImportException( msg );
        }
        String rankListAbbrev;
        if ( parts.size() == 1 )
        {
            rankListAbbrev = "";
            rankListInfo.description = parts[ 0 ];
        }
        else
        {
            rankListAbbrev = parts[ 0 ];
            rankListInfo.description = parts[ 1..-1 ].join( " " );
        }
        rankListInfo.rankListType = RankListType.findByAbbrev( rankListAbbrev );
        if ( rankListInfo.rankListType == null )
        {
            String msg = "No rank list type for " + rankListAbbrev;
            throw new ImportException( msg );
        }
    }

//=============================================================================

    static
    Closure getTableInfo = {
        String fileSpec,
        RankListInfo rankListInfo,
        RankListTableInfo tableInfo ->

            tableInfo.separator = TextTableSeparator.CSV;
            tableInfo.firstRow = 1;

            List< Map > fields =
                    [ [ rltField: "numFileColumns",
                        tiField: "numColumns",
                        required: true ],
                      [ rltField: "probeIdFileColumn",
                        tiField: "probeIdColumn",
                        required: true ],
                      [ rltField: "rankFileColumn",
                        tiField: "rankColumn",
                        required: true ],
                      [ rltField: "valueFileColumn",
                        tiField: "valueColumn",
                        required: false ],
                      [ rltField: "pvalFileColumn",
                        tiField: "pValColumn",
                        required: false ]
                    ];
            fields.each { f ->
                if ( rankListInfo.rankListType[ f.rltField ] >= 0 )
                {
                    tableInfo[ f.tiField ] =
                            rankListInfo.rankListType[ f.rltField ];
                }
                else if ( f.required )
                {
                    String msg = "Invalid " + f.rltField +
                            " for rank list type " +
                            rankListInfo.rankListType.abbrev;
                throw new ImportException( msg );
                }
            }

            tableInfo.numRows = 0;
            new File( fileSpec ).eachLine { line, lineNum ->
                if ( (lineNum > 1) && (line.size() > 0) )
                {
                    ++tableInfo.numRows;
                }
            }
    }

//-----------------------------------------------------------------------------
}                                                             //StdRankListFile


//*****************************************************************************
