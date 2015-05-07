package org.sagres.gene

class GenePubmed {
	int taxId;
	int geneId;
	int pubmedId;

	static constraints =
	{
		taxId(nullable: false);
		geneId(nullable: false);
		pubmedId(nullable: false);
	}
}
