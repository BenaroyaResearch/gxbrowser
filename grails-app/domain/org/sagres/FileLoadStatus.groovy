package org.sagres

class FileLoadStatus 
{
	String name;
	String description;

    static constraints = 
	{
		name( nullable: false );
		description( nullable: false );
    }

    String toString( )
    {
        return description;
    }
}
