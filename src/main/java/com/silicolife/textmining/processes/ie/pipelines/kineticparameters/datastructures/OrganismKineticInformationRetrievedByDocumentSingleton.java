package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.datastructures;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.processes.ie.ner.linnaeus.configuration.INERLinnaeusConfiguration;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces.INERREProcessByDocument;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps.KineticREByDocument;
import com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps.LinnaeusTaggerByDocument;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;

public  class  OrganismKineticInformationRetrievedByDocumentSingleton implements INERREProcessByDocument{
	
	final static Logger logger = LoggerFactory.getLogger(OrganismKineticInformationRetrievedByDocumentSingleton.class);
	
	private LinnaeusTaggerByDocument linnaeusTaggerByDocument;
	private KineticREByDocument kineticREByDocument;
	
	private static OrganismKineticInformationRetrievedByDocumentSingleton _instance;

	private OrganismKineticInformationRetrievedByDocumentSingleton()
	{
		
	}

	/**
	 * Gives access to the OpenNLP instance
	 * @return 
	 */
	public static synchronized OrganismKineticInformationRetrievedByDocumentSingleton getInstance() {
		if (_instance == null) {
			OrganismKineticInformationRetrievedByDocumentSingleton.createInstance();
		}
		return _instance;
	}
	
	/**
	 * Creates the singleton instance.
	 */
	private static void createInstance(){

		if (_instance == null) {
			_instance = new OrganismKineticInformationRetrievedByDocumentSingleton();
		}
	}
	
	public void initNERREConfiguration(INERLinnaeusConfiguration configuration,IREKineticREConfiguration kineticREConfiguration) throws ANoteException
	{
		if(linnaeusTaggerByDocument==null)
		{
			logger.info("Init Linnaues Tagger By Document");
			this.linnaeusTaggerByDocument = new LinnaeusTaggerByDocument(configuration);
		}
		else
		{
			logger.info("Linnaues Tagger By Document already initialized");
		}
		if(kineticREByDocument==null)
		{
			logger.info("Init KineticRE By Document");
			this.kineticREByDocument = new KineticREByDocument(kineticREConfiguration);	
		}
		else
		{
			logger.info("KineticRE By Document already initialized");
		}
	}
	
	public void updateNERREConfiguration(INERLinnaeusConfiguration configuration,IREKineticREConfiguration kineticREConfiguration) throws ANoteException
	{
		logger.info("Update Linnaues Tagger By Document configurations");
		this.linnaeusTaggerByDocument = new LinnaeusTaggerByDocument(configuration);
		logger.info("Update KineticRE By Document configurations");
		this.kineticREByDocument = new KineticREByDocument(kineticREConfiguration);
	}

	
	public List<IEventAnnotation> execute(IAnnotatedDocument annotationDoc) throws ANoteException
	{
		if(linnaeusTaggerByDocument==null)
			throw new ANoteException("Linnaues Tagger By Document not initialized");
		if(kineticREByDocument==null)
			throw new ANoteException("KineticRE By Document not initialized");
		List<IEntityAnnotation> entities = linnaeusTaggerByDocument.executeDocument(annotationDoc);
		annotationDoc.setEntities(entities);
		List<IEventAnnotation> events = kineticREByDocument.executeDocument(annotationDoc);
		return events;
	}
}
