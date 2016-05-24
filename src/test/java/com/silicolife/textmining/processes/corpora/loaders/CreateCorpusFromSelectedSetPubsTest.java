package com.silicolife.textmining.processes.corpora.loaders;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.corpora.CorpusCreateConfigurationImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateCorpusFromSelectedSetPubsTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException {
		DatabaseConnectionInit.init("localhost","3306","metabolites_anote","root","admin");
		Set<IPublication> docIds = new HashSet<>();
		long publicationID = 8854354172438469974L;
		IPublication publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 6103858061614977887L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 1308071351201875060L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 921959066855329165L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 6079445286924805295L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 1785588143226962852L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		publicationID = 4045088280341341648L;
		publication = InitConfiguration.getDataAccess().getPublication(publicationID);
		docIds.add(publication);
		String notes = new String();
		boolean journalRetrievalBefore = false;
		String corpusName = "Corpus publication test";
		CorpusTextType textType = CorpusTextType.Abstract;
		CorpusCreation creation = new CorpusCreation();
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName , notes , docIds , textType , journalRetrievalBefore);
		ICorpusCreateReport reportCreateCorpus = creation.createCorpus(configuration );
		assertTrue(reportCreateCorpus.isFinishing());
	}

}
