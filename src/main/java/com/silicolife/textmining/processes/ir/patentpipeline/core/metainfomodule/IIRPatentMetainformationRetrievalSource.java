package com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule;

import java.util.Map;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;

public interface IIRPatentMetainformationRetrievalSource {
	
	/**
	 * Get Patent Document given Patent ID List
	 * 
	 * @param patentsIds
	 * @return
	 * @throws ANoteException
	 */
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException;
	
	/**
	 * Get configuration
	 * 
	 * @return
	 */
	public IIRPatentMetaInformationRetrievalConfiguration getConfiguration();
	
	
	
	/**
	 * Return the name of Patent Retrieval System
	 * @return
	 */
	
	public String getSourceName();

}
