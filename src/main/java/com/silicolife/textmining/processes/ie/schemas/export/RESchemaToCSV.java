package com.silicolife.textmining.processes.ie.schemas.export;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.report.processes.io.export.RESchemaExportReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.processes.IRESchemaExportReport;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.core.interfaces.process.IE.re.IRECSVConfiguration;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class RESchemaToCSV {
	
	public RESchemaToCSV() {
	}
	
	public IRESchemaExportReport exportToCSV(IRECSVConfiguration configuration) throws FileNotFoundException, ANoteException,Exception {
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		PrintWriter pw;
		IRESchemaExportReport report = new RESchemaExportReportImpl();
		pw = new PrintWriter(configuration.getFile());
		IRESchema reschema = configuration.getRESchema();
		ICorpus corpus = reschema.getCorpus();
		IDocumentSet docs = corpus.getArticlesCorpus();
		Iterator<IPublication> itDocs = docs.iterator();	
		Map<Long,String> externalID = new HashMap<>();
		int step = 0;
		int total = docs.getAllDocuments().size();
		writeHeaderLine(configuration,pw);
		while(itDocs.hasNext())
		{
			IPublication doc = itDocs.next();
			IAnnotatedDocument docAnnot = new AnnotatedDocumentImpl(doc,reschema,corpus);
			for(IEventAnnotation ev : docAnnot.getEventAnnotations())
			{
				writeline(configuration,pw,docAnnot,ev,externalID);
				report.incrementeRelationsExported(1);
			}
			memoryAndProgressAndTime(step, total, startTime);
			step++;
		}
		pw.close();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}
	
	protected void memoryAndProgressAndTime(int position, int size, long starttime) {
		System.out.println((GlobalOptions.decimalformat.format((double)position/ (double) size * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");		
	}
	
	private void writeHeaderLine(IRECSVConfiguration configuration,PrintWriter pw) {
		String[] toWrite = new String[10];
		toWrite[configuration.getColumnConfiguration().getAnnotationIDColumn()] = "RelationID";
		toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = "PublicationID/PMID";
		toWrite[configuration.getColumnConfiguration().getElementColumn()] = "Verb (Clue)";
		toWrite[configuration.getColumnConfiguration().getStartOffset()] = "Start Offset";
		toWrite[configuration.getColumnConfiguration().getEndOffset()] = "End Offset";
		toWrite[configuration.getColumnConfiguration().getLeftEntitiesColumn()] = "LeftEntities";
		toWrite[configuration.getColumnConfiguration().getRightEntitiesColumn()] = "RightEntities";
		toWrite[configuration.getColumnConfiguration().getLeftEntitiesExternalIDs()] = "LeftEntities External IDs";
		toWrite[configuration.getColumnConfiguration().getRightEntitiesExternalIDs()] = "RightEntities External IDs";
		toWrite[configuration.getColumnConfiguration().getSentenceColumn()] = "Sentence";
		String header = getLineToWrite(configuration,toWrite);
		pw.write(header);
		pw.println();
	}
	

	private void writeline(IRECSVConfiguration configuration, PrintWriter pw,IAnnotatedDocument docAnnot, IEventAnnotation ev,Map<Long,String> externalID) throws ANoteException, IOException {
		String[] toWrite = new String[10];
		toWrite[configuration.getColumnConfiguration().getAnnotationIDColumn()] = configuration.getTextDelimiter().getValue() + ev.getId() + configuration.getTextDelimiter().getValue();
		String extenalLinks = PublicationImpl.getPublicationExternalIDsStream(docAnnot);
		if(configuration.exportPublicationOtherID() && extenalLinks!=null && !extenalLinks.isEmpty())
		{
			toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = configuration.getTextDelimiter().getValue() + extenalLinks + configuration.getTextDelimiter().getValue();
		}
		else
		{
			toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = configuration.getTextDelimiter().getValue() + "ID:"+docAnnot.getId() + configuration.getTextDelimiter().getValue();
		}
		toWrite[configuration.getColumnConfiguration().getElementColumn()] = configuration.getTextDelimiter().getValue() + ev.getEventClue() + configuration.getTextDelimiter().getValue();
		toWrite[configuration.getColumnConfiguration().getStartOffset()] = configuration.getTextDelimiter().getValue() + ev.getStartOffset() + configuration.getTextDelimiter().getValue();
		toWrite[configuration.getColumnConfiguration().getEndOffset()] = configuration.getTextDelimiter().getValue() + ev.getEndOffset() + configuration.getTextDelimiter().getValue();
		toWrite[configuration.getColumnConfiguration().getLeftEntitiesColumn()] = getEntitiesToString(configuration,ev.getEntitiesAtLeft());
		toWrite[configuration.getColumnConfiguration().getRightEntitiesColumn()] = getEntitiesToString(configuration,ev.getEntitiesAtRight());
		toWrite[configuration.getColumnConfiguration().getSentenceColumn()] = getSentenceAnnotation(configuration,docAnnot,ev);
		toWrite[configuration.getColumnConfiguration().getLeftEntitiesExternalIDs()] = getExternalIDs(configuration,docAnnot,ev.getEntitiesAtLeft(),externalID);
		toWrite[configuration.getColumnConfiguration().getRightEntitiesExternalIDs()] = getExternalIDs(configuration,docAnnot,ev.getEntitiesAtRight(),externalID);
		String lineToFile = getLineToWrite(configuration,toWrite);
		pw.write(lineToFile);
		pw.println();		
	}
	
	
	
	private String getExternalIDs(IRECSVConfiguration configuration,IAnnotatedDocument docAnnot, List<IEntityAnnotation> entities,Map<Long,String> externalID) throws ANoteException {
		if(!configuration.exportResourceExternalID())
		{
			return null;
		}
		String strExternalIds = new String();
		for(IEntityAnnotation entity : entities)
		{
			String entiTyExternalID = getEntityExternalIDs(configuration, entity, externalID);
			if(entiTyExternalID!=null)
			{
				strExternalIds = strExternalIds + configuration.getEntityDelimiter().getValue() + entiTyExternalID;
			}
		}
		if(strExternalIds.length() == 0)
		{
			return null;
		}
		return configuration.getTextDelimiter().getValue() + strExternalIds.substring(1) + configuration.getTextDelimiter().getValue();
	}

	private String getEntityExternalIDs(IRECSVConfiguration configuration, IEntityAnnotation entity,Map<Long,String> externalID) throws ANoteException {
		if(entity.getResourceElement() != null)
		{
			if(!externalID.containsKey(entity.getResourceElement().getId()))
			{
				IResourceElement resourceElement = InitConfiguration.getDataAccess().getResourceElementByID(entity.getResourceElement().getId());
				List<IExternalID> extIDs = resourceElement.getExtenalIDs();
				if(extIDs.size() > 0)
				{
					String strExternalIds = new String();
					for(IExternalID extID: extIDs)
					{
						strExternalIds = strExternalIds + configuration.getEntityExternalIDMainDelimiter().getValue() + extID.getExternalID() + configuration.getEntityExternalIDIntraDelimiter().getValue() + extID.getSource();
					}
					
					externalID.put(entity.getResourceElement().getId(), strExternalIds.substring(1));
				}
				else
				{
					externalID.put(entity.getResourceElement().getId(), null);
				}
			}

				return externalID.get(entity.getResourceElement().getId());	
		}
		else
		{
			return null;
		}
	}

	private String getSentenceAnnotation(IRECSVConfiguration configuration, IAnnotatedDocument docAnnot,IEventAnnotation ev) throws ANoteException, IOException {
		long startOffset = getStartRelationOffset(ev);
		long endOffset = getEndRelationOffset(ev);
		return configuration.getTextDelimiter().getValue() + getSentence(docAnnot,startOffset,endOffset) + configuration.getTextDelimiter().getValue() ;
	}
	
	private long getStartRelationOffset(IEventAnnotation ev) {
		long startOffsets = ev.getStartOffset();
		for(IEntityAnnotation lentities :ev.getEntitiesAtLeft())
		{
			if(startOffsets > lentities.getStartOffset())
			{
				startOffsets = lentities.getStartOffset();
			}
		}
		return startOffsets;
	}
	
	private long getEndRelationOffset(IEventAnnotation ev) {
		long endOffsets = ev.getEndOffset();
		for(IEntityAnnotation lentities :ev.getEntitiesAtLeft())
		{
			if(endOffsets < lentities.getEndOffset())
			{
				endOffsets = lentities.getEndOffset();
			}
		}
		return endOffsets;
	}

	private String getSentence(IAnnotatedDocument annotDOc, long startOffset,long endOffset) throws ANoteException, IOException {
		List<ISentence> sentences = annotDOc.getSentencesText();
		ISentence sentenceInit = findSentence(sentences,(int)startOffset);	
		ISentence sentenceEnd = findSentence(sentences,(int)endOffset);
		int start = (int)sentenceInit.getStartOffset();
		int end = (int)sentenceEnd.getEndOffset();
		return annotDOc.getDocumentAnnotationText().substring(start,end);
	}
	
	private ISentence findSentence(List<ISentence> sentences, int offset) {
		for(ISentence set:sentences)
		{
			if(set.getStartOffset() <= offset && offset <= set.getEndOffset())
			{
				return set;
			}
		}		
		return null;
	}

	private String getEntitiesToString(IRECSVConfiguration configuration,List<IEntityAnnotation> entitiesAtLeft) {
		if(entitiesAtLeft.size()==0)
			return null;
		String result = new String();
		for(IEntityAnnotation ent : entitiesAtLeft)
			result = result + ent.getAnnotationValue() + configuration.getEntityDelimiter().getValue();
		result = result.substring(0, result.length()-configuration.getEntityDelimiter().getValue().length());
		return configuration.getTextDelimiter().getValue() + result + configuration.getTextDelimiter().getValue();
	}

	private String getLineToWrite(IRECSVConfiguration configuration,String[] toWrite) {
		String line = new String();
		for(String value:toWrite)
		{
			if(value == null)
				line = line + configuration.getDefaultDelimiter().getValue();
			else
				line = line + value;
			line = line + configuration.getMainDelimiter().getValue();
		}
		return line.substring(0, line.length()-configuration.getMainDelimiter().getValue().length());
	}


}
