package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public interface IOrganismKineticInformationRetrieved {
	public IOrganismKineticInformationReport retrievedByOrganism(int ncbiTaxonomy) throws ANoteException;
	public IOrganismKineticInformationReport retrievedByEnzyme(String ecNumber) throws ANoteException;
	public IOrganismKineticInformationReport retrievedByCompound(int chebiID) throws ANoteException;


}
