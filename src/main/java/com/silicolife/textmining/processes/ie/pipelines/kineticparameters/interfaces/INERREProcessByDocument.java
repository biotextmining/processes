package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;

public interface INERREProcessByDocument {
	
	public List<IEventAnnotation> execute(IAnnotatedDocument annotationDoc) throws ANoteException;

}
