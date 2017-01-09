package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;

public interface IIRPatentIDRecoverBingSearchConfiguration extends IIRPatentIDRetrievalModuleConfiguration{

	/**
	 * Return Bing Search engine authentication access token
	 * 
	 * @return
	 */
	public String getAccessToken();
	
}
