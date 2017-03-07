package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.configuration;

import com.silicolife.textmining.core.interfaces.process.IE.ner.INERConfiguration;

public interface IKineticREPipelineConfiguration extends INERConfiguration{
	
	public Integer getNCBITaxonomy();
	public String getECNumber();
	public Integer getChEBI();
	public void setNCBITaxonomy(Integer nCBITaxonomy);
	public void setECNumber(String eCNumber);
	public void setChEBI(Integer chEBI);
}
