package org.sagres.sampleSet.component

class LookupList {
  static hasMany = [lookupDetails: LookupListDetail]

  static constraints = {
    name(blank: false, unique: true)
    description(nullable: true)
    type(nullable: true)
    defaultOption(nullable: true)
  }

  static mapping = {
    lookupDetails sort:'name', order:'asc'
    description type:'text'
  }

  String name
  String description
  String type
  LookupListDetail defaultOption

  String toString() {
    return name
  }
}
