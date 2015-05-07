package org.sagres.rankList

class RankListType 
{
	String abbrev;
	String description;
    int numFileColumns;
    int probeIdFileColumn;
    int rankFileColumn;
    int valueFileColumn;
    int pvalFileColumn;
	
    static constraints = 
	{
		abbrev( nullable: false, unique: true );
		description( nullable: false );
        numFileColumns( nullable: false, min: 2 );
        probeIdFileColumn( nullable: false );
        rankFileColumn( nullable: false );
        valueFileColumn( nullable: false );
        pvalFileColumn( nullable: false );
    }

    String toString( )
    {
        return description;
    }
}
