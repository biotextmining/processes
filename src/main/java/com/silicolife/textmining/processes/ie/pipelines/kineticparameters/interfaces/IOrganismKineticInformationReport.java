package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import com.silicolife.textmining.core.interfaces.core.report.IReport;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public interface IOrganismKineticInformationReport extends IReport{
	
	public IResourceElement getResourceElementMainOrganism();
//	public Map<IResourceElement,List<IPublication>> getMapKineticParameterPublications();

}
