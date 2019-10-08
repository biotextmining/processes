package com.silicolife.textmining.processes.ie.schemas.manualcuration;

import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.log.AnnotationLogImpl;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.corpus.CorpusProcessAnnotationLogs;
import com.silicolife.textmining.core.datastructures.exceptions.process.manualcuration.ApplyManualCurationToSchemaException;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.report.processes.manualcuration.NERSchemaWithManualCurationReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.AnnotationLogTypeEnum;
import com.silicolife.textmining.core.interfaces.core.annotation.IAnnotationLog;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.processes.manualcuration.INERSchemaWithManualCurationReport;
import com.silicolife.textmining.core.interfaces.process.IE.INERSchema;

public class NERSchemaApplyManualCuration {

	private INERSchema nerSchema;
	private boolean stop = false;

	public NERSchemaApplyManualCuration(INERSchema nerSchema)
	{
		this.nerSchema = nerSchema;
	}

	public INERSchemaWithManualCurationReport applyManualCurationForEntities(INERSchema nerSchemaWithManualCuration) throws ANoteException
	{
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();
		if(nerSchemaWithManualCuration.getCorpus().getId() != getBasedNERSchema().getCorpus().getId())
		{
			throw new ApplyManualCurationToSchemaException(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.ner.manualcuration.err.notsamecorpus"));
		}
		CorpusProcessAnnotationLogs annotation = new CorpusProcessAnnotationLogs(nerSchemaWithManualCuration,true);
		NERSchemaWithManualCurationReportImpl report = new NERSchemaWithManualCurationReportImpl(getBasedNERSchema(), nerSchemaWithManualCuration);
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


	private void processDocumentChanges(NERSchemaWithManualCurationReportImpl report,CorpusProcessAnnotationLogs annotation, long starttime) throws ANoteException {
		Set<Long> docIds = annotation.getDocumentWithManualCurationEntities();
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

	private void applyChangesToDocument(long docID,CorpusProcessAnnotationLogs annotations,INERSchemaWithManualCurationReport report) throws ANoteException {
		IPublication doc = nerSchema.getCorpus().getArticlesCorpus().getDocument(docID);
		IAnnotatedDocument annnoDocument = new AnnotatedDocumentImpl(doc, nerSchema, nerSchema.getCorpus());
		SortedSet<IAnnotationLog> documentAnnotations = annotations.getDocumentNERLogAnnotation(docID);
		List<IEntityAnnotation> entities = annnoDocument.getEntitiesAnnotations();
		AnnotationPositions annotPositions = new AnnotationPositions();
		for(IEntityAnnotation entAnnot : entities)
		{
			annotPositions.addAnnotationWhitConflicts(new AnnotationPosition((int)entAnnot.getStartOffset(), (int)entAnnot.getEndOffset()), entAnnot);
		}
		for(IAnnotationLog annotationLog : documentAnnotations)
		{
			applyChangesToAnnotation(annnoDocument,annotationLog,annotPositions,annotations,report);
		}
	}

	private void applyChangesToAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, AnnotationPositions annotPositions,CorpusProcessAnnotationLogs annotations, INERSchemaWithManualCurationReport report) throws ANoteException{
		if(annotationLog.getType().equals(AnnotationLogTypeEnum.ENTITYREMOVE))
		{
			removeEntityAnnotation(annnoDocument,annotationLog, annotPositions, annotations,report);
		}
		else if(annotationLog.getType().equals(AnnotationLogTypeEnum.ENTITYADD))
		{
			addEntityAnnotation(annnoDocument,annotationLog, annotPositions, annotations,report);

		}
		else if(annotationLog.getType().equals(AnnotationLogTypeEnum.ENTITYUPDATE))
		{
			editEntityAnnotation(annnoDocument,annotationLog, annotPositions, annotations,report);
		}

	}

	private void addEntityAnnotation(IAnnotatedDocument annnoDocument,
			IAnnotationLog annotationLog, AnnotationPositions annotPositions,
			CorpusProcessAnnotationLogs annotations,
			INERSchemaWithManualCurationReport report) throws ANoteException {
		if(annotationLog.getOriginalAnnotationID()>0)
		{
			IEntityAnnotation addEntity = annotations.getEntityAnnotationByID(annotationLog.getOriginalAnnotationID());
			// Test if Entity annotation is different from null
			if(addEntity!=null)
			{
				AnnotationPosition entPOs = new AnnotationPosition((int)addEntity.getStartOffset(), (int)addEntity.getEndOffset());
				// Test if document don´tn have entity in conflit position
				if(!annotPositions.containsConflits(entPOs))
				{
					// Insert on database
					IEntityAnnotation newAddEntity = new EntityAnnotationImpl(addEntity.getStartOffset(),
							addEntity.getEndOffset(), addEntity.getClassAnnotation(), addEntity.getResourceElement(), addEntity.getAnnotationValue(), addEntity.isAbreviation(),addEntity.isValidated(),addEntity.getProperties());			
					List<IEntityAnnotation> entityAnnotations = new ArrayList<IEntityAnnotation>();
					entityAnnotations.add(newAddEntity);
					InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(nerSchema, annnoDocument, entityAnnotations );
					String notes = "Added for Curation Process";
					String termClass = newAddEntity.getClassAnnotation().getName();
					String newStr = newAddEntity.getAnnotationValue() + " ("+ termClass  + ") Text Offset: "+entPOs.getStart()+ "-"+entPOs.getEnd();
					String user = InitConfiguration.getDataAccess().getUser().getAuUsername();
					AnnotationLogImpl log = new AnnotationLogImpl(newAddEntity.getId(), annnoDocument.getCorpus().getId(),
							annnoDocument.getProcess().getId(), annnoDocument.getId(), AnnotationLogTypeEnum.ENTITYADD, "", newStr, notes, null, user );
					List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
					annotationLogs.add(log);
					InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
					report.addInsertChanged(annotationLog);
					annotPositions.addAnnotationWhitConflicts(entPOs, newAddEntity);
				}
				else
				{
					report.addMissingConflitAnnotation(annotationLog);
				}
			}
			else
			{
				report.addMissingAnnotationByEntityNull(annotationLog);
			}
		}
		else
		{
			report.addMissingAnnotationByID(annotationLog);
		}		
	}

	private void editEntityAnnotation(IAnnotatedDocument annnoDocument,IAnnotationLog annotationLog, AnnotationPositions annotPositions,CorpusProcessAnnotationLogs annotations,
			INERSchemaWithManualCurationReport report) throws ANoteException {
		// Test if annotation log has original entity annotation ID
		if(annotationLog.getOriginalAnnotationID()>0)
		{
			IEntityAnnotation oldEntity = annotations.getEntityAnnotationByID(annotationLog.getOriginalAnnotationID());
			// Test if Entity annotation is different from null
			if(oldEntity!=null)
			{
				AnnotationPosition entPOs = new AnnotationPosition((int)oldEntity.getStartOffset(), (int)oldEntity.getEndOffset());
				// Test if document has entity in same position
				if(annotPositions.containsKey(entPOs))
				{
					IEntityAnnotation entAnnot = (IEntityAnnotation) annotPositions.get(entPOs);
					AnnotationPosition entPOS = new AnnotationPosition((int)entAnnot.getStartOffset(), (int)entAnnot.getEndOffset());
					if(entPOs.equals(entPOS))
					{
						String oldClass = annotationLog.getOldString().substring(annotationLog.getOldString().indexOf("(")+1,annotationLog.getOldString().indexOf(")"));
						String newClass = annotationLog.getNewString().substring(annotationLog.getNewString().indexOf("(")+1,annotationLog.getNewString().indexOf(")"));

						// Test the class of the entity is the same
						if(oldClass!=null && newClass!=null && !oldClass.equals(newClass))
						{
							IAnoteClass newClassID = ClassPropertiesManagement.getClassIDClassName(newClass);
							// Update Memory entity
							entAnnot.setClassAnnotation(newClassID);
							oldEntity.setClassAnnotation(newClassID);
							// Edit Annotation in Database		
							List<IEntityAnnotation> list = new ArrayList<IEntityAnnotation>();
							list.add(entAnnot);
							InitConfiguration.getDataAccess().updateEntityAnnotations(list);
							String notes = "Updated for Curation Process";
							// Get Class Name
							String oldStr = entAnnot.getAnnotationValue() + " ("+ oldClass + ") Text Offset: "+entPOS.getStart()+ "-"+entPOS.getEnd();
							String newStr = entAnnot.getAnnotationValue() + " ("+ newClass + ") Text Offset: "+entPOS.getStart()+ "-"+entPOS.getEnd();
							String user = InitConfiguration.getDataAccess().getUser().getAuUsername();
							AnnotationLogImpl log = new AnnotationLogImpl(entAnnot.getId(), annnoDocument.getCorpus().getId(), annnoDocument.getProcess().getId(), annnoDocument.getId(),
									AnnotationLogTypeEnum.ENTITYUPDATE, oldStr,newStr, notes,null,user );
							List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
							annotationLogs.add(log);
							InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
							report.addEditChanged(annotationLog);
						}
						else
						{
							report.addNoMatchingAnnotationByClass(annotationLog);
						}
					}
					else
					{
						report.addMissingNoMatchingAnnotation(annotationLog);
					}
				}
				else
				{
					report.addMissingNoMatchingAnnotation(annotationLog);
				}
			}
			else
			{
				report.addMissingAnnotationByEntityNull(annotationLog);
			}
		}
		else
		{
			report.addMissingAnnotationByID(annotationLog);
		}
	}

	private void removeEntityAnnotation(IAnnotatedDocument annnoDocument, IAnnotationLog annotationLog,
			AnnotationPositions annotPositions,
			CorpusProcessAnnotationLogs annotations,
			INERSchemaWithManualCurationReport report) throws ANoteException {
		// Test if annotation log has original entity annotation ID
		if(annotationLog.getOriginalAnnotationID()>0)
		{
			IEntityAnnotation oldEntity = annotations.getEntityAnnotationByID(annotationLog.getOriginalAnnotationID());
			// Test if Entity annotation is different from null
			if(oldEntity!=null)
			{
				AnnotationPosition entPOs = new AnnotationPosition((int)oldEntity.getStartOffset(), (int)oldEntity.getEndOffset());
				// Test if document has entity in same position
				if(annotPositions.containsKey(entPOs))
				{
					IEntityAnnotation entAnnot = (IEntityAnnotation) annotPositions.get(entPOs);
					AnnotationPosition entPOS = new AnnotationPosition((int)entAnnot.getStartOffset(), (int)entAnnot.getEndOffset());
					if(entPOs.equals(entPOS))
					{
						// Test the class of the entity is the same
						if(entAnnot.getClassAnnotation().getId() == oldEntity.getClassAnnotation().getId())
						{
							// Remove annotation from database
							List<Long> annotationsToRemove = new ArrayList<Long>();
							annotationsToRemove.add(entAnnot.getId());
							InitConfiguration.getDataAccess().inactiveAnnotations(annotationsToRemove);
							// Get Class Name
							String termClass = entAnnot.getClassAnnotation().getName();
							String oldStr = entAnnot.getAnnotationValue() + " ("+ termClass + ") Text Offset: "+entPOs.getStart()+ "-"+entPOs.getEnd();
							// Add Log Message
							String notes = "Removed for Curation Process";
							String user = InitConfiguration.getDataAccess().getUser().getAuUsername();
							AnnotationLogImpl log = new AnnotationLogImpl(entAnnot.getId(), annnoDocument.getCorpus().getId(), annnoDocument.getProcess().getId(), annnoDocument.getId(),
									AnnotationLogTypeEnum.ENTITYREMOVE, oldStr,"", notes,null,user );
							List<IAnnotationLog> annotationLogs = new ArrayList<IAnnotationLog>();
							annotationLogs.add(log);
							InitConfiguration.getDataAccess().addProcessDocumentLogs(annotationLogs );
							report.addRemoveChanged(annotationLog);
							annotPositions.removeAnnotation(entPOs);
						}
						else
						{
							report.addNoMatchingAnnotationByClass(annotationLog);
						}
					}
					else
					{
						report.addMissingNoMatchingAnnotation(annotationLog);
					}
				}
				else
				{
					report.addMissingNoMatchingAnnotation(annotationLog);
				}
			}
			else
			{
				report.addMissingAnnotationByEntityNull(annotationLog);
			}
		}
		else
		{
			report.addMissingAnnotationByID(annotationLog);
		}
	}

	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public INERSchema getBasedNERSchema() {
		return nerSchema;
	}



}
