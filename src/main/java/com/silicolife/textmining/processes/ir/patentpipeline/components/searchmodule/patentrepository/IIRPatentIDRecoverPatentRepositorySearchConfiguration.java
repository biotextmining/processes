package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.patentrepository;

import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;

public interface IIRPatentIDRecoverPatentRepositorySearchConfiguration extends IIRPatentIDRetrievalModuleConfiguration{
	
	public String getPatentRepositoryServerBasedUrl();
	public String getUserName();
	public String getPassword();

}
