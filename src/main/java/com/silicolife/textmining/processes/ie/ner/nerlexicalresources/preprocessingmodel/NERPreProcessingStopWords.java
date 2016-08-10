package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;

public class NERPreProcessingStopWords extends NERSimple {
	
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
	
	
	public AnnotationPositions executeNer(String text,List<Long> listClassIDCaseSensative,NERCaseSensativeEnum caseSensitive) throws ANoteException {
		Set<String> stopWordsList = new HashSet<String>();
		if(stopWords!=null && stopWords.getLexicalWords()!=null)
		{
			if(caseSensitive.equals(NERCaseSensativeEnum.INALLWORDS))
			{
				stopWordsList = stopWords.getLexicalWords().keySet();
			}
			else
			{
				for(String sp : stopWords.getLexicalWords().keySet())
				{
					if(caseSensitive.equals(NERCaseSensativeEnum.NONE) 
							||(caseSensitive.equals(NERCaseSensativeEnum.ONLYINSMALLWORDS) && sp.length()>caseSensitive.getSmallWordSize()))
						stopWordsList.add(sp.toLowerCase());
					else if(caseSensitive.equals(NERCaseSensativeEnum.ONLYINSMALLWORDS) && sp.length()<=caseSensitive.getSmallWordSize())
						stopWordsList.add(sp);
				}
			}
		}
		AnnotationPositions annotations = new AnnotationPositions();
		NERCaseSensativeEnum caseSensitiveOption;
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
			if(caseSensitive.equals(NERCaseSensativeEnum.NONE) && stopWordsList.contains(term.toLowerCase())
					|| (caseSensitive.equals(NERCaseSensativeEnum.INALLWORDS) && term.length()>caseSensitive.getSmallWordSize()
					&& stopWordsList.contains(term.toLowerCase())))
			{
			}
			else if(stopWordsList.contains(term))
			{
			}
			else
			{
				if(listClassIDCaseSensative.contains(termAnnot.getClassAnnotation().getId()))
				{
					if(caseSensitive.equals(NERCaseSensativeEnum.NONE)){
						caseSensitiveOption = NERCaseSensativeEnum.INALLWORDS;
					}else{
						caseSensitiveOption = caseSensitive;
					}
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
					IEntityAnnotation entity = new EntityAnnotationImpl(pos.getStart(), pos.getEnd(), termAnnot.getClassAnnotation(),
							termAnnot.getResourceElement(), element, false, null);
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

	public Properties getProperties(INERLexicalResourcesConfiguration configuration) {
		Properties prop = super.getProperties(configuration);
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
