package com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule;

/**
 * Inteface that represents the IR Patent Recovery configurations
 * 
 * @author Hugo Costa
 *
 */
public interface IIRPatentIDRecoverConfiguration {
	
	/**
	 * Return the Query configuration to search
	 * 
	 * @return
	 */
	public String getQuery();

	public void setQuery(String query);

}
