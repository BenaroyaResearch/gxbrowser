package org.sagres.common

import org.apache.jasper.tagplugins.jstl.core.Catch;

import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.ArticleType
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.AuthorType
import gov.nih.nlm.ncbi.www.soap.eutils.EFetchPubmedServiceStub.MedlineCitationType
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.LinkSetDbType
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.LinkSetType
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.LinkTypeE
import gov.nih.nlm.ncbi.www.soap.eutils.EUtilsServiceStub.ItemType;

class NcbiQueryService {

  static transactional = true

  def queryGEO(String geoId)
  {
    def geoxml = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=${geoId}&form=xml&view=quick"
    def xml = geoxml.toURL().text
    def fullXml = new XmlParser().parseText(xml)
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
	def geoxml = "http://www.ncbi.nlm.nih.gov/geo/query/acc.cgi?acc=${geoId}&form=xml&view=quick"
	def xml = geoxml.toURL().getText()
	def fullXml = new XmlParser().parseText(xml)
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
	try {
		EUtilsServiceStub service = new EUtilsServiceStub()
		EUtilsServiceStub.ESummaryRequest sumReq = new EUtilsServiceStub.ESummaryRequest()
		sumReq.setDb("gene")
		sumReq.setId(geneId)
		EUtilsServiceStub.ESummaryResult sumRes = service.run_eSummary(sumReq)
		sumRes.getDocSum()[0].getItem().each { field ->
			if (field.itemContent) {
				geneInfo.put(field.name, field.itemContent)
			}
		}
	} catch (Exception e) {
		println "Exception in ncbiQueryService queryGene ($geneId) - ${e.toString()}"
	}

// geneInfo.put("numArticles", getGeneLinks(geneId))

    return geneInfo
  }

  Map searchGene(String geneSymbol)
  {
    Map geneInfo = [:]
    String webEnv = null, queryKey = null
    try
    {
      EUtilsServiceStub service = new EUtilsServiceStub()
      EUtilsServiceStub.ESearchRequest req = new EUtilsServiceStub.ESearchRequest()
      req.setDb("pubmed")
      req.setTerm(geneSymbol)
      req.setSort("PublicationDate")
      req.setUsehistory("u")

      EUtilsServiceStub.ESearchResult res = service.run_eSearch(req)
      geneInfo.put("numArticles", res.count)
      webEnv = res.getWebEnv()
      queryKey = res.getQueryKey()
    }
    catch (Exception e) {
      println "Exception in ncbiQueryService searchGene::numArticles ($geneSymbol) - ${e.toString()}"
    }

    if (webEnv && queryKey)
    {
      try
      {
        List articles = []

        EFetchPubmedServiceStub service = new EFetchPubmedServiceStub()
        EFetchPubmedServiceStub.EFetchRequest fReq = new EFetchPubmedServiceStub.EFetchRequest()
        fReq.setWebEnv(webEnv)
        fReq.setQuery_key(queryKey)
        fReq.setRetmax("25")

        EFetchPubmedServiceStub.EFetchResult fRes = service.run_eFetch(fReq);
        for (int i = 0; i < fRes.getPubmedArticleSet().getPubmedArticleSetChoice().length; i++)
        {
          EFetchPubmedServiceStub.PubmedArticleType art = fRes.getPubmedArticleSet().getPubmedArticleSetChoice()[i].getPubmedArticle();
          EFetchPubmedServiceStub.PubmedBookArticleType book = fRes.getPubmedArticleSet().getPubmedArticleSetChoice()[i].getPubmedBookArticle();
          if (art) {
            MedlineCitationType citation = art.getMedlineCitation()
            ArticleType article = citation.getArticle()
            Map articleInfo = [ pmid: citation.getPMID().string, title: article.getArticleTitle().string ]

            EFetchPubmedServiceStub.ArticleDateType[] date = article.getArticleDate()
            if (date?.length > 0) {
              EFetchPubmedServiceStub.ArticleDateType pubmedDate = date[0]
              articleInfo.pubYear = pubmedDate.year
            } else {
              articleInfo.pubYear = citation.getDateRevised() ? citation.getDateRevised().year : citation.getDateCreated().year
            }
            articles.push(articleInfo)

          } else if (book) {
            EFetchPubmedServiceStub.BookDocumentType bk = book.getBookDocument()
            Map bookInfo = [ pmid: bk.getPMID().string, title: bk.getArticleTitle().string, pubYear:bk.getContributionDate().year ]
            articles.push(bookInfo)
          }
        }

        geneInfo.articles = articles
      }
      catch (Exception e)
      {
          println "Exception in ncbiQueryService searchGene::fetchArticles ($geneSymbol) - ${e.toString()}"
        //e.printStackTrace()
      }
    }

    return geneInfo
  }

  List getGeneLinks(String geneId)
  {
	List<String> ids = []
	
    try {
      String[] gid = new String[1]
      gid[0] = geneId
      EUtilsServiceStub service = new EUtilsServiceStub()
      EUtilsServiceStub.ELinkRequest linkReq = new EUtilsServiceStub.ELinkRequest()
      linkReq.setDbfrom("gene")
      linkReq.setDb("pubmed")
      linkReq.setId(gid)

      EUtilsServiceStub.ELinkResult linkRes = service.run_eLink(linkReq)
      linkRes.getLinkSet().each { LinkSetType lSet ->
        lSet.getLinkSetDb().each { LinkSetDbType ldb ->
          if (ldb.getLinkName() == "gene_pubmed") {
            ldb.getLink().each { LinkTypeE link ->
              ids.push(link.getId().getString())
            }
          }
        }
      }
    } catch (Exception e) {
      println "Exception in ncbiQueryService getGeneLinks ($geneId) - ${e.toString()}"
    }
    return ids
  }

  List getArticles(List<String> ids, int limit)
  {
    List results = []
	Map<String, Set<String>> authors = new HashMap<String, Set<String>>();
	
    try
    {
      EUtilsServiceStub fetchService = new EUtilsServiceStub()
      EUtilsServiceStub.ESummaryRequest req = new EUtilsServiceStub.ESummaryRequest()
      if (limit > 0) {
		  req.setRetmax(Integer.toString(limit))
      }
	  req.setDb("pubmed")
      req.setId(ids.join(","))
	  
      EUtilsServiceStub.ESummaryResult res = fetchService.run_eSummary(req)
	  
	  for (int i = 0; i < res.getDocSum().length; i++ ) {
		  //println "ID: " + res.getDocSum()[i].getId()
		  Map document = [:]
		  
		  res.getDocSum()[i].getItem().each { field ->
			  if (field.itemContent) {
				  if (field.name == 'PubDate') {
					  def (year, rest) = field.itemContent.tokenize(' ')
					  document.put(field.name, year)
				  } else {
				  	document.put(field.name, field.itemContent)
				  }
			  }
		  }
		  document.put('pmid', res.getDocSum()[i].getId())
		  
//		  for (int k = 0; k < res.getDocSum()[i].getItem().length; k++) {
//			  println "field: " + res.getDocSum()[i].getItem()[k].getName() + " value: " + res.getDocSum()[i].getItem()[k].getItemContent()
//			  if(res.getDocSum()[i].getItem()[k].getName().equals("AuthorList")){
//				  Set<String> auths = new HashSet<String>();
//				  if(res.getDocSum()[i].getItem()[k]!=null){
//					  ItemType[] items = res.getDocSum()[i].getItem()[k].getItem();
//					  if(items!=null){
//						  for(int a = 0; a<items.length; a++){
//							  auths.add(items[a].getItemContent());
//						  }
//						  println "authors: " + auths
//					  }
//				  }
//			  }
//		  }
		  
		  if (document) {
			  results.push(document)
		  }
	  }
    }
    catch (Exception e)
    {
      // unable to retrieve info for some reason
	  println "Exception in ncbiQueryService getArticles() - ${e.toString()}"
    }

    return results
  }


}
