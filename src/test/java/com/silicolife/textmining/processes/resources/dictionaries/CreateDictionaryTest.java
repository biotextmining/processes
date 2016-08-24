package com.silicolife.textmining.processes.resources.dictionaries;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateDictionaryTest {

	@Test
	public void createDictionarytest() throws InvalidDatabaseAccess, ANoteException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		createDictionary("Dictionary Name","put notes");
		assertTrue(true);

	}

	public static IResource<IResourceElement> createDictionary(String name,String notes) throws ANoteException {
		String info = notes;
		IResource<IResourceElement> newDictionary = new DictionaryImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newDictionary);
		return newDictionary;
	}
	
	public static IResource<IResourceElement> createDictionary(long id,String name,String notes) throws ANoteException {
		String info = notes;
		IResource<IResourceElement> newDictionary = new DictionaryImpl(id,name, info, true);
		InitConfiguration.getDataAccess().createResource(newDictionary);
		return newDictionary;
	}

}
