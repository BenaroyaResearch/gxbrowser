package org.sagres.geneList

class GeneListCategory {

	String name
	String description

	static hasMany = [ geneLists : GeneList ]
	
	static constraints = {
		name(unique:true, nullable:false, blank:false)
		description(nullable:true, blank:false)
	}

	String toString() {
		return name
	}
}
