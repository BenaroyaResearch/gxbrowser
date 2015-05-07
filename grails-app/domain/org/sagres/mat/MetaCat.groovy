package org.sagres.mat

import org.sagres.sampleSet.component.LookupListDetail;

import common.SecUser;

class MetaCat {

	static hasMany = [analyses: Analysis]
	
	String displayName
	LookupListDetail disease
	Integer generation
	SecUser	user
	Integer noSamples
	Integer noCases
	
    static constraints = {
		disease(nullable: true)
    }
	
	String toString() {
		return displayName
	}
	
}
