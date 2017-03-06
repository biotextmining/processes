package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.configuration;

import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;

public class KineticREPipelineConfigurationImpl extends NERConfigurationImpl implements IKineticREPipelineConfiguration{
	
	public final static String processUID = "kineticre.pipeline";
	
	private Integer NCBITaxonomy;

	public KineticREPipelineConfigurationImpl(Integer ncbiTaxonomy)
	{
		super();
		this.NCBITaxonomy = ncbiTaxonomy;
	}

	@Override
	public Map<String, String> getNERProperties() {
		return null;
	}

	@Override
	public void setConfiguration(Object obj) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Integer getNCBITaxonomy() {
		return NCBITaxonomy;
	} 

	@Override
	@JsonIgnore
	public String getUniqueProcessID() {
		return processUID ;
	}

	public void setNCBITaxonomy(Integer nCBITaxonomy) {
		NCBITaxonomy = nCBITaxonomy;
	}


}
