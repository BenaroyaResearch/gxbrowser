package org.sagres.mat

class MATWizardFileFormat {

	static constraints = {
		fileFormat(blank: false)
	}

	String fileFormat
	String displayName
	//NEED to map between chip type and module generation

}
