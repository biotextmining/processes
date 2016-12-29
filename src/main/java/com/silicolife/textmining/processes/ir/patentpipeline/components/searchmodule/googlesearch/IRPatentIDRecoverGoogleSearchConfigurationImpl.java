package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch;

import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentIDRecoverConfigurationImpl;

public class IRPatentIDRecoverGoogleSearchConfigurationImpl extends IRPatentIDRecoverConfigurationImpl implements IIRPatentIDRecoverGoogleSearchConfiguration {
	
	private String accessToken;
	private String CustomSearchID;
	
	public IRPatentIDRecoverGoogleSearchConfigurationImpl(String query,String accessToken, String CustomSearchID) {
		super(query);
		// TODO Auto-generated constructor stub
		this.accessToken=accessToken;
		this.CustomSearchID=CustomSearchID;
		
	}

	@Override
	public String getAccessToken() {
		// TODO Auto-generated method stub
		return accessToken;
	}

	@Override
	public String getCustomSearchID() {
		// TODO Auto-generated method stub
		return CustomSearchID;
	}

}
