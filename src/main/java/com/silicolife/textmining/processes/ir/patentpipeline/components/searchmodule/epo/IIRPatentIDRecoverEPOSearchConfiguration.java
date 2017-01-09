package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;

public interface IIRPatentIDRecoverEPOSearchConfiguration extends IIRPatentIDRetrievalModuleConfiguration{
	
	/**
	 * Return OPS authentication access token
	 * 
	 * @return
	 */
	public String getAccessToken();

}
