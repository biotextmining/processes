package com.silicolife.textmining.processes.corpora.exporters;

import java.io.IOException;
import java.io.StringReader;
import java.util.Properties;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCLocation;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class BioC2AnoteConverter {

	public IEntityAnnotation biocAnnotationToEntity(BioCAnnotation annotation) throws ANoteException {
		long id = Long.valueOf(annotation.getID());
		
		BioCLocation spans = annotation.getTotalLocation();
		long start = spans.getOffset();
		long end = spans.getOffset() + spans.getLength();
		
		IAnoteClass klass = new AnoteClass(annotation.getInfons().get("type"));
		IAnoteClass classAnnotation = ClassPropertiesManagement.getClassIDOrinsertIfNotExist(klass);
		IResourceElement resourceElement = null;
		try {
			Long resourceID = Long.valueOf(annotation.getInfons().get("resourceElement-id"));
			resourceElement = InitConfiguration.getDataAccess().getResourceElementByID(resourceID);
		}catch (Exception e) {
			//TODO or resources must be loaded before any annotation
		}
		
		String value = annotation.getText().toString();
		
		boolean abreviation = Boolean.valueOf(annotation.getInfons().get("abreviation"));
		Properties properties = new Properties();
		try {
			properties.load(new StringReader(annotation.getInfons().get("properties")));
		} catch (IOException e) {
			throw new ANoteException(e);
		}
		boolean active = Boolean.valueOf(annotation.getInfons().get("active"));
		boolean validated = Boolean.valueOf(annotation.getInfons().get("validated"));
		
		
		return new EntityAnnotationImpl(id, start, end, classAnnotation, resourceElement, value, abreviation, properties, active, validated);
	}
}
