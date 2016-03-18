package com.silicolife.textmining.processes.ie.schemas.evaluation;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.exceptions.evaluation.EvaluationException;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.report.processes.evaluation.INESchemasEvaluationReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.evaluation.IRESchemaEvaluationReport;
import com.silicolife.textmining.core.interfaces.process.IE.INERSchema;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.core.interfaces.process.IE.ner.INERSchemaEvaluation;
import com.silicolife.textmining.core.interfaces.process.IE.re.IRESchemaEvaluation;
import com.silicolife.textmining.processes.ie.schemas.evaluation.ner.NERSchemasClassEvaluationImpl;
import com.silicolife.textmining.processes.ie.schemas.evaluation.ner.NERSchemasEvaluationReportImpl;
import com.silicolife.textmining.processes.ie.schemas.evaluation.ner.NERSchemasEvaluationResultsImpl;
import com.silicolife.textmining.processes.ie.schemas.evaluation.re.RESchemaEvaluationImpl;
import com.silicolife.textmining.processes.ie.schemas.evaluation.re.RESchemasEvaluationReportImpl;
import com.silicolife.textmining.processes.ie.schemas.evaluation.re.RESchemasEvaluationResults;




public class SchemasEvaluation {


	private boolean stop = false;
	private boolean allowClueOverlap = true;
	private boolean synonymsRange = true;


	public SchemasEvaluation()
	{

	}
	/**
	 * Evaluation two different INERschemas where one are the goldenStandard and the other the schema to compare
	 * 
	 * @param nerSchemaGoldStandard
	 * @param nerSchemaForComparing
	 * @return
	 * @throws SQLException
	 * @throws DatabaseLoadDriverException
	 * @throws IOException
	 * @throws EvaluationException
	 */
	public INESchemasEvaluationReport evaluateNERSchemas(INERSchema nerSchemaGoldStandard, INERSchema nerSchemaForComparing) throws ANoteException{
		stop=false;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		// test if Processes belong to the same corpus 
		if(nerSchemaGoldStandard.getCorpus().getId() != nerSchemaForComparing.getCorpus().getId())
		{
			throw new EvaluationException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.re.evaluation.err.notsamecorpus"));
		}
		IDocumentSet documentSet = nerSchemaGoldStandard.getCorpus().getArticlesCorpus();
		Iterator<IPublication> itDocs = documentSet.iterator();
		int step=1;
		int total = documentSet.size();
		NERSchemasEvaluationResultsImpl results = new NERSchemasEvaluationResultsImpl();
		//store entity results for each document in datastructure
		while(itDocs.hasNext())
		{
			if(stop)
			{
				break;
			}
			IPublication doc = itDocs.next();
			AnnotatedDocumentImpl goldenStandardDocument = new AnnotatedDocumentImpl(doc,nerSchemaGoldStandard, nerSchemaGoldStandard.getCorpus());
			AnnotatedDocumentImpl toCompareDocument = new AnnotatedDocumentImpl(doc,nerSchemaForComparing, nerSchemaForComparing.getCorpus());
			generateResultsbyDocument(goldenStandardDocument, toCompareDocument, results);
			memoryAndProgressAndTime(step, total, startTime);
			step++;
		}
		INERSchemaEvaluation result = new NERSchemasClassEvaluationImpl(results);
		INESchemasEvaluationReport report = new NERSchemasEvaluationReportImpl("", result , nerSchemaGoldStandard, nerSchemaForComparing);
		return report;
	}


	private void generateResultsbyDocument(AnnotatedDocumentImpl goldenStandardDocument, AnnotatedDocumentImpl toCompareDocument, NERSchemasEvaluationResultsImpl results) throws ANoteException{
		//let's get all entities from Gold NER in order to compare with entities in tocompare NER.
		List<IEntityAnnotation> entitiesFromGold = goldenStandardDocument.getEntitiesAnnotations();
		List<IEntityAnnotation> entitiesFromToCompare = toCompareDocument.getEntitiesAnnotations();

		for(IEntityAnnotation goldEntity : entitiesFromGold){
			if(findEntityInToCompareAndUpdateToCompareList(goldEntity,entitiesFromToCompare)){
				results.addToEntitiesInBothNER(goldenStandardDocument.getId(), goldEntity);
			} else{
				results.addToentitiesOnlyNERGoldStandart(goldenStandardDocument.getId(), goldEntity);
			}
		}
		for(IEntityAnnotation onlyInToCompareEntity :entitiesFromToCompare){
			results.addToentitiesOnlyNERForComparingbyDocument(goldenStandardDocument.getId(), onlyInToCompareEntity);
		}
		
	}
	
	private boolean findEntityInToCompareAndUpdateToCompareList(IEntityAnnotation goldEntity,List<IEntityAnnotation> entitiesFromToCompare) {
		Iterator<IEntityAnnotation> itToCompare = entitiesFromToCompare.iterator();
		int index = 0;
		while(itToCompare.hasNext()){
			IEntityAnnotation entityToCompare = itToCompare.next();
			// test the same position
			if(entityToCompare.getStartOffset() == goldEntity.getStartOffset() && entityToCompare.getEndOffset() == goldEntity.getEndOffset())
			{
				// Test the same class
				if(entityToCompare.getClassAnnotation().equals(goldEntity.getClassAnnotation()))
				{
					entitiesFromToCompare.remove(index);
					return true;
				}
			}
			index++;
		}
		return false;
	}

	/**
	 * Evaluation two different IREschemas where one are the goldenStandard and the other the schema to compare
	 * 
	 * @param goldenStandard
	 * @param toCompare
	 * @return
	 * @throws EvaluationException
	 * @throws DatabaseLoadDriverException 
	 * @throws SQLException 
	 * @throws IOException 
	 */
	public IRESchemaEvaluationReport evaluateRESchemas(IRESchema goldenStandard,IRESchema toCompare) throws ANoteException
	{
		stop = false;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		// Test if Process has the same corpus
		if(goldenStandard.getCorpus().getId() != toCompare.getCorpus().getId())
		{
			throw new EvaluationException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.re.evaluation.err.notsamecorpus"));
		}
		IDocumentSet documentSet = goldenStandard.getCorpus().getArticlesCorpus();
		int step=1;
		int total = documentSet.size();
		Iterator<IPublication> iterator = documentSet.iterator();
		// Class to save the results
		RESchemasEvaluationResults result = new RESchemasEvaluationResults();
		while(iterator.hasNext())
		{
			if(stop)
			{
				break;
			}
			IPublication document = iterator.next();
			IAnnotatedDocument goldenStandardDocument = new AnnotatedDocumentImpl(document,goldenStandard, goldenStandard.getCorpus());
			IAnnotatedDocument toCompareDocument = new AnnotatedDocumentImpl(document,toCompare, goldenStandard.getCorpus());
			// evaluate RE document
			processDocumetREEvaluation(goldenStandardDocument,toCompareDocument,result);
			memoryAndProgressAndTime(step, total, startTime);
			step++;
		}
		IRESchemaEvaluation evaluation = new RESchemaEvaluationImpl(result);
		IRESchemaEvaluationReport report = new RESchemasEvaluationReportImpl(evaluation , goldenStandard, toCompare);
		if(stop)
		{
			report.setcancel();
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}

	private void processDocumetREEvaluation(IAnnotatedDocument goldenStandardDocument, IAnnotatedDocument toCompareDocument, RESchemasEvaluationResults result) throws ANoteException {
		// Get Golden Standard Events
		List<IEventAnnotation> goldenEvents = goldenStandardDocument.getEventAnnotations();	
		// Get ToCompare Events
		List<IEventAnnotation> toCompareEvents = copylist(toCompareDocument.getEventAnnotations());
		// Get Entities to ToCompare
		List<IEntityAnnotation> toCompareEntities = toCompareDocument.getEntitiesAnnotations();
		// Entities Sorted
		SortedMap<AnnotationPosition, IEntityAnnotation> toCompareEntitiesSorted = getOnlyEntitiesInSentenceRange(toCompareEntities);

		for(IEventAnnotation eventGold:goldenEvents)
		{
			// Test if entities are present in both Documents
			if(eventsMissingByEntities(eventGold,toCompareEntitiesSorted))
			{
				result.addEventMissingForMissingEntities(goldenStandardDocument,eventGold);
			}
			else if(eventsMatching(eventGold,toCompareEvents)) // Find Similar Events
			{
				result.addEventMatching(goldenStandardDocument,eventGold);
			}
			else // Just in Golden Standard
			{
				result.addEventJustInGoldenStandard(goldenStandardDocument,eventGold);
			}
		} 
		result.addEventJustInToCompare(goldenStandardDocument, toCompareEvents);
	}


	private List<IEventAnnotation> copylist(List<IEventAnnotation> eventAnnotations) {
		List<IEventAnnotation> result = new ArrayList<IEventAnnotation>();
		for(IEventAnnotation ev:eventAnnotations)
			result.add(ev);
		return result;
	}

	private boolean eventsMatching(IEventAnnotation eventGold,List<IEventAnnotation> toCompareStartingInSentence) {
		IEventAnnotation aux = null;
		boolean result = false;
		for(IEventAnnotation toCompareEvent:toCompareStartingInSentence)
		{
			// Compare Events in pars
			if(matchingEvents(eventGold,toCompareEvent))
			{
				result =  true;
				aux = toCompareEvent;
				break;
			}
		}
		// If Found remove from the search
		if(result)
			toCompareStartingInSentence.remove(aux);
		return result;
	}

	private boolean matchingEvents(IEventAnnotation eventGold,IEventAnnotation toCompareEvent) {
		if(allowClueOverlap)
		{

			// Compare clues
			if(eventGold.getEventClue().isEmpty() && !eventGold.getEventClue().equals(toCompareEvent.getEventClue()))
			{
				return false;
			}
			else if(toCompareEvent.getEventClue().isEmpty() && !eventGold.getEventClue().equals(toCompareEvent.getEventClue()))
			{
				return false;
			}
			
			if( (eventGold.getEventClue().isEmpty() && toCompareEvent.getEventClue().isEmpty()) 
				|| ((eventGold.getStartOffset() == eventGold.getEndOffset()) && (toCompareEvent.getStartOffset() == toCompareEvent.getEndOffset())))
			{
				if(sameEntityFinder(getAllEntitiesFromEvent(eventGold), getAllEntitiesFromEvent(toCompareEvent))){
					return true;
				}
				return false;
			}
			if(insideClue(eventGold,toCompareEvent) || insideClue(toCompareEvent,eventGold))
			{
				if(sameEntities(eventGold,toCompareEvent))
				{
					return true;
				}
				return false;
			}
			else
			{
				return false;
			}
		}
		else
		{
			// Compare clues
			if(!eventGold.getEventClue().equals(toCompareEvent.getEventClue()))
			{
				return false;
			}
			// Compare Clue Position
			else if(eventGold.getStartOffset() != toCompareEvent.getStartOffset() || eventGold.getEndOffset() != toCompareEvent.getEndOffset())
			{
				return false;
			}
			// Same Clue same position test if entities are the same
			else
			{
				if(sameEntities(eventGold,toCompareEvent))
				{
					return true;
				}
			}
			return false;
		}
	}

	private boolean insideClue(IEventAnnotation eventClue,IEventAnnotation eventClueInside) {
		if(eventClue.getStartOffset()<=eventClueInside.getStartOffset() && eventClueInside.getEndOffset() <= eventClue.getEndOffset())
		{
			return true;
		}
		return false;
	}

	private boolean sameEntities(IEventAnnotation eventGold,IEventAnnotation toCompareEvent) {
		List<IEntityAnnotation> entGoldLT = eventGold.getEntitiesAtLeft();
		List<IEntityAnnotation> entToComLT = toCompareEvent.getEntitiesAtLeft();
		// Test if enenties at left are the same
		if(!sameEntityFinder(entGoldLT,entToComLT))
		{
			return false;
		}
		List<IEntityAnnotation> entGoldRT = eventGold.getEntitiesAtRight();
		List<IEntityAnnotation> entToComRT = toCompareEvent.getEntitiesAtRight();
		// Test if the entities at right are the same
		if(!sameEntityFinder(entGoldRT,entToComRT))
		{
			return false;
		}
		return true;
	}

	private boolean sameEntityFinder(List<IEntityAnnotation> entsGold,List<IEntityAnnotation> entsToCom) {
		// Test if the size are the same
		if(entsGold.size() != entsToCom.size())
		{
			return false;
		}
		for(IEntityAnnotation entGold:entsGold)
		{
			// test if in entsToCom are the entity entGold
			if(!existsEntity(entGold,entsToCom))
			{
				return false;
			}
		}
		return true;
	}


	private boolean existsEntity(IEntityAnnotation entGold,List<IEntityAnnotation> entsToCom) {
		for(IEntityAnnotation entToCom:entsToCom)
		{
			// test the same position
			if(entToCom.getStartOffset() == entGold.getStartOffset() && entToCom.getEndOffset() == entGold.getEndOffset())
			{
				// Test the same class
				if(entToCom.getClassAnnotation().equals(entGold.getClassAnnotation()))
				{
					return true;
				}
			}
			else if(entToCom.getResourceElement()!=null && entToCom.getResourceElement().equals(entGold.getResourceElement()))
			{
				return true;
			}
		}
		return false;
	}
	
	private List<IEntityAnnotation> getAllEntitiesFromEvent(IEventAnnotation event){
		List<IEntityAnnotation> allEntities = new ArrayList<>();
		allEntities.addAll(event.getEntitiesAtLeft());
		allEntities.addAll(event.getEntitiesAtRight());
		return allEntities;
	}

	private boolean eventsMissingByEntities(IEventAnnotation eventGold,
			SortedMap<AnnotationPosition, IEntityAnnotation> toCompareEntities) {
		List<IEntityAnnotation> entitiesAtLeft = eventGold.getEntitiesAtLeft();
		List<IEntityAnnotation> entitiesAtRight = eventGold.getEntitiesAtRight();
		for(IEntityAnnotation entLF :entitiesAtLeft)
		{
			AnnotationPosition position = new AnnotationPosition(Integer.valueOf(String.valueOf(entLF.getStartOffset())), Integer.valueOf(String.valueOf(entLF.getEndOffset())));
			if(!toCompareEntities.containsKey(position))
			{
				if(synonymsRange && entLF.getResourceElement() != null)
				{
					if(!getEntitiesSynonyms(toCompareEntities, entLF))
					{
						return true;
					}

				}
				else
					return true;
			}
		}
		for(IEntityAnnotation entRT :entitiesAtRight)
		{
			AnnotationPosition position = new AnnotationPosition(Integer.valueOf(String.valueOf(entRT.getStartOffset())), Integer.valueOf(String.valueOf(entRT.getEndOffset())));
			if(!toCompareEntities.containsKey(position))
			{
				if(synonymsRange && entRT.getResourceElement()!= null)
				{
					if(!getEntitiesSynonyms(toCompareEntities, entRT))
					{
						return true;
					}
				}
				else
					return true;
			}
		}
		return false;
	}

	private boolean getEntitiesSynonyms(SortedMap<AnnotationPosition, IEntityAnnotation> toCompareEntities,IEntityAnnotation entLF) {
		for(IEntityAnnotation ent : toCompareEntities.values())
		{
			if(ent.getResourceElement()!=null && ent.getResourceElement().equals(entLF.getResourceElement()))
			{
				return true;
			}
		}
		return false;
	}

	private SortedMap<AnnotationPosition, IEntityAnnotation> getOnlyEntitiesInSentenceRange(List<IEntityAnnotation> entities) {
		SortedMap<AnnotationPosition, IEntityAnnotation> result = new TreeMap<AnnotationPosition, IEntityAnnotation>();
		for(IEntityAnnotation ent :entities)
		{
			result.put(new AnnotationPosition(Integer.valueOf(String.valueOf(ent.getStartOffset())), Integer.valueOf(String.valueOf(ent.getEndOffset()))), ent);
		}
		return result;
	}

	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public void stop() {
		this.stop  = true;
	}

}
