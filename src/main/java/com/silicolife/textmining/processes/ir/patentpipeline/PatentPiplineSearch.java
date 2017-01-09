package com.silicolife.textmining.processes.ir.patentpipeline;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.query.QueryImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryOriginTypeImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryPublicationRelevanceImpl;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.relevance.IQueryPublicationRelevance;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearch;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.IQuery;
import com.silicolife.textmining.core.interfaces.process.IR.IQueryOriginType;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchStepsConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentRetrievalMetaInformation;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class PatentPiplineSearch extends IRProcessImpl implements IIRSearch{

	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(PatentPiplineSearch.class.getName());

	private int nAbstracts = 0;
	private int nPublicacoes = 0;
	private List<IPublication> listPublicacoes;

	private int actuaPubs;
	private boolean cancel = false;
	private IQuery query;

	public PatentPiplineSearch() {
		super();
	}

	public IIRSearchProcessReport search(IIRSearchConfiguration searchConfiguration) throws ANoteException,InvalidConfigurationException, InternetConnectionProblemException
	{
		validateConfiguration(searchConfiguration);
		nAbstracts = 0;
		nPublicacoes = 0;
		listPublicacoes=new ArrayList<IPublication>();
		IIRPatentPipelineConfiguration patentpipelinesearchConfiguration = (IIRPatentPipelineConfiguration) searchConfiguration;
		IIRSearchProcessReport searchProcessReport = null;
		try {
			searchProcessReport = search(patentpipelinesearchConfiguration);
		} catch (PatentPipelineException e) {
			new ANoteException(e);
		} catch (IOException e) {
			new ANoteException(e);
		} catch (WrongIRPatentIDRecoverConfigurationException e) {
			new ANoteException(e);
		}
		return searchProcessReport;
	}

	public IDocumentSet getDocuments() { return null;}

	public List<IPublication> getPublicationDocuments(){ return listPublicacoes;}



	private IIRSearchProcessReport search(IIRPatentPipelineConfiguration configuration) throws ANoteException, InternetConnectionProblemException, PatentPipelineException, IOException, WrongIRPatentIDRecoverConfigurationException {
		String querySTR = configuration.getIRPatentPipelineSearchConfiguration().getQuery();
		Date date = new Date();
		String name = generateQueryName(configuration,date);
		IQueryOriginType queryType = new QueryOriginTypeImpl(PublicationSourcesDefaultEnum.patent.name());
		query = new QueryImpl(queryType, date , configuration.getIRPatentPipelineSearchConfiguration().getQuery(),"", querySTR, 0, 0, name, new String(),new HashMap<Long, IQueryPublicationRelevance>(), generateProperties(configuration));
		IIRSearchProcessReport report = searchMethod(query,configuration);
		if(cancel)
			report.setcancel();
		return report;
	}
	
	public Properties generateProperties(IIRPatentPipelineConfiguration configuration)
	{
		Properties properties = new Properties();
		List<IIRPatentIDRetrievalSource> searchIDs = configuration.getIIRPatentPipelineSearchConfiguration().getIIRPatentIDRecoverSource();
		properties.put(PatentPipelineSettings.patentPipelineSearchPatentIDs, getIIRPatentIDRecoverSourceString(searchIDs));
		List<IIRPatentRetrievalMetaInformation> patentRetrievalMetaInformations = configuration.getIIRPatentPipelineSearchConfiguration().getIIRPatentRetrievalMetaInformation();
		properties.put(PatentPipelineSettings.patentPipelineSearchPatentMetaInfo, getIIRPatentRetrievalMetaInformation(patentRetrievalMetaInformations));
		return properties;
	}
	
	public String getIIRPatentRetrievalMetaInformation(List<IIRPatentRetrievalMetaInformation> patentRetrievalMetaInformations)
	{
		String result = new String();
		for(IIRPatentRetrievalMetaInformation patentRetrievalMetaInformation:patentRetrievalMetaInformations)
		{
			result = result + patentRetrievalMetaInformation.getSourceName() + ",";
		}
		if(result.isEmpty())
			return result;
		return result.substring(0,result.length()-1);
	}

	public String getIIRPatentIDRecoverSourceString(List<IIRPatentIDRetrievalSource> searchIDs)
	{
		String result = new String();
		for(IIRPatentIDRetrievalSource searchID:searchIDs)
		{
			result = result + searchID.getSourceName() + ",";
		}
		if(result.isEmpty())
			return result;
		return result.substring(0,result.length()-1);
	}

	private IIRSearchProcessReport searchMethod(IQuery query,IIRPatentPipelineConfiguration searchConfiguration) throws ANoteException, InternetConnectionProblemException, PatentPipelineException, IOException, WrongIRPatentIDRecoverConfigurationException {
		registerQueryOnDatabase(query);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IIRSearchProcessReport report = new IRSearchReportImpl(query);
		patentPipeline(searchConfiguration,query,report);
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}

	protected void registerQueryOnDatabase(IQuery query) throws ANoteException {
		InitConfiguration.getDataAccess().createQuery(query);
	}



	private String generateQueryName(IIRPatentPipelineConfiguration configuration,Date date) {
		if(configuration.getQueryName()!=null && !configuration.getQueryName().isEmpty())
		{
			if(configuration.getQueryName().equals(GlobalOptions.defaulQuerytName))
			{
				String result = new String();
				if (configuration != null && !configuration.getIRPatentPipelineSearchConfiguration().getQuery().isEmpty())
					result = result + configuration.getIRPatentPipelineSearchConfiguration() + ":";
				if (date != null)
					result = result + date;
				return result;
			}
			else
				return configuration.getQueryName();
		}
		else
		{
			String result = new String();
			if (configuration != null && !configuration.getIRPatentPipelineSearchConfiguration().getQuery().isEmpty())
				result = result + configuration.getIRPatentPipelineSearchConfiguration() + ":";
			if (date != null)
				result = result + date;
			return result;
		}
	}


	private void patentPipeline(IIRPatentPipelineConfiguration searchConfiguration,IQuery query,IIRSearchProcessReport report) throws ANoteException, InternetConnectionProblemException, PatentPipelineException, IOException, WrongIRPatentIDRecoverConfigurationException {

		PatentPipeline patentPipeline = new PatentPipeline();
		Set<IPublication> documentsToInsert,documentsThatAlreayInDB;
		int abs_count=0;
		int step=0;

		//add configurations to pipeline class in order to get the all the requisites to search for the IDs and retrieve their PDFs
		IIRPatentPipelineSearchStepsConfiguration pipelineSearchConfiguration = searchConfiguration.getIIRPatentPipelineSearchConfiguration();
		for (IIRPatentIDRetrievalSource patentIDrecoverSource:pipelineSearchConfiguration.getIIRPatentIDRecoverSource()){
			patentPipeline.addPatentIDRecoverSource(patentIDrecoverSource);
		}

		for (IIRPatentRetrievalMetaInformation patentmetaInformationRetrieval:pipelineSearchConfiguration.getIIRPatentRetrievalMetaInformation()){
			patentPipeline.addPatentsMetaInformationRetrieval(patentmetaInformationRetrieval);
		}

		Map<String, IPublication> patentMap = patentPipeline.runMetaInformationPipeline(searchConfiguration.getIRPatentPipelineSearchConfiguration());

		// Previously download the existent documentID for PMID,PMC and DOI documents from System and Query
		Map<String, Long> patentidsAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.patent.name());
		Set<String> patentidsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.patent.name());

		//Ids processed
		Set<Long> alreadyAdded = new HashSet<>();
		documentsToInsert = new HashSet<>();
		documentsThatAlreayInDB = new HashSet<>();
		for(String patentID:patentMap.keySet()){
			// Get ID from publication
			IPublication pub = patentMap.get(patentID);
			String pubpatentID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.patent.name());

			if(patentidsAlreadyExistOnQuery.contains(pubpatentID))//if already exist on query or on other source that publication will be ignored 
			{

			}
			// Test if patentID already exists in System
			else if(patentidsAlreadyExistOnDB.containsKey(pubpatentID))
			{
				// Test if Publication already exists on added list
				if(!alreadyAdded.contains(patentidsAlreadyExistOnDB.get(pubpatentID)))
				{
					// If publication already exist in system give it the system ID
					pub.setId(patentidsAlreadyExistOnDB.get(pubpatentID));
					// Add to already exist list to later add to the query
					documentsThatAlreayInDB.add(pub);
					// Add if to The added ids
					alreadyAdded.add(patentidsAlreadyExistOnDB.get(pubpatentID));
					// Add new Document Relevance - Default
					query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					// Test is abstract is available an increase report
					if(!pub.getAbstractSection().isEmpty()){
						abs_count ++;}
					report.incrementDocumentRetrieval(1);
				}
			}
			else
			{
				patentidsAlreadyExistOnDB.put(pubpatentID, pub.getId());
				documentsToInsert.add(pub);
				query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				if(!pub.getAbstractSection().isEmpty()){
					abs_count ++;}
				report.incrementDocumentRetrieval(1);
			}
			step++;
			memoryAndProgress(step,patentMap.size());
		}
		// Insert publications in System
		if(!cancel && documentsToInsert.size()!=0){
			insertPublications(documentsToInsert);
		}

		Set<IPublication> publicationToAdd = new HashSet<>();
		publicationToAdd.addAll(documentsThatAlreayInDB);
		publicationToAdd.addAll(documentsToInsert);
		listPublicacoes.addAll(publicationToAdd);
		if(!cancel)
		{
			insertQueryPublications(query, publicationToAdd);
			addToCounts(publicationToAdd.size(), abs_count);
			query.setAvailableAbstracts(nAbstracts);
			query.setPublicationsSize(nPublicacoes);
			updateQueryOnDatabase(query);
		}

	}



	protected void memoryAndProgress(int step, int total) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	protected void addToCounts(int pubs, int absCount){
		this.nPublicacoes += pubs;
		this.nAbstracts += absCount;
	}

	public IProcessType getType() {
		return new ProcessTypeImpl(-1, "IR");
	}


	@Override
	public void stop() {
		cancel = true;		
	}


	public static Properties defaultPropertiesSettings() {
		Properties prop = new Properties();
		return prop;
	}

	public int getActuaPubs() {
		return actuaPubs;
	}

	public void setActuaPubs(int actuaPubs) {
		this.actuaPubs = actuaPubs;
	}

	public int getnAbstracts() {
		return nAbstracts;
	}

	public int getnPublicacoes() {
		return nPublicacoes;
	}

	@Override
	public long getId() {
		return query.getId();
	}

	@Override
	public IQuery getQuery() {
		return query;
	}

	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "Patent Pipeline Search");
	}

	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IIRPatentPipelineConfiguration)
		{
			if (((IIRPatentPipelineConfiguration) configuration).getIRPatentPipelineSearchConfiguration() == null ||
					(((IIRPatentPipelineConfiguration) configuration).getIRPatentPipelineSearchConfiguration().getQuery().isEmpty()))
			{
				throw new InvalidConfigurationException("IIRPatentSearchConfiguration instance must have Keywords to search");

			}
			else if (((IIRPatentPipelineConfiguration) configuration).getIIRPatentPipelineSearchConfiguration() == null )
			{
				throw new InvalidConfigurationException("IIRPatentSearchConfiguration instance must have a valid PipelineSearchConfiguration to search for patents");
			}
			else if(((IIRPatentPipelineConfiguration) configuration).getIIRPatentPipelineSearchConfiguration().getIIRPatentIDRecoverSource().isEmpty())
			{
				throw new InvalidConfigurationException("IIRPatentSearchConfiguration instance must have valid patentIDRecoverSource configurations to search for patents");

			}
			else if(((IIRPatentPipelineConfiguration) configuration).getIIRPatentPipelineSearchConfiguration().getIIRPatentRetrievalMetaInformation().isEmpty())
			{
				throw new InvalidConfigurationException("IIRPatentSearchConfiguration instance must have valid patentRetrievalMetaInformations configurations to search for patents metaInformation");
			}
		}	
		else
			throw new InvalidConfigurationException("Configuration is not a IIRPatentPipelineConfiguration instance");

	}

	@Override
	public int getExpectedQueryResults(String query) throws InternetConnectionProblemException {
		return 0;
	}


}
