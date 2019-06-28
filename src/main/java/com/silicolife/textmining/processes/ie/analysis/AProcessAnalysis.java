package com.silicolife.textmining.processes.ie.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.analysis.IAnnotatedDocumentStatistics;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;

public abstract class AProcessAnalysis {

	private IIEProcess process;
	private ICorpus corpus;
	private Long publicationsCount;
	private static Integer documentBatch = 1000;
	
	private static Logger log = Logger.getLogger(AProcessAnalysis.class.getName());

	public AProcessAnalysis(IIEProcess process) {
		this.process = process;
	}
	
	public IIEProcess getProcess() {
		return this.process;
	}
	
	public ICorpus getCorpus() {
		return getProcess().getCorpus();
	}
	
	public Long countDocuments() throws ANoteException {
		if(publicationsCount== null)
			this.publicationsCount = InitConfiguration.getDataAccess().getCorpusPublicationsCount(corpus);
		return this.publicationsCount;
	}
	
	public Map<IAnnotatedDocument, IAnnotatedDocumentStatistics> getAllStatisticsByDocument() throws ANoteException{
		Map<IAnnotatedDocument, IAnnotatedDocumentStatistics> map = new HashMap<>();
		int i = 0;
		Long size = countDocuments();
		while(i<=size) {
			
			IDocumentSet docs = InitConfiguration.getDataAccess().getCorpusPublicationsPaginated(this.getCorpus(), i, i+documentBatch);
			for(IPublication doc:docs) {
				IAnnotatedDocument anot = new AnnotatedDocumentImpl(doc, this.getProcess(),this.getCorpus());
				IAnnotatedDocumentStatistics stats = anot.getStatistics();
				map.put(anot, stats);
			}
			i+=documentBatch;
			log.info("Processed statistics of "+ String.valueOf(i) + " documents");
		}

		return map;
	}
}
