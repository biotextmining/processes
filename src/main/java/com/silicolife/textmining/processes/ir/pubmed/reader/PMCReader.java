package com.silicolife.textmining.processes.ir.pubmed.reader;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.PublicationFieldTypeEnum;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.structure.PublicationFieldImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;

public class PMCReader {

	private List<IPublication> publications ;
	public static final String pubmedLink = "https://www.ncbi.nlm.nih.gov/pubmed/";
	public static final String PMCLink = "https://www.ncbi.nlm.nih.gov/pmc/articles/";


	public PMCReader(){
		this.publications = new ArrayList<>();
	}


	public List<IPublication> getPublications(File xxmlFile) throws ANoteException{
		try{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			dbf.setValidating(false);
			dbf.setNamespaceAware(true);
			dbf.setFeature("http://xml.org/sax/features/namespaces", false);
			dbf.setFeature("http://xml.org/sax/features/validation", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-dtd-grammar", false);
			dbf.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
			Document doc = dbf.newDocumentBuilder().parse(xxmlFile);
			NodeList nodes = doc.getElementsByTagName("article");

			for (int j = 0; j < nodes.getLength(); j++) {
				Element elements = (Element) nodes.item(j);

				List<IPublicationExternalSourceLink> externalIDsSource = processExternalIds(elements);

				String title = processArticleTitle(elements);
				String authorList = processAuthorList(elements);

				List<IPublicationField> fullTextfields = new ArrayList<IPublicationField>();
				String abstractText = processAbstract(fullTextfields, elements);
				String type = "" ; //processSimpleElementTagName(elements, "PublicationType");

				List<IPublicationLabel> labels = new ArrayList<IPublicationLabel>();

				String status = ""; //processSimpleElementTagName(elements, "PublicationStatus");
				if(status.length()>25){
					status = status.substring(0,24);
				}
				String journal = processSimpleElementTagName(elements,"journal-title-group").trim();
				String pages = processSimpleElementTagName(elements, "fpage") + "-" + processSimpleElementTagName(elements, "lpage");
				if(pages.length()>128){
					System.out.println("Pages more that 128 characteres ");
					System.out.println("Pages : " + pages);
					pages = pages.substring(0,127);
				}
				String volume = processSimpleElementTagName(elements, "volume");
				if(volume.length()>128){
					System.out.println("Volume more that 128 characteres ");
					System.out.println("Volume : "+volume);
					volume = volume.substring(0, 127);
				}
				String issues = processSimpleElementTagName(elements, "issue");
				if(issues.length()>128){
					System.out.println("Issue more that 128 characteres ");
					System.out.println("Issue : "+issues);
					issues = issues.substring(0, 127);
				}
				String date = processPublicationDate(elements);
				if(date.length()>25){
					System.out.println("Date more that 25 characteres ");
					System.out.println("Date : "+date);
					date = date.substring(0, 24);
				}

				String yearDate = new String();
				if(date.length()>3){
					String[] dateparts = date.split("-");
					for(String part : dateparts){
						if(part.length()==4 && Utils.isIntNumber(part))
							yearDate = part;
							break;
					}
					
				}

				String fullTextContent = processFullText(fullTextfields, elements);

				String link = "";
				IPublication publication = new PublicationImpl(title, authorList, type, yearDate, date, status, journal, volume,
						issues, pages, abstractText, link , true, new String(), new String(), externalIDsSource, new ArrayList<IPublicationField>() , labels );
				publication.setFullTextContent(fullTextContent);
				String pmid = PublicationImpl.getPublicationExternalIDForSource(publication, "pmid");
				if(pmid!=null && !pmid.isEmpty())
				{
					publication.setExternalLink(pubmedLink+pmid);
				}
				String pmc = PublicationImpl.getPublicationExternalIDForSource(publication, "pmc");

				if(publication.getExternalLink().isEmpty() && pmc!=null && !pmc.isEmpty())
				{
					publication.setExternalLink(PMCLink+pmc);
				}
				addPublication(publication);
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
				if(!externalID.startsWith("pmc"))
					externalID = "pmc"+externalID;
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

	private String processArticleTitle(Element elements) {
		String title;
		title = processSimpleElementTagName(elements, "title-group");
		title = NormalizationForm.removeOffsetProblemSituation(title);
		return title;
	}

	private String processAuthorList(Element elements){
		String authors = new String();
		NodeList contributions = elements.getElementsByTagName("contrib-group");
		for(int i=0;i<contributions.getLength();i++){
			Element contribution = (Element) contributions.item(i);
			if(contribution != null){
				NodeList contribs = contribution.getElementsByTagName("contrib");
				for(int ii=0;ii<contribs.getLength();ii++){
					Element contrib = (Element) contribs.item(ii);
					if(contrib != null){
						String att = contrib.getAttribute("contrib-type");
						if(att != null && att.equals("author")){
							NodeList names = contrib.getElementsByTagName("name");
							for(int j=0;j<names.getLength();j++){
								Element name = (Element) names.item(j);
								if(!authors.isEmpty()){
									authors = authors + ", ";
								}
								authors = authors + processSimpleElementTagName(name, "given-names") + " ";
								authors = authors + processSimpleElementTagName(name, "surname");
							}
						}
					}
				}
			}

		}
		return authors;
	}

	private String processSimpleElementTagName(Element elements, String tagName) {
		String result = new String();
		NodeList node = elements.getElementsByTagName(tagName);
		if (node.item(0) != null) {
			result = node.item(0).getTextContent();
		}
		return result;
	}

	public String processAbstract(List<IPublicationField> fullTextfields,
			Element elements) throws ANoteException{
		NodeList node = elements.getElementsByTagName("abstract");
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

	private String processPublicationDate(Element elements) {
		NodeList node = elements.getElementsByTagName("pub-date");
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

	public String processFullText(List<IPublicationField> fullTextfields,
			Element elements) throws ANoteException{
		NodeList node = elements.getElementsByTagName("body");
		String fullText = new String();
		Set<String> publicationFiledsAlreayAdded = new HashSet<>();
		for(int i=0;i<node.getLength();i++)
		{
			if (node.item(i) != null) {
				Element body = (Element) node.item(i);
				List<Element> bodyElements = getAllBodyElements(body);
				
				int startindex = fullText.length();
				String field = new String();
				for(Element bodypart : bodyElements){
					String textParagraph = bodypart.getTextContent();
					textParagraph = NormalizationForm.removeOffsetProblemSituation(textParagraph);
					if(fullText.isEmpty() || Character.isWhitespace(fullText.charAt(fullText.length()-1))){
						fullText = fullText + textParagraph;
					}else{
						fullText = fullText +" "+ textParagraph;
					}
					if(bodypart.getTagName().equals("title"))
					{
						int endindex = fullText.length() - bodypart.getTextContent().length();
						if(!field.isEmpty() && !publicationFiledsAlreayAdded.contains(field) && startindex != endindex)
						{
							if(field.length()>250)
								field = field.substring(0,250);
							IPublicationField publicationField = new PublicationFieldImpl(startindex, endindex, field, PublicationFieldTypeEnum.fulltext);
							fullTextfields.add(publicationField);
							publicationFiledsAlreayAdded.add(field);
							startindex = endindex;
						}
						field = bodypart.getTextContent();
					}
				}
				if(startindex != fullText.length()){
					int endindex = fullText.length();
					if(!field.isEmpty() && !publicationFiledsAlreayAdded.contains(field) && startindex != endindex)
					{
						if(field.length()>250)
							field = field.substring(0,250);
						IPublicationField publicationField = new PublicationFieldImpl(startindex, endindex, field, PublicationFieldTypeEnum.fulltext);
						fullTextfields.add(publicationField);
						publicationFiledsAlreayAdded.add(field);
						startindex=fullText.length();
					}
				}
				
			}

		}
		return fullText;
	}

	private List<Element> getAllBodyElements(Element body){
		List<Element> partsBody = new ArrayList<>();
		fillBodyParts(body, partsBody);
		return partsBody;
	}

	private void fillBodyParts(Element part, List<Element> bodyParts){
		if(part.getChildNodes() != null && part.getChildNodes().getLength()!=1 && !part.getTagName().equals("p")){
			NodeList childs = part.getChildNodes();
			for(int i = 0; i<childs.getLength(); i++){
				Node child = childs.item(i);
				if(child instanceof Element){
					fillBodyParts((Element)child, bodyParts);
				}
			}
		}else{
			bodyParts.add(part);
		}
	}

}
