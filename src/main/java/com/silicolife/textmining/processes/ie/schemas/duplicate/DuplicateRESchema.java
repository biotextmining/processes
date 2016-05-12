package com.silicolife.textmining.processes.ie.schemas.duplicate;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.report.processes.IREDuplicationReport;
import com.silicolife.textmining.core.datastructures.report.processes.REDuplicationReportImpl;
import com.silicolife.textmining.core.datastructures.schemas.RESchemaImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;

public class DuplicateRESchema  {
	
	private Map<Long, IEntityAnnotation> oldIDEntToNewEntity;
	
	private boolean stop=false;
	private IRESchema reSchematocopy;

	public DuplicateRESchema(IRESchema reSchematocopy){
		this.reSchematocopy = reSchematocopy;
		oldIDEntToNewEntity = new HashMap<Long, IEntityAnnotation>();
	}
	
	public IREDuplicationReport duplicateRE() throws ANoteException{
		IRESchema newREProcess = createNewRESchema();
		long start = GregorianCalendar.getInstance().getTimeInMillis();
		int size = 0;//reSchematocopy.getCorpus().getCorpusStatistics().getDocumentNumber();
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();
		int position = 0;
		IDocumentSet docs = reSchematocopy.getCorpus().getArticlesCorpus();
		Iterator<IPublication> itDocs =docs.iterator();
		while(itDocs.hasNext())
		{				
			if(stop)
			{
				break;
			}
			IPublication doc = itDocs.next();
			IAnnotatedDocument annotDoc = new AnnotatedDocumentImpl(doc,reSchematocopy, reSchematocopy.getCorpus());
			//insert relations in db by document in this process
			List<IEntityAnnotation> newEntities = getDuplicatedEntities(annotDoc.getEntitiesAnnotations());
			InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(newREProcess, doc, newEntities);
			List<IEventAnnotation> oldEvents = annotDoc.getEventAnnotations();
			List<IEventAnnotation> newEvents = getDuplicatedEvents(oldEvents);
			InitConfiguration.getDataAccess().addProcessDocumentEventAnnoations(newREProcess, doc, newEvents);
			position++;
			memoryAndProgressAndTime(position, size, starttime);
		}
		long end = GregorianCalendar.getInstance().getTimeInMillis();
		IRESchema reSchema = new RESchemaImpl(newREProcess);
		REDuplicationReportImpl report = new REDuplicationReportImpl("", reSchema);
		if(stop)
			report.setcancel();
		report.setTime(end-start);
		return report;
	}

	private List<IEventAnnotation> getDuplicatedEvents(
			List<IEventAnnotation> oldEvents) {
		List<IEventAnnotation> newEvents = new ArrayList<>();
		for(IEventAnnotation eventToReplicate : oldEvents){
			eventToReplicate.generateNewId();
			eventToReplicate.setEntitiesAtLeft(getSideNewEntities(eventToReplicate.getEntitiesAtLeft()));
			eventToReplicate.setEntitiesAtRight(getSideNewEntities(eventToReplicate.getEntitiesAtRight()));
			newEvents.add(eventToReplicate);
		}
		return newEvents;
	}

	private List<IEntityAnnotation> getDuplicatedEntities(
			List<IEntityAnnotation> oldentities) {
		List<IEntityAnnotation> newEntities = new ArrayList<>();
		for(IEntityAnnotation entityToReplicate : oldentities){
			Long oldID = entityToReplicate.getId();
			entityToReplicate.generateNewId();
			oldIDEntToNewEntity.put(oldID, entityToReplicate);
			newEntities.add(entityToReplicate);
		}
		return newEntities;
	}

	private List<IEntityAnnotation> getSideNewEntities(List<IEntityAnnotation> oldEntities) {
		List<IEntityAnnotation> sideEntities = new ArrayList<>();
		for(IEntityAnnotation entity :oldEntities){
			sideEntities.add(oldIDEntToNewEntity.get(entity.getId()));
		}
		return sideEntities;
	}

	private IRESchema createNewRESchema() throws ANoteException {
		IEProcessImpl newREProcess = new IEProcessImpl(reSchematocopy.getCorpus(), reSchematocopy.getName() + " (Duplicated)", reSchematocopy.getNotes(), reSchematocopy.getType(), reSchematocopy.getProcessOrigin(), addDuplicatedProperty(reSchematocopy));
		// Saves it in the DB
		InitConfiguration.getDataAccess().createIEProcess(newREProcess);
		// Converts to RESchema
		IRESchema reProcess = new RESchemaImpl(newREProcess);
		return reProcess;
	}
	
	private Properties addDuplicatedProperty(IRESchema reSchematocopy2){
		// Add duplicated from ID propriety to new schema
		Properties properties = reSchematocopy2.getProperties();
		properties.put(GlobalNames.duplicatedFrom, String.valueOf(reSchematocopy2.getID()));
		return properties;
	}
	
	
	
	protected void memoryAndProgressAndTime(int step, int total,long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}
	
	public void stop() {
		stop = true;		
	}

}
