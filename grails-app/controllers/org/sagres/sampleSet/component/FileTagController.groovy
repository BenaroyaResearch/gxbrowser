package org.sagres.sampleSet.component

import grails.converters.JSON

class FileTagController {

  static scaffold = FileTag

  def sampleFileList = {
    def fileTagInstance = FileTag.get(params.id)
    render fileTagInstance.sampleSetFiles as JSON
    return null
  }

  def browse = {
    [fileTagInstanceList: FileTag.list(), fileTagInstanceTotal: FileTag.count()]
  }

  def add = {
    def fileTagInstance = new FileTag(tag: params.name)
    fileTagInstance.save()
    render(view:"browse", model:[fileTagInstance:fileTagInstance, fileTagInstanceList: FileTag.list(), fileTagInstanceTotal: FileTag.count()])
  }

  def tagList = {
    if (params.term == "")
    {
      render FileTag.all.collect { tag -> tag.tag } as JSON
    }
    else
    {
      render FileTag.findAllByTagIlike("%${params.term}%").collect { tag -> tag.tag } as JSON
    }
    return null
  }

}
