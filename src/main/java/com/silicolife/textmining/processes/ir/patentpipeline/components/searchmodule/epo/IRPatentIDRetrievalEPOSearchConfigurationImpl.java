package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo;

public class IRPatentIDRetrievalEPOSearchConfigurationImpl implements IIRPatentIDRecoverEPOSearchConfiguration{

	private String accessToken;


	public IRPatentIDRetrievalEPOSearchConfigurationImpl(String accessToken) {
		this.accessToken=accessToken;
	}


	@Override
	public String getAccessToken() {
		return accessToken;
	}

}
