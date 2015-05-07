package org.sagres.project

import common.SecUser
import org.sagres.project.Task
import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.component.LookupListDetail
import org.sagres.sampleSet.component.LookupList

class Project
{

	static hasMany = [deliverables:Deliverable, tasks:Task]
	
	String	name
	String	title
	String	priority
	Integer	importance
	String	hypothesis
	String	design
	String  purpose
	
	LookupListDetail	organism
	LookupListDetail	technology
	String	client
	String	contact
	String	color
	LookupListDetail	status
	Date	statusDate
	Long	tg2id
	SampleSet sampleSet
	Date	contactDate
	Date	analysisDate
	Date	deliveryDate
	SecUser	owner
	SecUser analyst
	Integer	locked
	Integer privateRecord
	Integer sampleCount
	Integer	markedForDelete
	String  comments
	
    static mapping = {
		hypothesis type:'text'
		design type:'text'
		purpose type:'text'
		comments:'text'
    }
	
	static constraints = {
		name(nullable:false)
		title(nullable:false)
		importance(nullable: true)
		design(nullable: true)
		hypothesis(nullable: true)
		purpose(nullable: true)
		technology(nullable: true)
		sampleSet(nullable: true)
		tg2id(nullable: true)
		organism(nullable: true)
		priority(nullable: true)
		status(nullable: true)
		analyst(nullable: true)
		client(nullable: true)
		contact(nullable: true)
		color(nullable: true)
		contactDate(blank: false)
		deliverables(nullable:true)
		tasks(nullable:true)
		statusDate(nullable: true)
		analysisDate(nullable: true)
		deliveryDate(nullable: true)
		locked(nullable: true)
		privateRecord(nullable: true)
		sampleCount(nullable: true)
		markedForDelete(nullable: true)
		comments(nullable: true)
	}
	
	String toString() {
		return title
	}
}
