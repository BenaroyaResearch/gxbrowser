package org.sagres.common

class ConverterService {

  def docToHTML(String docText) {
    return docToHTML(docText, false)
  }

  def docToHTML(String docText, boolean plainText) {
    return cleanDoc(docText, plainText)
  }

  private def cleanDoc(String text, boolean plainText) {
    def html = text.trim().replaceAll("\\s+", " ").trim()
    def matcher = (html =~ /(?i)(.*<!--StartFragment-->)(.*)(<!--EndFragment-->.*)/)
    if (matcher.matches())
    {
      def fragment = matcher.group(2)
      matcher = (fragment =~ /(?i)<!--(StartFragment|EndFragment|(\[(!|\w|\s)+\]))-->/)
      fragment = matcher.replaceAll("")
      html = fragment
    }
    matcher = (html =~ /(?i)<!--.*?-->/)
    html = matcher.replaceAll(" ").trim()
    matcher = (html =~ /(?i)(\s+)?(&nbsp;)+(\s+)?/)
    html = matcher.replaceAll(" ").trim()
    matcher = (html =~ /(?i)<style>(.*)<\/style>/)
    html = matcher.replaceAll("").trim()
    if (!html.startsWith("<") && html.size() > 0)
    {
      matcher = (html =~ /(?i)<br>/)
      html = "<p>${matcher.replaceAll("</p><p>")}</p>"
    }
    matcher = (html =~ /(?i)<(meta|link|h[1-5]|\/?o:|\/?w:|\/?m:|\/?style|\/?font|\/?st\d|\/?b|\/?a|\/?i|\/?u|\/?head|\/?html|body|\/?body|\/?span|!\[)[^>]*?>/)
    html = matcher.replaceAll("")
    matcher = (html =~ /(?i)<(p|div)[^>]*?>(\s+)?/)
    html = matcher.replaceAll("<p>")
    matcher = (html =~ /(?i)(\s+)?<(\/div)[^>]*?>/)
    html = matcher.replaceAll("</p>")
    def lists = ["ul", "ol", "dl", "li"]
    lists.each {
      matcher = (html =~ /<(${it})[^>]*?>(\s+)?/)
      html = matcher.replaceAll("<${it}>")
    }
    matcher = (html =~ /(?i)(<[^>]+>)+&nbsp;(<\/\w+>)+/)
    html = matcher.replaceAll("")
    matcher = (html =~ /(?i)<p>(\s*)<\/p>/)
    html = matcher.replaceAll("").trim()
    if (!html.startsWith("<") && html.size() > 0)
    {
      def close = html.indexOf("</")
      def tag = html.indexOf("<", 1)
      if (close >= tag)
      {
        html = "<p>${html}"
        if (close > tag)
        {
          html = new StringBuffer(html).insert(tag+3, "</p>").toString()
        }
      }
    }
    if (plainText)
    {
      matcher = (html =~ /<\/?(\w+)[^>]*?>/)
      html = matcher.replaceAll("")
    }
//    if (html.allWhitespace)
//    {
//      return null
//    }
    return html
  }
}
