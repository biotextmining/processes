package com.silicolife.textmining.processes.ie.re.relationcooccurrence;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.report.processes.REProcessReportImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.structure.ISentence;
import com.silicolife.textmining.core.interfaces.core.report.processes.IREProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.IREProcess;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.configuration.IRECooccurrenceConfiguration;
import com.silicolife.textmining.processes.ie.re.relationcooccurrence.models.IRECooccurrenceSentenceModel;

public class RECooccurrence extends IEProcessImpl implements IREProcess{
	
	private IIEProcess nerProcess;
	private boolean stop = false;
	private IREProcessReport report;
	private IRECooccurrenceSentenceModel model;
	
	public final static String relationCooccurrence = "Rel@tion Cooccurrence";

	public final static IProcessOrigin relationCooccurrenceProcessType = new ProcessOriginImpl(GenerateRandomId.generateID(),relationCooccurrence);
	
	public RECooccurrence(IRECooccurrenceConfiguration configuration)
	{
		super(configuration.getCorpus(),
				relationCooccurrence+" "+Utils.SimpleDataFormat.format(new Date()),
				configuration.getProcessNotes(),
				ProcessTypeImpl.getREProcessType(),
				relationCooccurrenceProcessType,
				gerateProperties(configuration));
		this.model = configuration.getCooccurrenceModel();
		this.nerProcess=configuration.getIEProcess();
	}
	
	
	
	private static Properties gerateProperties(IRECooccurrenceConfiguration configuration) {
		Properties prop = new Properties();
		prop.put(GlobalNames.entityBasedProcess,String.valueOf(configuration.getIEProcess().getID()));
		prop.put(GlobalNames.recooccurrenceModel, configuration.getCooccurrenceModel().getDescription());
		return prop;
	}



	public IREProcessReport executeRE() throws  ANoteException
	{
		InitConfiguration.getDataAccess().createIEProcess(this);
		report  = new REProcessReportImpl(relationCooccurrence,nerProcess,this,false);
		long start = GregorianCalendar.getInstance().getTimeInMillis();
		int size = getCorpus().getArticlesCorpus().getAllDocuments().size();
		long starttime = GregorianCalendar.getInstance().getTimeInMillis();
		int position = 0;
		IDocumentSet docs = getCorpus().getArticlesCorpus();
		Iterator<IPublication> itDocs =docs.iterator();
		while(itDocs.hasNext())
		{
			report.incrementDocument();
			if(stop)
			{
				report.setcancel();
				break;
			}
			IPublication doc = itDocs.next();
			IAnnotatedDocument annotDoc = new AnnotatedDocumentImpl(doc,nerProcess, getCorpus());
			List<IEntityAnnotation> allDoucmentSemanticLayer = annotDoc.getEntitiesAnnotations();
			List<IEventAnnotation> relations = this.model.processDocumetAnnotations(annotDoc,allDoucmentSemanticLayer);
			// Insert Entities and Relations in Database
			insertAnnotationsInDatabse(report,annotDoc,allDoucmentSemanticLayer,relations);
			position++;
			memoryAndProgressAndTime(position, size, starttime);
		}
		long end = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(end-start);
		return report;
	}
	
	private void insertAnnotationsInDatabse(IREProcessReport report,IAnnotatedDocument annotDoc,List<IEntityAnnotation> entitiesList,List<IEventAnnotation> relationsList) throws ANoteException {
		// Generate new Ids for Entities
		for(IEntityAnnotation entity:entitiesList)
		{
			entity.generateNewId();
		}
		
		InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(this, annotDoc, entitiesList);
		report.incrementEntitiesAnnotated(entitiesList.size());
		InitConfiguration.getDataAccess().addProcessDocumentEventAnnoations(this, annotDoc,relationsList);
		report.increaseRelations(relationsList.size());
	}


	public void stop() {
		stop = true;
	}
	
	public static  List<ISentence> getSentencesLimits(IAnnotatedDocument annotDoc) throws ANoteException, IOException {
		return annotDoc.getSentencesText();
	}

	public static  List<IEntityAnnotation> getSentenceEntties(List<IEntityAnnotation> listEntitiesSortedByOffset,ISentence sentence) {
		List<IEntityAnnotation> result = new ArrayList<IEntityAnnotation>();
		for(IEntityAnnotation ent:listEntitiesSortedByOffset)
		{
			if(ent.getStartOffset()>sentence.getStartOffset() && ent.getEndOffset()<sentence.getEndOffset())
			{
				result.add(ent);
			}
		}
		return result;
	}


	
}
