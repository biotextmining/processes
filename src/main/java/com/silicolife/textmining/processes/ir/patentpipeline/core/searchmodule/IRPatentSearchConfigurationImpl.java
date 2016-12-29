package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.process.ir.configuration.AIRSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentSearchConfiguration;

public  class IRPatentSearchConfigurationImpl extends AIRSearchConfigurationImpl implements IIRPatentSearchConfiguration {

	String keywords;
	IIRPatentPipelineSearchConfiguration pipelineConfiguration;
	
	public IRPatentSearchConfigurationImpl(IIRPatentPipelineSearchConfiguration pipelineConfiguration)
	{
		super();
		this.pipelineConfiguration=pipelineConfiguration;
	}

	public IRPatentSearchConfigurationImpl(String keywords,String queryName,Properties properties,IIRPatentPipelineSearchConfiguration pipelineConfiguration) {
		super(queryName, properties);
		this.keywords=keywords;
		this.pipelineConfiguration=pipelineConfiguration;
	}
	
	@Override
	public String getKeywords() {
	return keywords;
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	@Override
	public IIRPatentPipelineSearchConfiguration getIIRPatentPipelineSearchConfiguration() {
		return pipelineConfiguration;
	}

}
