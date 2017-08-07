package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository;

import java.io.IOException;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.AIRPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentrepository.PatentRepositoryAPI;

public class PatentRepositoryPatentMetaInformationRetrieval extends AIRPatentMetaInformationRetrieval{
	
	public final static String patentrepositoryName = "Patent Repository from SilicoLife and CEB (UMinho)";
	public final static String patentrepositoryProcessID = "patentrepository.searchpatentmetainformation";

	public PatentRepositoryPatentMetaInformationRetrieval(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(configuration);
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		Iterator<String> iterator = mapPatentIDPublication.keySet().iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			PatentEntity patentEntity = searchPatentEntity(patentID);
			if(patentEntity!=null)
			{
				IPublication publication = getPublicationToChange(patentID,mapPatentIDPublication,patentEntity);
				updatePublication(mapPatentIDPublication,publication, patentEntity);
			}
		}
	}
	
	private PatentEntity searchPatentEntity(String patentID)
	{
		try {
			IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration conf = (IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration) getConfiguration();
			PatentEntity result = PatentRepositoryAPI.getPatentMetaInformationByID(conf.getPatentRepositoryServerBasedUrl(), patentID);
			return result;
		} catch (IOException e) {
			return null;
		}
	}
	
	private IPublication getPublicationToChange(String patentID,Map<String, IPublication> mapPatentIDPublication,PatentEntity patentEntity)
	{
		IPublication out = mapPatentIDPublication.get(patentID);
		if(patentEntity.getSources()!=null && !patentEntity.getSources().isEmpty())
		{
			for(String id:patentEntity.getSources())
			{
				if(mapPatentIDPublication.containsKey(id))
				{
					return mapPatentIDPublication.get(id);
				}
			}
		}	
		return out;
	}
	
	public void updatePublication(Map<String, IPublication> mapPatentIDPublication,IPublication publication,PatentEntity patentEntity)
	{
		publication.getPublicationExternalIDSource().add(new PublicationExternalSourceLinkImpl(patentEntity.getId(), PublicationSourcesDefaultEnum.patent.toString()));
		if(publication.getTitle().isEmpty() && patentEntity.getTitle()!=null && !patentEntity.getTitle().isEmpty())
			publication.setTitle(patentEntity.getTitle());
		if(publication.getAbstractSection().isEmpty() && patentEntity.getAbstractText()!=null &&!patentEntity.getAbstractText().isEmpty())
			publication.setAbstractSection(patentEntity.getAbstractText());
		for(String otherids : patentEntity.getOtherIds())
		{
			publication.getPublicationExternalIDSource().add(new PublicationExternalSourceLinkImpl(otherids, PublicationSourcesDefaultEnum.patent.toString()));
		}
		if(publication.getAuthors().isEmpty() && patentEntity.getAuthors()!=null && !patentEntity.getAuthors().isEmpty())
		{
			publication.setAuthors(convertListStringIntoString(patentEntity.getAuthors()));
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
		if(patentEntity.getClassifications()!=null && patentEntity.getClassifications()!=null &&!patentEntity.getClassifications().isEmpty())
		{
			notes = notes + " Classification: "+convertListStringIntoString(patentEntity.getClassifications());
		}
		publication.setNotes(notes);
	}
	
	private String convertListStringIntoString(List<String> in)
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

	@Override
	public String getSourceName() {
		return "Patent Repository Metainformation Retrieval";
	}

	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		if(configuration instanceof IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration)
		{
			IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration moduleConf = (IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration) configuration;
			if(moduleConf.getPatentRepositoryServerBasedUrl() == null || moduleConf.getPatentRepositoryServerBasedUrl().isEmpty())
			{
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("PatentRepositoryServerBasedUrl can not be null or empty");		
			}
		}
		else
			throw new WrongIRPatentMetaInformationRetrievalConfigurationException("Configuration is not a IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration");	
	}

}
