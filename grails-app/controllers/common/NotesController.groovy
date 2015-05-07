package common

import grails.converters.JSON
import org.sagres.importer.TextTable
import org.sagres.importer.TextTableSeparator

class NotesController {

  def springSecurityService
  def notesService

  def index = {
    redirect(action:"list")
  }

  def list = {
    List notes = null, tags = [], users = []
	SecUser currentUser = springSecurityService.currentUser
    Map filterReq = [:]
    if (params.filter == "true")
    {
      [ "createdOn", "tags", "user" ].each {
        List values = request.getParameterValues(it)?.toList()
        if (values && !values.isEmpty()) {
          filterReq[it] = values
        }
      }
      notes = notesService.filterNotes(filterReq, null, [ "createdOn":-1 ], 0)
      Map filterOptions = notesService.getFilterOptions()
      tags = filterOptions?.tags ?: []
      users = filterOptions?.users ?: []
    }
    else
    {
      notes = notesService.getAllNotes(false)
      notes.each { Map note ->
        tags.addAll(note.tags)
        users.add(note.user)
      }
    }
    return [ notes:notes, tags:tags.unique().sort(), users:users.unique().sort(), selectedFilters:filterReq, currentUser: currentUser]
  }

  def create = {
    String url = params.url
    String reference = params.reference
    String user = getUser()
    String note = params.note
	boolean privacy = (params.privacy == 'true') ? true : false
	//println "Params privacy: " + params.privacy + " private: " + privacy
    if (user) {
      List<String> tags = TextTable.splitRow(reference, TextTableSeparator.CSV)
      Map msg = notesService.newNote(url, tags, user, note, privacy)
      if (msg) {
        render msg as JSON
      }
    }
    render ""
  }

  def deleteNote = {
    String noteObjectId = params.noteId
    if (getUser()) {
      Map msg = notesService.removeNote(noteObjectId)
      if (msg) {
        render msg as JSON
      }
    }
    render ""
  }

  def addComment = {
    String noteObjectId = params.noteId
    String user = getUser()
    String comment = params.comment
    if (user) {
      Map msg = notesService.newComment(noteObjectId, user, comment)
      if (msg) {
        render msg as JSON
      }
    }
    render ""
  }

  def deleteComment = {
    String commentObjectId = params.id
    if (getUser()) {
      Map msg = notesService.removeComment(commentObjectId)
      if (msg) {
        render msg as JSON
      }
    }
    render ""
  }

  def getNotes = {
    String url = params.url
    String reference = params.reference
    boolean returnComments = params.returnComments ?: false
    if (url || reference) {
      List notes = notesService.getNotes(reference, url, getUser(), true, returnComments)
      if (notes) {
        render notes as JSON
      }
    }
    render ""
  }

  def getComments = {
    String noteId = params.noteId
    int limit = params.int("limit") ?: -1
    List comments = notesService.getComments(noteId, limit)
    if (comments) {
      render comments as JSON
    }
    render ""
  }

  private String getUser() {
    if (loggedIn)
    {
      SecUser user = springSecurityService.currentUser
      return user?.username
    }
    return null
  }

}
