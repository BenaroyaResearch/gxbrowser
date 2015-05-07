package org.sagres.sampleSet

class FocusedArrayFoldChangeParams {

	SampleSet sampleSet;
	Float palx;
	Float floor;

    static constraints = {
		sampleSet( );
		palx( nullable: true );
		floor( nullable: true );
    }
}
