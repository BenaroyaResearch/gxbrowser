package common.chipInfo

import org.sagres.sampleSet.SampleSet
import org.sagres.FileLoadStatus;

class ChipsLoaded {

    static constraints = {
	    filename()
	    chipType()
	    noSamples()
	    noProbes()
	    loadStatus()
	    notes(nullable: true, widget: 'textarea')
	    dateStarted()
	    dateEnded(nullable: true)
	    genomicDataSource()
	    sampleSet(nullable: true)
        additionalFiles( nullable: true )
    }

	static mapping =
	{
		notes type: 'text'
	}


	String filename
	ChipType chipType
	int noSamples = -1
	int noProbes = -1
	FileLoadStatus loadStatus
	String notes  //@todo should map to textarea
	Date dateStarted = new Date()
	Date dateEnded
	GenomicDataSource genomicDataSource
	SampleSet sampleSet
    String additionalFiles

    String toString( )
    {
        return filename;
    }
}
