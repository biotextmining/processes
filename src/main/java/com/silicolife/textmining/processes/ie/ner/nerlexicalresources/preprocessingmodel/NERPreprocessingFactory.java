package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesPreProcessingModel;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.NERLexicalResourcesPreProssecingEnum;

public class NERPreprocessingFactory {

	public static INERLexicalResourcesPreProcessingModel build(INERLexicalResourcesConfiguration lexicalResurcesConfiguration, NERLexicalResourcesPreProssecingEnum preprocessing) {
		
		List<IEntityAnnotation> termAnnotations = null;
		if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.No))
		{
			return new NERSimple(termAnnotations);
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.StopWords))
		{
			return new NERPreProcessingStopWords(termAnnotations, lexicalResurcesConfiguration.getStopWords());
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.POSTagging))
		{
			return new NERPreProcessingPOSTagging(termAnnotations, lexicalResurcesConfiguration.getPOSTags());
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.Hybrid))
		{
			return new NERPreProcessingPOSTaggingAndStopWords(termAnnotations, lexicalResurcesConfiguration.getPOSTags(), lexicalResurcesConfiguration.getStopWords());
		}
		return null;
	}
	
	

}
