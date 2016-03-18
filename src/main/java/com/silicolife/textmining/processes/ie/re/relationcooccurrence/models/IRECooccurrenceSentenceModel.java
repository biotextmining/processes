package com.silicolife.textmining.processes.ie.re.relationcooccurrence.models;

import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;

public interface IRECooccurrenceSentenceModel {
	public List<IEventAnnotation> processDocumetAnnotations(IAnnotatedDocument annotationdDocument,List<IEntityAnnotation> listEntitiesSortedByOffset) throws ANoteException;	
	public String getDescription();	
	public String getImagePath();
	public String toString();
	public String getUID();
}
