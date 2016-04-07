package com.silicolife.textmining.processes.ie.ner.nerlexicalresources;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.textprocessing.NormalizationForm;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.structure.ITextSegment;
import com.silicolife.textmining.core.interfaces.resource.lexicalwords.ILexicalWords;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

public class NERPreProcessingPOSTaggingAndStopWords extends NERPreProcessingPOSTagging{
	
	private ILexicalWords stopWords;
	private boolean stop = false;
	
	public NERPreProcessingPOSTaggingAndStopWords(List<IEntityAnnotation> termAnnotations,Set<String> positiveFilterTags,ILexicalWords stopWords) {
		super(termAnnotations, positiveFilterTags);
		this.stopWords = stopWords;
	}
	
	
	public NERPreProcessingPOSTaggingAndStopWords(List<IEntityAnnotation> termAnnotations, HandRules rules,Set<String> positiveFilterTags,ILexicalWords stopWords) {
		super(termAnnotations, rules, positiveFilterTags);
		this.stopWords = stopWords;
	}

	public AnnotationPositions executeNer(String text,List<Long> listClassIDCaseSensative,NERCaseSensativeEnum caseSensitive) throws ANoteException, IOException{
		
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
		AnnotationPosition auxpos;
		if(stop)
			return new AnnotationPositions();
		List<ITextSegment> textSegments = OpenNLP.getInstance().geTextSegmentsFilterByPOSTags(text,getPositiveFilterTags());
		for(IEntityAnnotation termAnnot : getElementsToNER())
		{
			if(stop)
			{
				break;
			}
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
			String term = termAnnot.getAnnotationValue();
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
				List<AnnotationPosition> positions = searchTermInText(term, text+" ", caseSensitiveOption);
				for(AnnotationPosition pos : positions)
				{
					if(stop)
						return new AnnotationPositions();
					if(insideTExtSegment(textSegments,pos))
					{
						auxpos = new AnnotationPosition(pos.getStart(),pos.getEnd());
						String element = text.substring(pos.getStart(), pos.getEnd());
						String annotationValueNormalized = NormalizationForm.getNormalizationForm(element);
						IEntityAnnotation entity = new EntityAnnotationImpl(auxpos.getStart(), auxpos.getEnd(), termAnnot.getClassAnnotation(),
								termAnnot.getResourceElement(), element, annotationValueNormalized, null);
						annotations.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(auxpos,entity);
					}
				}
			}

		}
		if(getRules() != null)
		{
			for(ITextSegment segment:textSegments)
			{
				if(stop)
					return new AnnotationPositions();
				getRules().applyRules(segment, annotations);
			}
		}
		return annotations;
	}
	
	public Properties getProperties(ResourcesToNerAnote resources,boolean normalization) {
		Properties prop = super.getProperties(resources, normalization);
		Properties other = stopWordsProperties();
		prop.putAll(other);
		return prop;
	}
	
	private Properties stopWordsProperties() {
		Properties prop = new Properties();
		if(getStopWords()!=null)
		{
			prop.put(GlobalNames.stopWordsResourceID,String.valueOf(stopWords.getId()));
		}
		return prop;
	}
	
	public void stop() {
		stop = true;		
	}

	public ILexicalWords getStopWords() {
		return stopWords;
	}

	public void preprocessing(Properties prop) {
		prop.put(GlobalNames.nerpreProcessing,GlobalNames.nerpreProcessingPosTaggingAndStopWords);
	}

}
