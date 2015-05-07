package org.sagres.gene

class GeneRif
{
    int taxId;
    int geneId;
    int pubmedId;
    Date lastUpdate;
    // String genRif; //In article abstract table (key pubmedId), instead

    static constraints =
    {
        taxId( nullable: false );
        geneId( nullable: false );
        pubmedId( nullable: false );
        lastUpdate( nullable: false );
    }
}
