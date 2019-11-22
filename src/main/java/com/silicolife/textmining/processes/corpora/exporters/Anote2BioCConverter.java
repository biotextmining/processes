package com.silicolife.textmining.processes.corpora.exporters;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import com.pengyifan.bioc.BioCAnnotation;
import com.pengyifan.bioc.BioCDocument;
import com.pengyifan.bioc.BioCLocation;
import com.pengyifan.bioc.BioCPassage;
import com.pengyifan.bioc.BioCRelation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;
import com.silicolife.textmining.core.interfaces.resource.IResourceElement;

public class Anote2BioCConverter {

	public static BioCDocument joinAnnotationsToBioCDocument(List<IEntityAnnotation> entities, BioCDocument document) throws ANoteException{
		List<BioCAnnotation> annotations = entitiesToBioCAnnotationList(entities);
		List<BioCPassage> passages = document.getPassages();
		for(BioCPassage passage:passages) {
			int startoffset = passage.getOffset();
			int endoffset = startoffset + passage.getText().toString().length();
			for(BioCAnnotation annotation : annotations) {
				if(annotation.getLocationCount() == 1) {
					BioCLocation location = annotation.getLocations().iterator().next();
					if(location.getOffset() >= startoffset && endoffset >= startoffset + location.getLength())
						passage.addAnnotation(annotation);
				}
				//TODO multi location 
			}
		}
		return document;
	}
	
	public static List<BioCAnnotation> entitiesToBioCAnnotationList(List<IEntityAnnotation> entities) throws ANoteException{
		List<BioCAnnotation> annotations = new ArrayList<>();
		for(IEntityAnnotation entity : entities)
			annotations.add(entityToBioCAnnotation(entity));
		return annotations;
	}
	
	public static BioCAnnotation entityToBioCAnnotation(IEntityAnnotation entity) throws ANoteException {

		IAnoteClass anoteClass = entity.getClassAnnotation();
		IResourceElement resourceElm = entity.getResourceElement();

		BioCAnnotation biocAnn = new BioCAnnotation();
		//id from anote2
		biocAnn.setID(String.valueOf(entity.getId()));
		
		//anote2 data to be able to revert back
		biocAnn.getInfons().put("annotationtype", entity.getAnnotationType());
		biocAnn.getInfons().put("notes", entity.getNotes());
		biocAnn.getInfons().put("active", String.valueOf(entity.isActive()));
		biocAnn.getInfons().put("abreviation", String.valueOf(entity.isAbreviation()));
		biocAnn.getInfons().put("validated", String.valueOf(entity.isValidated()));
		Properties prop = entity.getProperties();
		StringWriter writer = new StringWriter();
		prop.list(new PrintWriter(writer));
		biocAnn.getInfons().put("properties", writer.getBuffer().toString());
		biocAnn.getInfons().put("resourceElement-id", String.valueOf(resourceElm.getId()));
		biocAnn.getInfons().put("resourceElement-term", resourceElm.getTerm());
		
		//ner class
		biocAnn.getInfons().put("type", anoteClass.getName());
		
		//external ids
		List<IExternalID> externalIds = resourceElm.getExtenalIDs();
		StringBuffer sb = new StringBuffer();
		for(IExternalID externalId : externalIds) {
			if(sb.length() != 0)
				sb.append(",");
			sb.append(externalId.getSource().getSource());
			sb.append(":");
			sb.append(externalId.getExternalID());
		}
		biocAnn.getInfons().put("externalIDs", sb.toString());
		
		
		biocAnn.getLocations().add(new BioCLocation((int)entity.getStartOffset(), (int)entity.getEndOffset()-(int)entity.getStartOffset()));
		biocAnn.setText(entity.getAnnotationValue());
		return biocAnn;
	}
	
	public static BioCRelation eventToBioCRelation(IEventAnnotation event) {
		
		List<IEntityAnnotation> left = event.getEntitiesAtLeft();
		List<IEntityAnnotation> right = event.getEntitiesAtRight();
		//TODO review of event structure - must be refactored to enable the verb but to have a more "event like" structure
		BioCRelation biorel = new BioCRelation();
		return biorel;
	}
	
	public static BioCDocument publicationToBioCDocument(IPublication publication) {
		BioCDocument document = new BioCDocument();
		document.setID(String.valueOf(publication.getId()));
		
		BioCPassage titlePass = new BioCPassage();
		titlePass.setText(publication.getTitle());
		titlePass.setOffset(0);
		titlePass.getInfons().put("section", "Title");
		document.addPassage(titlePass);
		
		BioCPassage abstractPass = new BioCPassage();
		abstractPass.setText(publication.getAbstractSection());
		abstractPass.setOffset(publication.getTitle().length()+1);
		abstractPass.getInfons().put("section", "Abstract");
		document.addPassage(abstractPass);
		
		BioCPassage fulltextPass = new BioCPassage();
		fulltextPass.setText(publication.getFullTextContent());
		fulltextPass.setOffset(0);
		abstractPass.getInfons().put("section", "Fulltext");
		document.addPassage(fulltextPass);
		
		document.getInfons().put("authors", publication.getAuthors());
		document.getInfons().put("category", publication.getCategory());
		document.getInfons().put("yeardate", publication.getYeardate());
		document.getInfons().put("fulldate", publication.getFulldate());
		document.getInfons().put("status", publication.getStatus());
		document.getInfons().put("journal", publication.getJournal());
		document.getInfons().put("volume", publication.getVolume());
		document.getInfons().put("issue", publication.getIssue());
		document.getInfons().put("pages", publication.getPages());
		document.getInfons().put("externalLink", publication.getExternalLink());
		document.getInfons().put("freeFullText", String.valueOf(publication.isFreeFullText()));
		document.getInfons().put("notes", publication.getNotes());
		document.getInfons().put("relativePath", publication.getRelativePath());
		document.getInfons().put("sourceURL", publication.getSourceURL());

		return document;
	}
}
