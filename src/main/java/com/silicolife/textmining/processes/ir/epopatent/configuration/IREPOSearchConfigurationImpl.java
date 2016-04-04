package com.silicolife.textmining.processes.ir.epopatent.configuration;

import java.util.Properties;

import com.silicolife.textmining.processes.ir.pubmed.configuration.IRPubmedSearchConfigurationImpl;

public class IREPOSearchConfigurationImpl extends IRPubmedSearchConfigurationImpl implements IIREPOSearchConfiguration{
	
	public static final String epopatentsearch = "ir.epopatentsearch";
	
	private String authentication;

	public IREPOSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,Properties propeties) {
		super(keywords, organism, queryName, propeties);
		this.authentication = authentication;
	}

	@Override
	public String getAuthentication() {
		return authentication;
	}
	

	@Override
	public String getConfigurationUID() {
		return epopatentsearch;
	}

}
