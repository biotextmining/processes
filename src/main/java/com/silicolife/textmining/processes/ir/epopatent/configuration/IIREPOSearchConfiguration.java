package com.silicolife.textmining.processes.ir.epopatent.configuration;

import java.util.Set;

import com.silicolife.textmining.processes.ir.pubmed.configuration.IIRPubmedSearchConfiguration;

public interface IIREPOSearchConfiguration extends IIRPubmedSearchConfiguration{
	
	public String getAuthentication();
	
	public Integer getMinYear();
	
	public Integer getMaxYear();
	
	public Set<String> getClassificationIPCFilter();

}
