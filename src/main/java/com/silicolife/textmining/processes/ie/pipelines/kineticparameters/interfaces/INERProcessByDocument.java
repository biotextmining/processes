package com.silicolife.textmining.processes.ie.pipelines.kineticparameters.interfaces;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;

public interface INERProcessByDocument {
	
	public List<IEntityAnnotation> executeDocument(IAnnotatedDocument annotatedDocument) throws ANoteException;

}
