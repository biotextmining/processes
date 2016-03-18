package com.silicolife.textmining.processes.ie.ner.linnaeus;

import java.util.ArrayList;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessOriginImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ElementToNer;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.datastructures.report.processes.NERProcessReportImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.EntitiesDesnormalization;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.textprocessing.TermSeparator;
import com.silicolife.textmining.core.datastructures.utils.GenerateRandomId;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.core.report.processes.INERProcessReport;
import com.silicolife.textmining.core.interfaces.process.IProcessOrigin;
import com.silicolife.textmining.core.interfaces.process.IE.INERProcess;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.martin.common.compthreads.IteratorBasedMaster;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.dataholders.Document;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.documentparser.input.DocumentIterator;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.Mention;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.doc.TaggedDocument;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers.ConcurrentMatcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers.MatchPostProcessor;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers.UnionMatcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.matchers.VariantDictionaryMatcher;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;

public class LinnaeusTagger extends IEProcessImpl implements INERProcess{
	
	public static final String linneausTagger = "Linnaeus Tagger";
	public static final String abreviation = "Abbreviation";
	public static final String disambiguation = "Disambiguation";
	
	public static final IProcessOrigin linnausOrigin= new ProcessOriginImpl(GenerateRandomId.generateID(),linneausTagger);
	
	private INERLinnaeusConfiguration configurations;
	private boolean stop = false;
	
	public LinnaeusTagger(INERLinnaeusConfiguration configurations) {
		super(configurations.getCorpus(),
				LinnaeusTagger.linneausTagger + " " +Utils.SimpleDataFormat.format(new Date()),
				configurations.getNotes(),
				ProcessTypeImpl.getNERProcessType(),
				linnausOrigin,
				gereateProperties(configurations)
				);
		this.configurations = configurations;
	}

	private static Properties gereateProperties(INERLinnaeusConfiguration configurations) {
		Properties properties = transformResourcesToOrderMapInProperties(configurations.getResourceToNER());
		if(configurations.isUseAbreviation()){
			properties.put(LinnaeusTagger.abreviation, "true");
		} else {
			properties.put(LinnaeusTagger.abreviation, "false");
		}
		properties.put(LinnaeusTagger.disambiguation, configurations.getDisambiguation().name());
		if(configurations.isCaseSensitive()){
			properties.put(GlobalNames.casesensitive, "true");
		} else {
			properties.put(GlobalNames.casesensitive, "false");
		}
		if(configurations.isNormalized()){
			properties.put(GlobalNames.normalization, "true");
		} else {
			properties.put(GlobalNames.normalization, "false");
		}
		if(configurations.getStopWords()!=null && configurations.getStopWords().getId() > 0)
		{
			properties.put(GlobalNames.nerpreProcessing,GlobalNames.stopWords);
			properties.put(GlobalNames.stopWordsResourceID,String.valueOf(configurations.getStopWords().getId()));
		}
		else
		{
			properties.put(GlobalNames.nerpreProcessing,GlobalNames.nerpreProcessingNo);
		}
		if(configurations.usingOtherResourceInfoToImproveRuleAnnotations())
		{
			properties.put(GlobalNames.useOtherResourceInformationInRules,"true");
		}
		return properties;
	}

	@Override
	public INERProcessReport executeCorpusNER(ICorpus corpus) throws ANoteException 
	{	
		setCorpus(corpus);
		InitConfiguration.getDataAccess().createIEProcess(this);
		long startime = GregorianCalendar.getInstance().getTimeInMillis();
		ElementToNer elementsToNER = new ElementToNer(configurations.getResourceToNER(), configurations.isNormalized());
		HandRules rules = new HandRules(elementsToNER);
		List<IEntityAnnotation> elements = elementsToNER.getTermsByAlphabeticOrder(!configurations.isCaseSensitive());
		Map<Long, Long> resourceMapClass = elementsToNER.getResourceMapClass();
		Map<Long, IResourceElement> resourceIDMapResource = elementsToNER.getMapResourceIDsToResourceElements();
		Map<String, Set<Long>> maplowerCaseToPossibleResourceIDs = elementsToNER.getMaplowerCaseToPossibleResourceIDs();
		Map<Long, String> mapPossibleResourceIDsToTermString = elementsToNER.getMapPossibleResourceIDsToTermString();
		Matcher matcher = getMatcher(elements);
		INERProcessReport report = new NERProcessReportImpl(LinnaeusTagger.linneausTagger + " report", this);
		DocumentIterator documents = new PublicationIt(corpus,this);
		ConcurrentMatcher tm = new ConcurrentMatcher(matcher,documents);
		IteratorBasedMaster<TaggedDocument> master = new IteratorBasedMaster<TaggedDocument>(tm,configurations.getNumberOfThreads());
		Thread threadmaster = new Thread(master);
		threadmaster.start();
		Set<String> stopwords = new HashSet<String>();
		if(this.configurations.getStopWords()!=null)
		{
			ILexicalWords st = this.configurations.getStopWords();
			Set<String> stopwordsTmp = st.getLexicalWords().keySet();
			if(this.configurations.isCaseSensitive())
			{
				stopwords = stopwordsTmp;
			}
			else
			{
				for(String word:stopwordsTmp)
				{
					stopwords.add(word.toLowerCase());
				}
			}
				
		}
		int counter = 0; 
		while (master.hasNext() && !stop){
			TaggedDocument td = master.next();
			report.incrementDocument();
			if (td != null && !stop)
			{
				String strid = td.getOriginal().getID();
				Long id = Long.valueOf(strid);
				ArrayList<Mention> matches = td.getAllMatches();
				AnnotationPositions positions = new AnnotationPositions();
				for(Mention men:matches)
				{
					String text = men.getText();
					if(stopwords.isEmpty() || 
							this.configurations.isCaseSensitive() && !stopwords.contains(text) 
							|| !this.configurations.isCaseSensitive() && !stopwords.contains(text.toLowerCase()))
					{
						if(!configurations.isCaseSensitive()){
							Set<Long> resourceIDs = maplowerCaseToPossibleResourceIDs.get(text.toLowerCase());
							if(resourceIDs == null){
								resourceIDs =maplowerCaseToPossibleResourceIDs.get(men.getIds()[1].toLowerCase());
							}
							for(Long resourceID : resourceIDs){
								Long classID = resourceMapClass.get(resourceID);
								String dictTerm = mapPossibleResourceIDsToTermString.get(resourceID);
								IAnoteClass klass = ClassPropertiesManagement.getClassGivenClassID(classID);
								IEntityAnnotation entityAnnotation = new EntityAnnotationImpl(men.getStart(), men.getEnd(), klass , resourceIDMapResource.get(resourceID), text, NormalizationForm.getNormalizationForm(text), new Properties());
								positions.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(new AnnotationPosition(men.getStart(), men.getEnd(), dictTerm, text), entityAnnotation);
							}
						}else{
							long dicEntityID = Long.valueOf(men.getIds()[0]);
							Long classID = resourceMapClass.get(dicEntityID);
							String dictTerm = men.getIds()[1];
							IAnoteClass klass = ClassPropertiesManagement.getClassGivenClassID(classID);
							IEntityAnnotation entityAnnotation = new EntityAnnotationImpl(men.getStart(), men.getEnd(), klass , resourceIDMapResource.get(dicEntityID), text, NormalizationForm.getNormalizationForm(text), new Properties());
							positions.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(new AnnotationPosition(men.getStart(), men.getEnd(), dictTerm, text), entityAnnotation);
						}
					}
				}
				if(!stop && elementsToNER.getRules()!=null && !elementsToNER.getRules().isEmpty())
				{
					if(rules != null && !stop)
						rules.applyRules(td.getOriginal().getBody(), positions);	
				}
				if(!stop)
				{
					report.incrementEntitiesAnnotated(positions.getAnnotations().size());
					List<IEntityAnnotation> entityAnnotations = positions.getEntitiesFromAnnoattionPositions();
					List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<IPublicationExternalSourceLink>();
					List<IPublicationField> publicationFields = new ArrayList<>();
					List<IPublicationLabel> publicationLabels = new ArrayList<>();
					IPublication document =  new PublicationImpl(id,
							"", "", "", "", "",
							"", "", "", "", "", "",
							"", false, "", "",
							publicationExternalIDSource ,
							publicationFields ,
							publicationLabels );
					
					if(configurations.isNormalized()){
						Document linnausDocument = td.getOriginal();
						EntitiesDesnormalization desnormalizer = new EntitiesDesnormalization(linnausDocument.getRawContent(), linnausDocument.getBody(), entityAnnotations);
						entityAnnotations = desnormalizer.getDesnormalizedAnnotations();
					}
					
					InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(this, document, entityAnnotations);
				}
			}
			counter++;
			memoryAndProgress(counter,corpus.getArticlesCorpus().size(),startime);		
		}
		try {
			threadmaster.join();
		} catch (InterruptedException e) {
			throw new ANoteException(e);
		}
		if(stop)
		{
			report.setcancel();
		}
		long endTime = GregorianCalendar.getInstance().getTimeInMillis();
		report.setTime(endTime-startime);
		return report;
	}
	


	/**
	 * returns a entity recognition Matcher based on the input parameters in ap (provided by the user on the command-line or in a configuration file)
	 * @param elements 
	 * @param ap an ArgParser object containing arguments used to construct the matcher
	 * @param logger A logger to which information messages will be logged. Nothing will be logged if this is null.
	 * @param tag Allows the use of multiple different matchers. For example, if tag == "Genes", properties will need to have the postfix "Genes" to be read. This allows the method to be called multiple times with different tags for different matching options.
	 * @return a matcher that can be used to find and normalize species names in text
	 * @throws DatabaseLoadDriverException 
	 * @throws SQLException 
	 */
	private Matcher getMatcher(List<IEntityAnnotation> elements) throws ANoteException{

		List<Matcher> matchers = new ArrayList<Matcher>();
		String[][] termToIdsMapArray;
		String[] terms;
		int i=0;
		termToIdsMapArray = new String[elements.size()][2] ;
		terms = new String[elements.size()];
		for(IEntityAnnotation elem : elements)
		{			
			termToIdsMapArray[i][0] = String.valueOf(elem.getResourceElement().getId());
			termToIdsMapArray[i][1] = elem.getAnnotationValue();
			if(this.configurations.isNormalized())
			{
				terms[i] = TermSeparator.termSeparator(elem.getAnnotationValue()).trim();
			}
			else
			{
				terms[i] = elem.getAnnotationValue();
			}
			if(!this.configurations.isCaseSensitive())
				terms[i] = terms[i].toLowerCase();
			i++;
		}
		matchers.add(new VariantDictionaryMatcher(termToIdsMapArray, terms, !this.configurations.isCaseSensitive()));
		if (matchers.size() == 0){
			return null;
		}
		Matcher matcher = matchers.size() == 1 ? matchers.get(0) : new UnionMatcher(matchers, true);
		matcher = new MatchPostProcessor(matcher, this.configurations.getDisambiguation(), this.configurations.isUseAbreviation(),null);
		matcher.match("test", new Document("none",null,null,null,null,null,null,null,null,null,null,null,null,null,null));
		return matcher;
	}
	
	protected void memoryAndProgress(int step, int total,long startime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");		
	}

	

	public static TaggedDocument matchDocument(Matcher matcher, Document doc){
		String rawText = doc.toString();

		List<Mention> matches = matcher.match(rawText, doc);

		if (matches == null)
			return new TaggedDocument(doc,null,null,matches,rawText);

		for (Mention m : matches){
			m.setDocid(doc.getID());
		}

		if (doc.isIgnoreCoordinates()){
			for (int i = 0; i < matches.size(); i++){
				Mention m = matches.get(i);
				m.setStart(-1);
				m.setEnd(-1);
			}
		}

		return new TaggedDocument(doc,null,null,matches,rawText);
	}

	@Override
	public void stop() {
		this.stop=true;
	}

}
