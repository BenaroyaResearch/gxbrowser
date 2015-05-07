package org.sagres.util.mongo

import com.mongodb.Mongo
import org.codehaus.groovy.grails.commons.ConfigurationHolder
import com.mongodb.BasicDBObject

class MongoConnector {

  private static MongoConnector INSTANCE = new MongoConnector()
  def mongoConnection
  def mongoDb
  def collection

  def MongoConnector()
  {
    // initialize with defaults
    mongoConnection = new Mongo(ConfigurationHolder.config.mongodb.host, ConfigurationHolder.config.mongodb.port)
    mongoDb = mongoConnection.getDB(ConfigurationHolder.config.mongodb.databasename)
    if (ConfigurationHolder.config.mongodb.username)
    {
      def username = ConfigurationHolder.config.mongodb.username
      def password = ConfigurationHolder.config.mongodb.password
      mongoDb.authenticate(username, password.toCharArray())
    }
    collection = mongoDb.getCollection(ConfigurationHolder.config.mongo.sample.collection)
    def index = new BasicDBObject("sampleId", 1)
    index.append("sampleSetId", 1)
    collection.createIndex(index)
  }

  def static getInstance()
  {
    return INSTANCE
  }

  def getDatabase()
  {
    return mongoDb
  }

  def getDatabase(String name)
  {
    return mongoConnection.getDB(name)
  }

  def getCollection()
  {
    return collection
  }

  def getCollection(String collection)
  {
    return mongoDb.getCollection(collection)
  }

  def getCollection(String db, String collection)
  {
    return getDatabase(db).getCollection(collection)
  }

}
