package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities.BingWebQuery;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.AIRPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;



public class BingSearchPatentIDRecoverSource extends AIRPatentIDRecoverSource {

	public final static String bingProcessID = "bing.searchpatentid";
	public final static String bingName= "Bing Web Search API from Bing";

	public static String bingURL = "(site:www.google.com/patents/ OR site:patents.google.com) ";
	public static String CHAR_SET = "UTF-8";

	public BingSearchPatentIDRecoverSource(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		super(configuration);
	}

	@Override
	public Set<String> retrievalPatentIds(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException {
		BingWebQuery query = new BingWebQuery();
		String tokenaccess = ((IIRPatentIDRecoverBingSearchConfiguration)getConfiguration()).getAccessToken(); 
		query.setAppid(tokenaccess);
		String newQuery = transformQuerytoBingPatterns(configuration.getQuery());
		query.setQuery(queryBuilder(bingURL+newQuery));
		int stopNumber =query.getNumberOfResults();//stop the cicle
		Set<String> patentlinks = new HashSet<>();
		int index = 0;
		while (index<stopNumber) {
			//Thread.sleep(2000);//pausar durante dois segundos
			Set<String> urls = query.doQuery();
			for (String wr : urls) {
				patentlinks.add(wr);
			}
			query.nextPage();
			index+=query.getPerPage();
		}  
		Set<String> patentidsAllCountries=patentIDExtraction(patentlinks);
		return patentidsAllCountries;
	}



	public static String transformQuerytoBingPatterns(String query){
		query = query.trim();
		String[] keywordsParts = query.split("AND|OR|NOT");
		for(String part : keywordsParts)
		{
			part = part.trim();
			if(!part.isEmpty())
			{
				query = query.replace(part, "\""+part+"\"");
			}
		}
		query = query.replace("AND"," AND ");
		query=query.replace("NOT", " NOT ");
		query = query.replace("OR"," OR ");
		query = query.replace("\"\"", "\"");
		query = query.replace("  ", " ");
		return query;

	}

	public static String queryBuilder(String query) throws ANoteException
	{
		try {
			query=URLEncoder.encode(query,CHAR_SET);
		} catch (UnsupportedEncodingException e) {
			throw new ANoteException(e);
		}
		return query;
	}

	/**
	 * PatentIDExtraction is a method which takes a set of urls as input and returns a new set with only PatentIds
	 * 
	 * @param setURLS
	 * @return
	 * @throws ANoteException 
	 */

	private static Set<String> patentIDExtraction (Set<String> setURLS) throws ANoteException{
		Set<String> PatentID =new HashSet<>();
		for (String url : setURLS) {
			try{
				String[] subArray = url.replaceAll("\\?.*", "").split("/");//get all URL parts separated
				for (int part = 0; part < subArray.length; part++) {
					if (subArray[part].matches("[A-Z]{1,2}\\d+[A-Z]{0,1}\\d{0,1}")){//patentID
						PatentID.add(subArray[part]);
					}
				}
			}catch(Exception e){
				//				throw new ANoteException("There's a problem with input query. Try to change it!"); dont do anything and try the next link
			}
		} 
		return PatentID;
	}

	@Override
	public String getSourceName() {
		return BingWebQuery.bingsearch;
	}

	@Override
	public int getNumberOfResults() throws ANoteException {
		return 0;
	}

	@Override
	public void validate(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		if(configuration instanceof IIRPatentIDRecoverBingSearchConfiguration)
		{
			IIRPatentIDRecoverBingSearchConfiguration configurationBingSearch = (IIRPatentIDRecoverBingSearchConfiguration) configuration;
			if(configurationBingSearch.getAccessToken()==null || configurationBingSearch.getAccessToken().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Bing Acess Token can not be null or empty");
			}
			if(!configurationBingSearch.getAccessToken().contains(":") && configurationBingSearch.getAccessToken().length() < 20)
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Invalid Bing access token");
			}
		}
		else
			throw new WrongIRPatentIDRecoverConfigurationException("Configuration is not a IIRPatentIDRecoverBingSearchConfiguration");
	}	

}

