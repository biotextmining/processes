package com.silicolife.textmining.processes.ir.springer.handler;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.IOUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class SpringerSearchHandler  implements ResponseHandler<List<IPublication>>{

	// 2013120618A1
	
	public SpringerSearchHandler()
	{
		
	}
	
	
	@Override
	public List<IPublication> buildResponse(InputStream response, String responseMessage,
			Map<String, List<String>> headerFields, int status)
			throws ResponseHandlingException {
		List<IPublication> pubs = new ArrayList<IPublication>();
		try {
			Document doc = createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("pam:message");
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

	private IPublication parserDocument(Node item) {
		String docID = getDOIDoc(item);
		String title = getTitle(item);
		title = NormalizationForm.removeOffsetProblemSituation(title);

		String authors = getAuthors(item);
		String date = getDate(item);
		String yeardate = new String();
		if(date!=null && date.length()>4)
			yeardate = date.substring(0,4);
		String abstractSection = getAbstract(item);
		abstractSection = NormalizationForm.removeOffsetProblemSituation(abstractSection);

		String extenalLink = getExternalLink(item);
		String journal = getJournal(item);
		String volume = getVolume(item);
		String pages = getPages(item);
		String issue = getIssue(item);
		boolean openAcess = getOpenAcess(item);
		List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<IPublicationExternalSourceLink>();
		IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(docID, PublicationSourcesDefaultEnum.DOI.name());
		publicationExternalIDSource.add(externalID);
		List<IPublicationField> publicationFields = new ArrayList<IPublicationField>();
		List<IPublicationLabel> publicationLabels = new ArrayList<IPublicationLabel>();
		IPublication pub = new PublicationImpl(title, authors, "", yeardate,date,"", journal, volume, issue,pages, abstractSection, extenalLink, openAcess,new String(),
				new String(),publicationExternalIDSource,publicationFields,publicationLabels);
		return pub;
	}


	private boolean getOpenAcess(Node item) {
		boolean openaccess = false;
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:openAccess"))
			{
				return Boolean.valueOf(documentid.getTextContent());
			}
		}
		return openaccess;
	}


	private String getIssue(Node item) {
		String issue = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:number"))
			{
				return documentid.getTextContent();
			}
		}
		return issue;
	}


	private String getPages(Node item) {
		String pages = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:startingPage"))
			{
				return documentid.getTextContent();
			}
		}
		return pages;
	}


	private String getVolume(Node item) {
		String volume = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:volume"))
			{
				return documentid.getTextContent();
			}
		}
		return volume;
	}


	private String getJournal(Node item) {
		String journal = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:publicationName"))
			{
				return documentid.getTextContent();
			}
		}
		return journal;
	}


	private String getExternalLink(Node item) {
		String externalLink = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:url"))
			{
				return documentid.getTextContent();
			}
		}
		return externalLink;
	}


	private String getAbstract(Node item) {
		Node bobyDocumentChilds = item.getChildNodes().item(1);
		return bobyDocumentChilds.getTextContent().replace("Abstract", "");
	}


	private String getDate(Node item) {
		String date = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:publicationDate"))
			{
				return documentid.getTextContent();
			}
		}
		return date;
	}


	private String getAuthors(Node item) {
		String authors = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("dc:creator"))
			{
				authors = authors + " " +documentid.getTextContent();
			}
		}
		return authors;
	}


	private String getTitle(Node item) {
		String title = new String();
		Node head = item.getFirstChild();
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("dc:title"))
			{
				return documentid.getTextContent();
			}
		}
		return title;
	}


	private String getDOIDoc(Node item) {
		String doi = new String();
		Node head = item.getFirstChild();	
		Node pam = head.getChildNodes().item(1);
		NodeList documentids = pam.getChildNodes();
		for(int i=0;i<documentids.getLength();i++)
		{
			Node documentid = documentids.item(i);
			if(documentid.getNodeName().equals("prism:doi"))
			{
				return documentid.getTextContent();
			}
		}
		return doi;
	}
	
	public static Document createJDOMDocument(InputStream response)
			throws ParserConfigurationException, SAXException, IOException {
		String stream = IOUtils.toString(response, "UTF-8");
		stream = stream.replaceAll("\n", "");
		stream = stream.replaceAll("\\s{2,}", "");
		InputStream imputstream = new ByteArrayInputStream(stream.getBytes("UTF-8"));
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder parser = factory.newDocumentBuilder();
		Document doc = parser.parse(imputstream);
		return doc;
	}

}
