package com.silicolife.textmining.processes.ie.ner.nerlexicalresources.configuration;

import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationPositions;
import com.silicolife.textmining.core.datastructures.process.ner.NERCaseSensativeEnum;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public interface INERLexicalResourcesPreProcessingModel {
	
	public Properties getProperties(INERLexicalResourcesConfiguration lexicalResourcesConfiguration);

	public AnnotationPositions executeNer(String text,List<Long> classIdCaseSensative,NERCaseSensativeEnum caseSensitive, boolean normalization) throws ANoteException;

	public void stop();

}
