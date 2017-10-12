
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
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
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
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchStepsConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class PatentPiplineSearch extends IRProcessImpl implements IIRSearch{

	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(PatentPiplineSearch.class.getName());

	private int nAbstracts = 0;
	private int nPublicacoes = 0;
	private int totalProcessSteps=3;//PatIDsSearch,PatMetSearch and Updatedatabase
	private int actualProcessStep=0;

	private List<IPublication> listPublicacoes;

	private int actuaPubs;
	private boolean stop = false;
	private IQuery query;
	private PatentPipeline patentPipeline;

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
		Date date = new Date();
		String name = generateQueryName(configuration,date);
		IQueryOriginType queryType = new QueryOriginTypeImpl(PublicationSourcesDefaultEnum.patent.name());
		query = new QueryImpl(queryType, date , configuration.getIRPatentPipelineSearchConfiguration().getQuery(),"", configuration.getIRPatentPipelineSearchConfiguration().getQuery(), 0, 0, name, new String(),new HashMap<Long, IQueryPublicationRelevance>(), generateProperties(configuration));
		IIRSearchProcessReport report = searchMethod(query,configuration);
		if(stop)
			report.setcancel();
		return report;
	}

	private Properties generateProperties(IIRPatentPipelineConfiguration configuration)
	{
		Properties properties = new Properties();
		List<IIRPatentIDRetrievalSource> searchIDs = configuration.getIIRPatentPipelineSearchConfiguration().getIIRPatentIDRecoverSource();
		properties.put(PatentPipelineSettings.patentPipelineSearchPatentIDs, getIIRPatentIDRecoverSourceString(searchIDs));
		List<IIRPatentMetainformationRetrievalSource> patentRetrievalMetaInformations = configuration.getIIRPatentPipelineSearchConfiguration().getIIRPatentRetrievalMetaInformation();
		properties.put(PatentPipelineSettings.patentPipelineSearchPatentMetaInfo, getIIRPatentRetrievalMetaInformation(patentRetrievalMetaInformations));
		IIRPatentPipelineSearchConfiguration queryConfiguration = configuration.getIRPatentPipelineSearchConfiguration();
		if(queryConfiguration.getPatentClassificationIPCAllowed()!=null && !queryConfiguration.getPatentClassificationIPCAllowed().isEmpty())
		{
			properties.put(PatentPipelineSettings.patentPipelineSearchPatentClassificationAllowed, queryConfiguration.getPatentClassificationIPCAllowed().toString());
		}
		if(queryConfiguration.getYearMax()!=null)
		{
			properties.put(PatentPipelineSettings.patentPipelineSearchYearMax, String.valueOf(queryConfiguration.getYearMax()));
		}
		if(queryConfiguration.getYearMin()!=null)
		{
			properties.put(PatentPipelineSettings.patentPipelineSearchYearMin, String.valueOf(queryConfiguration.getYearMin()));
		}
		return properties;
	}

	public String getIIRPatentRetrievalMetaInformation(List<IIRPatentMetainformationRetrievalSource> patentRetrievalMetaInformations)
	{
		String result = new String();
		for(IIRPatentMetainformationRetrievalSource patentRetrievalMetaInformation:patentRetrievalMetaInformations)
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
		IIRSearchProcessReport report = patentPipeline(searchConfiguration,query);
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
					result = result + configuration.getIRPatentPipelineSearchConfiguration().getQuery() + ":";
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
			if (configuration != null && !configuration.getQueryName().isEmpty())
				result = result + configuration.getQueryName() + ":";
			if (date != null)
				result = result + date;
			return result;
		}
	}


	private String testExistOnSet(List<String> patentPossible, Set<String> patentsFromDatabase){
		for (String id:patentPossible){
			if (patentsFromDatabase.contains(id)){
				return id;
			}	
		}
		return null;
	}

	private IIRSearchProcessReport patentPipeline(IIRPatentPipelineConfiguration searchConfiguration,IQuery query) throws ANoteException, InternetConnectionProblemException, PatentPipelineException, IOException, WrongIRPatentIDRecoverConfigurationException {

		IIRSearchProcessReport out = new IRSearchReportImpl(query);
		initPatentPipeline();
		//init the process counter
		actualProcessStep=1;
		increaseStep();

		Set<IPublication> documentsToInsert,documentsThatAlreayInDB;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();

		//add configurations to pipeline class in order to get the all the requisites to search for the IDs and retrieve their PDFs
		if(stop)
		{
			out.setcancel();
			return out;
		}
		addConfigurations(searchConfiguration);

		//execute the search step
		if(stop)
		{
			out.setcancel();
			return out;
		}
		Set<String> patentIds = searchIds(searchConfiguration);		


		// Previously download the existent documentID for PMID,PMC and DOI documents from System and Query
		Map<String, Long> patentidsAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.patent.name());
		Set<String> patentidsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.patent.name());


		//create a new patent set which is not registered on database to performa the metainformation step		
		Set<String> patentIDsForMetainformation = new HashSet<>();
		Map<String,String> patentIDsAlreadyOnDB =new HashMap<>();
		for (String patent: patentIds){
			List<String> patentPossible = PatentPipelineUtils.createPatentIDPossibilities(patent);
			String patentVariantRegistered = testExistOnSet(patentPossible, patentidsAlreadyExistOnDB.keySet());
			if (patentVariantRegistered==null){
				patentIDsForMetainformation.add(patent);
			}
			else{
				patentIDsAlreadyOnDB.put(patent,patentVariantRegistered);
			}
		}

		//execute the metainformation step
		if(stop)
		{
			out.setcancel();
			return out;
		}
		Map<String, IPublication> patentMap = metaInformationStep(patentIDsForMetainformation,searchConfiguration.getIRPatentPipelineSearchConfiguration());


		//Ids processed
		Set<Long> alreadyAdded = new HashSet<>();
		documentsToInsert = new HashSet<>();
		documentsThatAlreayInDB = new HashSet<>();

		//add the previous publication into the map to be verified
		for (String patentID: patentIDsAlreadyOnDB.keySet()){
			String registeredID = patentIDsAlreadyOnDB.get(patentID);
			Long pubID = patentidsAlreadyExistOnDB.get(registeredID);
			IPublication pub = InitConfiguration.getDataAccess().getPublication(pubID);
			patentMap.put(patentID, pub);
		}

		//process the final metainformation map, removing the repeated patents
		Map<String, List<String>> allPossibleSolutions = PatentPipelineUtils.getAllPatentIDPossibilitiesForAGivenSet(patentIds);
		patentMap = PatentPipelineUtils.processPatentMapWithMetadata(patentMap, allPossibleSolutions);

		processResults(query, documentsToInsert, documentsThatAlreayInDB, startTime,
				patentidsAlreadyExistOnDB, patentidsAlreadyExistOnQuery, patentMap, alreadyAdded);

		// Filter Documents By Configuration
		documentsToInsert = filterByClassification(searchConfiguration,documentsToInsert);
		documentsThatAlreayInDB = filterByClassification(searchConfiguration,documentsThatAlreayInDB);
		
		// Insert publications in System
		if(!stop && !documentsToInsert.isEmpty()){
			insertPublications(documentsToInsert);
		}
		//add to query process
		addQueryInformation(query, documentsToInsert, documentsThatAlreayInDB,out);	
		return out;
	}

	private Set<IPublication> filterByClassification(IIRPatentPipelineConfiguration configuration,Set<IPublication> publications) {
		Set<IPublication> out = new HashSet<>();
		for(IPublication publication:publications)
		{
			if(PatentPipeline.validPatentAccordingToSearchConfiguration(publication, configuration.getIRPatentPipelineSearchConfiguration()))
			{
				out.add(publication);
			}
		}
		return out;
	}

	private void addQueryInformation(IQuery query, Set<IPublication> documentsToInsert,
			Set<IPublication> documentsThatAlreayInDB, IIRSearchProcessReport out) throws ANoteException {
		Set<IPublication> publicationToAdd = new HashSet<>();
		publicationToAdd.addAll(documentsThatAlreayInDB);
		publicationToAdd.addAll(documentsToInsert);
		listPublicacoes.addAll(publicationToAdd);
		
		if(!stop)
		{
			insertQueryPublications(query,publicationToAdd);
			int abstractCalculated = calculateAvailableAbstracts(publicationToAdd);
			addToCounts(publicationToAdd.size(), abstractCalculated);
			query.setAvailableAbstracts(nAbstracts);
			query.setPublicationsSize(nPublicacoes);
			updateQueryOnDatabase(query);
		}
		out.incrementDocumentRetrieval(publicationToAdd.size());
		
	}

	private int calculateAvailableAbstracts(Set<IPublication> publicationsToAdd) {
		int out = 0;
		for(IPublication pub:publicationsToAdd)
		{
			if(!pub.getAbstractSection().trim().isEmpty())
				out++;
		}
		return out;
	}

	private void processResults(IQuery query,Set<IPublication> documentsToInsert,
			Set<IPublication> documentsThatAlreayInDB, long startTime,
			Map<String, Long> patentidsAlreadyExistOnDB, Set<String> patentidsAlreadyExistOnQuery,
			Map<String, IPublication> patentMap, Set<Long> alreadyAdded) {
		int step=0;
		for(String patentID:patentMap.keySet()){

			// Get ID from publication
			IPublication pub = patentMap.get(patentID);
			//			String pubpatentID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.patent.name());
			List<IPublicationExternalSourceLink> externalLinksList = pub.getPublicationExternalIDSource();
			Set<String> pubExternalIDs=new HashSet<>();
			for (IPublicationExternalSourceLink externaLink:externalLinksList){
				pubExternalIDs.add(externaLink.getSourceInternalId());
			}
			List<String> possibleIDs = PatentPipelineUtils.createPatentIDPossibilities(patentID);
			String patentIDInDB=testExistOnSet(possibleIDs,patentidsAlreadyExistOnDB.keySet());
			//if already exist on query or on other source that publication will be ignored 
			if(testExistOnSet(possibleIDs, patentidsAlreadyExistOnQuery)!=null){}

			// Test if patentID already exists in System
			else if(patentIDInDB!=null)
			{
				// Test if Publication already exists on added list
				if(!alreadyAdded.contains(patentidsAlreadyExistOnDB.get(patentIDInDB)))
				{
					// If publication already exist in system give it the system ID
					pub.setId(patentidsAlreadyExistOnDB.get(patentIDInDB));
					// Add to already exist list to later add to the query
					documentsThatAlreayInDB.add(pub);
					// Add if to The added ids
					alreadyAdded.add(patentidsAlreadyExistOnDB.get(patentIDInDB));
					// Add new Document Relevance - Default
					query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				}
			}
			else
			{
				patentidsAlreadyExistOnDB.put(patentID, pub.getId());
				documentsToInsert.add(pub);
				query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				String registeredExternalID=testExistOnSet(possibleIDs, pubExternalIDs);
				if (registeredExternalID==null){
					for (String externalID:pubExternalIDs){
						//						if (!externalID.equalsIgnoreCase(patentID) && patentMap.keySet().contains(externalID)){
						Map<String, List<String>> allMapIDs = PatentPipelineUtils.getAllPatentIDPossibilitiesForAGivenSet(patentMap.keySet());
						if (PatentPipelineUtils.existsOnCollection(externalID,allMapIDs.values())){
							//remove the previous entry on the dataset to be added
							documentsThatAlreayInDB.remove(pub);
							//get each external publication
							IPublication pubExternal = patentMap.get(PatentPipelineUtils.getKeyForAValue(allMapIDs, externalID));			
							//add the publication into the patent Ids alredy existent on DB set
							patentidsAlreadyExistOnDB.put(externalID, pub.getId());
							// Add if to The added ids
							alreadyAdded.add(patentidsAlreadyExistOnDB.get(externalID));
							//update publication fields
							pub=updatePublication(pub, pubExternal);						
							//add the updated pub into the list to be added to query					
							documentsThatAlreayInDB.add(pub);
						}
					}
				}
			}
			step++;
			memoryAndProgress(step,patentMap.size(),startTime);
		}
	}

	private Map<String, IPublication> metaInformationStep(Set<String> patentIDsForMetainformation, IIRPatentPipelineSearchConfiguration searchConfiguration)
			throws ANoteException {
		Map<String, IPublication> patentMap =new HashMap<>();
		if (!patentIDsForMetainformation.isEmpty()){
			IIRPatentMetaInformationRetrievalReport reportMetaInformation = patentPipeline.executePatentRetrievalMetaInformationStep(patentIDsForMetainformation,searchConfiguration);
			patentMap=reportMetaInformation.getMapPatentIDPublication();
		}
		increaseStep();
		return patentMap;
	}

	private Set<String> searchIds(IIRPatentPipelineConfiguration searchConfiguration)
			throws ANoteException, WrongIRPatentIDRecoverConfigurationException {
		Set<String> patentIds = patentPipeline.executePatentIDSearchStep(searchConfiguration.getIRPatentPipelineSearchConfiguration());
		increaseStep();
		return patentIds;
	}

	private void addConfigurations(IIRPatentPipelineConfiguration searchConfiguration) throws PatentPipelineException {
		IIRPatentPipelineSearchStepsConfiguration pipelineSearchConfiguration = searchConfiguration.getIIRPatentPipelineSearchConfiguration();
		for (IIRPatentIDRetrievalSource patentIDrecoverSource:pipelineSearchConfiguration.getIIRPatentIDRecoverSource()){
			patentPipeline.addPatentIDRecoverSource(patentIDrecoverSource);
		}
		
		for (IIRPatentMetainformationRetrievalSource patentmetaInformationRetrieval:pipelineSearchConfiguration.getIIRPatentRetrievalMetaInformation()){
			patentPipeline.addPatentsMetaInformationRetrieval(patentmetaInformationRetrieval);
		}
	}

	private void initPatentPipeline() {
		patentPipeline = new PatentPipeline(){
			protected void memoryProgressAndTime(int step, int total, long startTime) {
				memoryAndProgress(step, total, startTime);
			}
		};
	}

	protected IPublication updatePublication (IPublication pub1, IPublication pub2){
		if (pub1.getAbstractSection().isEmpty() || pub1.getAbstractSection().length()<pub2.getAbstractSection().length()){
			pub1.setAbstractSection(pub2.getAbstractSection());			
		}
		if (pub1.getAuthors().isEmpty() || pub1.getAuthors().length()<pub2.getAuthors().length()){
			pub1.setAuthors(pub2.getAuthors());			
		}
		if (pub1.getTitle().isEmpty() || pub1.getTitle().length()<pub2.getTitle().length()){
			pub1.setTitle(pub2.getTitle());			
		}
		if (pub1.getYeardate().isEmpty() || pub1.getYeardate().length()<pub2.getYeardate().length()){
			pub1.setYeardate(pub2.getYeardate());			
		}
		return pub1;

	}



	protected void increaseStep(){
		if (actualProcessStep==1){
			logger.info("Patent Retrieval IDs pipeline started");
		}
		else if (actualProcessStep==2){
			logger.info("Patent Metainformation pipeline started");
		}
		actualProcessStep+=1;
		System.out.println((GlobalOptions.decimalformat.format((double)actualProcessStep/ (double) totalProcessSteps * 100)) + " %...");
		logger.info((GlobalOptions.decimalformat.format((double)actualProcessStep/ (double) totalProcessSteps * 100)) + " %...");
	}


	protected void memoryAndProgress(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		logger.info((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
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
		stop = true;
		if(patentPipeline!=null)
			patentPipeline.stop();
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