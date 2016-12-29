package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverConfiguration;

public interface IIRPatentIDRecoverEPOSearchConfiguration extends IIRPatentIDRecoverConfiguration{
	
	/**
	 * Return OPS authentication access token
	 * 
	 * @return
	 */
	public String getAccessToken();

}
