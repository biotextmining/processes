package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalConfigurationImpl;

public class IROPSPatentMetaInformationRetrievalConfigurationImpl extends IRPatentMetaInformationRetrievalConfigurationImpl implements IIROPSPatentMetaInformationRetrievalConfiguration{
	
	private String accessToken;
	
	private boolean isAbstarctIncludeClaimsAndDescription;
	
	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken,boolean isAbstarctIncludeClaimsAndDescription) {
		super(proxy);
		this.accessToken=accessToken;
		this.isAbstarctIncludeClaimsAndDescription = isAbstarctIncludeClaimsAndDescription;
	}
	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken) {
		this(proxy, accessToken, false);
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

	public boolean isAbstarctIncludeClaimsAndDescription() {
		return isAbstarctIncludeClaimsAndDescription;
	}

}
