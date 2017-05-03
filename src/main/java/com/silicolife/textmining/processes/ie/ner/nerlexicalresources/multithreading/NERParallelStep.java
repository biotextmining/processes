package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.multithreading;

import java.util.List;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.textprocessing.EntitiesDesnormalization;
import com.silicolife.textmining.core.datastructures.textprocessing.TermSeparator;
import com.silicolife.textmining.core.datastructures.utils.multithearding.IParallelJob;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesPreProcessingModel;

public class NERParallelStep implements IParallelJob<Integer>{

	private String text;
	private List<Long> classIdCaseSensative;
	private IPublication doc;
	private IIEProcess process;
	private int entitiesAdded=0;
	private INERLexicalResourcesPreProcessingModel preprocessingmodel;
	private boolean stop = false;
	private NERCaseSensativeEnum caseSensitive;
	private boolean normalization;
	private boolean finished = false;
	
	
	public NERParallelStep(INERLexicalResourcesPreProcessingModel preprocessingmodel,IPublication doc,IIEProcess process,ICorpus corpus,String text,List<Long> classIdCaseSensative,NERCaseSensativeEnum caseSensitive,boolean normalization)
	{
		this.preprocessingmodel=preprocessingmodel;
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
			annotationsPositions = preprocessingmodel.executeNer(text,classIdCaseSensative,caseSensitive,normalization);
			if(!stop)
			{
				List<IEntityAnnotation> entityAnnotations = annotationsPositions.getEntitiesFromAnnoattionPositions();
				if(normalization){
					EntitiesDesnormalization desnormalizer = new EntitiesDesnormalization(text, TermSeparator.termSeparator(text), entityAnnotations);
					entityAnnotations = desnormalizer.getDesnormalizedAnnotations();
				}
				InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(process, doc, entityAnnotations);
			}
		}catch (ANoteException e) {
//			TreatExceptionForAIbench.treatExcepion(e);
		}
		entitiesAdded = annotationsPositions.getAnnotations().size();
		annotationsPositions = null;
		finished=true;
	}


	public Integer getResultJob() {
		return entitiesAdded;
	}

	@Override
	public void kill() {
		stop = true;
		preprocessingmodel.stop();	
	}

	@Override
	public boolean isFinished() {
		return finished;
	}

}
