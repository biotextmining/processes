package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.datastructures;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces.IKineticInformationReport;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces.IKineticInformationRetrieved;
import com.silicolife.textmining.processes.ie.pipelines.utils.OrganismUtils;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;

public abstract class  AOrganismKineticInformationRetrieved implements IKineticInformationRetrieved{
		
	final static Logger logger = LoggerFactory.getLogger(OrganismKineticInformationRetrievedByDocumentSingleton.class);

	private Set<IIEProcess>  processes;

	public abstract void processKineticInformation(IAnnotatedDocument annotedDocument,List<IEventAnnotation> kineticEvents) throws ANoteException;

	public AOrganismKineticInformationRetrieved(Set<IIEProcess>  processes,INERLinnaeusConfiguration configuration,IREKineticREConfiguration kineticREConfiguration) throws ANoteException{
		OrganismKineticInformationRetrievedByDocumentSingleton.getInstance().initNERREConfiguration(configuration, kineticREConfiguration);
		this.processes = processes;	
	}
	
	
	public IKineticInformationReport retrievedByOrganism(int ncbiTaxonomy) throws ANoteException
	{
		logger.info("Process Kinetic Information for Organism (NCBI TAxonomy) :"+ncbiTaxonomy);
		IResourceElement resourceElementOrganism = OrganismUtils.getOrganismResourceElement(ncbiTaxonomy);
		logger.info("NCBI TAxonomy Name:"+resourceElementOrganism.getTerm());
		return findKineticParametersForResourceElement(resourceElementOrganism);
	}
	
	public IKineticInformationReport retrievedByEnzyme(String ecNumber) throws ANoteException
	{
		logger.info("Process Kinetic Information for Enzyme (ECNumber):"+ecNumber);
		IResourceElement resourceElementEnzyme = OrganismUtils.getEnzymeResourceElement(ecNumber);
		logger.info("Enzyme Name:"+resourceElementEnzyme.getTerm());
		return findKineticParametersForResourceElement(resourceElementEnzyme);
	}
	
	public IKineticInformationReport retrievedByCompound(int chebiID) throws ANoteException
	{
		logger.info("Process Kinetic Information for Compound (ChEBI):"+chebiID);
		IResourceElement resourceElementCompound = OrganismUtils.getCompoundResourceElement(chebiID);
		logger.info("Compound Name:"+resourceElementCompound.getTerm());
		return findKineticParametersForResourceElement(resourceElementCompound);
	}

	public IKineticInformationReport retrievedByCompoundName(String compoundName) throws ANoteException
	{
		logger.info("Process Kinetic Information for Compound Name:"+compoundName);
		IResourceElement resourceElementCompound = OrganismUtils.getCompoundResourceElement(compoundName);
		logger.info("Compound Name:"+resourceElementCompound.getTerm());
		return findKineticParametersForResourceElement(resourceElementCompound);
	}
	
	private IKineticInformationReport findKineticParametersForResourceElement(IResourceElement resourceElementOrganism)
			throws ANoteException {
		Set<IResourceElement> organisms = new HashSet<>();
		organisms.add(resourceElementOrganism);
		List<Long> publicationIdsWithOrganism = InitConfiguration.getDataAccess().getPublicationsIdsByResourceElements(organisms);
		int total = publicationIdsWithOrganism.size()*processes.size();
		logger.info("Documents To Process:"+total);
		int step = 0;
		for(IIEProcess process: getProcesses()){
			for(Long publicationIdWithOrganism: publicationIdsWithOrganism){
				IPublication document = InitConfiguration.getDataAccess().getPublication(publicationIdWithOrganism);
				IAnnotatedDocument annotedDocument = new AnnotatedDocumentImpl(document, process, process.getCorpus());
				List<IEventAnnotation> events = OrganismKineticInformationRetrievedByDocumentSingleton.getInstance().execute(annotedDocument);
				processKineticInformation(annotedDocument,events);
				step++;
				logger.info(step + " / " + total);
			}
		}
		return new KineticInformationReportImpl(resourceElementOrganism);
	}
	


	public Set<IIEProcess> getProcesses() {
		return processes;
	}

	public void setProcesses(Set<IIEProcess> processes) {
		this.processes = processes;
	}
}
