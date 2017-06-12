package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.patentrepository;

public class IRPatentIDRetrievalPatentRepositorySearchConfigurationImpl implements IIRPatentIDRecoverPatentRepositorySearchConfiguration{

	private String patentRepositoryServerBasedUrl;
	private String userName;
	private String userPassword;
	
	public IRPatentIDRetrievalPatentRepositorySearchConfigurationImpl(String patentRepositoryServerBasedUrl, String userName, String userPassword) {
		super();
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

	@Override
	public String getPassword() {
		return userPassword;
	}

	
}
