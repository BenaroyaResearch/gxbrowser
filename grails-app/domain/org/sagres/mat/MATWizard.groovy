package org.sagres.mat

class MATWizard {

	static hasMany = [sampleGroups: MATWizardSampleGroup, samples: MATWizardSample]
	static int minimumSamplesInGroup = 3
	static annotationOptions = ["None (will specify later)", "Select file", "Included in zip file", "2nd row in a signal file"]
	static fileProcessStates = ["Loading", "Processing", "Formatted", "Error"]

	static wizardAvailableStatus = ["Gathering Information", "Ready For Analysis", "Analysis Complete", "Notification Sent", "No Email Available"]

	static constraints = {
		annotationInfo(blank: false, inList: annotationOptions)
		wizardStatus(blank: false, inList: wizardAvailableStatus)
		fileProcessState(blank: false, inList: fileProcessStates)
		annotationFile(blank: true, nullable:true)
		signalData(blank: true, nullable:true)
		step(blank: true, nullable:true)
		generationId(blank: true, nullable: true)
		chipId(blank: true, nullable: true)
		sampleCount(blank: true, nullable: true)
		probeCount(blank: true, nullable: true)
		groupSetName(blank: true, nullable: true)
		//email: true,
		userEmail(blank: true, nullable: true)
		fileType(blank: false )
		analysisId(blank: true, nullable: true)
		userName(blank: true, nullable: true)
    displayName(blank: false, nullable: true)
	}

	static transients = ['importMethodName', 'annotationFileRequired', 'annotationOptions', 'reasonsCantRunAnalysis']

	def boolean getAnnotationFileRequired() {
		return annotationOptions[1].equals(annotationInfo)
	}

	def  getReasonsCantRunAnalysis() {
		def reasons = []
		if (caseLabel == null || caseLabel.length() == 0) {
			reasons.add("Case Label cannot be left blank")
		}
		if (controlLabel == null || controlLabel.length() == 0) {
			reasons.add("Control Label cannot be left blank")
		}
		//Verify has case and control groups
		def reqGroups = [0,1] //case,control
		reqGroups.each {  gIndex ->
			def cGroup = sampleGroups.find { it.groupType.equals(MATWizardSampleGroup.groupTypes[gIndex])}
			if (cGroup == null) {
				reasons.add("No ${MATWizardSampleGroup.groupTypes[gIndex]} group selected")
			} else {
				//Verify minimum number of samples in group
				if (cGroup.samples.size() < minimumSamplesInGroup) {
					reasons.add("There must be at least ${minimumSamplesInGroup} samples in the ${MATWizardSampleGroup.groupTypes[gIndex]} group")
				}
			}
		}

		return reasons
	}


	String signalData
	long fileFormatId  = -1
	String annotationInfo
	String annotationFile
	String step
	long userId  = -1
	Long generationId
	Long chipId
	String fileType
	Long sampleCount
	Long probeCount
	String groupSetName
	String caseLabel = "case"
	String controlLabel = "control"
	String userEmail
	Long analysisId
	String userName
	String fileProcessState = fileProcessStates[0]
	String annotationProblem = "false"

  String displayName

  // parameters
  String method = "fold"
  Double foldCut = 2.0
  Double zscoreCut = 2.0
  Double diffThreshold = 100.0
  Double fdr = 0.05
  String mts = "TRUE"
//	String analysisName = "Please Name"

	String wizardStatus  = wizardAvailableStatus[0]


}
