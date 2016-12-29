package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverConfiguration;

public interface IIRPatentIDRecoverBingSearchConfiguration extends IIRPatentIDRecoverConfiguration{

	/**
	 * Return Bing Search engine authentication access token
	 * 
	 * @return
	 */
	public String getAccessToken();
	
}
