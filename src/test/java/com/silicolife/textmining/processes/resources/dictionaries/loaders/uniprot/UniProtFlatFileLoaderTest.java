package com.silicolife.textmining.processes.resources.dictionaries.loaders.uniprot;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.report.resources.IResourceUpdateReport;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.resources.dictionary.loaders.uniprot.UniProtFlatFileLoader;

public class UniProtFlatFileLoaderTest {

	@Test
	public void importAll() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","resources_test","root","admin");
		IResource<IResourceElement> resource = createDictionary("Uniport - Test 4","30-05-2018");
		IDictionary dictionary = new DictionaryImpl(resource);
		UniProtFlatFileLoader loader = new UniProtFlatFileLoader();
		Properties properties = new Properties();
		String filepath = "C:\\Users\\Hugo Costa\\Desktop//uniprot_sprot.dat";
		File file = new File(filepath );
		IDictionaryLoaderConfiguration configuration = new DictionaryLoaderConfigurationImpl("Uniprot", dictionary, file , properties , true);
		IResourceUpdateReport report = loader.loadTerms(configuration );
		System.out.println("Terms Added :"+report.getTermsAdding());
		System.out.println("Synonyms Added :"+report.getSynonymsAdding());
		System.out.println("External ids Added :"+report.getExternalIDs());

	}
	
	public void importByOrganism() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","resources_test","root","admin");
		IResource<IResourceElement> resource = createDictionary("Uniport","30-05-2018");
		IDictionary dictionary = new DictionaryImpl(resource);
		UniProtFlatFileLoader loader = new UniProtFlatFileLoader();
		Properties properties = new Properties();
		properties.setProperty(UniProtFlatFileLoader.propertyOrganism, "Escherichia coli");
		String filepath = "C:\\Users\\Hugo Costa\\Desktop//uniprot_sprot.dat";
		File file = new File(filepath );
		IDictionaryLoaderConfiguration configuration = new DictionaryLoaderConfigurationImpl("Uniprot", dictionary, file , properties , true);
		IResourceUpdateReport report = loader.loadTerms(configuration );
		System.out.println("Terms Added :"+report.getTermsAdding());
		System.out.println("Synonyms Added :"+report.getSynonymsAdding());
		System.out.println("External ids Added :"+report.getExternalIDs());

	}
	
	public static IResource<IResourceElement> createDictionary(String name,String notes) throws ANoteException {
		String info = notes;
		IResource<IResourceElement> newDictionary = new DictionaryImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newDictionary);
		return newDictionary;
	}

}
