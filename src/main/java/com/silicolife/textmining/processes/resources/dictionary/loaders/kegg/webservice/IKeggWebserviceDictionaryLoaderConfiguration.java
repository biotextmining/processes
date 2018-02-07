package com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice;

import java.util.List;

import com.silicolife.textmining.core.interfaces.resource.dictionary.configuration.IDictionaryLoaderConfiguration;

public interface IKeggWebserviceDictionaryLoaderConfiguration extends IDictionaryLoaderConfiguration{
	public List<KeggEntitiesEnum> getKeggEntities();

}
