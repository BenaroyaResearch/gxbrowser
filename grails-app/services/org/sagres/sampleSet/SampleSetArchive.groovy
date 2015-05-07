/*
  SampleSetArchive.groovy

  Routines for exporting and importing the data for a sample set.
*/

package org.sagres.sampleSet;

import org.sagres.sampleSet.SampleSetService;
import org.sagres.mat.MatConfigService;
import org.sagres.util.FileSys;
import org.sagres.util.OS;
import groovy.sql.Sql;
import groovy.util.ConfigObject;
import groovy.sql.GroovyRowResult;
import org.sagres.sampleSet.MongoDataService;
import com.mongodb.util.JSON;


//*****************************************************************************


class SampleSetArchive
{                                                            //SampleSetArchive
//-----------------------------------------------------------------------------

    SampleSetArchive( SampleSetService sampleSetService,
                      Sql sql,
                      MongoDataService mongoDataService,
                      ConfigObject appConfig,
                      MatConfigService matConfigService )
    {
        m_sampleSetService = sampleSetService;
        m_sql = sql;
        m_mongoDataService = mongoDataService;
        m_appConfig = appConfig;
        m_matConfigService = matConfigService;

        m_mongoConf =
                [
                    host: m_appConfig.mongodb.host,
                    port: m_appConfig.mongodb.port,
                    db: m_appConfig.mongodb.databasename,
                ];
        if ( m_appConfig.mongodb.username )
            m_mongoConf.username = m_appConfig.mongodb.username;
        if ( m_appConfig.mongodb.password )
            m_mongoConf.password = m_appConfig.mongodb.password;
    }

//=============================================================================

    boolean exportToFiles( SampleSet sampleSet, String directory,
                           boolean exportExpressionData = true )
    {
        if ( sampleSet == null )
        {
            println( "No such sample set" );
            return false;
        }
        
        println( "Exporting sample set " + sampleSet.id + " to " + directory );

        if ( directory[ -1 ] != '/' )
            directory += '/';
        FileSys.makeDirIfNeeded( directory );
        new File( directory ).setWritable( true, false ); //writable for all

        //SQL data

        List< Map > idQueryMaps = [ [ "id": sampleSet.id ] ];
        String tmpSSetIdsTable =
                makeTempIdsTable( m_sql, "sample_set", idQueryMaps );
        exportRecords( m_sql, directory, "sample_set", tmpSSetIdsTable, "id" );

        for ( String tableName : sqlSampleSetTables )
        {
            exportRecords( m_sql, directory, tableName, tmpSSetIdsTable,
                           "sample_set_id" );
        }

        String tmpPalxIdsTable =
                makeTempIdsTable( m_sql, "sample_set_palx",
                                  tmpSSetIdsTable, "sample_set_id" );
        exportRecords( m_sql, directory, "sample_set_palx_detail",
                       tmpPalxIdsTable, "sample_set_palx_id" );
        dropTable( m_sql, tmpPalxIdsTable );

        String tmpGrpSetIdsTable =
                makeTempIdsTable( m_sql, "dataset_group_set",
                                  tmpSSetIdsTable, "sample_set_id" );
        exportRecords( m_sql, directory, "dataset_group",
                       tmpGrpSetIdsTable, "group_set_id" );

        String tmpGrpIdsTable =
                makeTempIdsTable( m_sql, "dataset_group",
                                  tmpGrpSetIdsTable, "group_set_id" );
        exportRecords( m_sql, directory, "dataset_group_detail",
                       tmpGrpIdsTable, "group_id" );
        dropTable( m_sql, tmpGrpIdsTable );
        dropTable( m_sql, tmpGrpSetIdsTable );

        String tmpRankListIdsTable =
                makeTempIdsTable( m_sql, "rank_list",
                                  tmpSSetIdsTable, "sample_set_id" );
        exportRecords( m_sql, directory, "rank_list_detail",
                       tmpRankListIdsTable, "rank_list_id" );
        dropTable( m_sql, tmpRankListIdsTable );

        String tmpAnalIdsTable =
                makeTempIdsTable( m_sql, "analysis",
                                  tmpSSetIdsTable, "sample_set_id" );
        exportRecords( m_sql, directory, "analysis_summary",
                       tmpAnalIdsTable, "analysis_id" );
        exportRecords( m_sql, directory, "module_result",
                       tmpAnalIdsTable, "analysis_id" );
        dropTable( m_sql, tmpAnalIdsTable );

        String tmpChpsIdsTable =
                makeTempIdsTable( m_sql, "chips_loaded",
                                  tmpSSetIdsTable, "sample_set_id" );

        if ( sampleSet.parentSampleSet == null )
        {
            exportRecords( m_sql, directory, "chips_loaded", 
                           tmpSSetIdsTable, "sample_set_id" );

            exportRecords( m_sql, directory, "fluidigm_chip_loaded",
                           tmpChpsIdsTable, "chips_loaded_id" );
            exportRecords( m_sql, directory, "array_data",
                           tmpChpsIdsTable, "chip_id" );
        }

        if ( exportExpressionData )
        {
            String tmpArrIdsTable =
                    makeTempIdsTable( m_sql, "array_data",
                                      tmpChpsIdsTable, "chip_id" );
            exportRecords( m_sql, directory, "array_data_detail",
                           tmpArrIdsTable, "array_data_id" );
            exportRecords( m_sql, directory,
                           "array_data_detail_quantile_normalized",
                           tmpArrIdsTable, "array_data_id" );
            exportRecords( m_sql, directory,
                           "array_data_detail_tmm_normalized",
                           tmpArrIdsTable, "array_data_id" );
            exportRecords( m_sql, directory,
                           "focused_array_sample_assay_data",
                           tmpArrIdsTable, "array_data_id" );
            exportRecords( m_sql, directory, "focused_array_fold_changes",
                           tmpArrIdsTable, "array_data_id" );
            dropTable( m_sql, tmpArrIdsTable );
        }
        dropTable( m_sql, tmpChpsIdsTable );

        dropTable( m_sql, tmpSSetIdsTable );

        List< Map > ssQueryMaps = [ [ "sample_set_id": sampleSet.id ] ];
        List analysisIds = getTableIds( m_sql, "analysis", ssQueryMaps );

        //Mongo data

        ssQueryMaps = [ [ "sampleSetId": sampleSet.id ] ];
        for( String collName : mongoSampleSetCollections )
        {
//            exportDocuments( m_mongoConf, directory, collName, ssQueryMaps );
            exportDocuments( m_mongoDataService, directory, collName,
                             ssQueryMaps );
        }

        if ( analysisIds.size() > 0 )
        {
            List< Map > analysisQueryMaps = [];
            analysisIds.each { id ->
                analysisQueryMaps.add( [ "analysisId": id ] ) }
            for( String collName : mongoAnalysisCollections )
            {
//                exportDocuments( m_mongoConf, directory, collName,
//                                 analysisQueryMaps );
                exportDocuments( m_mongoDataService, directory, collName,
                                 analysisQueryMaps );
            }
        }

        //IDs

        Map idMap =
                [
                    sampleSetId: sampleSet.id,
                    analysisIds: analysisIds
                ];
        String idsJson = JSON.serialize( idMap );
        String idFileSpec = directory + "ids.json";
        new File( idFileSpec ).write( idsJson );
        
        //Files

        String fileOutputSource = m_appConfig.fileOutput.baseDir +
                "expressionTsv/" + sampleSet.id;
        if ( new File( fileOutputSource ).exists() )
        {
            String fileOutputDest = directory + "fileOutput";
            FileSys.linkFile( fileOutputSource, fileOutputDest );
        }
        
        String fileUploadsSource = m_appConfig.fileUpload.baseDir +
                "ds/" + sampleSet.id;
        if ( new File( fileUploadsSource ).exists() )
        {
            String fileUploadsDest = directory + "fileUploads";
            FileSys.linkFile( fileUploadsSource, fileUploadsDest );
        }

        String matWorkDir = m_matConfigService.getMATWorkDirectory();
        if ( matWorkDir[-1] != '/' )
            matWorkDir += '/';
        analysisIds.each { analysisId ->
            String matFileSource = matWorkDir + analysisId;
            if ( new File( matFileSource ).exists() )
            {
                String matFileDest = directory + "analysis/" +
                        analysisId + "/" + "matFiles";
                FileSys.linkFile( matFileSource, matFileDest );
            }
        }
        
        println( "Done exporting sample set " + sampleSet.id + " to " + directory );

        return true;
    }

//-----------------------------------------------------------------------------

    private
    void exportRecords( Sql sql, String directory, String tableName,
                        String tmpIdsTableName, String joinColumn )
    {
        String fileSpec = directory + "Sql_" + tableName + ".tsv";
        exportMySqlRecords( sql, tableName, fileSpec,
                            tmpIdsTableName, joinColumn );
    }

//-----------------------------------------------------------------------------

    private
    void exportDocuments( Map mongoConfig, String directory,
                          String tableName, List< Map > queryMaps )
    {
        String fileSpec = directory + "Mongo_" + tableName + ".bson";
        exportMongoDocuments( mongoConfig, tableName, fileSpec, queryMaps );
    }

//.............................................................................

    private
    void exportDocuments( MongoDataService mongoDataService, String directory,
                          String tableName, List< Map > queryMaps )
    {
        String fileSpec = directory + "Mongo_" + tableName + ".json";
        exportMongoDocuments( mongoDataService, tableName, fileSpec,
                              queryMaps );
    }

//=============================================================================

    boolean importFromFiles( String directory,
                             boolean importExpressionData = true )
    {
        //IDs

        String idFileSpec = directory + "ids.json";
        String idsJson = new File( idFileSpec ).getText( "UTF-8" );
        Map idMap = JSON.parse( idsJson );

        println( "Importing sample set " + idMap.sampleSetId +
                 " from " + directory );

        //SQL data
        List< String > tables =
                [
                    "sample_set",
                    "sample_set_admin_info",
                    "sample_set_annotation",
                    "sample_set_file",
                    "sample_set_link_detail",
                    "sample_set_overview_component",
                    "sample_set_palx",
                    "sample_set_platform_info",
                    "sample_set_sample_info",
                    "dataset_group_set",
                    "labkey_report",
                    "rank_list",
                    "rank_list_params",
                    "analysis",
                    "sample_set_palx_detail",
                    "dataset_group",
                    "dataset_group_detail",
                    "rank_list_detail",
                    "analysis_summary",
                    "module_result",
                    "chips_loaded",
					"fluidigm_chip_loaded",
                    "array_data"
                ];
        if ( importExpressionData )
        {
            tables.addAll(
                [
                    "array_data_detail",
                    "array_data_detail_quantile_normalized",
                    "array_data_detail_tmm_normalized",
					"focused_array_sample_assay_data",
					"focused_array_fold_changes"
                ]
            );
        }
        for ( String tableName : tables )
        {
            importRecords( m_sql, directory, tableName );
        }

        //Mongo data

        List< String > collections =
                [
                    "sample",
                    "sampleSet",
                    "sampleSetTabs",
                    "labkeyClinical",
                    "labkeyLabResults",
                    "labkeySubject",
                    "analysis_results",
                    "correlations",
                    "matfiles"
                ];
        for ( String collName : collections )
        {
//            importDocuments( m_mongoConf, directory, collName );
            importDocuments( m_mongoDataService, directory, collName );
        }

        //Files

        String fileOutputSourceDir = directory + "fileOutput";
        if ( new File( fileOutputSourceDir ).exists() )
        {
            String fileOutputDestDir = m_appConfig.fileOutput.baseDir +
                    "expressionTsv/" + idMap.sampleSetId;
            FileSys.copyDirectory( fileOutputSourceDir, fileOutputDestDir,
                                   true );
        }
        
        String fileUploadsSourceDir = directory + "fileUploads";
        if ( new File( fileUploadsSourceDir ).exists() )
        {
            String fileUploadsDestDir = m_appConfig.fileUpload.baseDir +
                    "ds/" + idMap.sampleSetId;
            FileSys.copyDirectory( fileUploadsSourceDir, fileUploadsDestDir,
                                   true );
        }

        String matWorkDir = m_matConfigService.getMATWorkDirectory();
        if ( matWorkDir[-1] != '/' )
            matWorkDir += '/';
        idMap.analysisIds.each { analysisId ->
            String matFilesSourceDir = directory + "analysis/" +
                        analysisId + "/" + "matFiles";
            if ( new File( matFilesSourceDir ).exists() )
            {
                String matFilesDestDir = matWorkDir + analysisId;
                FileSys.copyDirectory( matFilesSourceDir, matFilesDestDir, true );
            }
        }
        
        println( "Done importing sample set " + idMap.sampleSetId + " from " + directory );
        return true;
    }

//-----------------------------------------------------------------------------

    private
    void importRecords( Sql sql, String directory, String tableName )
    {
        String fileSpec = directory + "Sql_" + tableName + ".tsv";
        if ( new File( fileSpec ).exists() == false )
            return;
        importMySqlRecords( sql, tableName, fileSpec );
    }

//-----------------------------------------------------------------------------

    private
    void importDocuments( Map mongoConfig, String directory, String collName )
    {
        String fileSpec = directory + "Mongo_" + collName + ".bson";
        if ( new File( fileSpec ).exists() == false )
            return;
        importMongoDocuments( mongoConfig, collName, fileSpec );
    }

//.............................................................................

    private
    void importDocuments( MongoDataService mongoDataService,
                          String directory, String collName )
    {
        String fileSpec = directory + "Mongo_" + collName + ".json";
        if ( new File( fileSpec ).exists() == false )
            return;
        importMongoDocuments( mongoDataService, collName, fileSpec );
    }

//=============================================================================

    boolean exportToTarball( SampleSet sampleSet, String fileSpec )
    {
        println( "Exporting sample set " + sampleSet.id + " to " + fileSpec );
        FileSys.deleteFile( fileSpec );
        String dir = getTarballDir( fileSpec );
        if ( exportToFiles( sampleSet, dir ) == false )
        {
            return false;
        }
        String command = "tar -chzvf ${fileSpec} *";
        Map results = [:];
        boolean rslt = OS.execSysCommand( command, [], null, 1, dir );
        println( "Done exporting sample set " + sampleSet.id +
                 " to " + fileSpec );
        return rslt;
    }

//-----------------------------------------------------------------------------

    boolean importFromTarball( String fileSpec )
    {
        println( "Importing sample set from " + fileSpec );
        String dir = getTarballDir( fileSpec );
        String command = "tar -xzvf ${fileSpec}";
        Map results = [:];
        boolean rslt = OS.execSysCommand( command, [], null, 1, dir );
        if ( rslt == false )
        {
            return false;
        }
        rslt = importFromFiles( dir );
        println( "Done importing sample set from " + fileSpec );
        return rslt;
    }

//=============================================================================

    boolean deleteAllData( SampleSet sampleSet )
    {
        List< Map > idQueryMaps = [ [ "id": sampleSet.id ] ];
        String tmpSSetIdsTable =
                makeTempIdsTable( m_sql, "sample_set", idQueryMaps );
        String tmpPalxIdsTable =
                makeTempIdsTable( m_sql, "sample_set_palx",
                                  tmpSSetIdsTable, "sample_set_id" );
        String tmpGrpSetIdsTable =
                makeTempIdsTable( m_sql, "dataset_group_set",
                                  tmpSSetIdsTable, "sample_set_id" );
        String tmpGrpIdsTable =
                makeTempIdsTable( m_sql, "dataset_group",
                                  tmpGrpSetIdsTable, "group_set_id" );
        String tmpRankListIdsTable =
                makeTempIdsTable( m_sql, "rank_list",
                                  tmpSSetIdsTable, "sample_set_id" );
        String tmpAnalIdsTable =
                makeTempIdsTable( m_sql, "analysis",
                                  tmpSSetIdsTable, "sample_set_id" );
        String tmpChpsIdsTable =
                makeTempIdsTable( m_sql, "chips_loaded",
                                  tmpSSetIdsTable, "sample_set_id" );
        List< Map > ssQueryMaps = [ [ "sample_set_id": sampleSet.id ] ];
        List analysisIds = getTableIds( m_sql, "analysis", ssQueryMaps );

        //SQL data

        deleteMySqlRecords( m_sql, "sample_set", tmpSSetIdsTable, "id" );
        for ( String tableName : sqlSampleSetTables )
        {
            deleteMySqlRecords( m_sql, tableName, tmpSSetIdsTable,
                           "sample_set_id" );
        }
        deleteMySqlRecords( m_sql, "sample_set_palx_detail",
                       tmpPalxIdsTable, "sample_set_palx_id" );
        deleteMySqlRecords( m_sql, "dataset_group",
                       tmpGrpSetIdsTable, "group_set_id" );
        deleteMySqlRecords( m_sql, "dataset_group_detail",
                       tmpGrpIdsTable, "group_id" );
        deleteMySqlRecords( m_sql, "rank_list_detail",
                       tmpRankListIdsTable, "rank_list_id" );
        deleteMySqlRecords( m_sql, "analysis_summary",
                       tmpAnalIdsTable, "analysis_id" );
        deleteMySqlRecords( m_sql, "module_result",
                       tmpAnalIdsTable, "analysis_id" );

        if ( sampleSet.parentSampleSet == null )
        {
            String tmpArrIdsTable =
                    makeTempIdsTable( m_sql, "array_data",
                                  tmpChpsIdsTable, "chip_id" );

            deleteMySqlRecords( m_sql, "chips_loaded", 
                           tmpSSetIdsTable, "sample_set_id" );

            deleteMySqlRecords( m_sql, "fluidigm_chip_loaded",
                           tmpChpsIdsTable, "chips_loaded_id" );
            deleteMySqlRecords( m_sql, "array_data",
                           tmpChpsIdsTable, "chip_id" );
            deleteMySqlRecords( m_sql, "array_data_detail",
                           tmpArrIdsTable, "array_data_id" );
            deleteMySqlRecords( m_sql,
                           "array_data_detail_quantile_normalized",
//                           tmpArrIdsTable, "array_data_id" );
						   tmpSSetIdsTable, "sample_set_id" );
            deleteMySqlRecords( m_sql,
                           "array_data_detail_tmm_normalized",
//                           tmpArrIdsTable, "array_data_id" );
						   tmpSSetIdsTable, "sample_set_id" );
            deleteMySqlRecords( m_sql,
                           "focused_array_sample_assay_data",
                           tmpArrIdsTable, "array_data_id" );
            deleteMySqlRecords( m_sql, "focused_array_fold_changes",
                           tmpArrIdsTable, "array_data_id" );

            dropTable( m_sql, tmpArrIdsTable );
        }

		// TODO: put all of the above in a try, catch errors, then finally drop the tables.
        dropTable( m_sql, tmpChpsIdsTable );
        dropTable( m_sql, tmpSSetIdsTable );
        dropTable( m_sql, tmpAnalIdsTable );
        dropTable( m_sql, tmpRankListIdsTable );
        dropTable( m_sql, tmpGrpIdsTable );
        dropTable( m_sql, tmpGrpSetIdsTable );
        dropTable( m_sql, tmpPalxIdsTable );

        //Mongo data

        ssQueryMaps = [ [ "sampleSetId": sampleSet.id ] ];
        for( String collName : mongoSampleSetCollections )
        {
            deleteMongoDocuments( m_mongoDataService, collName, ssQueryMaps );
        }

        if ( analysisIds.size() > 0 )
        {
            List< Map > analysisQueryMaps = [];
            analysisIds.each { id ->
                analysisQueryMaps.add( [ "analysisId": id ] ) }
            for( String collName : mongoAnalysisCollections )
            {
                deleteMongoDocuments( m_mongoDataService, collName,
                                      analysisQueryMaps );
            }
        }

        //Files

        String fileOutputDir = m_appConfig.fileOutput.baseDir +
                "expressionTsv/" + sampleSet.id;
        if ( new File( fileOutputDir ).exists() )
        {
            FileSys.deleteDirectory( fileOutputDir );
        }

        String fileUploadsDir = m_appConfig.fileUpload.baseDir +
                "ds/" + sampleSet.id;
        if ( new File( fileUploadsDir ).exists() )
        {
            FileSys.deleteDirectory( fileUploadsDir );
        }

        String matWorkDir = m_matConfigService.getMATWorkDirectory();
        if ( matWorkDir[-1] != '/' )
            matWorkDir += '/';
        analysisIds.each { analysisId ->
            String matFileDir = matWorkDir + analysisId;
            if ( new File( matFileDir ).exists() )
            {
                FileSys.deleteDirectory( matFileDir );
            }
        }

        println( "Done deleting sample set " + sampleSet.id );
        return true;
    }

//=============================================================================
    
    void exportMySqlRecords( Sql sql, String tableName, String fileSpec,
                             String tmpIdsTableName, String joinColumn )
    {
        List< String > columns = getColumnNames( sql, tableName );
        FileSys.deleteFile( fileSpec );
        String query =
                "SELECT " + "\n" +
                "    " + columns.collect( { "'" + it + "'" } ).join( ", " ) + "\n" +
                "  UNION ALL \n" +
                "  SELECT " + "\n" +
                "    " + columns.collect( { tableName + ".`" + it + "`" } ).join( ", " ) + "\n" +
                "    FROM " + tableName + "\n" +
                "    INNER JOIN " + tmpIdsTableName + "\n" +
                "      ON " + tableName + ".`" + joinColumn +
                "`=" + tmpIdsTableName + ".`id`" + "\n" +
                "  INTO OUTFILE '" + fileSpec + "'";
        sql.execute( query );
    }

//.............................................................................

    String makeTempIdsTable( Sql sql, String tableName, List< Map > queryMaps )
    {
        String tmpTableName = "tmp_" + tableName;
        String command =
                "CREATE TEMPORARY TABLE " + tmpTableName + "\n" +
                "  (id INT PRIMARY KEY) ENGINE=MEMORY";
        sql.execute( command );
        for ( Map queryMap : queryMaps )
        {
            List< String > conditions = [];
            queryMap.each { key, val ->
                String condition = "`" + key + "` = ";
                if ( val instanceof String )
                    condition += "'" + val + "'";
                else
                    condition += val.toString();
                conditions.add( condition );
            }
            command =
                    "INSERT INTO " + tmpTableName + "\n" +
                    "  (`id`)\n" +
                    "  SELECT `id` FROM " + tableName + "\n" +
                    "    WHERE " + conditions.join( " AND " );
            sql.execute( command );
        }

        return tmpTableName;
    }

//.............................................................................

    String makeTempIdsTable( Sql sql, String tableName,
                             String tmpIdsTableName, String joinColumn )
    {
        String tmpTableName = "tmp_" + tableName;
        String command =
                "CREATE TEMPORARY TABLE " + tmpTableName + "\n" +
                "  (id INT PRIMARY KEY) ENGINE=MEMORY";
        sql.execute( command );

        command =
                "INSERT INTO " + tmpTableName + "\n" +
                "  (`id`)\n" +
                "  SELECT " + tableName + ".`id` FROM " + tableName + "\n" +
                "    INNER JOIN " + tmpIdsTableName + "\n" +
                "      ON " + tableName + ".`" + joinColumn +
                "`=" + tmpIdsTableName + ".`id`";
        sql.execute( command );

        return tmpTableName;
    }

//.............................................................................

    private
    List getTableIds( Sql sql, String tableName, List< Map > queryMaps )
    {
        List ids = [];
        for ( Map queryMap : queryMaps )
        {
            String condition = buildSqlCondition( queryMap );
            String query =
                    "SELECT `id` FROM " + tableName + "\n" +
                    "  WHERE " + condition;
            List rows = sql.rows( query );
            ids.addAll( rows*.id );
        }
        return ids;
    }

//.............................................................................

    private
    String buildSqlCondition( Map queryMap )
    {
        List< String > conditions = [];
        queryMap.each { key, val ->
            String condition = "`" + key + "` = ";
            if ( val instanceof String )
                condition += "'" + val + "'";
            else
                condition += val.toString();
            conditions.add( condition );
        }
        return conditions.join( " AND " );
    }

//-----------------------------------------------------------------------------

    private
    void importMySqlRecords( Sql sql, String tableName, String fileSpec )
    {
        BufferedReader reader = new File( fileSpec ).newReader( );
        String header = reader.readLine( );
        List< String > dataColumns = header.split( "\t" );

        List< String > tableColumns = getColumnNames( sql, tableName );
        List< String > inputColumns = dataColumns.collect {
            if ( tableColumns.contains( it ) )
            {
                return "`" + it + "`";
            }
            else
            {
                println( "Importing sample set: Column " + it + " from " + tableName + " is no longer in the database schema." );
                return "@dummy";
            }
        }

        String command =
                "LOAD DATA LOCAL INFILE '" + fileSpec + "'" + "\n" +
                "  REPLACE\n" +
                "  INTO TABLE " + tableName + "\n" +
                "  IGNORE 1 LINES\n" +
                "  ( " + inputColumns.join( ", " ) + " )";
        sql.execute( command );
    }

//-----------------------------------------------------------------------------

    void deleteMySqlRecords( Sql sql, String tableName,
                             String tmpIdsTableName, String joinColumn )
    {
        //println( "  Deleting from table " + tableName );
        String command =
                "DELETE FROM " + tableName +
                "  WHERE `" + joinColumn + "` IN" +
                "  ( SELECT `id` FROM " + tmpIdsTableName + " )";
        sql.execute( command );
    }
    
//-----------------------------------------------------------------------------

    private
    void dropTable( Sql sql, String tableName )
    {
        String command =
                "DROP TABLE IF EXISTS " + tableName;
        sql.execute( command );
    }

//-----------------------------------------------------------------------------

    private
    List< String > getColumnNames( Sql sql, String tableName )
    {
        String query =
                "SHOW COLUMNS FROM " + tableName;
        List< GroovyRowResult > records = sql.rows( query );
        return records*.Field;
    }

//=============================================================================

    private
    void exportMongoDocuments( Map config, String collection, String fileSpec,
                               List< Map > queryMaps )
    {
        String command = 'mongodump';
        List arguments = [];
        arguments.add( '-h' + config.host + ':' + config.port );
        arguments.add( '-d' + config.db );
        if ( config.user && config.password )
        {
            arguments.add( '-u' + config.username );
            arguments.add( '-p' + config.password );
        }
        arguments.add( '-c' + collection );

        List< String > queries = [];
        for ( Map queryMap : queryMaps )
        {
            List< String > conditions = [];
            queryMap.each { key, val ->
                String condition = '"' + key + '": ';
                if ( val instanceof String )
                    condition += '"' + val + '"';
                else
                    condition += val.toString();
                conditions.add( condition );
            }
            String query = '{ ' + conditions.join( ', ' ) + ' }';
            queries.add( query );
        }
        if ( queries.size() > 0 )
        {
            String query;
            if ( queries.size() == 1 )
            {
                query = queries[ 0 ];
            }
            else
            {
                query = '{ \$or: [ ' + queries.join( ', ' ) + ' ] }';
            }
            arguments.add( "-q'" + query + "'" );
        }

        arguments.add( '-o-' );

        Map results = [:];
        boolean rslt = OS.execSysCommand( command, arguments, results, 4,
                                          null, true );
        if ( rslt )
        {
            new File( fileSpec ).setBytes( results.output );
        }
    }

//.............................................................................

    private
    void exportMongoDocuments( MongoDataService mongoDataService,
                               String collection, String fileSpec,
                               List< Map > queryMaps )
    {
        File collectionFile = new File( fileSpec );
        collectionFile.withWriter { writer ->
            boolean first = true;
            writer.write( "[ " );
            for ( Map queryMap : queryMaps )
            {
                List docs = mongoDataService.find( collection, queryMap,
                                                   null, null, 0 );
                for ( Map doc : docs )
                {
                    if ( first )
                        first = false;
                    else
                        writer.write( ",\n" );
                    writer.write( JSON.serialize( doc ) );
                }
            }
            writer.write( " ]" );
        }
    }

//-----------------------------------------------------------------------------

    private
    void importMongoDocuments( Map config, String collection, String fileSpec )
    {
        String command = 'mongorestore';
        List arguments = [];
        arguments.add( '-h' + config.host + ':' + config.port );
        arguments.add( '-d' + config.db );
        if ( config.user && config.password )
        {
            arguments.add( '-u' + config.username );
            arguments.add( '-p' + config.password );
        }
        arguments.add( '-c' + collection );
        arguments.add( fileSpec );

        Map results = [:];
        boolean rslt = OS.execSysCommand( command, arguments, results, 1 );
    }

//.............................................................................

    private
    void importMongoDocuments( MongoDataService mongoDataService,
                               String collection, String fileSpec )
    {
        File collectionFile = new File( fileSpec );
        String collectionJson = collectionFile.getText( "UTF-8" );
        List< Map > docs = JSON.parse( collectionJson );
        for ( Map doc : docs )
        {
            m_mongoDataService.insert( collection, null, doc );
        }
    }

//-----------------------------------------------------------------------------

    private
    void deleteMongoDocuments( MongoDataService mongoDataService,
                                String collection,
                                List< Map > queryMaps )
    {
        for ( Map queryMap : queryMaps )
        {
            mongoDataService.remove( collection, queryMap, null );
        }
    }

//=============================================================================

    private String getTarballDir( String fileSpec )
    {
        int extPos = fileSpec.lastIndexOf( ".tgz" );
        if ( extPos < 0 )
        {
            extPos = fileSpec.lastIndexOf( ".tar.gz" );
        }
        if ( extPos < 0 )
        {
            println( "No tarball extension on " + fileSpec );
            return null;
        }
        String dirName = fileSpec.substring( 0, extPos );
        dirName += "/";
        File dir = new File( dirName );
        if ( dir.exists() == false )
        {
            if ( dir.mkdirs() == false )
            {
                println( "Unable to mkdir " + dirName );
                return null;
            }
        }
        return dirName;
    }

//=============================================================================

    private SampleSetService m_sampleSetService;
    private Sql m_sql;
    private MongoDataService m_mongoDataService;
    private ConfigObject m_appConfig;
    private Map m_mongoConf;
    private MatConfigService m_matConfigService;

    private static String[] sqlSampleSetTables =
                [
                    "sample_set_admin_info",
                    "sample_set_annotation",
                    "sample_set_file",
                    "sample_set_link_detail",
                    "sample_set_overview_component",
                    "sample_set_palx",
                    "sample_set_platform_info",
                    "sample_set_sample_info",
                    "dataset_group_set",
                    "labkey_report",
                    "rank_list",
                    "rank_list_params",
                    "analysis"
                ];
    private static String[] mongoSampleSetCollections =
            [
                "sample",
                "sampleSet",
                "sampleSetTabs",
                "labkeyClinical",
                "labkeyLabResults",
                "labkeySubject"
            ];
    private static String[] mongoAnalysisCollections =
            [
                "analysis_results",
                "correlations",
                "matfiles"
            ];

//-----------------------------------------------------------------------------
}                                                            //SampleSetArchive


//*****************************************************************************
