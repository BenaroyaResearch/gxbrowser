package org.sagres.labkey

class LabkeyReport {

	static constraints = {
		sampleSetId(nullable: false, blank: false)
		category(nullable: false, blank: false)
		reportName(nullable: false, blank: false)
		reportURL(nullable: false, blank: false)
		dateLastLoaded(nullable: true, blank: true)
		enabled(nullable: true, blank: true)
    reportColumn(nullable: true, blank: false)
    sampleSetColumn(nullable: true, blank: false)
	}

	Long sampleSetId
	String reportName
	String category
	String reportURL
	Date dateLastLoaded
	Boolean enabled     = true

  String reportColumn
  String sampleSetColumn
}
