package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalConfigurationImpl;

public class IROPSPatentRetrievalConfigurationImpl extends IRPatentRetrievalConfigurationImpl implements IIROPSPatentRetrievalConfiguration{

	private String accessToken;
	
	public IROPSPatentRetrievalConfigurationImpl(String outputDir, IProxy proxy, String accessToken) {
		super(outputDir, proxy);
		this.accessToken=accessToken;
		
	}

	public String getAccessToken() {
		return accessToken;
	}

}
