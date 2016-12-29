package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRecoverConfiguration;

public class IRPatentIDRecoverConfigurationImpl implements IIRPatentIDRecoverConfiguration{
	
	private String query;
	
	public IRPatentIDRecoverConfigurationImpl(String query) {
		this.query=query;
	}

	@Override
	public String getQuery() {
		return query;
	}

	@Override
	public void setQuery(String query) {
		this.query=query;
		
	}

}
