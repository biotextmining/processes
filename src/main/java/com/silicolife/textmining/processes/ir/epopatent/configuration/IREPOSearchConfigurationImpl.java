package com.silicolife.textmining.processes.ir.epopatent.configuration;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.process.ir.configuration.IRSearchConfigurationImpl;

public class IREPOSearchConfigurationImpl extends IRSearchConfigurationImpl implements IIREPOSearchConfiguration{
	
	private String authentication;

	public IREPOSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,Properties propeties) {
		super(keywords, organism, queryName, propeties);
		this.authentication = authentication;
	}

	@Override
	public String getAuthentication() {
		return authentication;
	}

}
