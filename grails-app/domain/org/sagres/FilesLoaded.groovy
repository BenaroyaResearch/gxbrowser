package org.sagres

class FilesLoaded 
{
	String filename;
	FileLoadStatus loadStatus;
	Date dateStarted;
	Date dateEnded;
	String notes;
	
    static constraints = 
	{
		filename( blank: false );
		loadStatus( nullable: false );
		dateStarted( nullable: false );
		dateEnded( nullable: true );
		notes( nullable: true, maxSize: 2000 );
    }

    String toString( )
    {
        return filename;
    }
}
