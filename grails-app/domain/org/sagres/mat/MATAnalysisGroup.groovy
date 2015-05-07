package org.sagres.mat

class MATAnalysisGroup implements Comparable{

	static constraints = {
		groupSetName(blank: true, nullable:true)
		groupLabel(blank: true, nullable:true)
	}

	long groupSetId
	long groupId
	long caseControlGroup =-1l
	String groupSetName
	String groupLabel = "omitted"
	int groupOrder

	int compareTo(obj) {
		groupOrder.compareTo(obj.groupOrder)
	}


}

