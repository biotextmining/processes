package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

public class IRPatentPipelineSearchConfigurationImpl implements IIRPatentPipelineSearchConfiguration{
	
	private String query;
	
	public IRPatentPipelineSearchConfigurationImpl(String query)
	{
		this.query=query;
	}

	@Override
	public String getQuery() {
		return query;
	}

}
