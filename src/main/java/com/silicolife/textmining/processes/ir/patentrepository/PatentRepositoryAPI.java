package com.silicolife.textmining.processes.ir.patentrepository;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.PatentEntity;

public class PatentRepositoryAPI {
	
	private static int timeout = 60000;

	
	public static Set<String> getPatentIdsGivenTextQuery(String url,String query) throws MalformedURLException, IOException {
		ObjectMapper mapper = new ObjectMapper();
		URL u = new URL(url + "/search/patentkeywords/");
		HttpURLConnection conn = (HttpURLConnection) u.openConnection();
		conn.setDoOutput(true);
		conn.setRequestMethod("POST");
		String type = "application/json";
		conn.setRequestProperty( "Content-Type", type );
		conn.setRequestProperty( "Content-Length", String.valueOf(query.length()));
		conn.setConnectTimeout(timeout);
		conn.setReadTimeout(timeout);
		OutputStream os = conn.getOutputStream();
		os.write(query.getBytes());
		@SuppressWarnings("unchecked")
		Set<String> result = mapper.readValue(conn.getInputStream(),Set.class);
		return result;
	}

	public static PatentEntity getPatentMetaInformationByID(String url,String patentID) throws MalformedURLException, IOException
	{
		String urlGetPatentInformation = url + "/patent/metainformation/" + patentID;
		URL urlURL = new URL(urlGetPatentInformation);
		HttpURLConnection huc = (HttpURLConnection) urlURL.openConnection();
	    HttpURLConnection.setFollowRedirects(false);
	    huc.setConnectTimeout(timeout);
	    huc.setReadTimeout(timeout);
	    huc.setRequestMethod("GET");
	    huc.connect();
	    InputStream imputstream = huc.getInputStream();
	    ObjectMapper objectMapper = new ObjectMapper();
		PatentEntity result = objectMapper.readValue(imputstream,PatentEntity.class);
		return result;
	}
	
	public static String getPatentFullText(String url,String patentID) throws MalformedURLException, IOException
	{
		String urlGetPatentFullText = url + "/patent/fulltext/" + patentID;
		URL urlURL = new URL(urlGetPatentFullText);
		HttpURLConnection huc = (HttpURLConnection) urlURL.openConnection();
	    HttpURLConnection.setFollowRedirects(false);
	    huc.setConnectTimeout(timeout);
	    huc.setReadTimeout(timeout);
	    huc.setRequestMethod("GET");
	    huc.connect();
	    InputStream imputstream = huc.getInputStream();
		String result = FileHandling.convertImputStream(imputstream);
		return result;
	}
	
}
