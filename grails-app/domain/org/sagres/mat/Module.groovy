package org.sagres.mat

class Module {

  static constraints = {
    moduleName(nullable:false, blank:false)
    moduleAnnotationId(nullable:true)
  }

  long moduleGenerationId
  long moduleAnnotationId
  String moduleName
  int probeCount = 0

}
