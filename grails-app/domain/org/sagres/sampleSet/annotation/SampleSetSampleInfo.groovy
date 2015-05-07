package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.SampleSet
import common.chipInfo.Species
import org.sagres.sampleSet.component.LookupListDetail

class SampleSetSampleInfo {

  static belongsTo = [sampleSet: SampleSet]
  static hasMany = [sampleSources: LookupListDetail]

  static constraints = {
    numberOfSamples(min: 1, matches: "[0-9]+", nullable: true)
    species(nullable: true)
    disease(nullable: true)
    sampleSources(nullable: true)
    treatmentProtocol(nullable: true)
    growthProtocol(nullable: true)
    extractionProtocol(nullable: true)
    extractionKit(nullable: true)
    sampleType(nullable: true)
    storageSolution(nullable: true)
    storageConditions(nullable: true)
    sampleSetSpreadsheet(nullable: true)
  }

  static mapping = {
    treatmentProtocol type:'text'
    growthProtocol type:'text'
    extractionProtocol type:'text'
    storageConditions type:'text'
  }

  Integer numberOfSamples
  Species species
  LookupListDetail disease
  String treatmentProtocol
  String growthProtocol
  String extractionProtocol
  String extractionKit
  String sampleType
  String storageSolution
  String storageConditions
  SampleSetFile sampleSetSpreadsheet

}
