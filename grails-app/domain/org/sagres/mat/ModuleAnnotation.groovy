package org.sagres.mat

class ModuleAnnotation {

  static constraints = {
    generation(nullable:false, blank:false)
//    moduleGenerationId(nullable:false)
    moduleName(nullable:false, blank:false)
    annotation(nullable:false, blank:false)
    abbreviation(nullable:true, blank:false)
    hexColor(nullable:false, blank:false)
  }

//  long moduleGenerationId
  int generation
  String moduleName
  String annotation
  String abbreviation
  String hexColor

}
