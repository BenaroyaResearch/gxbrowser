package org.sagres.sampleSet

class DatasetGroup
{

	static belongsTo = [groupSet:DatasetGroupSet]
	static hasMany = [groupDetails:DatasetGroupDetail]

	static constraints = {
	  name(blank: false, nullable: false, unique:'groupSet')
	  displayOrder(nullable: false)
	  hexColor(nullable: true)
	}

	String name
	Integer displayOrder = 0
	String hexColor

	String toString()
	{
	  return name
	}
}
