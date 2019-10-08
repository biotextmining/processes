package com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule;

import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public interface IIRPatentRetrieval {
	
	/**
	 * Get Patent Document given Patent ID List
	 * 
	 * @param patentsIds
	 * @return
	 * @throws ANoteException
	 */
	public IIRPatentRetrievalReport retrievedPatents(Set<String> patentsIds) throws ANoteException;
	
	/**
	 * Get configuration
	 * 
	 * @return
	 */
	public IIRPatentRetrievalConfiguration getConfiguration();
	
	
	
	/**
	 * Return the name of Patent Retrieval System
	 * @return
	 */
	
	public String getSourceName();

	/**
	 * Cancel process
	 * 
	 */
	public void stop();
	
}
