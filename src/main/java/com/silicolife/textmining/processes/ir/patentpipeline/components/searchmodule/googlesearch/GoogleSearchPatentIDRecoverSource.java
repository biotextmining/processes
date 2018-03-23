package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch;


import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.googleEntities.GoogleResults;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.googleEntities.GoogleWebQuery;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.googleEntities.Items;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.AIRPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;





public class GoogleSearchPatentIDRecoverSource extends AIRPatentIDRecoverSource {

	public final static String googleproccessID = "google.searchpatentid";
	public final static String googleName= "Custom Search API from Google";

	public GoogleSearchPatentIDRecoverSource(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		super(configuration);
	}

	@Override
	public Set<String> retrievalPatentIds(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException {
		String tokenaccess = ((IIRPatentIDRecoverGoogleSearchConfiguration)getConfiguration()).getAccessToken();
		String customSearchID = ((IIRPatentIDRecoverGoogleSearchConfiguration)getConfiguration()).getCustomSearchID();
		Set<String> links = new HashSet<>();
		//Create a new GoogleSearch object
		GoogleWebQuery googleWebQuery = new GoogleWebQuery(tokenaccess, customSearchID);
		String query = transformQueryToGoogleSearchOperators(configuration.getQuery());
		googleWebQuery.addExtraParam("siteSearch", "www.google.com/patents");//to search only on specified sites
		googleWebQuery.addExtraParam("siteSearchFilter", "i");
		for (int i = 0; i < 91 && !stop; i+=10) {
			Set<String> processPage = processGooglePage(googleWebQuery,i,query);
			if(processPage==null)
				break;
			links.addAll(processPage);
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
			}
		}
		Set<String> patentidsAllCountries=patentIDExtraction(links);
		return patentidsAllCountries;
	}

	private Set<String> processGooglePage(GoogleWebQuery googleWebQuery, int startindex,String query)
	{
		int numbersOfTries = 0;
		int numbersOfTriesMax = 10;

		Set<String> out = new HashSet<>();
		while(numbersOfTries<numbersOfTriesMax)
		{
			googleWebQuery.setStartIndexOfResult(startindex);		
			try {
				GoogleResults r = googleWebQuery.search(query);
				//print results
				List<Items> items=new ArrayList<Items>();//iniciar uma lista
				if(items!=null)
				{
					items = r.getItems();
					for(Items it:items){
						out.add(it.getLink());
					}
				}
				return out;
			} catch (ANoteException e) {
				try {
					Thread.sleep(numbersOfTries*1000);
				} catch (InterruptedException e1) {
				}
				numbersOfTries++;
			}			
		}
		return null;
	}

	public static String transformQueryToGoogleSearchOperators(String query){
		String queryTransformed = query.trim();
		//		String[] keywordsParts = queryTransformed.split("AND|OR|NOT");
		//		for(String part : keywordsParts)
		//		{
		//			part = part.trim();
		//			if(!part.isEmpty())
		//			{
		//				queryTransformed = queryTransformed.replace(part, "\""+part+"\"");
		//			}
		//		}
		//		queryTransformed = queryTransformed.replace("AND"," + ");
		//		queryTransformed=queryTransformed.replace("NOT", " - ");
		//		queryTransformed = queryTransformed.replace("OR"," OR ");
		queryTransformed = queryTransformed.replace("\"", "'");
		//		queryTransformed = queryTransformed.replace("  ", " ");
		//		queryTransformed= queryTransformed.replace("+ ", "+");
		//		queryTransformed= queryTransformed.replace("- ", "-");
		return queryTransformed;
	}



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
				throw new ANoteException("There's a problem with input query. Try to change it!");
			}
		} 
		return PatentID;
	} 

	@Override
	public String getSourceName() {
		return GoogleWebQuery.googlesearch;
	}

	@Override
	public int getNumberOfResults() throws ANoteException {
		return 0;
	}


	@Override
	public void validate(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		if(configuration instanceof IIRPatentIDRecoverGoogleSearchConfiguration)
		{
			IIRPatentIDRecoverGoogleSearchConfiguration configurationGoogleSearch = (IIRPatentIDRecoverGoogleSearchConfiguration) configuration;
			if(configurationGoogleSearch.getAccessToken()==null || configurationGoogleSearch.getAccessToken().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Google Acess Token can not be null or empty");
			}
			if(!configurationGoogleSearch.getAccessToken().contains(":") && configurationGoogleSearch.getAccessToken().length() < 20)
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Invalid Google access token");
			}
			if(configurationGoogleSearch.getCustomSearchID()==null || configurationGoogleSearch.getCustomSearchID().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("CustomSearchID can not be null or empty");
			}
			if(configurationGoogleSearch.getCustomSearchID().length() < 20)
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Invalid CustomSearchID");
			}
		}
		else
			throw new WrongIRPatentIDRecoverConfigurationException("Configuration is not a IIRPatentIDRecoverGoogleSearchConfiguration");
	}
}