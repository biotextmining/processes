package com.silicolife.textmining.processes.ie.ner.linnaeus;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;

public class LinnauesTaggerByDocumentSingleton {
	
	final static Logger logger = LoggerFactory.getLogger(LinnauesTaggerByDocumentSingleton.class);
	
	private LinnaeusTaggerByDocument linnaeusTaggerByDocument;
	
	private static LinnauesTaggerByDocumentSingleton _instance;

	private LinnauesTaggerByDocumentSingleton()
	{
		
	}
	
	/**
	 * Gives access to the OpenNLP instance
	 * @return 
	 */
	public static synchronized LinnauesTaggerByDocumentSingleton getInstance() {
		if (_instance == null) {
			LinnauesTaggerByDocumentSingleton.createInstance();
		}
		return _instance;
	}
	
	/**
	 * Creates the singleton instance.
	 */
	private static void createInstance(){

		if (_instance == null) {
			_instance = new LinnauesTaggerByDocumentSingleton();
		}
	}
	
	public void initConfiguration(INERLinnaeusConfiguration configuration) throws ANoteException
	{
		if(linnaeusTaggerByDocument==null)
		{
			logger.info("Init Linnaues Tagger By Document Singleton");
			this.linnaeusTaggerByDocument = new LinnaeusTaggerByDocument(configuration);
		}
		else
		{
			logger.info("Linnaues Tagger By Document Singleton already initialized");
		}
	}
	
	public boolean isInit()
	{
		return linnaeusTaggerByDocument != null;
	}
	
	public void updateConfiguration(INERLinnaeusConfiguration configuration) throws ANoteException
	{
		logger.info("Update Linnaues Tagger By Document configurations");
		this.linnaeusTaggerByDocument = new LinnaeusTaggerByDocument(configuration);
	}
	
	public List<IEntityAnnotation> execute(IAnnotatedDocument annotationDoc) throws ANoteException
	{
		if(linnaeusTaggerByDocument==null)
			throw new ANoteException("Linnaues Tagger By Document not initialized");
		List<IEntityAnnotation> entities = linnaeusTaggerByDocument.executeDocument(annotationDoc);
		return entities;
	}
	
	public List<IEntityAnnotation> execute(String textStream) throws ANoteException
	{
		if(linnaeusTaggerByDocument==null)
			throw new ANoteException("Linnaues Tagger By Document not initialized");
		List<IEntityAnnotation> entities = linnaeusTaggerByDocument.executeDocument(textStream);
		return entities;
	}

}
