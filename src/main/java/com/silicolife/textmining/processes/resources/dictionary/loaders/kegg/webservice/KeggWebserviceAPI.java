package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;

public class KeggWebserviceAPI {

	private static String baseEntityListURL = "http://rest.kegg.jp/%s/%s";

	public static List<String> getEntityStream(String entityClass) throws IOException
	{
		List<String> out = new ArrayList<>();
		String urlStr = String.format(baseEntityListURL, "list", entityClass);
		URL url = new URL(urlStr);
		InputStream is = url.openStream();
		String response = IOUtils.toString(is);
		is.close();
		String[] httpResponseLines = response.split("\n");
		for(String responseLine:httpResponseLines)
		{
			out.add(responseLine);
		}
		return out;
	}

	public static List<String> getGenesByOrganismStream(String keggOrganism) throws IOException
	{
		return getEntityStream(keggOrganism);
	}

	public static List<String> getAvailableOrganismStream() throws IOException
	{
		return getEntityStream("organism");
	}

	public static String getEntityIDGivenEntityStream(String entityClass,String entityStream)
	{
		String id = entityStream.split("\\t")[0].substring(entityClass.length()+1);
		return id;
	}

	public static List<String> getEntityNames(String entityStream)
	{
		List<String> out = new ArrayList<>();
		String[] namesStreamSplited = entityStream.split("\\t");
		if(namesStreamSplited.length > 1)
		{
			String namesStream = namesStreamSplited[1];
			String[] names = namesStream.split(";");
			for(String name:names)
				out.add(name.trim());
		}
		return out;
	}

}
