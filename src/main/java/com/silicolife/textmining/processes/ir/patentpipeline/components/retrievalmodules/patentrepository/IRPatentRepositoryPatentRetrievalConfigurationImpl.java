package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalConfigurationImpl;

public class IRPatentRepositoryPatentRetrievalConfigurationImpl extends IRPatentRetrievalConfigurationImpl implements IIRPatentRepositoryPatentRetrievalConfiguration{
	
	private String patentRepositoryServerBasedUrl;
	private String userName;
	private String userPassword;
		
	public IRPatentRepositoryPatentRetrievalConfigurationImpl(IProxy proxy,String outputDir,
			String patentRepositoryServerBasedUrl, String userName, String userPassword) {
		super(outputDir,proxy);
		this.patentRepositoryServerBasedUrl = patentRepositoryServerBasedUrl;
		this.userName = userName;
		this.userPassword = userPassword;
	}



	public String getPatentRepositoryServerBasedUrl() {
		return patentRepositoryServerBasedUrl;
	}

	public String getUserName() {
		return userName;
	}

	public String getPassword() {
		return userPassword;
	}


}
