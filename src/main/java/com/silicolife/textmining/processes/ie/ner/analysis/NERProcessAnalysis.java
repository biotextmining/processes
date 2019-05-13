package com.silicolife.textmining.processes.ie.ner.analysis;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import com.silicolife.textmining.core.datastructures.analysis.StatisticsImpl;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationType;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.analysis.IAnnotatedDocumentStatistics;
import com.silicolife.textmining.core.interfaces.core.analysis.IStatistics;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class NERProcessAnalysis {

	private IIEProcess nerProcess;
	private ICorpus corpus;
	private Long publicationsCount;
	private static Integer documentBatch = 1000;
	private static Logger log = Logger.getLogger(NERProcessAnalysis.class.getName());

	public NERProcessAnalysis(IIEProcess nerProcess) {
		this.nerProcess = nerProcess;
		this.corpus = nerProcess.getCorpus();
		this.publicationsCount = null;
	}
	
	public Long countDocuments() throws ANoteException {
		if(publicationsCount== null)
			this.publicationsCount = InitConfiguration.getDataAccess().getCorpusPublicationsCount(corpus);
		return this.publicationsCount;
	}
	
	public Long countNERAnnotations() throws ANoteException {
		return InitConfiguration.getDataAccess().countAnnotationsByAnnotationType(nerProcess, AnnotationType.ner);
	}
	
	public Map<IAnoteClass, Long> countNERAnoteClassInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countAnnotationsByClassInProcess(nerProcess);
	}
	
	public Map<IAnnotatedDocument, IAnnotatedDocumentStatistics> getAllStatisticsByDocument() throws ANoteException{
		Map<IAnnotatedDocument, IAnnotatedDocumentStatistics> map = new HashMap<>();
		int i = 0;
		Long size = countDocuments();
		while(i<=size) {
			
			IDocumentSet docs = InitConfiguration.getDataAccess().getCorpusPublicationsPaginated(corpus, i, i+documentBatch);
			for(IPublication doc:docs) {
				IAnnotatedDocument anot = new AnnotatedDocumentImpl(doc, nerProcess, corpus);
				IAnnotatedDocumentStatistics stats = anot.getStatistics();
				map.put(anot, stats);
			}
			i+=documentBatch;
			log.info("Processed statistics of "+ String.valueOf(i) + " documents");
		}

		return map;
	}
	
	public Map<IResourceElement, Long> countNERAnoteResourcesAnnotatedInProcess() throws ANoteException{
		return InitConfiguration.getDataAccess().countAnnotationsByResourceElementInProcess(nerProcess);
	}

	
	public IStatistics<IIEProcess> getProcessStatistics() throws ANoteException{
		IStatistics<IIEProcess> processstatistics = new StatisticsImpl<>();
		
		// generate NER statistics
		IStatistics<IEntityAnnotation> nerstatistics = new StatisticsImpl<>();
		nerstatistics.setTotalCount(this.countNERAnnotations());
		//TODO by entity name w/ synonyms
		processstatistics.getSubStatistics().add(nerstatistics);
		
		//generate class statistics
		IStatistics<IAnoteClass> nerclassstatistics = new StatisticsImpl<>();
		nerclassstatistics.addOrSumAllCountBy(this.countNERAnoteClassInProcess());
		processstatistics.getSubStatistics().add(nerclassstatistics);
		
		//generate resources statistics
		IStatistics<IResourceElement> nerResourceStatistics = new StatisticsImpl<>();
		nerResourceStatistics.addOrSumAllCountBy(this.countNERAnoteResourcesAnnotatedInProcess());
		processstatistics.getSubStatistics().add(nerResourceStatistics);
		// doc id 2120595566144580435 procc: 791710063734696223
		//if annotation does not have resource element, it is null, so group by ann_element, otherwise group by resource_element_id

		
		return processstatistics;
	}
	
}
