package com.silicolife.textmining.processes.ir.pubmed.reader;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.PublicationFieldTypeEnum;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.lables.PublicationLabelImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.PublicationFieldImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.processes.ir.pubmed.PubmedReader;

public class PubmedEbookReader {

	public List<IPublication> getPublications(Document doc) throws ANoteException {
		try{
			List<IPublication> publicationsResulty = new ArrayList<>();
			XPathFactory factory = XPathFactory.newInstance();
			XPath xpath = factory.newXPath();
			NodeList nodes = doc.getElementsByTagName("PubmedBookArticle");
			NodeList nodesPubMed = doc.getElementsByTagName("BookDocument");
			XPathExpression lastNameExpresion = xpath.compile("BookDocument/AuthorList/Author/LastName");

			for (int j = 0; j < nodes.getLength(); j++) {
				
				Element elements = (Element) nodes.item(j);
				Element pubElements = (Element) nodesPubMed.item(j);
				NodeList node = elements.getElementsByTagName("PMID");
				String pubmedID = node.item(0).getTextContent();
				List<IPublicationExternalSourceLink> externalIDsSource = processExternalIds(pubElements, pubmedID);
				String title = processArticleTitle(elements);
				String authorList = processAuthorList(nodes, lastNameExpresion, j, elements);

				List<IPublicationField> fullTextfields = new ArrayList<IPublicationField>();
				String abstractText = processAbstract(fullTextfields, elements);
				String type = "Book "+processSimpleElementTagName(elements, "PublicationType");

				List<IPublicationLabel> labels = new ArrayList<IPublicationLabel>();
				processMeshTerms(elements, labels, "Keyword");
				processMeshTerms(elements, labels, "DescriptorName");
				processMeshTerms(elements, labels, "NameOfSubstance");

				String status = processSimpleElementTagName(elements, "PublicationStatus");
				if(status.length()>25){
					System.out.println("Status more that 25 characteres "+pubmedID);
					System.out.println("Status : " + status);
					status = status.substring(0,24);
				}
				String journal = processSimpleElementTagName(elements,"PublisherName");
				if(journal.length()>500){
					System.out.println("Pages more that 128 characteres "+pubmedID);
					System.out.println("Journal : " + journal);
					journal = journal.substring(0,499);
				}
				String pages = processSimpleElementTagName(elements, "MedlinePgn");
				if(pages.length()>128){
					System.out.println("Pages more that 128 characteres "+pubmedID);
					System.out.println("Pages : " + pages);
					pages = pages.substring(0,127);
				}
				String volume = processSimpleElementTagName(elements, "Volume");
				if(volume.length()>128){
					System.out.println("Volume more that 128 characteres "+pubmedID);
					System.out.println("Volume : "+volume);
					volume = volume.substring(0, 127);
				}
				String issues = processSimpleElementTagName(elements, "Issue");
				if(issues.length()>128){
					System.out.println("Issue more that 128 characteres "+pubmedID);
					System.out.println("Issue : "+issues);
					issues = issues.substring(0, 127);
				}
				String date = processPublicationDate(elements);
				if(date.length()>25){
					System.out.println("Date more that 25 characteres "+pubmedID);
					System.out.println("Date : "+date);
					date = date.substring(0, 24);
				}

				String yearDate = new String();
				if(date.length()>3)
					yearDate = date.substring(0,4);
				IPublication pub = new PublicationImpl(title, authorList, type, yearDate, date, status, journal, volume,
						issues, pages, abstractText, PubmedReader.pubmedLink+pubmedID, false, new String(), new String(), externalIDsSource, fullTextfields , labels );
				publicationsResulty.add(pub);
			}
			return publicationsResulty;
		}catch(XPathExpressionException e){
			throw new ANoteException(e);
		}
	}
	
	private String processAbstract(List<IPublicationField> fullTextfields,
			Element elements) throws ANoteException{
		NodeList node = elements.getElementsByTagName("AbstractText");
		String abstractText = new String();
		Set<String> publicationFiledsAlreayAdded = new HashSet<>();
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
						if(field.length()>250)
							field = field.substring(0,250);
						IPublicationField publicationField = new PublicationFieldImpl(startindex, endindex, field, PublicationFieldTypeEnum.abstracttext);
						fullTextfields.add(publicationField);
						publicationFiledsAlreayAdded.add(field);
					}
				}
			}
		}
		return abstractText;
	}

	private String processAuthorList(NodeList nodes, XPathExpression lastNameExpresion, int j, Element elements) throws ANoteException {
		try {
			String authorList = new String();
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
			return authorList;
		} catch (XPathExpressionException e) {
			throw new ANoteException(e);
		}
	}

	private String processArticleTitle(Element elements) {
		String title;
		NodeList node;
		node = elements.getElementsByTagName("ArticleTitle");
		if(node.item(0)== null)
			return "";
		title = node.item(0).getTextContent();
		title = NormalizationForm.removeOffsetProblemSituation(title);
		return title;
	}

	private List<IPublicationExternalSourceLink> processExternalIds(Element pubElements, String pubmedID) {
		List<IPublicationExternalSourceLink> externalIDsSource = new ArrayList<IPublicationExternalSourceLink>();
		externalIDsSource.add(new PublicationExternalSourceLinkImpl(pubmedID, PublicationSourcesDefaultEnum.PUBMED.name()));
		if(pubElements != null){
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
		}
		return externalIDsSource;
	}

	private void processMeshTerms(Element elements, List<IPublicationLabel> labels, String tagName) {
		NodeList meshNode = elements.getElementsByTagName(tagName);
		for (int x = 0; x < meshNode.getLength(); x++) {
			String meshTerms = meshNode.item(x).getTextContent();
			if(!meshTerms.isEmpty())
			{
				IPublicationLabel pubLAbel = new PublicationLabelImpl(meshTerms);
				labels.add(pubLAbel);
			}
		}
	}

	private String processSimpleElementTagName(Element elements, String tagName) {
		String result = new String();
		NodeList node = elements.getElementsByTagName(tagName);
		if (node.item(0) != null) {
			result = node.item(0).getTextContent();
		}
		return result;
	}

	private String processPublicationDate(Element elements) {
		NodeList node = elements.getElementsByTagName("PubDate");
		String date = new String();
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
		return date;
	}
	
}
