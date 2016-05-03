package com.silicolife.textmining.processes.ie.ner.linnaeus.configuration;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.NERConfigurationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.utils.GenericTriple;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.resource.IResource;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.linnaeus.LinnaeusTagger;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher.Disambiguation;

public class NERLinnaeusConfigurationImpl extends NERConfigurationImpl implements INERLinnaeusConfiguration{
	
	public static final String nerLinnaeusUID = "ner.linnaeus";

	private Map<String, Pattern> patterns;
	private ResourcesToNerAnote resourceToNER;
	private int numThreads;
	private boolean useabreviation;
	private Disambiguation disambiguation;
	private NERCaseSensativeEnum caseSensitiveEnum;
	private boolean normalized;
	private ILexicalWords stopwords;
	private NERLinnaeusPreProcessingEnum preprocessing;
	private boolean usingOtherResourceInfoToImproveRuleAnnotations;
	
	public NERLinnaeusConfigurationImpl(ICorpus corpus,Map<String, Pattern> patterns, ResourcesToNerAnote resourceToNER, boolean useabreviation,
			Disambiguation disambiguation, NERCaseSensativeEnum caseSensitiveEnum,boolean normalized, int numThreads,ILexicalWords stopwords,
			NERLinnaeusPreProcessingEnum preprocessing,boolean usingOtherResourceInfoToImproveRuleAnnotations) {
		super(corpus,LinnaeusTagger.linneausTagger,LinnaeusTagger.linneausTagger);
		this.patterns = patterns;
		this.resourceToNER = resourceToNER;
		this.useabreviation = useabreviation;
		this.disambiguation = disambiguation;
		this.caseSensitiveEnum = caseSensitiveEnum;
		this.normalized = normalized;
		this.numThreads = numThreads;
		this.stopwords = stopwords;
		this.usingOtherResourceInfoToImproveRuleAnnotations = usingOtherResourceInfoToImproveRuleAnnotations;
		this.preprocessing = preprocessing;
	}

	@Override
	public Map<String, Pattern> getPatterns() {
		return patterns;
	}

	@Override
	public ResourcesToNerAnote getResourceToNER() {
		return resourceToNER;
	}
	
	@Override
	public int getNumberOfThreads() {
		return numThreads;
	}

	@Override
	public boolean isUseAbreviation() {
		return useabreviation;
	}

	@Override
	public NERCaseSensativeEnum getCaseSensitiveEnum() {
		return caseSensitiveEnum;
	}

	@Override
	public boolean isNormalized() {
		return normalized;
	}

	@Override
	public Disambiguation getDisambiguation() {
		return disambiguation;
	}
	
	@Override
	public void setNormalized(boolean newNormalizedOption) {
		normalized = newNormalizedOption;
	}

	@Override
	public ILexicalWords getStopWords() {
		return stopwords;
	}

	@Override
	public boolean usingOtherResourceInfoToImproveRuleAnnotations() {
		return usingOtherResourceInfoToImproveRuleAnnotations;
	}

	public NERLinnaeusPreProcessingEnum getPreProcessingOption() {
		return preprocessing;
	}

	@Override
	public Map<String, String> getNERProperties() {
		Map<String, String> properties = new HashMap<String, String>();
		properties.put(NERLinnaeusTaggerDefaultSettings.USE_PARTIAL_MATCH_WITH_DICTIONARIES, String.valueOf(usingOtherResourceInfoToImproveRuleAnnotations));
		long ruleResourceID = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.RULES_RESOURCE_ID, String.valueOf(ruleResourceID));
		properties.put(NERLinnaeusTaggerDefaultSettings.USE_ABREVIATION, String.valueOf(useabreviation));
		properties.put(NERLinnaeusTaggerDefaultSettings.DISAMBIGUATION, disambiguation.name());
		properties.put(NERLinnaeusTaggerDefaultSettings.CASE_SENSITIVE, caseSensitiveEnum.name());
		properties.put(NERLinnaeusTaggerDefaultSettings.NORMALIZATION, String.valueOf(normalized));
		properties.put(NERLinnaeusTaggerDefaultSettings.NUM_THREADS, String.valueOf(numThreads));
		properties.put(NERLinnaeusTaggerDefaultSettings.PRE_PROCESSING, preprocessing.name());
		long stopwordsID = 0;
		if(stopwords!=null)
			stopwordsID =stopwords.getId();
		properties.put(NERLinnaeusTaggerDefaultSettings.LEXICAL_RESOURCE_STOPWORDS_ID, String.valueOf(stopwordsID));
		int lookuptable = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.LOOKUPTABLE_RESOURCE_ID, String.valueOf(lookuptable));
		int ontology = 0;
		properties.put(NERLinnaeusTaggerDefaultSettings.ONTOLOGY_RESOURCE_ID, String.valueOf(ontology));
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

	@Override
	public String getConfigurationUID() {
		return NERLinnaeusConfigurationImpl.nerLinnaeusUID;
	}

}