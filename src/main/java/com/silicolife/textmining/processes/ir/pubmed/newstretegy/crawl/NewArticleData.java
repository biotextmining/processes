package com.silicolife.textmining.processes.ir.pubmed.newstretegy.crawl;

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.xml.sax.SAXException;

//import es.uvigo.ei.sing.PMID2LinkOut;

public class NewArticleData
{
  private String pmid; //pmid del articulo
  private String [] enlacesPDF; //url's iniciales a partir de las que buscar el pdf
  private NewArticleBody trozo_texto; //abstract o t�tulo del articulo
  private String publicationID;
  
  //obtiene los datos del articulo a partir de su pmid
  public NewArticleData(long publicationID,String pmid) throws XPathExpressionException, IOException, SAXException, ParserConfigurationException 
  {
	this.setPublicationID(String.valueOf(publicationID));
	this.pmid=pmid;
	enlacesPDF=NewAccesEUtilsHTTPClient.getLinksWithHTTPClient(pmid);
	trozo_texto=NewAccesEUtilsHTTPClient.getAbstractOrTitleWithHTTPClient(pmid);
  }
  
  public boolean tieneTrozoTexto() {
	return (trozo_texto!=null && trozo_texto.getTexto()!=null);
  }
  
  public NewArticleBody getTrozo_texto() {
	return trozo_texto;
  }
  
  public String getPmid() {
	return pmid;
  }
  
  public String[] getEnlacesPDF() {
	return enlacesPDF;
  }

public String getPublicationID() {
	return publicationID;
}

public void setPublicationID(String publicationID) {
	this.publicationID = publicationID;
}
}