package com.silicolife.textmining.processes.ie.ner.datatstructures;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;

/**
 * Interface that represent what do do after getting Process Document annnotation.
 *(Examples in case of create or resume a new process should only add annotation to database however when the processes is to update entities,
related with lexical resources changes, the previous annotations should be deleted and the newEntityAnnotations added
 * 
 * @author Hugo Costa
 *
 */
public interface INERPosProccessAddEntities {
	
	/**
	 * Method to decide what to do with new Annotations from process
	 * 
	 * @param process
	 * @param document
	 * @param newEntityAnnotations
	 * @throws ANoteException
	 */
	public void addAnnotatedDocumentEntities(IIEProcess process,IPublication document,List<IEntityAnnotation> newEntityAnnotations) throws ANoteException;

}
