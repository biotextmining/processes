package com.silicolife.textmining.processes.ir.epopatent.opshandler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
		String epodocID = OPSSearchHandler.getEpoDoc(item);
		String title = OPSSearchHandler.getTitle(item);
		title = NormalizationForm.removeOffsetProblemSituation(title);
		if(publication.getTitle().isEmpty())
			publication.setTitle(title);
		String authors = OPSSearchHandler.getAuthors(item);
		if(publication.getAuthors().isEmpty())
			publication.setAuthors(authors);
		String date = OPSSearchHandler.getDate(item);
		if(publication.getYeardate().isEmpty())
			publication.setYeardate(date);
		String abstractSection = OPSSearchHandler.getAbstract(item);
		abstractSection = NormalizationForm.removeOffsetProblemSituation(abstractSection);
		if(publication.getAbstractSection().isEmpty())
			publication.setAbstractSection(abstractSection);
		String extenalLink = OPSSearchHandler.getExternalLink(item);
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
		List<IPublicationLabel> publicationLabels = OPSSearchHandler.getLabels(item);
		if(publication.getPublicationLabels().isEmpty())
		{
			publication.setPublicationLabels(publicationLabels);
		}
		else
		{
			publication.getPublicationLabels().addAll(publicationLabels);
		}
		if(publication.getNotes().isEmpty() || !publication.getNotes().contains("Classification"))
		{
			String notes = publication.getNotes();
			String notesClassification = OPSSearchHandler.getNotesClassification(item);
			publication.setNotes((notes + " " + notesClassification).trim());
		}
		if(publication.getNotes().isEmpty() || !publication.getNotes().contains("Owner"))
		{
			String notes = publication.getNotes();
			String owners = OPSSearchHandler.getApplicants(item);
			if(!owners.isEmpty())
				publication.setNotes((notes + "  [ Owners: " + owners + "]").trim());
		}
	}
}
