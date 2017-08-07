package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;

public interface IIRPatentRepositoryPatentRetrievalConfiguration extends IIRPatentMetaInformationRetrievalConfiguration{
	
	public String getPatentRepositoryServerBasedUrl();
	public String getUserName();
	public String getPassword();
	
}
