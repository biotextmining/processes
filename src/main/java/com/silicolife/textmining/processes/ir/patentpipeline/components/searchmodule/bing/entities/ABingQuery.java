package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.io.input.BOMInputStream;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.client.AuthCache;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.protocol.BasicHttpContext;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

/**
 * The abstract class representing a search query.  The class is intended to have a subclass 
 * instantiated, have a number of fields set on that object, run the {@link doQuery()} method,
 * then get results one page at a time through the {@link getQueryResult()} method.
 * 
 * @param <ResultT> Type of AzureSearch(something)Result that the class will return
 */

public abstract class ABingQuery <ResultT> {

	private String q;
	public static final String bingsearch = "Bing Patent Search";
	private String _queryOption = "";
	private String mkt = "en-US";
	private AZURESEARCH_QUERYADULT safeSearch = null;

	/**
	 * API comes in regular and "Web Only".  This chooses the more expansive option
	 */
	protected AZURESEARCH_API _bingApi = AZURESEARCH_API.BINGSEARCH;

	/**
	 * All of the parsing later is based on XML format.
	 */
	protected AZURESEARCH_FORMAT _format = AZURESEARCH_FORMAT.JSON;
	// private static final Logger log = Logger
	// .getLogger(AbstractAzureSearchQuery.class.getName());
	private BingResultSet<ResultT> _queryResult;
	private Document _rawResult;
	private String _appid;
	private Integer count = 50;//MAX
	private String cc = "";
	private AZURESEARCH_FRESHNESS freshness;
	private String setLang="EN";
	private boolean textDecorations;
	private String textFormat;

	private Integer offset = 0;
	private String _queryExtra = "";

	private Boolean _processHTTPResults = true;
	private Boolean _debug = false;  //Trade an extra copy of the results in memory for better exceptions


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
	public static enum AXURESEARCH_RESPONSEFILTER {

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
	 * Type of query that Bing can understand.  Matches to the AzureSearch(Thing)Query class.
	 */
	public static enum AZURESEARCH_QUERYTYPE {

		/**
		 * Web search
		 */
		WEB
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
	 * Bing's API comes in regular and "Web Only".
	 */
	public static enum AZURESEARCH_API {

		/**
		 * Arbitrary searches
		 */
		BINGSEARCH,

		/**
		 * Web only searches
		 */
		BINGSEARCHWEBONLY
	}

	/**
	 * What format the results should be returned in
	 */
	public static enum AZURESEARCH_FORMAT {

		/**
		 * JSON
		 */
		JSON,

		/**
		 * XML
		 */
		XML
	}

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
	 * Azure's web-only search path
	 */
	public static final String AZURESEARCHWEB_PATH = "/bing/v5.0/search";

	// HTTP objects

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
	 *
	 */
	protected HttpResponse _responsePost;

	/**
	 *
	 */
	protected HttpEntity _resEntity;

	/**
	 * @return the responsePost to get a response entity out of
	 */
	public HttpResponse getResponsePost() {
		return _responsePost;
	}

	/**
	 * @param responsePost the responsePost to get a response entity out of
	 */
	protected void setResponsePost(HttpResponse responsePost) {
		this._responsePost = responsePost;
	}

	/**
	 * @return the resEntity to read a response out of
	 */
	public HttpEntity getResEntity() {
		return _resEntity;
	}

	/**
	 * @param resEntity the response entity to read a response out of
	 */
	protected void setResEntity(HttpEntity resEntity) {
		this._resEntity = resEntity;
	}

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

	/**
	 * @return the set of results to the query
	 */
	public BingResultSet<ResultT> getQueryResult() {
		return _queryResult;
	}

	/**
	 * @param queryResult the queryResult to set
	 */
	protected void setQueryResult(BingResultSet<ResultT> queryResult) {
		_queryResult = queryResult;
	}

	/**
	 *
	 * @return The XML that Bing gave back as a result
	 */
	public Document getRawResult() {
		return _rawResult;
	}

	public AZURESEARCH_FRESHNESS getFreshness() {
		return freshness;
	}

	public void setFreshness(AZURESEARCH_FRESHNESS freshness) {
		this.freshness = freshness;
	}

	/**
	 *
	 * @param _rawResult The XML that Bing gave back as a result
	 */
	public void setRawResult(Document _rawResult) {
		this._rawResult = _rawResult;
	}

	/**
	 *
	 * @return the appropriate path based on whether the class is using general search or web-only
	 */
	public String getPath() {
		switch (_bingApi) {
		case BINGSEARCH:
			return AZURESEARCH_PATH;
		case BINGSEARCHWEBONLY:
			return AZURESEARCHWEB_PATH;
		default:
			return AZURESEARCH_PATH;
		}
	}

	/**
	 *
	 * @return the query path from {@link getPath()} plus any class-specific query parameters
	 */
	public abstract String getQueryPath();

	/**
	 *
	 * @return The Bing options string for this query
	 */
	public String getQueryOption() {
		return _queryOption;
	}

	/**
	 *
	 * @param _queryOption The Bing options string for this query
	 */
	public void setQueryOption(String _queryOption) {
		this._queryOption = _queryOption;
	}

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
	 *
	 * @param entry an XML node representing one search result of a given type
	 * @return a new AzureSearch(Something)Result object that represents the search result
	 */
	public abstract ResultT parseEntry(Node entry);

	// cast _queryresult to the right thing
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
	 *
	 * @return True to use an extra memory segment to hold a copy of the response from Bing in case there's a later exception.  False to keep that memory empty but exceptions do not show the data they were working on.
	 */
	public Boolean getDebug() {
		return _debug;
	}

	/**
	 *
	 * @param _debug True to use an extra memory segment to hold a copy of the response from Bing in case there's a later exception.  False to keep that memory empty but exceptions do not show the data they were working on.
	 */
	public void setDebug(Boolean _debug) {
		this._debug = _debug;
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
	 * @return Class specific additional query parameters to add to the querystring
	 */
	public abstract String getAdditionalUrlQuery();

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


		//        sb.append(this.getAdditionalUrlQuery());

		//        sb.append(this.getQueryExtra());

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
	 * Next step would be to get the results with {@link getQueryResult()}
	 * @throws ANoteException 
	 */
	public void doQuery() throws ANoteException {

		//

		//                .setCredentials(
		//                        new AuthScope(_targetHost.getHostName(),
		//                                _targetHost.getPort()),
		//                        new UsernamePasswordCredentials(this.getAppid(), this
		//                                .getAppid()));

		URI uri;
		try {
			String full_path = getQueryPath();
			String full_query = getUrlQuery();
			uri = new URI(AZURESEARCH_SCHEME, AZURESEARCH_AUTHORITY, full_path,
					full_query, null);
			uri = new URI(uri.getScheme() + "://" + uri.getAuthority()
			+ uri.getPath() + "?"
			+ uri.getQuery()
					);
			// log.log(Level.WARNING, uri.toString());
		} catch (URISyntaxException e1) {

			throw new ANoteException(e1);
		}

		HttpURLConnection conn;
		try {
			conn = (HttpURLConnection) uri.toURL().openConnection();
			conn.setRequestProperty("Ocp-Apim-Subscription-Key", this._appid);
			conn.setRequestMethod("GET");
			conn.setDoOutput(true);
			conn.connect();

			InputStream a = conn.getInputStream();
			File file = new File("TesteBing/");
			if (!file.exists()){
				file.mkdirs();
				
			}
			FileOutputStream outputStream = new FileOutputStream(file.getAbsolutePath()+"\teste.txt");

			int read = 0;
			byte[] bytes = new byte[1024];

			while ((read = a.read(bytes)) != -1) {
				outputStream.write(bytes, 0, read);
			}
			outputStream.close();



			//			_rawResult=loadXMLFromStream(a);
			//			_resEntity = _responsePost.getEntity();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}




		//        HttpGet get =new HttpGet(uri);
		//
		//        get.addHeader("Ocp-Apim-Subscription-Key", this._appid);
		//        get.addHeader("Accept", "application/json");
		//        get.addHeader("Content-Type", "application/json");

		//        try {
		//            _responsePost = client.executeMethod((HttpMethod)get);
		//            _resEntity = _responsePost.getEntity();
		//
		//            if (this.getProcessHTTPResults()) {
		//                _rawResult = loadXMLFromStream(_resEntity.getContent());
		//                this.loadResultsFromRawResults();
		//            }

		// Adding an automatic HTTP Result to String really requires Apache Commons IO. 
		//That would break Android compatibility.

		//        } catch (Exception e) {
		//        	throw new ANoteException(e);
		//        }

	}

	/**
	 * Run only the parsing and object creation parts of the query process, 
	 * assuming that there are already raw results that have been set through
	 * the {@link setRawResult()} method
	 */
	public void loadResultsFromRawResults() {

		////////////////

		//int j = 0;
		if (_rawResult != null) {
			NodeList parseables = _rawResult
					.getElementsByTagName("entry");
			_queryResult = new BingResultSet<ResultT>();
			if (parseables != null) {
				for (int i = 0; i < parseables.getLength(); i++) {
					Node parseable = parseables.item(i);
					ResultT ar = this.parseEntry(parseable);
					if (ar != null) {
						_queryResult.addResult(ar);						
					} 
				}
			}
		}
	}

	/**
	 *
	 * @param is An InputStream holding some XML that needs parsing
	 * @return a parsed Document from the XML in the stream
	 */
	public Document loadXMLFromStream(InputStream is) {
		DocumentBuilderFactory factory;
		DocumentBuilder builder;
		BOMInputStream bis;
		String dumpable = "";
		try {
			factory = DocumentBuilderFactory
					.newInstance();
			builder = factory.newDocumentBuilder();
			bis = new BOMInputStream(is);

			if (_debug) {
				@SuppressWarnings("resource")
				java.util.Scanner s = new java.util.Scanner(bis).useDelimiter("\\A");
				dumpable = s.hasNext() ? s.next() : "";
				// convert String into InputStream
				InputStream istwo = new java.io.ByteArrayInputStream(dumpable.getBytes());

				return builder.parse(istwo);

			} else {
				return builder.parse(bis);


			}


		} catch (IOException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		}
		return null;
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
	 * @return Additional things appended to the querystring
	 */
	public String getQueryExtra() {
		return _queryExtra;
	}

	/**
	 *
	 * @param queryExtra Additional things appended to the querystring.
	 */
	public void setQueryExtra(String queryExtra) {
		_queryExtra = queryExtra;
	}


	/**
	 *
	 * @return whether or not to process the HTTP results
	 */
	public Boolean getProcessHTTPResults() {
		if (this.getFormat() == AZURESEARCH_FORMAT.JSON) {
			return false;
		}
		return _processHTTPResults;
	}

	/**
	 *
	 * @param processHTTPResults whether or not to process the HTTP results
	 */
	public void setProcessHTTPResults(Boolean processHTTPResults) {
		_processHTTPResults = processHTTPResults;
	}

	/**
	 * @return XML or JSON format
	 */
	public AZURESEARCH_FORMAT getFormat() {
		return _format;
	}

	/**
	 * @param format the format to request of Bing.  XML or JSON.
	 */
	public void setFormat(AZURESEARCH_FORMAT format) {
		_format = format;
	}

	/**
	 *
	 * @param node XML data node
	 * @return A string dumping the XML
	 */
	public static String xmlToString(Node node) {
		try {
			Source source = new DOMSource(node);
			StringWriter stringWriter = new StringWriter();
			Result result = new StreamResult(stringWriter);
			TransformerFactory factory = TransformerFactory.newInstance();
			Transformer transformer = factory.newTransformer();
			transformer.transform(source, result);
			return stringWriter.getBuffer().toString();
		} catch (TransformerConfigurationException e) {
			e.printStackTrace();
		} catch (TransformerException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 *
	 * @param type from the AZURESEARCH_QUERYTYPE enum
	 * @return the string representation of the enum selection that the URL is expecting
	 */
	public static String querytypeToUrl(AZURESEARCH_QUERYTYPE type) {
		if (type == null) {
			return "Web";
		}

		switch (type) {
		case WEB:
			return "Web";
		default:
			return "Web";
		}

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
	 * @param format from the AZURESEARCH_FORMAT enum
	 * @return the string representation of the enum selection that the URL is expecting
	 */
	public static String formatToParam(AZURESEARCH_FORMAT format) {
		if (format == null) {
			return "Atom";
		}

		switch (format) {
		case JSON:
			return "JSON";
		case XML:
			return "Atom";
		default:
			return "Atom";
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
	//    
	//    public boolean verifySkip <Integer int1, Integer int2>{
	//    	Map<Integer, Integer> map;
	//    	List<Integer> listk ;
	//    }

}
