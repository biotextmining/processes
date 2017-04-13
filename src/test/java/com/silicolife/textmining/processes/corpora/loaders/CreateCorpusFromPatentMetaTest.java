package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.corpora.CorpusCreateConfigurationImpl;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.corpora.CorpusCreateSourceEnum;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateCorpusFromPatentMetaTest {

	@Test
	public void fulltext() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","textminingdatabase","root","admin");
		File fileOrDirectory =  new File("src/test/resources/patentmetainfo");
		String corpusName = "Patent FT";
		String notes = "";
		PatentMetaFilesCorpusLoader loader = new PatentMetaFilesCorpusLoader();		
		List<IPublication> list = loader.processFile(fileOrDirectory, new Properties());
		CorpusCreationInBatch corpuscreation = new CorpusCreationInBatch();
		Set<IPublication> docIds = new HashSet<>();
		boolean processJournalRetrievalBeforeNeeded = false;
		CorpusTextType corpusTextType = CorpusTextType.FullText;
		CorpusCreateSourceEnum corpusCreateSourceEnum = CorpusCreateSourceEnum.USPTO;
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName, notes, docIds , corpusTextType , processJournalRetrievalBeforeNeeded , corpusCreateSourceEnum );
		ICorpus corpus = corpuscreation.startCorpusCreation(configuration );
		System.out.println("Create corpus " + corpusName);
		List<IPublication> tmp = new ArrayList<>();
		for(int i=0;i<list.size();i++)
		{
			if(i!=0 && i % 100 == 0)
			{
				System.out.println("Update corpus 100");
				corpuscreation.addPublications(corpus, new HashSet<IPublication>(tmp));
				tmp = new ArrayList<>();
			}
			tmp.add(list.get(i));
		}
		corpuscreation.addPublications(corpus, new HashSet<IPublication>(tmp));
		System.out.println("End " + corpusName);
	}
	
	@Test
	public void abstractTest() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","textminingdatabase","root","admin");
		File fileOrDirectory =  new File("src/test/resources/patentmetainfo");
		String corpusName = "Patent Meta";
		String notes = "";
		PatentMetaFilesCorpusLoader loader = new PatentMetaFilesCorpusLoader();		
		List<IPublication> list = loader.processFile(fileOrDirectory, new Properties());
		CorpusCreationInBatch corpuscreation = new CorpusCreationInBatch();
		Set<IPublication> docIds = new HashSet<>();
		boolean processJournalRetrievalBeforeNeeded = false;
		CorpusTextType corpusTextType = CorpusTextType.Abstract;
		CorpusCreateSourceEnum corpusCreateSourceEnum = CorpusCreateSourceEnum.USPTO;
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName, notes, docIds , corpusTextType , processJournalRetrievalBeforeNeeded , corpusCreateSourceEnum );
		ICorpus corpus = corpuscreation.startCorpusCreation(configuration );
		System.out.println("Create corpus " + corpusName);
		List<IPublication> tmp = new ArrayList<>();
		for(int i=0;i<list.size();i++)
		{
			if(i!=0 && i % 100 == 0)
			{
				System.out.println("Update corpus 100");
				corpuscreation.addPublications(corpus, new HashSet<IPublication>(tmp));
				tmp = new ArrayList<>();
			}
			tmp.add(list.get(i));
		}
		corpuscreation.addPublications(corpus, new HashSet<IPublication>(tmp));
		System.out.println("End " + corpusName);
	}

}
