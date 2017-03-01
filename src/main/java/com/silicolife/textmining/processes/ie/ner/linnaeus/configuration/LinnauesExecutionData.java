package com.silicolife.textmining.processes.ie.ner.linnaeus.configuration;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.process.ner.ElementToNer;
import com.silicolife.textmining.core.datastructures.process.ner.HandRules;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class LinnauesExecutionData {
	
	private ElementToNer elementsToNER;
	private HandRules rules;
	private List<IEntityAnnotation> elements;
	private Map<Long, Long> resourceMapClass;
	private Map<Long, IResourceElement> resourceIDMapResource;
	private Map<String, Set<Long>> maplowerCaseToPossibleResourceIDs;
	private Map<Long, String> mapPossibleResourceIDsToTermString;
	private Set<String> stopwords;
	
	
	public LinnauesExecutionData(ElementToNer elementsToNER, HandRules rules, List<IEntityAnnotation> elements,
			Map<Long, Long> resourceMapClass, Map<Long, IResourceElement> resourceIDMapResource,
			Map<String, Set<Long>> maplowerCaseToPossibleResourceIDs,
			Map<Long, String> mapPossibleResourceIDsToTermString, Set<String> stopwords) {
		super();
		this.elementsToNER = elementsToNER;
		this.rules = rules;
		this.elements = elements;
		this.resourceMapClass = resourceMapClass;
		this.resourceIDMapResource = resourceIDMapResource;
		this.maplowerCaseToPossibleResourceIDs = maplowerCaseToPossibleResourceIDs;
		this.mapPossibleResourceIDsToTermString = mapPossibleResourceIDsToTermString;
		this.stopwords = stopwords;
	}


	public ElementToNer getElementsToNER() {
		return elementsToNER;
	}


	public void setElementsToNER(ElementToNer elementsToNER) {
		this.elementsToNER = elementsToNER;
	}


	public HandRules getRules() {
		return rules;
	}


	public void setRules(HandRules rules) {
		this.rules = rules;
	}


	public List<IEntityAnnotation> getElements() {
		return elements;
	}


	public void setElements(List<IEntityAnnotation> elements) {
		this.elements = elements;
	}


	public Map<Long, Long> getResourceMapClass() {
		return resourceMapClass;
	}


	public void setResourceMapClass(Map<Long, Long> resourceMapClass) {
		this.resourceMapClass = resourceMapClass;
	}


	public Map<Long, IResourceElement> getResourceIDMapResource() {
		return resourceIDMapResource;
	}


	public void setResourceIDMapResource(Map<Long, IResourceElement> resourceIDMapResource) {
		this.resourceIDMapResource = resourceIDMapResource;
	}


	public Map<String, Set<Long>> getMaplowerCaseToPossibleResourceIDs() {
		return maplowerCaseToPossibleResourceIDs;
	}


	public void setMaplowerCaseToPossibleResourceIDs(Map<String, Set<Long>> maplowerCaseToPossibleResourceIDs) {
		this.maplowerCaseToPossibleResourceIDs = maplowerCaseToPossibleResourceIDs;
	}


	public Map<Long, String> getMapPossibleResourceIDsToTermString() {
		return mapPossibleResourceIDsToTermString;
	}


	public void setMapPossibleResourceIDsToTermString(Map<Long, String> mapPossibleResourceIDsToTermString) {
		this.mapPossibleResourceIDsToTermString = mapPossibleResourceIDsToTermString;
	}


	public Set<String> getStopwords() {
		return stopwords;
	}


	public void setStopwords(Set<String> stopwords) {
		this.stopwords = stopwords;
	}
	
	
	
	
	
	

}
