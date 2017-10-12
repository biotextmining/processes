package com.silicolife.textmining.processes.ir.epopatent;

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
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchReportImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.DaemonException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.relevance.IQueryPublicationRelevance;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearch;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.IQuery;
import com.silicolife.textmining.core.interfaces.process.IR.IQueryOriginType;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.epopatent.configuration.IIREPOSearchConfiguration;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.processes.ir.epopatent.configuration.PatentSearchDefaultSettings;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineSettings;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

/**
 * Properties
 * 
 * FindClaims
 * 
 * @author Hugo Costa
 *
 */
public class OPSSearch  extends IRProcessImpl implements IIRSearch{
	
	
	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(OPSSearch.class.getName());
	
	
	private boolean cancel = false;
	private IQuery query;
	private String autentication;
	private String tokenaccess;
	private int nPubs;
	private int abstractAvailable;
	
	public OPSSearch()
	{
		super();
	}

	
	@Override
	public IIRSearchProcessReport search(IIRSearchConfiguration configuration) throws ANoteException,InvalidConfigurationException, InternetConnectionProblemException {
		cancel = false;
		validateConfiguration(configuration);
		IIREPOSearchConfiguration configurationEPOSearch = (IIREPOSearchConfiguration) configuration;
		String autentication = configurationEPOSearch.getAuthentication();
		if(autentication!=null && !autentication.isEmpty())
		{
			this.autentication = Utils.get64Base(autentication);
			configurationEPOSearch.getProperties().put(PatentSearchDefaultSettings.ACCESS_TOKEN, autentication);
		}
		else
		{
			autentication = PropertiesManager.getPManager().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN).toString();
			if(!autentication.isEmpty())
			{
				this.autentication = Utils.get64Base(autentication);
				configurationEPOSearch.getProperties().put(PatentSearchDefaultSettings.ACCESS_TOKEN, autentication);
			}
		}
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IQueryOriginType queryOrigin = new QueryOriginTypeImpl(OPSConfiguration.opssearch);
		Date date = new Date();
		String name = generateQueryName(configurationEPOSearch,date);
		String completeQuery = buildQuery(configurationEPOSearch.getKeywords(), configurationEPOSearch.getOrganism(), configurationEPOSearch.getProperties());
		query = new QueryImpl(queryOrigin,date,configurationEPOSearch.getKeywords(),configurationEPOSearch.getOrganism(),completeQuery,0,0,
				name,new String(),new HashMap<Long, IQueryPublicationRelevance>(),generateProperties(configurationEPOSearch));		
		IIRSearchProcessReport report = new IRSearchReportImpl(query);
		InitConfiguration.getDataAccess().createQuery(query);
		try {
			findDocuments(report,completeQuery,configurationEPOSearch);
		} catch (RedirectionException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ClientErrorException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ServerErrorException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ConnectionException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ResponseHandlingException e) {
			throw new InternetConnectionProblemException(e);
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}
	
	private Properties generateProperties(IIREPOSearchConfiguration configurationEPOSearch)
	{
		Properties out = new Properties();
		if(configurationEPOSearch.getMinYear()!=null)
		{
			out.put(PatentSearchDefaultSettings.MINYEAR, String.valueOf(configurationEPOSearch.getMinYear()));
		}
		if(configurationEPOSearch.getMaxYear()!=null)
		{
			out.put(PatentSearchDefaultSettings.MAXYEAR, String.valueOf(configurationEPOSearch.getMaxYear()));
		}
		if(configurationEPOSearch.getClassificationIPCFilter()!=null && !configurationEPOSearch.getClassificationIPCFilter().isEmpty())
		{
			out.put(PatentPipelineSettings.patentPipelineSearchYearMin, String.valueOf(configurationEPOSearch.getClassificationIPCFilter()));
		}
		return out;
	}
	
	private String generateQueryName(IIREPOSearchConfiguration configuration,Date date) {
		if(configuration.getQueryName()!=null && !configuration.getQueryName().isEmpty())
		{
			if(configuration.getQueryName().equals(GlobalOptions.defaulQuerytName))
			{
				String result = new String();
				if (configuration != null && !configuration.getKeywords().isEmpty())
					result = result + configuration.getKeywords() + ":";
				if (configuration.getOrganism() != null && !configuration.getOrganism().isEmpty())
					result = result + configuration.getOrganism() + ":";
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
			if (configuration != null && !configuration.getKeywords().isEmpty())
				result = result + configuration.getKeywords() + ":";
			if (configuration.getOrganism() != null && !configuration.getOrganism().isEmpty())
				result = result + configuration.getOrganism() + ":";
			if (date != null)
				result = result + date;
			return result;
		}
	}
	
	public IIRSearchUpdateReport updateQuery(IQuery queryInfo) throws ANoteException, InternetConnectionProblemException {
		this.cancel = false;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IIRSearchUpdateReport report = new IRSearchUpdateReportImpl(queryInfo);
		try {
			updateDocuments(report, queryInfo);	
		} catch (RedirectionException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ClientErrorException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ServerErrorException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ConnectionException e) {
			throw new InternetConnectionProblemException(e);
		} catch (ResponseHandlingException e) {
			throw new InternetConnectionProblemException(e);
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}

	private void updateDocuments(IIRSearchProcessReport report,IQuery query) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException, DaemonException {
		if(query.getProperties().stringPropertyNames().contains(PatentSearchDefaultSettings.ACCESS_TOKEN))
		{
			this.autentication = Utils.get64Base(query.getProperties().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN));
		}
		else
		{
			this.autentication = Utils.get64Base(PropertiesManager.getPManager().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN).toString());
		}
		if(autentication!=null && !cancel)
		{
			tokenacess();
		}
		nPubs = query.getPublicationsSize();
		abstractAvailable = query.getAvailableAbstracts();
		int results = getExpectedQueryResults(tokenaccess,query.getCompleteQuery());
		if(results > OPSConfiguration.MAX_RESULTS)
			results = OPSConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		for(int step=1;step<=results && !cancel;step = step + OPSConfiguration.STEP)
		{
			// Get EPO document in Datbase
			Map<String, Long> patentIDAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.patent.name());
			// Get EPO document in Query
			Set<String> patentsIds = InitConfiguration.getDataAccess().getQueryPublicationsExternalIDFromSource(query,PublicationSourcesDefaultEnum.patent.name());
			List<IPublication> pubs = OPSUtils.getSearch(tokenaccess,query.getCompleteQuery(),step);
			Set<IPublication> newQueryDocuments = new HashSet<>();
			Set<IPublication> documentsToInsert = new HashSet<>();
			Set<Long> alreadyAdded = new java.util.HashSet<>();
			for(IPublication pub:pubs)
			{
				String patentID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.patent.name());
				// If Query already contains document
				if(patentsIds.contains(patentID))
				{
	
				}
				// If DataBase already contains document
				else if(patentIDAlreadyExistOnDB.containsKey(patentID))
				{
					long oldID = patentIDAlreadyExistOnDB.get(patentID);
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(patentIDAlreadyExistOnDB.get(patentID)))
					{
						pub.setId(oldID);
						newQueryDocuments.add(pub);
						if(!pub.getAbstractSection().isEmpty())
							abstractAvailable ++;
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
						report.incrementDocumentRetrieval(1);
						// Add publication to blockIds
						alreadyAdded.add(oldID);
					}
				}
				else
				{
					documentsToInsert.add(pub);
					if(!pub.getAbstractSection().isEmpty())
						abstractAvailable ++;
					query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					patentIDAlreadyExistOnDB.put(patentID, pub.getId());
					report.incrementDocumentRetrieval(1);
					patentsIds.add(patentID);
				}
			}
			if( !cancel && documentsToInsert.size()!=0)
			{
				getPatentDetails(report, patentIDAlreadyExistOnDB, documentsToInsert);
				nPubs = nPubs +documentsToInsert.size();
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
			}
			Set<IPublication> publicationToAdd = new HashSet<>();
			publicationToAdd.addAll(newQueryDocuments);
			publicationToAdd.addAll(documentsToInsert);
			InitConfiguration.getDataAccess().addQueryPublications(query, publicationToAdd );
			query.setPublicationsSize(nPubs);
			query.setAvailableAbstracts(abstractAvailable);
			query.setDate(new Date());
			InitConfiguration.getDataAccess().updateQuery(query);
			try {
				if(autentication==null && !cancel)
				{
					System.out.println("sleeping...62 seconds");
					Thread.sleep(62000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			memoryAndProgressAndTime(step + OPSConfiguration.STEP,results+1,startTime);
		}
		if(!cancel)
		{
			InitConfiguration.getDataAccess().updateQuery(query);
		}
		else
		{
			report.setcancel();
			InitConfiguration.getDataAccess().inactiveQuery(query);
		}

	}

	private int getExpectedQueryResults(String autentication,String query) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		return OPSUtils.getSearchResults(autentication, query);
	}
	
	public int getExpectedQueryResults(String query) throws RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException {
		return this.getExpectedQueryResults(autentication, query);
	}
	


	private void findDocuments(IIRSearchProcessReport report,String query, IIREPOSearchConfiguration configurationEPOSearch) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException {
		if(autentication!=null && !cancel)
		{
			tokenacess();
		}
		nPubs = 0;
		abstractAvailable = 0;
		int results = getExpectedQueryResults(tokenaccess,query);
		if(results > OPSConfiguration.MAX_RESULTS)
			results = OPSConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		for(int step=1;step<=results && !cancel;step = step + OPSConfiguration.STEP)
		{
			// Get All EPO ID in Dtabase
			Map<String, Long> epodocAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.patent.name());
			// Documents to Insert into databse 
			Set<IPublication> documentsToInsert = new HashSet<>();
			// Documents already present in DB - Just to add to Query
			Set<IPublication> documentsThatAlreayInDB = new HashSet<>();
			// Step Document retrieved
			List<IPublication> pubs  = OPSUtils.getSearch(tokenaccess,query,step);
			for(IPublication pub:pubs)
			{
				String epoDocPMID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.patent.name());
				if(epodocAlreadyExistOnDB.containsKey(epoDocPMID))
				{
					pub.setId(epodocAlreadyExistOnDB.get(epoDocPMID));
					documentsThatAlreayInDB.add(pub);
					// Add new Document Relevance - Default
					this.query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());				
				}
				else
				{
					documentsToInsert.add(pub);
					this.query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				}
			}
			
			// Filter result by year and classification
			documentsToInsert = filterByClassification(configurationEPOSearch, documentsToInsert);
			documentsThatAlreayInDB = filterByClassification(configurationEPOSearch, documentsThatAlreayInDB);
			
			if( !cancel && documentsToInsert.size()!=0)
			{
				getPatentDetails(report, epodocAlreadyExistOnDB, documentsToInsert);
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
			}
			
			Set<IPublication> publications = new HashSet<>();
			publications.addAll(documentsToInsert);
			publications.addAll(documentsThatAlreayInDB);
			nPubs = nPubs + publications.size();
			abstractAvailable = abstractAvailable + calculateAvailableAbstracts(publications);

			if(!cancel)
				InitConfiguration.getDataAccess().addQueryPublications(this.query, publications );
			this.query.setAvailableAbstracts(abstractAvailable);
			this.query.setPublicationsSize(nPubs);
			report.incrementDocumentRetrieval(publications.size());
			InitConfiguration.getDataAccess().updateQuery(this.query);
			memoryAndProgressAndTime(step + OPSConfiguration.STEP,results+1,startTime);
		}
		if(!cancel)
		{
			InitConfiguration.getDataAccess().updateQuery(this.query);
		}
		else
		{
			report.setcancel();
			InitConfiguration.getDataAccess().removeQuery(this.query);
		}

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
	
	private Set<IPublication> filterByClassification(IIREPOSearchConfiguration configuration,Set<IPublication> publications) {
		Set<IPublication> out = new HashSet<>();
		for(IPublication publication:publications)
		{
			if(validPatentAccordingToSearchConfiguration(publication, configuration))
			{
				out.add(publication);
			}
		}
		return out;
	}
	
	private static boolean validPatentAccordingToSearchConfiguration(IPublication patent,IIREPOSearchConfiguration configuration)
	{
		if(!validateMinYear(patent.getYeardate(),configuration.getMinYear()))
		{
			return false;
		}
		if(!validateMaxYear(patent.getYeardate(),configuration.getMaxYear()))
		{
			return false;
		}
		if(!validateAllowedClassification(patent.getPublicationLabels(),configuration.getClassificationIPCFilter()))
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

	private void getPatentDetails(IIRSearchProcessReport report,Map<String, Long> patentIDAlreadyExistOnDB,
			Set<IPublication> pubs) throws ANoteException, InternetConnectionProblemException
	{
		{
			for(IPublication pub:pubs)
			{
				OPSUtils.updateAbstractwithDescritionandclaims(tokenaccess , pub);
			}
		}
	}

	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	private void tokenacess() {
		try {
			tokenaccess = OPSUtils.postAuth(autentication);
		} catch (RedirectionException e) {
			logger.warn(e.getMessage());
			OPSConfiguration.STEP = 10;
			return;
		} catch (ClientErrorException e) {
			logger.warn(e.getMessage());
			OPSConfiguration.STEP = 10;
			return;
		} catch (ServerErrorException e) {
			logger.warn(e.getMessage());
			OPSConfiguration.STEP = 10;
			return;
		} catch (ConnectionException e) {
			logger.warn(e.getMessage());
			OPSConfiguration.STEP = 10;
			return;
		} catch (ResponseHandlingException e) {
			logger.warn(e.getMessage());
			OPSConfiguration.STEP = 10;
			return;
		}
		OPSConfiguration.STEP = 100;
		
	}
	
	public static String buildQuery(String keywords,String organism,Properties properties) {
		return OPSUtils.queryBuilder(keywords);
	}
	

	
	@Override
	public IProcessType getType() {
		return new ProcessTypeImpl(1, "IRSearch");
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public void stop() {
		this.cancel = true ;
	}
	
	@Override
	public IQuery getQuery() {
		return query;
	}

	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "OPS Search");
	}

	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IIREPOSearchConfiguration)
		{
			
		}
		else
			throw new InvalidConfigurationException("Configuration is not a IIREPOSearchConfiguration instance");
		
	}



}
