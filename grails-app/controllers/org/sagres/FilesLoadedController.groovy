package org.sagres

import common.chipInfo.ChipData;
import common.chipInfo.ChipType;


//*****************************************************************************

class FilesLoadedController 
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
		redirect(action: "list", params: params)
    }

//=============================================================================

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

	def showUploadChipAnnotationForm = 
	{
		render( view: "upload_chip_annotation" );
	}

//-----------------------------------------------------------------------------

	def uploadChipAnnotation =
	{
        String successAction = "list";
        String failureAction = "showUploadChipAnnotationForm";
        ChipType chipType = ChipType.get( params.chipType );
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "chipAnnotations/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "annotationFile" ];
            if ( fileSpec )
            {
                return importService.importAnnotationFile( fileSpec, chipType );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
            
        return uploadFiles( [ "annotationFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//=============================================================================

	def showUploadSoftAnnotationForm = 
	{
		render( view: "upload_soft_annotation" );
	}

//-----------------------------------------------------------------------------

	def uploadSoftAnnotation =
	{
        String successAction = "list";
        String failureAction = "showUploadSoftAnnotationForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "chipAnnotations/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "annotationFile" ];
            if ( fileSpec )
            {
                return importService.importSoftAnnotationFile( fileSpec );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
            
        return uploadFiles( [ "annotationFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//=============================================================================

	def showUploadGeoAnnotationTableForm = 
	{
		render( view: "upload_geo_annotation_table" );
	}

//-----------------------------------------------------------------------------

	def uploadGeoAnnotationTable =
	{
        String successAction = "list";
        String failureAction = "showUploadGeoAnnotationTableForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "chipAnnotations/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "annotationFile" ];
            if ( fileSpec )
            {
                return importService.importGeoAnnotationTableFile( fileSpec );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
            
        return uploadFiles( [ "annotationFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//=============================================================================

    def showUploadFluidigmDesignFileForm =
    {
        render( view: "upload_fluidigm_design_file" );
    }

//-----------------------------------------------------------------------------

    def uploadFluidigmDesignFile =
    {
        String successAction = "list";
        String failureAction = "showUploadFluidigmDesignFileForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "chipAnnotations/toBeImported/";
        ChipData chipData = ChipData.get( params.chipData );
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "designFile" ];
			String housekeepingGenesFileSpec =
					uploadedFiles[ "housekeepingGenesFile" ];
			String referenceSamplesFileSpec =
					uploadedFiles[ "referenceSamplesFile" ];
            if ( fileSpec )
            {
                return importService.importFluidigmDesignFile(
                    fileSpec, housekeepingGenesFileSpec,
					referenceSamplesFileSpec, params.chipTypeName, chipData );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }

        return uploadFiles( [ "designFile", "housekeepingGenesFile",
							  "referenceSamplesFile" ],
							uploadPath,
                            runImport, successAction, failureAction );
    }

//=============================================================================

	def showUploadNcbiGeneForm = 
	{
		render( view: "upload_ncbi_gene" );
	}

//-----------------------------------------------------------------------------

	def uploadNcbiGene =
    {
        String successAction = "list";
        String failureAction = "showUploadNcbiGeneForm";
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "ncbiGenes/toBeImported/";
        Closure runImport = { Map< String, String > uploadedFiles, Map params ->
            String fileSpec = uploadedFiles[ "ncbiGeneFile" ];
            if ( fileSpec )
            {
                return importService.importNcbiGeneFile( fileSpec );
            }
            else
            {
                return [ success: false,
                         message: "File was not uploaded" ];
            }
        }
            
        return uploadFiles( [ "ncbiGeneFile" ], uploadPath,
                            runImport, successAction, failureAction );
	}

//-----------------------------------------------------------------------------
}


//*****************************************************************************
