package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;

public interface IIRPatentIDRecoverGoogleSearchConfiguration extends IIRPatentIDRetrievalModuleConfiguration{
	/**
	 * Return Google Search engine authentication access token (API id)
	 * More information in https://developers.google.com/custom-search/json-api/v1/introduction#background-data-model
	 * 
	 * @return
	 */
	public String getAccessToken();
	
	
	/**
	 * Return Bing Search engine authentication (custom search engine ID -cx)
	 * More information in https://developers.google.com/custom-search/json-api/v1/introduction#background-data-model
	 * 
	 * @return
	 */
	public String getCustomSearchID();
	

}
