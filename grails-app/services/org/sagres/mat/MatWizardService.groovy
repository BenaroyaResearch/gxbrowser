package org.sagres.mat

import au.com.bytecode.opencsv.CSVReader
import groovy.sql.Sql
import common.chipInfo.ChipType

class MatWizardService {

	static transactional = true
	def dataSource
	def matConfigService
	def matDataService
	def mailService
	def grailsApplication
	def fileSep = System.getProperty("file.separator")


	def serviceMethod() {

	}


	def createWizardArchive(def wizardId) {
		def wDir = matConfigService.getMATWorkDirectory() + "/MATWizard/"
		File wDirF = new File(wDir)
		if (!wDirF.exists()) {
			wDirF.mkdir()
		}
		def wizDir = wDir + wizardId
		File wizDirF = new File(wizDir)
		if (!wizDirF.exists()) {
			wizDirF.mkdir()
		}
	}


	def saveFile(def matWizardId, def uploadedFile, def fileName) {
		createWizardArchive(matWizardId)
		def fullFilePath = matConfigService.getMATWorkDirectory() + "/MATWizard/" + matWizardId + "/" + fileName
		uploadedFile.transferTo(new File(fullFilePath))
		return fullFilePath
	}

	def getMATReadyChips(String manufacturer) {
		def chipTypes = [:]
		Sql db = Sql.newInstance(dataSource)
		//select ct.id, ct.active, ct.name, ct.chip_data_id, cd.manufacturer, cd.name, cd.model from chip_type ct, chip_data cd where ct.chip_data_id = cd.id and model is not null;
		String sqlS = "select ct.id, ct.name,  " +
			"cd.manufacturer, cd.name, ct.module_version_id from chip_type ct, chip_data cd " +
			"where ct.chip_data_id = cd.id and model is not null and ct.active >= 0 " +
			"and ct.module_version_id > 0 and cd.manufacturer = '${manufacturer}'"
		db.eachRow(sqlS.toString()) {
			def parms = [chipTypeName: it.getAt(1), manufacturer: it.getAt(2), chipName: it.getAt(3), versionId: it.getAt(4)]
			chipTypes.put(it.getAt(0), parms)
		}
		db.close()
		return chipTypes
	}



	def getFirstColumnName(def filePath) {
		def sampleIdColName = null
		new File(filePath).eachCsvLine { tokens ->
			if (!sampleIdColName) {
				sampleIdColName = tokens[0]
			}
		}
		return sampleIdColName
	}

	def loadAnnotationFile(def filePath) {
		def sampleInfo = [:]
		def sampleIdColName = getFirstColumnName(filePath)
		new File(filePath).toCsvMapReader().each {
			sampleInfo.put(it."$sampleIdColName", it)
		}
		return sampleInfo
	}

	//return column Name, map of group name and count
	def getGroupInfo(def wId) {
		MATWizard matWizard = MATWizard.get(wId)
		def samples = getSamples(wId)
		def filePath = matConfigService.getMATWorkDirectory() + "/MATWizard/" + wId + "/formattedAnnotations.csv"
		if (matWizard.annotationFileRequired) {
			filePath = matConfigService.getMATWorkDirectory() + "/MATWizard/" + wId + "/uploadedAnnotationFile"
		}
		def groupInfo = [:]
		def groupNames = []
		new File(filePath).eachCsvLine { tokens ->
			if (groupInfo.size() == 0) {
				(0..tokens.size() - 1).each {
					groupInfo.put(tokens[it], [:])
					groupNames.add(tokens[it])
				}
			} else {
				try {
				(1..tokens.size() - 1).each {
					def group = groupInfo.get(groupNames[it])
					if (group.containsKey(tokens[it])) {
						def total = group.get(tokens[it])
						total += 1
						group.put(tokens[it], total)
					} else {
						group.put(tokens[it], 1)
					}
				}
				} catch (Exception ex) {
					println "Exception parsing annotation file wizard id: ${wId}: ${ex.toString()}"
				}
			}
		}
		groupInfo.remove(groupNames[0])
		//Time to test group Info to make sure it matches

		return groupInfo
	}

	def getAllSamplesInOneGroup(def annotationFilePath) {
		def samples = [:]
		def skipFirstLine = true
		new File(annotationFilePath).eachCsvLine { tokens ->
			if (skipFirstLine) {
				skipFirstLine = false
			} else {
				samples.put(tokens[0], 'Ignore')
			}
		}
		return samples
	}


	def validateAnnotationFile(Long wizardId) {
		def valid = true
		if (matWizard.annotationFileRequired) {
			try {
				def groupInfo =  getGroupInfo(wizardId)
			} catch (Exception ex) {
				println "Exception validating annotation file for WizardId: ${wizardId}"
				valid =false
			}
		}
		return valid
	}

	def getSamples(def wizardId, def reloadSamples = false) {
		MATWizard matWizard = MATWizard.get(wizardId)
		if (matWizard.samples.size() > 0 && !reloadSamples ) {
			return matWizard.samples
		}
		def groupName = matWizard.groupSetName
		def filePath = matConfigService.getMATWorkDirectory() + "/MATWizard/" + wizardId + "/formattedAnnotations.csv"
		def defaultFilePath = filePath
		if (matWizard.annotationFileRequired && groupName != null &&  !groupName.equalsIgnoreCase("none") ) {
			filePath = matConfigService.getMATWorkDirectory() + "/MATWizard/" + wizardId + "/uploadedAnnotationFile"
		} else {
			return getAllSamplesInOneGroup(filePath)
		}

		def defaultSamples = getAllSamplesInOneGroup(defaultFilePath)
		def samples = [:]
		def groupColumn
		new File(filePath).eachCsvLine { tokens ->
			if (!groupColumn) {
				(0..tokens.size() - 1).each {
					if (tokens[it].toString().equalsIgnoreCase(groupName)) {
						groupColumn = it
					}
				}
			} else {
				if ( defaultSamples.containsKey(tokens[0])) {
					//theres a match
					samples.put(tokens[0], tokens[groupColumn])
				}  else {
					def newId = tokens[0].toString().substring(0,tokens[0].toString().lastIndexOf("."))
					//println "old id: ${tokens[0]} - new id: $newId"
					//todo make failure behave gracefully
					samples.put(newId, tokens[groupColumn])
				}
			}
		}
		return samples
	}

	def addSampleToMATWizard(def wizardId, MATWizardSample sample) {
		MATWizard wizard = MATWizard.get(wizardId)
		//verify the group the sample is in exists  - if necessary create
		def sampleGroups = wizard.sampleGroups
		def existingGroup = false

		sampleGroups.each {
			if (it.samples.contains(sample.sampleId)) {
				it.samples.remove(sample.sampleId)
			}
			if (it.groupName.equals(sample.groupName)) {
				it.samples.add(sample.sampleId)
				existingGroup = true
			}
		}
		if (!existingGroup) {
			MATWizardSampleGroup group = new MATWizardSampleGroup(groupName: sample.groupName)
			group.addToSamples(sample.sampleId)
			wizard.addToSampleGroups(group)
		}
		def existingSample
		wizard.samples.each {
			if (it.sampleId.equals(sample.sampleId)) {
				existingSample = it
			}
		}
		if (existingSample) {
			wizard.samples.remove(existingSample)
		}
		wizard.samples.add(sample)
		return wizard.save()
	}


	def generateDesignFile(def matWizardId, def designFilePath) {
		MATWizard matWizard = MATWizard.get(matWizardId)
		def caseLabel = matWizard.caseLabel
		def controlLabel = matWizard.controlLabel
		def designFile = new FileOutputStream(designFilePath)
		designFile << "barcode,sample_id,group,group_label\n"
		matWizard.sampleGroups.each { MATWizardSampleGroup group ->
			if (MATWizardSampleGroup.groupTypes[0].equals( group.groupType)) {
				//CASE
				group.samples.each { String sampleId ->
					designFile << "${sampleId}, ${sampleId},1,${caseLabel}\n"
				}
			}
			if (MATWizardSampleGroup.groupTypes[1].equals( group.groupType)) {
				//CONTROL
				group.samples.each { String sampleId ->
					designFile << "${sampleId}, ${sampleId},0,${controlLabel}\n"
				}
			}
		}
	}


	def cleanupDatasetName(String datasetName) {
		StringBuilder sb = new StringBuilder()
		for (char c: datasetName?.toCharArray()) {
			if (((c >= 'a' && c <='z') || (c >= 'A' && c <= 'Z')) || ((c >= '0' && c <= '9') || (c == ' '))) {
				sb.append(c)
			}
		}
		return sb.toString()
	}

	def startModuleAnalysis(def matWizardId) {
		MATWizard matWizard = MATWizard.get(matWizardId)
		def formattedExpressionFile = matConfigService.getMATWorkDirectory() + "/MATWizard/" + matWizard.id + "/formattedExpression.tsv"
		def annotationFileFromImport = matConfigService.getMATWorkDirectory() + "/MATWizard/" + matWizard.id + "/formattedAnnotations.csv"
		//write out design File
		def designFile = matConfigService.getMATWorkDirectory() + "/MATWizard/" + matWizard.id + "/designFile.csv"

		generateDesignFile(matWizardId, designFile)
		Analysis analysis = new Analysis()
    analysis.displayName = matWizard.displayName
    analysis.datasetName = cleanupDatasetName(matWizard.signalData)
		analysis.user = matWizard.userName
		analysis.runDate = new Date()
		analysis.setExpressionDataFile("formattedExpression.tsv")
		analysis.setDesignDataFile("designFile.csv")
		analysis.chipId = matWizard.chipId
		ChipType ct = ChipType.get(matWizard.chipId)
		Version version = Version.get(ct.moduleVersionId)
		analysis.setModuleFunctionFile(version.getFunctionFileName())
		analysis.setModuleVersionFile(version.getVersionFileName())
		analysis.moduleVersion = version.id
    // set parameters
    analysis.deltaType = matWizard.method
    if (matWizard.method == "fold") {
      analysis.foldCut = matWizard.foldCut
    } else if (matWizard.method == "zscore") {
      analysis.zscoreCut = matWizard.zscoreCut
    }
    analysis.fdr = matWizard.fdr
    analysis.multipleTestingCorrection = matWizard.mts
    analysis.deltaCut = matWizard.diffThreshold

		def saveAnalysis = analysis.save(flush: true)
		matWizard.analysisId = analysis.id
		matWizard.save(flush:true)
		String dataDir = "${matConfigService.MATWorkDirectory}${fileSep}${analysis.id}${fileSep}Data${fileSep}${analysis.datasetName.replaceAll(" ", "_")}"
		def matExpFile = dataDir + "/formattedExpression.tsv"
		def matDesFile = dataDir + "/designFile.csv"
		matDataService.createAnalysisFolder(analysis.id, analysis.datasetName)

		matDataService.writeOutDataFiles(analysis.id, version)
		matDataService.writeOutScriptFiles(analysis.id)
		matDataService.copyFile(formattedExpressionFile, matExpFile)
		matDataService.copyFile(designFile, matDesFile )

		matDataService.writeParameterFile(analysis)
		def afterScriptCompletion = {
		//	def mailMatWizard = matWizard.get(matWizard.id) //reloading in case there is an added email address
			def url = grailsApplication.config.grails.serverURL + "/analysis/show/${analysis.id}"
			runAsync {
				try {
					matWizard.wizardStatus = MATWizard.wizardAvailableStatus[2]
					matWizard.save(flush:true)
					def mailAddress = matWizard.userEmail
				if (mailAddress != null && mailAddress.length() > 5 && grailsApplication.config.send.email.on) {
				def message = """\
					Your Module Analysis run ${analysis.datasetName} is finished.
					You can view your results here it here: (${url}).
					For help interpreting the results, please see (module wiki).
				"""

				println "trying to mail link ${url} to ${mailAddress}"
					mailService.sendMail {
						to "${mailAddress}"
						from "Chaussabel Lab BioIT <softdevteam@benaroyaresearch.org>"
        		subject "Your Module Analysis run ${analysis.datasetName} is complete"
        		html "${message}"
					}
					matWizard.wizardStatus = matWizard.wizardAvailableStatus[3]

				} else {
					matWizard.wizardStatus = matWizard.wizardAvailableStatus[4]
				}
				matWizard.save(flush: true)
			} catch (Exception ex) {
					println "Exception generating email: ${ex.toString()}"
				}
			}
		}
		matDataService.runScript(matDataService.createCommandLine(analysis.id),analysis.id, afterScriptCompletion)

	}

}
