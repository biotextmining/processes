package com.silicolife.textmining.processes.ir.springer.configuration;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.process.ir.configuration.IRSearchConfigurationImpl;

public class IRSpringerSearchConfigurationImpl extends IRSearchConfigurationImpl implements IIRSpringerSearchConfiguration{
	
	private String authentication;

	public IRSpringerSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,Properties propeties) {
		super(keywords, organism, queryName, propeties);
		this.authentication = authentication;
	}

	@Override
	public String getAuthentication() {
		return authentication;
	}

}
