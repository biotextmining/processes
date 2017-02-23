package com.silicolife.textmining.processes.ie.ner.linnaeus;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.process.ProcessRunStatusConfigurationEnum;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
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
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.NERLinnaeusConfigurationImpl;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.NERLinnaeusPreProcessingEnum;
import com.silicolife.textmining.processes.resources.dictionaries.CreateDictionaryTest;
import com.silicolife.textmining.processes.resources.dictionary.loaders.byocyc.BioMetaEcoCycFlatFileLoader;

public class LinnaeusTest {

//	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException, IOException, InvalidConfigurationException {
		DatabaseConnectionInit.init("localhost","3306","testdaemon","root","admin");
		ICorpus corpus = CreateCorpusFromPublicationManagerTest.createCorpus().getCorpus();
		IDictionary dictionary = createDictionaryAndUpdateditWithByocycFiles();
		INERProcessReport report = executeLinnaeus(corpus, dictionary);
		assertTrue(report.isFinishing());
	}
	
	@Test
	public void test2() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException, IOException, InvalidConfigurationException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
//		long corpusID = 4050288017084732744L;
		long corpusID = 2334324709253224511L;
		long keggResourceID = 8976153089388760683L;
//		long metacycResourceID = 8943458235895149470L;
//		long chebiResourceID = 4976336839593779118L;
//		long bioDBID = 6866333013075336357L;
//		long lexicalStopwordID = 2537673202633729523L;
		List<IDictionary> dictionaries = new ArrayList<>();
		ICorpus corpus = InitConfiguration.getDataAccess().getCorpusByID(corpusID);
		IResource<IResourceElement> keggResource = InitConfiguration.getDataAccess().getResourceByID(keggResourceID);
		IDictionary keggDictionary = new DictionaryImpl(keggResource);
		dictionaries.add(keggDictionary);
//		IResource<IResourceElement> metacycResource = InitConfiguration.getDataAccess().getResourceByID(metacycResourceID);
//		IDictionary metacycDictionary = new DictionaryImpl(metacycResource);
//		dictionaries.add(metacycDictionary);
//		IResource<IResourceElement> chebiResource = InitConfiguration.getDataAccess().getResourceByID(chebiResourceID);
//		IDictionary chebiDictionary = new DictionaryImpl(chebiResource);
//		dictionaries.add(chebiDictionary);
//		IResource<IResourceElement> biodbResource = InitConfiguration.getDataAccess().getResourceByID(bioDBID);
//		IDictionary biodbDictionary = new DictionaryImpl(biodbResource);
//		dictionaries.add(biodbDictionary);
//		IResource<IResourceElement> stopWordsResource = InitConfiguration.getDataAccess().getResourceByID(lexicalStopwordID);
		ILexicalWords stopwords = null;//new LexicalWordsImpl(stopWordsResource);
		INERProcessReport report = executeLinnaeusWithLexicalStopWords(corpus, dictionaries, stopwords);
		assertTrue(report.isFinishing());
	}
	
	public static INERProcessReport executeLinnaeusWithLexicalStopWords(ICorpus corpus,
			List<IDictionary> dictionaries, ILexicalWords stopwords) throws ANoteException, InvalidConfigurationException {
		boolean useabreviation = true;
		boolean normalized = true;
		NERCaseSensativeEnum caseSensitive = NERCaseSensativeEnum.INALLWORDS;
		NERLinnaeusPreProcessingEnum preprocessing = NERLinnaeusPreProcessingEnum.No;
		Disambiguation disambiguation = Disambiguation.OFF;
		boolean usingOtherResourceInfoToImproveRuleAnnotations = false;
		int sizeOfSmallWordsToBeNotAnnotated = 2;
		ResourcesToNerAnote resourceToNER = new ResourcesToNerAnote(caseSensitive, usingOtherResourceInfoToImproveRuleAnnotations, sizeOfSmallWordsToBeNotAnnotated);
		for(IDictionary dictionary : dictionaries){
			resourceToNER.addUsingAnoteClasses(dictionary, dictionary.getResourceClassContent(), dictionary.getResourceClassContent());

		}
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		int numThreads = 4;
		INERLinnaeusConfiguration configurations = new NERLinnaeusConfigurationImpl(corpus,ProcessRunStatusConfigurationEnum.createnew, patterns , resourceToNER, useabreviation , disambiguation , caseSensitive , normalized , numThreads , stopwords , preprocessing , usingOtherResourceInfoToImproveRuleAnnotations, sizeOfSmallWordsToBeNotAnnotated);
		LinnaeusTagger linnaues = new LinnaeusTagger();
		System.out.println("Execute Linnaeus");
		INERProcessReport report = linnaues.executeCorpusNER(configurations);
		return report;
	}

	public static INERProcessReport executeLinnaeus(ICorpus corpus,
			IDictionary dictionary) throws ANoteException, InvalidConfigurationException {
		boolean useabreviation = true;
		boolean normalized = true;
		NERCaseSensativeEnum caseSensitive = NERCaseSensativeEnum.INALLWORDS;
		ILexicalWords stopwords = null;
		NERLinnaeusPreProcessingEnum preprocessing = NERLinnaeusPreProcessingEnum.No;
		Disambiguation disambiguation = Disambiguation.OFF;
		ResourcesToNerAnote resourceToNER = new ResourcesToNerAnote();
		resourceToNER.addUsingAnoteClasses(dictionary, dictionary.getResourceClassContent(), dictionary.getResourceClassContent());
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		int numThreads = 4;
		boolean usingOtherResourceInfoToImproveRuleAnnotations = false;
		int sizeOfSmallWordsToBeNotAnnotated = 0;
		INERLinnaeusConfiguration configurations = new NERLinnaeusConfigurationImpl(corpus,ProcessRunStatusConfigurationEnum.createnew, patterns , resourceToNER, useabreviation , disambiguation , caseSensitive , normalized , numThreads , stopwords , preprocessing , usingOtherResourceInfoToImproveRuleAnnotations, sizeOfSmallWordsToBeNotAnnotated );
		LinnaeusTagger linnaues = new LinnaeusTagger();
		System.out.println("Execute Linnaeus");
		INERProcessReport report = linnaues.executeCorpusNER(configurations);
		return report;
	}
	
	public static INERProcessReport executeLinnaeusWithLexicalStopWords(ICorpus corpus,
			IDictionary dictionary, ILexicalWords stopwords) throws ANoteException, InvalidConfigurationException {
		boolean useabreviation = true;
		boolean normalized = true;
		NERCaseSensativeEnum caseSensitive = NERCaseSensativeEnum.NONE;
		NERLinnaeusPreProcessingEnum preprocessing = NERLinnaeusPreProcessingEnum.No;
		Disambiguation disambiguation = Disambiguation.ON_WHOLE;
		boolean usingOtherResourceInfoToImproveRuleAnnotations = false;
		int sizeOfSmallWordsToBeNotAnnotated = 0;
		ResourcesToNerAnote resourceToNER = new ResourcesToNerAnote(caseSensitive, usingOtherResourceInfoToImproveRuleAnnotations, sizeOfSmallWordsToBeNotAnnotated);
		resourceToNER.addUsingAnoteClasses(dictionary, dictionary.getResourceClassContent(), dictionary.getResourceClassContent());
		Map<String, Pattern> patterns = new HashMap<String, Pattern>();
		int numThreads = 4;
		INERLinnaeusConfiguration configurations = new NERLinnaeusConfigurationImpl(corpus,ProcessRunStatusConfigurationEnum.createnew, patterns , resourceToNER, useabreviation , disambiguation , caseSensitive , normalized , numThreads , stopwords , preprocessing , usingOtherResourceInfoToImproveRuleAnnotations, sizeOfSmallWordsToBeNotAnnotated);
		LinnaeusTagger linnaues = new LinnaeusTagger();
		System.out.println("Execute Linnaeus");
		INERProcessReport report = linnaues.executeCorpusNER(configurations);
		return report;
	}

	public static IDictionary createDictionaryAndUpdateditWithByocycFiles()
			throws ANoteException, IOException {
		System.out.println("Create Dictionary");
		IResource<IResourceElement> resource = CreateDictionaryTest.createDictionary("Biocyc","");
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
	
	public static IDictionary createSimpleDictionaryAndUpdateditWithByocycFiles()
			throws ANoteException, IOException {
		System.out.println("Create Dictionary");
		IResource<IResourceElement> resource = CreateDictionaryTest.createDictionary("Biocyc","");
		IDictionary dictionary = new DictionaryImpl(resource);
		BioMetaEcoCycFlatFileLoader loader = new BioMetaEcoCycFlatFileLoader();
		String byocycFolder = "src/test/resources/BioCyc/small";
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
