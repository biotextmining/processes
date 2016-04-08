package com.silicolife.textmining.processes.ie.ner.nerlexicalresources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;

import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ElementToNer;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.report.processes.NERProcessReportImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.multithearding.IParallelJob;
import com.silicolife.textmining.core.datastructures.utils.multithearding.ThreadProcessManager;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.INERProcess;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.multithreading.NERParallelStep;


public class NERLexicalResources extends IEProcessImpl implements INERProcess{

	public static String nerlexicalresourcesTagger = "NER Lexical Resources Tagger";

	public static final IProcessOrigin nerlexicalresourcesOrigin= new ProcessOriginImpl(GenerateRandomId.generateID(),nerlexicalresourcesTagger);

	private NER nerDocumentPipeline;
	private boolean stop = false;
	private ThreadProcessManager multi = new ThreadProcessManager(false);
	private NERCaseSensativeEnum caseSensitive;
	private boolean normalization;


	public NERLexicalResources(ElementToNer elementsToNER,boolean normalization,NER nerDocumentPipeline,NERCaseSensativeEnum caseSensitive) {
		super(null,
				NERLexicalResources.nerlexicalresourcesTagger  + " " +Utils.SimpleDataFormat.format(new Date()),
				null,
				ProcessTypeImpl.getNERProcessType(),
				nerlexicalresourcesOrigin,
				nerDocumentPipeline.getProperties(elementsToNER.getResourceToNER(),normalization));
		this.nerDocumentPipeline = nerDocumentPipeline;
		this.caseSensitive = caseSensitive;
		this.normalization = normalization;
	}

	public INERProcessReport executeCorpusNER(ICorpus corpus) throws ANoteException {
		setCorpus(corpus);
		InitConfiguration.getDataAccess().createIEProcess(this);
		NERProcessReportImpl report = new NERProcessReportImpl(nerlexicalresourcesTagger,this);
		stop = false;
		if(!stop)
		{
			processingParallelNER(report,corpus);
		}
		else
		{
			report.setFinishing(false);
		}
		return report;
	}

	private void processingParallelNER(NERProcessReportImpl report, ICorpus corpus) throws ANoteException {
		int size = corpus.getCorpusStatistics().getDocumentNumber();
		long startTime = Calendar.getInstance().getTimeInMillis();
		long actualTime,differTime;
		int i=0;
		Collection<IPublication> docs = corpus.getArticlesCorpus().getAllDocuments().values();
		for(IPublication pub:docs)
		{
			if(!stop)
			{
				List<Long> classIdCaseSensative = new ArrayList<Long>();
				IAnnotatedDocument annotDoc = new AnnotatedDocumentImpl(pub,this, corpus);
				String text = annotDoc.getDocumentAnnotationText();
				if(text==null)
				{
//					Logger logger = Logger.getLogger(Workbench.class.getName());
//					logger.warn("The article whit id: "+pub.getId()+"not contains abstract ");
					System.err.println("The article whit id: "+pub.getId()+"not contains abstract ");
				}
				else
				{
					executeNER(corpus, multi,classIdCaseSensative, annotDoc, text,caseSensitive, normalization);
				}
				report.incrementDocument();
			}
			else
			{
				report.setFinishing(false);
				break;
			}
		}

		startTime = Calendar.getInstance().getTimeInMillis();
		multi.run();
		while(!multi.isComplete() && !stop) {
			actualTime = Calendar.getInstance().getTimeInMillis();
			differTime = actualTime - startTime;
			if(differTime > 10000 * i) {
				memoryAndProgressAndTime(multi.numberOfCompleteJobs(), size, startTime);
				i++;
			};
		}
		if(stop)
		{
			report.setFinishing(false);
		}
		else
		{

			try {
				multi.join();

				List<Object> list = multi.getResults();
				for(Object elem:list)
				{
					if(elem instanceof Integer)
					{
						report.incrementEntitiesAnnotated((Integer) elem);
					}	
				}
				actualTime = Calendar.getInstance().getTimeInMillis();
				report.setTime(actualTime-startTime);
			} catch (InterruptedException e) {
				throw new ANoteException(e);
			}
		}
	}

	private void executeNER(ICorpus corpus,ThreadProcessManager multi,List<Long> classIdCaseSensative,IAnnotatedDocument annotDoc,String text,NERCaseSensativeEnum caseSensitive,boolean normalization) {
		IParallelJob<Integer> job = new NERParallelStep(nerDocumentPipeline,annotDoc, this, corpus, text, classIdCaseSensative,caseSensitive,normalization);
		multi.addJob(job);
	}

	public void stop() {
		stop = true;
		multi.kill();
	}

}
