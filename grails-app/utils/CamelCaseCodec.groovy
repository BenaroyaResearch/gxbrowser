class CamelCaseCodec {
  static encode = { text ->
    String[] words = ((String)text).split(" ").collect { word ->
      word.toLowerCase().capitalize()
    }
    words[0] = words[0].toLowerCase()
    return words.join("")
  }
}