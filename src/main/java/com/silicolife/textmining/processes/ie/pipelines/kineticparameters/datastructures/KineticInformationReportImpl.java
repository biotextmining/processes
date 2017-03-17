package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.datastructures;

import com.silicolife.textmining.core.datastructures.report.ReportImpl;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces.IKineticInformationReport;

public class KineticInformationReportImpl extends ReportImpl implements IKineticInformationReport{

	private IResourceElement resourceElement;
	
	public KineticInformationReportImpl(IResourceElement resourceElement)
	{
		super("Kinetic Information retrieval Report");
		this.resourceElement = resourceElement;
	}
	
	@Override
	public IResourceElement getResourceElement() {
		return resourceElement;
	}

}
