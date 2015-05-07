package org.sagres.gene

class GeneGroup 
{
    int taxId;
    int geneId;
    String relationship;
    int otherTaxId;
    int otherGeneId;
    
    static constraints =
    {
        taxId( nullable: false );
        geneId( nullable: false );
        relationship( blank: false );
        otherTaxId( nullable: false );
        otherGeneId( nullable: false );
    }
}
