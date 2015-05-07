package org.sagres.rankList

class RankListParams {

	static constraints = {
		userName(blank: true, nullable:true)
		runDate(blank: true, nullable: true)
		rankListName(blank: true, nullable: true)
		sampleSetName(blank: true, nullable: true)


	}

	static hasMany = [comparisons: RankListComparison]


	long sampleSetId
	long sampleSetGroupSetId
	Date runDate
	String userName
	String rankListName
	String sampleSetName


}
