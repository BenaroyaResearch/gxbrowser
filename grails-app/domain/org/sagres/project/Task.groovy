package org.sagres.project

import java.util.Date;

import org.sagres.project.Project
import org.sagres.sampleSet.component.LookupListDetail
import org.sagres.sampleSet.component.LookupList

class Task {

	static belongsTo = [project: Project]
	
	String			 name
	LookupListDetail type
	LookupListDetail resource
	Date			 beginDate
	Date			 endDate
	String			 comments
	
	static mapping = {
		comments:'text'
	}

    static constraints = {
		type(nullable: false)
		project(nullable: true)
		resource(nullable: true)
		beginDate(nullable: true)
		endDate(nullable: true)
		comments(nullable: true)
    }
	
	String toString() {
		
		String rString = ""
		
		if (name)
		{
			rString = name + " - "
		}

		rString += type

		if (resource)
		{
			rString += " (" + resource + ")"
		}
		
		return rString
	}

}
