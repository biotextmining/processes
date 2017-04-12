package com.silicolife.textmining.processes.corpora.loaders;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.corpora.CorpusCreateConfigurationImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.corpora.CorpusCreateSourceEnum;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateCorpusFromPDFDirectoryTest {

	@Test
	public void createCorpusFromPDF() throws ANoteException, IOException, InvalidDatabaseAccess {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		InitConfiguration.addProperty("General.PDFDirectoryDocuments","src/test/resources/pdf/output");
		String directory = "src/test/resources/pdf";
		PDFDirectoryCorpusLoaders loader = new PDFDirectoryCorpusLoaders();
		Properties properties = new Properties();
		List<IPublication> publications = loader.processFile(new File(directory), properties );
		Set<IPublication> docIds = new HashSet<>(publications);
		String notes = new String();
		boolean journalRetrievalBefore = false;
		String corpusName = "Corpus PDF test";
		CorpusTextType textType = CorpusTextType.FullText;
		CorpusCreation creation = new CorpusCreation();
		ICorpusCreateConfiguration configuration = new CorpusCreateConfigurationImpl(corpusName , notes , docIds , textType , journalRetrievalBefore,CorpusCreateSourceEnum.Other);
		ICorpusCreateReport reportCreateCorpus = creation.createCorpus(configuration );
		assertTrue(reportCreateCorpus.isFinishing());
	}

}
