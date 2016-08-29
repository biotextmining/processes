package com.silicolife.textmining.processes.corpora.loaders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.corpora.CorpusCreateConfigurationImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateCorpusFromBiocreativeFiles {

	@Test
	public void createCorpusFromBioCreativeFiles() throws InvalidDatabaseAccess, ANoteException, IOException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		String corpusName = "Biocreative V - chemdner patent training";
		String folder= "src/test/resources/chemdner/trainingset";
		String documentFile = "chemdner_patents_train_text.txt";
		String annotationsFile = "chemdner_cemp_gold_standard_train.tsv";
		CHEMDNERCorpusLoader loader = new CHEMDNERCorpusLoader();
		ICorpusCreateReport reportCreateCorpus = createBioCreativeCorpus(corpusName, folder, documentFile, annotationsFile, loader);
		loadAnnotationsToCorpus(corpusName, loader, reportCreateCorpus);
	}


	private void loadAnnotationsToCorpus(String corpusName, CHEMDNERCorpusLoader loader, ICorpusCreateReport reportCreateCorpus) throws ANoteException {
		String chemdnerCorpusLoader = "CHEMDNER Corpus Loader";
		IProcessOrigin chemdnerCorpusLoaderOrigin= new ProcessOriginImpl(GenerateRandomId.generateID(),chemdnerCorpusLoader);
		ICorpus corpus = reportCreateCorpus.getCorpus();
		IIEProcess nerProcess = new IEProcessImpl(corpus, corpusName,"",ProcessTypeImpl.getNERProcessType(),chemdnerCorpusLoaderOrigin, new Properties());
		InitConfiguration.getDataAccess().createIEProcess(nerProcess);

		IDocumentSet docs = corpus.getArticlesCorpus();
		Iterator<IPublication> docit = docs.iterator();
		while(docit.hasNext() )
		{
			IPublication pub = docit.next();
			if(loader.getDocumentEntityAnnotations().containsKey(pub.getId())){
				IAnnotatedDocument annotDoc = loader.getDocumentEntityAnnotations().get(pub.getId());
				InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(nerProcess, pub, annotDoc.getEntitiesAnnotations());
			}
		}
	}

	
	private ICorpusCreateReport createBioCreativeCorpus(String corpusName, String folder, String documentFile,
			String annotationsFile, CHEMDNERCorpusLoader loader) throws ANoteException, IOException {
		Properties properties = new Properties();
		properties.put(CHEMDNERCorpusLoader.annotationFile, annotationsFile);
		properties.put(CHEMDNERCorpusLoader.documentsFile, documentFile);
		List<IPublication> publications = loader.processFile(new File(folder), properties );
		Set<IPublication> docIds = new HashSet<>(publications);
		CorpusTextType textType = CorpusTextType.Abstract;
		String notes = new String();
		boolean journalRetrievalBefore = false;
		CorpusCreation creation = new CorpusCreation();
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName , notes , docIds , textType , journalRetrievalBefore);
		ICorpusCreateReport reportCreateCorpus = creation.createCorpus(configuration);
		assertTrue(reportCreateCorpus.isFinishing());
		return reportCreateCorpus;
	}

}
