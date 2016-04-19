package com.silicolife.textmining.processes.ir.pubmed.configuration;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.process.ir.configuration.AIRSearchConfigurationImpl;

public class IRPubmedSearchConfigurationImpl extends AIRSearchConfigurationImpl implements IIRPubmedSearchConfiguration{
	
	public static final String pubmedsearchUID = "ir.pubmedsearch";
	

	private String keywords;
	private String organism;
	
	public IRPubmedSearchConfigurationImpl()
	{
		super();
	}

	public IRPubmedSearchConfigurationImpl(String keywords, String organism,String queryName,Properties propeties) {
		super(queryName, propeties);
		this.keywords=keywords;
		this.organism=organism;
	}

	@Override
	public String getKeywords() {
		return keywords;
	}

	@Override
	public String getOrganism() {
		return organism;
	}

	@Override
	public String getConfigurationUID() {
		return pubmedsearchUID;
	}
	
	public void setKeywords(String keywords) {
		this.keywords = keywords;
	}

	public void setOrganism(String organism) {
		this.organism = organism;
	}


}
