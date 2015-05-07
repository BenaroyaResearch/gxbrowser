package org.sagres.gene

import common.GeneInfo

/** current synonyms for gene in the common gene_info table
 * loaded from the synonym column in the ncbi gene_info table
 */
class GeneSynonym
{

	static constraints = {
	}

	GeneInfo gene
	String synonym

}
