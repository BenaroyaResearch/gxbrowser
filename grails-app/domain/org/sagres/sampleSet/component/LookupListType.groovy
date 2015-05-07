package org.sagres.sampleSet.component

class LookupListType {

  static constraints = {
    name(blank: false, unique: true)
  }

  String name

  String toString() {
    return name
  }
}
