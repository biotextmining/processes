package com.silicolife.textmining.processes.ir.patentpipeline.configuration;

import java.util.Set;

public interface IIRPatentPipelineSearchConfiguration {
	
	/**
	 * Get Query String to find patent example "table"
	 * 
	 * @return
	 */
	public String getQuery();
	
	/**
	 * Get allowed Patent Classification filters
	 * Return null if all are allowed
	 * 
	 * 
	 * @return
	 */
	public Set<String> getPatentClassificationIPCAllowed();
	
	/**
	 * Get minimum Year to search for
	 * Null if there is not a minimum date
	 * 
	 * @return
	 */
	public Integer getYearMin();
	
	
	/**
	 * Get maximum Year to search for
	 * Null if there is not a maximum date
	 * 
	 * @return
	 */
	public Integer getYearMax();

}
