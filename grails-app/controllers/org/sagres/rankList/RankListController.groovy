package org.sagres.rankList

import org.sagres.importer.TextTableSeparator;
import grails.converters.JSON
import groovy.sql.Sql


//*****************************************************************************

class RankListController 
{
//-----------------------------------------------------------------------------

	def scaffold = true

    def dataSource; //injected
	def mongoDataService; //injected
    def importService; //injected

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

//=============================================================================

    def index = 
    {
        render( view: "list", params: params );
//		redirect(action: "list", params: params)
    }

//=============================================================================

	def list =
	{
		params.max = Math.min(params.max ? params.int('max') : 10, 100)

		def rankListInstanceList
		
		if (params.sampleSetId) {
			rankListInstanceList = RankList.findAllBySampleSetIdAndMarkedForDeleteIsNull(params.long('sampleSetId'))
		} else {
			rankListInstanceList = RankList.findAllByMarkedForDeleteIsNull()
		}
		
		def total = rankListInstanceList.size()
		//println "rl: " + rankListInstance
		//println "total: " + total 
		[rankListInstanceList: rankListInstanceList, params: params]
	}

	def delete =
	{
		def rankListInstance = RankList.get(params.id)
		if (rankListInstance) {
			try {
				// This works faster, but locks the RankListDetail table.
				//RankListDetail.executeUpdate("delete RankListDetail r where r.rankList = :rlid", [rlid: rankListInstance])
				// This works slower, but does not lock the table.
				rankListInstance.rankListDetails.clear()

				rankListInstance.fileLoaded.delete()
				rankListInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'rankList.label', default: 'RankList'), params.id])}"
				redirect(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'rankList.label', default: 'RankList'), params.id])}"
				redirect(action: "show", id: params.id)
			}
		}
		else {
		  flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'rankList.label', default: 'RankList'), params.id])}"
		  redirect(action: "list")
		}
	  }

	def markForDeletion = {
		long id = params.long("id")
	
		def deleteFlag = "TRUE".toString()
	
		def deleteState = params.int("deleteState")
		def colspan  = params.int("colspan")
	
		if (deleteState == 0) {
		  deleteFlag = "NULL".toString()
		} else {
		  deleteFlag = "1".toString()
		}
	
		//println "markForDeletion id: " + rankList.id + " deleteState: " + params.deleteState + " deleteFlag: " + deleteFlag
	
		if (RankList.exists(id))
		{
		  def returnMsg = [message:"", error:false]
	
		  try
		  {
			Sql sql = Sql.newInstance(dataSource)
			sql.execute("UPDATE rank_list SET marked_for_delete=${deleteFlag} WHERE id=${id}".toString())
			sql.close()
			if (deleteState == 1) {
			  returnMsg.message = "<td colspan=\"${colspan}\">${RankList.get(id).toString()} has been successfully marked for deletion</td><td style=\"text-align: center;\"><button class=\"ui-icon-undo-cross\" title=\"Restore\" onclick=\"javascript:markRankListForDeletion(this, ${id}, 0, ${colspan});\"/></td>"
			} else {
			  returnMsg.message = "<td colspan=\"${colspan + 1}\">${RankList.get(id).toString()} Restored</td>"
			}
		  }
		  catch (Exception e)
		  {
			println "exception: " + e.toString()
			returnMsg.message = "There was an error in marking this rankList for deletion."
			returnMsg.error = true
		  }
		  render returnMsg as JSON
		}
	  }
	
    private
    def uploadFiles( List< String > fileFieldNames,
                     String uploadPath,
                     Closure runImport,
                     String successAction,
                     String failureAction
                   )
    {
        File uploadPathFile = new File( uploadPath );
        if ( (uploadPathFile.exists() == false) &&
             (uploadPathFile.mkdirs() == false) )
        {
            flash.message = "Unable to make directory '${uploadPath}'" +
                    " for uploading";
            log.error( flash.message );
            redirect( action: "list" );
        }

        Map< String, String > uploadedFiles = [:];

        fileFieldNames.each { fileFieldName ->
            //uses Spring MultipartHttpServerRequest to get a MultipartFile
            def mpFile = request.getFile( fileFieldName );
            if ( mpFile.empty == false )
            {
                String filename = mpFile.getOriginalFilename();
                String uploadSpec = uploadPath + filename;
                File dest = new File( uploadSpec );
                mpFile.transferTo( dest );
                uploadedFiles[ fileFieldName ] = uploadSpec;
            }
        }

        if ( params.runNow )
        {
            importService.setSqlDataSource( dataSource );
            importService.setMongoDataService( mongoDataService );
            Map result = runImport( uploadedFiles, params );
            if ( result.success )
            {
                flash.message = "Import succeeded";
                log.debug( flash.message );
                redirect( action: successAction );
            }
            else
            {
                flash.message = "Import failed. " + result.message;
                log.error( flash.message );
                redirect( action: failureAction );
            }
        }
        else
        {
            //!!!Create to-be-imported entry
            flash.message = "File(s) uploaded for processing later";
            log.debug( flash.message );
            redirect( action: successAction );
        }
    }

//=============================================================================

	def showUploadStdRankListForm = 
	{
		render( view: "upload_std_rank_list" );
	}

//-----------------------------------------------------------------------------

	def uploadStdRankList =
	{
        String successAction = "list";
        String failureAction = "showUploadStdRankListForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "rankLists/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "rankListFile" ];
            if ( fileSpec )
            {
                int sampleSetId = Integer.parseInt( params.sampleSetId );
                int groupSetId = Integer.parseInt( params.groupSetId );
                return importService.importStdRankList( fileSpec,
                                                      sampleSetId, groupSetId );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
            
        return uploadFiles( [ "rankListFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//=============================================================================

	def showUploadGenericRankListForm = 
	{
		render( view: "upload_generic_rank_list" );
	}

//-----------------------------------------------------------------------------

	def uploadGenericRankList =
	{
        String successAction = "list";
        String failureAction = "showUploadGenericRankListForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "rankLists/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "rankListFile" ];
            if ( fileSpec )
            {
                int sampleSetId = Integer.parseInt( params.sampleSetId );
                int groupSetId = Integer.parseInt( params.groupSetId );
                String description = params.description;
                TextTableSeparator separator =
                        Enum.valueOf( TextTableSeparator,
                                      params.format ?: "CSV" );
                int firstRow = Integer.parseInt( params.headerLines );
                int probeIdColumn = Integer.parseInt( params.probeIdColumn );
                int valueColumn = Integer.parseInt( params.valueColumn );
                boolean descending = ( params.sortOrder != "ascending" );
                return importService.importGenericRankList(
                    fileSpec, sampleSetId, groupSetId, description,
                    separator, firstRow,
                    probeIdColumn, valueColumn, descending );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
        params.runNow = true;
        return uploadFiles( [ "rankListFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//-----------------------------------------------------------------------------
}

//*****************************************************************************
