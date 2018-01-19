package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem;

import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.resources.dictionary.loaders.configuration.DictionaryLoaderConfigurationImpl;
import com.silicolife.textmining.core.interfaces.resource.dictionary.IDictionary;

public class PubChemDicitionayLoaderConfigurationImpl extends DictionaryLoaderConfigurationImpl implements IPubChemDictionaryLoaderConfiguration{

	private static String uid = "pubchem";
	private Set<String> pubChemIds;
	
	public PubChemDicitionayLoaderConfigurationImpl(IDictionary dictionary,Set<String> pubChemIds) {
		super(uid, dictionary, null, new Properties(), true);
		this.pubChemIds=pubChemIds;
	}

	public Set<String> getPubChemIds() {
		return pubChemIds;
	}

}
