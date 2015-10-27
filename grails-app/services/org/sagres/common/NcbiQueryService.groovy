package org.sagres.common

import org.sagres.util.SagresException

class NcbiQueryService {

  def eutilsBase = "http://eutils.ncbi.nlm.nih.gov/entrez/eutils"
  def geoBase = "http://www.ncbi.nlm.nih.gov/geo/query"
  
  def queryGEO(String geoId)
  {
    def geoURL = geoBase + "/acc.cgi?acc=${geoId}&form=xml&view=quick"
    def geoXML = geoURL.toURL().text
    def fullXml = new XmlParser().parseText(geoXML)
    def sample = fullXml.Sample
    def summaryResult = [:]
    summaryResult.put("Title", sample.Title.text())
    summaryResult.put("Sample Type", sample.Type.text())
    // get characteristics
    sample.Channel.each { channel ->
      def position = channel.'@position'
      summaryResult.put("Channel ${position} Source", channel.Source.text())
      channel.Characteristics.each {
        def tag = it.'@tag' ?: "Info"
        def key = "Channel ${position} ${tag.encodeAsHumanize()}"
        if (summaryResult.containsKey(key))
        {
          summaryResult.put(key, summaryResult.get(key) + ", " + it.text())
        }
        else
        {
          summaryResult.put(key, it.text())
        }
      }
    }
    return summaryResult
  }

  def queryGEOSeries(String geoId)
  {
	def geoURL = geoBase + "/acc.cgi?acc=${geoId}&form=xml&view=quick"
	def geoXML = geoURL.toURL().getText()
	def fullXml = new XmlParser().parseText(geoXML)
	def series = fullXml.Series
	def seriesResult = [:]
	seriesResult.put("title", series.Title.text())
	seriesResult.put("summary", series.Summary.text())
	seriesResult.put("accession", series.Accession.text())
	return seriesResult
  }

  Map queryGene(String geneId)
  {
    Map geneInfo = [:]
	def summaryURL = eutilsBase + "/esummary.fcgi?db=gene&id=${geneId}"
	def summaryXML = summaryURL.toURL().getText()
	def parser = new XmlSlurper()
	parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
	def fullXml = parser.parseText(summaryXML)
	def dsummary = fullXml.DocumentSummarySet.DocumentSummary
	if (dsummary) {
		geneInfo.put("Name", dsummary.Name.text())
		geneInfo.put("Summary", dsummary.Summary.text())
		geneInfo.put("Description", dsummary.Description.text())
	} else {
		println "queryGene: no document (gene info) for ${geneId}..."
	}
	
	return geneInfo
  }


  List getGeneLinks(String geneId)
  {
	  List<String> geneLinks = []
	  def linksURL = eutilsBase + "/elink.fcgi?dbfrom=gene&db=pubmed&id=${geneId}"
	  def linksXML = linksURL.toURL().getText()
	  def parser = new XmlSlurper()
	  parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
	  def fullXml = parser.parseText(linksXML)
	  def linkList = fullXml.LinkSet.LinkSetDb
	  if (linkList) {
		  geneLinks = linkList.Link.Id.findAll{ node -> node.name()}*.text()
	  } else {
	  	  println "getGeneLinks: no links (to pubmed) for ${geneId}..."
	  }
	  
	  return geneLinks
  }

  List getArticles(List<String> ids, int limit = 25) throws SagresException
  {
	  List<String> pids = ids
	  List results		= []

	  // This code chops it off the list of pubmed ids, before it goes to the server (for speed)
	  // An alternative would be to ask for all ids, order by year, and then take the top 25/limits.
	  if (ids.size() > limit) {
		  pids = ids[0..limit-1]
	  }
	  def summaryURL = eutilsBase + "/esummary.fcgi?db=pubmed&id=" + pids.join(",")
	  def summaryXML = summaryURL.toURL().getText()
	  def parser = new XmlSlurper()
	  parser.setFeature("http://apache.org/xml/features/disallow-doctype-decl", false)
	  def fullXml = parser.parseText(summaryXML)
	  def docList = fullXml

	  if (docList) {
		  def count = 0
		  docList.'*'.each { doc ->  // breath first search into DocSum
			  if (doc.name() == 'DocSum') {
				  count++
				  //println "stepping down with : '" + doc.name() + "' count: " + count
				  def document = [:]
				  doc.'**'.findAll { node ->  // now looking at all Ids/Items in that document
					//println "    name: " + node.name() + ", text: " + node.text()
					if (node.name() == "Id") {
						document.put('pmid', node.text())
						def id = node.text()
						//println "pmid: " + id
					}
					if (node.name() == "Item") {
						if (node['@Name'] == "PubDate") {
							def (year, rest) = node.text().tokenize(' ')
							document.put('PubDate', year)
						} else if (node['@Name'] == "Title") {
							document.put('Title', node.text())
						}
					}
				  }
				  if (document) {
					  results.push(document)
				  }
			  }
		  }
		  if (count == 0)
		  {
			  throw new SagresException('getArticles: docList does not contain any DocSum')
		  }
	  }	else {
			println "getArticles: no summary for link list..."
	  }

	  return results
  }

}
