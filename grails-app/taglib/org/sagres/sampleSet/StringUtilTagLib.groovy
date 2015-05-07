package org.sagres.sampleSet

class StringUtilTagLib {
  def hintize = { attrs ->
    if (!attrs["value"])
    {
      out << "<span class='hint'>${grailsApplication.config.hint.default.text}</span>"
    }
    else
    {
      out << attrs["value"]
    }
  }

  def abbreviate = { attrs ->
    def value = attrs["value"]
    if (value)
    {
      // max length is defaulted to 50 characters
      def maxLength = attrs["maxLength"] ?: 50
      if (maxLength instanceof String)
      {
        maxLength = Integer.parseInt(maxLength)
      }
      String text = ""
      if (value instanceof Collection)
      {
        int usedValues = 0
        value.each {
          def newText = it.toString()
          def newTextLength = usedValues == 0 ? newText.length() : text.length() + 2 + newText.length()
          if (newTextLength < maxLength)
          {
            if (text.length() > 0)
            {
              text += ", "
            }
            text += newText
            usedValues++
          }
        }
        if (text == "" && value.size() > 0)
        {
          def firstValue = value[0].toString()
          def maxIndex = Math.min(maxLength, firstValue.length())
          text = firstValue.substring(0, maxIndex)
        }
        if (usedValues < value.size())
        {
          text += " ..."
        }
      }
      else
      {
        def maxIndex = Math.min(maxLength, ((String)value).length())
        text = ((String)value).substring(0, maxIndex)
        if (maxLength < ((String)value).length())
        {
          text += " ..."
        }
      }
      out << text
    }
    else
    {
      // null value or empty string
      if (attrs["hint"])
      {
        out << '<span class="hint">'
        if (attrs["hint"])
        {
          out << attrs["hint"]
        }
        else
        {
          out << grailsApplication.config.hint.default.text
        }
        out << '</span>'
      }
      else
      {
        out << '&nbsp;'
      }
    }
  }

  def fixBlank = { attrs ->
    if (!attrs["value"])
    {
      out << '<span class="hint">'
      if (attrs["hint"])
      {
        out << attrs["hint"]
      }
      else
      {
        out << grailsApplication.config.hint.default.text
      }
      out << '</span>'
    }
  }
}
