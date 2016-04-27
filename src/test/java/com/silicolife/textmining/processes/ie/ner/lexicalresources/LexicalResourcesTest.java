package com.silicolife.textmining.processes.ie.ner.lexicalresources;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.nlptools.PartOfSpeechLabels;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.corpora.loaders.CreateCorpusFromPublicationManagerTest;
import com.silicolife.textmining.processes.ie.ner.linnaeus.LinnaeusTest;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.NERLexicalResources;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.NERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.NERLexicalResourcesPreProssecingEnum;

public class LexicalResourcesTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException, IOException, InvalidConfigurationException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		ICorpus corpus = CreateCorpusFromPublicationManagerTest.createCorpus().getCorpus();
		IDictionary dictionary = LinnaeusTest.createDictionaryAndUpdateditWithByocycFiles();
		INERProcessReport report = executeLinnaeus(corpus, dictionary);
		assertTrue(report.isFinishing());
	}

	private INERProcessReport executeLinnaeus(ICorpus corpus,IDictionary dictionary) throws ANoteException, InvalidConfigurationException {
		boolean normalized = true;
		NERCaseSensativeEnum caseSensitive = NERCaseSensativeEnum.INALLWORDS;
		ILexicalWords stopwords = null;
		ResourcesToNerAnote resourceToNER = new ResourcesToNerAnote();
		resourceToNER.addUsingAnoteClasses(dictionary, dictionary.getResourceClassContent(), dictionary.getResourceClassContent());
		boolean usingOtherResourceInfoToImproveRuleAnnotations = false;
		NERLexicalResourcesPreProssecingEnum preProcessing = NERLexicalResourcesPreProssecingEnum.POSTagging;	
		Set<String> posTgas = PartOfSpeechLabels.getDefaultPOStags();
		INERLexicalResourcesConfiguration configurations = new NERLexicalResourcesConfiguration(corpus, preProcessing , resourceToNER,
				posTgas, stopwords, caseSensitive, normalized, usingOtherResourceInfoToImproveRuleAnnotations);
		NERLexicalResources nerLexicalResources = new NERLexicalResources();
		System.out.println("Execute Lexical Resources");
		INERProcessReport report = nerLexicalResources.executeCorpusNER(configurations);
		return report;
	}

}
