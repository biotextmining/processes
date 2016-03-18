package com.silicolife.textmining.processes.ie.ner.linnaeus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.DictionaryImpl;
import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;
import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.corpora.loaders.CreateCorpusFromPublicationManagerTest;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher.Disambiguation;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.NERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.NERLinnaeusPreProcessingEnum;
import com.silicolife.textmining.processes.resources.dictionaries.CreateDictionaryTest;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class LinnaeusTest {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		ICorpus corpus = CreateCorpusFromPublicationManagerTest.createCorpus().getCorpus();
		IDictionary dictionary = createDictionaryAndUpdateditWithByocycFiles();
		INERProcessReport report = executeLinnaeus(corpus, dictionary);
		assertTrue(report.isFinishing());
	}

	public static INERProcessReport executeLinnaeus(ICorpus corpus,
			IDictionary dictionary) throws ANoteException {
		boolean useabreviation = true;
		boolean normalized = true;
		boolean caseSensitive = true;
		ILexicalWords stopwords = null;
		NERLinnaeusPreProcessingEnum preprocessing = NERLinnaeusPreProcessingEnum.No;
		Disambiguation disambiguation = Disambiguation.OFF;
		ResourcesToNerAnote resourceToNER = new ResourcesToNerAnote();
		resourceToNER.addUsingAnoteClasses(dictionary, dictionary.getResourceClassContent(), dictionary.getResourceClassContent());
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		int numThreads = 4;
		boolean usingOtherResourceInfoToImproveRuleAnnotations = false;
		INERLinnaeusConfiguration configurations = new NERLinnaeusConfiguration(corpus, patterns , resourceToNER, useabreviation , disambiguation , caseSensitive , normalized , numThreads , stopwords , preprocessing , usingOtherResourceInfoToImproveRuleAnnotations );
		LinnaeusTagger linnaues = new LinnaeusTagger(configurations );
		System.out.println("Execute Linnaeus");
		INERProcessReport report = linnaues.executeCorpusNER(corpus);
		return report;
	}

	public static IDictionary createDictionaryAndUpdateditWithByocycFiles()
			throws ANoteException, IOException {
		System.out.println("Create Dictionary");
		IResource<IResourceElement> resource = CreateDictionaryTest.createDictionary("Biocyc");
		IDictionary dictionary = new DictionaryImpl(resource);
		BioMetaEcoCycFlatFileLoader loader = new BioMetaEcoCycFlatFileLoader();
		String byocycFolder = "src/test/resources/BioCyc/data";
		File file = new File(byocycFolder);
		if(loader.checkFile(file))
		{
			Properties properties = new Properties();
			String loaderUID = "";
			boolean loadExtendalIDds = true;
			IDictionaryLoaderConfiguration configuration = new DictionaryLoaderConfigurationImpl(loaderUID , dictionary, file, properties , loadExtendalIDds );
			loader.loadTerms(configuration );
		}
		return dictionary;
	}

}
