class KeyCaseCodec {
  static encode = { text ->
    return ((String)text).toLowerCase().replaceAll("\\s+","_").replaceAll("\\.","")
  }
}