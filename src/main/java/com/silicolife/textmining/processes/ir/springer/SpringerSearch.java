package com.silicolife.textmining.processes.ir.springer;

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
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchReportImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.DaemonException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
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
import com.silicolife.textmining.core.interfaces.process.utils.ISimpleTimeLeft;
import com.silicolife.textmining.processes.ir.springer.configuration.IIRSpringerSearchConfiguration;
import com.silicolife.textmining.processes.ir.springer.configuration.SpringerConfiguration;
import com.silicolife.textmining.processes.ir.springer.configuration.SpringerSearchDefaultSettings;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

/**
 * Properties
 * 
 * FindClaims
 * FindFullText
 * 
 * @author Hugo Costa
 *
 */
public class SpringerSearch  extends IRProcessImpl implements IIRSearch{


	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(SpringerSearch.class.getName());

	public static final IQueryOriginType queryOrigin = new QueryOriginTypeImpl("Springer");



	private boolean cancel = false;
	private IQuery query;
	private String autentication;
	private int nPubs;
	private int abstractAvailable;

	public SpringerSearch()
	{
		super();
	}
	
	private int getExpectedQueryResults(String query)throws InternetConnectionProblemException {
		try {

			return SpringerSearchUtils.getSearchResults(query,autentication);
		} catch (Exception e) {
			throw new InternetConnectionProblemException(e);
		}	
	}

	@Override
	public IIRSearchProcessReport search(IIRSearchConfiguration configuration2)throws ANoteException, InternetConnectionProblemException, InvalidConfigurationException{
		cancel = false;
		validateConfiguration(configuration2);
		IIRSpringerSearchConfiguration configurationSpringSearch = (IIRSpringerSearchConfiguration) configuration2;
		{
			String autenticationProp = ((IIRSpringerSearchConfiguration) configuration2).getAuthentication();
			if(autenticationProp!=null && !autenticationProp.isEmpty())
			{
				this.autentication = autenticationProp;
				configurationSpringSearch.getProperties().put(SpringerSearchDefaultSettings.ACCESS_TOKEN, autentication);
			}
			else
			{
				this.autentication = PropertiesManager.getPManager().getProperty(SpringerSearchDefaultSettings.ACCESS_TOKEN).toString();
				if(!autentication.isEmpty())
				{
					configurationSpringSearch.getProperties().put(SpringerSearchDefaultSettings.ACCESS_TOKEN, autentication);
				}
			}
		}
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		Date date = new Date();
		String name = generateQueryName(configurationSpringSearch,date);
		String completeQuery = buildQuery(configurationSpringSearch.getKeywords(), configurationSpringSearch.getOrganism(), configurationSpringSearch.getProperties());
		query = new QueryImpl(queryOrigin,date,configurationSpringSearch.getKeywords(),configurationSpringSearch.getOrganism(),completeQuery,0,0,
				name,new String(),new HashMap<Long, IQueryPublicationRelevance>(),configurationSpringSearch.getProperties());		
		IIRSearchProcessReport report = new IRSearchReportImpl(query);
		InitConfiguration.getDataAccess().createQuery(query);
		try {
			findDocuments(report,completeQuery,configurationSpringSearch.getProperties());
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

	
	private String generateQueryName(IIRSpringerSearchConfiguration configuration,Date date) {
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

	public IIRSearchUpdateReport updateQuery(IQuery queryInfo,ISimpleTimeLeft progress) throws ANoteException, InternetConnectionProblemException {
		this.query = queryInfo;
		if(queryInfo.getProperties().stringPropertyNames().contains(SpringerSearchDefaultSettings.ACCESS_TOKEN))
		{
			this.autentication = queryInfo.getProperties().getProperty(SpringerSearchDefaultSettings.ACCESS_TOKEN);
		}
		else
		{
			this.autentication = PropertiesManager.getPManager().getProperty(SpringerSearchDefaultSettings.ACCESS_TOKEN).toString();
		}
		cancel = false;
		IIRSearchUpdateReport report = new IRSearchUpdateReportImpl(queryInfo);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		try {
			updateDocuments(report,queryInfo);
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
		queryInfo.setDate(new Date());
		return report;
	}


	private void updateDocuments(IIRSearchUpdateReport report, IQuery queryInfo) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException, DaemonException {

		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int results = getExpectedQueryResults(queryInfo.getCompleteQuery());
		if(results > SpringerConfiguration.MAX_RESULTS)
			results = SpringerConfiguration.MAX_RESULTS;
		nPubs = queryInfo.getPublicationsSize();
		abstractAvailable = queryInfo.getAvailableAbstracts();
		for(int step=1;step<=results && !cancel;step = step + SpringerConfiguration.STEP)
		{
			Map<String, Long> doiAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.DOI.name());
			Set<String> doiSet = InitConfiguration.getDataAccess().getQueryPublicationsExternalIDFromSource(queryInfo,PublicationSourcesDefaultEnum.DOI.name());
			List<IPublication> pubs = SpringerSearchUtils.getSearchRange(autentication, query.getCompleteQuery(), step);
			Set<IPublication> newQueryDocuments = new HashSet<>();
			Set<IPublication> documentsToInsert = new HashSet<>();
			Set<Long> alreadyAdded = new java.util.HashSet<>();

			for(IPublication pub:pubs)
			{
				String pubDOI = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.DOI.name());
				if(doiSet.contains(pubDOI))
				{

				}
				else if(doiAlreadyExistOnDB.containsKey(pubDOI))
				{
					long oldID = doiAlreadyExistOnDB.get(pubDOI);
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(doiAlreadyExistOnDB.get(pubDOI)))
					{
						pub.setId(doiAlreadyExistOnDB.get(pubDOI));
						newQueryDocuments.add(pub);
						if(!pub.getAbstractSection().isEmpty())
							abstractAvailable ++;
						queryInfo.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
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
					queryInfo.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					doiAlreadyExistOnDB.put(pubDOI, pub.getId());
					report.incrementDocumentRetrieval(1);
					doiSet.add(pubDOI);
				}
			}
			if(!cancel)
			{
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
				Set<IPublication> publicationToAdd = new HashSet<>();
				publicationToAdd.addAll(newQueryDocuments);
				publicationToAdd.addAll(documentsToInsert);
				InitConfiguration.getDataAccess().addQueryPublications(queryInfo, publicationToAdd);
				nPubs = nPubs+publicationToAdd.size();
				queryInfo.setPublicationsSize(nPubs);
				queryInfo.setAvailableAbstracts(abstractAvailable);
				queryInfo.setDate(new Date());
				InitConfiguration.getDataAccess().updateQuery(queryInfo);
				memoryAndProgressAndTime(step + SpringerConfiguration.STEP,results+1,startTime);
			}
		}
	}



	private void findDocuments(IIRSearchProcessReport report, String completeQuery, Properties properties) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException {
		nPubs = 0;
		abstractAvailable = 0;
		int results = getExpectedQueryResults(completeQuery);
		if(results > SpringerConfiguration.MAX_RESULTS)
			results = SpringerConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		for(int step=1;step<=results && !cancel;step = step + SpringerConfiguration.STEP)
		{
			// Get All DOI ID in System
			Map<String, Long> doiAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.DOI.name());
			// Documents to Insert into databse 
			Set<IPublication> documentsToInsert = new HashSet<>();
			// Documents already present in DB - Just to add to Query
			Set<IPublication> documentsThatAlreayInDB = new HashSet<>();
			// Step Document retrieved
			List<IPublication> pubs = getSpringerResults(report,autentication,query.getCompleteQuery(),step);
			for(IPublication pub:pubs)
			{
				String epoDocPMID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.DOI.name());
				if(doiAlreadyExistOnDB.containsKey(epoDocPMID))
				{
					pub.setId(doiAlreadyExistOnDB.get(epoDocPMID));
					documentsThatAlreayInDB.add(pub);
					// Add new Document Relevance - Default
					this.query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());		
				}
				else
				{
					documentsToInsert.add(pub);
					this.query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				}
				report.incrementDocumentRetrieval(1);
				if(!pub.getAbstractSection().isEmpty())
					abstractAvailable ++;
				nPubs++;
			}
			if( !cancel && documentsToInsert.size()!=0)
			{
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
			}
			Set<IPublication> publications = new HashSet<>();
			publications.addAll(documentsToInsert);
			publications.addAll(documentsThatAlreayInDB);
			if(!cancel)
				InitConfiguration.getDataAccess().addQueryPublications(this.query, publications );
			this.query.setAvailableAbstracts(abstractAvailable);
			this.query.setPublicationsSize(nPubs);
			InitConfiguration.getDataAccess().updateQuery(this.query);
			memoryAndProgressAndTime(step + SpringerConfiguration.STEP,results+1,startTime);

		}
		if(!cancel)
		{
			InitConfiguration.getDataAccess().updateQuery(this.query);
		}
		else
		{
			report.setcancel();
			InitConfiguration.getDataAccess().inactiveQuery(this.query);
		}
	}

	
	private List<IPublication> getSpringerResults(IIRSearchProcessReport report, String autentication, String query, int step) throws InternetConnectionProblemException{
		int retries = 0;
		while(retries < SpringerSearchUtils.numberOFRetries && !cancel)
		{
			try {
				return SpringerSearchUtils.getSearchRange(autentication, query, step);
			}  catch (RedirectionException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ClientErrorException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ServerErrorException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ConnectionException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			} catch (ResponseHandlingException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}
		}
		if(cancel)
			return new ArrayList<IPublication>();
		throw new InternetConnectionProblemException(new SpringerException("The Springer search was not completed for server problems"),report);

	}

	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public static String buildQuery(String keywords,String organism,Properties properties) {
		String query = new String();
		query = keywords;
		query = query.replaceAll("\\?", "%3F");
		query = query.replaceAll("@", "%40");
		query = query.replaceAll("#", "%23");
		query = query.replaceAll("%", "%25");
		query = query.replaceAll("\\$", "%24");
		query = query.replaceAll("&", "%26");
		query = query.replaceAll("\\+", "%2B");
		query = query.replaceAll(",", "%2C");
		query = query.replaceAll(":", "%3A");
		query = query.replaceAll(" ", "%20");
		query = query.replaceAll("=", "%3D");
		query = query.replaceAll("\"", "%22");
		query = query.replaceAll("<", "%3C");
		query = query.replaceAll(">", "%3E");
		query = query.replaceAll("\\{", "%7B");
		query = query.replaceAll("\\}", "%7D");
		query = query.replaceAll("\\|", "%7C");
		query = query.replaceAll("\\^", "%5E");
		query = query.replaceAll("~", "%7E");
		query = query.replaceAll("\\[", "%5B");
		query = query.replaceAll("\\]", "%5D");
		query = query.replaceAll("`", "%60");
		return query;
	}


	@Override
	public IProcessType getType() {
		return new ProcessTypeImpl(-1,"IRSearch");
	}

	@Override
	public long getId() {
		return -1;
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
		return new ProcessOriginImpl(-1, "Springer Search");
	}

	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IIRSpringerSearchConfiguration)
		{
			
		}
		else
			throw new InvalidConfigurationException("configuration is not a IIRSpringerSearchConfiguration");
		
	}






}
