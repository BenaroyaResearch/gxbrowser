package org.sagres.mat

class ModuleDetail {

  static constraints = {
    probeId(nullable:false, blank:false, unique:'moduleId')
    geneSymbol(nullable:true, blank:false)
  }

  static mapping = {
    geneSymbol type:'text'
  }

  long moduleId
  String probeId
  String geneSymbol

}
