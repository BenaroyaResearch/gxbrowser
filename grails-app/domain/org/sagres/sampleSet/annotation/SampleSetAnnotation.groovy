package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.SampleSet

class SampleSetAnnotation {
  static belongsTo = [sampleSet: SampleSet]

  static constraints = {
    purpose(nullable: true)
    hypothesis(nullable: true)
    controls(nullable: true)
    methods(nullable: true)
    samplingMethodInclusionCriteria(nullable: true)
    samplingMethodExclusionCriteria(nullable: true)
    controlGroups(nullable: true)
    methodOfMatching(nullable: true)
    experimentalDesign(nullable: true)
    experimentalDesignIsLongitudinal(nullable: true)
    experimentalVariables(nullable: true)
    possibleSourcesOfVariation(nullable: true)
    additionalInfo(nullable: true)
  }

  static mapping = {
    purpose type:'text'
    hypothesis type:'text'
    controls type:'text'
    methods type:'text'
    samplingMethodInclusionCriteria type:'text'
    samplingMethodExclusionCriteria type:'text'
    controlGroups type:'text'
    methodOfMatching type:'text'
    experimentalDesign type:'text'
    experimentalVariables type:'text'
    possibleSourcesOfVariation type:'text'
    additionalInfo type:'text'
  }

  String purpose
  String hypothesis
  String controls
  String methods
  String samplingMethodInclusionCriteria
  String samplingMethodExclusionCriteria
  String controlGroups
  String methodOfMatching
  String experimentalDesign
  String experimentalDesignIsLongitudinal
  String experimentalVariables
  String possibleSourcesOfVariation
  String additionalInfo

}
