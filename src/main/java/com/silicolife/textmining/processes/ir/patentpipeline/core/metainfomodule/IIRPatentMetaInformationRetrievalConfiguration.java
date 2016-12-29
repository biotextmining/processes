package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;

public interface IIRPatentMetaInformationRetrievalConfiguration {
	
	/**
	 * Return {@link IProxy}
	 * Return null if Proxy is not defined
	 * 
	 * @return
	 */
	public IProxy getProxy();

}
