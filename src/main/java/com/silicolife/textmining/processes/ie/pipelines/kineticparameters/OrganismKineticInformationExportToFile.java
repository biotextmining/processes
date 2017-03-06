package com.silicolife.textmining.processes.ie.pipelines.kineticparameters;

import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.datastructures.AOrganismKineticInformationRetrieved;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps.ExportKineticResultsTOCSVExtension;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;
import com.silicolife.textmining.processes.ie.re.kineticre.io.IREKineticREResultsExportConfiguration;

public class OrganismKineticInformationExportToFile extends AOrganismKineticInformationRetrieved{
	
	private ExportKineticResultsTOCSVExtension export;

	public OrganismKineticInformationExportToFile(Set<IIEProcess>processes,INERLinnaeusConfiguration configuration,
			IREKineticREConfiguration kineticREConfiguration,
			IREKineticREResultsExportConfiguration kineticREResultsExportConfiguration) throws ANoteException {
		super(processes, configuration, kineticREConfiguration);
		export = new ExportKineticResultsTOCSVExtension(kineticREResultsExportConfiguration);
	}

	@Override
	public void processKineticInformation(IAnnotatedDocument annotedDocument, List<IEventAnnotation> kineticEvents)throws ANoteException {
		for(IEventAnnotation event:kineticEvents)
			export.writeEvent(annotedDocument, event);
	}

}
