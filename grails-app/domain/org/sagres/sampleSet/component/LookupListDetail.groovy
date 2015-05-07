package org.sagres.sampleSet.component

class LookupListDetail {

  static belongsTo = [lookupList: LookupList]

  static constraints = {
    name(blank: false)
  }

  String name

  String toString() {
    return name
  }

}
