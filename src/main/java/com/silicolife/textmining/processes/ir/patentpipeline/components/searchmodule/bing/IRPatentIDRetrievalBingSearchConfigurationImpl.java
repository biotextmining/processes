package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing;

public class IRPatentIDRetrievalBingSearchConfigurationImpl  implements IIRPatentIDRecoverBingSearchConfiguration {

	private String accessToken;


	public IRPatentIDRetrievalBingSearchConfigurationImpl(String accessToken) {
		this.accessToken=accessToken;
	}


	@Override
	public String getAccessToken() {
		return accessToken;
	}

}
