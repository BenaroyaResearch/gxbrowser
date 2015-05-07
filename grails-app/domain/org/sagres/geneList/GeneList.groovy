package org.sagres.geneList

import common.SecUser

class GeneList {

  String name
  String description
  SecUser user
  
  static hasOne =  [ geneListCategory : GeneListCategory ]
  static hasMany = [ geneListDetails: GeneListDetail ]

  static constraints = {
    name(unique:true, nullable:false, blank:false)
    description(nullable:true, blank:false)
  }

  static mapping = {
    name type:'text'
    description type:'text'
  }

}
