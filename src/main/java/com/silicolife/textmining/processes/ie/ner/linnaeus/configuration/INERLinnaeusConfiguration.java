package com.silicolife.textmining.processes.ie.ner.linnaeus.configuration;


import java.util.Map;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.interfaces.process.IE.ner.INERConfiguration;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.linnaeus.adapt.uk.ac.man.entitytagger.matching.Matcher.Disambiguation;



public interface INERLinnaeusConfiguration extends INERConfiguration{
	
	public Map<String, Pattern> getPatterns();
	public ResourcesToNerAnote getResourceToNER();
	public int getNumberOfThreads();
	public boolean isUseAbreviation();
	public Disambiguation getDisambiguation();
	public NERCaseSensativeEnum getCaseSensitiveEnum();
	public boolean isNormalized();
	public void setNormalized( boolean newNormalizedOption );
	public ILexicalWords getStopWords();
	public boolean isUsingOtherResourceInfoToImproveRuleAnnotations();
	public NERLinnaeusPreProcessingEnum getPreProcessing();
	public int getSizeOfSmallWordsToBeNotAnnotated();
}