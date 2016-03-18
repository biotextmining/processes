package com.silicolife.textmining.processes.ir.pubmed.newstretegy.crawl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;

//clase usada para obtener datos de los art�culos (abstract, t�tulo, y enlaces)
//con eUtils y HTTPClient
public class NewAccesEUtilsHTTPClient {
  //Obtiene los enlaces a partir de los que buscar el pdf
  //de un articulo de PubMed dado su pmid usando HTTPClient
  static public String [] getLinksWithHTTPClient(String id) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException
  {
	  CloseableHttpClient httpclient = HttpClients.createDefault();
	  HttpGet httpGet = new HttpGet("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?db=pubmed&cmd=llinks&id="+id);
	  Proxy proxySystem = InitConfiguration.getProxy();
	  Type type = InitConfiguration.getProxy().type();
	  if (proxySystem != null && !type.equals(Type.DIRECT)) {
		  InetSocketAddress addressSocket = InetSocketAddress.class.cast(proxySystem.address());
		  HttpHost proxy = new HttpHost(addressSocket.getHostName(), addressSocket.getPort());
		  RequestConfig config = RequestConfig.custom()
                  .setProxy(proxy)
                  .build();
		  httpGet.setConfig(config);
	  }
	  CloseableHttpResponse response1 = httpclient.execute(httpGet);  
	  int statusCode = response1.getStatusLine().getStatusCode();

	  if (statusCode == org.apache.http.HttpStatus.SC_INTERNAL_SERVER_ERROR) {
		return null;
	  }
	  HttpEntity entity1 = response1.getEntity();
	  
	  Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entity1.getContent());
	  XPathFactory factory = XPathFactory.newInstance();
	  XPath xpath = factory.newXPath();
	  NodeList datos_enlaces=getXPathNodes(xpath,doc,"//ObjUrl");
	  String [] urls=new String[datos_enlaces.getLength()];
	  
	  int aux=0;
	  for(int i=0;i<datos_enlaces.getLength();i++)
	  {
		NodeList datos_enlace=datos_enlaces.item(i).getChildNodes();
		NodeList atributos_enlace=getXPathNodes(xpath,datos_enlace,"Attribute");
		for(int j=0;j<atributos_enlace.getLength();j++)
		{
		  String atributo=atributos_enlace.item(j).getTextContent();
		  //interesan las urls con el atributo "full-text online" o "full-text PDF" o
		  //"author manuscript" porque son las que llevan al texto completo en PDF o
		  //al manuscrito del autor
		  if(atributo.equals("full-text online") || atributo.equals("full-text PDF") || 
			atributo.equals("author manuscript"))
		  {
			urls[aux]=getXPathNode(xpath,datos_enlace,"Url");
			aux++;
			break;
		  }
		}
	  }
	  
	  String [] arr=new String[aux];
	  System.arraycopy(urls, 0, arr, 0, aux);
	  return arr;
  }
  
  //Obtiene el abstract o el ttulo (si no se encuentra el abstract)
  //de un articulo de PubMed dado su pmid usando HTTPClient
  static public NewArticleBody getAbstractOrTitleWithHTTPClient(String id)
  {
	  try // 
	  {
		  CloseableHttpClient httpclient = HttpClients.createDefault();
		  HttpGet httpGet = new HttpGet("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id="+id);
		  Proxy proxySystem = InitConfiguration.getProxy();
		  Type type = InitConfiguration.getProxy().type();
		  if (proxySystem != null && !type.equals(Type.DIRECT)) {
			  InetSocketAddress addressSocket = InetSocketAddress.class.cast(proxySystem.address());
			  HttpHost proxy = new HttpHost(addressSocket.getHostName(), addressSocket.getPort());
			  RequestConfig config = RequestConfig.custom()
	                  .setProxy(proxy)
	                  .build();
			  httpGet.setConfig(config);
		  }
		  CloseableHttpResponse response1 = httpclient.execute(httpGet);  
		  HttpEntity entity1 = response1.getEntity();

		  Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(entity1.getContent());
		  XPathFactory factory = XPathFactory.newInstance();
		  XPath xpath = factory.newXPath();

		  try
		  {
			  String abs=getXPathNode(xpath,doc,"//AbstractText");
			  if(abs!=null) return new NewArticleBody(abs,true);
		  }catch(Exception e){}
		  return new NewArticleBody(getXPathNode(xpath,doc,"//ArticleTitle"),false);
	  }catch(Exception e)
	  {
		  return new NewArticleBody(null,false);
	  }
  }
  
  static private String getXPathNode(XPath xpath, Object inic, String name) throws XPathExpressionException
  {
	Node node = (Node)xpath.evaluate(name, inic, XPathConstants.NODE);
	return node.getTextContent();
  }
  
  static private NodeList getXPathNodes(XPath xpath, Object inic, String name) throws XPathExpressionException 
  {
	return (NodeList)xpath.evaluate(name, inic, XPathConstants.NODESET);
  }
}