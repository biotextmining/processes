package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentIDRecoverConfigurationImpl;

public class IRPatentIDRecoverBingSearchConfigurationImpl extends IRPatentIDRecoverConfigurationImpl implements IIRPatentIDRecoverBingSearchConfiguration {

	private String accessToken;


	public IRPatentIDRecoverBingSearchConfigurationImpl(String query,String accessToken) {
		super(query);
		this.accessToken=accessToken;
	}


	@Override
	public String getAccessToken() {
		return accessToken;
	}

}
