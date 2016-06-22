package com.silicolife.textmining.processes.ir.pubmed;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;

public class PMCReader {
	
	public static final String pubmedLink = "http://www.ncbi.nlm.nih.gov/pubmed/";

	public PMCReader(){
		
	}

	
	public IPublication getPublications(File xxmlFile) throws ANoteException{
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document doc = dbf.newDocumentBuilder().parse(xxmlFile);
			
//			XPathFactory factory = XPathFactory.newInstance();
//			XPath xpath = factory.newXPath();
			NodeList nodesArticle = doc.getElementsByTagName("article-meta");
			Element elements = (Element) nodesArticle.item(0);
			
//			Element pubElements = (Element) nodesPubMed.item(j);
//			NodeList node = elements.getElementsByTagName("PMID");
//			String pubmedID = node.item(0).getTextContent();
			List<IPublicationExternalSourceLink> externalIDsSource = processExternalIds(elements);

			String title = "";//processArticleTitle(elements);
			//				String authorList = "";
			String authorList = "" ;// processAuthorList(nodes, lastNameExpresion, j, elements);

			List<IPublicationField> fullTextfields = new ArrayList<IPublicationField>();
			String abstractText = ""; //processAbstract(fullTextfields, elements);
			String type = "" ; //processSimpleElementTagName(elements, "PublicationType");

			List<IPublicationLabel> labels = new ArrayList<IPublicationLabel>();

			String status = ""; //processSimpleElementTagName(elements, "PublicationStatus");
			if(status.length()>25){
				status = status.substring(0,24);
			}
			String journal = "" ;//processSimpleElementTagName(elements,"Title");
			String pages = ""; //processSimpleElementTagName(elements, "MedlinePgn");
			if(pages.length()>128){
				System.out.println("Pages more that 128 characteres ");
				System.out.println("Pages : " + pages);
				pages = pages.substring(0,127);
			}
			String volume = ""; //processSimpleElementTagName(elements, "Volume");
			if(volume.length()>128){
				System.out.println("Volume more that 128 characteres ");
				System.out.println("Volume : "+volume);
				volume = volume.substring(0, 127);
			}
			String issues = ""; //processSimpleElementTagName(elements, "Issue");
			if(issues.length()>128){
				System.out.println("Issue more that 128 characteres ");
				System.out.println("Issue : "+issues);
				issues = issues.substring(0, 127);
			}
			String date = ""; //processPublicationDate(elements);
			if(date.length()>25){
				System.out.println("Date more that 25 characteres ");
				System.out.println("Date : "+date);
				date = date.substring(0, 24);
			}

			String yearDate = new String();
			if(date.length()>3)
				yearDate = date.substring(0,4);
			return new PublicationImpl(title, authorList, type, yearDate, date, status, journal, volume,
					issues, pages, abstractText, "", false, new String(), new String(), externalIDsSource, fullTextfields , labels );

		}catch(SAXException | IOException | ParserConfigurationException e){
			throw new ANoteException(e);
		}

	}


	private List<IPublicationExternalSourceLink> processExternalIds(Element elements) {
		List<IPublicationExternalSourceLink> externalIdsList = new ArrayList<>();
		NodeList articleIDs = elements.getElementsByTagName("article-id");
		for(int i=0;i<articleIDs.getLength();i++)
		{
			Element extenalIDElem = (Element) articleIDs.item(i);
			String source = extenalIDElem.getAttribute("pub-id-type");
			String externalID = extenalIDElem.getTextContent();
			switch (source) {
				case "pmid" :
					externalIdsList.add(new PublicationExternalSourceLinkImpl(externalID, PublicationSourcesDefaultEnum.PUBMED.toString()));
					break;
				case "pmc" :
					externalIdsList.add(new PublicationExternalSourceLinkImpl(externalID, PublicationSourcesDefaultEnum.pmc.toString()));
					break;
				case "doi" :
					externalIdsList.add(new PublicationExternalSourceLinkImpl(externalID, PublicationSourcesDefaultEnum.DOI.toString()));
					break;
				default :
					break;
			}
		}
		return externalIdsList;
	}


}
