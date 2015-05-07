package org.sagres.mat

class MATAnalysisGroupSet {

	static constraints = {
		groupName(blank: true, nullable:true)
		controlLabel(blank: true, nullable:true)
		caseLabel(blank: true, nullable:true)

	}

	static hasMany = [groups: MATAnalysisGroup]

	long groupId
	long analysisId
	String groupName

	String controlLabel = "control"
	String caseLabel    = "case"

	SortedSet groups


}
