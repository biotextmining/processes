package com.silicolife.textmining.processes.ir.pubmed;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.PublicationFieldTypeEnum;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.lables.PublicationLabelImpl;
import com.silicolife.textmining.core.datastructures.documents.structure.PublicationFieldImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;


public class MedLineReader {

	private InputStream stream;
	public static final String pubmedLink = "http://www.ncbi.nlm.nih.gov/pubmed/";
	private List<IPublication> publications ;

	public MedLineReader(InputStream stream){
		this.stream = stream;
		this.publications = new ArrayList<>();
	}

	private InputStream getInputStream(){
		return stream;
	}

	public List<IPublication> getMedlinePublications() throws ANoteException{
		try{
			
			Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(getInputStream());
//			XPathFactory factory = XPathFactory.newInstance();
//			XPath xpath = factory.newXPath();
			NodeList nodes = doc.getElementsByTagName("MedlineCitation");
			NodeList nodesPubMed = doc.getElementsByTagName("PubmedData");
//			XPathExpression lastNameExpresion = xpath.compile("Article/AuthorList/Author/LastName");

			for (int j = 0; j < nodes.getLength(); j++) {
				
				Element elements = (Element) nodes.item(j);
				Element pubElements = (Element) nodesPubMed.item(j);
				NodeList node = elements.getElementsByTagName("PMID");
				String pubmedID = node.item(0).getTextContent();
				List<IPublicationExternalSourceLink> externalIDsSource = processExternalIds(pubElements, pubmedID);

				String title = processArticleTitle(elements);
				String authorList = "";
//				String authorList = processAuthorList(nodes, lastNameExpresion, j, elements);

				List<IPublicationField> fullTextfields = new ArrayList<IPublicationField>();
				String abstractText = processAbstract(fullTextfields, elements);
				String type = processSimpleElementTagName(elements, "PublicationType");

				List<IPublicationLabel> labels = new ArrayList<IPublicationLabel>();
				processMeshTerms(elements, labels, "Keyword");
				processMeshTerms(elements, labels, "DescriptorName");
				processMeshTerms(elements, labels, "NameOfSubstance");

				String status = processSimpleElementTagName(elements, "PublicationStatus");
				String journal = processSimpleElementTagName(elements,"Title");
				String pages = processSimpleElementTagName(elements, "MedlinePgn");
				String volume = processSimpleElementTagName(elements, "Volume");
				String issues = processSimpleElementTagName(elements, "Issue");
				String date = processPublicationDate(elements);

				String yearDate = new String();
				if(date.length()>3)
					yearDate = date.substring(0,4);
				IPublication pub = new PublicationImpl(title, authorList, type, yearDate, date, status, journal, volume,
						issues, pages, abstractText, pubmedLink+pubmedID, false, new String(), new String(), externalIDsSource, fullTextfields , labels );
				addPublication(pub);
			}
			return getPublications();
		}catch(SAXException | IOException | ParserConfigurationException e){
			throw new ANoteException(e);
		}

	}

	public List<IPublication> getPublications() {
		return publications;
	}
	
	public void addPublication(IPublication pub){
		getPublications().add(pub);
	}

	public String processAbstract(List<IPublicationField> fullTextfields,
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
				abstractText = abstractText + abstractParagraph;
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
		title = node.item(0).getTextContent();
		title = NormalizationForm.removeOffsetProblemSituation(title);
		return title;
	}

	private List<IPublicationExternalSourceLink> processExternalIds(Element pubElements, String pubmedID) {
		List<IPublicationExternalSourceLink> externalIDsSource = new ArrayList<IPublicationExternalSourceLink>();
//		externalIDsSource.add(new PublicationExternalSourceLinkImpl(pubmedID, PublicationSourcesDefault.pubmed));
//		if(pubElements != null){
//			NodeList articleIDs = pubElements.getElementsByTagName("ArticleId");
//			for(int i=0;i<articleIDs.getLength();i++)
//			{
//				if (articleIDs.item(i) != null) {
//
//					String internalID = articleIDs.item(i).getTextContent().toLowerCase();
//					String source = articleIDs.item(i).getAttributes().getNamedItem("IdType").getTextContent();
//					if(internalID.length()>3 && source.equalsIgnoreCase("pmc"))
//					{
//						externalIDsSource.add(new PublicationExternalSourceLinkImpl(internalID.toLowerCase(), source));
//					}
//					else if(internalID.startsWith("10.") && source.equalsIgnoreCase("doi"))
//					{
//						externalIDsSource.add(new PublicationExternalSourceLinkImpl(internalID.toLowerCase(), source));
//					}
//				}
//			}
//		}
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
