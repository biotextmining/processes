package com.silicolife.textmining.processes.ie.schemas.manualcuration;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationType;
import com.silicolife.textmining.core.datastructures.annotation.log.AnnotationLogImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventAnnotationImpl;
import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.CorpusProcessAnnotationLogs;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.exceptions.process.manualcuration.ApplyManualCurationToSchemaException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.processes.manualcuration.RESchemaWithManualCurationReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.AnnotationLogTypeEnum;
import com.silicolife.textmining.core.interfaces.core.annotation.IAnnotationLog;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.report.processes.manualcuration.IRESchemaWithManualCurationReport;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;

public class RESchemaApllyManualCuration {
	
	private Pattern findOffset = Pattern.compile("(\\d+)-(\\d+)");

	
	private IRESchema reSchema;
	private boolean stop = false;

	public RESchemaApllyManualCuration(IRESchema reSchema)
	{
		this.reSchema = reSchema;
	}
	
	public IRESchemaWithManualCurationReport applyManualCurationForEvents(IRESchema reSchemaWithManualCuration) throws ANoteException
	{
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();
		if(reSchemaWithManualCuration.getCorpus().getId() != getBasedRESchema().getCorpus().getId())
		{
			throw new ApplyManualCurationToSchemaException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.re.manualcuration.err.notsamecorpus"));
		}
		CorpusProcessAnnotationLogs annotation = new CorpusProcessAnnotationLogs(reSchemaWithManualCuration,true);
		IRESchemaWithManualCurationReport report = new RESchemaWithManualCurationReportImpl(getBasedRESchema(), reSchemaWithManualCuration);
		if(annotation.getManualCurationChanges() != 0)
		{
			processDocumentChanges(report,annotation,starttime);
		}
		if(stop)
		{
			report.setcancel();
		}
		long endtime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endtime-starttime);
		return report;
	}
	
	private void processDocumentChanges(IRESchemaWithManualCurationReport report,CorpusProcessAnnotationLogs annotation, long starttime) throws ANoteException {
		Set<Long> docIds = annotation.getDocumentWithManualCurationEvents();
		int step=1;
		int total = docIds.size();
		for(long docID : docIds)
		{
			if(stop)
			{
				break;
			}
			applyChangesToDocument(docID,annotation,report);
			memoryAndProgressAndTime(step, total, starttime);
			step++;
		}

	}
	
	private void applyChangesToDocument(long docID,CorpusProcessAnnotationLogs annotations,IRESchemaWithManualCurationReport report) throws ANoteException {
		IPublication doc = reSchema.getCorpus().getArticlesCorpus().getDocument(docID);
		IAnnotatedDocument annnoDocument = new AnnotatedDocumentImpl(doc, reSchema, reSchema.getCorpus());
		SortedSet<IAnnotationLog> documentAnnotations = annotations.getDocumentRELogAnnotation(docID);
		List<IEntityAnnotation> entities = annnoDocument.getEntitiesAnnotations();
		AnnotationPositions entAnnotPositions = new AnnotationPositions();
		List<IEventAnnotation> events = annnoDocument.getEventAnnotations();		
		for(IEntityAnnotation entAnnot : entities)
		{
			entAnnotPositions.addAnnotationWhitConflicts(new AnnotationPosition((int)entAnnot.getStartOffset(), (int)entAnnot.getEndOffset()), entAnnot);
		}
		for(IAnnotationLog annotationLog : documentAnnotations)
		{
			applyChangesToAnnotation(annnoDocument,annotationLog,events,entAnnotPositions,annotations,report);
		}
	}

	
	private void applyChangesToAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, List<IEventAnnotation> relations, AnnotationPositions entAnnotPositions,CorpusProcessAnnotationLogs annotations, IRESchemaWithManualCurationReport report) throws ANoteException {
		
		if(annotationLog.getType().equals(AnnotationLogTypeEnum.RELATIONREMOVE))
		{
			removeEventRElationAnnotation(annnoDocument,annotationLog,relations, entAnnotPositions, annotations,report);
		}
		else if(annotationLog.getType().equals(AnnotationLogTypeEnum.RELATIONADD))
		{
			addEventAnnotation(annnoDocument,annotationLog, relations,entAnnotPositions, annotations,report);
		}
		else if(annotationLog.getType().equals(AnnotationLogTypeEnum.RELATIONUPDATE))
		{
			editEntityAnnotation(annnoDocument,annotationLog, relations,entAnnotPositions, annotations,report);
		}

	}
	
	private void editEntityAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, List<IEventAnnotation> relations,
			AnnotationPositions entAnnotPositions,CorpusProcessAnnotationLogs annotations,IRESchemaWithManualCurationReport report) throws ANoteException {
		IEventAnnotation eventUpdated = annotations.getEventRelationAnnotationIDEventAnnotation().get(annotationLog.getOriginalAnnotationID());
		// compare if event already exists
		IEventAnnotation eventToEdit = eventsMatching(eventUpdated,relations);
		if(eventToEdit==null)
		{
			
			IEventAnnotation oldRelation = calculatesOldRelation(annotationLog,eventUpdated,entAnnotPositions);
			if(oldRelation!=null)
			{
				oldRelation = eventsMatching(oldRelation,relations);
				if(oldRelation!=null)
				{
					long oldID = oldRelation.getId();
					// Try find entities
					eventToEdit = tryfindEntities(eventUpdated,entAnnotPositions);
					if(eventToEdit!=null)
					{	
						eventToEdit = new EventAnnotationImpl(oldID, eventToEdit.getStartOffset(), eventToEdit.getEndOffset(), AnnotationType.re.name(),
								eventToEdit.getEntitiesAtLeft(),
								eventToEdit.getEntitiesAtRight(), eventToEdit.getEventClue(), -1, "", eventToEdit.getEventProperties(),true);
						// Edit relation in Database
						List<IEventAnnotation> list = new ArrayList<IEventAnnotation>();
						list.add(eventToEdit);
						InitConfiguration.getDataAccess().updateEventsAnnotations(list);
						String user = InitConfiguration.getDataAccess().getUser().getAuUsername();;
						AnnotationLogImpl logEvent = new AnnotationLogImpl(oldRelation.getId(), annnoDocument.getCorpus().getId(), annnoDocument.getProcess().getID(), annnoDocument.getId(),
								AnnotationLogTypeEnum.RELATIONUPDATE, oldRelation.toString(),eventToEdit.toString(), "",null,user);
						List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
						annotationLogs.add(logEvent);
						InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
						report.addChangedEvent(annotationLog);
						annotations.getEventRelationAnnotationIDEventAnnotation().put(oldRelation.getId(), eventToEdit);
					}
					else
					{
						report.addMissingAnnotationByMissingEntities(annotationLog);
					}
				}
				else
				{
					report.addMissingAnnotation(annotationLog);
				}
			}
			else
			{
				report.addMissingAnnotation(annotationLog);
			}
		}
		else // Already have the relation
		{
			report.addMissingAnnotationAlreadyAnnotated(annotationLog);
		}
	}

	private IEventAnnotation calculatesOldRelation(IAnnotationLog annotationLog,IEventAnnotation eventUpdated, AnnotationPositions entAnnotPositions) {
		
		List<IEntityAnnotation> entitiesAtRight = new ArrayList<IEntityAnnotation>();
		List<IEntityAnnotation> entitiesAtLeft = new ArrayList<IEntityAnnotation>();
		String entitiesAtLeftStream = annotationLog.getOldString().split("Entities At Left")[1].split("Entities At Right")[0];
		String entitiesAtRightStream = annotationLog.getOldString().split("Entities At Left")[1].split("Entities At Right")[1];
		
		List<AnnotationPosition> posLEft = calculateFromStream(entitiesAtLeftStream);
		List<AnnotationPosition> posRight = calculateFromStream(entitiesAtRightStream);
		for(AnnotationPosition pos:posLEft)
		{
			if(entAnnotPositions.containsKey(pos))
			{
				entitiesAtLeft.add((IEntityAnnotation)entAnnotPositions.get(pos));
			}
			else
			{
				return null;
			}
		}
		for(AnnotationPosition pos:posRight)
		{
			if(entAnnotPositions.containsKey(pos))
			{
				entitiesAtRight.add((IEntityAnnotation)entAnnotPositions.get(pos));
			}
			else
			{
				return null;
			}
		}
		IEventAnnotation result = new EventAnnotationImpl(eventUpdated.getStartOffset(), eventUpdated.getEndOffset(), AnnotationType.re.name(),
				entitiesAtLeft,entitiesAtRight, eventUpdated.getEventClue(), -1, "", eventUpdated.getEventProperties());
		return result;
	}

	private List<AnnotationPosition> calculateFromStream(String entitiesAtLeftStream) {
		List<AnnotationPosition> result = new ArrayList<AnnotationPosition>();
		Matcher m = findOffset.matcher(entitiesAtLeftStream);
		while(m.find())
		{
			int start = Integer.valueOf(m.group(1));
			int end = Integer.valueOf(m.group(2));
			result.add(new AnnotationPosition(start ,end));
		}
		return result;
	}

	private void addEventAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, List<IEventAnnotation> relations,
			AnnotationPositions entAnnotPositions,CorpusProcessAnnotationLogs annotations,IRESchemaWithManualCurationReport report) throws ANoteException {
		IEventAnnotation eventToAdd = annotations.getEventRelationAnnotationIDEventAnnotation().get(annotationLog.getOriginalAnnotationID());
		IEventAnnotation event = eventsMatching(eventToAdd,relations);
		if(event==null)
		{

			eventToAdd = tryfindEntities(eventToAdd,entAnnotPositions);
			if(eventToAdd!=null)
			{
				// Add relation In database
				List<IEventAnnotation> events = new ArrayList<IEventAnnotation>();
				events.add(eventToAdd);
				InitConfiguration.getDataAccess().addProcessDocumentEventAnnoations(reSchema, annnoDocument, events );
				//	Add To Log
				String newRelation = eventToAdd.toString();
				String user = InitConfiguration.getDataAccess().getUser().getAuUsername();
				AnnotationLogImpl logEvent = new AnnotationLogImpl(eventToAdd.getId(), annnoDocument.getCorpus().getId(), annnoDocument.getProcess().getID(), annnoDocument.getId(),
						AnnotationLogTypeEnum.RELATIONADD, "",newRelation, "",null,user );
				List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
				annotationLogs.add(logEvent);
				InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
				report.addEvent(annotationLog);
			}
			else
			{
				report.addMissingAnnotationByMissingEntities(annotationLog);
			}
		}
		else // Already have the relation
		{
			report.addMissingAnnotationAlreadyAnnotated(annotationLog);
		}
	}

	private IEventAnnotation tryfindEntities(IEventAnnotation eventToAdd, AnnotationPositions entAnnotPositions) {
		
		List<IEntityAnnotation> leftEntities = new ArrayList<IEntityAnnotation>();
		List<IEntityAnnotation> rightEntities = new ArrayList<IEntityAnnotation>();
		for(IEntityAnnotation leftAnnotation : eventToAdd.getEntitiesAtLeft())
		{
			AnnotationPosition pos = new AnnotationPosition((int)leftAnnotation.getStartOffset(), (int)leftAnnotation.getEndOffset());
			if(entAnnotPositions.containsKey(pos) && leftAnnotation.getClassAnnotation().getId() == ((IEntityAnnotation) entAnnotPositions.get(pos)).getClassAnnotation().getId())
			{
				leftEntities.add(((IEntityAnnotation) entAnnotPositions.get(pos)));
			}
			else
			{
				return null;
			}
		}
		for(IEntityAnnotation rightAnnotation : eventToAdd.getEntitiesAtRight())
		{
			AnnotationPosition pos = new AnnotationPosition((int)rightAnnotation.getStartOffset(), (int)rightAnnotation.getEndOffset());
			if(entAnnotPositions.containsKey(pos) && rightAnnotation.getClassAnnotation().getId() == ((IEntityAnnotation) entAnnotPositions.get(pos)).getClassAnnotation().getId())
			{
				rightEntities.add(((IEntityAnnotation) entAnnotPositions.get(pos)));
			}
			else
			{
				return null;
			}
		}	
		IEventAnnotation event = new EventAnnotationImpl(eventToAdd.getStartOffset(), eventToAdd.getEndOffset(),
				AnnotationType.re.name(), leftEntities, rightEntities, eventToAdd.getEventClue(), -1, "", eventToAdd.getEventProperties());
		return event;
	}

	private void removeEventRElationAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, List<IEventAnnotation> relations, AnnotationPositions annotPositions,
			CorpusProcessAnnotationLogs annotations,IRESchemaWithManualCurationReport report) throws ANoteException {
		IEventAnnotation eventToRemove = annotations.getEventRelationAnnotationIDEventAnnotation().get(annotationLog.getOriginalAnnotationID());
		IEventAnnotation event = eventsMatching(eventToRemove,relations);
		if(event!=null)
		{
			List<Long> annotation = new ArrayList<Long>();
			annotation.add(event.getId());
			InitConfiguration.getDataAccess().inactiveAnnotations(annotation );
			String oldRelation = event.toString();
			String user = InitConfiguration.getDataAccess().getUser().getAuUsername();
			AnnotationLogImpl logEvent = new AnnotationLogImpl(event.getId(), annnoDocument.getCorpus().getId(), annnoDocument.getProcess().getID(), annnoDocument.getId(),
					AnnotationLogTypeEnum.RELATIONREMOVE, oldRelation,"", "",null,user );
			List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
			annotationLogs.add(logEvent);
			InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
			report.addRemoveChanged(annotationLog);
		}
		else
		{
			report.addMissingAnnotation(annotationLog);
		}
	}
	
	private IEventAnnotation eventsMatching(IEventAnnotation eventGold,List<IEventAnnotation> toCompareStartingInSentence) {
		// For each relation compare
		for(IEventAnnotation toCompareEvent:toCompareStartingInSentence)
		{
			// Compare Events in pars
			if(matchingEvents(eventGold,toCompareEvent))
			{
				return toCompareEvent;
			}
		}
		return null;
	}
	
	private boolean matchingEvents(IEventAnnotation eventGold,IEventAnnotation toCompareEvent) {
		// Compare clues
		if(!eventGold.getEventClue().isEmpty() && toCompareEvent.getEventClue().isEmpty() && !eventGold.getEventClue().equals(toCompareEvent.getEventClue()))
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
				if(entToCom.getClassAnnotation().getId() == entGold.getClassAnnotation().getId())
				{
					return true;
				}
			}
		}
		return false;
	}


	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public IRESchema getBasedRESchema() {
		return reSchema;
	}

}