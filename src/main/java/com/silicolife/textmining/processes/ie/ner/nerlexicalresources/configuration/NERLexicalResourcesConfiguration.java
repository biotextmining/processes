package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.language.LanguageProperties;
import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.utils.GenericTriple;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.NERLexicalResources;

public class NERLexicalResourcesConfiguration extends NERConfigurationImpl implements INERLexicalResourcesConfiguration{

	private NERLexicalResourcesPreProssecingEnum preProcessing;
	private ResourcesToNerAnote resourceToNER;
	private Set<String> posTags;
	private boolean caseSensitive;
	private ILexicalWords stopWords;
	private boolean normalized;
	private boolean usingOtherResourceInfoToImproveRuleAnnotstions;
	
	
	public NERLexicalResourcesConfiguration(ICorpus corpus,NERLexicalResourcesPreProssecingEnum preProcessing,ResourcesToNerAnote resourceToNER,
			Set<String> posTgas,ILexicalWords stopWords, boolean caseSensitive,boolean normalized,boolean usingOtherResourceInfoToImproveRuleAnnotstions) {
		super(corpus,NERLexicalResources.nerlexicalresourcesTagger,LanguageProperties.getLanguageStream("pt.uminho.anote2.ner.operations.name"));
		this.preProcessing = preProcessing;
		this.resourceToNER = resourceToNER;
		this.posTags = posTgas;
		this.stopWords = stopWords;
		this.normalized = normalized;
		this.usingOtherResourceInfoToImproveRuleAnnotstions = usingOtherResourceInfoToImproveRuleAnnotstions;
		this.caseSensitive = caseSensitive;
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
	public boolean isCaseSensitive() {
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
			List<GenericTriple<IResource<IResourceElement>, Set<Long>, Set<Long>>> listResources = resourceToNER.getList();
			for(GenericTriple<IResource<IResourceElement>, Set<Long>, Set<Long>> res:listResources)
			{
				if(!this.resourceToNER.containsResource(res.getX()))
				{
					this.resourceToNER.add(res.getX(), res.getY(), res.getZ());
				}
			}
		}
	}

}
