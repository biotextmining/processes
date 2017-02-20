package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel;

import java.io.IOException;
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
import com.silicolife.textmining.core.interfaces.core.document.structure.ITextSegment;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.nlptools.opennlp.OpenNLP;

public class NERPreProcessingPOSTagging extends NERSimple{

	private Set<String> positiveFilterTags;
	private boolean stop = false;

	
	public NERPreProcessingPOSTagging(List<IEntityAnnotation> termAnnotations,Set<String> positiveFilterTags) {
		super(termAnnotations);
		this.positiveFilterTags = positiveFilterTags;
	}
	
	public NERPreProcessingPOSTagging(List<IEntityAnnotation> termAnnotations, HandRules rules,Set<String> positiveFilterTags)
	{
		super(termAnnotations,rules);
		this.positiveFilterTags = positiveFilterTags;
	}
	
	public AnnotationPositions executeNer(String text,List<Long> listClassIDCaseSensative,NERCaseSensativeEnum caseSensitive) throws ANoteException, IOException{
		AnnotationPositions annotations = new AnnotationPositions();
		NERCaseSensativeEnum caseSensitiveOption;
		AnnotationPosition auxpos;
		if(stop)
			return new AnnotationPositions();
		List<ITextSegment> textSegments = OpenNLP.getInstance().geTextSegmentsFilterByPOSTags(text,positiveFilterTags);
		for(IEntityAnnotation termAnnot : getElementsToNER())
		{
			if(stop)
			{
				break;
			}
			String term = termAnnot.getAnnotationValue();
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
			List<AnnotationPosition> positions = searchTermInText(term, text+" ", caseSensitiveOption);
			for(AnnotationPosition pos : positions)
			{
				if(stop)
					return new AnnotationPositions();
				if(insideTExtSegment(textSegments,pos))
				{
					auxpos = new AnnotationPosition(pos.getStart(),pos.getEnd());
					String element = text.substring(pos.getStart(), pos.getEnd());
					IEntityAnnotation entity = new EntityAnnotationImpl(auxpos.getStart(), auxpos.getEnd(), termAnnot.getClassAnnotation(),
							termAnnot.getResourceElement(), element, false,false, null);
					annotations.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(auxpos,entity);
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
	
	protected boolean insideTExtSegment(List<ITextSegment> textSegments,AnnotationPosition pos) {
		
		for(ITextSegment textSegment:textSegments)
		{
			if(textSegment.getStartOffset() > pos.getEnd())
			{
				return false;
			}
			if(textSegment.getStartOffset() <= pos.getStart() && textSegment.getEndOffset() >= pos.getEnd())
			{
				return true;
			}
		}
		return false;
	}

	public Properties getProperties(INERLexicalResourcesConfiguration configuration) {
		Properties prop = super.getProperties(configuration);
		Properties other = tagProperties();
		prop.putAll(other);
		return prop;
	}
	
	public void preprocessing(Properties prop) {
		prop.put(GlobalNames.nerpreProcessing,GlobalNames.nerpreProcessingPosTagging);
	}
	
	private Properties tagProperties()
	{
		Properties prop = new Properties();
		String posTags = new String();
		for(String label:positiveFilterTags)
		{
			posTags = posTags + label + "|";
		}
		prop.put(GlobalNames.nerpreProcessingTags,posTags);
		return prop;
	}
	
	public void stop() {
		stop = true;		
	}
	
	public Set<String> getPositiveFilterTags() {
		return positiveFilterTags;
	}

}
