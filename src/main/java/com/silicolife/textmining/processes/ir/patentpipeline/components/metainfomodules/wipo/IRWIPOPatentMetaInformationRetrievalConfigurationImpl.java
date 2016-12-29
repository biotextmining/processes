package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IRPatentMetaInformationRetrievalConfigurationImpl;

public class IRWIPOPatentMetaInformationRetrievalConfigurationImpl extends IRPatentMetaInformationRetrievalConfigurationImpl implements IIRWIPOPatentMetaInformationRetrievalConfiguration {
	private String username;
	private String pwd;
	
	public IRWIPOPatentMetaInformationRetrievalConfigurationImpl(String username, String pwd,IProxy proxy) {
		super(proxy);
		this.username=username;
		this.pwd=pwd;
	}

	@Override
	public String getUserName() {
		return username;
	}

	@Override
	public String getPassword() {
		return pwd;
	}

}
