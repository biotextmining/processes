package com.silicolife.textmining.processes.resources.ontology.chebi;

import java.io.IOException;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.ontology.OntologyImpl;
import com.silicolife.textmining.core.datastructures.resources.ontology.loaders.OntologyLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.ontologies.configuration.IOntologyLoaderConfiguration;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

import uk.ac.ebi.chebi.webapps.chebiWS.model.Entity;

public class ChebiOntologyWSLoaderTest {

//	@Test
	public void isMetabolite() {
		ChebiOntologyWSLoader chebiOntologyWSLoader = new ChebiOntologyWSLoader();
		Entity chebiComplete = new Entity();
		chebiComplete.setChebiId("CHEBI:28885");
		System.out.println(chebiOntologyWSLoader.isMetabolite(chebiComplete));
	}
	
//	@Test
	public void isNotMetabolite() {
		ChebiOntologyWSLoader chebiOntologyWSLoader = new ChebiOntologyWSLoader();
		Entity chebiComplete = new Entity();
		chebiComplete.setChebiId("CHEBI:78547");
		System.out.println(chebiOntologyWSLoader.isMetabolite(chebiComplete));
	}
	
	@Test
	public void createAndloader() throws ANoteException, IOException, InvalidDatabaseAccess {
		DatabaseConnectionInit.init("localhost","3306","testChebi","root","admin");
		IResource<IResourceElement> ontology = createOntology("Chebi Ontology","");		
		ChebiOntologyWSLoader chebiOntologyWSLoader = new ChebiOntologyWSLoader();
		IOntologyLoaderConfiguration configuration = new OntologyLoaderConfigurationImpl(ontology, null, true, true);
		IResourceUpdateReport report = chebiOntologyWSLoader.processOntologyFile(configuration );
		System.out.println("Terms Added : "+report.getTermsAdding());
		System.out.println("Synonyms Added : "+report.getSynonymsAdding());
		System.out.println("External Ids Added : "+report.getExternalIDs());
	}
	
	@Test
	public void loader() throws ANoteException, IOException, InvalidDatabaseAccess {
		DatabaseConnectionInit.init("localhost","3306","testChebi","root","admin");
		long id = 3755728197151750794L;
		IResource<IResourceElement> ontology = new OntologyImpl(id , "", "", true)	;
		ChebiOntologyWSLoader chebiOntologyWSLoader = new ChebiOntologyWSLoader();
		IOntologyLoaderConfiguration configuration = new OntologyLoaderConfigurationImpl(ontology, null, true, true);
		IResourceUpdateReport report = chebiOntologyWSLoader.processOntologyFile(configuration );
		System.out.println("Terms Added : "+report.getTermsAdding());
		System.out.println("Synonyms Added : "+report.getSynonymsAdding());
		System.out.println("External Ids Added : "+report.getExternalIDs());
	}

	private IResource<IResourceElement> createOntology(String name, String info) throws ANoteException {
		IResource<IResourceElement> newOntology = new OntologyImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newOntology);
		return newOntology;
	}

}
