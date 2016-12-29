package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;

public interface IIRPatentRetrievalConfiguration {
	
	/**
	 * Get Output base directory ( to put PDF files)
	 * 
	 * @return
	 */
	public String getOutputDirectory();
	
	/**
	 * Return {@link IProxy}
	 * Return null if Proxy is not difened
	 * 
	 * @return
	 */
	public IProxy getProxy();
}
