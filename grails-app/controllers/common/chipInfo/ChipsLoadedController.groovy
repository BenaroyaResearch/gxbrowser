package common.chipInfo

import static groovy.io.FileType.*
import org.sagres.FileLoadStatus;
import org.sagres.importer.ImportService;
import org.sagres.sampleSet.MongoDataService;
import grails.converters.JSON


//*****************************************************************************

class ChipsLoadedController
{
//-----------------------------------------------------------------------------

	def scaffold = true

    def dataSource; //injected
	def mongoDataService; //injected
    def importService; //injected
    def mailService; //injected

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

//=============================================================================

	def index =
	{
		redirect(action: "list", params: params)
	}

//=============================================================================

	def list =
	{
		def chipsLoadedList
		def chipsLoadedCount = 0
		
        def pagParams = [sort:"dateStarted", order: "desc"]

		//println "params: " + params
		//println "mfilter: " + m_filter
 		if ( m_filter )
		{
			chipsLoadedList = ChipsLoaded.createCriteria().list( pagParams )
			{
				if ( m_filter.chipType )
				{
					eq( "chipType", m_filter.chipType );
				}
				if ( m_filter.loadStatus )
				{
					eq( "loadStatus", m_filter.loadStatus );
				}
				if ( m_filter.firstDate )
				{
					ge( "dateStarted", m_filter.firstDate );
				}
				if ( m_filter.lastDate )
				{
					le( "dateStarted", m_filter.lastDate );
				}
				if ( m_filter.dataSource )
					eq( "genomicDataSource", m_filter.dataSource );
			}
		}
			else
			{
				chipsLoadedList = ChipsLoaded.list( pagParams );
			}
		if (chipsLoadedList)
		{
			chipsLoadedCount = chipsLoadedList.size()
		}
        def model = [ chipsLoadedList: chipsLoadedList, chipsLoadedCount: chipsLoadedCount, filter: m_filter, params: params]
       
		return model
	}

//-----------------------------------------------------------------------------

    def filteredChipsLoaded =
    {
		//println "filtered params: " + params
        def filterParams = [:];
        if ( params.chipTypeFilter )
        {
            filterParams.chipType =
                    ChipType.get( params.chipTypeFilter?.toLong() );
        }
        if ( params.loadStatusFilter )
        {
            filterParams.loadStatus =
                    FileLoadStatus.get( params.loadStatusFilter?.toLong() );
        }
        if ( params.firstDateFilter )
        {
            filterParams.firstDate =
                    Date.parse( "yyyy-MM-dd", params.firstDateFilter );
        }
        if ( params.lastDateFilter )
        {
            filterParams.lastDate =
                    Date.parse( "yyyy-MM-dd", params.lastDateFilter );
        }
        if ( params.dataSourceFilter )
        {
            filterParams.dataSource =
                    GenomicDataSource.get( params.dataSourceFilter.toLong() );
        }
        m_filter = new ChipsLoadedFilter( filterParams );
        redirect( action: "list" , params: params);
    }

//=============================================================================

	def show =
	{
		def chipsLoadedInstance = ChipsLoaded.get(params.id)
		if (!chipsLoadedInstance)
		{
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'chipsLoaded.label', default: 'ChipsLoaded'), params.id])}"
			redirect(action: "list")
		}
		else
		{
			[chipsLoadedInstance: chipsLoadedInstance]
		}
	}
// { "_id" : ObjectId("5193d0fde4b0ea10ab23ed38"), "importType" : "Expression data",
//	 "files" : [ "/var/local/bit/fileUploads/expressionData/toBeImported/illumina_human12_v4/P107_2012_TestChip_1HT12V4_BM_V4_0_R2_23Oct2012_NoSymbolNoProbe2_Bg.txt" ],
//	 "importParams" : { "type" : "General Expression Data", "chipTypeId" : NumberLong(17), "genomicDataSourceId" : NumberLong(1),
//	 	"formatValidation" : { "signalColumnRequired" : true, "pValColumnRequired" : true },
//   	"dataValidation" : { "requireKnownProbeId" : true, "requireSignal" : true, "requireDetection" : true, "requireNegatives" : true },
//   	"emailRecipient" : "SPresnell@benaroyaresearch.org"
//	  }
// }
//=============================================================================

    private
    Map uploadFiles( List< String > fileFieldNames,
                     String fileType,
                     String defaultDataSource,
                     Map formatValidation = [:],
                     Map dataValidation = [:],
                     String subDir = "" )
    {
        Map importParams = [ type: fileType ];
        if ( params.chipType )
        {
            ChipType chipType = ChipType.get( params.chipType );
            importParams.chipTypeId = chipType.id;
            if ( subDir == "" )
            {
                String chipDir = chipType?.importDirectoryName;
                if ( ! chipDir )
                {
                    String msg = "No import directory found for chip type" +
                            params.chipType;
                    return [ success: false,
                             message: msg ];
                }
                subDir = chipDir;
            }
        }
        else if ( (fileType == "GEO Family (soft)") ||
                  (fileType == "GEO Series Matrix (txt)") )
        {
            subDir = "soft";
        }
        String uploadPath =
                grailsApplication.config.fileUpload.baseDir +
                "expressionData/toBeImported/" + subDir + "/";
        GenomicDataSource genomicDataSource = params.genomicDataSource ?
                GenomicDataSource.get( params.genomicDataSource ) :
                GenomicDataSource.findByName( defaultDataSource );
        importParams.genomicDataSourceId = genomicDataSource.id;
        importParams.formatValidation = formatValidation;
        importParams.dataValidation = dataValidation;
        importParams.emailRecipient = params.emailRecipient;

        File uploadPathFile = new File( uploadPath );
        if ( (uploadPathFile.exists() == false) &&
             (uploadPathFile.mkdirs() == false) )
        {
            String msg = "Unable to make directory '${uploadPath}'" +
                    " for uploading";
            return [ success: false,
                     message: msg ];
        }

        List< String > uploadedFiles = [];

		for ( int i = 0; i < fileFieldNames.size(); ++i )
        {
			String fileFieldName = fileFieldNames[ i ];
            //uses Spring MultipartHttpServerRequest to get a MultipartFile
            def mpFile = request.getFile( fileFieldName );
            if ( mpFile.empty == false )
            {
                String filename = mpFile.getOriginalFilename();
                String uploadSpec = uploadPath + filename;
                File dest = new File( uploadSpec );
                mpFile.transferTo( dest );
                uploadedFiles.add( uploadSpec );
            }
            else
            {
				if ( i == 0 )
				{
					return [ success: false,
							 message: fileFieldName + " was not uploaded" ];
				}
				else
				{
					uploadedFiles.add( null );
				}
            }
        }

        if ( params.importNow || params.runNow )
        {
            importService.setSqlDataSource( dataSource );
            importService.setMongoDataService( mongoDataService );
            importService.setMailService( mailService );
            Map result =
                    importService.importExpressionDataFiles( uploadedFiles,
                                                             importParams );
            if ( result.success )
            {
                return [ success: true,
                         message: "Import succeeded" ];
            }
            else
            {
                return [ success: false,
                         message: "Import failed. " + result.message ];
            }
        }
        else
        {
            Map importDoc =
                    [
                        importType: "Expression data",
                        files: uploadedFiles,
                        importParams: importParams
                    ];
            mongoDataService.insert( "ToBeImported", null, importDoc );
            return [ success: true,
                     message: "File(s) uploaded for processing later" ];
        }
    }

//=============================================================================

	def showUploadExpressionDataForm = 
	{
		render( view: "upload_expression_data" );
	}

	def batchImportExpressionFiles = 
	{
		def fileList = []
		def chipTypes = ChipType.findAllByTechnology(Technology.findByName("Microarray"))
		List importDocs = mongoDataService.find( "ToBeImported", [ importType: "Expression data" ], null, null, 0 );

		chipTypes.each { chip ->
			String chipDir = chip?.importDirectoryName;
			if (chipDir)
			{
				String uploadPath = grailsApplication.config.fileUpload.baseDir + "expressionData/toBeImported/" + chipDir + "/";
				File uploadPathFile = new File( uploadPath );
				if (uploadPathFile.exists() == true) {
					uploadPathFile.eachFileMatch(FILES, ~/.*\.(txt|tsv)/, { // match only .txt or .tsv files.
						def fileNamePath = uploadPath + it.name
						if (importDocs)
						{
							importDocs.each { doc -> // check against scheduled uploads, ignore if already scheduled.
								if (doc.files[0] != fileNamePath)
								{
									fileList <<  ['path': fileNamePath, 'chipTypeId': chip.id, 'type': "General Expression Data"]
								}
							}
						}
						else
						{
							fileList <<  ['path': fileNamePath, 'chipTypeId': chip.id, 'type': "General Expression Data"]
						}
					})
				}
			}
		}
		if (fileList) {
			String report = ""
			
			//println "files to be uploaded: " + fileList

			importService.setSqlDataSource( dataSource );
			importService.setMongoDataService( mongoDataService );
			importService.setMailService( mailService );

			def genomicDataSource = GenomicDataSource.findByName( (String) grailsApplication.config.importer.defaultGenomicSource );
			
			fileList.each { file ->
				
				def importParams = [:]
				def dataParams = [  'requireKnownProbeId' : true,
									'requireSignal': true,
									'requireNegatives': true,
									'requireExactlyProbes': true] 
				
				importParams.chipTypeId  = file.chipTypeId
				importParams.type = file.type
				importParams.genomicDataSourceId = genomicDataSource.id;
//				importParams.formatValidation = makeFormatValidationMap(null)
				importParams.dataValidation = makeDataValidationMap(dataParams)
				importParams.emailRecipient = "none" // grailsApplication.config.importer.defaultEMailTo

				println "Importing: " + file.path + " with: " + importParams + "\n"
				// Map result = ['success': false]
				Map result = importService.importExpressionDataFiles( [ file.path ] , importParams )
					
				if ( result.success )
	            {
	                report += "SUCCESS: " + file.path + "\n"
	            }
	            else
	            {
	                report += "FAILURE: " + file.path + "\n"
	                report += result.message ? result.message + "\n" : ""
	            }
			}
			
			render(report)
			
		} else {
			render("No files to Import\n")
		}
	}
//-----------------------------------------------------------------------------

    def uploadExpressionData =
    {
        String fileType = params.fileType;
        Map result = uploadFiles( [ "expressionDataFile" ],
                                  fileType, "Benaroya",
                                  makeFormatValidationMap( params ),
                                  makeDataValidationMap( params ) );
        flash.message = result.message;
        log.debug( flash.message );
        redirect( action: (result.success ? "list" :
                           "showUploadExpressionDataForm") );
    }
    
//=============================================================================

    private
    Map makeFormatValidationMap( def params )
    {
        Map validation = [:];
        validation[ "signalColumnRequired" ] = true; //always required
        List< String > booleanFields =
                [
                    "pValColumnRequired"
                ];
        for ( String field : booleanFields )
        {
            validation[ (field) ] = (params[ (field) ] != null);
        }
        return validation;
    }

//-----------------------------------------------------------------------------

    private
    Map makeDataValidationMap( def params )
    {
        Map validation = [:];
        List< String > booleanFields =
                [
                    "requireKnownProbeId",
					"requireExactlyProbes",
                    "requireSignal",
                    "requireDetection",
                    "requireNegatives"
                ];
        for ( String field : booleanFields )
        {
            validation[ (field) ] = (params[ (field) ] != null);
        }
        return validation;
    }

//=============================================================================

	def showUploadBriRnaSeqFilesForm = 
	{
        List chipTypeList = ChipType.findAll().findAll { chipType ->
            chipType.name.startsWith( "RnaSeq" ) &&
            chipType.importDirectoryName
        }
		render( view: "upload_bri_rnaseq_files",
                model: [ chipTypeList: chipTypeList ] );
	}

//-----------------------------------------------------------------------------

	def uploadBriRnaSeqFiles =
	{
        Map result = uploadFiles( [ "rawCountFile", "tmmNormalizedFile" ],
                                  "RnaSeqCountAndTmm", "Benaroya" );
        flash.message = result.message;
        log.debug( flash.message );
        redirect( action: (result.success ? "list" :
                           "showUploadBriRnaSeqFilesForm") );
	}

//=============================================================================

	def showUploadFocusedArrayDataForm = 
	{
		render( view: "upload_focused_array_data" );
	}

//-----------------------------------------------------------------------------

    def uploadFocusedArrayData =
    {
		List< String > fileFields = []
		for ( int i = 1; i < 100; ++i )
		{
			String fileFieldName = "expressionDataFile_" + i;
            def mpFile = request.getFile( fileFieldName );
            if ( mpFile && (mpFile.empty == false) )
            {
				fileFields.add( fileFieldName );
			}
			else
			{
				break;
			}
		}
		fileFields.addAll( [ "housekeepingGenesFile",
							 "referenceSamplesFile" ] );

        Map result = uploadFiles( fileFields,
                                  "FocusedArray", "Benaroya",
								  [:],
                                  [:] );
        flash.message = result.message;
        log.debug( flash.message );
        redirect( action: (result.success ? "list" :
                           "showUploadFocusedArrayDataForm") );
    }
    
//=============================================================================

    def batchUploadSoftFile =
    {
        String fileType;
        if ( params.type == "Family" )
        {
            fileType = "GEO Family (soft)";
        }
        else if ( params.type == "SeriesMatrix" )
        {
            fileType = "GEO Series Matrix (txt)";
        }
        else
        {
            render "Failed: Unknown type\n";
            return;
        }
        Map result = uploadFiles( [ "file" ], fileType, "GEO",
                                  [:], [:], "soft" );
        if ( result.success )
        {
            render "OK\n";
        }
        else
        {
            render "Failed: " + result.message + "\n";
        }
    }

//=============================================================================

    def doImports =
    {
        String report = "";

			if (dataSource == null) {
				println("dataSource is null in doImports")
				log.error("dataSource is null in doImports")
			}
			
        importService.setSqlDataSource( dataSource );
        importService.setMongoDataService( mongoDataService );
        importService.setMailService( mailService );

		if (dataSource == null) {
			println("dataSource is null after setting in doImports")
			log.error("dataSource is null after setting in doImports")
		}

        List importDocs =
                mongoDataService.find( "ToBeImported",
                                       [ importType: "Expression data" ],
                                       null, null, 0 );
        importDocs.each { doc ->
            Map result = importService.importExpressionDataFiles(
                doc.files, doc.importParams );

            if ( result.success )
            {
                report += "SUCCESS: " + doc.files.join( ", " ) + "\n";
            }
            else
            {
                report += "FAILURE: " + doc.files.join( ", " ) + "\n" +
                        result.message ? result.message + "\n" : "";
            }
            mongoDataService.remove( "ToBeImported", doc, null );
        }

        render( report );
    }

//=============================================================================

    def listFileTypes =
    {
        List< Map > fileTypes = importService.getExpressionFileFormats( );
        render fileTypes as JSON;
    }

//-----------------------------------------------------------------------------

    def listChipTypes =
    {
        List< Map > resultList = [];
        List< ChipType > chipTypes = ChipType.findAll( );
        for ( ChipType chipType : chipTypes )
        {
            if ( (chipType.probeListTable == "") ||
                 (chipType.active < 0) ||
                 (chipType.importDirectoryName == "") )
            {
                continue;
            }
			if ( params.technology &&
				 (chipType.technology.name != params.technology) )
			{
				continue;
			}
            if ( params.forMat &&
                 (chipType.moduleVersionId <= 0) )
            {
                continue;
            }
            Species species = Species.get( chipType.chipData.speciesId );
            String name = chipType.chipData.manufacturer + " " +
                    chipType.chipData.model + " " +
                    (chipType.chipData.chipVersion ?
                     ("v" + chipType.chipData.chipVersion + " ")  :  "") +
                    "(" + chipType.name + ") " +
                    "(" + species.english + ")";
            int validationLevel = 1;
            if ( chipType.chipData.manufacturer == "Illumina" )
            {
                validationLevel = 2;
            }
            Map chip =
                    [
                        id: chipType.id,
                        name: name,
                        validationLevel: validationLevel,
                        manufacturer: chipType.chipData.manufacturer
                    ];
            resultList.add( chip );
        }
        resultList.sort { it.name };
        render resultList as JSON;
    }

//-----------------------------------------------------------------------------

    def listGenomicDataSources =
    {
        List< Map > resultList = [];
        List< GenomicDataSource > dataSources = GenomicDataSource.findAll( );
        for ( GenomicDataSource dataSource: dataSources )
        {
            Map source =
                    [
                        id: dataSource.id,
                        name: dataSource.displayName
                    ];
            resultList.add( source );
        }
        render resultList as JSON;
    }

//=============================================================================

    static ChipsLoadedFilter m_filter;

//-----------------------------------------------------------------------------
}

//*****************************************************************************

class ChipsLoadedFilter
{
//-----------------------------------------------------------------------------

    ChipType            chipType;
    FileLoadStatus      loadStatus;
    Date                firstDate;
    Date                lastDate;
    GenomicDataSource   dataSource;
    
//-----------------------------------------------------------------------------
}

//*****************************************************************************
