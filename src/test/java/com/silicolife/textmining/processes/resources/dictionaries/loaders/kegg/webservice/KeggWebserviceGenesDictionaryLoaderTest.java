package com.silicolife.textmining.processes.resources.dictionaries.loaders.kegg.webservice;

import java.io.IOException;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice.KeggWebserviceGenesDictionaryLoader;
import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice.KeggWebserviceGenesDictionaryLoaderConfigurationImpl;

public class KeggWebserviceGenesDictionaryLoaderTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","testkegg","root","admin");
		IResource<IResourceElement> resource = createDictionary("Kegg","");
		IDictionary dictionary = new DictionaryImpl(resource);
		KeggWebserviceGenesDictionaryLoader loader = new KeggWebserviceGenesDictionaryLoader();
		String organism = "sce";
		IDictionaryLoaderConfiguration keggLoaderConfiguration = new KeggWebserviceGenesDictionaryLoaderConfigurationImpl(dictionary, true, organism  );
		loader.loadTerms(keggLoaderConfiguration );
	}
	
	public static IResource<IResourceElement> createDictionary(String name,String notes) throws ANoteException {
		String info = notes;
		IResource<IResourceElement> newDictionary = new DictionaryImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newDictionary);
		return newDictionary;
	}

}
