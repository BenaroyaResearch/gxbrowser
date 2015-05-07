package org.sagres.sampleSet

class SampleSetLink {

  static hasMany = [links:SampleSetLinkDetail]

  static constraints = {
    name(nullable: false, blank: false, unique: true)
    displayName(nullable: true, blank: false)
    baseUrl(nullable: false, blank: false)
    icon(nullable: true, blank: false)
  }

  String name
  String displayName
  String baseUrl
  String icon
  int visible = 1

}
