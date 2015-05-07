package org.sagres.mat

import grails.converters.JSON
import common.chipInfo.ChipType
import common.SecUser
import org.codehaus.groovy.grails.commons.DefaultGrailsApplication

class MATWizardController {

	def debug = true

	static allowedMethods = [save: "POST", update: "POST", delete: "POST"]
	def matWizardService
	def matConfigService
	def importService
	def dataSource
	def springSecurityService
  def mongoDataService



	def wizardStatus = {
		MATWizard wizardInstance = MATWizard.get(params.id)
		def wizardStatus = ['status': wizardInstance.wizardStatus]
		render wizardStatus as JSON
	}

	def fileStatus = {

		MATWizard wizardInstance = MATWizard.get(params.id)
		def fileStatus = ['status': wizardInstance.fileProcessState]
		render fileStatus as JSON
	}



	def transferSamples = {
		//println "wizardId: ${params.id}, sampleId:${params.samples}, receiveGroup:${params.receiveGroup}, fromGroup: ${params.fromGroup.substring(5)}"
		MATWizard wizardInstance = MATWizard.get(params.id)
		MATWizardSample sample = null
		def receiveGroupId = Long.parseLong(params.receiveGroup.substring(5))
		def receiveGroup = MATWizardSampleGroup.get(receiveGroupId)
		sample = wizardInstance.samples.find {w -> w.sampleId.equals(params.samples)}
		sample.setGroupName(receiveGroup.groupName)
		matWizardService.addSampleToMATWizard(params.id, sample)
		render params.id
	}

	//0
	def intro = {


	}

	//1
	def uploadSignalData = {
		def fileFormats = MATWizardFileFormat.list()
		def wizardInstance = new MATWizard(params)
		[fileFormats: fileFormats, wizardInstance: wizardInstance]
	}

	def saveUploadData = {
		 println "uploading via MATWizard"
		//VALIDATE
		try {
			def MATWizardInstance = new MATWizard(params)
			def expressionFile = request.getFile("signalDataFile")
			def annotationFile = request.getFile("annotationFile")
			def user = springSecurityService.currentUser
			MATWizardInstance.userName = user?.username
			def passing = true
			def importSuccess = false
			def saveSuccess = false
			def expressionOutFile
			def fileType = params.fileType
			def chipId = params.chipType

            Map fileFormat = importService.getExpressionFileFormat( fileType );
			if (fileFormat.requiresChipType &&
                ((chipId == null) || (chipId.equalsIgnoreCase("-1")))) {
				passing = false
				flash.message = "For the ${params.fileType}, you need to select a chip type"
			}
			if (expressionFile == null || expressionFile.empty) {
				flash.message = "Please select the signal file to analyze"
				passing = false
			}
			//Check annotation
			if (MATWizardInstance.getAnnotationFileRequired()) {
				if (annotationFile == null || annotationFile.empty) {
					passing = false
					MATWizardInstance.annotationFile = null
					flash.message = "Please select the annotation file to upload"
				} else {
					MATWizardInstance.annotationFile = annotationFile.originalFilename
					MATWizardInstance.fileProcessState = MATWizard.fileProcessStates[1]
				}
			}
			//println "passing: ${passing}"
			if (passing) {
				MATWizardInstance.signalData = expressionFile.originalFilename
				MATWizardInstance.clearErrors()
				flash.clear()
				saveSuccess = MATWizardInstance.save(flush: true)
				MATWizardInstance.clearErrors()
				expressionOutFile = matWizardService.saveFile(MATWizardInstance.id, expressionFile, "uploadedExpressionFile")
				if (MATWizardInstance.annotationFileRequired) {
					matWizardService.saveFile(MATWizardInstance.id, annotationFile, "uploadedAnnotationFile")
				}
			}

			//IMPORT
			if (saveSuccess) {
				println "save success"
				runAsync {

					if (debug) println "Time: ${new Date()}"
					def res
					try {
						importService.setSqlDataSource(dataSource)
						ChipType ct
						if (chipId)
                        {
							ct = ChipType.get(chipId)
						}
						def formattedExpressionFile = matConfigService.getMATWorkDirectory() + "/MATWizard/" + MATWizardInstance.id + "/formattedExpression.tsv"
						def annotationFileFromImport = matConfigService.getMATWorkDirectory() + "/MATWizard/" + MATWizardInstance.id + "/formattedAnnotations.csv"
                        Map importParams =
                                [
                                    type: fileType,
                                    outDataFileSpec: formattedExpressionFile,
                                    outSamplesFileSpec: annotationFileFromImport,
                                ];
                        if ( chipId )
                        {
                            importParams.chipTypeId = chipId;
                        }
                        res = importService.validateAndExportExpressionDataFiles( [ expressionOutFile ], importParams );
						importSuccess = res.success
						println "ImportSucces : ${importSuccess}"
						res.properties.each { key, value ->
							println "Result Properties: $key : $value"
						}
						if (res.chipType != null) {
							 ct = res.chipType
							MATWizardInstance.chipId = ct.id
							println "Detected Chip: ${ct.name}"


						}

						if (importSuccess) {

							MATWizardInstance.fileProcessState = MATWizard.fileProcessStates[2]
							MATWizardInstance.sampleCount = res.numSamples
							MATWizardInstance.probeCount = res.numProbes
							saveSuccess = MATWizardInstance.save(flush: true)
						} else {
							MATWizardInstance.fileProcessState = MATWizard.fileProcessStates[3]
							saveSuccess = MATWizardInstance.save(flush: true)
						}
					} catch (Exception ex) {
						println "Error importing expressionFile ${expressionOutFile}: ${ex.toString()}"
						sleep(2000) //Prevent locking problems
						MATWizardInstance.fileProcessState = MATWizard.fileProcessStates[3]
						saveSuccess = MATWizardInstance.save(flush: true)
					}
					if (debug) println "ProcessTime: ${new Date()}"
				}
			}

			if (saveSuccess) {
				forward(action: "verifySignalData", id: MATWizardInstance.id, params: params)
			} else {
				MATWizardInstance.errors.each {
					println "Unable to save matWizard ${MATWizardInstance?.id} because ${it}"
				}
				forward(action: "uploadSignalData", id: MATWizardInstance.id)
			}
		} catch (Exception ex) {
			println "Error uploading signal Data : ${ex.toString()}"
			ex.printStackTrace()

			forward(action: "uploadSignalData")

		}
	}


	def getAvailableModuleGenerations(def wizardId) {
		//TODO - have a temporary method to simulate 1 or multiple versions   will need to replace with a real working one eventually
		def moduleGenerations = [1: "Generation 1", 2: "Generation 2", 3: "Generation 3"]
		def random = new Random()
		def nextR = random.nextInt(3)
		def gens = [:]
		(1..(nextR + 1)).each {
			gens.put(it, moduleGenerations.get(it))
		}
		return gens
	}

	//2
	def verifySignalData = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		[matWizardInstance: matWizardInstance]
	}

	//2.5
	def signalData = {
		//Generate Sample Information
		MATWizard matWizardInstance = MATWizard.get(params.id)
		def importSuccess = matWizardInstance.fileProcessState.equals(MATWizard.fileProcessStates[2])
		ChipType chipType
		if (matWizardInstance.chipId != null &&   matWizardInstance.chipId > 0) {
			chipType = ChipType.get(matWizardInstance.chipId)
		}
		MATWizardFileFormat fileFormat = MATWizardFileFormat.get(matWizardInstance.fileFormatId)
		[matWizardInstance: matWizardInstance, fileFormat: fileFormat, importSuccess: importSuccess, chipType: chipType]
	}

	//3       Not currently supported
	def selectModuleGeneration = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		def generations = getAvailableModuleGenerations(params.id)
		[matWizardInstance: matWizardInstance, generations: generations]
	}

	def saveModuleGeneration = {
		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}
		MATWizard matWizardInstance = MATWizard.get(params.id)
		matWizardInstance.generationId = params.generationId
		def user = springSecurityService.currentUser
		matWizardInstance.userName = user?.username

		//TODO Save Files to filesystem
		if (matWizardInstance.save(flush: true)) {
			if (matWizardInstance.annotationFileRequired) {
				forward(action: "selectAnnotationFieldForGroups", id: matWizardInstance.id, params: params)
			} else {
				forward(action: "selectGroups", id: matWizardInstance.id, params: params)
			}
		} else {
			matWizardInstance.errors.each {
				println "Unable to save because ${it}"
			}
			render(view: "selectGroups", model: [matWizardInstance: matWizardInstance])
		}
	}

	//3.5
	def selectAnnotationFieldForGroups = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		def groupInfo = matWizardService.getGroupInfo(params.id)
		[matWizardInstance: matWizardInstance, groupInfo: groupInfo]
	}

	def saveAnnotationField = {
		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}
		MATWizard matWizardInstance = MATWizard.get(params.id)
		def analysisGroupSet = params.groupSetName
		matWizardInstance.groupSetName = analysisGroupSet
		if (matWizardInstance.save(flush: true)) {
			forward(action: "selectGroups", id: matWizardInstance.id, params: params)
		} else {
			forward(view: "selectAnnotationFieldForGroups", id: matWizardInstance.id)
		}
	}


	def getGroupNames(def wizardId, def annotationField) {
		return ['Ignore': 0, 'Case': 1, 'Control': 2]
	}

	//5
	def selectGroups = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		def groupTypes = [0: 'Ignore', 1: 'Case', 2: 'Control']
//		if (matWizardInstance.samples.size() == 0) {
		//Remove existing samples, existing groups in case user changed group selection
		matWizardInstance.sampleGroups.clear()
		matWizardInstance.samples.clear()
		def rawSamples = matWizardService.getSamples(params.id)
		rawSamples.each {	key, value ->
			MATWizardSample sample = new MATWizardSample(sampleId: key, groupName: value)
			matWizardService.addSampleToMATWizard(params.id, sample)
		}
		//	}
		//Make sure there are at least 3 groups
		def groupSize = matWizardInstance.sampleGroups.size()
		while (groupSize < 3) {
			matWizardInstance.addToSampleGroups(new MATWizardSampleGroup(groupName: "group ${groupSize}"))
			groupSize++
		}
		matWizardInstance.save()
		def groups = matWizardInstance.sampleGroups.sort { it.groupName}

		def samples = matWizardInstance.samples.sort {it.sampleId}
		def reasons = []
//		if (matWizardInstance.step.equals("5"))
//			reasons = matWizardInstance.getReasonsCantRunAnalysis()
		[matWizardInstance: matWizardInstance, groups: groups, samples: samples, groupTypes: groupTypes, reasons: reasons]
	}

	def saveGroups = {
		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}

		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}

		MATWizard matWizardInstance = MATWizard.get(params.id)
		matWizardInstance.step = params.step
		matWizardInstance.caseLabel = params.caseLabel
		matWizardInstance.controlLabel = params.controlLabel
		def user = springSecurityService.currentUser
		matWizardInstance.userName = user?.username
		matWizardInstance.sampleGroups.each { MATWizardSampleGroup group ->
			group.groupType = params."group_${group.id}"
		}
    if (matWizardInstance.save(flush: true)) {
			forward(action: "setParameters", id: matWizardInstance.id, params: params)
		} else {
			forward(view: "selectGroups", id: matWizardInstance.id)
		}
//		def res = matWizardInstance.save(flush: true)
//		if (res) {
//			matWizardInstance.clearErrors()
//			flash.clear()
//			matWizardInstance.save(flush: true)
//			def reasonsCantRunMAT = matWizardInstance.getReasonsCantRunAnalysis()
//			if (reasonsCantRunMAT.size() == 0) {
//				if (matWizardInstance.userEmail == null || matWizardInstance.userEmail.length() < 5) {
//					forward(action: "confirmation", id: matWizardInstance.id)
//				} else {
//					forward(action: "generatingModules", id: matWizardInstance.id)
//				}
//			} else {
//				forward(action: "selectGroups", id: matWizardInstance.id)
//			}
//		} else {
//			matWizardInstance.errors.each {
//				println "Unable to save because ${it}"
//			}
//			render(view: "selectGroups", model: [matWizardInstance: matWizardInstance])
//		}
	}

  // 6
  def setParameters = {
    MATWizard matWizardInstance = MATWizard.get(params.id)
    def reasons = []
		if (matWizardInstance.step.equals("6")) {
			reasons = matWizardInstance.getReasonsCantRunAnalysis()
    }
    [matWizardInstance: matWizardInstance, reasons:reasons]
  }

  def saveParameters = {
    if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}

    MATWizard matWizardInstance = MATWizard.get(params.id)
    matWizardInstance.step = params.step
    def user = springSecurityService.currentUser
		matWizardInstance.userName = user?.username

    matWizardInstance.method = params.method
    if (params.method == "fold") {
      matWizardInstance.foldCut = params.double("foldCut")
    } else if (params.method == "zscore") {
      matWizardInstance.zscoreCut = params.double("zscoreCut")
    }
    matWizardInstance.diffThreshold = params.double("diffThreshold")
    matWizardInstance.fdr = params.double("fdr")
    matWizardInstance.mts = params.mts

    def res = matWizardInstance.save(flush: true)
		if (res) {
			matWizardInstance.clearErrors()
			flash.clear()
			matWizardInstance.save(flush: true)
			def reasonsCantRunMAT = matWizardInstance.getReasonsCantRunAnalysis()
			if (reasonsCantRunMAT.size() == 0) {
				if (matWizardInstance.userEmail == null || matWizardInstance.userEmail.length() < 5) {
					forward(action: "confirmation", id: matWizardInstance.id)
				} else {
					forward(action: "generatingModules", id: matWizardInstance.id)
				}
			} else {
				forward(action: "setParameters", id: matWizardInstance.id)
			}
		} else {
			matWizardInstance.errors.each {
				println "Unable to save because ${it}"
			}
			render(view: "setParameters", model: [matWizardInstance: matWizardInstance])
		}

  }

	//7
	def confirmation = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		[matWizardInstance: matWizardInstance]
	}

	def saveConfirmation = {
		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}
		MATWizard matWizardInstance = MATWizard.get(params.id)
		matWizardInstance.step = params.step
		matWizardInstance.userEmail = params.userEmail
		def user = springSecurityService.currentUser
		matWizardInstance.userName = user?.username
		matWizardInstance.wizardStatus = MATWizard.wizardAvailableStatus[1]
		if (matWizardInstance.save(flush: true)) {
			//Start MAT Analysis
			matWizardService.startModuleAnalysis(params.id)
			forward(action: "generatingModules", id: matWizardInstance.id)
		} else {
			forward(action: "confirmation", id: matWizardInstance.id)
		}

	}

	//8
	def generatingModules = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		[matWizardInstance: matWizardInstance]
	}

	def saveEmailAddress = {
		if (params.id == null) {
			forward(action: "uploadSignalData")
			return
		}
		MATWizard matWizardInstance = MATWizard.get(params.id)
		matWizardInstance.step = params.step
		if (params.userEmail != null) {
			matWizardInstance.userEmail = params.userEmail
		}
		matWizardInstance.save(flush: true)
		forward(action: "generatingModules", id: matWizardInstance.id)
	}

	//9
	def moduleAnalysisResults = {
		MATWizard matWizardInstance = MATWizard.get(params.id)
		[matWizardInstance: matWizardInstance]
	}





	def index = {
		forward(action: "intro", params: params)
	}

	def list = {
		params.max = Math.min(params.max ? params.int('max') : 10, 100)
		[MATWizardInstanceList: MATWizard.list(params), MATWizardInstanceTotal: MATWizard.count()]
	}

	def save = {
		def MATWizardInstance = new MATWizard(params)
		if (MATWizardInstance.save(flush: true)) {
			flash.message = "${message(code: 'default.created.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), MATWizardInstance.id])}"
			forward(action: "list", id: MATWizardInstance.id)
		}
		else {
			render(view: "saveUploadData", model: [MATWizardInstance: MATWizardInstance])
		}
	}

	def update = {
		def MATWizardInstance = MATWizard.get(params.id)
		if (MATWizardInstance) {
			if (params.version) {
				def version = params.version.toLong()
				if (MATWizardInstance.version > version) {

					MATWizardInstance.errors.rejectValue("version", "default.optimistic.locking.failure", [message(code: 'MATWizard.label', default: 'MATWizard')] as Object[], "Another user has updated this MATWizard while you were editing")
					render(view: "edit", model: [MATWizardInstance: MATWizardInstance])
					return
				}
			}
			MATWizardInstance.properties = params
			if (!MATWizardInstance.hasErrors() && MATWizardInstance.save(flush: true)) {
				flash.message = "${message(code: 'default.updated.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), MATWizardInstance.id])}"
				forward(action: "show", id: MATWizardInstance.id)
			}
			else {
				render(view: "edit", model: [MATWizardInstance: MATWizardInstance])
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), params.id])}"
			forward(action: "list")
		}
	}

	def delete = {
		def MATWizardInstance = MATWizard.get(params.id)
		if (MATWizardInstance) {
			try {
				MATWizardInstance.delete(flush: true)
				flash.message = "${message(code: 'default.deleted.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), params.id])}"
				forward(action: "list")
			}
			catch (org.springframework.dao.DataIntegrityViolationException e) {
				flash.message = "${message(code: 'default.not.deleted.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), params.id])}"
				forward(action: "show", id: params.id)
			}
		}
		else {
			flash.message = "${message(code: 'default.not.found.message', args: [message(code: 'MATWizard.label', default: 'MATWizard'), params.id])}"
			redirect(action: "list")
		}
	}

	def getChipsByManufacturer = {
		def manufacturer = params.id
		def chips = matWizardService.getMATReadyChips(manufacturer)
		render chips as JSON
	}

}
