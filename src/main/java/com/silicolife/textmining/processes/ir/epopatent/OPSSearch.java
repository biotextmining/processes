package com.silicolife.textmining.processes.ir.epopatent;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.apache.log4j.Logger;

import com.silicolife.http.exceptions.ClientErrorException;
import com.silicolife.http.exceptions.ConnectionException;
import com.silicolife.http.exceptions.RedirectionException;
import com.silicolife.http.exceptions.ResponseHandlingException;
import com.silicolife.http.exceptions.ServerErrorException;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryOriginTypeImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryPublicationRelevanceImpl;
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
	public int getExpectedQueryResults(String query) throws InternetConnectionProblemException {
		try {
			return OPSUtils.getSearchResults(query);
		} catch (Exception e) {
			throw new InternetConnectionProblemException(e);
		}
	}
	
	@Override
	public IIRSearchProcessReport search(IIRSearchConfiguration configuration) throws ANoteException, InternetConnectionProblemException {
		cancel = false;
		if(configuration instanceof IIREPOSearchConfiguration)
		{
			String autentication = ((IIREPOSearchConfiguration) configuration).getAuthentication();
			if(autentication!=null && !autentication.isEmpty())
			{
				this.autentication = Utils.get64Base(autentication);
				configuration.getProperties().put(PatentSearchDefaultSettings.ACCESS_TOKEN, autentication);
			}
			else
			{
				autentication = PropertiesManager.getPManager().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN).toString();
				if(!autentication.isEmpty())
				{
					this.autentication = Utils.get64Base(autentication);
					configuration.getProperties().put(PatentSearchDefaultSettings.ACCESS_TOKEN, autentication);
				}
			}

		}
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IQueryOriginType queryOrigin = new QueryOriginTypeImpl(OPSConfiguration.opssearch);
		Date date = new Date();
		String name = generateQueryName(configuration,date);
		String completeQuery = buildQuery(configuration.getKeywords(), configuration.getOrganism(), configuration.getProperties());
		query = new QueryImpl(queryOrigin,date,configuration.getKeywords(),configuration.getOrganism(),completeQuery,0,0,
				name,new String(),new HashMap<Long, IQueryPublicationRelevance>(),configuration.getProperties());		
		IIRSearchProcessReport report = new IRSearchReportImpl(query);
		InitConfiguration.getDataAccess().createQuery(query);
		try {
			findDocuments(report,completeQuery,configuration.getProperties());
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
	
	private String generateQueryName(IIRSearchConfiguration configuration,Date date) {
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

	private void updateDocuments(IIRSearchProcessReport report,IQuery queryInfo) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException, DaemonException {
		this.query = queryInfo;
		if(queryInfo.getProperties().stringPropertyNames().contains(PatentSearchDefaultSettings.ACCESS_TOKEN))
		{
			this.autentication = Utils.get64Base(queryInfo.getProperties().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN));
		}
		else
		{
			this.autentication = Utils.get64Base(PropertiesManager.getPManager().getProperty(PatentSearchDefaultSettings.ACCESS_TOKEN).toString());
		}
		if(autentication!=null && !cancel)
		{
			tokenacess();
		}
		nPubs = queryInfo.getPublicationsSize();
		abstractAvailable = queryInfo.getAvailableAbstracts();
		int results = getExpectedQueryResults(queryInfo.getCompleteQuery());
		if(results > OPSConfiguration.MAX_RESULTS)
			results = OPSConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		for(int step=1;step<=results && !cancel;step = step + OPSConfiguration.STEP)
		{
			// Get EPO document in Datbase
			Map<String, Long> epodocAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(OPSConfiguration.epodoc);
			// Get EPO document in Query
			Set<String> epoDocs = InitConfiguration.getDataAccess().getQueryPublicationsExternalIDFromSource(queryInfo,OPSConfiguration.epodoc);
			List<IPublication> pubs = OPSUtils.getSearch(tokenaccess,queryInfo.getCompleteQuery(),step);
			List<IPublication> newQueryDocuments = new ArrayList<IPublication>();
			List<IPublication> documentsToInsert = new ArrayList<IPublication>();
			Set<Long> alreadyAdded = new java.util.HashSet<>();
			for(IPublication pub:pubs)
			{
				String epoDoc = PublicationImpl.getPublicationExternalIDForSource(pub,OPSConfiguration.epodoc);
				// If Query already contains document
				if(epoDocs.contains(epoDoc))
				{
	
				}
				// If DataBase already contains document
				else if(epodocAlreadyExistOnDB.containsKey(epoDoc))
				{
					long oldID = epodocAlreadyExistOnDB.get(epoDoc);
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(epodocAlreadyExistOnDB.get(epoDoc)))
					{
						pub.setId(oldID);
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
					epodocAlreadyExistOnDB.put(epoDoc, pub.getId());
					report.incrementDocumentRetrieval(1);
					epoDocs.add(epoDoc);
				}
			}
			if( !cancel && documentsToInsert.size()!=0)
			{
				getPatentDetails(report, epodocAlreadyExistOnDB, documentsToInsert);
				nPubs = nPubs +documentsToInsert.size();
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
			}
			List<IPublication> publicationToAdd = new ArrayList<IPublication>();
			publicationToAdd.addAll(newQueryDocuments);
			publicationToAdd.addAll(documentsToInsert);
			InitConfiguration.getDataAccess().addQueryPublications(query, publicationToAdd );
			queryInfo.setPublicationsSize(nPubs);
			queryInfo.setAvailableAbstracts(abstractAvailable);
			queryInfo.setDate(new Date());
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
			InitConfiguration.getDataAccess().updateQuery(this.query);
		}
		else
		{
			report.setcancel();
			InitConfiguration.getDataAccess().inactiveQuery(this.query);
		}

	}

	private void findDocuments(IIRSearchProcessReport report,String query, Properties properties) throws ANoteException, RedirectionException, ClientErrorException, ServerErrorException, ConnectionException, ResponseHandlingException, InternetConnectionProblemException {
		if(autentication!=null && !cancel)
		{
			tokenacess();
		}
		nPubs = 0;
		abstractAvailable = 0;
		int results = getExpectedQueryResults(query);
		if(results > OPSConfiguration.MAX_RESULTS)
			results = OPSConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		for(int step=1;step<=results && !cancel;step = step + OPSConfiguration.STEP)
		{
			// Get All EPO ID in Dtabase
			Map<String, Long> epodocAlreadyExistOnDB = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(OPSConfiguration.epodoc);
			// Documents to Insert into databse 
			List<IPublication> documentsToInsert = new ArrayList<IPublication>();
			// Documents already present in DB - Just to add to Query
			List<IPublication> documentsThatAlreayInDB = new ArrayList<IPublication>();
			// Step Document retrieved
			List<IPublication> pubs  = OPSUtils.getSearch(tokenaccess,query,step);
			for(IPublication pub:pubs)
			{
				String epoDocPMID = PublicationImpl.getPublicationExternalIDForSource(pub,OPSConfiguration.epodoc);
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
				report.incrementDocumentRetrieval(1);
				if(!pub.getAbstractSection().isEmpty())
					abstractAvailable ++;
				nPubs++;
			}
			
			if( !cancel && documentsToInsert.size()!=0)
			{
				getPatentDetails(report, epodocAlreadyExistOnDB, documentsToInsert);
				InitConfiguration.getDataAccess().addPublications(documentsToInsert);
			}
			
			List<IPublication> publications = new ArrayList<>();
			publications.addAll(documentsToInsert);
			publications.addAll(documentsThatAlreayInDB);
			if(!cancel)
				InitConfiguration.getDataAccess().addQueryPublications(this.query, publications );
			this.query.setAvailableAbstracts(abstractAvailable);
			this.query.setPublicationsSize(nPubs);
			try {
				if(autentication==null && !cancel)
				{
					System.out.println("sleeping...62 seconds");
					Thread.sleep(62000);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
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

	private void getPatentDetails(IIRSearchProcessReport report,Map<String, Long> epodocAlreadyExistOnDB,
			List<IPublication> pubs) throws ANoteException, InternetConnectionProblemException
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
	public long getID() {
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
	
}
