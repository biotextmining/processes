package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.googleEntities;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map.Entry;

import com.google.gson.Gson;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;



public class GoogleWebQuery {
	public static String CHAR_SET = "UTF-8";
	public static String GOOGLE_API_URL = "https://www.googleapis.com/customsearch/v1?";
	//public static String GOOGLE_API_URL= "http://www.google.com/custom?";
	public static final String googlesearch = "Google Custom Search";
	private String APIKey;
	private String customSearchEngineID;
	private HashMap<String,String>extraParams;
	private int timeout = 20000;

	/**
	 * Create a GoogleSearch with the specified api and cx
	 * @param APIkey your Google API key
	 * @param cx your Google Custom Search Engine id
	 */
	public GoogleWebQuery(String APIkey, String cx){
		this.APIKey = APIkey;
		customSearchEngineID = cx;
	}


	/**
	 * Index of the first result.
	 * @param start a number between 1 and 101-num (where <b>num</b> is the number of results to return)
	 */
	public void setStartIndexOfResult(int start){
		if(start<1)start=1;
		else if(start>101-getNumberOfResults())start=101-getNumberOfResults();

		addPair("start", ""+start);

	}

	/**
	 * Set how many results return. 
	 * @param n an integer between 1 and 10 (max). 
	 */
	public void setNumberOfResults(int n){
		//if(n<1)n=1;
		//else if(n>10)n=10;

		addPair("num", ""+n);
	}

	/**
	 * Add a custom extra params to the query (Read Google api docs https://developers.google.com/custom-search/v1/using_rest)
	 * @param key
	 * @param value
	 */
	public void addExtraParam(String key, String value){
		if(key!=null && value!=null)
			addPair(key, value);
	}

	/**
	 * Remove all extra params
	 */
	public void clearExtraParams(){
		extraParams = null;
	}


	private void addPair(String k, String v){
		if(extraParams == null) extraParams=new HashMap<String, String>();
		extraParams.put(k, v);
	}

	private String generateExtraParams(){
		String params ="";

		if(extraParams !=null){
			for(Entry<String, String> e  : extraParams.entrySet())
				params += "&"+e.getKey()+"="+e.getValue();
		}

		return params;
	}


	/**
	 * Search using the specified keywords
	 * @param keywords
	 * @return
	 * @throws ANoteException 
	 */
	public GoogleResults search(String keywords) throws ANoteException{
		if(keywords==null)return null;

		String google_arguments= "&key="+APIKey+"&cx="+customSearchEngineID+"&alt=json"+generateExtraParams()+"&q=";
		URL url;
		String keywords_encoded;
		try {
			keywords_encoded = URLEncoder.encode(keywords, CHAR_SET);
			url = new URL(GOOGLE_API_URL + google_arguments + keywords_encoded);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			conn.setRequestProperty("Accept", "application/json");
			conn.setReadTimeout(timeout );
			conn.setConnectTimeout(timeout);
			BufferedReader br = new BufferedReader(new InputStreamReader(
					(conn.getInputStream())));
			GoogleResults results = new Gson().fromJson(br, GoogleResults.class);
			return results;
		} catch (UnsupportedEncodingException e) {
			throw new ANoteException(e);
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
		} catch (ProtocolException e) {
			throw new ANoteException(e);
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}


	public String getCustomSearchEngineID() {
		return customSearchEngineID;
	}

	public void setCustomSearchEngineID(String customSearchEngineID) {
		this.customSearchEngineID = customSearchEngineID;
	}

	public String getAPIKey() {
		return APIKey;
	}

	public void setAPIKey(String aPIKey) {
		APIKey = aPIKey;
	}

	/**
	 * Get how many result will be returned
	 * @return
	 */
	public int getNumberOfResults() {
		if(extraParams!=null){
			String r= extraParams.get("num");
			if(r!=null)return Integer.parseInt(r);
		}
		return 10;
	}


}