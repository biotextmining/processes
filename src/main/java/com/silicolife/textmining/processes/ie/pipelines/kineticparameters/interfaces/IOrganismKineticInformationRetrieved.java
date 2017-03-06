package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public interface IOrganismKineticInformationRetrieved {
	public IOrganismKineticInformationReport retrieved(int ncbiTaxonomy) throws ANoteException;

}
