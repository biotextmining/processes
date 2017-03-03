package com.silicolife.textmining.processes.ie.re.kineticre.io;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
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
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.processes.IRESchemaExportReport;
import com.silicolife.textmining.core.interfaces.process.IE.IRESchema;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.Delimiter;
import com.silicolife.textmining.core.interfaces.process.IE.io.export.TextDelimiter;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.processes.ie.re.kineticre.core.REKineticConfigurationClasses;

public class ExportKineticResultsTOCSV {
	
	private static Delimiter mainDelimiter = Delimiter.TAB;
	private static TextDelimiter textDelimiter = TextDelimiter.QUOTATION_MARK;

	
	public ExportKineticResultsTOCSV()
	{
		
	}
	
	public IRESchemaExportReport export(IREKineticREResultsExportConfiguration configuration) throws ANoteException, IOException
	{
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		PrintWriter pw;
		IRESchemaExportReport report = new RESchemaExportReportImpl();
		pw = new PrintWriter(configuration.getExportFile());
		IRESchema reschema = configuration.getRESchema();
		ICorpus corpus = reschema.getCorpus();
		IDocumentSet docs = corpus.getArticlesCorpus();
		Iterator<IPublication> itDocs = docs.iterator();	
		int step = 0;
		int total = docs.getAllDocuments().size();
		writeHeaderLine(pw);
		while(itDocs.hasNext())
		{
			IPublication doc = itDocs.next();
			IAnnotatedDocument docAnnot = new AnnotatedDocumentImpl(doc,reschema,corpus);
			for(IEventAnnotation ev : docAnnot.getEventAnnotations())
			{
				writeline(pw,docAnnot,ev,configuration);
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
	
	protected void writeline(PrintWriter pw,IAnnotatedDocument docAnnot, IEventAnnotation ev,IREKineticREResultsExportConfiguration configuration) throws ANoteException, IOException {
		REKineticConfigurationClasses classConfiguration = configuration.getREKineticConfigurationClasses();
		String[] toWrite = new String[17];
		List<IEntityAnnotation> allEntities = ev.getEntitiesAtLeft();
		allEntities.addAll(ev.getEntitiesAtRight());
		List<IEntityAnnotation> kineticparameters = getEntitiesByClass(allEntities,classConfiguration.getKineticParametersClasses());
		toWrite[0] = textDelimiter.getValue() +(kineticparameters.size()==1 ? toStringEntities(kineticparameters): "Error") + textDelimiter.getValue();
		List<IEntityAnnotation> values = getEntitiesByClass(allEntities,classConfiguration.getValuesClasses());
		toWrite[1] = textDelimiter.getValue() +(values.size()==1 ? toStringEntities(values): "Error") + textDelimiter.getValue();
		List<IEntityAnnotation> units = getEntitiesByClass(allEntities,classConfiguration.getUnitsClasses());
		toWrite[2] = textDelimiter.getValue() +(units.size()==1 ? toStringEntities(units): "Error") + textDelimiter.getValue();
		List<IEntityAnnotation> metabolites = getEntitiesByClass(allEntities,classConfiguration.getMetabolitesClasses());
		toWrite[3] = textDelimiter.getValue() + toStringEntities(metabolites) + textDelimiter.getValue();
		toWrite[4] = textDelimiter.getValue() + toStringEntitiesExternalIds(metabolites) + textDelimiter.getValue();
		List<IEntityAnnotation> enzymes = getEntitiesByClass(allEntities,classConfiguration.getEnzymesClasses());
		toWrite[5] = textDelimiter.getValue() +toStringEntities(enzymes) + textDelimiter.getValue();
		toWrite[6] = textDelimiter.getValue() +toStringEntitiesExternalIds(enzymes) + textDelimiter.getValue();
		List<IEntityAnnotation> organisms = getEntitiesByClass(allEntities,classConfiguration.getOrganismClasses());
		toWrite[7] = textDelimiter.getValue() +toStringEntities(organisms) + textDelimiter.getValue();
		toWrite[8] = textDelimiter.getValue() +toStringEntitiesExternalIds(organisms) + textDelimiter.getValue();
		toWrite[9] = textDelimiter.getValue() +ev.getEventProperties().getGeneralProperties("organism_score_penalty")!=null ? ev.getEventProperties().getGeneralProperties("organism_score_penalty"): "Na" + textDelimiter.getValue() ;
		toWrite[10] = textDelimiter.getValue() +PublicationImpl.getPublicationExternalIDForSource(docAnnot, PublicationSourcesDefaultEnum.PUBMED.toString()) + textDelimiter.getValue();
		toWrite[11] = getSentenceAnnotationIgnoreOrganism(configuration,docAnnot,ev,classConfiguration.getOrganismClasses());
		toWrite[12] = getSentenceAnnotationIgnoreOrganism(configuration,docAnnot,ev,new HashSet<IAnoteClass>());
		toWrite[13] = textDelimiter.getValue() + docAnnot.getId() + textDelimiter.getValue();
		toWrite[14] = textDelimiter.getValue() +(kineticparameters.size()==1 ? kineticparameters.get(0).getResourceElement().getTerm(): "Error") + textDelimiter.getValue();
		toWrite[15] = textDelimiter.getValue() +(kineticparameters.size()==1 ? kineticparameters.get(0).getResourceElement().getId(): "Error") + textDelimiter.getValue();
		toWrite[16] = textDelimiter.getValue() + ev.getId() + textDelimiter.getValue();
		String lineToFile = getLineToWrite(toWrite);
		pw.write(lineToFile);
		pw.println();		
	}
	
	
	private void writeHeaderLine(PrintWriter pw) {
		String[] toWrite = new String[17];
		toWrite[0] = "Kinetic Parameter";
		toWrite[1] = "Value";
		toWrite[2] = "Unit";
		toWrite[3] = "Metabolite(s)";
		toWrite[4] = "Metabolite(s) ExternalIds";
		toWrite[5] = "Enzyme(s)";
		toWrite[6] = "Enzyme(s) External Ids";
		toWrite[7] = "Organism(s)";
		toWrite[8] = "Organism(s) External Ids";
		toWrite[9] = "Organism Score";
		toWrite[10] = "Pubmed";
		toWrite[11] = "Sentence";
		toWrite[12] = "Sentence (Organism)";
		toWrite[13] = "Publication (ID)";
		toWrite[14] = "Kinetic Parameter Normalization";
		toWrite[15] = "Kinetic Parameter ID";
		toWrite[16] = "RelationID (ID)";
		String header = getLineToWrite(toWrite);
		pw.write(header);
		pw.println();
	}
	
	public String toStringEntities(List<IEntityAnnotation> entityAnnotations)
	{
		String result = new String();
		for(IEntityAnnotation entity:entityAnnotations)
		{
			result = result + entity.getAnnotationValue() + "|";
		}
		return result.isEmpty() ? "" :result.substring(0,result.length()-1);
	}
	
	public String toStringEntitiesExternalIds(List<IEntityAnnotation> entityAnnotations) throws ANoteException
	{
		String result = new String();
		for(IEntityAnnotation entity:entityAnnotations)
		{
			IResourceElement resourceElement = entity.getResourceElement();
			if(resourceElement!=null)
			{
				List<IExternalID> externalIds = resourceElement.getExtenalIDs();
				for(IExternalID externalId:externalIds)
				{
					result = result + externalId.getSource().getSource() + ":" +  externalId.getExternalID() + ",";
				}
				if(!result.isEmpty())
					result.substring(0,result.length()-1);
				result = result + "|";

			}
		}
		return result.isEmpty() ? "" :result.substring(0,result.length()-1);
	}
	
	private List<IEntityAnnotation> getEntitiesByClass(List<IEntityAnnotation> allEntities,Set<IAnoteClass> classFilter)
	{
		List<IEntityAnnotation> result = new ArrayList<>();
		for(IEntityAnnotation entity:allEntities)
		{
			if(classFilter.contains(entity.getClassAnnotation()))
			{
				result.add(entity);
			}
		}
		return result;
	}
	
	private String getSentenceAnnotationIgnoreOrganism(IREKineticREResultsExportConfiguration configuration, IAnnotatedDocument docAnnot,IEventAnnotation ev,Set<IAnoteClass> ignoredClasses) throws ANoteException, IOException {
		if(!configuration.isSentencesToExport())
			return new String();
		long startOffset = getStartRelationOffsetIgnoreClasses(ev,ignoredClasses);
		long endOffset = getEndRelationOffsetIgnoreClasses(ev,ignoredClasses);
		return textDelimiter.getValue() + getSentence(docAnnot,startOffset,endOffset) + textDelimiter.getValue() ;
	}

	private long getStartRelationOffsetIgnoreClasses(IEventAnnotation ev,Set<IAnoteClass> ignoredClasses) {
		long startOffsets = ev.getStartOffset();
		for(IEntityAnnotation lentities :ev.getEntitiesAtLeft())
		{
			if(!ignoredClasses.contains(lentities.getClassAnnotation()))
			{
				if(startOffsets > lentities.getStartOffset())
				{
					startOffsets = lentities.getStartOffset();
				}
			}
		}
		return startOffsets;
	}
	
	private long getEndRelationOffsetIgnoreClasses(IEventAnnotation ev,Set<IAnoteClass> ignoredClasses) {
		long endOffsets = ev.getEndOffset();
		for(IEntityAnnotation lentities :ev.getEntitiesAtLeft())
		{
			if(!ignoredClasses.contains(lentities.getClassAnnotation()))
			{
				if(endOffsets < lentities.getEndOffset())
				{
					endOffsets = lentities.getEndOffset();
				}
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
	
	

	
	private String getLineToWrite(String[] toWrite) {
		String line = new String();
		for(String value:toWrite)
		{
			if(value == null)
				line = line + mainDelimiter.getValue();
			else
				line = line + value;
			line = line + mainDelimiter.getValue();
		}
		return line.substring(0, line.length()-mainDelimiter.getValue().length());
	}

}
