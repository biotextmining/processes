package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel;

import java.util.List;

import com.silicolife.textmining.core.datastructures.process.ner.ElementToNer;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesPreProcessingModel;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.NERLexicalResourcesPreProssecingEnum;

public class NERPreprocessingFactory {

	public static INERLexicalResourcesPreProcessingModel build(INERLexicalResourcesConfiguration lexicalResurcesConfiguration, NERLexicalResourcesPreProssecingEnum preprocessing) throws ANoteException {
		
		ElementToNer elemntsToNER = new ElementToNer(lexicalResurcesConfiguration.getResourceToNER(), lexicalResurcesConfiguration.isNormalized());
		List<IEntityAnnotation> elements = elemntsToNER.getTerms();
		if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.No))
		{
			return new NERSimple(elements);
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.StopWords))
		{
			return new NERPreProcessingStopWords(elements, lexicalResurcesConfiguration.getStopWords());
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.POSTagging))
		{
			return new NERPreProcessingPOSTagging(elements, lexicalResurcesConfiguration.getPOSTags());
		}
		else if(preprocessing.equals(NERLexicalResourcesPreProssecingEnum.Hybrid))
		{
			return new NERPreProcessingPOSTaggingAndStopWords(elements, lexicalResurcesConfiguration.getPOSTags(), lexicalResurcesConfiguration.getStopWords());
		}
		return null;
	}
	
	

}
