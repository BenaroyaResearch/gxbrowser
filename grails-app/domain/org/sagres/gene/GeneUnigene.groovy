package org.sagres.gene

class GeneUnigene 
{
    int geneId;
    String unigeneCluster;

    static constraints = 
    {
        geneId( nullable: false );
        unigeneCluster( nullable: false );
    }
}
