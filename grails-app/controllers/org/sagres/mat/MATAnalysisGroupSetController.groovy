package org.sagres.mat

import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication
import org.sagres.util.FileSys;
import org.sagres.util.OS;
import org.sagres.sampleSet.SampleSet;
import org.sagres.sampleSet.SampleSetService;


class MATAnalysisGroupSetController {

	def dm3Service
	def matConfigService
	def matDataService
	def springSecurityService
    def mongoDataService
	def grailsApplication
	def mailService
	SampleSetService sampleSetService
	def fileSep = System.getProperty("file.separator")

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]

	def index = {
		redirect(action: "list", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[MATAnalysisGroupSetInstanceList: MATAnalysisGroupSet.list(params), MATAnalysisGroupSetInstanceTotal: MATAnalysisGroupSet.count()]
	}

	def create = {
		def MATAnalysisGroupSetInstance = new MATAnalysisGroupSet()
		MATAnalysisGroupSetInstance.properties = params
		return [MATAnalysisGroupSetInstance: MATAnalysisGroupSetInstance]
	}

	def save = {
		def MATAnalysisGroupSetInstance = MATAnalysisGroupSet.get(params.id)
		if (MATAnalysisGroupSetInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), MATAnalysisGroupSetInstance.id])}"
			redirect(action: "show", id: MATAnalysisGroupSetInstance.id)
		}
		else {
			render(view: "create", model: [MATAnalysisGroupSetInstance: MATAnalysisGroupSetInstance])
		}
	}

	def show = {
		def MATAnalysisGroupSetInstance = MATAnalysisGroupSet.get(params.id)
		if (!MATAnalysisGroupSetInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), params.id])}"
			redirect(action: "list")
		}
		else {
			[MATAnalysisGroupSetInstance: MATAnalysisGroupSetInstance]
		}
	}

	def edit = {
		def MATAnalysisGroupSetInstance = MATAnalysisGroupSet.get(params.id)

		def sampleSetId = params.sampleSetId
		def analysisId = params.analysisId
		if (!MATAnalysisGroupSetInstance) {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), params.id])}"
			redirect(action: "list")
		}
		else {
			def analysis = Analysis.findById(analysisId)
			return [MATAnalysisGroupSetInstance: MATAnalysisGroupSetInstance, analysisId: analysisId, sampleSetId: sampleSetId, analysisInstance: analysis]
		}
	}

	def update = {
		println( "MATAnalysisGroupSetController update" ); //!!!
		MATAnalysisGroupSet matAnalysisGroupSet = MATAnalysisGroupSet.findById(params.id)

		def caseLabel = params.caseLabel
		def controlLabel = params.controlLabel
		def analysis = Analysis.findById(params.analysisId)
		def user = springSecurityService.currentUser
		String sender = grailsApplication.config.importer.defaultEMailFrom
		String emailAdd = user?.email.toString()
		String userName = user?.username.toString().capitalize()
		analysis.user = user?.username
		def sampleSetId = params.sampleSetId
		//Find Radio buttons, save groups
		matAnalysisGroupSet.groups.each {MATAnalysisGroup mag ->
			def key = "rad_${mag.groupSetName.replaceAll(" ", "_")}".toString()
			def value = params.get(key)
			if (value.equalsIgnoreCase("case")) {
				mag.setCaseControlGroup(1)
				mag.setGroupLabel(caseLabel)
			} else if (value.equalsIgnoreCase("control")) {
				mag.setCaseControlGroup(0)
				mag.setGroupLabel(controlLabel)
			} else {
				mag.setGroupLabel(value)
				mag.setCaseControlGroup(-1)
			}
			mag.save()
		}
		def designFile = "designFile.csv"
		analysis.setDesignDataFile(designFile)
		analysis.save()

		AnalysisSummary analysisSummary = new AnalysisSummary()
		analysisSummary.analysisId = analysis.id
		analysisSummary.setAnalysisStartTime(new Date())
		analysisSummary.save()

		SampleSet sampleSet = SampleSet.get( sampleSetId );
		if ( sampleSet?.chipType?.technology?.name == "Focused Array" )
		{
			runFocusedArrayAnalysis( analysis, sampleSet, matAnalysisGroupSet );
			redirect( controller: "analysis",
					  action: "show",
					  id: analysis.id );
			return;
		}

		//write out design file
		String dataDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}Data${fileSep}${analysis.datasetName.replaceAll(" ", "_")}"

		def projDir = "${matConfigService.getMATWorkDirectory()}${fileSep}${analysis.id}${fileSep}"
		dm3Service.generateDesignFile(designFile, params.id, analysis.datasetName, analysis.id)
		String emailText = 	 "${userName}, a Module Analysis Tool (MAT) run has completed. You can view the results at " +
       					       "${grailsApplication.config.grails.serverURL}/analysis/show/${analysis.id}."
		runAsync {

			//Call web service to write out file
			// Need to move this to part of the run script portion

			if (dm3Service.requestExpressionFile(sampleSetId, "${dataDir}${fileSep}${analysis.expressionDataFile}")) {
				matDataService.runScript(matDataService.createCommandLine(analysis.id),analysis.id,
				{
						if (emailAdd != null && grailsApplication.config.send.email.on) {
							mailService.sendMail {
							   from "${sender}"
								 to "${emailAdd}"
							subject "MAT run complete"
							   html "${emailText}"
								}
						} else {
							println "No User to send notification email for analysis ${analysis.id}"
						}
					})
				//Have expression file written out
			} else {
				println "Error in generating expression file for analysis ${analysis.id}"
			}
		}
		redirect(controller: "analysis", action: "notifyJobStarted", id: analysis.id)
	}

	void runFocusedArrayAnalysis( Analysis analysis,
								  SampleSet sampleSet,
								  MATAnalysisGroupSet matAnalysisGroupSet )
	{
		Date start = new Date( );
		String baseDir = matConfigService.getMATWorkDirectory() + '/';
		String analysisDir = baseDir + analysis.id + '/';
		String dataDir = analysisDir + "Data/";
		FileSys.makeDirIfNeeded( dataDir );
		String paramsDir = analysisDir + "Parameters/";
		FileSys.makeDirIfNeeded( paramsDir );
		String scriptsSrcDir = baseDir + "scripts/FocusedArray/";
		String scriptsCopyDir = analysisDir + "scripts/";
		FileSys.makeDirIfNeeded( scriptsCopyDir );
		String resultsDir = analysisDir + "Results/";
		FileSys.makeDirIfNeeded( resultsDir );

		// runAsync
		// {
			sampleSetService.generateFocusedArrayExpressionCSV(
				sampleSet, dataDir + "FC.csv" );
			int moduleGeneration = 3; //!!!
			matDataService.writeModuleAssignFile(
				sampleSet.chipType, moduleGeneration,
				dataDir + "moduleAssignFile.csv" );
			dm3Service.generateFocusedArrayDesignFile(
				matAnalysisGroupSet, dataDir + "designFile.csv" );
			FileSys.copyDirectory( scriptsSrcDir, scriptsCopyDir, true );

			String scriptName = "genomicCBCMATanalysis.R";
			String scriptSpec = scriptsSrcDir + scriptName;
			Map results = [:];
			boolean rslt =
					OS.execSysCommand( grailsApplication.config.mat.R.executable,
									   [ scriptSpec, analysisDir ],
									   results, 2 );
			Date end = new Date( );
			println( "runFocusedArrayAnalysis took " + (end.getTime() - start.getTime()) + "ms" );
			if ( results.error )
			{
				PrintWriter errorsFile =
						new PrintWriter( analysisDir + "analysis.log" );
				errorsFile.write( results.error );
				errorsFile.flush( );
				errorsFile.close( );
			}
			if ( results.output )
			{
				PrintWriter messagesFile =
						new PrintWriter( analysisDir + "analysis.stdout" );
				messagesFile.write( results.output );
				messagesFile.flush( );
				messagesFile.close( );
			}
			if ( rslt )
			{
				Map csvIds = [ "analysisId": analysis.id,
							   "sampleSetId": sampleSet.id ];
				def pattern = ~/.*\.csv/;
				new File( resultsDir ).eachFileMatch(
					groovy.io.FileType.FILES, pattern,
					{ File resultFile ->
						mongoDataService.storeCsv( resultFile, true,
												   "analysis_results", csvIds );
					} );

				PrintWriter writer =
						new PrintWriter( analysisDir + "scriptComplete" );
				writer.println( "Finished Running Analysis Script" );
				writer.flush( );
				writer.close( );
			}
			else
			{
				println( "Error executing R scripts: " + results.error );
			}
		// }
	}

def delete = {
	def MATAnalysisGroupSetInstance = MATAnalysisGroupSet.get(params.id)
	if (MATAnalysisGroupSetInstance) {
		try {
			MATAnalysisGroupSetInstance.delete(flush: true)
			flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), params.id])}"
			redirect(action: "list")
		}
		catch (org.springframework.dao.DataIntegrityViolationException e) {
			flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), params.id])}"
			redirect(action: "show", id: params.id)
		}
	}
	else {
		flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATAnalysisGroupSet.label', default: 'MATAnalysisGroupSet'), params.id])}"
		redirect(action: "list")
	}
}
}
