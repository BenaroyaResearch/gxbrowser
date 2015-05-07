class ParagraphCodec {
  static encode = { str ->
    return ((String)str).replaceAll('(\r)*\n', '<br/>')
  }
}
