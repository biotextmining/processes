package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;

public interface IIRPatentIDRetrievalSource {
	
	/**
	 * Get Patent ID as a result of IIRPatentIDRecoverConfiguration
	 * 
	 * @return
	 */
	public Set<String> retrievalPatentIds(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException;
	
	/**
	 * Return Source Name for PAtent ID Search ( Example WIPO or EPO )
	 * 
	 * @return
	 */
	public String getSourceName();
	
//	/**
//	 * Get IIRPatentIDRecoverConfiguration configuration
//	 * 
//	 * @return
//	 */
//	public IIRPatentIDRecoverConfiguration getConfiguration();
//	
//	/**
//	 * Set IIRPatentIDRecoverConfiguration configuration
//	 * 	
//	 * 
//	 * @param configuration
//	 * @throws WrongIRPatentIDRecoverConfigurationException if configuration is wrong for the source
//	 */
//	public void setConfigutaion(IIRPatentIDRecoverConfiguration  configuration) throws WrongIRPatentIDRecoverConfigurationException;
	
	/**
	 * Stop process
	 * 
	 */
	public void stop();
	
	/**
	 * Return the preious numnet of patentsIDs
	 * 
	 * @return
	 * @throws ANoteException 
	 */
	public int getNumberOfResults() throws ANoteException;

}
