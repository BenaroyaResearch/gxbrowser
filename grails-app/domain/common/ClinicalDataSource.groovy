package common

class ClinicalDataSource {

  static constraints = {
    name(nullable: false, blank: false)
    iconName(nullable: true, blank: false)
    displayName(nullable: false, blank: false)
    baseUrl(nullable: true, blank: false)
  }

  String name
  String iconName
  String displayName
  String baseUrl

}
