package org.sagres.sampleSet

import org.sagres.util.mongo.MongoConnector
import com.mongodb.*
import org.sagres.importer.TextTable

class MongoDataService {

	def grailsApplication
	
  public static int ASCENDING = 1
  public static int DESCENDING = -1

  private def cacheKeys = new HashSet<String>()
  private def collection = MongoConnector.getInstance().getCollection()

  def List getSamplesInSampleSet(long sampleSetId)
  {
    getSamplesInSampleSet(sampleSetId, null, ASCENDING, -1)
  }

  def List getSamplesInSampleSet(long sampleSetId, String sortKey, int sortDirection, int limit)
  {
    DBCursor cur = collection.find(new BasicDBObject("sampleSetId", sampleSetId))
    if (sortKey)
    {
      cur = cur.sort(new BasicDBObject(sortKey, sortDirection))
    }
    if (limit > 0 && cur.length() > limit)
    {
      return cur.toArray()[0..limit-1]
    }
    else
    {
      return cur.toArray()
    }
  }

  def List getSampleIdsInSampleSet(long sampleSetId)
  {
    def sampleIds = []
    DBCursor cur = collection.find(new BasicDBObject("sampleSetId", sampleSetId),
      new BasicDBObject("sampleId", 1))
    while (cur.hasNext())
    {
      sampleIds.push(cur.next().get("sampleId"))
    }
    return sampleIds
  }

  def List getColumnValuesAndSampleId(long sampleSetId, String column)
  {
    BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
    BasicDBObject fields = new BasicDBObject("sampleId", 1)
    fields.put(column, 1)
    def fieldName = column.split("\\.")
    def result = []
    DBCursor cur = collection.find(query, fields)
    while (cur.hasNext())
    {
      DBObject obj = cur.next()
      def v = null
      if (fieldName.size() > 1) {
//        v = obj.get(fieldName[0]) ? (((Map)obj.get(fieldName[0])).get(fieldName[1])) : "null"
		  v = obj.get(fieldName[0]) ? (((Map)obj.get(fieldName[0])).get(fieldName[1])) : ""
      } else {
//		  v = obj.get(fieldName[0]) ?: "null"
	  	  v = obj.get(fieldName[0]) ?: ""
      }
      def m = ["sampleId": obj.get("sampleId")]
      m.put(column, v)
      result.push(m)
    }
    return result
  }

	def Object getValuesForSingleSample(long sampleID)
	{
		BasicDBObject query = new BasicDBObject("sampleId", sampleID);
		def results = collection.findOne(query)
		return results != null ? results.get("values") : null
	}


  def Object getValuesForSample(long sampleSetId, long sampleId)
  {
    BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
    query.put("sampleId", sampleId)
	  def results = collection.findOne(query)
	  return results ? results.get("values") : null
  }

  def List getColumnValues(long sampleSetId, String column)
  {
    BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
    return collection.distinct(column, query)
  }

  def List getColumnValues(long sampleSetId, Collection<Long> sampleIds, String column)
  {
    BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
    query.put("sampleId", new BasicDBObject("\$in", sampleIds.toArray()))
    return collection.distinct(column, query)
  }

  def Object getValueForColumn(long sampleSetId, long sampleId, String column)
  {
    BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
    query.put("sampleId", sampleId)
    DBObject value = collection.findOne(query, new BasicDBObject(column, 1))
    def fieldName = column.split("\\.")
    if (fieldName.length > 1)
    {
      return value && value.get(fieldName[0]) ? value.get(fieldName[0]).get(fieldName[1]) : "null"
    }
    else
    {
      return value ? value.get(column) : "null"
    }
  }

  def Map getSampleFields()
  {
    def sampleFields = [:]
    def collection = MongoConnector.getInstance().getCollection(grailsApplication.config.mongo.sampleField.collection)
    DBCursor cur = collection.find().sort(new BasicDBObject("defaultDisplayOrder", 1))
    while (cur.hasNext())
    {
      def key = cur.next().get("key")
      def field = [:]
      field.put("displayName", cur.curr().get("displayName"))
      field.put("type", cur.curr().get("type"))
      sampleFields.put(key, field)
    }
    return sampleFields
  }

  def List getSampleSetFieldKeys(long sampleSetId, boolean containingValues = false)
  {
    Map sampleFields = getSampleFields()

    def fields = []
    String ignoreNulls = containingValues ? "if (this.values[key] != null && this.values[key] != 'null') { emit(key,1); }" : "emit(key,1);"
    String map = "function() { for (var key in this.values) { ${ignoreNulls} } }"
    String reduce = "function(key, value) { return 1; }"
    MapReduceOutput output = collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, new BasicDBObject("sampleSetId", sampleSetId))
    output.results().each { DBObject obj ->
      def fieldName = obj.get("_id")
      def headerName = sampleFields.containsKey(fieldName) ? sampleFields.get(fieldName).displayName : fieldName
      fields.push([label:"values.${fieldName}".toString(), header:headerName.encodeAsHumanize()])
    }

    return fields
  }

  def List getTg2Fields(long sampleSetId, boolean containingValues = false)
  {
    def fields = []
    String ignoreNulls = containingValues ? "if (this.tg2[key] != null && this.tg2[key] != 'null') { emit(key,1); }" : "emit(key,1);"
    String map = "function() { for (var key in this.tg2) { ${ignoreNulls} } }"
    String reduce = "function(key, value) { return 1; }"
    MapReduceOutput output = collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, new BasicDBObject("sampleSetId", sampleSetId))
    output.results().each { DBObject obj ->
      def fieldName = obj.get("_id")
      def humanized = fieldName.split("_").collect { String h -> h.charAt(0).toUpperCase().toString() + h.substring(1) }.join(" ")
      fields.push([label:"tg2.${fieldName}", header:humanized])
    }
    return fields;
  }

  def Map getSampleSetFields(long sampleSetId, Integer limit = null)
  {
    def fields = []
    String map = "function() { for (var key in this.values) { emit(key, 1); } }"
    String reduce = "function(key, value) { return 1; }"
    MapReduceOutput output = collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, new BasicDBObject("sampleSetId", sampleSetId))
    output.results().each { DBObject obj ->
      fields.push(obj.get("_id"))
    }

    def sampleFields = [:]
    def collection = MongoConnector.getInstance().getCollection(grailsApplication.config.mongo.sampleField.collection)
    BasicDBObject query = new BasicDBObject("key", new BasicDBObject("\$in", fields.toArray()))
    DBCursor cur = collection.find(query).sort(new BasicDBObject("defaultDisplayOrder", 1))
    if (limit)
    {
      cur.limit(limit)
    }
    while (cur.hasNext())
    {
      def key = cur.next().get("key")
      def field = [:]
      field.put("displayName", cur.curr().get("displayName"))
      field.put("type", cur.curr().get("type"))
      sampleFields.put(key, field)
    }
    return sampleFields
  }

  def List getSampleFieldKeys()
  {
    def sampleFieldKeys = []
    def collection = MongoConnector.getInstance().getCollection(grailsApplication.config.mongo.sampleField.collection)
    DBCursor cur = collection.find().sort(new BasicDBObject("defaultDisplayOrder", 1))
    while (cur.hasNext())
    {
      def key = cur.next().get("key")
      sampleFieldKeys.push(key)
    }
    return sampleFieldKeys
  }

  def List getSamples(long sampleSetId)
  {
    def samples = []
    DBCursor cur = collection.find(new BasicDBObject("sampleSetId", sampleSetId)).sort(new BasicDBObject("sampleId", 1))
    while (cur.hasNext())
    {
      DBObject obj = cur.next()
      def sampleId = obj.containsKey("sampleBarcode") ? obj.get("sampleBarcode") : obj.get("sampleId")
      def sample = [sampleId: sampleId, id: obj.get("sampleId")]
      if (obj.get("values"))
      {
        sample.putAll((Map)obj.get("values"))
      }
      samples.push(sample)
    }
    return samples
  }

	def getSamplesForDynamicGroups(long sampleSetID, String key)
	{
		def groups = [:]
		def keys = key.split("\\.")
    def fields = new BasicDBObject(key, 1)
    fields.append("sampleId", 1)
    DBCursor cur = collection.find(new BasicDBObject("sampleSetId", sampleSetID), fields)
		while (cur.hasNext())
		{
			DBObject obj = cur.next()
			DBObject value = obj.get(keys[0].toString())

			def val
			if (keys.size() > 1)
				val = value?.get(keys[1]) ? value.get(keys[1]) : "null"
			else
				val = value
			def sampleID = obj.get("sampleId")

			def groupData = groups.get(val)
			if (groupData == null)
			{
				groupData = [:]
				def samples = []
				groupData.put("samples", samples)
			}
			def samples = groupData.get("samples")
			samples.push(sampleID)
			groups.put(val, groupData)
		}
		return groups;
	}

  def updateSamples(long sampleSetId)
  {
    def ss = SampleSet.get(sampleSetId)
    if (ss && ss.defaultGroupSet)
    {
      ss.defaultGroupSet.groups.groupDetails.flatten().each { DatasetGroupDetail d ->
        BasicDBObject query = new BasicDBObject("sampleSetId", sampleSetId)
        query.append("sampleId", d.sample.id)
        BasicDBObject values = new BasicDBObject("sampleBarcode", d.sample.barcode)
        BasicDBObject set = new BasicDBObject("\$set", values)
        collection.update(query, set, true, true)
      }
    }
  }

	def updateSampleEx(long sampleSetId, long sampleId, String key, Object newValue)
	{
		BasicDBObject query = new BasicDBObject("sampleId", sampleId)
		if (sampleSetId >= 0)
		{
		  query.append("sampleSetId", sampleSetId)
		}
		BasicDBObject values = new BasicDBObject(key, newValue)
		BasicDBObject set = new BasicDBObject("\$set", values)
		collection.update(query, set, true, true)
	}

  def updateSample(long sampleSetId, long sampleId, String key, Object newValue)
  {
    BasicDBObject query = new BasicDBObject("sampleId", sampleId)
    if (sampleSetId >= 0)
    {
      query.append("sampleSetId", sampleSetId)
    }
    BasicDBObject values = new BasicDBObject("values.${key}", newValue)
    BasicDBObject set = new BasicDBObject("\$set", values)
    collection.update(query, set, true, true)
  }

  def updateFields(Collection<String> keys)
  {
    def currentKeys = getSampleFieldKeys()
    def fieldCollection = MongoConnector.getInstance().getCollection(grailsApplication.config.mongo.sampleField.collection)
    def numFields = fieldCollection.getCount()
    keys.each {
      if (!currentKeys.contains(it))
      {
        def field = new BasicDBObject("key", it)
        field.put("displayName", it)
        field.put("type", "text")
        field.put("defaultDisplayOrder", numFields)
        fieldCollection.insert(field)
        numFields++
      }
    }
  }

  def updateTg2Sample(long sampleSetId, long sampleId, Map values)
  {
    def skipFields = ["sample_name", "sample_id", "id", "comp_barcode"]
    BasicDBObject toFind = new BasicDBObject("sampleSetId", sampleSetId)
    toFind.put("sampleId", sampleId)
    DBCursor cur = collection.find(toFind)
    if (cur.hasNext())
    {
      DBObject obj = cur.next()
      BasicDBObject newValues = new BasicDBObject()
      newValues.append("sampleName", values.sample_name)
      newValues.append("tg2SampleId", values.sample_id)
      newValues.append("tg2chipPositionId", values.id)
      newValues.append("tg2compBarcode", values.comp_barcode.toString())
      values.each { key, value ->
        if (!skipFields.contains(key) && value)
        {
          if (value instanceof GString)
          {
            value = value.toString()
          }
          newValues.append("tg2.${key}", value)
        }
      }
      BasicDBObject setValue = new BasicDBObject("\$set", newValues)
      collection.update(obj, setValue, true, true)
    }
  }

  def deleteSampleSet(long sampleSetId)
  {
	  
	DBCollection queryCollection = MongoConnector.getInstance().getCollection("sampleSet")
	// create the query
	BasicDBObject deleteQuery = new BasicDBObject("sampleSetId", sampleSetId)
	// create the unset query - remove field only
	BasicDBObject unset = new BasicDBObject("\$unset", new BasicDBObject("spreadsheet", 1))
	queryCollection.update(deleteQuery, unset, false, true)
	
	queryCollection = MongoConnector.getInstance().getCollection("sampleSetTabs")
	unset = new BasicDBObject("\$unset", new BasicDBObject("tabs", 1))
	queryCollection.update(deleteQuery, unset, false, true)

	queryCollection = MongoConnector.getInstance().getCollection("sample")
	unset = new BasicDBObject("\$unset", new BasicDBObject("values", 1))
	queryCollection.update(deleteQuery, unset, false, true)

    //BasicDBObject sampleSet = new BasicDBObject("sampleSetId", sampleSetId)
    //collection.remove(sampleSet)
  }

  def Map<String,String> sampleSetFields(long sampleSetId)
  {
    def map = """function() {
      for (var key in this.values) { emit(key, "values"); } }""".toString()
    def reduce = "function(key,values) { return values[0]; }"
    MapReduceOutput output = collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, new BasicDBObject("sampleSetId", sampleSetId))
    def fields = [:]
    output.results().each { DBObject obj ->
      fields.put(obj.get("_id"),
      obj.get("value"))
    }
    return fields
  }

  /**
   * Create a new query from key/value pairs
   * @param keyValuePairs
   * @return
   */
  def BasicDBObject newQuery(Map<String,Object> keyValuePairs)
  {
    if (keyValuePairs)
    {
      return buildQuery(keyValuePairs, new BasicDBObject())
      //return new BasicDBObject(keyValuePairs)
    }
    else
    {
      return new BasicDBObject()
    }
  }

  private def buildQuery(Map<String,Object> keyValuePairs, BasicDBObject obj)
  {
    keyValuePairs.each { key, value ->
      if (value instanceof Map)
      {
        obj.put(key, buildQuery(value, new BasicDBObject()))
      }
      else
      {
        obj.append(key, value)
      }
    }
  }

  def BasicDBObject newReturnFields(List<String> fields)
  {
    BasicDBObject returnfields = new BasicDBObject()
    fields.each {
      returnfields.append(it, 1)
    }
    return returnfields
  }

  /**
   * Insert a document or update the document with new values
   *
   * @param dbCollection
   * @param query
   * @param values
   * @return
   */
  def insert(String dbCollection, Map<String,Object> query, Map<String,Object> values)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create a setter
      BasicDBObject newValues = new BasicDBObject(values)
      BasicDBObject set = new BasicDBObject("\$set", newValues)
      // insert or update
      if (query)
      {
        // create the query
        BasicDBObject theQuery = newQuery(query)
        queryCollection.update(theQuery, set, true, true)
      }
      else
      {
        queryCollection.insert(newValues)
        return newValues._id
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Find all documents matching query
   *
   * @param dbCollection
   * @param query
   * @param fields Return only fields defined here
   * @param sorters
   * @param limit
   * @return
   */
  def List find(String dbCollection, Map<String,Object> query, List<String> fields, Map<String,Integer> sorters, int limit)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create the query
      BasicDBObject theQuery = newQuery(query)
      // query
      DBCursor c = null
      if (fields)
      {
        c = queryCollection.find(theQuery, newReturnFields(fields))
      }
      else
      {
        c = queryCollection.find(theQuery)
      }

      if (sorters)
      {
        BasicDBObject sort = newQuery(sorters)
        c = c.sort(sort)
      }
      if (limit > 0)
      {
        c = c.limit(limit)
      }
	  // Only for debugging.
//      if (c.size() == 0) {
//		  println "No results returned searching mongo collection ${dbCollection} for ${query}"
//      }
      // parse the results
      List result = new ArrayList()
      while (c.hasNext())
      {
        DBObject o = c.next()
        result.push(o.toMap())
      }
      c.close()
      return result
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Find one document matching the query
   *
   * @param dbCollection
   * @param query
   * @param fields
   * @return
   */
  def Map findOne(String dbCollection, Map<String,Object> query, List<String> fields)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create the query
      BasicDBObject theQuery = newQuery(query)
      // query
      return queryCollection.findOne(theQuery, newReturnFields(fields))?.toMap()
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Remove a document (if field is null) or a field from a document
   *
   * @param dbCollection
   * @param query
   * @param field The field to remove or set to null if removing entire document
   * @return
   */
  def remove(String dbCollection, Map<String,Object> query, String field)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create the query
      BasicDBObject deleteQuery = newQuery(query)
      if (field)
      {
        // create the unset query - remove field only
        BasicDBObject unset = new BasicDBObject("\$unset", new BasicDBObject(field, 1))
        queryCollection.update(deleteQuery, unset, false, false)
      }
      else
      {
        // remove document
        queryCollection.remove(deleteQuery)
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Insert into the Mongo collection
   * @param dbCollection
   * @param query
   * @param values
   * @return
   */
  def update(String dbCollection, Map<String,Object> query, Map<String,Object> values)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create a setter
      BasicDBObject newValues = newQuery(values)//new BasicDBObject(values)
      BasicDBObject set = new BasicDBObject("\$set", newValues)
      // add the values
      if (query)
      {
        BasicDBObject theQuery = newQuery(query)
        queryCollection.update(theQuery, set, true, true)
      }
      else
      {
        queryCollection.insert(newValues)
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  def insertDocuments(String dbCollection, List searchKeys, List documents)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      if (searchKeys) {
        documents.each {
          BasicDBObject search = new BasicDBObject()
          searchKeys.each { String sk ->
            search.append(sk, it[sk])
          }
          queryCollection.update(search, new BasicDBObject(it), true, false)
        }
      } else {
        documents.each {
          queryCollection.insert(new BasicDBObject(it))
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  def createIndex(String dbCollection, Map<String,Integer> indexes) {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create the query
      BasicDBObject theQuery = newQuery(indexes)
      // create indexes
      queryCollection.createIndex(theQuery)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Add to a list in a document
   *
   * @param dbCollection
   * @param query
   * @param listName
   * @param newValue
   * @param unique  Set to true if you only want unique values in the list (a.k.a set)
   * @param pushAll Set to true if appending to list
   * @return
   */
  def add(String dbCollection, Map<String,Object> query, String listName, Object newValue, boolean unique, boolean pushAll)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create a setter
      def cmd = unique == true ? "\$addToSet" : (pushAll ? "\$pushAll" : "\$push")
      if (unique && pushAll) {
        newValue = ['$each', newValue]
      }
      BasicDBObject push = new BasicDBObject(cmd, new BasicDBObject(listName, newValue))
      // add the values
      BasicDBObject theQuery = newQuery(query)
      queryCollection.update(theQuery, push, true, true)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  /**
   * Remove an item from a list in a document
   *
   * @param dbCollection
   * @param query
   * @param listName
   * @param oldValue
   * @return
   */
  def drop(String dbCollection, Map<String,Object> query, String listName, Object oldValue)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      // create a setter
      BasicDBObject pull = new BasicDBObject("\$pull", new BasicDBObject(listName, oldValue))
      // add the values
      BasicDBObject theQuery = newQuery(query)
      queryCollection.update(theQuery, pull, true, true)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  private static final int MAX_BATCH_SIZE = 1000
  /**
   * Store a CSV file as a document
   *
   * @param csvFile
   * @param hasHeader
   * @param dbCollection
   * @param ids
   * @return
   */
  def storeCsv(File csvFile, boolean hasHeader, String dbCollection, Map<String,Object> ids)
  {
    try {
      if (csvFile) {
        String filename = csvFile.getName()
        List header = null, rows = []
        csvFile.eachLine { text, i ->
          String[] values = TextTable.splitCsvRow(text)
          if (hasHeader && i == 1)
          {
            header = values.toList()
          }
          else
          {
            rows.push(values.toList())
          }
        }
        if (!rows.isEmpty())
        {
          int numRows = rows.size()
          ids.put("filename", filename)
          Map data = [:]
          if (header) {
            data.put("header", header)
          }
          data.put("numRows", numRows)
          data.put("numCols", rows[0].size())

          int batchMinus = MAX_BATCH_SIZE - 1
          int batchStart = MAX_BATCH_SIZE
          if (numRows > MAX_BATCH_SIZE) {
            data.put("rows", rows[0..batchMinus])
//            batchStart += MAX_BATCH_SIZE
          } else {
            data.put("rows", rows)
          }
          insert(dbCollection, ids, data)

          while (numRows > batchStart) {
            int batchEnd = Math.min(numRows - 1, batchStart + batchMinus)
            add(dbCollection, ids, "rows", rows[batchStart..batchEnd], false, true)
            batchStart += MAX_BATCH_SIZE
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  def boolean exists(String dbCollection, Map<String,Object> query, String key)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      BasicDBObject theQuery = newQuery(query)
      if (key) {
        theQuery.put(key, new BasicDBObject('$exists', true))
      }
      return queryCollection.count(theQuery) > 0
    } catch (Exception e) {
      e.printStackTrace()
    }
    return false
  }

  def int count(String dbCollection, Map<String,Object> query)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = dbCollection ? MongoConnector.getInstance().getCollection(dbCollection) : collection
      BasicDBObject theQuery = newQuery(query)
      return queryCollection.count(theQuery)
    } catch (Exception e) {
      e.printStackTrace()
    }
    return 0
  }

  def logger(Map<String,Object> loggingData)
  {
    try {
      // get the correct collection
      DBCollection queryCollection = MongoConnector.getInstance().getCollection("logger")
      BasicDBObject newValues = new BasicDBObject(loggingData)
      queryCollection.insert(newValues)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }

  MapReduceOutput mapReduce(String dbCollection, String map, String reduce, Map<String,Object> query)
  {
    try {
      // get the correct collection
      DBCollection collection = MongoConnector.getInstance().getCollection(dbCollection)
      if ( collection.count() == 0 )
      {
          return null;
      }
      BasicDBObject theQuery = query ? newQuery(query) : null
      return collection.mapReduce(map, reduce, null, MapReduceCommand.OutputType.INLINE, theQuery)
    } catch (Exception e) {
      e.printStackTrace()
    }
  }


}
