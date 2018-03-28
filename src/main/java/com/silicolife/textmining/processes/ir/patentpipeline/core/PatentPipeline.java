package com.silicolife.textmining.processes.ir.patentpipeline.core;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineException;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
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

	private boolean isrunning = false;
	private boolean stop = false;


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
		isrunning = true;
		logger.info("Patent Complete pipeline started");
		Set<String> patentIds = executePatentIDSearchStep(configuration);
		IIRPatentMetaInformationRetrievalReport reportMetaInformation = executePatentRetrievalMetaInformationStep(patentIds,configuration);
		IIRPatentRetrievalReport reportDownload = executePatentRetrievalPDFStep(reportMetaInformation.getMapPatentIDPublication());
		printReport(reportDownload);
		return reportMetaInformation.getMapPatentIDPublication();
	}

	public Set<String> executePatentIDSearchStep(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException, WrongIRPatentIDRecoverConfigurationException {
		int totalSearchSteps = patentIDrecoverSourceList.size();
		int step = 0;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		if(configuration.getQuery()==null || configuration.getQuery().isEmpty())
		{
			throw new WrongIRPatentIDRecoverConfigurationException("Query can not be null or empty");
		}
		logger.info("Patent Ids Retrieval Step");
		Set<String> patentIds = new HashSet<>();
		Iterator<IIRPatentIDRetrievalSource> iterator = patentIDrecoverSourceList.iterator();
		while(iterator.hasNext() && !stop)
		{
			IIRPatentIDRetrievalSource patentSource = iterator.next();
			logger.info(patentSource .getSourceName());
			Set<String> patentsSource = patentSource.retrievalPatentIds(configuration);
			patentIds.addAll(patentsSource);
			step+=1;
			memoryProgressAndTime(step, totalSearchSteps, startTime);
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
		isrunning = true;
		logger.info("Patent Retrieval IDs pipeline started");
		Set<String> patentIds = executePatentIDSearchStep(configuration);
		logger.info("Patent Metainformation pipeline started");
		IIRPatentMetaInformationRetrievalReport reportMetaInformation = executePatentRetrievalMetaInformationStep(patentIds,configuration);
		return reportMetaInformation.getMapPatentIDPublication();
	}


	protected void memoryProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		logger.info((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public IIRPatentMetaInformationRetrievalReport executePatentRetrievalMetaInformationStep(Set<String> patentIds,IIRPatentPipelineSearchConfiguration configuration) throws ANoteException {
		isrunning = true;
		logger.info("Meta Information find Step");
		Map<String, IPublication> mapPatentIDPublication = createSimplePublicationMaps(patentIds);
		runMetaInformationForTheGivenSources(mapPatentIDPublication);
		// Filter results
		return filterResults(mapPatentIDPublication, configuration);
	}

	public IIRPatentMetaInformationRetrievalReport executePatentRetrievalMetaInformationStep(Set<String> patentIds) throws ANoteException {
		return this.executePatentRetrievalMetaInformationStep(patentIds,null);
	}
	
	public IPublication executePatentRetrievalMetaInformationStep(String patentID) throws ANoteException
	{
		Set<String> patentIds = new HashSet<>();
		IIRPatentMetaInformationRetrievalReport report = this.executePatentRetrievalMetaInformationStep(patentIds,null);
		return report.getMapPatentIDPublication().get(patentID);
	}

	private IIRPatentMetaInformationRetrievalReport filterResults(Map<String, IPublication> mapPatentIDPublication,IIRPatentPipelineSearchConfiguration configuration)
	{
		IIRPatentMetaInformationRetrievalReport report = new IRPatentMetaInformationRetrievalReportImpl();
		if(configuration==null || configuration.getPatentClassificationIPCAllowed()==null && configuration.getYearMin() == null && configuration.getYearMax()==null)
		{
			report.setMapPatentIDPublication(mapPatentIDPublication);
		}
		else
		{
			Map<String, IPublication> mapPatentIDPublicationToAdd = new HashMap<>();
			Map<String, IPublication> mapPatentIDPublicationToExcluded = new HashMap<>();
			for(String patentID:mapPatentIDPublication.keySet())
			{
				IPublication patent = mapPatentIDPublication.get(patentID);
				// valid Patent according to patent search filters
				if(validPatentAccordingToSearchConfiguration(patent, configuration))
				{
					mapPatentIDPublicationToAdd.put(patentID, patent);
				}
				else
				{
					mapPatentIDPublicationToExcluded.put(patentID, patent);
				}
			}
			report.setMapPatentIDPublication(mapPatentIDPublicationToAdd);
			report.setMapPatentIDPublicationExcluded(mapPatentIDPublicationToExcluded);
		}
		return report;
	}

	public static boolean validPatentAccordingToSearchConfiguration(IPublication patent,IIRPatentPipelineSearchConfiguration configuration)
	{
		if(!validateMinYear(patent.getYeardate(),configuration.getYearMin()))
		{
			return false;
		}
		if(!validateMaxYear(patent.getYeardate(),configuration.getYearMax()))
		{
			return false;
		}
		if(!validateAllowedClassification(patent.getPublicationLabels(),configuration.getPatentClassificationIPCAllowed()))
		{
			return false;
		}
		return true;
	}

	private static boolean validateMinYear(String yeardate, Integer yearMin) {
		if(yearMin==null)
			return true;
		if(yeardate==null || yeardate.isEmpty() || !Utils.isIntNumber(yeardate))
			return false;
		return Integer.valueOf(yeardate) >= yearMin;
	}

	private static boolean validateMaxYear(String yeardate, Integer yearMax) {
		if(yearMax==null)
			return true;
		if(yeardate==null || yeardate.isEmpty() || !Utils.isIntNumber(yeardate))
			return false;
		return Integer.valueOf(yeardate) <= yearMax;
	}

	private static boolean validateAllowedClassification(List<IPublicationLabel> publicationLabels,
			Set<String> patentClassificationIPCAllowed) {
		if(patentClassificationIPCAllowed==null || patentClassificationIPCAllowed.isEmpty())
			return true;
		if(publicationLabels==null || publicationLabels.isEmpty())
			return false;
		for(IPublicationLabel label:publicationLabels)
		{
			if(label.getLabel().startsWith(PatentPipelineUtils.labelIPCStart))
			{
				String classification = label.getLabel().replaceAll(PatentPipelineUtils.labelIPCStart, "").replaceAll(":", "").trim();
				if(!classification.isEmpty())
				{
					for(String classificationAllowed:patentClassificationIPCAllowed)
					{
						if(classification.startsWith(classificationAllowed))
						{
							return true;
						}
					}
				}
			}
		}
		return false;
	}


	public static Map<String, IPublication> createSimplePublicationMaps(Set<String> patentIds) {
		Map<String, IPublication> mapPatentIDPublication = new HashMap<>();
		for(String patentID:patentIds)
		{
			IPublication pub = new PublicationImpl();
			pub.setType("Patent");
			mapPatentIDPublication.put(patentID, pub);
			IPublicationExternalSourceLink e = new PublicationExternalSourceLinkImpl(patentID, PublicationSourcesDefaultEnum.patent.name());
			mapPatentIDPublication.get(patentID).getPublicationExternalIDSource().add(e );
		}
		return mapPatentIDPublication;
	}

	private void runMetaInformationForTheGivenSources (Map<String, IPublication> mapPatentIDPublication) throws ANoteException{
		int step = 0;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int totalSize=patentMetaInformationRetrievelSourceList.size();
		Iterator<IIRPatentMetainformationRetrievalSource> iterator = patentMetaInformationRetrievelSourceList.iterator();
		while(iterator.hasNext() && !stop)
		{
			IIRPatentMetainformationRetrievalSource patentMetaInformationRetrievelSource = iterator.next();
			logger.info(patentMetaInformationRetrievelSource .getSourceName());
			patentMetaInformationRetrievelSource.retrievePatentsMetaInformation(mapPatentIDPublication);
			step++;
			memoryProgressAndTime(step, totalSize, startTime);
		}
	}

	public void runMetaInformationForTheGivenSourcesLogOff (Map<String, IPublication> mapPatentIDPublication) throws ANoteException{
		Iterator<IIRPatentMetainformationRetrievalSource> iterator = patentMetaInformationRetrievelSourceList.iterator();
		while(iterator.hasNext() && !stop)
		{
			IIRPatentMetainformationRetrievalSource patentMetaInformationRetrievelSource = iterator.next();
			patentMetaInformationRetrievelSource.retrievePatentsMetaInformation(mapPatentIDPublication);
		}
	}

	/**
	 * Method designed to iterate over the different patent retrieval systems
	 * @return 
	 * @throws ANoteException 
	 * 
	 */	
	public IIRPatentRetrievalReport executePatentRetrievalPDFStep (Map<String, IPublication> mapPatentIDPublication) throws ANoteException{
		isrunning = true;
		IRPatentRetrievalReport finalReport= new IRPatentRetrievalReport ();//Open the report class
		Set<String> patentsIDs = mapPatentIDPublication.keySet();
		Set<String> retrievedPatents=new HashSet<>();
		Set<String> notRetrievedPatents=new HashSet<>();
		Iterator<IIRPatentRetrieval> iterator = patentRetrievalProcessList.iterator();
		while(iterator.hasNext() && !stop)
		{	
			IIRPatentRetrieval patentRetrievalProcess = iterator.next();
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

	public void stop() {
		stop = true;
		if(isrunning)
		{
			for(IIRPatentIDRetrievalSource searchIds:patentIDrecoverSourceList)
			{
				searchIds.stop();
			}
			for(IIRPatentMetainformationRetrievalSource metaInfo:patentMetaInformationRetrievelSourceList)
			{
				metaInfo.stop();
			}
			for(IIRPatentRetrieval pdfretrieval:patentRetrievalProcessList)
			{
				pdfretrieval.stop();
			}
		}
	}




}
