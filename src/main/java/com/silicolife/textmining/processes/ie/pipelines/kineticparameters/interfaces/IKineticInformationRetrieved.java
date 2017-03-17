package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public interface IKineticInformationRetrieved {
	public IKineticInformationReport retrievedByOrganism(int ncbiTaxonomy) throws ANoteException;
	public IKineticInformationReport retrievedByEnzyme(String ecNumber) throws ANoteException;
	public IKineticInformationReport retrievedByCompound(int chebiID) throws ANoteException;
	public IKineticInformationReport retrievedByCompoundName(String compoundName) throws ANoteException;


}
