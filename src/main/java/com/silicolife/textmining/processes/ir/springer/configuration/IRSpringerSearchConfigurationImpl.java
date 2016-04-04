package com.silicolife.textmining.processes.ir.springer.configuration;

import java.util.Properties;

import com.silicolife.textmining.processes.ir.pubmed.configuration.IRPubmedSearchConfigurationImpl;

public class IRSpringerSearchConfigurationImpl extends IRPubmedSearchConfigurationImpl implements IIRSpringerSearchConfiguration{
	
	
	public static final String springsearch = "ir.springesearch";

	private String authentication;

	public IRSpringerSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,Properties propeties) {
		super(keywords, organism, queryName, propeties);
		this.authentication = authentication;
	}

	@Override
	public String getAuthentication() {
		return authentication;
	}
	
	@Override
	public String getConfigurationUID() {
		return springsearch;
	}


}
