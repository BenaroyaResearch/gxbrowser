package org.sagres.gene

class GeneRefseq {
	int taxId;
	int geneId;
	String status;
	String rnaNucleotideAccession;
	String rnaNucleotideGi;
	String proteinAccession;
	String proteinGi;
	String genomicNucleotideAccession;
	String genomicNucleotideGi;
	Integer startPosition;
	Integer endPosition;
	String orientation;
	String assembly;

	static constraints =
	{
		taxId(nullable: false);
		geneId(nullable: false);
		status(nullable: true);
		rnaNucleotideAccession(nullable: true);
		rnaNucleotideGi(nullable: true);
		proteinAccession(nullable: true);
		proteinGi(nullable: true);
		genomicNucleotideAccession(nullable: true);
		genomicNucleotideGi(nullable: true);
		startPosition(nullable: true);
		endPosition(nullable: true);
		orientation(nullable: true);
		assembly(nullable: true);
	}
}
