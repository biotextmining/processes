package com.silicolife.textmining.processes.ir.epopatent.configuration;

import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.processes.ir.pubmed.configuration.IRPubmedSearchConfigurationImpl;

public class IREPOSearchConfigurationImpl extends IRPubmedSearchConfigurationImpl implements IIREPOSearchConfiguration{
	
	public static final String epopatentsearch = "ir.epopatentsearch";
	
	private String authentication;
	private Integer minYear;
	private Integer maxYear;
	private Set<String> classificationIPCFilter;


	public IREPOSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,
			Integer minYear,Integer maxYear,Set<String> classificationIPCFilter,Properties propeties) {
		super(keywords, organism, queryName, propeties);
		this.authentication = authentication;
		this.minYear=minYear;
		this.maxYear=maxYear;
		this.classificationIPCFilter=classificationIPCFilter;
	}
	
//	public IREPOSearchConfigurationImpl(String keywords, String organism,String queryName,String authentication,Properties propeties) {
//		this(keywords, organism, queryName, authentication, null, null, null, propeties);
//	}

	@Override
	public String getAuthentication() {
		return authentication;
	}	

	@Override
	public String getConfigurationUID() {
		return epopatentsearch;
	}

	public Integer getMinYear() {
		return minYear;
	}

	public Integer getMaxYear() {
		return maxYear;
	}

	public Set<String> getClassificationIPCFilter() {
		return classificationIPCFilter;
	}

}
