package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.pubchem;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.lables.PublicationLabelImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.AIRPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.pubchem.PubchemPatentDataObject;
import com.silicolife.textmining.processes.ir.pubchem.PubchemPatentParser;
import com.silicolife.textmining.processes.ir.pubchem.PubchemPatentRetrievalAPI;

public class PubchemPatentMetaInformationRetrieval extends AIRPatentMetaInformationRetrieval{

	public final static String pubchemProcessID = "pubchem.searchpatentmetainformation";
	public final static String pubchemName= "PubChem Crawling";
	
	public static SimpleDateFormat dt = new SimpleDateFormat("yyyyy-MM-dd"); 

	
	public int delayTimeBetweenSteps = 2;

	public PubchemPatentMetaInformationRetrieval()
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(null);
	}
	
	public PubchemPatentMetaInformationRetrieval(int delayTimeBetweenSteps)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(null);
		this.delayTimeBetweenSteps = delayTimeBetweenSteps;
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		Iterator<String> iterator = mapPatentIDPublication.keySet().iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			List<String> patentPossibilities = PatentPipelineUtils.createPatentIDPossibilities(patentID);
			for(String patentId:patentPossibilities)
			{
				PubchemPatentDataObject patentEntity = PubchemPatentParser.retrieveMetaInformation(patentId);
				if(patentEntity!=null && !stop)
				{
					IPublication publication = mapPatentIDPublication.get(patentID);
					updatePublication(mapPatentIDPublication,publication, patentEntity);
					break;
				}
				PubchemPatentRetrievalAPI.delay(delayTimeBetweenSteps);			
			}
		}		
	}



	private void updatePublication(Map<String, IPublication> mapPatentIDPublication, IPublication publication,PubchemPatentDataObject patentEntity) {
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
			publication.setFullDate(dt.format(patentEntity.getDate()));
		}
		if(publication.getExternalLink().isEmpty() && patentEntity.getLink()!=null && !patentEntity.getLink().isEmpty())
			publication.setExternalLink(patentEntity.getLink());
		String notes = publication.getNotes();
		if(patentEntity.getOwners()!=null && !patentEntity.getOwners().isEmpty())
		{
			notes = notes + "[ Owners: "+convertListStringIntoString(patentEntity.getOwners()) + "]";
		}
		if(!notes.contains("Classification") && patentEntity.getPatentClassifications()!=null &&!patentEntity.getPatentClassifications().isEmpty())
		{
			notes = notes + "[ Classification IPC: "+convertListStringIntoString(patentEntity.getPatentClassifications()) + "]";
		}
		if(patentEntity.getPatentClassifications()!=null &&!patentEntity.getPatentClassifications().isEmpty())
		{
			List<IPublicationLabel> labelsToAdd = new ArrayList<>();
			for(String classification:patentEntity.getPatentClassifications())
			{
				String labelClassification = "Classification IPC: "+classification.trim();
				labelsToAdd.add(new PublicationLabelImpl(labelClassification));
			}
			labelsToAdd =  PublicationImpl.getNotExistentLabels(publication,labelsToAdd);
			if(publication.getPublicationLabels()==null)
				publication.setPublicationLabels(labelsToAdd);
			else
				publication.getPublicationLabels().addAll(labelsToAdd);
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

	@Override
	public String getSourceName() {
		return "Pubchem Patent Metainformation Retrieval";
	}

	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration) throws WrongIRPatentMetaInformationRetrievalConfigurationException {

	}

}
