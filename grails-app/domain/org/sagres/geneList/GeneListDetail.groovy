package org.sagres.geneList

class GeneListDetail {

  static belongsTo = [geneList: GeneList]
  long geneId
  
  static constraints = {
    geneList(nullable:false, blank:false)
    geneId(nullable:false, blank:false)
  }

  	static mapping = {
		  geneId index: 'geneId_Index'
	  }
}
