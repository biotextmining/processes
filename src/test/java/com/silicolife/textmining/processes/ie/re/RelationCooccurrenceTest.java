package com.silicolife.textmining.processes.ie.re;

import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.IREProcessReport;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.corpora.loaders.CreateCorpusFromPublicationManagerTest;
import com.silicolife.textmining.processes.ie.ner.linnaeus.LinnaeusTest;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.RECooccurrence;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration.IRECooccurrenceConfiguration;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration.RECooccurrenceConfiguration;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.IRECooccurrenceSentenceModel;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.RECooccurrenceSentenceContiguous;

public class RelationCooccurrenceTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		ICorpus corpus = CreateCorpusFromPublicationManagerTest.createCorpus().getCorpus();
		IDictionary dictionary = LinnaeusTest.createDictionaryAndUpdateditWithByocycFiles();
		INERProcessReport report = LinnaeusTest.executeLinnaeus(corpus, dictionary);
		IIEProcess entityProcess = report.getNERProcess();
		boolean useManualCurationFromOtherProcess = false;
		IRECooccurrenceSentenceModel model = new RECooccurrenceSentenceContiguous();
//		IRECooccurrenceSentenceModel model = new RECooccurrenceSentencePortion();
		IIEProcess manualCurationFromOtherProcess = null;
		IRECooccurrenceConfiguration configuration = new RECooccurrenceConfiguration(corpus, entityProcess, model, useManualCurationFromOtherProcess, manualCurationFromOtherProcess);
		RECooccurrence reCoorrence = new RECooccurrence(configuration );
		System.out.println("Execute Relation Cooccurrence");
		IREProcessReport reportRelationRE = reCoorrence.executeRE();
		assertTrue(reportRelationRE.isFinishing());
	}

}
