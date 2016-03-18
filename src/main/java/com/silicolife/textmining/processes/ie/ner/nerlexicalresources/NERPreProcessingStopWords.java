package com.silicolife.textmining.processes.ie.ner.nerlexicalresources;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;

public class NERPreProcessingStopWords extends NER {
	
	private ILexicalWords stopWords;
	private boolean stop = false;
		
	public NERPreProcessingStopWords(List<IEntityAnnotation> termAnnotations,ILexicalWords stopWords){
		super(termAnnotations);
		this.stopWords = stopWords;
	}
	
	public NERPreProcessingStopWords(List<IEntityAnnotation> termAnnotations, HandRules rules,ILexicalWords stopWords){
		super(termAnnotations,rules);
		this.stopWords = stopWords;
	}
	
	
	public AnnotationPositions executeNer(String text,List<Long> listClassIDCaseSensative,boolean caseSensitive) throws ANoteException {
		Set<String> stopWordsList = new HashSet<String>();
		if(stopWords!=null && stopWords.getLexicalWords()!=null)
		{
			if(caseSensitive)
			{
				stopWordsList = stopWords.getLexicalWords().keySet();
			}
			else
			{
				for(String sp : stopWords.getLexicalWords().keySet())
				{
					stopWordsList.add(sp.toLowerCase());
				}
			}
		}
		AnnotationPositions annotations = new AnnotationPositions();
		boolean caseSensitiveOption;
		if(stop)
		{
			return new AnnotationPositions();
		}
		for(IEntityAnnotation termAnnot : getElementsToNER())
		{
			String term = termAnnot.getAnnotationValue();
			if(stop)
			{
				return new AnnotationPositions();
			}
			if(!caseSensitive && stopWordsList.contains(term.toLowerCase()))
			{
			}
			else if(stopWordsList.contains(term))
			{
			}
			else
			{
				if(listClassIDCaseSensative.contains(termAnnot.getClassAnnotation().getId()))
				{
					caseSensitiveOption = true;
				}
				else
				{
					caseSensitiveOption = caseSensitive;
				}
				/**
				 * For working we have to put a space character - regular expression problem
				 */
				List<AnnotationPosition> positions = searchTermInText(term, text+" ", caseSensitiveOption);		
				for(AnnotationPosition pos : positions)
				{
					String element = text.substring(pos.getStart(), pos.getEnd());
					String annotationValueNormalized = NormalizationForm.getNormalizationForm(element);
					IEntityAnnotation entity = new EntityAnnotationImpl(pos.getStart(), pos.getEnd(), termAnnot.getClassAnnotation(),
							termAnnot.getResourceElement(), element, annotationValueNormalized, null);
					annotations.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(pos,entity);
				}
			}
		}
		if(getRules() != null && !stop)
			getRules().applyRules(text, annotations);	
		return annotations;
	}


	public void stop() {
		stop = true;		
	}

	public Properties getProperties(ResourcesToNerAnote resources,boolean normalization) {
		Properties prop = super.getProperties(resources, normalization);
		Properties other = stopWordsProperties();
		prop.putAll(other);
		return prop;
	}
	
	private Properties stopWordsProperties() {
		Properties prop = new Properties();
		if(stopWords!=null)
		{
			prop.put(GlobalNames.stopWordsResourceID,String.valueOf(stopWords.getId()));
		}
		return prop;
	}
	
	public void preprocessing(Properties prop) {
		prop.put(GlobalNames.nerpreProcessing,GlobalNames.stopWords);
	}


}
