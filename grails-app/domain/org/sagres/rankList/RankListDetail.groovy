package org.sagres.rankList

class RankListDetail 
{
	static belongsTo =  [ rankList : RankList ]
	
	//	RankList rankList
	String probeId
	Integer rank
	Double value
	Double fdrPValue

    static constraints = 
	{
		//rankList( nullable: false )
		probeId( blank: false )
		rank( nullable: false )
		value( nullable: true )
		fdrPValue( nullable: true )
    }
	
}
