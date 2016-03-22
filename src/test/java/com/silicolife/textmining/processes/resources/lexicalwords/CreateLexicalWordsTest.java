package com.silicolife.textmining.processes.resources.lexicalwords;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.resources.lexiacalwords.LexicalWordsImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateLexicalWordsTest {

	@Test
	public void createLexicalWordstest() throws InvalidDatabaseAccess, ANoteException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		createLexicalWords("Lexical Words Name");
		assertTrue(true);
	}
	
	public static IResource<IResourceElement> createLexicalWords(String name) throws ANoteException {
		String info = "put notes";
		IResource<IResourceElement> newLexicalWords = new LexicalWordsImpl(name, info, true);
		InitConfiguration.getDataAccess().createResource(newLexicalWords);
		return newLexicalWords;
	}

}
