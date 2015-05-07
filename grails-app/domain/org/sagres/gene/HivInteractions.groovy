package org.sagres.gene

class HivInteractions 
{
    int taxId1;
    int geneId1;
    String productAccession1;
    String productName1;
    String interaction; //short phrase
    int taxId2;
    int geneId2;
    String productAccession2;
    String productName2;
    String pubmedList; //comma-separated
    Date lastUpdate;
    String geneRif;

    static constraints =
    {
        taxId1( nullable: false );
        geneId1( nullable: false );
        productAccession1( nullable: false );
        productName1( nullable: false );
        interaction( blank: false ); //short phrase
        taxId2( nullable: false );
        geneId2( nullable: false );
        productAccession2( nullable: false );
        productName2( nullable: false );
        pubmedList( nullable: false ); //comma-separated
        lastUpdate( nullable: false );
        geneRif( nullable: false );
    }
}
