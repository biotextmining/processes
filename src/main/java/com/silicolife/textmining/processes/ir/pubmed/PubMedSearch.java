package com.silicolife.textmining.processes.ir.pubmed;

import java.io.IOException;
import java.net.Proxy.Type;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodBase;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.documents.query.QueryImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryOriginTypeImpl;
import com.silicolife.textmining.core.datastructures.documents.query.QueryPublicationRelevanceImpl;
import com.silicolife.textmining.core.datastructures.exceptions.PubmedException;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IRProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchReportImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IRSearchUpdateReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.relevance.IQueryPublicationRelevance;
import com.silicolife.textmining.core.interfaces.core.report.IReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearch;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.IQuery;
import com.silicolife.textmining.core.interfaces.process.IR.IQueryOriginType;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.pubmed.configuration.IIRPubmedSearchConfiguration;
import com.silicolife.textmining.processes.ir.pubmed.utils.ESearchContext;
import com.silicolife.textmining.processes.ir.pubmed.utils.PMSearch;

public class PubMedSearch extends IRProcessImpl implements IIRSearch{
	
	/**
	 * Logger
	 */
	static Logger logger = Logger.getLogger(PubMedSearch.class.getName());
	static{
		Logger.getLogger(HttpMethodBase.class).setLevel(Level.OFF);	
	}

	private static final long timeToReconnect = 1000000; 
	
	private int nAbstracts = 0;
	private int nPublicacoes = 0;

	private int nPubs;
	private int actuaPubs;
	private boolean cancel = false;
	private IQuery query;
	
	private long lastQuery = System.currentTimeMillis();
	
	public PubMedSearch() {
		super();
	}

	public IIRSearchProcessReport search(IIRSearchConfiguration searchConfiguration) throws ANoteException,InvalidConfigurationException, InternetConnectionProblemException
	{
		cancel = false;
		nAbstracts = 0;
		nPublicacoes = 0;
		nPubs = 0;
		actuaPubs = 0;
		query = null;
		validateConfiguration(searchConfiguration);
		return normalSearch((IIRPubmedSearchConfiguration) searchConfiguration);
	}

	public IDocumentSet getDocuments() { return null;}
	
	public List<IPublication> getPublicationDocuments(){ return null;}
	
	private List<IPublication> getPubmedArticlesInRange(String webEnv, String queryKey, int indexMin, int step,IReport report) throws InternetConnectionProblemException {
		waitIfNecessary();
		HttpClient client = getHTTPGeneralConfiguration();
		PostMethod post = configurationHttp(webEnv, queryKey, indexMin,step);
		return getPubmedResults(client, post,report);		
	}

	private HttpClient getHTTPGeneralConfiguration() {
		HttpClient client = new HttpClient();
		client.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
		// setting property
		if(InitConfiguration.getProxy()!=null && !InitConfiguration.getProxy().type().equals(Type.DIRECT))
			client.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY,InitConfiguration.getProxy());
		return client;
	}


	private List<IPublication> getPubmedResults(HttpClient client,PostMethod post,IReport report) throws InternetConnectionProblemException{
		int retries = 0;
		while(retries < PubMedConfiguration.numberOFRetries && !cancel)
		{
			try {
				client.executeMethod(post);
				return PMSearch.readXMLResultFile(post);
			} catch (ANoteException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}catch (IOException e) {
				retries++;
				logger.warn(e.getMessage() + "...Retry:"+(retries+1));
			}	
		}
		if(cancel)
			return new ArrayList<IPublication>();
		throw new InternetConnectionProblemException(new PubmedException("The Pubmed search was not completed for server problems"),report);
	}

	
	/**
	 * Method that return the number of publications for query
	 * 
	 * @return number of publications
	 */
	public int getExpectedQueryResults(String query) throws InternetConnectionProblemException{
		return getResultCount(query);
	}
	
	private IIRSearchProcessReport normalSearch(IIRPubmedSearchConfiguration configuration) throws ANoteException, InternetConnectionProblemException {
		String querySTR = buildQuery(configuration.getKeywords(),configuration.getOrganism(),configuration.getProperties());
		nPubs = getExpectedQueryResults(querySTR);
		Date date = new Date();
		String name = generateQueryName(configuration,date);
		IQueryOriginType queryType = new QueryOriginTypeImpl(PublicationSourcesDefaultEnum.PUBMED.name());
		query = new QueryImpl(queryType, date , configuration.getKeywords(),configuration.getOrganism(), querySTR, 0, 0, name, new String(),new HashMap<Long, IQueryPublicationRelevance>(), configuration.getProperties());
		IIRSearchProcessReport report = searchMethod(query);
		if(cancel)
			report.setcancel();
		return report;
	}

	
	private IIRSearchProcessReport searchMethod(IQuery query) throws ANoteException, InternetConnectionProblemException {
		registerQueryOnDatabase(query);
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IIRSearchProcessReport report = new IRSearchReportImpl(query);
		searchPubmed(query,report);
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}

	protected void registerQueryOnDatabase(IQuery query) throws ANoteException {
		InitConfiguration.getDataAccess().createQuery(query);
	}
	
	
	
	private String generateQueryName(IIRPubmedSearchConfiguration configuration,Date date) {
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


	private void searchPubmed(IQuery query,IIRSearchProcessReport report) throws ANoteException, InternetConnectionProblemException {
		int total = getExpectedQueryResults(query.getCompleteQuery());
		ESearchContext context = PMSearch.query(query.getCompleteQuery());
		this.lastQuery = System.currentTimeMillis();
		long startQuery = System.currentTimeMillis();
		Set<IPublication> documentsToInsert,documentsThatAlreayInDB;
		int abs_count;
		for(int i=0;i<total &&!cancel;i=i+PubMedConfiguration.blockSearchSize)
		{
			long nowTime = System.currentTimeMillis();
			if(nowTime-startQuery>timeToReconnect)
			{
				context = PMSearch.query(query.getCompleteQuery());
			}
			abs_count = 0;
			int step = PubMedConfiguration.blockSearchSize;
			if(i+PubMedConfiguration.blockSearchSize > total)
			{
				step = total - PubMedConfiguration.blockSearchSize;
			}
			// Get Block publication from Pubmed
			List<IPublication> publications = getPubmedArticlesInRange(context.getWebEnv(), context.getQueryKey(), i,step,report);
			// Previously download the existent documentID for PMID,PMC and DOI documents from System and Query
			Map<String, Long> pmidsAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.PUBMED.name());
			Set<String> pmidsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.PUBMED.name());
			Map<String, Long> doiAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.DOI.name());
			Set<String> doisAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.DOI.name());
			Map<String, Long> pmcAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.pmc.name());
			Set<String> pmcsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.pmc.name());
			// Block Ids processed
			Set<Long> alreadyAdded = new java.util.HashSet<>();
			documentsToInsert = new HashSet<>();
			documentsThatAlreayInDB = new HashSet<>();
			for(IPublication pub:publications)
			{
				// Get ID from publication
				String pubPMID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.PUBMED.name());
				String pubDOI = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.DOI.name());
				String pubPMC = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.pmc.name());
				if(pmidsAlreadyExistOnQuery.contains(pubPMID) ||
						pubDOI!=null && !pubDOI.isEmpty() && doisAlreadyExistOnQuery.contains(pubDOI) ||
						pubPMC!=null && !pubPMC.isEmpty() && pmcsAlreadyExistOnQuery.contains(pubPMC))
				{
	
				}
				// Test if PMID already exists in System
				else if(pmidsAlreadyExistOnDB.containsKey(pubPMID))
				{
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(pmidsAlreadyExistOnDB.get(pubPMID)))
					{
						// If publication already exist in system give it the system ID
						pub.setId(pmidsAlreadyExistOnDB.get(pubPMID));
						// Add to already exist list to later add to the query
						documentsThatAlreayInDB.add(pub);
						// Add if to The Ids block
						alreadyAdded.add(pmidsAlreadyExistOnDB.get(pubPMID));
						// Add new Document Relevance - Default
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
						// Test is abstract is available an increase report
						if(!pub.getAbstractSection().isEmpty())
							abs_count ++;
						report.incrementDocumentRetrieval(1);
					}
				}
				// Test if DOI already exists in System
				else if(pubDOI!=null && !pubDOI.isEmpty() && doiAlreadyExistOnDB.containsKey(pubDOI))
				{
					if(!alreadyAdded.contains(doiAlreadyExistOnDB.get(pubDOI)))
					{
						pub.setId(doiAlreadyExistOnDB.get(pubDOI));
						documentsThatAlreayInDB.add(pub);
						alreadyAdded.add(doiAlreadyExistOnDB.get(pubDOI));
						// Add new Document Relevance - Default
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
						if(!pub.getAbstractSection().isEmpty())
							abs_count ++;
						report.incrementDocumentRetrieval(1);
					}
				}
				// Test if PMC already exists in System
				else if(pubPMC!=null && !pubPMC.isEmpty() && pmcAlreadyExistOnDB.containsKey(pubPMC))
				{
					if(!alreadyAdded.contains(pmcAlreadyExistOnDB.get(pubPMC)))
					{
						pub.setId(pmcAlreadyExistOnDB.get(pubPMC));
						documentsThatAlreayInDB.add(pub);
						alreadyAdded.add(pmcAlreadyExistOnDB.get(pubPMC));
						// Add new Document Relevance - Default
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
						if(!pub.getAbstractSection().isEmpty())
							abs_count ++;
						report.incrementDocumentRetrieval(1);
					}
				}
				else
				{
					pmidsAlreadyExistOnDB.put(pubPMID, pub.getId());
					if(pubDOI!=null && !pubDOI.isEmpty())
						doiAlreadyExistOnDB.put(pubDOI,  pub.getId());
					if(pubPMC!=null && !pubPMC.isEmpty())
						pmcAlreadyExistOnDB.put(pubPMC,  pub.getId());
					documentsToInsert.add(pub);
					query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					if(!pub.getAbstractSection().isEmpty())
						abs_count ++;
					report.incrementDocumentRetrieval(1);
				}
			}
			// Insert publications in System
			if(!cancel && documentsToInsert.size()!=0){
				insertPublications(documentsToInsert);
			}
			
			
			Set<IPublication> publicationToAdd = new HashSet<>();
			publicationToAdd.addAll(documentsThatAlreayInDB);
			publicationToAdd.addAll(documentsToInsert);
			if(!cancel)
				insertQueryPublications(query, publicationToAdd);
			// update Query publication size (In memory)
			if(!cancel)
			{
				addToCounts(publicationToAdd.size(), abs_count);
				query.setAvailableAbstracts(nAbstracts);
				query.setPublicationsSize(nPublicacoes);
				// Update Query
				updateQueryOnDatabase(query);
			}
			memoryAndProgress(i+PubMedConfiguration.blockSearchSize,nPubs);
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
	    
    protected String buildQuery(String keywords,String organism,Properties properties){	
    	String query = "";
    	
       	if(!keywords.equals(""))
    	    query = " (" + keywords + ") ";

    	if(!organism.equals(""))
    	    query = query + getOrganismoName(organism);

    	if(properties.getProperty("authors")!=null)
    	    query = " (" + properties.get("authors") + "[Author]) AND" + query;
    	
    	if(properties.getProperty("journal")!=null)
    	    query = " (\"" + properties.get("journal") + "\"[Journal]) AND" + query;
    	if(properties.getProperty("fromDate")==null && properties.getProperty("toDate") != null)
    	{
    		query = " (\"" + GlobalOptions.searchYearStarting + "\"[Publication date]:\" "+ properties.get("toDate") +"\"[Publication Date]) AND " + query;
    	}
    	else if(properties.getProperty("fromDate")==null && properties.getProperty("toDate") == null)
    	{
    		query = " (\"" + GlobalOptions.searchYearStarting + "\"[Publication date]:\"3000\"[Publication Date]) AND " + query;
    	}
    	else if(properties.getProperty("toDate")==null)
    	{
        	query = " (\"" + properties.get("fromDate") + "\"[Publication date]:\"3000\"[Publication Date]) AND " + query;
    	}	
    	else
    	{
    	    query = " (\"" + properties.get("fromDate") + "\"[Publication date]:\""+properties.get("toDate")+"\"[Publication Date]) AND " + query;
    	}
       	if(properties.getProperty("articleDetails")!=null)
    	{
       		String articleDetails = properties.getProperty("articleDetails");
       		
       		if(articleDetails.equals("abstract"))
       		{
       			query = " (hasabstract[text]) AND"+ query;
       		}
       		else if(articleDetails.equals("freefulltext"))
       		{
       			query = " (free full text[sb]) AND"+ query;
       		}
       		else
       		{
       			query = " (full text[sb]) AND"+query;
       		}
    	}
    	if(properties.getProperty("ArticleSource")!=null)
    	{
    		String medpmc = properties.getProperty("ArticleSource");
    		if(medpmc.equals("medpmc"))
    		{
    			query = " ((medline[sb] OR pubmed pmc local[sb])) AND"+ query;
    		}
    		else if(medpmc.equals("med"))
    		{
    			query = " ((medline[sb])) AND"+ query;
    		}
    		else
    		{
    			query = " ((pubmed pmc local[sb])) AND"+ query;
    		}
    	}
    	if(properties.getProperty("articletype")!=null)
    	{
    		query = " (("+properties.get("articletype")+"[ptyp])) AND"+query;
    	}
       	if(query.endsWith("AND"))
    	{
    		query = query.substring(0,query.length()-3);
    	}
    	return query;
    }
    
	private String getOrganismoName(String orgaSTream) {
		
		String[] organisms = orgaSTream.split("AND|OR");
		String changeOrganism;
		for(String organism:organisms)
		{
			changeOrganism = parse(organism);
			orgaSTream = orgaSTream.replace(organism, changeOrganism);
		}
		orgaSTream = orgaSTream.replace("AND"," AND ");
		orgaSTream = orgaSTream.replace("OR"," OR ");
		return 	" AND  (" + orgaSTream +")";		
	}
	
	private String parse(String organism){

		Pattern p = Pattern.compile("\\s*\\w\\.\\s\\w+\\s*");
		Matcher m;
		String organism2 = organism;
		organism = organism.replace("\"","");

		m = p.matcher(organism);
		
		if(m.find())
			return "\"" + organism + "\"";
		else
		{
			p = Pattern.compile("\\s*(\\w+)\\s(\\w+)\\s*");
			m = p.matcher(organism);
			if(m.find())
			{
				String newOrgan =  "(\"" + m.group(1).charAt(0) + ". " + m.group(2) + "\"OR\"" + m.group(1)+" "+m.group(2) + "\")";	

				String ret = organism2.replace(organism, newOrgan);

				return ret;
			}
			else
				return organism;
		}
	}
        
		
	private int getResultCount(String term) throws InternetConnectionProblemException
	{
		ESearchContext context = PMSearch.query(term);
		return context.getCount();
	}

	private void waitIfNecessary() {

		while (System.currentTimeMillis() - lastQuery < PubMedConfiguration.timeToWaitbetweenQueries) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		lastQuery = System.currentTimeMillis();
	}

	

	private static PostMethod configurationHttp(String webEnv, String queryKey,
			int start, int max) {

		PostMethod post = new PostMethod(
				"https://www.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi/");

		NameValuePair[] data = { 
				new NameValuePair("db", "pubmed"),
				new NameValuePair("WebEnv", webEnv),
				new NameValuePair("query_key", queryKey),
				new NameValuePair("retstart", Integer.toString(start)),
				new NameValuePair("retmax", Integer.toString(max)),
				new NameValuePair("retmode", "xml") 
		};
		post.setRequestBody(data);
		return post;
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
			
	public IIRSearchUpdateReport updateQuery(IQuery query) throws ANoteException, InternetConnectionProblemException  
	{
		cancel = false;
		nAbstracts = 0;
		nPublicacoes = 0;
		nPubs = 0;
		actuaPubs = 0;	
		return normalUpdate(query);
	}

	private IIRSearchUpdateReport normalUpdate(IQuery query) throws ANoteException, InternetConnectionProblemException {
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();	
		nPubs = getExpectedQueryResults(query.getCompleteQuery());
		IIRSearchUpdateReport report = searchPubmedUpdate(query);
		long endtime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endtime-starttime);
		if(cancel)
			report.setcancel();
		return report;
	}

	private  IIRSearchUpdateReport searchPubmedUpdate(IQuery query) throws ANoteException, InternetConnectionProblemException {
		String querySTR = query.getCompleteQuery();
		int total = getExpectedQueryResults(querySTR);
		ESearchContext context = PMSearch.query(querySTR);
		this.lastQuery = System.currentTimeMillis();
		Set<IPublication> documentsToInsert;
		int abstractsAvailable = 0;
		IIRSearchUpdateReport report = new IRSearchUpdateReportImpl(query);
		for(int i=0;i<total &&!cancel;i=i+PubMedConfiguration.blockSearchSize)
		{
			abstractsAvailable = 0;
			int step = PubMedConfiguration.blockSearchSize;
			if(i+PubMedConfiguration.blockSearchSize > total)
			{
				step = total - PubMedConfiguration.blockSearchSize;
			}
			// Get Block publication from Pubmed
			List<IPublication> documents = getPubmedArticlesInRange(context.getWebEnv(), context.getQueryKey(), i,step,report);
			Map<String, Long> pmidsAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.PUBMED.name());
			Set<String> pmidsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.PUBMED.name());
			Map<String, Long> doiAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.DOI.name());
			Set<String> doisAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.DOI.name());
			Map<String, Long> pmcAlreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.pmc.name());
			Set<String> pmcsAlreadyExistOnQuery = getQueryPublicationIDWithGivenSource(query, PublicationSourcesDefaultEnum.pmc.name());
			Set<Long> alreadyAdded = new java.util.HashSet<>();
			List<IPublication> newQueryDocuments = new ArrayList<IPublication>();
			documentsToInsert = new HashSet<>();
			for(IPublication pub:documents)
			{
				// Get ID's from publication
				String pubPMID = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.PUBMED.name());
				String pubDOI = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.DOI.name());
				String pubPMC = PublicationImpl.getPublicationExternalIDForSource(pub,PublicationSourcesDefaultEnum.pmc.name());
				// Test if publication already in Query
				if(pmidsAlreadyExistOnQuery.contains(pubPMID) ||
						pubDOI!=null && !pubDOI.isEmpty() && doisAlreadyExistOnQuery.contains(pubDOI) ||
						pubPMC!=null && !pubPMC.isEmpty() && pmcsAlreadyExistOnQuery.contains(pubPMC))
				{
	
				}
				// Test if PMID already exists in System
				else if(pmidsAlreadyExistOnDB.containsKey(pubPMID))
				{
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(pmidsAlreadyExistOnDB.get(pubPMID)))
					{
						// If publication already exist in system give it the system ID
						pub.setId(pmidsAlreadyExistOnDB.get(pubPMID));
						// Add To Document that already exist
						newQueryDocuments.add(pub);
						// Update abstract and publication for Query
						if(!pub.getAbstractSection().isEmpty())
							abstractsAvailable ++;
						// Increase report
						report.incrementDocumentRetrieval(1);
						// Add publication to blockIds
						alreadyAdded.add(pmidsAlreadyExistOnDB.get(pubPMID));
						// Add default relevance
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					}
				}
				// Test if DOI already exists in System
				else if(pubDOI!=null && !pubDOI.isEmpty() && doiAlreadyExistOnDB.containsKey(pubDOI))
				{
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(doiAlreadyExistOnDB.get(pubDOI)))
					{
						pub.setId(doiAlreadyExistOnDB.get(pubDOI));
						newQueryDocuments.add(pub);
						if(!pub.getAbstractSection().isEmpty())
							abstractsAvailable ++;
						report.incrementDocumentRetrieval(1);
						alreadyAdded.add(doiAlreadyExistOnDB.get(pubDOI));
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					}
				}
				// Test if PMC already exists in System
				else if(pubPMC!=null && !pubPMC.isEmpty() && pmcAlreadyExistOnDB.containsKey(pubPMC))
				{
					// Test if Publication already exists in the block
					if(!alreadyAdded.contains(pmcAlreadyExistOnDB.get(pubDOI)))
					{
						pub.setId(pmcAlreadyExistOnDB.get(pubPMC));
						newQueryDocuments.add(pub);
						if(!pub.getAbstractSection().isEmpty())
							abstractsAvailable ++;
						report.incrementDocumentRetrieval(1);
						alreadyAdded.add(pmcAlreadyExistOnDB.get(pubDOI));
						query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
					}
				}
				else
				{
					documentsToInsert.add(pub);
					if(!pub.getAbstractSection().isEmpty())
						abstractsAvailable ++;
					report.incrementDocumentRetrieval(1);
					pmidsAlreadyExistOnDB.put(pubPMID, pub.getId());
					if(pubDOI!=null && !pubDOI.isEmpty())
						doiAlreadyExistOnDB.put(pubDOI,  pub.getId());
					if(pubPMC!=null && !pubPMC.isEmpty())
						pmcAlreadyExistOnDB.put(pubPMC,  pub.getId());
					query.getPublicationsRelevance().put(pub.getId(), new QueryPublicationRelevanceImpl());
				}

			}
			// Add publications to the system
			if(!cancel && documentsToInsert.size()>0)
				insertPublications(documentsToInsert);
			Set<IPublication> publicationToAdd = new HashSet<>();
			publicationToAdd.addAll(newQueryDocuments);
			publicationToAdd.addAll(documentsToInsert);
			// Add Query Publications
			if(!cancel)
				insertQueryPublications(query, publicationToAdd);
			// Update Publication data (In memory)
			addToCounts(publicationToAdd.size(), abstractsAvailable);
			query.setPublicationsSize(query.getPublicationsSize()+publicationToAdd.size());
			query.setAvailableAbstracts(query.getAvailableAbstracts()+abstractsAvailable);
			query.getPublications().addAll(publicationToAdd);
			// Update date
			query.setDate(new Date());
			updateQueryOnDatabase(query);
			memoryAndProgress(i+PubMedConfiguration.blockSearchSize,nPubs);
		}
		return report;
	}
	
	public Set<IPublication> searchPublicationMetaInfoUsingPMID(List<String> pmids) throws InternetConnectionProblemException
	{
		this.cancel = false;
		int total = pmids.size();
		Set<IPublication> listAll = new HashSet<>();
		String query = new String();
		for(int i=0;i<total &&!cancel;i++)
		{
			query = query  + pmids.get(i)+"[uid] OR ";
			if(i>0 && i%PubMedConfiguration.searchMetaInfoblockSize+1==0)
			{
				String queryStr = query.substring(0,query.length()-4);
				ESearchContext context = PMSearch.query(queryStr);
				listAll.addAll(getPubmedArticlesInRange(context.getWebEnv(), context.getQueryKey(), 0, context.getCount(),null));
				query = new String();
				memoryAndProgress(i+PubMedConfiguration.searchMetaInfoblockSize, total);
			}
		}
		if(!query.isEmpty())
		{
			String queryStr = query.substring(0,query.length()-4);
			ESearchContext context = PMSearch.query(queryStr);
			listAll.addAll(getPubmedArticlesInRange(context.getWebEnv(), context.getQueryKey(), 0, context.getCount(),null));
			memoryAndProgress(total, total);
		}
		return listAll;
	}

	@Override
	public long getId() {
		return 0;
	}

	@Override
	public IQuery getQuery() {
		return query;
	}

	@Override
	public IProcessOrigin getProcessOrigin() {
		return new ProcessOriginImpl(-1, "Pubmed Search");
	}

	@Override
	public void validateConfiguration(IIRSearchConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof IIRPubmedSearchConfiguration)
		{
			
		}
		else
			throw new InvalidConfigurationException("configuration is not a IIRPubmedSearchConfiguration instance");
		
	}

	
}