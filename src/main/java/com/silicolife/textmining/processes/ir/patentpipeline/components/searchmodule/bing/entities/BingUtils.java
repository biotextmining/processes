package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONObject;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class BingUtils {



	public static InputStream getInputStreamJSON(URI uri, String tokenAccess) throws MalformedURLException, IOException{
		HttpURLConnection conn;
		InputStream result=null;
		conn = (HttpURLConnection) uri.toURL().openConnection();
		conn.setRequestProperty("Ocp-Apim-Subscription-Key", tokenAccess);
		conn.setRequestMethod("GET");
		conn.setDoOutput(true);
		conn.connect();
		conn.setReadTimeout(20000);
		if (conn.getResponseMessage()!=null){
			 result = conn.getInputStream();
		}
		return result;
	}





	@SuppressWarnings("unchecked")
	public static Set<String> createJSONFile(InputStream inputStreamObject) throws ANoteException{
		Set<String> urls= new HashSet<>(); 
		try{
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> jsonMap;
			jsonMap = mapper.readValue(inputStreamObject, Map.class);
			JSONObject json = new JSONObject(jsonMap);
			LinkedHashMap<String,String> value = (LinkedHashMap<String, String>) json.get("webPages");
			json=new JSONObject(value);
			//			Integer totalResults =new Integer(json.get("totalEstimatedMatches").toString());
			ArrayList<LinkedHashMap<String, String>> a = (ArrayList<LinkedHashMap<String, String>>) json.get("value");
			for (LinkedHashMap<String, String> entry:a){
				urls.add(entry.get("displayUrl"));
			}
		}catch (IOException e) {
			throw new ANoteException(e);
		}
		return urls;

	}



	@SuppressWarnings("unchecked")
	public static Integer getNumberofResults(InputStream inputStreamObject) throws ANoteException{
		Integer totalResults;
		try{
			ObjectMapper mapper = new ObjectMapper();
			Map<String, Object> jsonMap;
			jsonMap = mapper.readValue(inputStreamObject, Map.class);
			JSONObject json = new JSONObject(jsonMap);
			LinkedHashMap<String,String> value = (LinkedHashMap<String, String>) json.get("webPages");
			JSONObject newJson = new JSONObject(value);
			totalResults =new Integer(newJson.get("totalEstimatedMatches").toString());

		}catch (IOException e) {
			throw new ANoteException(e);
		}
		return totalResults;
	}
}
