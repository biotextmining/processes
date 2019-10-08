package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.steps;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.process.IE.re.IREProcessByDocument;
import com.silicolife.textmining.processes.ie.re.kineticre.configuration.IREKineticREConfiguration;
import com.silicolife.textmining.processes.ie.re.kineticre.core.KineticRE;

public class KineticREByDocument extends KineticRE implements IREProcessByDocument{
	
	private IREKineticREConfiguration kineticREConfiguration;
	
	public KineticREByDocument(IREKineticREConfiguration kineticREConfiguration)
	{
		this.kineticREConfiguration=kineticREConfiguration;
	}

	public List<IEventAnnotation> executeDocument(IAnnotatedDocument annotationDoc) throws ANoteException
	{
		List<IEntityAnnotation> entitiesdoc = annotationDoc.getEntitiesAnnotations();
		List<IEventAnnotation> eventsDoc = processDocument(kineticREConfiguration, entitiesdoc, annotationDoc);
		return eventsDoc;
	}
	

}
