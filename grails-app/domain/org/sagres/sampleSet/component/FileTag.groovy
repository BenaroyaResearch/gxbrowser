package org.sagres.sampleSet.component

import org.sagres.sampleSet.annotation.SampleSetFile

class FileTag {

  static hasMany = [sampleSetFiles: SampleSetFile]

  static constraints = {
    tag(blank: false, nullable: false, unique: true)
    sampleSetFiles(nullable: true)
  }

  String tag

  String toString()
  {
    return tag
  }
}
