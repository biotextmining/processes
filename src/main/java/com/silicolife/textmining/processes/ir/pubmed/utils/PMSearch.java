package com.silicolife.textmining.processes.ir.pubmed.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.PublicationFieldTypeEnum;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.lables.PublicationLabelImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.PublicationFieldImpl;
import com.silicolife.textmining.core.datastructures.exceptions.PubmedException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.pubmed.PubmedReader;

public class PMSearch {
	
	private static long lastQuery = System.currentTimeMillis();
	public static final int timeToWaitbetweenQueries = 3000;
	public static final int numberOFRetries = 5;
//	public static final String pubmedSourceName = "PUBMED";
//	public static final String doiSourceName = "doi";
//	public static final String pmcSourceName = "pmc";
	public static final String pubmedLink = "https://www.ncbi.nlm.nih.gov/pubmed/";
	public static int timeoutManager = 300000;
	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(PMSearch.class.getName());
	static{
		Logger.getLogger(HttpMethodBase.class).setLevel(Level.OFF);	
	}
	
	

	public static List<IPublication> getPublicationByQuery(String query) throws InternetConnectionProblemException
	{
		ESearchContext context = query(query);
		return getPubmedArticlesInRange(context.getWebEnv(), context.getQueryKey(), 0, context.getCount());
	}
	
	private static List<IPublication> getPubmedArticlesInRange(String webEnv, String queryKey, int indexMin, int step) throws InternetConnectionProblemException {
		waitIfNecessary();
		HttpClient client = getHTTPGeneralConfiguration();
		PostMethod post = configurationHttp(webEnv, queryKey, indexMin,step);
		return getPubmedResults(client, post);		
	}
	
	private static List<IPublication> getPubmedResults(HttpClient client,PostMethod post) throws InternetConnectionProblemException{
		int retries = 0;
		while(retries < numberOFRetries)
		{
			try {
				client.executeMethod(post);
				return readXMLResultFile(post);
			} catch (ANoteException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}catch (IOException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}	
		}
		throw new InternetConnectionProblemException(new PubmedException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.pubmed.search.err.searchnotcomplete")));
	}

	private static HttpClient getHTTPGeneralConfiguration() {
	
//		Builder configuration = RequestConfig.custom();
//		configuration.setConnectTimeout(timeoutManager);
//		configuration.setCookieSpec(CookieSpecs.DEFAULT);
//
//		if (InitConfiguration.getProxy() != null && !InitConfiguration.getProxy().type().equals(Type.DIRECT)) {
//			Proxy proxy = InitConfiguration.getProxy();
//			configuration.setProxy(new HttpHost(proxy.toString()));
//		}
//
//		RequestConfig defaultRequestConfig = configuration.build();
//		CloseableHttpClient httpclient = HttpClients.custom().setDefaultRequestConfig(defaultRequestConfig).build();
//
//		return httpclient;
		
		
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());
		client.getParams().setConnectionManagerTimeout(timeoutManager);
		client.getParams().setSoTimeout(timeoutManager);
		return client;
	}
	
	private static void waitIfNecessary() {

		while (System.currentTimeMillis() - lastQuery < timeToWaitbetweenQueries) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		lastQuery = System.currentTimeMillis();
	}
	
	private static PostMethod configurationHttp(String webEnv, String queryKey,
			int start, int max) {

		PostMethod post = new PostMethod(
				"https://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi/");

		NameValuePair[] data = { 
				new NameValuePair("db", "pubmed"),
				new NameValuePair("WebEnv", webEnv),
				new NameValuePair("query_key", queryKey),
				new NameValuePair("retstart", Integer.toString(start)),
				new NameValuePair("retmax", Integer.toString(max)),
				new NameValuePair("retmode", "xml") 
		};
		post.setRequestBody(data);
		return post;
	}
	
	public static List<IPublication> readXMLResultFile(PostMethod post) throws ANoteException, IOException{
		return new PubmedReader().getPublications(post.getResponseBodyAsStream());
	}

	@Deprecated
	public static List<IPublication> getPublicationsFromXMLStream(InputStream stream)
			throws SAXException, IOException, ParserConfigurationException, XPathExpressionException {
		List<IPublication> publications = new ArrayList<IPublication>();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		XPathFactory factory = XPathFactory.newInstance();
		XPath xpath = factory.newXPath();
		NodeList nodes = doc.getElementsByTagName("MedlineCitation");
		NodeList nodesPubMed = doc.getElementsByTagName("PubmedData");
//		XPathExpression meshExpresion = xpath.compile("MeshHeadingList/MeshHeading/DescriptorName");
		XPathExpression lastNameExpresion = xpath.compile("Article/AuthorList/Author/LastName");
//		XPathExpression foreNameExpresion = xpath.compile("Article/AuthorList/Author/ForeName");
		String title,authorList,abstractText,status,journal,pages,volume,issues,date;
		IPublication pub;
		// XPathExpression initialsExpression =
		// xpath.compile("Article/AuthorList/Author/Initials");
		for (int j = 0; j < nodes.getLength(); j++) {
			List<IPublicationField> fullTextfields = new ArrayList<IPublicationField>();
			Set<String> publicationFiledsAlreayAdded = new HashSet<>();
			Element elements = (Element) nodes.item(j);
			Element pubElements = (Element) nodesPubMed.item(j);
			NodeList node;
			// Pubid
			// String pubid= getXMLElemnt(nodes.item(j),"PMID");
			node = elements.getElementsByTagName("PMID");
			String pubmedID = node.item(0).getTextContent();
			List<IPublicationExternalSourceLink> externalIDsSource = new ArrayList<IPublicationExternalSourceLink>();
			externalIDsSource.add(new PublicationExternalSourceLinkImpl(pubmedID, PublicationSourcesDefaultEnum.PUBMED.name()));
			
			// External IDs 

			NodeList articleIDs = pubElements.getElementsByTagName("ArticleId");
			for(int i=0;i<articleIDs.getLength();i++)
			{
				if (articleIDs.item(i) != null) {

					String internalID = articleIDs.item(i).getTextContent().toLowerCase();
					String source = articleIDs.item(i).getAttributes().getNamedItem("IdType").getTextContent();
					if(internalID.length()>3 && source.equalsIgnoreCase("pmc"))
					{
						externalIDsSource.add(new PublicationExternalSourceLinkImpl(internalID.toLowerCase(), source));
					}
					else if(internalID.startsWith("10.") && source.equalsIgnoreCase("doi"))
					{
						externalIDsSource.add(new PublicationExternalSourceLinkImpl(internalID.toLowerCase(), source));
					}
				}
			}
			
			// Article Title
			node = elements.getElementsByTagName("ArticleTitle");
			title = node.item(0).getTextContent();
			title = NormalizationForm.removeOffsetProblemSituation(title);
			
			// Author List
			authorList = new String();
			NodeList lastNameNode = (NodeList) lastNameExpresion.evaluate(nodes.item(j), XPathConstants.NODESET);
			NodeList initialsNode = (NodeList) elements.getElementsByTagName("Initials");

			if (initialsNode.item(0) != null) {
				for (int x = 0; x < initialsNode.getLength(); x++) {
					if (lastNameNode.item(x) == null)
						break;
					authorList += lastNameNode.item(x).getTextContent()
							+ " "
							+ initialsNode.item(x).getTextContent()
							+ ", ";
				}
				if (authorList.length() >= 1)
					authorList = authorList.substring(0, authorList.length() - 2);
			}

			// Abstract
			node = elements.getElementsByTagName("AbstractText");
			abstractText = new String();
			for(int i=0;i<node.getLength();i++)
			{
				if (node.item(i) != null) {
					int startindex = abstractText.length();
					String abstractParagraph = node.item(i).getTextContent();
					abstractParagraph = NormalizationForm.removeOffsetProblemSituation(abstractParagraph);
					if(abstractText.isEmpty() || Character.isWhitespace(abstractText.charAt(abstractText.length()-1))){
						abstractText = abstractText + abstractParagraph;
					}else{
						abstractText = abstractText +" "+ abstractParagraph;
					}
					int endindex = abstractText.length();
					if(node.item(i).getAttributes().getNamedItem("Label")!=null)
					{
						String field = node.item(i).getAttributes().getNamedItem("Label").getTextContent();
						if(!field.isEmpty() && !publicationFiledsAlreayAdded.contains(field))
						{
							IPublicationField publicationField = new PublicationFieldImpl(startindex, endindex, field, PublicationFieldTypeEnum.abstracttext);
							fullTextfields.add(publicationField);
							publicationFiledsAlreayAdded.add(field);
						}
					}
				}
			}
			String type = new String();
			// Publciation Type
			NodeList publictionTypeNode = elements.getElementsByTagName("PublicationType");
			if (publictionTypeNode.item(0) != null) {
				type = publictionTypeNode.item(0).getTextContent();
			}
			// Mesh Terms -> Keywords
			List<IPublicationLabel> labels = new ArrayList<IPublicationLabel>();
			NodeList meshNode = elements.getElementsByTagName("Keyword");
			for (int x = 0; x < meshNode.getLength(); x++) {
				String meshTerms = meshNode.item(x).getTextContent();
				if(!meshTerms.isEmpty())
				{
					IPublicationLabel pubLAbel = new PublicationLabelImpl(meshTerms);
					labels.add(pubLAbel);
				}
			}
			// Mesh Terms
			NodeList meshNode2 = elements.getElementsByTagName("DescriptorName");
			for (int x = 0; x < meshNode2.getLength(); x++) {
				String meshTerms = meshNode2.item(x).getTextContent();
				if(!meshTerms.isEmpty())
				{
					IPublicationLabel pubLAbel = new PublicationLabelImpl(meshTerms);
					labels.add(pubLAbel);
				}
			}
			// Chemical List
			NodeList meshNode3 = elements.getElementsByTagName("NameOfSubstance");
			for (int x = 0; x < meshNode3.getLength(); x++) {
				String meshTerms = meshNode3.item(x).getTextContent();
				if(!meshTerms.isEmpty())
				{
					IPublicationLabel pubLAbel = new PublicationLabelImpl(meshTerms);
					labels.add(pubLAbel);
				}
			}
			
			// ISSN

			// Publication status
			node = pubElements.getElementsByTagName("PublicationStatus");
			status = new String();
			if (node.item(0) != null) {
				status = node.item(0).getTextContent();
			}

			// journal title
			node = elements.getElementsByTagName("Title");
			journal = new String();
			if (node.item(0) != null) {
				journal = node.item(0).getTextContent();
			}

			// pagination
			node = elements.getElementsByTagName("MedlinePgn");
			pages = new String();
			if (node.item(0) != null) {
				pages = node.item(0).getTextContent();
			}

			// journal volume
			node = elements.getElementsByTagName("Volume");
			volume = new String();
			if (node.item(0) != null) {
				volume = node.item(0).getTextContent();
			}

			// journal issue
			node = elements.getElementsByTagName("Issue");
			issues = new String();
			if (node.item(0) != null) {
				issues = node.item(0).getTextContent();
			}

			// publication date
			node = elements.getElementsByTagName("PubDate");
			date = new String();
			if (node.item(0) != null) {
				NodeList childs = node.item(0).getChildNodes();
				for (int i = 0; i < childs.getLength(); i++) {
					if (childs.item(i).getTextContent().matches("[a-zA-Z0-9]+")) {
						date += childs.item(i).getTextContent() + "-";
					}
				}
				if (date.endsWith("-")) {
					date = date.substring(0, date.length() - 1);
				}
			}
			
			String yearDate = new String();
			if(date.length()>3)
				yearDate = date.substring(0,4);
			pub = new PublicationImpl(title, authorList, type, yearDate, date, status, journal, volume,
					issues, pages, abstractText, pubmedLink+pubmedID, false, new String(), new String(), externalIDsSource, fullTextfields , labels );
			publications.add(pub);
		}
		return publications;
	}
	
	public  static  ESearchContext query(String terms) throws InternetConnectionProblemException{
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());
		client.getParams().setConnectionManagerTimeout(timeoutManager);
		client.getParams().setSoTimeout(timeoutManager);
		PostMethod post = new PostMethod(
				"https://www.ncbi.nlm.nih.gov/entrez/eutils/esearch.fcgi?");
			NameValuePair[] data = { 
				new NameValuePair("db", "PubMed"),
				new NameValuePair("usehistory", "y"),
				new NameValuePair("retmode", "xml"),
				new NameValuePair("term", terms)
		};
		post.setRequestBody(data);
		int retries = 0;
		while(retries < numberOFRetries)
		{
			try {
				client.executeMethod(post);
				return readXMLResultFileESearchContext(post);
			} catch (XPathExpressionException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (SAXException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (IOException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ParserConfigurationException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}	
		}
		throw new InternetConnectionProblemException(new PubmedException("The PubMed search was not completed for server problems"));
	}
	
	private static ESearchContext readXMLResultFileESearchContext(PostMethod post) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException{
		InputStream stream = post.getResponseBodyAsStream();
//		System.out.println(readString(stream));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	    DocumentBuilder builder = factory.newDocumentBuilder();;
	    org.w3c.dom.Document dDoc = builder.parse(stream);

	    XPath xPath = XPathFactory.newInstance().newXPath();
	    XPathExpression expr = xPath.compile("//eSearchResult/Count");  // these 2 lines
	    String count = (String) expr.evaluate(dDoc, XPathConstants.STRING);  // are different
	    expr = xPath.compile("//eSearchResult/QueryKey");  // these 2 lines
	    String queryKey = (String) expr.evaluate(dDoc, XPathConstants.STRING);  // are different
	    expr = xPath.compile("//eSearchResult/WebEnv");  // these 2 lines
	    String webEnv = (String) expr.evaluate(dDoc, XPathConstants.STRING);  // are different
		return new ESearchContext(webEnv, queryKey, Integer.valueOf(count));
	}
	
	static String readString(InputStream is) throws IOException {
		  char[] buf = new char[2048];
		  Reader r = new InputStreamReader(is, "UTF-8");
		  StringBuilder s = new StringBuilder();
		  while (true) {
		    int n = r.read(buf);
		    if (n < 0)
		      break;
		    s.append(buf, 0, n);
		  }
		  String result = s.toString();
		  r.close();
		  return result;
		}

	public static Set<String> getPublicationAvailableFreeFullText(String query) throws InternetConnectionProblemException {
		ESearchContext context = query(query);
		return getPublicationFreeFullText(context.getWebEnv(), context.getQueryKey(), 0, context.getCount());
	}

	private static Set<String> getPublicationFreeFullText(String webEnv,String queryKey, int indexMin, int step) throws InternetConnectionProblemException {
		waitIfNecessary();
		HttpClient client = getHTTPGeneralConfiguration();
		PostMethod post = configurationHttp(webEnv, queryKey, indexMin,step);
		return getPublicationFreeFullTextResults(client, post);	
	}

	private static Set<String> getPublicationFreeFullTextResults(HttpClient client, PostMethod post) throws InternetConnectionProblemException {
		int retries = 0;
		while(retries < numberOFRetries)
		{
			try {
				client.executeMethod(post);
				return readXMLResultFileAvailableFreeFullTextPublications(post);
			} catch (XPathExpressionException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (SAXException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (IOException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ParserConfigurationException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}	
		}
		throw new InternetConnectionProblemException(new PubmedException("The PubMed search was not completed for server problems"));
	}

	private static Set<String> readXMLResultFileAvailableFreeFullTextPublications(PostMethod post) throws SAXException, IOException, ParserConfigurationException, XPathExpressionException{
		Set<String> pmidsList = new HashSet<>();
		InputStream stream = post.getResponseBodyAsStream();
		Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
		NodeList nodes = doc.getElementsByTagName("MedlineCitation");
//		XPathExpression foreNameExpresion = xpath.compile("Article/AuthorList/Author/ForeName");
		// XPathExpression initialsExpression =
		// xpath.compile("Article/AuthorList/Author/Initials");
		for (int j = 0; j < nodes.getLength(); j++) {

			Element elements = (Element) nodes.item(j);
			NodeList node;
			// Pubid
			// String pubid= getXMLElemnt(nodes.item(j),"PMID");
			node = elements.getElementsByTagName("PMID");
			String pubmedID = node.item(0).getTextContent();		
			pmidsList.add(pubmedID);
		}
		return pmidsList;
	}
	

}
