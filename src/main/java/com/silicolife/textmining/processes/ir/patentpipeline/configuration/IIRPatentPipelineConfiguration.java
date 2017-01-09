package com.silicolife.textmining.processes.ir.patentpipeline.configuration;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;

public interface IIRPatentPipelineConfiguration extends IIRSearchConfiguration{
	
	
	/**
	 * Return keywords for the patent search process
	 * @return
	 */
	public IIRPatentPipelineSearchConfiguration getIRPatentPipelineSearchConfiguration();
	
	public IIRPatentPipelineSearchStepsConfiguration getIIRPatentPipelineSearchConfiguration();
	
	
}
