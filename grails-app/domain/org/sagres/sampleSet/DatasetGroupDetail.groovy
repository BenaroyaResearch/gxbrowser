package org.sagres.sampleSet

import common.ArrayData

class DatasetGroupDetail
{
	static belongsTo = [group:DatasetGroup]

	static constraints = {
	  sample(nullable: false)
	  displayOrder(nullable: true)
	  hexColor(nullable: true)
	}

	ArrayData sample
	Integer displayOrder = 0
	String hexColor

	String toString() {
	  return "${sample.id}: ${sample.barcode}"
	}
}
