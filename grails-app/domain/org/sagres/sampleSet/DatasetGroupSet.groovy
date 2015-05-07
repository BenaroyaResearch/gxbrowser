package org.sagres.sampleSet

import org.sagres.rankList.RankList

class DatasetGroupSet
{
	static belongsTo = [sampleSet:SampleSet]
	static hasMany = [groups:DatasetGroup]

	static constraints = {
	  name(nullable: false, blank: false, unique:'sampleSet')
    defaultRankList(nullable: true)
	}

	static mapping = {
	  groups sort:'displayOrder', order:'asc'
	}

	String name
  RankList defaultRankList

	public String toString()
	{
		return name
	}
}
