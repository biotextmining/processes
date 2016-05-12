package com.silicolife.textmining.processes.ie.schemas.export;

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
import com.silicolife.textmining.core.datastructures.report.processes.io.export.NERSchemaExportReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERSchemaExportReport;
import com.silicolife.textmining.core.interfaces.process.IE.INERSchema;
import com.silicolife.textmining.core.interfaces.process.IE.ner.export.INERCSVExporterConfiguration;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class NERSchemaToCSV {
	
	
	public NERSchemaToCSV()
	{
		
	}
	
	public INERSchemaExportReport exportToCSV(INERCSVExporterConfiguration configuration) throws ANoteException, IOException 
	{ 
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		INERSchemaExportReport report = new NERSchemaExportReportImpl();
		PrintWriter pw = new PrintWriter(configuration.getFile());
		writeHeaderLine(configuration,pw);
		
		Map<Long,String> externalIdsSTr = new HashMap<>();
		Map<Long,String> resourceIdsSTr = new HashMap<>();
		INERSchema nerSchema = configuration.getNERSchema();
		IDocumentSet docs = nerSchema.getCorpus().getArticlesCorpus();
		Iterator<IPublication> itDocs = docs.iterator();
		int progressionStep = 0;
		int progressionTotalSteps = docs.getAllDocuments().size();
		
		while(itDocs.hasNext())
		{	
			IPublication doc = itDocs.next();
			IAnnotatedDocument docAnnot = new AnnotatedDocumentImpl(doc, nerSchema, nerSchema.getCorpus());
			for(IEntityAnnotation ent : docAnnot.getEntitiesAnnotations())
			{
				writeline(configuration,pw,docAnnot,ent,externalIdsSTr,resourceIdsSTr);
				report.incremetExportedEntity(1);
			}
			memoryAndProgressAndTime(progressionStep, progressionTotalSteps, startTime);
			progressionStep++;
		}
		pw.close();
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startTime);
		return report;
	}
	
	private void writeHeaderLine(INERCSVExporterConfiguration configuration,PrintWriter pw) {
		String[] toWrite = new String[10];
		toWrite[configuration.getColumnConfiguration().getAnnotationIDColumn()] = "AnnotationID";
		toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = "PublicationID/PMID";
		toWrite[configuration.getColumnConfiguration().getElementColumn()] = "Term";
		toWrite[configuration.getColumnConfiguration().getClassColumn()] = "Class";
		toWrite[configuration.getColumnConfiguration().getStartOffset()] = "StartOffset";
		toWrite[configuration.getColumnConfiguration().getEndOffset()] = "EndOffset";
		toWrite[configuration.getColumnConfiguration().getResourceIDColumn()] = "ResourceID";
		toWrite[configuration.getColumnConfiguration().getResourceInformation()] = "Resource Name";
		toWrite[configuration.getColumnConfiguration().getResourceExternalIDs()] = "External IDs";
		toWrite[configuration.getColumnConfiguration().getSentenceColumn()] = "Sentence";
		String header = getLineToWrite(configuration,toWrite);
		pw.write(header);
		pw.println();
	}
	
	private void writeline(INERCSVExporterConfiguration configuration, PrintWriter pw,IAnnotatedDocument docID, IEntityAnnotation entAnnot, Map<Long, String> externalIdsSTr, Map<Long, String> resourceIdsSTr) throws ANoteException, IOException {
	String[] toWrite = new String[10];
	toWrite[configuration.getColumnConfiguration().getAnnotationIDColumn()] = configuration.getTextDelimiter().getValue() + entAnnot.getId() + configuration.getTextDelimiter().getValue();
	String extenalLinks = PublicationImpl.getPublicationExternalIDsStream(docID);
	if(configuration.exportPublicationOtherID() && extenalLinks!=null && !extenalLinks.isEmpty())
	{
		toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = configuration.getTextDelimiter().getValue() + extenalLinks + configuration.getTextDelimiter().getValue();
	}
	else
	{
		toWrite[configuration.getColumnConfiguration().getPublicationIDColumn()] = configuration.getTextDelimiter().getValue() + "ID:"+docID.getId() + configuration.getTextDelimiter().getValue();
	}
	toWrite[configuration.getColumnConfiguration().getElementColumn()] = configuration.getTextDelimiter().getValue() + entAnnot.getAnnotationValue() + configuration.getTextDelimiter().getValue();
	toWrite[configuration.getColumnConfiguration().getClassColumn()] = configuration.getTextDelimiter().getValue() + entAnnot.getClassAnnotation().getName() + configuration.getTextDelimiter().getValue();
	toWrite[configuration.getColumnConfiguration().getStartOffset()] = configuration.getTextDelimiter().getValue() + entAnnot.getStartOffset() + configuration.getTextDelimiter().getValue();
	toWrite[configuration.getColumnConfiguration().getEndOffset()] = configuration.getTextDelimiter().getValue() + entAnnot.getEndOffset() + configuration.getTextDelimiter().getValue();
	if(entAnnot.getResourceElement() != null)
	{
		toWrite[configuration.getColumnConfiguration().getResourceIDColumn()] = configuration.getTextDelimiter().getValue() + entAnnot.getResourceElement() + configuration.getTextDelimiter().getValue();
		if(configuration.exportResourceInformation())
		{
			if(!resourceIdsSTr.containsKey(entAnnot.getResourceElement()))
			{

				resourceIdsSTr.put(entAnnot.getResourceElement().getId(), entAnnot.getResourceElement().toString());
			}
			toWrite[configuration.getColumnConfiguration().getResourceInformation()] = resourceIdsSTr.get(entAnnot.getResourceElement());
		}
		else
		{
			toWrite[configuration.getColumnConfiguration().getResourceInformation()] = null;
		}
		if(configuration.exportResourceExternalID())
		{
			if(!externalIdsSTr.containsKey(entAnnot.getResourceElement().getId()))
			{
				String strExternalIds = null;
				IResourceElement resourceElement = InitConfiguration.getDataAccess().getResourceElementByID(entAnnot.getResourceElement().getId());
				List<IExternalID> extIDs = resourceElement.getExtenalIDs();
				if(extIDs.size() > 0)
				{
					strExternalIds = new String();
					for(IExternalID extID: extIDs)
					{
						strExternalIds = strExternalIds + configuration.getExternalIDDelimiter().getValue() + extID.getExternalID() + configuration.getIntraExtenalIDdelimiter().getValue() + extID.getSource();
					}
					strExternalIds = strExternalIds.substring(1);
					strExternalIds = configuration.getTextDelimiter().getValue() + strExternalIds  + configuration.getTextDelimiter().getValue();
				}
				externalIdsSTr.put(entAnnot.getResourceElement().getId(), strExternalIds);
			}
		}
		toWrite[configuration.getColumnConfiguration().getResourceExternalIDs()] = externalIdsSTr.get(entAnnot.getResourceElement());
	}
	else
	{
		toWrite[configuration.getColumnConfiguration().getResourceIDColumn()] = null;
		toWrite[configuration.getColumnConfiguration().getResourceInformation()] = null;
		toWrite[configuration.getColumnConfiguration().getResourceExternalIDs()] = null;
	}
	toWrite[configuration.getColumnConfiguration().getSentenceColumn()] = getSentenceAnnotation(configuration, docID, entAnnot);
	String lineToFile = getLineToWrite(configuration,toWrite);
	pw.write(lineToFile);
	pw.println();
}
	
	private String getSentenceAnnotation(INERCSVExporterConfiguration configuration, IAnnotatedDocument docAnnot,IEntityAnnotation entityAnnot) throws ANoteException, IOException {
		return configuration.getTextDelimiter().getValue() + getSentence(docAnnot,entityAnnot.getStartOffset(),entityAnnot.getEndOffset()) + configuration.getTextDelimiter().getValue() ;
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
	

	private String getLineToWrite(INERCSVExporterConfiguration configuration,String[] toWrite) {
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
	
	protected void memoryAndProgressAndTime(int position, int size, long starttime) {
		System.out.println((GlobalOptions.decimalformat.format((double)position/ (double) size * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");		
	}
	
}
