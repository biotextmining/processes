package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo;

import java.util.Calendar;
import java.util.Collection;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.utils.FGOParser;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.AIRPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;

public class FGOPatentMetaInformationRetrieval extends AIRPatentMetaInformationRetrieval{
	
	public FGOPatentMetaInformationRetrieval()
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(null);
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		for(String patentID:mapPatentIDPublication.keySet())
		{
			FGOPatentDataObject patentEntity = FGOParser.retrieveMetaInformation(patentID);
			if(patentEntity!=null)
			{
				IPublication publication = mapPatentIDPublication.get(patentID);
				updatePublication(mapPatentIDPublication,publication, patentEntity);
			}
		}
	}


	private void updatePublication(Map<String, IPublication> mapPatentIDPublication, IPublication publication,
			FGOPatentDataObject patentEntity) {
		publication.getPublicationExternalIDSource().add(new PublicationExternalSourceLinkImpl(patentEntity.getPatentID(), PublicationSourcesDefaultEnum.patent.toString()));
		if(publication.getTitle().isEmpty() && patentEntity.getTitle()!=null && !patentEntity.getTitle().isEmpty())
			publication.setTitle(patentEntity.getTitle());
		if(publication.getAbstractSection().isEmpty() && patentEntity.getAbstractText()!=null &&!patentEntity.getAbstractText().isEmpty())
			publication.setAbstractSection(patentEntity.getAbstractText());
		for(String otherids : patentEntity.getOtherPatentIDs())
		{
			publication.getPublicationExternalIDSource().add(new PublicationExternalSourceLinkImpl(otherids, PublicationSourcesDefaultEnum.patent.toString()));
		}
		if(publication.getAuthors().isEmpty() && patentEntity.getInventors()!=null && !patentEntity.getInventors().isEmpty())
		{
			publication.setAuthors(convertListStringIntoString(patentEntity.getInventors()));
		}
		if(publication.getYeardate().isEmpty() && patentEntity.getDate()!=null)
		{
			Calendar cal = Calendar.getInstance();
			cal.setTime(patentEntity.getDate());
			int year = cal.get(Calendar.YEAR);
			publication.setYeardate(String.valueOf(year));
		}
		if(publication.getExternalLink().isEmpty() && patentEntity.getLink()!=null && !patentEntity.getLink().isEmpty())
			publication.setExternalLink(patentEntity.getLink());
		String notes = publication.getNotes();
		if(patentEntity.getOwners()!=null && !patentEntity.getOwners().isEmpty())
		{
			notes = notes + " Owners: "+convertListStringIntoString(patentEntity.getOwners());
		}
		if(patentEntity.getPatentClassifications()!=null && patentEntity.getPatentClassifications()!=null &&!patentEntity.getPatentClassifications().isEmpty())
		{
			notes = notes + " Classification: "+convertListStringIntoString(patentEntity.getPatentClassifications());

		}
		publication.setNotes(notes);		
	}
	
	private String convertListStringIntoString(Collection<String> in)
	{
		String out = new String();
		for(String item:in)
		{
			out = out + item + ", ";
		}
		if(!out.isEmpty())
			out = out.substring(0,out.length()-2);
		return out;
	}
	
	

	public String getSourceName() {
		return "FGO Patent Metainformation Retrieval";
	}

	
	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {

	}

}
