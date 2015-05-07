package common

import common.chipInfo.ChipsLoaded

class ArrayData {

	String barcode;
	ChipsLoaded chip;
	Double averageSignal;
	String externalID;
	String externalDB;
    String sampleName;
	String origSampleType;
    String sampleType;
    Float sampleConcentration;

  static constraints =
	{
        barcode( );
        chip( );
		averageSignal( nullable: true );
		externalID( nullable: true );
		externalDB( nullable: true );
        sampleName( nullable: true );
		origSampleType( nullable: true );
        sampleType( nullable: true );
        sampleConcentration( nullable: true );
  }

  String toString() {
    return this.id
  }
}
