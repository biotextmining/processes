package com.silicolife.textmining.processes.ir.patentrepository;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.PatentEntity;

public class PatentRepositoryAPI {
	
	public static Set<String> getPatentIdsGivenTextQuery(String url,String query) throws MalformedURLException, IOException {
		String urlgetKeywordsSearch = url + "/search/patentkeywords/" + query;
		InputStream imputstream = new URL(urlgetKeywordsSearch).openStream();
		ObjectMapper objectMapper = new ObjectMapper();
		@SuppressWarnings("unchecked")
		List<String> result = objectMapper.readValue(imputstream,List.class);
		return new HashSet<>(result);
	}

	public static PatentEntity getPatentMetaInformationByID(String url,String patentID) throws MalformedURLException, IOException
	{
		String urlGetPatentInformation = url + "/patent/metainformation/" + patentID;
		InputStream imputstream = new URL(urlGetPatentInformation).openStream();
		ObjectMapper objectMapper = new ObjectMapper();
		PatentEntity result = objectMapper.readValue(imputstream,PatentEntity.class);
		return result;
	}
	
	public static String getPatentFullText(String url,String patentID) throws MalformedURLException, IOException
	{
		String urlGetPatentFullText = url + "/patent/fulltext/" + patentID;
		InputStream imputstream = new URL(urlGetPatentFullText).openStream();
		String result = FileHandling.convertImputStream(imputstream);
		return result;
	}
	
}
