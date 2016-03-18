package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.multithreading;

import java.io.IOException;
import java.util.List;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.textprocessing.EntitiesDesnormalization;
import com.silicolife.textmining.core.datastructures.textprocessing.TermSeparator;
import com.silicolife.textmining.core.datastructures.utils.multithearding.IParallelJob;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.NER;

public class NERParallelStep implements IParallelJob<Integer>{

	private String text;
	private List<Long> classIdCaseSensative;
	private IPublication doc;
	private IIEProcess process;
	private int entitiesAdded=0;
	private NER ner;
	private boolean stop = false;
	private boolean caseSensitive;
	private boolean normalization;
	
	
	public NERParallelStep(NER ner,IPublication doc,IIEProcess process,ICorpus corpus,String text,List<Long> classIdCaseSensative,boolean caseSensitive,boolean normalization)
	{
		this.ner=ner;
		this.doc=doc;
		this.process=process;
		this.text=text;
		this.classIdCaseSensative = classIdCaseSensative;
		this.caseSensitive = caseSensitive;
		this.normalization = normalization;
	}
	
	public void run() {
		AnnotationPositions annotationsPositions = new AnnotationPositions();
		try {
			annotationsPositions = ner.executeNer(text,classIdCaseSensative,caseSensitive,normalization);
			if(!stop)
			{
				List<IEntityAnnotation> entityAnnotations = annotationsPositions.getEntitiesFromAnnoattionPositions();
				if(normalization){
					EntitiesDesnormalization desnormalizer = new EntitiesDesnormalization(text, TermSeparator.termSeparator(text), entityAnnotations);
					entityAnnotations = desnormalizer.getDesnormalizedAnnotations();
				}
				InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(process, doc, entityAnnotations);
			}
		} catch (IOException e) {
//			TreatExceptionForAIbench.treatExcepion(e);
		} catch (ANoteException e) {
//			TreatExceptionForAIbench.treatExcepion(e);
		}
		entitiesAdded = annotationsPositions.getAnnotations().size();
		annotationsPositions = null;
	}


	public Integer getResultJob() {
		return entitiesAdded;
	}

	@Override
	public void kill() {
		stop = true;
		ner.stop();	
	}

}
