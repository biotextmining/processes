package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities.BingResultSet;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities.BingWebQuery;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.entities.BingWebResult;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.AIRPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;



public class BingSearchPatentIDRecoverSource extends AIRPatentIDRecoverSource {

	public static String bingURL = "site:www.google.com/patents/ ";
	public static String CHAR_SET = "UTF-8";

	public BingSearchPatentIDRecoverSource(IIRPatentIDRecoverConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		super(configuration);
	}

	@Override
	public Set<String> recoverPatentIDs() throws ANoteException {
		BingWebQuery query = new BingWebQuery();
		String newQuery = transformQuerytoBingPatterns((getConfiguration().getQuery()));
		query.setQuery(queryBuilder(bingURL + newQuery));
		String tokenaccess = ((IIRPatentIDRecoverBingSearchConfiguration)getConfiguration()).getAccessToken(); 
		query.setAppid(tokenaccess);
		int stopNumber =1;//stop the cicle when its turned to 0.
		Set<String> patentlinks = new HashSet<>();
		while (stopNumber>0) {
			//Thread.sleep(2000);//pausar durante dois segundos
			query.doQuery();
			BingResultSet<BingWebResult> ResultSet= query.getQueryResult();
			stopNumber=ResultSet.getAsrs().size();
			if (stopNumber==0) {
				break;
			}else{
				for (BingWebResult wr : ResultSet) {
					patentlinks.add(wr.getUrl());
				}
				query.nextPage();
			} 
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
			System.out.println(query);
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
			System.out.println(url);
			try{
				String[] subArray = url.replaceAll("\\?.*", "").split("/");//get all URL parts separated
				for (int part = 0; part < subArray.length; part++) {
					if (subArray[part].matches("[A-Z]{1,2}\\d+[A-Z]{0,1}\\d{0,1}")){//patentID
						PatentID.add(subArray[part]);
					}
				}
			}catch(Exception e){
				throw new ANoteException("There's a problem with input query. Try to change it!");
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
		//		BingWebQuery query = new BingWebQuery();
		//		String patent = " site:www.google.com/patents/";
		//		int results = 0;
		//		//		System.out.println(q+patent);
		//		query.setQuery(getConfiguration().getQuery() + patent );
		//		//query.setWebSearchOptions("DisableHostCollapsing");
		//		//query.setWebSearchOptions("DisableQueryAlterations"); 
		//
		//		String tokenaccess = ((IIRPatentIDRecoverBingSearchConfiguration)getConfiguration()).getAccessToken(); 
		//		query.setAppid(tokenaccess);
		//
		//		//Primary Account Key available at https://datamarket.azure.com/account
		//		//query.setAppid("Ju0WWwspaT9tVXY+JhWeftB2Om9yQeCCz2cRpA+fGCE");
		//
		//		int stopNumber =1;//stop the cicle when its turned to 0.
		//		while (stopNumber>0) {
		//			query.doQuery();
		//			BingResultSet<BingWebResult> ResultSet= query.getQueryResult();
		//			stopNumber=ResultSet.getAsrs().size();
		//			results+=ResultSet.getAsrs().size();
		//			if (stopNumber==0) {
		//				break;
		//
		//			}else{
		//				query.nextPage();
		//				}
		//		}
		return recoverPatentIDs().size();
	}

	@Override
	public void validate(IIRPatentIDRecoverConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		if(configuration instanceof IIRPatentIDRecoverBingSearchConfiguration)
		{
			if(configuration.getQuery()==null || configuration.getQuery().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Query can not be null or empty");

			}
			IIRPatentIDRecoverBingSearchConfiguration configurationBingSearch = (IIRPatentIDRecoverBingSearchConfiguration) configuration;
			if(configurationBingSearch.getAccessToken()==null || configurationBingSearch.getAccessToken().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Acess Token can not be null or empty");
			}
			if(!configurationBingSearch.getAccessToken().contains(":") && configurationBingSearch.getAccessToken().length() < 20)
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Invalid access token");
			}
		}
		else
			throw new WrongIRPatentIDRecoverConfigurationException("Configuration is not a IIRPatentIDRecoverBingSearchConfiguration");
	}	

}

