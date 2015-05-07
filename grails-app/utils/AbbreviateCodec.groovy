class AbbreviateCodec {

  static encode = { String text ->
    int len = text.length()
    if (len > 50) {
      return "${text.substring(0,25)}...${text.substring(len-25,len)}"
    } else {
      return text
    }
  }

}
