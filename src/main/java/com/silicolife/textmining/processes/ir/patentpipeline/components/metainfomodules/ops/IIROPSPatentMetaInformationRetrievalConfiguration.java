package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;

public interface IIROPSPatentMetaInformationRetrievalConfiguration extends IIRPatentMetaInformationRetrievalConfiguration{
	
	public String getAccessToken();
	public boolean isAbstarctIncludeClaimsAndDescription();

}
