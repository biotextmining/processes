package com.silicolife.textmining.processes.ie.ner.datatstructures;

import java.util.List;

import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.process.IE.IIEProcess;

public class NERPosProcessAddEntitiesImpl implements INERPosProccessAddEntities{

	@Override
	public void addAnnotatedDocumentEntities(IIEProcess process, IPublication document,
			List<IEntityAnnotation> newEntityAnnotations) throws ANoteException {
		InitConfiguration.getDataAccess().addProcessDocumentEntitiesAnnotations(process, document, newEntityAnnotations);		
	}

}
