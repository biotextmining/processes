package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.util.Properties;

import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;

public class KeggWebserviceGenesDictionaryLoaderConfigurationImpl extends DictionaryLoaderConfigurationImpl implements IKeggWebserviceGenesDictionaryLoaderConfiguration{

	private static String loaderUID = "Kegg.genes";
	private String organism;
	
	public KeggWebserviceGenesDictionaryLoaderConfigurationImpl(IDictionary dictionary,boolean loadExtendalIDds, String organism) {
		super(loaderUID , dictionary, null, new Properties(), loadExtendalIDds);
		this.organism = organism;
	}

	@Override
	public String gerOrganism() {
		return organism;
	}


}
