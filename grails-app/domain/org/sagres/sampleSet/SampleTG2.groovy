package org.sagres.sampleSet

/** maintains the link between the general sample ID and the TG2 LIMS sample ID */
class SampleTG2
{

	static constraints = {
		tg2ID(nullable: true)
		barcode(nullable: true)
	}

	int sampleID
	String tg2ID
	String barcode
}
