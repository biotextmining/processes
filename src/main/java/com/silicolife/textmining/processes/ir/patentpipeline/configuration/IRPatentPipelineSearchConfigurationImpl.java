package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.Set;

public class IRPatentPipelineSearchConfigurationImpl implements IIRPatentPipelineSearchConfiguration{
	
	private String query;
	private Set<String> patentClassificationIPCAllowed;
	private Integer yearMin;
	private Integer yearMax;
	
	public IRPatentPipelineSearchConfigurationImpl(String query,Set<String> patentClassificationIPCAllowed,Integer yearMin,Integer yearMax)
	{
		this.query = query;
		this.patentClassificationIPCAllowed = patentClassificationIPCAllowed;
		this.yearMin = yearMin;
		this.yearMax = yearMax;
	}
	
	public IRPatentPipelineSearchConfigurationImpl(String query)
	{
		this(query, null, null, null);
	}

	public String getQuery() {
		return query;
	}

	public Integer getYearMin() {
		return yearMin;
	}

	public Integer getYearMax() {
		return yearMax;
	}

	public Set<String> getPatentClassificationIPCAllowed() {
		return patentClassificationIPCAllowed;
	}

	public void setPatentClassificationIPCAllowed(Set<String> patentClassificationIPCAllowed) {
		this.patentClassificationIPCAllowed = patentClassificationIPCAllowed;
	}

	public void setQuery(String query) {
		this.query = query;
	}

	public void setYearMin(Integer yearMin) {
		this.yearMin = yearMin;
	}

}
