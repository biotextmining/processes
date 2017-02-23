package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.process.IEProcessImpl;
import com.silicolife.textmining.core.datastructures.process.ProcessRunStatusConfigurationEnum;
import com.silicolife.textmining.core.datastructures.process.ProcessTypeImpl;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ResourceSelectedClassesMap;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.NERLexicalResources;

public class NERLexicalResourcesConfiguration extends NERConfigurationImpl implements INERLexicalResourcesConfiguration{

	public static String nerLexicalResourcesUID = "ner.lexicalresources";

	private NERLexicalResourcesPreProssecingEnum preProcessing;
	private ResourcesToNerAnote resourceToNER;
	private Set<String> posTags;
	private NERCaseSensativeEnum caseSensitive;
	private ILexicalWords stopWords;
	private boolean normalized;
	private boolean usingOtherResourceInfoToImproveRuleAnnotstions;
	
	
	public NERLexicalResourcesConfiguration(ICorpus corpus,ProcessRunStatusConfigurationEnum processRunStatusConfigurationEnum,NERLexicalResourcesPreProssecingEnum preProcessing,ResourcesToNerAnote resourceToNER,
			Set<String> posTgas,ILexicalWords stopWords, NERCaseSensativeEnum caseSensitive,boolean normalized,boolean usingOtherResourceInfoToImproveRuleAnnotstions) {
		super(corpus,NERLexicalResources.nerlexicalresourcesTagger,build(corpus),processRunStatusConfigurationEnum);
		this.preProcessing = preProcessing;
		this.resourceToNER = resourceToNER;
		this.posTags = posTgas;
		this.stopWords = stopWords;
		this.normalized = normalized;
		this.usingOtherResourceInfoToImproveRuleAnnotstions = usingOtherResourceInfoToImproveRuleAnnotstions;
		this.caseSensitive = caseSensitive;
	}
	
	private static IIEProcess build(ICorpus corpus) {

		String description = NERLexicalResources.nerlexicalresourcesTagger  + " " +Utils.SimpleDataFormat.format(new Date());
		String notes = new String();
		Properties properties = new Properties();
		IIEProcess processToRun = new IEProcessImpl(corpus, description, notes, ProcessTypeImpl.getNERProcessType(), NERLexicalResources.nerlexicalresourcesOrigin, properties);
		return processToRun;
	}
	
	@Override
	public NERLexicalResourcesPreProssecingEnum getPreProcessingOption() {
		return preProcessing;
	}

	@Override
	public ResourcesToNerAnote getResourceToNER() {
		return resourceToNER;
	}

	@Override
	public Set<String> getPOSTags() {
		return posTags;
	}

	@Override
	public ILexicalWords getStopWords() {
		return stopWords;
	}

	@Override
	public NERCaseSensativeEnum getCaseSensitive() {
		return caseSensitive;
	}

	@Override
	public boolean isNormalized() {
		return normalized;
	}

	@Override
	public void setNormalized(boolean newNormalizedOption) {
		normalized = newNormalizedOption;
	}


	@Override
	public boolean usingOtherResourceInfoToImproveRuleAnnotations() {
		return usingOtherResourceInfoToImproveRuleAnnotstions;
	}


	@Override
	public Map<String, String> getNERProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(NERLexicalResourcesDefaultSettings.USE_PARTIAL_MATCH_WITH_DICTIONARIES, String.valueOf(usingOtherResourceInfoToImproveRuleAnnotstions));
		properties.put(NERLexicalResourcesDefaultSettings.CASE_SENSITIVE, String.valueOf(caseSensitive));
		properties.put(NERLexicalResourcesDefaultSettings.PRE_PROCESSING, preProcessing.name());
		properties.put(NERLexicalResourcesDefaultSettings.NORMALIZATION, String.valueOf(normalized));
		long stopwordsID = 0;
		if(stopWords!=null)
			stopwordsID =  stopWords.getId();
		properties.put(NERLexicalResourcesDefaultSettings.LEXICAL_RESOURCE_STOPWORDS_ID, String.valueOf(stopwordsID));
		int ruleID = 0;
		properties.put(NERLexicalResourcesDefaultSettings.RULES_RESOURCE_ID, String.valueOf(ruleID));
		int lookuptable = 0;
		properties.put(NERLexicalResourcesDefaultSettings.LOOKUPTABLE_RESOURCE_ID, String.valueOf(lookuptable));
		int ontology = 0;
		properties.put(NERLexicalResourcesDefaultSettings.ONTOLOGY_RESOURCE_ID, String.valueOf(ontology));
		return properties;
	}


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
		return NERLexicalResourcesConfiguration.nerLexicalResourcesUID;
	}


	@Override
	public void setConfigurationUID(String uid) {
		NERLexicalResourcesConfiguration.nerLexicalResourcesUID = uid;
		
	}

}
