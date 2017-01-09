package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.process.ir.configuration.AIRSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchStepsConfiguration;

public  class IRPatentSearchConfigurationImpl extends AIRSearchConfigurationImpl implements IIRPatentPipelineConfiguration {

	private IIRPatentPipelineSearchConfiguration patentIDRetrievalConfiguration;
	private IIRPatentPipelineSearchStepsConfiguration pipelineConfiguration;
	
	public IRPatentSearchConfigurationImpl(IIRPatentPipelineSearchStepsConfiguration pipelineConfiguration)
	{
		super();
		this.pipelineConfiguration=pipelineConfiguration;
	}

	public IRPatentSearchConfigurationImpl(IIRPatentPipelineSearchConfiguration patentIDRetrievalConfiguration,String queryName,Properties properties,IIRPatentPipelineSearchStepsConfiguration pipelineConfiguration) {
		super(queryName, properties);
		this.patentIDRetrievalConfiguration=patentIDRetrievalConfiguration;
		this.pipelineConfiguration=pipelineConfiguration;
	}
	
	@Override
	public IIRPatentPipelineSearchConfiguration getIRPatentPipelineSearchConfiguration() {
		return patentIDRetrievalConfiguration;
	}

	@Override
	public IIRPatentPipelineSearchStepsConfiguration getIIRPatentPipelineSearchConfiguration() {
		return pipelineConfiguration;
	}

}
