package org.sagres.mat

class MATWizardSampleGroup {

	public static groupTypes = ["Case", "Control", "Ignore"]
	static constraints = {
		groupType(blank: false, inList: groupTypes)
	}

	 String groupName
	 String groupType = "Ignore" //I.e. Case/Control/Ignore
		static hasMany = [samples: String]

}
