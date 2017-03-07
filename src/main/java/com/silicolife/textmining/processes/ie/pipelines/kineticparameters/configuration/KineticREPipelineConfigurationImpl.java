package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.configuration;

import java.util.Map;

import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;

public class KineticREPipelineConfigurationImpl extends NERConfigurationImpl implements IKineticREPipelineConfiguration{
	
	public final static String processUID = "kineticre.pipeline";
	
	private Integer NCBITaxonomy;
	private String ECNumber;
	private Integer ChEBI;
	
	public KineticREPipelineConfigurationImpl()
	{
		super();
		setConfigurationUID(processUID);
	}

	@Override
	public Map<String, String> getNERProperties() {
		return null;
	}

	@Override
	public void setConfiguration(Object obj) {
		
	}

	@Override
	public Integer getNCBITaxonomy() {
		return NCBITaxonomy;
	} 

	public void setNCBITaxonomy(Integer nCBITaxonomy) {
		NCBITaxonomy = nCBITaxonomy;
	}

	public String getECNumber() {
		return ECNumber;
	}

	public void setECNumber(String eCNumber) {
		ECNumber = eCNumber;
	}

	public Integer getChEBI() {
		return ChEBI;
	}

	public void setChEBI(Integer chEBI) {
		ChEBI = chEBI;
	}

	



}
