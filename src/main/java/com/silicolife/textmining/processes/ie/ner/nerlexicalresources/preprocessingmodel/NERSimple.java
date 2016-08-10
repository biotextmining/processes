package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.preprocessingmodel;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPosition;
import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.datastructures.process.ner.ResourcesToNerAnote;
import com.silicolife.textmining.core.datastructures.resources.ResourceImpl;
import com.silicolife.textmining.core.datastructures.textprocessing.ParsingUtils;
import com.silicolife.textmining.core.datastructures.textprocessing.TermSeparator;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesConfiguration;
import com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration.INERLexicalResourcesPreProcessingModel;

public class NERSimple implements INERLexicalResourcesPreProcessingModel{

	private List<IEntityAnnotation> elements;	
	private HandRules rules;
	private static final String SEPARATOR = "[ '<>/(),\n\\.]+";
	private boolean stop = false;
		
	public NERSimple(List<IEntityAnnotation> termAnnotations){
		this.elements = termAnnotations;
		this.rules = null;
	}
	
	public NERSimple(List<IEntityAnnotation> termAnnotations, HandRules rules){
		this.elements = termAnnotations;
		this.rules = rules;
	}
	
	
	public AnnotationPositions executeNer(String text,List<Long> listClassIDCaseSensative,NERCaseSensativeEnum caseSensitive,boolean normalization) throws  ANoteException{
		if(normalization){
			text = TermSeparator.termSeparator(text);
		}
		AnnotationPositions annotations = new AnnotationPositions();
		NERCaseSensativeEnum caseSensitiveOption;
		if(stop)
		{
			return new AnnotationPositions();
		}
		for(IEntityAnnotation termAnnot : elements)
		{
			if(stop)
			{
				return new AnnotationPositions();
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
				
				String element = text.substring(pos.getStart(), pos.getEnd());
				IEntityAnnotation entity = new EntityAnnotationImpl(pos.getStart(), pos.getEnd(), termAnnot.getClassAnnotation(),
						termAnnot.getResourceElement(), element, false, null);
				annotations.addAnnotationWhitConflitsAndReplaceIfRangeIsMore(pos,entity);
			}
		}
		if(rules != null && !stop)
			rules.applyRules(text, annotations);	
		return annotations;
	}

	
	public List<IEntityAnnotation> getElementsToNER() {
		return elements;
	}

	public HandRules getRules() {
		return rules;
	}

	protected List<AnnotationPosition> searchTermInText(String term, String text, NERCaseSensativeEnum caseSensitive){
		List<AnnotationPosition> positions = new ArrayList<AnnotationPosition>();
		String termExp = ParsingUtils.textToRegExp(term);
		Pattern p = null;
		if(caseSensitive.equals(NERCaseSensativeEnum.NONE)||
				caseSensitive.equals(NERCaseSensativeEnum.ONLYINSMALLWORDS)&& term.length()>caseSensitive.getSmallWordSize()){
			p = Pattern.compile(SEPARATOR + "("+termExp+")" + SEPARATOR, Pattern.CASE_INSENSITIVE);
		}else{
			p = Pattern.compile(SEPARATOR + "("+termExp+")" + SEPARATOR);
		}
		Matcher m = p.matcher(text);
		
		while(m.find())
		{
			AnnotationPosition pos = new AnnotationPosition(m.start(1), m.end(1), term, text.substring(m.start(1), m.end(1)));
			positions.add(pos);
		}
		if(caseSensitive.equals(NERCaseSensativeEnum.NONE)||
				caseSensitive.equals(NERCaseSensativeEnum.ONLYINSMALLWORDS)&& term.length()>caseSensitive.getSmallWordSize()){
			p = Pattern.compile("^" + "("+termExp+")" + SEPARATOR, Pattern.CASE_INSENSITIVE);
		}else{
			p = Pattern.compile("^" + "("+termExp+")" + SEPARATOR);
		}
		m = p.matcher(text);
		
		while(m.find())
		{
			AnnotationPosition pos = new AnnotationPosition(m.start(1), m.end(1),term, text.substring(m.start(1), m.end(1)));
			positions.add(pos);
		}
	
		return positions;
	}
	
	public void setDictionary(List<IEntityAnnotation> dictionary) {
		this.elements = dictionary;
	}

	public void setRules(HandRules rules) {
		this.rules = rules;
	}

	public void stop() {
		stop = true;		
	}

	public Properties getProperties(INERLexicalResourcesConfiguration configuration) {
		return configureProperties(configuration.getResourceToNER(),configuration.isNormalized()) ;
	}
	
	
	private Properties configureProperties(ResourcesToNerAnote resources,boolean normalization) {
		Properties prop = transformResourcesToOrderMapInProperties(resources);
		if(normalization)
		{
			prop.put(GlobalNames.normalization,String.valueOf(normalization));
		}
		prop.put(GlobalNames.casesensitive, resources.getCaseSensitive().name());
		if(resources.isUseOtherResourceInformationInRules())
		{
			prop.put(GlobalNames.useOtherResourceInformationInRules,"true");
		}
		preprocessing(prop);
		return prop;
	}
	
	public void preprocessing(Properties prop) {
		prop.put(GlobalNames.nerpreProcessing,GlobalNames.nerpreProcessingNo);
	}

	private static Properties transformResourcesToOrderMapInProperties(ResourcesToNerAnote resources) {
		Properties prop = new Properties();
		for(int i=0;i<resources.getList().size();i++)
		{
			Set<Long> selected = resources.getList().get(i).getSelectedClassesID();
			long id = resources.getList().get(i).getResource().getId();
			{
				prop.put(String.valueOf(id),ResourceImpl.convertClassesToResourceProperties(selected));
			}
		}
		return prop;
	}
	

}
