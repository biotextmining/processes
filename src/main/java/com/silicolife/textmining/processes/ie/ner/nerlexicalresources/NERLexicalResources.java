package com.silicolife.textmining.processes.ie.ner.nerlexicalresources;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.report.processes.NERProcessReportImpl;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.datastructures.utils.multithearding.IParallelJob;
import com.silicolife.textmining.core.datastructures.utils.multithearding.ThreadProcessManager;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.process.IE.INERProcess;
import com.silicolife.textmining.core.interfaces.process.IE.ner.INERConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesPreProcessingModel;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.NERLexicalResourcesPreProssecingEnum;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.multithreading.NERParallelStep;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel.NERPreprocessingFactory;


public class NERLexicalResources implements INERProcess{

	public static String nerlexicalresourcesTagger = "NER Lexical Resources Tagger";
	public static final IProcessOrigin nerlexicalresourcesOrigin= new ProcessOriginImpl(GenerateRandomId.generateID(),nerlexicalresourcesTagger);
	private boolean stop = false;
	private ThreadProcessManager multi;


	public NERLexicalResources() {
		
	}

	public INERProcessReport executeCorpusNER(INERConfiguration configuration) throws ANoteException, InvalidConfigurationException {
		validateConfiguration(configuration);
		INERLexicalResourcesConfiguration lexicalResurcesConfiguration = (INERLexicalResourcesConfiguration) configuration;
		NERLexicalResourcesPreProssecingEnum preprocessing = lexicalResurcesConfiguration.getPreProcessingOption();
		INERLexicalResourcesPreProcessingModel model = NERPreprocessingFactory.build(lexicalResurcesConfiguration,preprocessing);
		IIEProcess processToRun = getIEProcess(lexicalResurcesConfiguration,model);
		multi = new ThreadProcessManager(false);
		InitConfiguration.getDataAccess().createIEProcess(processToRun);
		InitConfiguration.getDataAccess().registerCorpusProcess(configuration.getCorpus(), processToRun);
		NERProcessReportImpl report = new NERProcessReportImpl(nerlexicalresourcesTagger,processToRun);
		stop = false;
		if(!stop)
		{
			processingParallelNER(report,lexicalResurcesConfiguration,processToRun,model);
		}
		else
		{
			report.setFinishing(false);
		}
		return report;
	}

	private IIEProcess getIEProcess(INERLexicalResourcesConfiguration lexicalResurcesConfiguration,INERLexicalResourcesPreProcessingModel model) {

		String description = NERLexicalResources.nerlexicalresourcesTagger  + " " +Utils.SimpleDataFormat.format(new Date());
		Properties properties = model.getProperties(lexicalResurcesConfiguration);	
		IIEProcess processToRun = lexicalResurcesConfiguration.getIEProcess();
		processToRun.setName(description);
		processToRun.setProperties(properties);
		return processToRun;
	}

	private void processingParallelNER(NERProcessReportImpl report,INERLexicalResourcesConfiguration configuration,IIEProcess process,INERLexicalResourcesPreProcessingModel model) throws ANoteException {
		int size = process.getCorpus().getCorpusStatistics().getDocumentNumber();
		long startTime = Calendar.getInstance().getTimeInMillis();
		long actualTime,differTime;
		int i=0;
		Collection<IPublication> docs = process.getCorpus().getArticlesCorpus().getAllDocuments().values();
		for(IPublication pub:docs)
		{
			if(!stop)
			{
				List<Long> classIdCaseSensative = new ArrayList<Long>();
				IAnnotatedDocument annotDoc = new AnnotatedDocumentImpl(pub,process, process.getCorpus());
				String text = annotDoc.getDocumentAnnotationText();
				if(text==null)
				{
//					Logger logger = Logger.getLogger(Workbench.class.getName());
//					logger.warn("The article whit id: "+pub.getId()+"not contains abstract ");
					System.err.println("The article whit id: "+pub.getId()+"not contains abstract ");
				}
				else
				{
					executeNER(process.getCorpus(), multi,classIdCaseSensative, annotDoc, text,configuration,model,process);
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

	@JsonIgnore
	protected void memoryAndProgress(int step, int total) {
		System.out.println((GlobalOptions.decimalformat.format((double) step / (double) total * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB ");
	}

	@JsonIgnore
	protected void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double) step / (double) total * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / (1024 * 1024) + " MB ");
	}

	private void executeNER(ICorpus corpus,ThreadProcessManager multi,List<Long> classIdCaseSensative,IAnnotatedDocument annotDoc,String text,INERLexicalResourcesConfiguration confguration,INERLexicalResourcesPreProcessingModel nerpreprocessingmodel,IIEProcess process) {
		IParallelJob<Integer> job = new NERParallelStep(nerpreprocessingmodel,annotDoc, process, corpus, text, classIdCaseSensative,confguration.getCaseSensitive(),confguration.isNormalized());
		multi.addJob(job);
	}

	public void stop() {
		stop = true;
		multi.kill();
	}

	@Override
	public void validateConfiguration(INERConfiguration configuration)throws InvalidConfigurationException {
		if(configuration instanceof INERLexicalResourcesConfiguration)
		{
			INERLexicalResourcesConfiguration lexicalResurcesConfiguration = (INERLexicalResourcesConfiguration) configuration;
			if(lexicalResurcesConfiguration.getCorpus()==null)
			{
				throw new InvalidConfigurationException("Corpus can not be null");
			}
		}
		else
			throw new InvalidConfigurationException("configuration must be INERLexicalResourcesConfiguration isntance");
		
	}
	
	

}
