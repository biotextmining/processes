package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo;

import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentIDRecoverConfigurationImpl;

public class IRPatentIDRecoverEPOSearchConfigurationImpl extends IRPatentIDRecoverConfigurationImpl implements IIRPatentIDRecoverEPOSearchConfiguration{

	private String accessToken;


	public IRPatentIDRecoverEPOSearchConfigurationImpl(String query,String accessToken) {
		super(query);
		this.accessToken=accessToken;
	}


	@Override
	public String getAccessToken() {
		return accessToken;
	}

}
