package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch;

public class IRPatentIDRetrievalGoogleSearchConfigurationImpl implements IIRPatentIDRecoverGoogleSearchConfiguration {
	
	private String accessToken;
	private String CustomSearchID;
	
	public IRPatentIDRetrievalGoogleSearchConfigurationImpl(String accessToken, String CustomSearchID) {
		this.accessToken=accessToken;
		this.CustomSearchID=CustomSearchID;
		
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	@Override
	public String getCustomSearchID() {
		return CustomSearchID;
	}

}
