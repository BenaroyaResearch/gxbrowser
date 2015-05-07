class HumanizeCodec {
  static encode = { String text ->
    if (text == "") {
      return text
    }

    return text.split("_").collect { String w ->
      if (w.equalsIgnoreCase("id"))
      {
        return "ID"
      }
      else
      {
        w.capitalize()
      }
    }.join(" ")
  }
}