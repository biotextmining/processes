package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;

public class IRPatentMetaInformationRetrievalConfigurationImpl implements IIRPatentMetaInformationRetrievalConfiguration{
	
	private IProxy proxy;


	public IRPatentMetaInformationRetrievalConfigurationImpl(IProxy proxy) {
		this.proxy=proxy;
	}
	

	@Override
	public IProxy getProxy() {
		return proxy;
	}


}
