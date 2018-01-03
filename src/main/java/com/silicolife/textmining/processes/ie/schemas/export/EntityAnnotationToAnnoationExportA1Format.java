package com.silicolife.textmining.processes.ie.schemas.export;

import java.util.ArrayList;
import java.util.List;

import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class EntityAnnotationToAnnoationExportA1Format {
	
	public static List<AnnotationExportA1Format> convert(List<IEntityAnnotation> entities) throws ANoteException
	{
		List<AnnotationExportA1Format> out = new ArrayList<>();
		
		for(IEntityAnnotation entity:entities)
		{
			out.add(convertEntity(entity));
		}
		return out;
	}

	private static AnnotationExportA1Format convertEntity(IEntityAnnotation entity) throws ANoteException {
		String startOffset = String.valueOf(entity.getStartOffset());
		String endOffset = String.valueOf(entity.getEndOffset());;
		String tagEntity = entity.getAnnotationValue();
		String classEntity = new String();;
		if(entity.getClassAnnotation()!=null)
			classEntity = entity.getClassAnnotation().getName();
		String dbNormalization = new String();
		if(entity.getResourceElement()!=null)
			dbNormalization = processDatabaseNormalization(entity.getResourceElement());
		AnnotationExportA1Format out = new AnnotationExportA1Format(startOffset, endOffset, tagEntity, classEntity, dbNormalization);
		return out;
	}

	private static String processDatabaseNormalization(IResourceElement resourceElement) throws ANoteException {
		String out = new String();
		List<IExternalID> externalIds = resourceElement.getExtenalIDs();
		for(IExternalID externalId:externalIds)
		{
			String source = externalId.getSource().getSource();
			String externalIdStream = externalId.getExternalID();
			out = out + source + ":" + externalIdStream + ",";
		}
		if(!out.isEmpty())
			out = out.substring(0, out.length()-1);
		return out;
	}

}
