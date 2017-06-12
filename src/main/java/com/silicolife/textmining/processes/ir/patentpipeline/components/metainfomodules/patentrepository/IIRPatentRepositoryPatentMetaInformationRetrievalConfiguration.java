package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;

public interface IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration extends IIRPatentMetaInformationRetrievalConfiguration{
	
	public String getPatentRepositoryServerBasedUrl();
	public String getUserName();
	public String getPassword();
	
}
