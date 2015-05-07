package org.sagres.mat

import groovy.sql.Sql
import org.sagres.rankList.RankListParams
import org.sagres.rankList.RankListComparison
import static groovy.io.FileType.FILES
import groovy.sql.GroovyRowResult
import common.chipInfo.ChipType
import org.sagres.importer.TextTableSeparator;
import org.sagres.importer.TextTable;

class Dm3Service {

	static transactional = true

	def serviceMethod() {
	}

	def matConfigService
	def importService
	def dataSource
	def fileSep = System.getProperty("file.separator")
	def springSecurityService //injected
	def grailsApplication
	def sampleSetService
	def mailService



	def lastUpdated = new Date()

	long sampleSetRefreshLength = 0

	def availableSampleSets = [:]

	def versionIdForSampleSet(def sampleSetId) {
		Sql db = Sql.newInstance(dataSource)
		def query =
		"SELECT a.module_version_id FROM dataset_group_set dgs, dataset_group dg, dataset_group_detail dgd, array_data ad, chip_type a, chips_loaded b  " +
			"WHERE dgs.sample_set_id = '" + sampleSetId + "'" +
			" AND dg.group_set_id = dgs.id" +
			" AND dgd.group_id = dg.id" +
			" AND ad.id = dgd.sample_id" +
			" AND b.id = ad.chip_id" +
			" AND b.chip_type_id = a.id" +
			" LIMIT 1"
		def result = (GroovyRowResult) db.firstRow(query)
		def versionId = result ? result.get("module_version_id") : null
		db.close()
		return versionId
	}

	def getVersionIdForChipType(def chipId) {
		def ChipType ct = ChipType.get(chipId)
		return ct.moduleVersionId
	}

 def getGen3VersionIdForChipType(def chipId) {
		def ChipType ct = ChipType.get(chipId)
		return ct.moduleGen3MappingId
	}


	def getChipIdForSampleSet(def sampleSetId) {
		Sql db = Sql.newInstance(dataSource)
		def query =
		"SELECT a.id FROM dataset_group_set dgs, dataset_group dg, dataset_group_detail dgd, array_data ad, chip_type a, chips_loaded b  " +
			"WHERE dgs.sample_set_id = '" + sampleSetId + "'" +
			" AND dg.group_set_id = dgs.id" +
			" AND dgd.group_id = dg.id" +
			" AND ad.id = dgd.sample_id" +
			" AND b.id = ad.chip_id" +
			" AND b.chip_type_id = a.id" +
			" LIMIT 1"
		def result = (GroovyRowResult) db.firstRow(query)
		def chipId = result ? result.get("id") : null
		db.close()
		return chipId
	}





	def getChipTypesAvailableForAnalysis() {
		Sql db = Sql.newInstance(dataSource)

		def chips = [:]
		def query = "SELECT  ct.id, ct.name, cd.manufacturer from chip_type ct, chip_data cd where " +
			"ct.chip_data_id = cd.id and ct.module_version_id is not null and ct.module_version_id > 0 " +
			"order by ct.id"
		db.eachRow(query) {
			chips.put(it.id, "${it.manufacturer}: ${it.name}")
		}
		db.close()
		return chips
	}


	def getAvailableSampleSets() {
		def db = new Sql(dataSource)
		def loggedIn = springSecurityService.isLoggedIn()
		def setsRequiringLogin = grailsApplication.config.genomic_datasource_names_requiring_login

		def sampleSets = [:]
		sampleSets.put('-1', 'Select a sample set')
		db.eachRow("select id, name from sample_set") {
			def versionId = versionIdForSampleSet(it.id)
			if (versionId != -1 && versionId != null) {
				if (loggedIn || !setsRequiringLogin.containsKey(sampleSetService.getDatasetType(it.id)?.name)) {
					sampleSets.put(it.id, it.name)
				}
			}
		}
		return sampleSets
	}

	def getSampleSetName(def sampleId) {
		def db = new Sql(dataSource)
		def sqlS = "select name from sample_set where id=${sampleId}".toString()
		def name = "Unknown"
		db.eachRow(sqlS) {
			name = it.getProperty('name')
			println "Name = ${name}"
		}
		return name.replaceAll(" ", "_")

	}

	//Return true if the request returns ok
	def requestExpressionFile(def sampleSetId, def fileName) {
		String fileRequest = "${grailsApplication.config.mat.dm3expressionfileservice}?sampleSetId=${sampleSetId}&tsvFileSpec=${fileName}"
		try {
			def result = fileRequest.toURL().text
			return result.contains("OK")
		} catch (Exception fnfe) {
			//Possible connection held too long, errored out - check if file exists
			println "Exception requesting expression file: ${fnfe.toString()}"
			File doesExist = new File(fileName)
			return doesExist.exists()
		}
	}

	def getAvailableGroupSets(def sampleSetId) {
		def db = new Sql(dataSource)
		def groupSets = [:]
		db.eachRow("select  distinct a.name, b.group_set_id from dataset_group_set a, dataset_group b "
			+ "where sample_set_id = " + sampleSetId
			+ " and a.id = b.group_set_id") {
			groupSets.put(it.getAt(1), it.getAt(0))
		}
		return groupSets
	}

	def getGroupsInSet(def groupSetId) {
		def db = new Sql(dataSource)
		def groups = [:]
		db.eachRow("select b.id, b.name, b.display_order from dataset_group b where b.group_set_id = " + groupSetId + " order by b.display_order") {
			groups.put(it.getAt(1), it.getAt(2))
		}
		return groups
	}

	def getGroupsInSetForRanking(def groupSetId) {
		def db = new Sql(dataSource)
		def groups = [:]
		db.eachRow("select b.id, b.name, b.display_order from dataset_group b, dataset_group_detail c where c.group_id = b.id and b.group_set_id = " + groupSetId + " group by b.name order by b.display_order") {
			groups.put(it.getAt(1), it.getAt(0))
		}
		return groups
	}

	def createNewMATAnalysisGroupSet(def analysisId, def groupSetId) {
		def db = new Sql(dataSource)
		MATAnalysisGroupSet mags = new MATAnalysisGroupSet();
		db.eachRow("select name from dataset_group_set a where a.id = ${groupSetId}") {
			mags.groupName = it.getProperty('name')
			mags.analysisId = analysisId
			mags.groupId = Long.parseLong(groupSetId)
		}
		mags.save()
		db.eachRow("select id, name, display_order from dataset_group b where b.group_set_id = " + groupSetId) {
			MATAnalysisGroup mag = new MATAnalysisGroup(groupSetId: groupSetId, groupId: it.getProperty('id'), caseControlGroup: -1l, groupSetName: it.getProperty('name'), groupOrder: it.getProperty('display_order'))
			mags.addToGroups(mag)
			mags.save()
		}
		return mags;
	}

	def getChipTable(long chipId) {
		Sql db = new Sql(dataSource)
		def query =
		"SELECT probe_list_table from chipe_type where id=" + chipId
		def result = (GroovyRowResult) db.firstRow(query.toString())
		def probeATable = result ? result.get("probe_list_table") : null
		db.close()
		return probeATable

	}


	def getChipInfo(def chipId) {

		Sql db = new Sql(dataSource)

		String sqlS = "select ct.id, ct.name,  " +
			"cd.manufacturer, cd.name, ct.module_version_id from chip_type ct, chip_data cd " +
			"where ct.chip_data_id = cd.id and model is not null and ct.active >= 0 and ct.id = '" + chipId + "'"
		def parms
		db.eachRow(sqlS.toString()) {
			parms = [chipTypeName: it.getAt(1), manufacturer: it.getAt(2), chipName: it.getAt(3), versionId: it.getAt(4)]
		}
		db.close()
		return parms
	}



	def generateProbeToGeneExpressionMapping(def analysisId) {

		def analysis = Analysis.get(analysisId)
		String dataDir = "${matConfigService.MATWorkDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}ModuleData${fileSep}"
		File dDir = new File(dataDir)
		if (!dDir.exists()) {
			dDir.mkdir()
		}
		def prefix = ""
		def probeToSymbol = new FileOutputStream("${dataDir}${fileSep}ProbeToSymbol.csv")

		def chipId = analysis.chipId
		try {
			def chipInfo = getChipInfo(chipId)
			if ("Illumina".equalsIgnoreCase(chipInfo?.manufacturer)) {
				prefix = "ILMN_"
			}

		} catch (Exception ex) {
			println "Exception getting manufacturer for analysis:${analysisId}: ${ex.toString()}"
		}
		Sql db = new Sql(dataSource)
		def query
		try {
			query = "select probe_list_column,probe_list_table, symbol_column from chip_type where id=" + chipId
			def result = (GroovyRowResult) db.firstRow(query.toString())
			def probeTable = result.get("probe_list_table")
			def probeListColumn = result.get("probe_list_column")

			def symbolColumn = result.get("symbol_column")
			query = "select ${probeListColumn},${symbolColumn} from ${probeTable}".toString()
			probeToSymbol << "probe_id,gene_symbol\n"
			db.eachRow(query) {
				def id1 = it.getAt(0)
				if (id1 != null && id1.length() > 0) {
					probeToSymbol << "${prefix}${id1},${it.getAt(1)}\n"
				}
			}

		} catch (Exception ex) {
			println "Error generating probe to symbol map:\nquery: ${query}\n${ex.toString()}"
		}
		db.close()
	}



	def generateDesignFile(def fileName, def groupSetId, def datasetName, def analysisId) {
		String dataDir = "${matConfigService.MATWorkDirectory}${fileSep}${analysisId}${fileSep}Data${fileSep}${datasetName.replaceAll(" ", "_")}"
		File dDir = new File(dataDir)
		if (!dDir.exists()) {
			dDir.mkdir()
		}
		def matAnalysisGroupSet = MATAnalysisGroupSet.findById(groupSetId)

		def db = new Sql(dataSource)
		def design = new FileOutputStream("${dataDir}${fileSep}${fileName}")
		design << "barcode,sample_id,group,group_label\n"
		matAnalysisGroupSet.groups.each {	MATAnalysisGroup mag ->
			if (mag.caseControlGroup >= 0) {

				def sqlS = "select b.id, b.name, d.id, c.sample_id from dataset_group_set a, dataset_group b, dataset_group_detail c, array_data d " +
					"where a.id = " + matAnalysisGroupSet.groupId + " and a.id = b.group_set_id " +
					"and b.id =" + mag.groupId + " and b.id = c.group_id " +
					"and c.sample_id=d.id".toString()
				db.eachRow(sqlS) {
					def barcode = "X" + it.getAt(2)
					design << "${barcode}, ${it.getAt(3)}, ${mag.caseControlGroup}, ${mag.groupLabel}\n"
				}
			}
		}
		/**
		 db.eachRow("SELECT c.barcode, c.id, b.id, a.name "
		 + "from dataset_group a, dataset_group_detail b, array_data c "
		 + "where a.group_set_id = " + groupSetId
		 + " and a.id = b.group_id "
		 + " and b.sample_id = c.id") {design <<"${it.getAt(0)}, A, ${it.getAt(1)}, ${it.getAt(2)}, ${it.getAt(3)}\n"}*   */
		design.close()
	}

	void generateFocusedArrayDesignFile( MATAnalysisGroupSet matGroupSet,
										 String fileSpec,
										 Sql sql = null )
	{
		sql = sql ?: Sql.newInstance( dataSource );

		PrintWriter writer = new PrintWriter( fileSpec );
		TextTableSeparator sep = TextTableSeparator.CSV;
		List< String > fields =
				[ "sampleID", "sampleName", "groupName", "groupID" ];
		writer.println( TextTable.joinRow( fields, sep, true ) );
		for ( MATAnalysisGroup matGroup : matGroupSet.groups )
		{
			if ( matGroup.caseControlGroup >= 0 )
			{
				String query =
						"SELECT ad.id AS `sample_id`," +
						"       ad.sample_name AS `sample_name`" +
						"  FROM array_data AS ad," +
						"       dataset_group_detail AS gd" +
						"  WHERE ad.id=gd.sample_id" +
						"    AND ad.sample_type='NonRef'" +
						"    AND gd.group_id="+ matGroup.groupId;
				List< GroovyRowResult > rows = sql.rows( query );
				for ( GroovyRowResult row : rows )
				{
					fields = [ "S" + row.sample_id, row.sample_name,
							   matGroup.groupLabel, matGroup.caseControlGroup ];
					writer.println( TextTable.joinRow( fields, sep, true ) );
				}
			}
		}
		writer.flush( );
		writer.close( );
	}

	def getGroupNameFromGroupId(def groupId) {
		def db = new Sql(dataSource)
		String sqlS = "select name from dataset_group where id='" + groupId + "'"
		def res = db.firstRow(sqlS)
		return res.getAt(0)
	}


	def generateRankListDesignFile(def rankListId) {
		def rankList = RankListParams.findById(rankListId)
		String designFile = "${matConfigService.MATWorkDirectory}/ranklist/${rankListId}/Data/design.csv"
		def design = new FileOutputStream(designFile)
		design << "barcode,group_label\n"
		def db = new Sql(dataSource)
		def sqlS = ("SELECT a.sample_id, b.name FROM dataset_group_detail a, dataset_group b WHERE a.group_id = b.id AND b.group_set_id='" + rankList.sampleSetGroupSetId + "'").toString()
        println "Generating Rank List {$rankListId} SQL: " + sqlS
		db.eachRow(sqlS) {
            String barcode = "X" + it.getAt(0).toString()
            String group = it.getAt(1).toString()
			//design << "${it.getAt(0)},${it.getAt(1)}\n"
            design << "${barcode},${group}\n"
		}
		design.close()
	}

	def generateRankListComparisonFile(def rankListId) {
		def rankList = RankListParams.findById(rankListId)
		String comparisonFile = "${matConfigService.MATWorkDirectory}/ranklist/${rankListId}/Data/comparison.csv"
		def comp = new FileOutputStream(comparisonFile)
		comp << "cases,controls\n"
		rankList.comparisons.each {RankListComparison rlc ->
			comp << "${rlc.groupOneName},${rlc.groupTwoName}\n"
		}
		comp.close()
	}

	def generateRankListCommandLine(def rankListId) {
		def projDir = "${matConfigService.getMATWorkDirectory()}/ranklist/${rankListId}/"
		def script = "${matConfigService.getRScriptLocation()} ${projDir}${fileSep}scripts${fileSep}callRanking.R ${projDir}"
		return script
	}



	def runRankList(def rankListParamsId, def user) {
		def rankListParams = RankListParams.findById(rankListParamsId)
		
		//Generate thread, spin off
		runAsync {
			try {

				//Generate Directory Structure
				// def rankListComparison = RankListComparison.findbyId(rankListComparisonId)
				def workDir = matConfigService.getMATWorkDirectory() + "/ranklist/"
				File workDirF = new File(workDir)
				if (!workDirF.exists()) {
					workDirF.mkdir()
				}
				def jobDir = workDir + "${rankListParamsId}"
				File jobDirF = new File(jobDir)
				if (!jobDirF.exists()) {
					jobDirF.mkdir()
				}
				def datadir = jobDir + "/Data/"
				File dataDirF = new File(datadir)
				if (!dataDirF.exists()) {
					dataDirF.mkdir()
				}
				def resDir = jobDir + "/Results/"
				File resDirF = new File(resDir)
				if (!resDirF.exists()) {
					resDirF.mkdir()
				}
				def scriptDir = jobDir + "/scripts/"
				File scriptDirF = new File(scriptDir)
				if (!scriptDirF.exists()) {
					scriptDirF.mkdir()
				}
				//Copy R Files
				String workingDirectory = matConfigService.getMATWorkDirectory()
				File sourceScriptDir = new File("${workingDirectory}${fileSep}scripts${fileSep}ModuleAnalysis")
				def p = ~/.*\.R/
				sourceScriptDir.eachFileMatch FILES, p, { File source ->
					new File("${workingDirectory}${fileSep}ranklist${fileSep}${rankListParamsId}${fileSep}scripts${fileSep}${source.name}") << source.bytes
				}
				//Generate Design File
				generateRankListDesignFile(rankListParamsId)
				generateRankListComparisonFile(rankListParamsId)
				//Generate Data File
				def expFile = datadir + "expression.tsv"
				requestExpressionFile(rankListParams.sampleSetId, expFile)
				def cmdLine = generateRankListCommandLine(rankListParamsId)
				runRankListScript(cmdLine, rankListParamsId)
				/** 1/9/2012 No longer needed - pass in samplesetId and groupSetID to the importer
				 def prepend = "ds${rankList.sampleSetId}_"
				 resDirF.eachFileMatch FILES, p, { File source ->
				 source.renameTo("${source.parentFile.absolutePath}/${prepend}${source.name}")}* */
				//Load Rank lists
				p = ~/.*\.csv/
				importService.setSqlDataSource(dataSource)
				resDirF.eachFileMatch FILES, p, { File source ->
					importService.importStdRankList(source.absolutePath, rankListParams.sampleSetId, rankListParams.sampleSetGroupSetId)
				}
			} catch (Exception ex) {
				println "Exception running rankList ${ex.toString()}"
			}
			
			try {

				String sender = grailsApplication.config.importer.defaultEMailFrom
				String emailAdd = user?.email.toString()
				String userName = user?.username.toString().capitalize()

				String emailText = 	 "${userName}, your ranklist requests have completed. You can view the results at " +
				"${grailsApplication.config.grails.serverURL}/geneBrowser/show/${rankListParams.sampleSetId}."
				println( "Dm3Service.sendMail: to=${emailAdd} from=${sender} subject='Ranklist run complete'" );
  			    if (emailAdd != null && grailsApplication.config.send.email.on) {
					  mailService.sendMail {
						   from "${sender}"
						     to "${emailAdd}"
					  	subject "Ranklist run complete"
						   html "${emailText}"
					  }
			    } else {
				  println "No User to send notification email for ranklists on ssid${rankListParams.sampleSetId}"
				}
			} catch (Exception ex) {
				println "Exception sending email about rankList ${ex.toString()}"
			}
		}
	}

	def runRankListScript(def cmdLine, def rankListId) {
		def rankList = RankListParams.findById(rankListId)
		def projDir = "${matConfigService.getMATWorkDirectory()}/ranklist/${rankListId}/"
		File scriptF = new File("${projDir}runScript")
		scriptF << cmdLine
		String logFileErr = "${projDir}analysis.log"
		String logFileStd = "${projDir}analysis.stdout"
		def fose = new FileOutputStream(logFileErr)
		def foss = new FileOutputStream(logFileStd)
		Process proc = cmdLine.execute()
		fose << proc.in
		fose << proc.err
		foss << proc.out
		proc.waitFor()
		foss << "\n----------------------------------------------------"
	}


}
