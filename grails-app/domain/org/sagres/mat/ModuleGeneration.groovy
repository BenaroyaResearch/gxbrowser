package org.sagres.mat

class ModuleGeneration {

  static constraints = {
    versionName(nullable:false, blank:false, unique:true)
    generation(nullable:false, blank:false)
  }

  String versionName
  int generation
  long chipTypeId

}
