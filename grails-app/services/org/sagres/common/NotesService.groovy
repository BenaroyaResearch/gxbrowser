package org.sagres.common

import org.bson.types.ObjectId
import com.mongodb.MongoException
import com.mongodb.MapReduceOutput
import com.mongodb.MapReduceCommand
import com.mongodb.DBObject

class NotesService {

  def mongoDataService

  Map newNote(String url, List<String> reference, String user, String note, boolean privacy)
  {
	if (!mongoDataService)
	{
		println "mongoData service is false"
	}
    Map errors = [:]
    if (url && reference && note) {
      try {
        Date now = new Date()
        mongoDataService.insert("notes", null, [ createdOn:now, user:user, url:url, tags:reference, note:note, privacy: privacy])
        return null
      } catch (MongoException e) {
        errors.error = true
        errors.message = e.getMessage()
      }
    } else {
      errors.error = true
      errors.message = "Please provide a value for the note."
    }
    return errors
  }

  Map removeNote(String id)
  {
    Map errors = [:]
    if (ObjectId.isValid(id)) {
      try {
        ObjectId noteObjId = new ObjectId(id)
        mongoDataService.remove("notes", [ "_id":noteObjId ], null)
        // also delete its comments
        mongoDataService.remove("comments",[ noteId:noteObjId ], null)
        return null
      } catch (MongoException e) {
        errors.error = true
        errors.message = e.getMessage()
      }
    } else {
      errors.error = true
      errors.message = "No matching note with that ID found."
    }
    return errors
  }

  Map newComment(String noteId, String user, String comment)
  {
    Map error = [:]
    if (ObjectId.isValid(noteId) && comment) {
      try {
        Date now = new Date()
        ObjectId noteObjId = new ObjectId(noteId)
        mongoDataService.insert("comments", null, [ noteId:noteObjId, createdOn:now, user:user, comment:comment ])
        return null
      } catch (MongoException e) {
        error.error = true
        error.message = e.getMessage()
      }
    } else {
      error.error = true
      error.message = comment.isAllWhitespace() ? "Please provide a value for the comment." : "No matching note with that ID found."
    }
    return error
  }

  Map removeComment(String id)
  {
    Map errors = [:]
    if (ObjectId.isValid(id)) {
      try {
        ObjectId commentObjId = new ObjectId(id)
        mongoDataService.remove("comments", [ "_id":commentObjId ], null)
        return null
      } catch (MongoException e) {
        errors.error = true
        errors.message = e.getMessage()
      }
    } else {
      errors.error = true
      errors.message = "No matching comment with that ID found."
    }
    return errors
  }

  Map getNote(String id)
  {
    if (ObjectId.isValid(id)) {
      ObjectId noteObjId = new ObjectId(id)
      Map note = mongoDataService.findOne("notes", [ "_id":noteObjId ], null)
      if (note) {
        note._id = id
      }
      return note
    }
    return null
  }

  List getAllNotes(boolean returnComments)
  {
    return getNotes(null, null, [ "createdOn":-1 ], 0, returnComments)
  }

  List getNotes(String reference, String url, user, privacy, boolean returnComments)
  {
    Map query = [:]
    if (reference) {
      query.tags = reference
    }
    if (url) {
      query.url = url
    }
	List notes = getNotes(query, null, [ "createdOn":-1 ], -1, returnComments)
	
	//return notes available to this user
	return notes.findAll { it.user == user || !it.privacy }
  }

  List getNotes(Map<String,Object> query, List<String> fields, Map<String,Integer> sorters, int limit, boolean returnComments)
  {
    List notes = mongoDataService.find("notes", query, fields, sorters, limit)
    if (returnComments) {
      notes.each { Map note ->
        note._id = note._id.toString()
        List comments = mongoDataService.find("comments", [ noteId:note._id ], null, [ "createdOn":-1 ], -1)
        if (comments) {
          note.comments = comments
        }
      }
    } else {
      notes.each { Map note ->
        note._id = note._id.toString()
      }
    }
    return notes
  }

  Map getComment(String id)
  {
    if (ObjectId.isValid(id)) {
      ObjectId commentObjId = new ObjectId(id)
      Map comment = mongoDataService.findOne("comments", [ "_id":commentObjId ], null)
      if (comment) {
        comment._id = id
      }
      return comment
    }
    return null
  }

  List getComments(String noteId, int limit)
  {
    if (ObjectId.isValid(noteId)) {
      ObjectId noteObjId = new ObjectId(noteId)
      return getComments([ noteId:noteObjId ], null, [ "createdOn":-1 ], -1)
    }
    return null
  }

  List getComments(Map<String,Object> query, List<String> fields, Map<String,Integer> sorters, int limit)
  {
    List comments = mongoDataService.find("comments", query, fields, sorters, limit)
    comments.each { Map comment ->
      comment._id = comment._id.toString()
    }
    return comments
  }

  List filterNotes(Map<String,List> filterReq, List<String> fields, Map<String,Integer> sorters, int limit)
  {
    Map query = buildFilter(filterReq)
    List notes = mongoDataService.find("notes", query, fields, sorters, limit)
    return notes
  }

  private Map buildFilter(Map<String,List> filterReq)
  {
    Map query = [:]
    filterReq.each { String key, List values ->
      if (key == "createdOn")
      {
        Map dateFilter = [:]
        if (values[0])
        {
          dateFilter.put('$gte', Date.parse("MM/dd/yyyy", values[0]))
        }
        if (values[1])
        {
          if (values[1] == values[0])
          {
            dateFilter.put('$lt', Date.parse("MM/dd/yyyy", values[1]).next())
          }
          else
          {
            dateFilter.put('$lte', Date.parse("MM/dd/yyyy", values[1]))
          }
        }
        if (!dateFilter.isEmpty())
        {
          query[key] = dateFilter
        }
      }
      else
      {
        if (values.size() == 1)
        {
          query[key] = values.get(0)
        }
        else if (values.size() > 0)
        {
          query[key] = [ '$in':values ]
        }
      }
    }
    return query
  }

  Map getFilterOptions() {
    List tags = [], users = []
    List options = mongoDataService.find("notes", null, [ "tags", "user" ], null, -1)
    if (options)
    {
      options.each { Map note ->
        tags.addAll(note.tags)
        users.push(note.user)
      }
      return [ tags:tags.unique(), users:users.unique() ]
    }
    return null
  }

  Map tagCounts(Map<String,Object> query) {
    String map = """function() {
        if (!this.tags) { return; }
        for (index in this.tags) { emit(this.tags[index], 1); }
      }""".toString()
    String reduce = """function(prev, cur) {
        var count = 0;
        for (index in cur) { count += cur[index]; }
        return count;
      }"""
    MapReduceOutput out = mongoDataService.mapReduce("notes", map, reduce, query)
    Map counts = [:]
    if ( out )
    {
      out.results().each { DBObject obj ->
        def tag = obj.get("_id")
        def count = obj.get("value")
        counts.put(tag, count)
      }
    }
    return counts
  }

}
