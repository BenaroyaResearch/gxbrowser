package org.sagres.mat

class MATImage {

	static constraints = {
		description(size: 0..2000)
	}

	static hasMany = [tags: String]

	String imageName
	String description = "Not Available"
	int priority = 60



}
