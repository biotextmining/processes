package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.lables.PublicationLabelImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.utils.http.ResponseHandler;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;

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
		IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(epodocID, PublicationSourcesDefaultEnum.patent.name());
		publicationExternalIDSource.add(externalID);
		List<IPublicationField> publicationFields = new ArrayList<IPublicationField>();
		List<IPublicationLabel> publicationLabels = getLabels(item);
		String notes = getNotes(item);
		String relativePath = new String();
		String type = "Patent";
		String category = "EPO Patent";
		IPublication pub = new PublicationImpl(title, authors,category , date,date,"", "", "", "","", abstractSection, extenalLink, true,notes,
				relativePath,type,publicationExternalIDSource,publicationFields,publicationLabels);
		return pub;
	}
	
	public static List<IPublicationLabel> getLabels(Node item) {
		List<IPublicationLabel> out = new ArrayList<>();
		Set<String> classificationsIPC = getClassificationIPCR(item);
		for(String classificationIPC:classificationsIPC)
			out.add(new PublicationLabelImpl(PatentPipelineUtils.labelIPCStart+": " + classificationIPC));
		return out;
	}


	public static String getApplicants(Node item){//method to return applicants from patent bibliographic data
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
						if(applicants.isEmpty())
							return new String(); 
						return applicants.substring(0, applicants.length()-2);
					}
				}
			}	
		}
		return applicants;
	}



	private String getNotes(Node item) {
		String notesClassification = getNotesClassification(item);
		String owners = getApplicants(item);
		if(owners.isEmpty())
			return notesClassification.trim();
		return (notesClassification + " [ Owners: "+ owners + "]");
		
	}


	public static String getNotesClassification(Node item) {
		Set<String> classificationIPCRSet = getClassificationIPCR(item);
		if(classificationIPCRSet.isEmpty())
			return new String();
		String notes = "[ Classification IPCR: ";
		for(String classificationIPCR:classificationIPCRSet)
		{
			notes = notes + classificationIPCR +  " , ";
		}
		if(!notes.endsWith(", "))
			return new String();
		return notes.substring(0, notes.length()-3) + "]";
	}


	public static Set<String> getClassificationIPCR(Node item) {
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


	public static String getExternalLink(Node item) {
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


	public static String getAbstract(Node item) {
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


	public static String getDate(Node item) {
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


	public static String getAuthors(Node item) {
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
						if(authors.isEmpty())
							return new String();
						return authors.substring(0, authors.length()-2);
					}
				}
			}	
		}
		return authors;
	}


	public static String getTitle(Node item) {
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
	

	public static String getEpoDoc(Node item) {
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
