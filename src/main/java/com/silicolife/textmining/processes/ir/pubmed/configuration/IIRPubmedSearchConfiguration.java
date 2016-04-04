package com.silicolife.textmining.processes.ir.pubmed.configuration;

import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;

public interface IIRPubmedSearchConfiguration extends IIRSearchConfiguration{
	
	/**
	 * Return keywords
	 * 
	 * @return
	 */
	public String getKeywords();
	
	/**
	 * Return Organism
	 * @return
	 */
	public String getOrganism();

}
