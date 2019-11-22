package com.silicolife.textmining.processes.corpora.exporters;

import java.util.Iterator;
import java.util.List;

import com.pengyifan.bioc.BioCAnnotation;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.corpora.exporters.IBioCCorpusExporter;
import com.silicolife.textmining.core.interfaces.core.corpora.exporters.IBioCCorpusExporterConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;

public class BioCExporter implements IBioCCorpusExporter{

	private IBioCCorpusExporterConfiguration configuration;

	public BioCExporter(IBioCCorpusExporterConfiguration configuration) {
		this.configuration = configuration;
	}
	
	@Override
	public boolean exportCorpus(ICorpus corpus) throws ANoteException {
		
		String type = configuration.getProcessTypeToExport();
		
		List<IIEProcess> processes = null;
		if(type != null && !type.isEmpty())
			processes = corpus.getIEProcessesFilterByType(type);
		else
			processes = corpus.getIEProcesses();
		
		Long pubSize = InitConfiguration.getDataAccess().getCorpusPublicationsCount(corpus);
		int paginationIndex = 0;
		while( paginationIndex <= pubSize) {
			IDocumentSet publications = InitConfiguration.getDataAccess().getCorpusPublicationsPaginated(corpus, paginationIndex, configuration.getPublicationBatchSize());
			Iterator<IPublication> itpub = publications.iterator();
			IPublication document;
			while(itpub.hasNext()) {
				document = itpub.next();
				for( IIEProcess process : processes) {
					IAnnotatedDocument annotedDocument = new AnnotatedDocumentImpl(document, process, corpus);
					List<IEntityAnnotation> annotations = InitConfiguration.getDataAccess().getAnnotatedDocumentEntities(annotedDocument);
					List<BioCAnnotation> convertedAnn = Anote2BioCConverter.entitiesToBioCAnnotationList(annotations);
				}
			}

			paginationIndex += configuration.getPublicationBatchSize();
		}

		
		return false;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public IBioCCorpusExporterConfiguration getConfiguration() {
		return configuration;
	}

}
