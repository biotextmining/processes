package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;

public class IRPatentRetrievalConfigurationImpl implements IIRPatentRetrievalConfiguration{
	
	private String outputDir;
	private IProxy proxy;


	public IRPatentRetrievalConfigurationImpl(String outputDir,IProxy proxy) {
		this.outputDir=outputDir;
		this.proxy=proxy;
	}
	

	@Override
	public String getOutputDirectory() {
		return outputDir;
	}


	@Override
	public IProxy getProxy() {
		return proxy;
	}

}
