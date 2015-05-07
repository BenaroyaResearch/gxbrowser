package org.sagres.sampleSet

class SampleSetLinkDetail {

  static belongsTo = [sampleSet:SampleSet]
  static hasOne = [linkType:SampleSetLink]

  static constraints = {
    dataUrl(nullable: false, blank: false)
  }

  String dataUrl
}
