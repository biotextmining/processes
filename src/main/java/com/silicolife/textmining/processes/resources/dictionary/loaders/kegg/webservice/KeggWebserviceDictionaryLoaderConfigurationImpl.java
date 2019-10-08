package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;

public class KeggWebserviceDictionaryLoaderConfigurationImpl extends DictionaryLoaderConfigurationImpl implements IKeggWebserviceDictionaryLoaderConfiguration{

	private static String loaderUID = "Kegg";
	private List<KeggEntitiesEnum> keggEntitiesToLoad;
	
	public KeggWebserviceDictionaryLoaderConfigurationImpl(IDictionary dictionary,boolean loadExtendalIDds,List<KeggEntitiesEnum> keggEntitiesToLoad) {
		super(loaderUID , dictionary, null, new Properties(), loadExtendalIDds);
		this.keggEntitiesToLoad = keggEntitiesToLoad;
	}

	public List<KeggEntitiesEnum> getKeggEntities() {
		return keggEntitiesToLoad;
	}

}
