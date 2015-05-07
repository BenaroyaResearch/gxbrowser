package org.sagres.sampleSet.annotation

import org.sagres.sampleSet.SampleSet
import org.sagres.sampleSet.component.FileTag

class SampleSetFile {

  static belongsTo = [sampleSet: SampleSet]
  static hasOne = [tag: FileTag]

  static constraints = {
    description(blank: false, nullable: true)
    filename(blank: false, nullable: false)
    extension(blank: true, nullable: false)
    header(nullable: true)
    tag(nullable: true)
    fileVersion(nullable: false)
  }

  static mapping = {
    description type:'text'
    header type:'text'
  }

  String description
  String filename
  String extension
  String header
  Integer fileVersion = 0

  String toString()
  {
    return filename
  }


}
