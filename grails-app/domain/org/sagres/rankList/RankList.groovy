package org.sagres.rankList

import org.sagres.FilesLoaded

class RankList
{
	static hasMany = [ rankListDetails: RankListDetail ]
	
	FilesLoaded fileLoaded
	Integer sampleSetId
    Integer groupSetId
	RankListType rankListType
	String description
	Integer numProbes
	Integer markedForDelete
	
	static mapping =
	{
		rankListDetails cascade: 'all-delete-orphan'
	}

    static constraints =
	{
		fileLoaded( nullable: true )
		sampleSetId( nullable: false, min: 1 )
        groupSetId( nullable: true )
		rankListType( nullable: false )
		description( nullable: false )
		numProbes( nullable: true )
		markedForDelete( nullable: true )
    }

    String toString( )
    {
        return description + " (" + rankListType + ")"
    }

	static transients = [ 'name' ]
	
	public String getName() {
		return "${rankListType.description} - ${description}"
	}

}
