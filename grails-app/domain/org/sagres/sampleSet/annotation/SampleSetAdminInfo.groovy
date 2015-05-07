package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.SampleSet

class SampleSetAdminInfo {

  static belongsTo = [sampleSet: SampleSet]

  static constraints = {
    analyst(nullable: true)
    irbApprovalNumber(nullable: true)
    principleInvestigator(nullable: true)
    institution(nullable: true)
    contactPersons(nullable: true)
    samplesSent(nullable: true)
    resultsNeeded(nullable: true)
    billTo(nullable: true)
    billToAddress(nullable: true)
    billToReference(nullable: true)
    comments(nullable: true)
  }

  static mapping = {
    contactPersons type:'text'
    comments type:'text'
  }

  String analyst
  String irbApprovalNumber
  String principleInvestigator
  String institution
  String contactPersons

  Date samplesSent
  Date resultsNeeded

  String billTo
  String billToAddress
  String billToReference

  String comments
}
