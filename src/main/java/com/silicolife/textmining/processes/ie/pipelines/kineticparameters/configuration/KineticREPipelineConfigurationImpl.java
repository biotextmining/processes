package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.configuration;

import java.util.Map;

import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;

public class KineticREPipelineConfigurationImpl extends NERConfigurationImpl implements IKineticREPipelineConfiguration{
	
	public final static String processUID = "kineticre.pipeline";
	
	private Integer NCBITaxonomy;
	private String ECNumber;
	
	public KineticREPipelineConfigurationImpl()
	{
		super();
		setConfigurationUID(processUID);
	}

	public KineticREPipelineConfigurationImpl(Integer ncbiTaxonomy)
	{
		this();
		this.NCBITaxonomy = ncbiTaxonomy;
		this.ECNumber = null;
	}
	
	public KineticREPipelineConfigurationImpl(String eCNumber)
	{
		this();
		this.NCBITaxonomy = null;
		this.ECNumber = eCNumber;
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

	



}
