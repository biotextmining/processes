package com.silicolife.textmining.processes.ir.patentpipeline.configuration;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;

public interface IIRPatentSearchConfiguration extends IIRSearchConfiguration{
	
	/**
	 * Return keywords for the patent search process
	 * @return
	 */
	public String getKeywords();
	
	public IIRPatentPipelineSearchConfiguration getIIRPatentPipelineSearchConfiguration();
	
	
}
