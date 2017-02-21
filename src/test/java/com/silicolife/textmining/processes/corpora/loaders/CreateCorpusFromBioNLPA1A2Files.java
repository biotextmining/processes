package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.processes.DatabaseConnectionInit;

public class CreateCorpusFromBioNLPA1A2Files {

	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, IOException {
		BioNLPA2A1CorpusLoader loader = new BioNLPA2A1CorpusLoader();
		DatabaseConnectionInit.init("localhost","3306","textminingcarbontest","root","admin");
		File directory = new File("C:\\Users\\RRodrigues\\Desktop\\tutorial\\BioNLP-ST_2011_genia_train_data_rev1");
		loader.processFile(directory, null);
		Map<Long, IAnnotatedDocument> annotations = loader.getDocumentEntityAnnotations();
		List<IEntityAnnotation> entities = annotations.get(annotations.keySet().iterator().next()).getEntitiesAnnotations();
		System.out.println(entities);
		Map<Long, IAnnotatedDocument> eventannotations = loader.getDocumentEventAnnotations();
		List<IEventAnnotation> events = eventannotations.get(eventannotations.keySet().iterator().next()).getEventAnnotations();
		System.out.println(events);

	}

}
