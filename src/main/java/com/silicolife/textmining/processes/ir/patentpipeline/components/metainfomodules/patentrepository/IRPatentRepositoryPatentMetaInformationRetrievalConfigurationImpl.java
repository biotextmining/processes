package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalConfigurationImpl;

public class IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl extends IRPatentMetaInformationRetrievalConfigurationImpl implements IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration{
	
	private String patentRepositoryServerBasedUrl;
	private String userName;
	private String userPassword;
		
	public IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,
			String patentRepositoryServerBasedUrl, String userName, String userPassword) {
		super(proxy);
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
