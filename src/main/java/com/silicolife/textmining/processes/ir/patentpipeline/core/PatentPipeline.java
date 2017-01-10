package com.silicolife.textmining.processes.ir.patentpipeline.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineException;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalReportImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class PatentPipeline {
	
	static Logger logger = Logger.getLogger(PatentPipeline.class.getName());

	
	private List<IIRPatentIDRetrievalSource> patentIDrecoverSourceList;
	private List<IIRPatentMetainformationRetrievalSource> patentMetaInformationRetrievelSourceList;
	private List<IIRPatentRetrieval> patentRetrievalProcessList;

	public PatentPipeline()
	{
		this.patentRetrievalProcessList = new ArrayList<>();
		this.patentIDrecoverSourceList = new ArrayList<>();
		this.patentMetaInformationRetrievelSourceList = new ArrayList<>();
	}

	/**
	 * Add IIRPatentIDRecoverSource and Configuration to the Pipeline ( PatentID Retrieval)
	 * 
	 * @param patentIDrecoverSource
	 * @throws PatentPipelineException
	 */
	public void addPatentIDRecoverSource(IIRPatentIDRetrievalSource patentIDrecoverSource) throws PatentPipelineException
	{
		for(IIRPatentIDRetrievalSource patentSource:patentIDrecoverSourceList)
		{
			if(patentSource.getSourceName().equals(patentIDrecoverSource.getSourceName()))
			{
				throw new PatentPipelineException("IIRPatentIDRecoverSource already registed");
			}
		}
		patentIDrecoverSourceList.add(patentIDrecoverSource);
	}

	/**
	 * Add IIRPatentMetainformationRetrievalSource and Configuration to the Pipeline ( Meta Information Retrieval)
	 * 
	 * @param patentIDrecoverSource
	 * @throws PatentPipelineException
	 */
	public void addPatentsMetaInformationRetrieval(IIRPatentMetainformationRetrievalSource patentMetaInformationSourceToAdd) throws PatentPipelineException
	{
		for(IIRPatentMetainformationRetrievalSource patentMetaInformationSource:patentMetaInformationRetrievelSourceList)
		{
			if(patentMetaInformationSource.getSourceName().equals(patentMetaInformationSourceToAdd.getSourceName()))
			{
				throw new PatentPipelineException("IIRPatentMetainformationRetrievalSource already registed");
			}
		}
		patentMetaInformationRetrievelSourceList.add(patentMetaInformationSourceToAdd);
	}


	/**
	 * Add IIRPatentIDRetrieval and Configuration to the Pipeline (Patent PDF Retrieval)
	 * 
	 * @param patentIDRetrieval
	 * @throws PatentPipelineException
	 */	
	public void addPatentIDRetrieval(IIRPatentRetrieval patentIDRetrievalSource) throws PatentPipelineException
	{
		for(IIRPatentRetrieval patentRetrieval:patentRetrievalProcessList)
		{
			if(patentRetrieval.getSourceName().equals(patentIDRetrievalSource.getSourceName()))
			{
				throw new PatentPipelineException("IIRPatentRetrieval Source already registed");
			}
		}
		patentRetrievalProcessList.add(patentIDRetrievalSource);
	}

	/**
	 * Execute Complete Pipeline
	 * 1) Run all Source and get the patents IDs
	 * 2) Try find out Patent Meta Information
	 * 3) trying get Patent PDF according to Patent ID List
	 * @return 
	 * 
	 * @throws ANoteException
	 * @throws WrongIRPatentIDRecoverConfigurationException 
	 */
	public Map<String, IPublication> runCompletePipeline(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException, WrongIRPatentIDRecoverConfigurationException
	{
		if(configuration.getQuery()==null || configuration.getQuery().isEmpty())
		{
			throw new WrongIRPatentIDRecoverConfigurationException("Query can not be null or empty");
		}
		logger.info("Patent Complete pipeline started");
		Set<String> patentIds = executePatentIDSearchStep(configuration);
		IIRPatentMetaInformationRetrievalReport reportMetaInformation = executePatentRetrievalMetaInformationStep(patentIds);
		IIRPatentRetrievalReport reportDownload = executePatentRetrievalPDFStep(reportMetaInformation.getMapPatentIDPublication());
		printReport(reportDownload);
		return reportMetaInformation.getMapPatentIDPublication();
	}
	
	public Set<String> executePatentIDSearchStep(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException, WrongIRPatentIDRecoverConfigurationException {
		if(configuration.getQuery()==null || configuration.getQuery().isEmpty())
		{
			throw new WrongIRPatentIDRecoverConfigurationException("Query can not be null or empty");
		}
		logger.info("Patent Ids Retrieval Step");
		Set<String> patentIds = new HashSet<>();
		for(IIRPatentIDRetrievalSource patentSource:patentIDrecoverSourceList)
		{
			logger.info(patentSource.getSourceName());
			Set<String> patentsSource = patentSource.retrievalPatentIds(configuration);
			patentIds.addAll(patentsSource);
		}
		return patentIds;
	}

	/**
	 * Execute Pipeline
	 * 1) Run all Source and get the patents IDs
	 * 2) Try find out Patent Meta Information
	 * @return 
	 * 
	 * @throws ANoteException
	 * @throws WrongIRPatentIDRecoverConfigurationException 
	 */
	public Map<String, IPublication> runMetaInformationPipeline(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException, WrongIRPatentIDRecoverConfigurationException
	{
		logger.info("Patent Metainformation pipeline started");
		Set<String> patentIds = executePatentIDSearchStep(configuration);
		IIRPatentMetaInformationRetrievalReport reportMetaInformation = executePatentRetrievalMetaInformationStep(patentIds);
		return reportMetaInformation.getMapPatentIDPublication();
	}

	protected IIRPatentMetaInformationRetrievalReport executePatentRetrievalMetaInformationStep(Set<String> patentIds) throws ANoteException {
		logger.info("Meta Information find Step");
		Map<String, IPublication> mapPatentIDPublication = createSimplePublicationMaps(patentIds);
		runMetaInformationForTheGivenSources(mapPatentIDPublication);
		IIRPatentMetaInformationRetrievalReport report = new IRPatentMetaInformationRetrievalReportImpl();
		report.setMapPatentIDPublication(mapPatentIDPublication);
		int previousMapSize = mapPatentIDPublication.size();
		Map<String, IPublication> newMap = verifyMetadataAndCreateNewMap(mapPatentIDPublication);
		while (newMap.size()<previousMapSize && (((double)newMap.size()/(double)previousMapSize)<=0.9)){
			runMetaInformationForTheGivenSources(newMap);
			report.updateMapPatentIDPublication(newMap);
			previousMapSize=newMap.size();
			newMap=verifyMetadataAndCreateNewMap(newMap);
		}
		return report;
	}
	
	public static Map<String, IPublication> createSimplePublicationMaps(Set<String> patentIds) {
		Map<String, IPublication> mapPatentIDPublication = new HashMap<>();
		for(String patentID:patentIds)
		{
			mapPatentIDPublication.put(patentID, new PublicationImpl());
			IPublicationExternalSourceLink e = new PublicationExternalSourceLinkImpl(patentID, PublicationSourcesDefaultEnum.patent.name());
			mapPatentIDPublication.get(patentID).getPublicationExternalIDSource().add(e );
		}
		return mapPatentIDPublication;
	}

	private void runMetaInformationForTheGivenSources (Map<String, IPublication> mapPatentIDPublication) throws ANoteException{
		for(IIRPatentMetainformationRetrievalSource patentMetaInformationRetrievelSource:patentMetaInformationRetrievelSourceList)
		{
			logger.info(patentMetaInformationRetrievelSource.getSourceName());
			patentMetaInformationRetrievelSource.retrievePatentsMetaInformation(mapPatentIDPublication);
		}
	}

	private Map<String, IPublication> verifyMetadataAndCreateNewMap(Map<String, IPublication> mapPatentIDPublication){
		Set<String> patentIDs = mapPatentIDPublication.keySet();
		HashMap<String, IPublication> newPatentIDPublication = new HashMap<>();
		for (int patIndex = 0; patIndex < patentIDs.size(); patIndex++) {
			String patentID = (String) patentIDs.toArray()[patIndex];
			if ((mapPatentIDPublication.get(patentID).getTitle()==null||mapPatentIDPublication.get(patentID).getTitle().isEmpty())||
					((mapPatentIDPublication.get(patentID).getAbstractSection()==null||mapPatentIDPublication.get(patentID).getAbstractSection().isEmpty())&&
							(mapPatentIDPublication.get(patentID).getAuthors()==null||mapPatentIDPublication.get(patentID).getAuthors().isEmpty())&&
							(mapPatentIDPublication.get(patentID).getYeardate()==null||mapPatentIDPublication.get(patentID).getYeardate().isEmpty()))){//empty publication
				newPatentIDPublication.put(patentID, mapPatentIDPublication.get(patentID));
			}
		}
		return newPatentIDPublication;
	}

	/**
	 * Method designed to iterate over the different patent retrieval systems
	 * @return 
	 * @throws ANoteException 
	 * 
	 */	
	public IIRPatentRetrievalReport executePatentRetrievalPDFStep (Map<String, IPublication> mapPatentIDPublication) throws ANoteException{

		IRPatentRetrievalReport finalReport= new IRPatentRetrievalReport ();//Open the report class
		Set<String> patentsIDs = mapPatentIDPublication.keySet();
		Set<String> retrievedPatents=new HashSet<>();
		Set<String> notRetrievedPatents=new HashSet<>();
		for (IIRPatentRetrieval patentRetrievalProcess:patentRetrievalProcessList)
		{	
			IIRPatentRetrievalReport result = patentRetrievalProcess.retrievedPatents(patentsIDs);
			retrievedPatents.addAll(result.getRetrievedPatents());
			patentsIDs.removeAll(result.getRetrievedPatents());
			notRetrievedPatents.removeAll(result.getRetrievedPatents());
			notRetrievedPatents.addAll(result.getNotRetrievedPatents());
		}
		finalReport.setNotRetrievedPatents(notRetrievedPatents);
		finalReport.setRetrievedPatents(retrievedPatents);
		return finalReport;

	}

	protected void printReport(IIRPatentRetrievalReport report){
		logger.info("Retrieved PatentIds:\n"+report.getRetrievedPatents().toString());
		logger.info("Not Retrieved PatentIds:\n"+report.getNotRetrievedPatents().toString());
		logger.info("Percentage of Total Retrieved Patents:\n"+((float)report.getRetrievedPatents().size()/(float)(report.getNotRetrievedPatents().size()+report.getRetrievedPatents().size()))*100+"%");
	}




}
