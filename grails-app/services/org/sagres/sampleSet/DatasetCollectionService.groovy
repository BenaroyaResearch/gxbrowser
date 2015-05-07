package org.sagres.sampleSet

class DatasetCollectionService {

  def springSecurityService //injected
  def mongoDataService //injected

  def Map<String,List<Long>> getCollections(String term)
  {
    if (springSecurityService.isLoggedIn())
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        def query = [user:username]
        if (term)
        {
          query.put("name", ["\$regex":"${term.trim()}".toString(), "\$options":"i"])
        }
        def sampleSets = [:]
        mongoDataService.find("dscollections", query, ["name","samplesets"], null, 0).each {
          if (it.name != "temp-collection")
            sampleSets.put(it.name, it.samplesets)
        }
        return sampleSets
      }
    }
  }

  def List getCollectionNames()
  {
    if (springSecurityService.isLoggedIn())
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        def query = [user:username]
        def sampleSets = []
        mongoDataService.find("dscollections", query, ["name"], null, 0).each {
          if (it.name != "temp-collection")
            sampleSets.push(it.name)
        }
        return sampleSets
      }
    }
  }

  def void saveCollection(String name, List<Long> sampleSetIds)
  {
    if (springSecurityService.isLoggedIn() && name)
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        def mongoValues = null
        if (sampleSetIds)
        {
          mongoValues = [samplesets:sampleSetIds]
        }
        else
        {
          def tempSets = mongoDataService.findOne("dscollections", [user:username, name:"temp-collection"], ["samplesets"]).samplesets
          mongoValues = [samplesets:tempSets]
        }
        if (!mongoValues?.isEmpty())
        {
          mongoDataService.remove("dscollections", [user:username, name:name], "samplesets")
          mongoDataService.update("dscollections", [user:username, name:name], mongoValues)
        }
      }
    }
  }

  def void addToCollection(String name, long sampleSetId, boolean unique)
  {
    if (springSecurityService.isLoggedIn() && name && sampleSetId)
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        if (!getSampleSetsForCollection(name)?.contains(sampleSetId))
        {
          mongoDataService.add("dscollections", [user:username, name:name], "samplesets", sampleSetId, unique, false)
        }
      }
    }
  }

  def void removeFromCollection(String name, long sampleSetId)
  {
    if (springSecurityService.isLoggedIn() && name && sampleSetId)
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        mongoDataService.drop("dscollections", [user:username, name:name], "samplesets", sampleSetId)
      }
    }
  }

  def List<Long> getSampleSetsForCollection(String name)
  {
    if (springSecurityService.isLoggedIn() && name)
    {
      def username = springSecurityService.getCurrentUser()?.username
      if (username)
      {
        def sampleSetIds = mongoDataService.findOne("dscollections", [user:username, name:name], ["samplesets"])?.samplesets
        return sampleSetIds
      }
    }
  }


}
