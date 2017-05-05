package com.silicolife.textmining.processes.ie.ner.linnaeus.configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Pattern;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessRunStatusConfigurationEnum;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ResourceSelectedClassesMap;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.resources.lexiacalwords.LexicalWordsImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.linnaeus.LinnaeusTagger;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher.Disambiguation;

public class NERLinnaeusConfigurationImpl extends NERConfigurationImpl implements INERLinnaeusConfiguration{
	
	public static final String nerLinnaeusUID = "ner.linnaeus";

	private Map<String, Pattern> patterns;
	private ResourcesToNerAnote resourceToNER;

	private int numberOfThreads;
	private boolean useAbreviation;
	private Disambiguation disambiguation;
	private NERCaseSensativeEnum caseSensitiveEnum;
	private boolean normalized;
	private ILexicalWords stopWords;
	private NERLinnaeusPreProcessingEnum preProcessing;
	private boolean usingOtherResourceInfoToImproveRuleAnnotations;
	private int sizeOfSmallWordsToBeNotAnnotated;
	
	public NERLinnaeusConfigurationImpl()
	{
		super();
	}
	
	public NERLinnaeusConfigurationImpl(IIEProcess process,ProcessRunStatusConfigurationEnum processRunStatus)
	{
		super(process.getCorpus(),LinnaeusTagger.linneausTagger,process,processRunStatus);
	}
	
	public NERLinnaeusConfigurationImpl(ICorpus corpus,ProcessRunStatusConfigurationEnum processRunStatus,Map<String, Pattern> patterns, ResourcesToNerAnote resourceToNER, boolean useabreviation,
			Disambiguation disambiguation, NERCaseSensativeEnum caseSensitiveEnum,boolean normalized, int numThreads,ILexicalWords stopwords,
			NERLinnaeusPreProcessingEnum preprocessing,boolean usingOtherResourceInfoToImproveRuleAnnotations, int sizeOfSmallWordsToBeNotAnnotated) {
		super(corpus,LinnaeusTagger.linneausTagger, build(corpus),processRunStatus);
		this.patterns = patterns;
		this.resourceToNER = resourceToNER;
		this.useAbreviation = useabreviation;
		this.disambiguation = disambiguation;
		this.caseSensitiveEnum = caseSensitiveEnum;
		this.normalized = normalized;
		this.numberOfThreads = numThreads;
		this.stopWords = stopwords;
		this.usingOtherResourceInfoToImproveRuleAnnotations = usingOtherResourceInfoToImproveRuleAnnotations;
		this.preProcessing = preprocessing;
		this.sizeOfSmallWordsToBeNotAnnotated =sizeOfSmallWordsToBeNotAnnotated;
	}
	
	private static IIEProcess build(ICorpus corpus)
	{
		String description = LinnaeusTagger.linneausTagger  + " " +Utils.SimpleDataFormat.format(new Date());
		String notes = new String();
		Properties properties = new Properties();
		IIEProcess processToRun = new IEProcessImpl(corpus, description, notes , ProcessTypeImpl.getNERProcessType(), LinnaeusTagger.linnausOrigin, properties );
		return processToRun;
	}

	public Map<String, Pattern> getPatterns() {
		return patterns;
	}
	
	public void setPatterns(Map<String, Pattern> patterns) {
		this.patterns = patterns;
	}


	public ResourcesToNerAnote getResourceToNER() {
		return resourceToNER;
	}
	
	public void setResourceToNER(ResourcesToNerAnote resourceToNER) {
		this.resourceToNER = resourceToNER;
	}
	
	public int getNumberOfThreads() {
		return numberOfThreads;
	}

	public void setNumberOfThreads(int numberOfThreads) {
		this.numberOfThreads = numberOfThreads;
	}

	public boolean isUseAbreviation() {
		return useAbreviation;
	}

	public void setUseAbreviation(boolean useAbreviation) {
		this.useAbreviation = useAbreviation;
	}

	public NERCaseSensativeEnum getCaseSensitiveEnum() {
		return caseSensitiveEnum;
	}
	
	
	public void setCaseSensitiveEnum(NERCaseSensativeEnum caseSensitiveEnum) {
		this.caseSensitiveEnum = caseSensitiveEnum;
	}

	public boolean isNormalized() {
		return normalized;
	}

	public void setNormalized(boolean newNormalizedOption) {
		normalized = newNormalizedOption;
	}
	
	public Disambiguation getDisambiguation() {
		return disambiguation;
	}
	
	public void setDisambiguation(Disambiguation disambiguation) {
		this.disambiguation = disambiguation;
	}

	@JsonDeserialize(as=LexicalWordsImpl.class)
	public ILexicalWords getStopWords() {
		return stopWords;
	}

	public void setStopWords(ILexicalWords stopWords) {
		this.stopWords = stopWords;
	}

	public boolean isUsingOtherResourceInfoToImproveRuleAnnotations() {
		return usingOtherResourceInfoToImproveRuleAnnotations;
	}
	
	public void setUsingOtherResourceInfoToImproveRuleAnnotations(
			boolean usingOtherResourceInfoToImproveRuleAnnotations) {
		this.usingOtherResourceInfoToImproveRuleAnnotations = usingOtherResourceInfoToImproveRuleAnnotations;
	}

	public NERLinnaeusPreProcessingEnum getPreProcessing() {
		return preProcessing;
	}

	public void setPreProcessing(NERLinnaeusPreProcessingEnum preProcessing) {
		this.preProcessing = preProcessing;
	}

	public int getSizeOfSmallWordsToBeNotAnnotated() {
		return sizeOfSmallWordsToBeNotAnnotated;
	}

	public void setSizeOfSmallWordsToBeNotAnnotated(int sizeOfSmallWordsToBeNotAnnotated) {
		this.sizeOfSmallWordsToBeNotAnnotated = sizeOfSmallWordsToBeNotAnnotated;
	}

	@JsonIgnore
	public Map<String, String> getNERProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(NERLinnaeusTaggerDefaultSettings.USE_PARTIAL_MATCH_WITH_DICTIONARIES, String.valueOf(usingOtherResourceInfoToImproveRuleAnnotations));
		long ruleResourceID = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.RULES_RESOURCE_ID, String.valueOf(ruleResourceID));
		properties.put(NERLinnaeusTaggerDefaultSettings.USE_ABREVIATION, String.valueOf(useAbreviation));
		properties.put(NERLinnaeusTaggerDefaultSettings.DISAMBIGUATION, disambiguation.name());
		properties.put(NERLinnaeusTaggerDefaultSettings.CASE_SENSITIVE, caseSensitiveEnum.name());
		properties.put(NERLinnaeusTaggerDefaultSettings.NORMALIZATION, String.valueOf(normalized));
		properties.put(NERLinnaeusTaggerDefaultSettings.NUM_THREADS, String.valueOf(numberOfThreads));
		properties.put(NERLinnaeusTaggerDefaultSettings.PRE_PROCESSING, preProcessing.name());
		long stopwordsID = 0;
		if(stopWords!=null)
			stopwordsID =stopWords.getId();
		properties.put(NERLinnaeusTaggerDefaultSettings.LEXICAL_RESOURCE_STOPWORDS_ID, String.valueOf(stopwordsID));
		int lookuptable = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.LOOKUPTABLE_RESOURCE_ID, String.valueOf(lookuptable));
		int ontology = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.ONTOLOGY_RESOURCE_ID, String.valueOf(ontology));
//		properties.put(NERLinnaeusTaggerDefaultSettings.SIZE_OF_SMALL_WORDS_TO_BE_NOT_ANNOTATED, String.valueOf(sizeOfSmallWordsToBeNotAnnotated));
		return properties;
	}
	
	@JsonIgnore
	public void setConfiguration(Object obj) {
		if(obj instanceof ResourcesToNerAnote && resourceToNER!=null)
		{
			ResourcesToNerAnote resourceToNER = (ResourcesToNerAnote) obj;
			List<ResourceSelectedClassesMap> listResources = resourceToNER.getList();
			for(ResourceSelectedClassesMap res:listResources)
			{
				if(!this.resourceToNER.containsResource(res.getResource()))
				{
					this.resourceToNER.add(res.getResource(), res.getAllClassesID(), res.getSelectedClassesID());
				}
			}
		}
	}
	
	@Override
	public String getConfigurationUID() {
		return NERLinnaeusConfigurationImpl.nerLinnaeusUID;
	}


//	@Override
//	public void setConfigurationUID(String uid) {
//		NERLinnaeusConfigurationImpl.nerLinnaeusUID = uid;
//		
//	}

}