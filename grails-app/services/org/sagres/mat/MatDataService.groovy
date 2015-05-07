package org.sagres.mat

import java.util.List;
import java.util.Map;

import static groovy.io.FileType.FILES
import groovy.sql.GroovyRowResult
import groovy.sql.Sql
import common.chipInfo.ChipType
import org.sagres.importer.TextTableSeparator;
import org.sagres.importer.TextTable;
import org.sagres.util.FileSys;

class MatDataService {

	static transactional = true
	def fileSep = System.getProperty("file.separator")
	def matConfigService
	def matResultsService
	def mailService
	def mongoDataService
	def dataSource
	def dm3Service

	def springSecurityService

	def thumbnailSize = 200

	def serviceMethod() {}

	def escapeForCSV(String parameter, def value) {
		def altValue = value
		if (value.toString().contains(",")) {
			altValue = "\"${value}\""
		}
		return "${parameter},${altValue}${System.getProperty("line.separator")}"
	}


	def createAnalysisFolder(long analysisId, String dataSetName = null) {
		String workingDirectory = matConfigService.getMATWorkDirectory()
		File analysisDir = new File("${workingDirectory}${fileSep}${analysisId}")
		if (analysisDir.exists()) {
			log.error("Working directory ${analysisId} already exists")
		} else {
			analysisDir.mkdir()
			File dataDir = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Data")
			dataDir.mkdir()
			if ( dataSetName )
			{
				File infoName = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}${dataSetName.replaceAll(" ", "_")}")
				infoName.mkdir()
			}
			File paramDir = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Parameters")
			paramDir.mkdir()
			File scriptDir = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}scripts")
			scriptDir.mkdir()
			File resultDir = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Results")
			resultDir.mkdir()
		}
	}

	def writeOutDataFiles(long analysisId, Version version) {
		String workingDirectory = matConfigService.getMATWorkDirectory()
		File scriptDir = new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}ModuleData")
		scriptDir.mkdir()
		new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}ModuleData${fileSep}${version.getVersionFileName()}") << version.getVersionData()
		new File("${workingDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}ModuleData${fileSep}${version.getFunctionFileName()}") << version.getFunctionData()
	}

	def writeOutScriptFiles(long analysisId) {
		String workingDirectory = matConfigService.getMATWorkDirectory()
		File sourceScriptDir = new File("${workingDirectory}${fileSep}scripts${fileSep}ModuleAnalysis")
		def p = ~/.*\.R/
		sourceScriptDir.eachFileMatch FILES, p, { File source ->
			new File("${workingDirectory}${fileSep}${analysisId}${fileSep}scripts${fileSep}${source.name}") << source.bytes
		}
	}

	def copyFile (def sourceFile, def destinationFile) {
		File source = new File(sourceFile)
			new File(destinationFile) << source.bytes
	}

	def writeOutUploadedFile(long analysisId, def uploadedFile, def datasetName) {
		String dataDir = "${matConfigService.MATWorkDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}${datasetName.replaceAll(" ", "_")}"
		File dDir = new File(dataDir)
		if (!dDir.exists()) {
			dDir.mkdir()
		}
		uploadedFile.transferTo(new File("${dataDir}${fileSep}${uploadedFile.originalFilename}"));
	}

	def copyExistingDataSet(long analysisId, def dataName, def datasetName) {
		String dataDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}Data${fileSep}${datasetName.replaceAll(" ", "_")}"
		String scriptDir = "${matConfigService.getMATWorkDirectory()}${fileSep}data"
		File dDir = new File(dataDir)
		if (!dDir.exists()) {
			dDir.mkdir()
		}
		File sourceData = new File("${scriptDir}${fileSep}${dataName}")
		new File(dDir, dataName) << sourceData.bytes
	}

	def writeParameterFile(Analysis analysis) {
		def parameterFile = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Parameters${fileSep}parameters.csv")
		parameterFile << "Parameter,Value${System.getProperty("line.separator")}"
		parameterFile << "project,${analysis.getDatasetName().replaceAll(' ', '_')}${System.getProperty("line.separator")}"
		analysis.returnScriptParametersForR().each {
			if (it.value != null) {
				parameterFile << escapeForCSV(it.key, it.value)
			}
		}
	}

	def processResults(long analysisId) {
		/**
		 **/
	}

	def createCommandLine(long analysisID) {
		def projDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisID}${fileSep}"
		def script = "${matConfigService.getRScriptLocation()} ${projDir}${fileSep}scripts${fileSep}ModuleAnalysis.R ${projDir}"
		return script
	}


	//Done seperately from hibernate to prevent two open sessions
	def getDatasetNameFromAnalysis(long analysisId) {
		Sql db = Sql.newInstance(dataSource)
				 def query =
				 "SELECT dataset_name from analysis where id= " + analysisId +
				 " LIMIT 1"
		def result = (GroovyRowResult)db.firstRow(query)
		def datasetName = result ? result.get("dataset_name") : null
		db.close()
		return datasetName
	}

		//Done seperately from hibernate to prevent two open sessions
	def getSampleSetIdFromAnalysis(long analysisId) {
		Sql db = Sql.newInstance(dataSource)
				 def query =
				 "SELECT sample_set_id from analysis where id= " + analysisId +
				 " LIMIT 1"
		def result = (GroovyRowResult)db.firstRow(query)
		def sampleSetId = result ? result.get("sample_set_id") : null
		db.close()
		return sampleSetId
	}

	def runScript(String cmdLine, long analysisId, Closure postRunActions = {}) {
		dm3Service.generateProbeToGeneExpressionMapping(analysisId)
		def projDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}"
		File scriptF = new File("${projDir}runScript")
		scriptF << cmdLine
		String logFileErr = "${projDir}analysis.log"
		String logFileStd = "${projDir}analysis.stdout"
		def datasetName = getDatasetNameFromAnalysis(analysisId)
		def csvIds = [:]
		csvIds.put("analysisId", analysisId)
		def sampleSetId = getSampleSetIdFromAnalysis(analysisId)
		csvIds.put("sampleSetId", sampleSetId)

		def resultsDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}Results${fileSep}${datasetName.replaceAll(" ", "_")}"
		runAsync {
			def fose = new FileOutputStream(logFileErr)
			def foss = new FileOutputStream(logFileStd)
			Process proc = cmdLine.execute()
			fose << proc.in
			fose << proc.err
			foss << proc.out
			proc.waitFor()
			foss << "\n----------------------------------------------------"
			File scriptComplete = new File("${projDir}scriptComplete")
			try {
				 File resDir = new File(resultsDir)
				def p = ~/.*\.csv/
				resDir.eachFileMatch FILES, p, { File outFile ->
					mongoDataService.storeCsv(outFile, true, "analysis_results", csvIds)
				}
			} catch (Exception ex) {
				println "Exception loading analysis ${analysisId} csv files into mongo: ${ex}"
			}
			try {
				println "About to attempt post run actions - ${analysisId}"
				postRunActions()

			}  catch  (Exception ex) {
				println "Exception with postRunActions : ${ex.toString()}"
			}
			scriptComplete << "Finished Running Analysis Script"
		}
	}

	def getJobsForListView(def params = null, boolean hasResults = true, common.SecUser user = null) {
		int loopCount = 0;
		long startTime = System.currentTimeMillis()
		def workDir = matConfigService.getMATWorkDirectory()
		def htmlDir = matConfigService.getMATLink()

		def sort = 'runDate'
		def order = 'desc'
		if (params.sort != null) {
			 sort = params.sort
			 order = params.order
		}
		def allJobs
		try {
			 allJobs = Analysis.createCriteria().list(sort: sort, order: order) {
				 isNull('flagDelete')
			 }

		} catch (Exception ex) {
			println "Exception ${ex.toString()}"
		}
		//println "time allJobs = ${System.currentTimeMillis() - startTime}ms"
		
		def results = [:]
		//String userName = user?.username
		allJobs.each { Analysis job ->
//			try {
//				if (userName != null && !userName.equals(job.user)) {
//					next
//				}
//				if (++loopCount % 100 == 0) {
//					println "time top ${loopCount} = ${System.currentTimeMillis() - startTime}ms"
//					startTime = System.currentTimeMillis()
//				}
		
//        if (job.flagDelete == null) {
          if (isAnalysisComplete(job.id) == hasResults) {
            def analysisSummary = AnalysisSummary.findByAnalysisId(job.id)
            if (hasResults && !analysisSummary.resultsLoaded) {
              loadAnalysis(analysisSummary)
            }
			File logFile = new File(workDir + fileSep + job.id + fileSep + "analysis.log")
			if (logFile.exists() && logFile.size() > 0) {
				String resultLink = htmlDir + "/" + job.id + "/analysis.log"
				results.put(job, resultLink)
			  } else {
			  	results.put(job, "")
			  }
          } else {
          }
//        }
//      }	catch (Exception ex) {
//				println "Error trying to read job: ${job.id} ${ex.printStackTrace()}"
//			}
		}
		return results
	}

	def convertFileNameToDesc(String datasetName, String fileName) {
		def fName = fileName.substring(0, fileName.length() - 4)
    fName = fName.replaceFirst(datasetName.replaceAll(" ","_"),"")
		def parts = fName.split("_")
    StringBuilder sb = new StringBuilder()
    int start = 1
    if (parts[0] == "thumb") {
      start = 2
    }
    for (int i = start; i < parts.size() - 5; i++) {
      sb.append(parts[i] + " ")
    }
    return sb.toString().trim()
	}


	def isAnalysisComplete(def analysisId) {
		AnalysisSummary analysisSummary = AnalysisSummary.findByAnalysisId(analysisId)
		if (analysisSummary == null) {
			analysisSummary = new AnalysisSummary(analysisId: analysisId)
			if (!analysisSummary.save()) {
				analysisSummary.errors.each {
					println "Error Saving new analysisSummary: ${it}"
				}
			}
		}
		if (analysisSummary.resultsLoaded && analysisSummary.analysisCompleteTime != null) {
			return true
		}
		File logFile = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}analysis.log")
		def complete = false;
		if (!logFile.exists()) { return complete}
		if (logFile.length() == 0) { return complete}
		logFile.eachLine {
			if (it.contains(matConfigService.getAnalysisCompleteString())) {
				complete = true
				//analysisSummary.analysisCompleteTime = new java.sql.Date(logFile.lastModified())
			}
		}
		if (!analysisSummary.save() ) {
			analysisSummary.errors.each {
				println it
			}
		}
		return complete
	}

	def loadAnalysis(def analysisSummary) {
//		  def analysisSummary = AnalysisSummary.findByAnalysisId(analysisId)
//			if (analysisSummary == null) {
//				analysisSummary = new AnalysisSummary(analysisId: analysisId)
//			}
//			runAsync {
//				matResultsService.processAnalysisResults(analysisId)
//			}
//			File logFile = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysisId}${fileSep}analysis.log")
			analysisSummary.resultsLoaded = true
			analysisSummary.analysisCompleteTime = new Date()
			if (!analysisSummary.save()  ) {
				analysisSummary.errors.each {
					println "Exception saving analysisSummary: " + it
				}
			}
	}

	def convertFileNameToDate(String fileName) {
		def fName = fileName.substring(0, fileName.length() - 4)
		def parts = fName.split("_")
		def fileDate = "${parts[parts.size() - 5]} ${parts[parts.size() - 4]} ${parts[parts.size() - 3]}"
		return fileDate
	}

	def getResultLinks(Analysis analysis, String fileType) {
		def resultLinks = [:]
		File resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Results${fileSep}${analysis.datasetName.replaceAll(" ", "_")}")
		def urlPref = "${matConfigService.MATLink}/${analysis.id}/Results/${analysis.datasetName.replaceAll(' ', '_')}/"
		if (!resDir.exists()) {
			return resultLinks
		}
		def p = ~/.*\.${fileType}/
		resDir.eachFileMatch FILES, p, { File outFile ->
			resultLinks.put("${urlPref}${outFile.name}", convertFileNameToDesc(analysis.datasetName,outFile.name))
		}
		return resultLinks.sort()
	}


  def getAnnotationFile(Analysis analysis)
  {
    File resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Data${fileSep}ModuleData${fileSep}")
    if (!resDir.exists()) {
			return null
		}
    def f = null
    def p = ~/.*_function\.csv/
		resDir.eachFileMatch FILES, p, { File outFile ->
      if (!f) f = outFile
		}
    return f
  }

  def getDataFile(Analysis analysis, String filename)
  {
    File resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Data${fileSep}${analysis.datasetName.replaceAll(" ", "_")}")
    if (!resDir.exists()) {
		resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Data");
		if (!resDir.exists())
		{
			return null
		}
	}
    def f = null
    def p = ~/${filename}/
		resDir.eachFileMatch FILES, p, { File outFile ->
			if (!f) f = outFile
		}
    return f
  }

  def getFile(Analysis analysis, def filename)
  {
    File resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Results${fileSep}${analysis.datasetName.replaceAll(" ", "_")}")
    if (!resDir.exists()) {
		resDir = new File("${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Results")
		if (!resDir.exists())
		{
			return null
		}
	}
    def f = null
    def p = ~/${filename}/
		resDir.eachFileMatch FILES, p, { File outFile ->
			if (!f) f = outFile
		}
    return f
  }

  def archiveCsvFiles() {
    runAsync {
      Sql sql = Sql.newInstance(dataSource)
      String query = """
        SELECT a.id 'id', a.dataset_name 'ds', a.sample_set_id 'ss' FROM analysis_summary s
        JOIN analysis a ON s.analysis_id = a.id
        WHERE s.analysis_complete_time IS NOT NULL""".toString()
      sql.eachRow(query) { analysis ->
        def csvIds = [analysisId:analysis.id, sampleSetId:analysis.ss]
        if (!mongoDataService.exists("analysis_results", csvIds, null)) {
          println "Archiving ${analysis.id}"
          def resultsDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Results${fileSep}${analysis.ds.replaceAll(" ", "_")}"
          try {
            Thread.start {
              File resDir = new File(resultsDir)
              def p = ~/.*\.csv/
              resDir.eachFileMatch FILES, p, { File outFile ->
                mongoDataService.storeCsv(outFile, true, "analysis_results", csvIds)
              }
            }
          } catch (Exception ex) {
            println "Exception loading analysis ${analysis.id} csv files into mongo: ${ex}"
          }
        }
      }
      sql.close()
    }
  }

  def archiveDesignCsvFiles() {
    runAsync {
      def analyses = Analysis.list()
      analyses.each { analysis ->
        def csvIds = [analysisId:analysis.id, sampleSetId:analysis.sampleSetId, filename:"designFile.csv"]
        if (!mongoDataService.exists("analysis_results", csvIds, null)) {
          try {
            Thread.start {
              File designFile = getDataFile(analysis, "designFile.csv")
              if (designFile) {
                mongoDataService.storeCsv(designFile, true, "analysis_results", csvIds)
              }
            }
          } catch (Exception ex) {
            println "Exception loading analysis ${analysis.id} design csv file into mongo: ${ex}"
          }
        }
      }
    }
  }

	void writeModuleAssignFile( ChipType chipType, int moduleGeneration,
								String fileSpec, Sql sql = null )
	{
		sql = sql ?: Sql.newInstance( dataSource );
		PrintWriter writer = new PrintWriter( fileSpec );
		TextTableSeparator sep = TextTableSeparator.CSV;
		List< String > fields = [ "geneName", "module", "geneID" ];
		writer.println( TextTable.joinRow( fields, sep, true ) );

		String query =
				"SELECT id" +
				"  FROM module_generation" +
				"  WHERE chip_type_id=? AND generation=?";
		long moduleGenerationId =
				sql.firstRow( query, [ chipType.id, moduleGeneration ] ).id;

		query =
				"SELECT md.probe_id, m.module_name, f.id" +
				"  FROM module AS m, module_detail AS md, fluidigm AS f" +
				"  WHERE m.module_generation_id=" + moduleGenerationId +
				"    AND md.module_id = m.id" +
				"    AND f.chip_type_id=" + chipType.id +
				"    AND f.target=md.probe_id";
		List< GroovyRowResult > rows = sql.rows( query );
		for ( GroovyRowResult row : rows )
		{
			fields = [ row.probe_id, row.module_name, "A" + row.id ];
			writer.println( TextTable.joinRow( fields, sep, true ) );
		}

		writer.flush( );
		writer.close( );
	}
}
