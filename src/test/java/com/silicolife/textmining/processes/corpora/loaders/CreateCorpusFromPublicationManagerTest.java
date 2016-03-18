package com.silicolife.textmining.processes.corpora.loaders;

import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.corpora.CorpusCreateConfigurationImpl;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ir.PubmedSearchTest;

public class CreateCorpusFromPublicationManagerTest {

	@Test
	public void createFromAQuery() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		ICorpusCreateReport reportCreateCorpus = createCorpus();
		assertTrue(reportCreateCorpus.isFinishing());
	}

	public static ICorpusCreateReport createCorpus() throws InvalidDatabaseAccess,
			ANoteException, InternetConnectionProblemException {
		IIRSearchProcessReport report = PubmedSearchTest.createQuery();
		System.out.println("Create Corpus");
		CorpusCreation creation = new CorpusCreation();
		String corpusName = "Corpus test";
		CorpusTextType textType = CorpusTextType.Abstract;
		List<IPublication> publictions = report.getQuery().getPublications();
		Set<IPublication> docIds = new HashSet<>(publictions);
		String notes = new String();
		boolean journalRetrievalBefore = false;
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName , notes , docIds , textType , journalRetrievalBefore);
		ICorpusCreateReport reportCreateCorpus = creation.createCorpus(configuration );
		return reportCreateCorpus;
	}
	
	@Test
	public void createFromMultipleQueriesQuery() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IIRSearchProcessReport report = PubmedSearchTest.createQuery();
		IIRSearchProcessReport report2 = PubmedSearchTest.createQuery2();
		CorpusCreation creation = new CorpusCreation();
		String corpusName = "Corpus test";
		CorpusTextType textType = CorpusTextType.Abstract;
		List<IPublication> publictions = report.getQuery().getPublications();
		List<IPublication> publictions2 = report2.getQuery().getPublications();
		Set<IPublication> docIds = new HashSet<>(publictions);
		docIds.addAll(publictions2);
		String notes = new String();
		boolean journalRetrievalBefore = false;
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName , notes , docIds , textType , journalRetrievalBefore);
		ICorpusCreateReport reportCreateCorpus = creation.createCorpus(configuration );
		assertTrue(reportCreateCorpus.isFinishing());
	}



}
