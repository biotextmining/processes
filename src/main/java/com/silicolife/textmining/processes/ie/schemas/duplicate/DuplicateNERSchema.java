package com.silicolife.textmining.processes.ie.schemas.duplicate;

import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.report.processes.INERDuplicationReport;
import com.silicolife.textmining.core.datastructures.report.processes.NERDuplicationReportImpl;
import com.silicolife.textmining.core.datastructures.schemas.NERSchemaImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.INERSchema;

public class DuplicateNERSchema {
	
	private INERSchema nerSchematocopy;
	private boolean stop=false;

	
	public DuplicateNERSchema(INERSchema nerSchematocopy)
	{
		this.nerSchematocopy = nerSchematocopy;
	}

	public INERDuplicationReport duplicateNERSchema() throws ANoteException {
		
		INERSchema nerSchema = createNewNERSchema();
		
		int progressStep = 0;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		IDocumentSet docs = nerSchematocopy.getCorpus().getArticlesCorpus();
		int progressStepSize = docs.getAllDocuments().size();
		Iterator<IPublication> itDocs =docs.iterator();
		while(itDocs.hasNext())
		{
			if(stop)
			{
				break;
			}
			IPublication doc = itDocs.next();
			IAnnotatedDocument annotDOc = new AnnotatedDocumentImpl(doc,nerSchematocopy, nerSchematocopy.getCorpus());
			List<IEntityAnnotation> listEntities = annotDOc.getEntitiesAnnotations();

			for(IEntityAnnotation entity:listEntities)
			{
				entity.generateNewId();
				if(stop)
					break;
			}
			if(stop)
				break;
			InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(nerSchema, annotDOc, listEntities);
			progressStep++;
			memoryAndProgressAndTime(progressStep, progressStepSize, startTime);
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		
		INERDuplicationReport report = new NERDuplicationReportImpl("", nerSchema);
		if(stop)
			report.setcancel();
		report.setTime(endTime-startTime);
		return report;
	}

	private INERSchema createNewNERSchema() throws ANoteException {
		// Add duplicated from ID propriety to new schema
		Properties properties = nerSchematocopy.getProperties();
		properties.put(GlobalNames.duplicatedFrom, String.valueOf(nerSchematocopy.getID()));
		// Creates a new IIESchema
		IIEProcess newNERProcess = new IEProcessImpl(nerSchematocopy.getCorpus(), nerSchematocopy.getName() + " (Duplicated)", nerSchematocopy.getNotes(), nerSchematocopy.getType(), nerSchematocopy.getProcessOrigin(), properties);
		// Saves it in the DB
		InitConfiguration.getDataAccess().createIEProcess(newNERProcess);
		// Converts to NERSchema
		INERSchema nerSchema = new NERSchemaImpl(newNERProcess);
		return nerSchema;
	}

	protected void memoryAndProgressAndTime(int position, int size, long starttime) {
		System.out.println((GlobalOptions.decimalformat.format((double)position/ (double) size * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");		
	}
	
	public void stop() {
		stop = true;		
	}
	

}
