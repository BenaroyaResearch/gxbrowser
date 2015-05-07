package org.sagres.sampleSet

class HtmlElementsTagLib {

  def selectField = { attrs ->
    def selectClass = attrs.class ? " class='${attrs.class}'" : ""
    out << "<select name='${attrs.name}'${selectClass} id='${attrs.name}'>"
    attrs.from.each {
      if (it != attrs.exclude)
      {
        out << "<option value='${attrs.optionKey == "id" ? it.id : it.getPersistentValue(attrs.optionKey)}'>${it.getPersistentValue(attrs.optionValue)}</option>"
      }
    }
    out << "</select>"
  }

  def spinnerField = { attrs ->
    out << "<div class='spinner'>"
    out << "<button class='spin-button decrease ui-button ui-state-default ui-corner-left generic-button-left' style='margin-right: 0.2em'><div class='ui-icon ui-icon-circle-triangle-w'></div></button>"
    out << textField(name: attrs.name, class: 'spinner-text')
    out << "<button class='spin-button increase ui-button ui-state-default ui-corner-right generic-button-left'><div class='ui-icon ui-icon-circle-triangle-e'></div></button>"
    out << "<button id='save'>Save</button>"
    out << "</div>"
  }

  def confirmDialog = { attrs ->
    out << render(template: "/common/confirmationDialogTemplate", model:[message:attrs.message, form:attrs.form])
  }

  def namingConvention = {
    out << select(name: "namingConvention", from:['Appending column name to group name', 'Using column name'])
  }

  def draggableList = { attrs ->
    out << render(template: "/common/sortableListTemplate", model:[name:attrs.name, items:attrs.from, optionKey:attrs.optionKey, optionValue:attrs.optionValue, isEmpty:attrs.empty])
  }

}
