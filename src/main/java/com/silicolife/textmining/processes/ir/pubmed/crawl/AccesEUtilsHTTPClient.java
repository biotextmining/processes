package com.silicolife.textmining.processes.ir.pubmed.crawl;

import java.io.IOException;
import java.net.Proxy.Type;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.http.conn.params.ConnRoutePNames;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;

//clase usada para obtener datos de los art�culos (abstract, t�tulo, y enlaces)
//con eUtils y HTTPClient
public class AccesEUtilsHTTPClient {
  //Obtiene los enlaces a partir de los que buscar el pdf
  //de un articulo de PubMed dado su pmid usando HTTPClient
  static public String [] getLinksWithHTTPClient(String id) throws HttpException, IOException, SAXException, ParserConfigurationException, XPathExpressionException
  {
//	try
//	{

	  HttpClient client= new HttpClient();
	  // setting property
	  if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
		  client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());

	  GetMethod get = new GetMethod("http://eutils.ncbi.nlm.nih.gov/entrez/eutils/elink.fcgi?db=pubmed&cmd=llinks&id="+id);

	  int statusCode = client.executeMethod(get);

	  if (statusCode == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
//		No se ha podido iniciar la busqueda para el articulo con pmid "+id);
//		El servidor de las eUtils del NCBI parece estar cado, debera volver a intentarlo ms tarde");
		return null;
	  }

	  Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(get.getResponseBodyAsStream());
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
//	}catch(Exception e)
//	{
//	  System.err.println("No se ha podido iniciar la bsqueda para el art�culo con pmid "+id);
//	  System.err.println("El formato de respuesta obtenido para la consulta parece no ser el esperado");
//	  System.err.println("Si se repite este error para todas las consultas es posible que hayan modificado las eUtils y deberan arreglarse los mtodos que las usen");
//	  return null;
//	}
  }
  
  //Obtiene el abstract o el ttulo (si no se encuentra el abstract)
  //de un articulo de PubMed dado su pmid usando HTTPClient
  static public ArticleBody getAbstractOrTitleWithHTTPClient(String id)
  {
	  try
	  {
		  HttpClient client= new HttpClient();
		  // setting property
		  if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			  client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());

		  GetMethod get = new GetMethod("http://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&retmode=xml&id="+id);
		  client.executeMethod(get);
		  Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(get.getResponseBodyAsStream());
		  XPathFactory factory = XPathFactory.newInstance();
		  XPath xpath = factory.newXPath();

		  try
		  {
			  String abs=getXPathNode(xpath,doc,"//AbstractText");
			  if(abs!=null) return new ArticleBody(abs,true);
		  }catch(Exception e){}
		  return new ArticleBody(getXPathNode(xpath,doc,"//ArticleTitle"),false);
	  }catch(Exception e)
	  {
		  return new ArticleBody(null,false);
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