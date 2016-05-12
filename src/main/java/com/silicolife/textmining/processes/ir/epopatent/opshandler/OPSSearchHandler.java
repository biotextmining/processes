package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.http.ResponseHandler;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;

public class OPSSearchHandler  implements ResponseHandler<List<IPublication>>{

	// 2013120618A1
	
	public OPSSearchHandler()
	{
		
	}
	
	
	@Override
	public List<IPublication> buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status)
			throws ResponseHandlingException {
		List<IPublication> pubs = new ArrayList<IPublication>();
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("exchange-document");
			for(int i=0;i<extchangeNode.getLength();i++)
			{
				IPublication pub = parserDocument(extchangeNode.item(i));
				pubs.add(pub);
			}
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pubs;
	}


	private IPublication parserDocument(Node item) {
		String epodocID = getEpoDoc(item);
		String title = getTitle(item);
		String authors = getAuthors(item);
		String date = getDate(item);
		String abstractSection = getAbstract(item);
		title = NormalizationForm.removeOffsetProblemSituation(title);
		abstractSection = NormalizationForm.removeOffsetProblemSituation(abstractSection);
		String extenalLink = getExternalLink(item);
		List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<IPublicationExternalSourceLink>();
		IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(epodocID, OPSConfiguration.epodoc);
		publicationExternalIDSource.add(externalID);
		List<IPublicationField> publicationFields = new ArrayList<IPublicationField>();
		List<IPublicationLabel> publicationLabels = new ArrayList<IPublicationLabel>();
		IPublication pub = new PublicationImpl(title, authors, "Patent", date,date,"", "", "", "","", abstractSection, extenalLink, true,new String(),
				new String(),publicationExternalIDSource,publicationFields,publicationLabels);
		return pub;
	}


	private String getExternalLink(Node item) {
		String externalLink = new String();
		Node bibliographicDate = item.getFirstChild();
		Node publicationReference = bibliographicDate.getFirstChild();
		NodeList documentids = publicationReference.getChildNodes();
		Properties documentProperties = new Properties();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			String documentIDType = documentid.getAttributes().getNamedItem("document-id-type").getNodeValue();
			if(documentIDType.equals("docdb"))
			{
				NodeList docNumberChild = documentid.getChildNodes();
				for(int j=0;j<docNumberChild.getLength();j++)
				{
					Node documentidChild = docNumberChild.item(j);
					String nodeName = documentidChild.getNodeName();
					String nodeValue = documentidChild.getTextContent();
					documentProperties.put(nodeName, nodeValue);
				}
				return OPSConfiguration.opsStartLink+"CC="+documentProperties.getProperty("country")+"&NR="+documentProperties.getProperty("doc-number")+documentProperties.getProperty("kind");
			}
		}
		
		return externalLink;
	}


	private String getAbstract(Node item) {
		String abstractText = new String();
		NodeList extchangeDocumentChilds = item.getChildNodes();
		for(int i=0;i<extchangeDocumentChilds.getLength();i++)
		{
			Node extchangeDocumentChild = extchangeDocumentChilds.item(i);
			String extchangeDocumentChildNodeName = extchangeDocumentChild.getNodeName();
			if(extchangeDocumentChildNodeName.equals("abstract"))
			{
				Node abstractNode =  extchangeDocumentChild;
				if(abstractNode.getAttributes().getNamedItem("lang")!=null)
				{
					String nodeLanguage = abstractNode.getAttributes().getNamedItem("lang").getNodeValue();
					if(nodeLanguage.endsWith("en"))
					{
						return abstractNode.getTextContent();
					}
					else
					{
//						return abstractNode.getTextContent();
					}
				}
				else
				{
					return abstractNode.getTextContent();
				}
			}
		}
		return abstractText;
	}


	private String getDate(Node item) {
		String date = new String();
		Node bibliographicDate = item.getFirstChild();
		Node publicationReference = bibliographicDate.getFirstChild();
		NodeList documentids = publicationReference.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			String documentIDType = documentid.getAttributes().getNamedItem("document-id-type").getNodeValue();
			if(documentIDType.equals("epodoc"))
			{
				NodeList docNumberChild = documentid.getChildNodes();
				for(int j=0;j<docNumberChild.getLength();j++)
				{
					Node documentidChild = docNumberChild.item(j);
					String nodeName = documentidChild.getNodeName();
					if(nodeName.equals("date"))
					{
						return documentidChild.getTextContent();
					}
				}
			}
		}
		return date;
	}


	private String getAuthors(Node item) {
		String authors = new String();
		Node bibliographicDate = item.getFirstChild();
		NodeList bibliographicDateChilds = bibliographicDate.getChildNodes();
		for(int i=0;i<bibliographicDateChilds.getLength();i++)
		{
			Node bibliographicDateChild = bibliographicDateChilds.item(i);
			String nodeNAme = bibliographicDateChild.getNodeName();
			if(nodeNAme.equals("parties"))
			{
				Node partiesNode = bibliographicDateChild;
				NodeList partiesChilds = partiesNode.getChildNodes();
				for(int j=0;j<partiesChilds.getLength();j++)
				{
					Node partiesChild = partiesChilds.item(j);
					String partiesChildNodeName = partiesChild.getNodeName();
					if(partiesChildNodeName.equals("inventors"))
					{
						Node invetors = partiesChild;
						NodeList inventorList = invetors.getChildNodes();
						for(int k=0;k<inventorList.getLength();k++)
						{
							Node inventor = inventorList.item(k);
							String inventorType = inventor.getAttributes().getNamedItem("data-format").getNodeValue();
							if(inventorType.equals("epodoc"))
							{
								authors = authors + inventor.getTextContent() + ", ";
							}
						}
						return authors.substring(0, authors.length()-2);
					}
				}
			}	
		}
		return authors;
	}


	private String getTitle(Node item) {
		String title = new String();
		Node bibliographicDate = item.getFirstChild();
		NodeList bibliographicDateChilds = bibliographicDate.getChildNodes();
		for(int i=0;i<bibliographicDateChilds.getLength();i++)
		{
			Node bibliographicDateChild = bibliographicDateChilds.item(i);
			String nodeNAme = bibliographicDateChild.getNodeName();
			if(nodeNAme.equals("invention-title"))
			{
				if(bibliographicDateChild.getAttributes().getNamedItem("lang")!=null)
				{
					String langAtribute = bibliographicDateChild.getAttributes().getNamedItem("lang").getNodeValue();
					if(langAtribute.equals("en"))
					{
						return bibliographicDateChild.getTextContent();
					}
					else
					{
//						return bibliographicDateChild.getTextContent();
					}
				}
				else
				{
					return bibliographicDateChild.getTextContent();
				}
			}	
		}
		return title;
	}


	private String getEpoDoc(Node item) {
		String epoDoc = new String();
		Node bibliographicDate = item.getFirstChild();
		Node publicationReference = bibliographicDate.getFirstChild();
		NodeList documentids = publicationReference.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			String documentIDType = documentid.getAttributes().getNamedItem("document-id-type").getNodeValue();
			if(documentIDType.equals("epodoc"))
			{
				NodeList docNumberChild = documentid.getChildNodes();
				for(int j=0;j<docNumberChild.getLength();j++)
				{
					Node documentidChild = docNumberChild.item(j);
					String nodeName = documentidChild.getNodeName();
					if(nodeName.equals("doc-number"))
					{
						return documentidChild.getTextContent();
					}
				}
			}
		}
		return epoDoc;
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
		  return s.toString();
		}

}
