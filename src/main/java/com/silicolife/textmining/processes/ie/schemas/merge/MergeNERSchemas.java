package com.silicolife.textmining.processes.ie.schemas.merge;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.NERMergeSchemasReportImpl;
import com.silicolife.textmining.core.datastructures.schemas.NERSchemaImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERMergeProcess;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IProcessType;
import com.silicolife.textmining.core.interfaces.process.ProcessTypeEnum;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.INERSchema;

/**
 * Class that contains methods to Merge NER Schemas
 * 
 * @author Hugo Costa
 *
 */
public class MergeNERSchemas {
	
	private ICorpus corpus;
	private IIEProcess baseProcess;
	private boolean stop = false;
	
	public MergeNERSchemas(ICorpus corpus,IIEProcess baseProcess)
	{
		this.corpus = corpus;
		this.baseProcess = baseProcess;
	}

	
	/**
	 * Method that merges a List of {@link IIEProcess}
	 * 
	 * @param nerProcessesToMerge
	 * @return {@link INERMergeProcess}
	 */
	public INERMergeProcess mergeNERProcessesMergeAnnotations(List<IIEProcess> nerProcessesToMerge) throws ANoteException{;
		List<IIEProcess> nerProcessesToMergeValidated = validateNERPprocesses(nerProcessesToMerge);
		if(nerProcessesToMergeValidated.size()==0)
		{
			INERMergeProcess report = new NERMergeSchemasReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.ner.merge.report"),null, baseProcess, nerProcessesToMergeValidated);
			report.setcancel();
			report.setNotes(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.process.merge.err.novalidprocesstomerge"));
			return report;
		}
		else
		{
			Properties properties = getProperties(nerProcessesToMergeValidated);
			String descrition = GlobalNames.mergeNER;
			String notes = mergeBetweenNProcess(nerProcessesToMerge);
			IProcessType processType = new ProcessTypeImpl(GenerateRandomId.generateID(),  ProcessTypeEnum.NER.toString());
			IProcessOrigin processOrigin = new ProcessOriginImpl(GenerateRandomId.generateID(), GlobalNames.mergeNER);
			IIEProcess newProcess = new IEProcessImpl(corpus,descrition ,notes,processType,processOrigin, properties);
			INERSchema newNERProcess = new NERSchemaImpl(newProcess);
			InitConfiguration.getDataAccess().createIEProcess(newNERProcess);
			INERMergeProcess report = new NERMergeSchemasReportImpl(LanguageProperties.getLanguageStream("pt.uminho.anote2.general.ner.merge.report"),newNERProcess, baseProcess, nerProcessesToMergeValidated);
			long startTime = Calendar.getInstance().getTimeInMillis();
			int i=0;
			Collection<IPublication> docs = corpus.getArticlesCorpus().getAllDocuments().values();
			int size = corpus.getCorpusStatistics().getDocumentNumber();
			for(IPublication pub:docs)
			{
				if(!stop)
				{
					List<IEntityAnnotation> entityAnnotations = processDocument(pub,nerProcessesToMergeValidated);
					if(!stop)
						InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(newNERProcess, pub, entityAnnotations);
					memoryAndProgressAndTime(i, size, startTime);
				}
				else
				{
					report.setcancel();
					break;
				}
				i++;
			}
			InitConfiguration.getDataAccess().registerCorpusProcess(corpus, newProcess);
			long endTime = GregorianCalendar.getInstance().getTimeInMillis();
			report.setTime(endTime-startTime);
			return report;
		}

	}
	
	private String mergeBetweenNProcess(List<IIEProcess> nerProcessesToMerge) {
		String result = "Merge between processes :";
		for(IIEProcess process:nerProcessesToMerge)
		{
			result = result +" "+process.getId() + " AND";
		}
		result = result.substring(result.length()-2);
		return result;
	}

	private Properties getProperties(List<IIEProcess> nerProcessesToMergeValidated) {
		Properties prop = new Properties();
		prop.put(GlobalNames.mergeNERSchema, "true");
		prop.put(GlobalNames.NERSchema+1,baseProcess.toString());
		int i=2;
		for(IIEProcess proc : nerProcessesToMergeValidated)
		{
			prop.put(GlobalNames.NERSchema+i,proc.toString());
			i++;
		}
		return prop;
	}

	private List<IEntityAnnotation> processDocument(IPublication doc, List<IIEProcess> nerProcessesToMergeValidated) throws ANoteException {
		AnnotationPositions annot = new AnnotationPositions();
		AnnotatedDocumentImpl annotatedDocument = new AnnotatedDocumentImpl(doc, baseProcess, corpus);
		for(AnnotationPosition position:annotatedDocument.getEntitiesAnnotationsOrderByOffset().getAnnotations().keySet()){
			if(stop)
				return new ArrayList<IEntityAnnotation>();
			IAnnotation entityAnnotaion = annotatedDocument.getEntitiesAnnotationsOrderByOffset().getAnnotations().get(position);
			entityAnnotaion.generateNewId();
			annot.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(position, entityAnnotaion);
		}
		for(IIEProcess process:nerProcessesToMergeValidated)
		{
			if(stop)
				return new ArrayList<IEntityAnnotation>();
			annotatedDocument = new AnnotatedDocumentImpl(doc,process, corpus);
			for(AnnotationPosition pos:annotatedDocument.getEntitiesAnnotationsOrderByOffset().getAnnotations().keySet())
			{
				if(stop)
					return new ArrayList<IEntityAnnotation>();
				IAnnotation entityAnnotaion = annotatedDocument.getEntitiesAnnotationsOrderByOffset().getAnnotations().get(pos);
				entityAnnotaion.generateNewId();
				annot.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(pos, entityAnnotaion);
			}
		}
		List<IEntityAnnotation> result = new ArrayList<IEntityAnnotation>();
		for(IAnnotation annotation:annot.getAnnotations().values())
		{
			if(annotation instanceof IEntityAnnotation)
			{
				result.add((IEntityAnnotation) annotation);
			}
		}
		return result;
	}

	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	private List<IIEProcess> validateNERPprocesses(List<IIEProcess> nerProcessesToMerge) {
		List<IIEProcess> nerProcessesOk = new ArrayList<>();
		for(IIEProcess process:nerProcessesToMerge)
		{

			if(valideProcess(process))
			{
				nerProcessesOk.add(process);
			}
		}
		return nerProcessesOk;
	}
	
	private boolean valideProcess(IIEProcess process) {

		if(!testSameCorpus(process))
		{
			return false;
		}
		return true;
	}
	
	
	private boolean testSameCorpus(IIEProcess process) {
		long corpusID = this.corpus.getId();
		long processCorpusID = process.getCorpus().getId();
		if(corpusID==processCorpusID)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public void stop()
	{
		stop = true;
	}

}
