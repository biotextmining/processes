package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalConfigurationImpl;

public class IRWIPOPatentRetrievalConfigurationImpl extends IRPatentRetrievalConfigurationImpl implements IIRWIPOPatentRetrievalConfiguration{
	
	private String username;
	private String pwd;

	public IRWIPOPatentRetrievalConfigurationImpl(String username,String pwd,String outputDir,IProxy proxy)
	{
		super(outputDir,proxy);
		this.username=username;
		this.pwd=pwd;
	}

	@Override
	public String getUserName() {
		return username;
	}

	@Override
	public String getPassword() {
		return pwd;
	}

}
