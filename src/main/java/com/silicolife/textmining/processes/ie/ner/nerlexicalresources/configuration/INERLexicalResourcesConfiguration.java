package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration;

import java.util.Set;

import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.interfaces.process.IE.ner.INERConfiguration;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;

public interface INERLexicalResourcesConfiguration extends INERConfiguration{
	
	public NERLexicalResourcesPreProssecingEnum getPreProcessingOption();
	public ResourcesToNerAnote getResourceToNER();
	public Set<String> getPOSTags();
	public ILexicalWords getStopWords();
	public boolean isNormalized();
	public void setNormalized(boolean newNormalizedOption);
	public NERCaseSensativeEnum getCaseSensitive();
	public boolean usingOtherResourceInfoToImproveRuleAnnotations();
}
