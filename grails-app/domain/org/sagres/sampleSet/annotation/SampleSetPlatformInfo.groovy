package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.SampleSet

class SampleSetPlatformInfo {

  static belongsTo = [sampleSet: SampleSet]

  static constraints = {
    platform(nullable: true)
    platformOption(nullable: true)
	libraryPrepOption(nullable: true)
  }

  String platform
  String platformOption
  String libraryPrepOption

}
