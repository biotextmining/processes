package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem;

import java.util.Set;

import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public interface IPubChemDictionaryLoaderConfiguration extends IDictionaryLoaderConfiguration{
	public Set<String> getPubChemIds();

}
