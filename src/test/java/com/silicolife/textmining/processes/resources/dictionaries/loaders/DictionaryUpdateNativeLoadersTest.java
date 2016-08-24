package com.silicolife.textmining.processes.resources.dictionaries.loaders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.resources.dictionaries.CreateDictionaryTest;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class DictionaryUpdateNativeLoadersTest {

	@Test
	public void updateDictionaryBiocyc() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IResource<IResourceElement> resource = CreateDictionaryTest.createDictionary("Biocyc","");
		IDictionary dictionary = new DictionaryImpl(resource);
		BioMetaEcoCycFlatFileLoader loader = new BioMetaEcoCycFlatFileLoader();
		String byocycFolder = "src/test/resources/BioCyc/small";
		File file = new File(byocycFolder);
		if(loader.checkFile(file ))
		{
			Properties properties = new Properties();
			String loaderUID = "";
			boolean loadExtendalIDds = true;
			IDictionaryLoaderConfiguration configuration = new DictionaryLoaderConfigurationImpl(loaderUID , dictionary, file, properties , loadExtendalIDds );
			loader.loadTerms(configuration );
		}
		else
			assertTrue(false);

		
	}

}
