package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
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
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

public class OPSPatentUpdateHandler implements ResponseHandler<Boolean>{

	private IPublication publication;

	public OPSPatentUpdateHandler(IPublication publication)
	{
		this.publication=publication;
	}

	@Override
	public Boolean buildResponse(InputStream response, String responseMessage,Map<String, List<String>> headerFields, int status)throws ResponseHandlingException {
		try {
			Document doc = OPSUtils.createJDOMDocument(response);
			NodeList extchangeNode = doc.getElementsByTagName("exchange-document");		


			if(extchangeNode.getLength() > 0 && !findStatus(extchangeNode.item(0)))
			{
				updatePublication(extchangeNode.item(0));
				return true;
			}
			else
			{
				return false;
			}

		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}


	private boolean findStatus(Node item){
		NamedNodeMap nodeAttributes = item.getAttributes();
		try{
			Node statusNode = nodeAttributes.getNamedItem("status");
			if (statusNode.getTextContent().equalsIgnoreCase("not found")){
				publication.setNotes("NF" + publication.getNotes());
				return true;
			}
		}catch(Exception e){
			return false;
		}
		return false;
	}



	private void updatePublication(Node item) {
		String epodocID = getEpoDoc(item);
		String title = getTitle(item);
		title = NormalizationForm.removeOffsetProblemSituation(title);
		if(publication.getTitle().isEmpty())
			publication.setTitle(title);
		String authors = getAuthors(item);
		if(publication.getAuthors().isEmpty())
			publication.setAuthors(authors);
		String date = getDate(item);
		if(publication.getYeardate().isEmpty())
			publication.setYeardate(date);
		String abstractSection = getAbstract(item);
		abstractSection = NormalizationForm.removeOffsetProblemSituation(abstractSection);
		if(publication.getAbstractSection().isEmpty())
			publication.setAbstractSection(abstractSection);
		String extenalLink = getExternalLink(item);
		if(publication.getExternalLink().isEmpty())
			publication.setExternalLink(extenalLink);
		String patentID = PublicationImpl.getPublicationExternalIDForSource(publication, PublicationSourcesDefaultEnum.patent.name());
		if(patentID==null || patentID.isEmpty())
		{
			List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<IPublicationExternalSourceLink>();
			IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(epodocID, PublicationSourcesDefaultEnum.patent.name());
			publicationExternalIDSource.add(externalID);
			publication.setPublicationExternalIDSource(publicationExternalIDSource);
		}
		List<IPublicationField> publicationFields = new ArrayList<IPublicationField>();
		if(publication.getPublicationFields().isEmpty())
		{
			publication.setPublicationFields(publicationFields);
		}
		else
		{
			publication.getPublicationFields().addAll(publicationFields);

		}
		List<IPublicationLabel> publicationLabels = new ArrayList<IPublicationLabel>();
		if(publication.getPublicationLabels().isEmpty())
		{
			publication.setPublicationLabels(publicationLabels);
		}
		else
		{
			publication.getPublicationLabels().addAll(publicationLabels);
		}
		if(publication.getNotes().isEmpty())
		{
			Set<String> classificationIPCRSet = getClassificationIPCR(item);
			if(!classificationIPCRSet.isEmpty())
			{
				String notes = "Classification IPCR : ";
				for(String classificationIPCR:classificationIPCRSet)
				{
					notes = notes + classificationIPCR +  " , ";
				}
				publication.setNotes(notes);
			}
		}
	}
	
	private Set<String> getClassificationIPCR(Node item) {
		Set<String> out = new HashSet<>();
		Node bibliographicDate = item.getFirstChild();
		NodeList bibliographicDateChilds = bibliographicDate.getChildNodes();
		for(int i=0;i<bibliographicDateChilds.getLength();i++)
		{
			Node bibliographicDateChild = bibliographicDateChilds.item(i);
			String nodeNAme = bibliographicDateChild.getNodeName();
			if(nodeNAme.equals("classifications-ipcr"))
			{
				Node classificationsipcrNode = bibliographicDateChild;
				NodeList classificationsipcrChilds = classificationsipcrNode.getChildNodes();
				for(int j=0;j<classificationsipcrChilds.getLength();j++)
				{
					Node classificationsipcrChild = classificationsipcrChilds.item(j);
					out.add(classificationsipcrChild.getTextContent().trim());
				}
			}	
		}
		return out;
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
		boolean titleInEngllish = false;
		for(int i=0;i<bibliographicDateChilds.getLength()&&!titleInEngllish;i++)
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
						title = bibliographicDateChild.getTextContent();
						titleInEngllish = true;
					}
					else
					{
						title = bibliographicDateChild.getTextContent();
					}
				}
				else
				{
					title = bibliographicDateChild.getTextContent();
				}
			}	
		}
		return title;
	}

	private String getApplicants (Node item){//method to return applicants from patent bibliographic data
		String applicants = new String();
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
					if(partiesChildNodeName.equals("applicants"))
					{
						Node applicnts = partiesChild;
						NodeList applicantList = applicnts.getChildNodes();
						for(int k=0;k<applicantList.getLength();k++)
						{
							Node applicant = applicantList.item(k);
							String inventorType = applicant.getAttributes().getNamedItem("data-format").getNodeValue();
							if(inventorType.equals("epodoc"))
							{
								applicants= applicants + applicant.getTextContent() + ", ";
							}
						}
						return applicants.substring(0, applicants.length()-2);
					}
				}
			}	
		}
		return applicants;
	}
}
