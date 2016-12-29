package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalConfigurationImpl;

public class IROPSPatentMetaInformationRetrievalConfigurationImpl extends IRPatentMetaInformationRetrievalConfigurationImpl implements IIROPSPatentMetaInformationRetrievalConfiguration{
	
	private String accessToken;
	
	public IROPSPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy,String accessToken) {
		super(proxy);
		this.accessToken=accessToken;
	}

	@Override
	public String getAccessToken() {
		return accessToken;
	}

}
