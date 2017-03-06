package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.processes.ie.ner.linnaeus.LinnaeusTagger;
import com.silicolife.textmining.processes.ie.ner.linnaeus.PublicationIt;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.compthreads.IteratorBasedMaster;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DocumentIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.doc.TaggedDocument;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers.ConcurrentMatcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.LinnauesExecutionData;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces.INERProcessByDocument;

public class LinnaeusTaggerByDocument extends LinnaeusTagger implements INERProcessByDocument{
	
	final static Logger logger = LoggerFactory.getLogger(LinnaeusTaggerByDocument.class);

	
	private INERLinnaeusConfiguration linnauesConfiguration;
	private LinnauesExecutionData linnauesExecutionData;
	private Matcher matcher;
	
	public LinnaeusTaggerByDocument(INERLinnaeusConfiguration linnauesConfiguration) throws ANoteException
	{
		this.linnauesConfiguration=linnauesConfiguration;
		loaddata(linnauesConfiguration);
	}

	
	private void loaddata(INERLinnaeusConfiguration linnauesConfiguration) throws ANoteException {
		linnauesExecutionData = loadExecutionData(linnauesConfiguration);
		matcher = getMatcher(linnauesConfiguration,linnauesExecutionData.getElements());
	}

	public List<IEntityAnnotation> executeDocument(IAnnotatedDocument annotatedDocument) throws ANoteException
	{
		DocumentIterator documents = new PublicationIt(null, annotatedDocument, linnauesConfiguration.getIEProcess());		
		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,1);
		Thread threadmaster = new Thread(master);
		threadmaster.start();
		TaggedDocument td = master.next();
		if (td != null)
		{
			AnnotationPositions positions = new AnnotationPositions();
			AnnotationPositions positionsRules = new AnnotationPositions();
			addMatchesToAnnotationPositions(linnauesConfiguration, linnauesExecutionData, td,positions);
			applyHandRulesToAnnotationPositions(linnauesExecutionData, td, positionsRules);
			return getEntities(td, positions, positionsRules);
		}	
		try {
			threadmaster.join();
		} catch (InterruptedException e) {
			throw new ANoteException(e);
		};
		return new ArrayList<>();
	}


	private List<IEntityAnnotation> getEntities(TaggedDocument td, AnnotationPositions positions,
			AnnotationPositions positionsRules) {
		List<IEntityAnnotation> entityAnnotations = positions.getEntitiesFromAnnoattionPositions();

		entityAnnotations = correctEntitiesAfterNormalization(linnauesConfiguration, td, entityAnnotations);
		AnnotationPositions annotationsPositionsResult = new AnnotationPositions();
		for(IEntityAnnotation entityAnnotation:entityAnnotations)
		{
			AnnotationPosition position = new AnnotationPosition((int) entityAnnotation.getStartOffset(),(int) entityAnnotation.getEndOffset());
			annotationsPositionsResult.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(position, entityAnnotation);
		}
		// Add Rules Entities
		List<IEntityAnnotation> entityAnnotationsRules = positionsRules.getEntitiesFromAnnoattionPositions();
		for(IEntityAnnotation entityAnnotationsRule:entityAnnotationsRules)
		{
			AnnotationPosition position = new AnnotationPosition((int) entityAnnotationsRule.getStartOffset(),(int) entityAnnotationsRule.getEndOffset());
			annotationsPositionsResult.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(position, entityAnnotationsRule);
		}
		return  annotationsPositionsResult.getEntitiesFromAnnoattionPositions();
	}
	
}
