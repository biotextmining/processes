package com.silicolife.textmining.processes.resources.dictionaries.loaders.ncbi.pubchem;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

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
import com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.PubChemDicitionayLoaderConfigurationImpl;
import com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.PubChemWebServiceLoader;

public class PubChemWebServiceLoaderTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IResource<IResourceElement> resource = createDictionary("Pubchem","");
		IDictionary dictionary = new DictionaryImpl(resource);
		PubChemWebServiceLoader loader = new PubChemWebServiceLoader();
		Set<String> pubChemIds = new HashSet<>();
		pubChemIds.add("123");
		pubChemIds.add("12345");
		pubChemIds.add("3847");
		pubChemIds.add("32094");
		IDictionaryLoaderConfiguration pubchemConfiguration = new PubChemDicitionayLoaderConfigurationImpl(dictionary, pubChemIds,true );
		loader.loadTerms(pubchemConfiguration );
	}
	
	public static IResource<IResourceElement> createDictionary(String name,String notes) throws ANoteException {
		String info = notes;
		IResource<IResourceElement> newDictionary = new DictionaryImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newDictionary);
		return newDictionary;
	}

}
