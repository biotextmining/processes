package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Set;

import org.apache.http.HttpHost;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;


public abstract class ABingQuery {

	private String q;
	public static final String bingsearch = "Bing Patent Search";
	private String mkt = "en-US";
	private AZURESEARCH_QUERYADULT safeSearch = null;
	private String _appid;
	private Integer count =50;//MAX
	private String cc = "";
	private AZURESEARCH_FRESHNESS freshness;
	private String setLang="EN";
	private boolean textDecorations;
	private String textFormat;
	private Integer offset = 0;
	private int MAX_URL_LENGTH = 2048;


	/**
	 * Filter search results by age. Age refers to the date and time that the webpage was discovered by Bing..   
	 * @author Tiago
	 *
	 */
	public static enum AZURESEARCH_FRESHNESS {
		/**
		 * Return webpages discovered within the last 24 hours
		 */
		DAY,

		/**
		 * Return webpages discovered within the last 7 days
		 */
		WEEK,

		/**
		 * Return webpages discovered within the last 30 days
		 */
		MONTH

	}

	/**
	 * A comma-delimited list of answers to include in the response. If you do not specify this parameter, the response will include all search answers for which there's data. 
	 * @author Tiago
	 *
	 */
	public static enum AZURESEARCH_RESPONSEFILTER {

		Computation,

		Images,

		News,

		RelatedSearches,

		SpellSuggestions,

		TimeZone,

		Videos,

		Webpages
	}



	/**
	 * Choose whether or not to limit out objectionable content
	 */
	public static enum AZURESEARCH_QUERYADULT {

		/**
		 * Show everything
		 */
		OFF,

		/**
		 * Show some content that might be considered objectionable
		 */
		MODERATE,

		/**
		 * Show almost nothing that might be objectionable
		 */
		STRICT
	};


	/**
	 * HTTP or HTTPS
	 */
	public static final String AZURESEARCH_SCHEME = "https";

	/**
	 * What port to connect to
	 */
	public static final Integer AZURESEARCH_PORT = 443;

	/**
	 * Azure's search hostname
	 */
	public static final String AZURESEARCH_AUTHORITY = "api.cognitive.microsoft.com";

	/**
	 * Azure's search path
	 */
	public static final String AZURESEARCH_PATH = "/bing/v5.0/search";


	/**
	 * HttpHost that represents where to post queries
	 */
	protected HttpHost _targetHost = new HttpHost(AZURESEARCH_AUTHORITY,
			AZURESEARCH_PORT, AZURESEARCH_SCHEME);

	/**
	 * Cache the auth
	 */
	protected AuthCache _authCache = new BasicAuthCache();

	/**
	 *
	 */
	protected BasicScheme _basicAuth = new BasicScheme();

	/**
	 *
	 */
	protected BasicHttpContext _localcontext = new BasicHttpContext();


	/**
	 * @return the level of objectionable content
	 */
	protected AZURESEARCH_QUERYADULT getAdult() {
		return safeSearch;
	}

	/**
	 * @param adult the level of objectionable content to set
	 */
	protected void setAdult(AZURESEARCH_QUERYADULT adult) {
		safeSearch = adult;
	}


	public AZURESEARCH_FRESHNESS getFreshness() {
		return freshness;
	}

	public void setFreshness(AZURESEARCH_FRESHNESS freshness) {
		this.freshness = freshness;
	}


	/**
	 *
	 * @return the appropriate path
	 */
	public String getPath() {
		return AZURESEARCH_PATH;
	}

	/**
	 *
	 * @return the query path from {@link getPath()} plus any class-specific query parameters
	 */
	public abstract String getQueryPath();


	/**
	 *
	 * @return The Bing code for what language market to focus results on
	 */
	public String getMarket() {
		return mkt;
	}

	/**
	 *
	 * @param _market The Bing code for what language market to focus results on
	 */
	public void setMarket(String _market) {
		this.mkt = _market;
	}


	/**
	 * @return the The querystring to search for
	 */
	public String getQuery() {
		return q.replace("&", "%26");
	}

	/**
	 * @param query The querystring to search for
	 */
	public void setQuery(String query) {
		q = query;
	}

	/**
	 * @return the number of results per page to ask Bing to return.  Default is 15.
	 */
	public Integer getPerPage() {
		return count;
	}

	/**
	 * @param perPage Number of results per page to ask Bing to return.  Default is 15.
	 */
	public void setPerPage(Integer perPage) {
		count = perPage;
	}



	/**
	 * Basic constructor, creates a basic context to do a query to be defined later
	 */
	public ABingQuery() {
		super();

		// Generate BASIC scheme object and add it to the local
		// auth cache
		_authCache.put(_targetHost, _basicAuth);

		// Add AuthCache to the execution context
		_localcontext.setAttribute(ClientContext.AUTH_CACHE, _authCache);
	}

	/**
	 *
	 * @return The URL querystring that represents this query and its various options
	 */
	public String getUrlQuery() {

		StringBuilder sb = new StringBuilder();
		sb.append("q=");
		sb.append(this.getQuery());
		//        sb.append("'");

		if (safeSearch != null) {
			sb.append("&safeSearch=");
			sb.append(adultToParam(this.getAdult()));
			//            sb.append("'");
		}

		if (!this.getMarket().equals("")) {
			sb.append("&mkt=");
			sb.append(this.getMarket());
			//            sb.append("'");
		}
		sb.append("&count=");
		sb.append(this.getPerPage());

		if (this.getSkip() > 0) {
			sb.append("&offset=");
			sb.append(this.getSkip());
		}

		if (this.freshness!=null){
			sb.append("&freshness=");
			sb.append(freshnessToParam(this.freshness));

		}

		if (!this.cc.equals("")){
			sb.append("&cc=");
			sb.append(this.getCc());
		}

		if (!this.setLang.equals("")){
			sb.append("&setLang=");
			sb.append(this.getSetLang());

		}

		return sb.toString();
	}


	public static String freshnessToParam(AZURESEARCH_FRESHNESS freshness) {
		if (freshness == null) {
			return "";
		}

		switch (freshness) {
		case DAY:
			return "Day";
		case MONTH:
			return "Month";
		case WEEK:
			return "Week";
		default:
			return "";
		}


	}

	/**
	 * Run the query that has been set up in this instance.
	 * 
	 * * @throws ANoteException 
	 * @return 
	 */
	public Set<String> doQuery() throws ANoteException {
		Set<String> urls = null;
		InputStream inputStreamFile;
		try {
			URI uri = buildURI();
			inputStreamFile = requestDataFromServer(uri);
			if (inputStreamFile!=null){
				urls = BingUtils.createJSONFile(inputStreamFile);
			}
		} catch (IOException | URISyntaxException e) {
			throw new ANoteException(e);
		}

		return urls;



	}


	private URI buildURI () throws URISyntaxException{
		URI uri;
		String full_path = getQueryPath();
		String full_query = getUrlQuery();
		uri = new URI(AZURESEARCH_SCHEME, AZURESEARCH_AUTHORITY, full_path,
				full_query, null);
		uri = new URI(uri.getScheme() + "://" + uri.getAuthority()
		+ uri.getPath() + "?"
		+ uri.getQuery());
		return uri;
	}


	private InputStream requestDataFromServer (URI uri) throws MalformedURLException, IOException{
		InputStream inputStreamFile=null;
		if (uri.toASCIIString().length()<MAX_URL_LENGTH){
			inputStreamFile = BingUtils.getInputStreamJSON(uri, _appid);
		}
		return inputStreamFile;

	}


	public int getNumberOfResults () throws ANoteException{
		InputStream inputStreamFile;
		int numResults=0;
		try {
			URI uri = buildURI();
			inputStreamFile = requestDataFromServer(uri);
			if (inputStreamFile!=null){
				numResults = BingUtils.getNumberofResults(inputStreamFile);
			}
		} catch (IOException | URISyntaxException | ANoteException e) {
			throw new ANoteException(e);
		}
		return numResults;
	}

	/**
	 *
	 * @return the Azure Appid
	 */
	public String getAppid() {
		return _appid;
	}

	/**
	 *
	 * @param appid the Azure Appid
	 */
	public void setAppid(String appid) {
		_appid = appid;
	}


	/**
	 *
	 * @param adult from the AZURESEARCH_QUERYADULT enum
	 * @return the string representation of the enum selection that the URL is expecting
	 */
	public static String adultToParam(AZURESEARCH_QUERYADULT adult) {
		if (adult == null) {
			return "Off";
		}

		switch (adult) {
		case OFF:
			return "Off";
		case MODERATE:
			return "Moderate";
		case STRICT:
			return "Strict";
		default:
			return "Off";
		}

	}


	/**
	 *
	 * @return the number of results to skip for pagination
	 */
	public Integer getSkip() {
		return offset;
	}



	public String getCc() {
		return cc;
	}

	public void setCc(String cc) {
		this.cc = cc;
	}

	public String getSetLang() {
		return setLang;
	}

	public void setSetLang(String setLang) {
		this.setLang = setLang;
	}

	public boolean isTextDecorations() {
		return textDecorations;
	}

	public void setTextDecorations(boolean textDecorations) {
		this.textDecorations = textDecorations;
	}

	public String getTextFormat() {
		return textFormat;
	}

	public void setTextFormat(String textFormat) {
		this.textFormat = textFormat;
	}

	/**
	 *
	 * @param skip the number of results to skip for pagination
	 */
	public void setSkip(Integer skip) {
		offset = skip;
		if (offset < 0) {
			offset = 0;
		}
	}

	/**
	 * Set this query to ask for the next page of results
	 */
	public void nextPage() {
		this.setSkip(this.getSkip() + this.getPerPage());
	}

	/**
	 *
	 * @param page Ask for a particular page of results for this query 
	 */
	public void setPage(int page) {
		this.setSkip(this.getPerPage() * (page - 1));
	}

}
