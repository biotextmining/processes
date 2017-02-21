package com.silicolife.textmining.processes.corpora.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.io.filefilter.SuffixFileFilter;

import com.silicolife.textmining.core.datastructures.annotation.AnnotationType;
import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventAnnotationImpl;
import com.silicolife.textmining.core.datastructures.annotation.re.EventPropertiesImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.IEventAnnotation;
import com.silicolife.textmining.core.interfaces.core.annotation.re.IEventProperties;
import com.silicolife.textmining.core.interfaces.core.corpora.loaders.ICorpusEventAnnotationLoader;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;

public class BioNLPA2A1CorpusLoader implements ICorpusEventAnnotationLoader{

	private List<IPublication> documents;
	private Map<Long, IAnnotatedDocument> entities;
	private Map<Long, IAnnotatedDocument> events;

	public BioNLPA2A1CorpusLoader(){
		documents = new ArrayList<>();
		entities = new HashMap<>();
		events = new HashMap<>();
	}

	@Override
	public List<IPublication> processFile(File directory, Properties properties)
			throws ANoteException, IOException {
		if(validateFile(directory)){

			Map<String, IPublication> nameFileToPublication = new HashMap<>();
			Map<String, Map<String, IEntityAnnotation>> nameFileToAnnotations = new HashMap<>();

			extractPublications(directory, nameFileToPublication);

			extractNERAnnotations(directory, nameFileToPublication, nameFileToAnnotations);

			extractEventAnnotations(directory, nameFileToPublication, nameFileToAnnotations);

			return getDocuments();
		}else
			return null;

	}

	@Override
	public boolean validateFile(File firectory) {
		if(!firectory.isDirectory())
			return false;

		if(getPublicationFiles(firectory).length == 0)
			return false;
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub

	}

	public List<IPublication> getDocuments() {
		return documents;
	}

	@Override
	public Map<Long, IAnnotatedDocument> getDocumentEntityAnnotations() {
		return entities;
	}

	@Override
	public Map<Long, IAnnotatedDocument> getDocumentEventAnnotations() {
		return events;
	}

	private File[] getPublicationFiles(File directory){
		FilenameFilter filter = new SuffixFileFilter(".txt");
		return directory.listFiles(filter);
	}

	private File[] getEntityFiles(File directory){
		FilenameFilter filter = new SuffixFileFilter(".a1");
		return directory.listFiles(filter);
	}

	private File[] getEventFiles(File directory){
		FilenameFilter filter = new SuffixFileFilter(".a2");
		return directory.listFiles(filter);
	}

	private IPublication convertFileToPublication(File publicationFile) throws IOException{

		StringBuilder publicationExcerpt = new StringBuilder();
		BufferedReader br = new BufferedReader(new FileReader(publicationFile));
		String line;
		while((line = br.readLine()) != null){
			if(!line.trim().isEmpty()){
				line = line.replaceAll("\\s", " ");
				line = line.replaceAll("\\n", " ");
				line = line.replaceAll("\\r", " ");
				publicationExcerpt.append(line+" ");
			}
		}
		br.close();

		IPublication publication = new PublicationImpl();
		publication.setAbstractSection(publicationExcerpt.toString());

		List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<>();
		String name = publicationFile.getName();
		if(name.toLowerCase().contains("pmid")){
			publicationExternalIDSource.add(new PublicationExternalSourceLinkImpl(name.replaceAll("\\D+",""), PublicationSourcesDefaultEnum.PUBMED.toString()));
		}else{
			publicationExternalIDSource.add(new PublicationExternalSourceLinkImpl(name.replaceAll(".txt", ""), "BioNLP"));
		}

		publication.setPublicationExternalIDSource(publicationExternalIDSource);
		return publication;
	}

	private Map<String, IEntityAnnotation> convertFileToNERAnnotations(File nerAnnotationFile) throws IOException, ANoteException{
		Map<String, IEntityAnnotation> idInFileToAnnotation = new HashMap<>();
		BufferedReader br = new BufferedReader(new FileReader(nerAnnotationFile));
		String line;
		while((line = br.readLine()) != null){
			if(!line.trim().isEmpty()){
				String[] columns = line.split("\t");
				String id = columns[0];
				idInFileToAnnotation.put(id, convertColumnsToNEREntity(columns));
			}
		}
		br.close();
		return idInFileToAnnotation;
	}

	private IEntityAnnotation convertColumnsToNEREntity(String[] columns) throws ANoteException{
		String[] annotationColumns = columns[1].split("\\s");
		String annotationClass = annotationColumns[0];
		String startOffset = annotationColumns[1];
		String endOffset = annotationColumns[2];
		String annotationValue = columns[2];
		IAnoteClass classAnnotation = new AnoteClass(annotationClass);
		classAnnotation = ClassPropertiesManagement.getClassIDOrinsertIfNotExist(classAnnotation);
		long start = Long.valueOf(startOffset);
		long end = Long.valueOf(endOffset);
		return new EntityAnnotationImpl(start, end, classAnnotation, null, annotationValue, false, true,new Properties());
	}

	private Map<String, IEntityAnnotation> cloneNEREntitiesWithNewId(Map<String, IEntityAnnotation> entitiesToClone){
		Map<String, IEntityAnnotation> entitiesToDuplicate = new HashMap<>();
		for(String id : entitiesToClone.keySet()){
			IEntityAnnotation entity = entitiesToClone.get(id);
			IEntityAnnotation newEntity = entity.clone();
			newEntity.generateNewId();
			entitiesToDuplicate.put(id, newEntity);
		}
		return entitiesToDuplicate;
	}

	private void extractPublications(File directory, Map<String, IPublication> nameFileToPublication)
			throws IOException {
		for(File pubFile : getPublicationFiles(directory)){
			IPublication publication = convertFileToPublication(pubFile);
			nameFileToPublication.put(pubFile.getName().replaceAll(".txt", ""), publication);
			getDocuments().add(publication);
		}
	}

	private void extractNERAnnotations(File directory, Map<String, IPublication> nameFileToPublication,
			Map<String, Map<String, IEntityAnnotation>> nameFileToAnnotations) throws IOException, ANoteException {
		for(File entitiesFile : getEntityFiles(directory)){
			String pubFileName = entitiesFile.getName().replaceAll(".a1", "");
			if(nameFileToPublication.containsKey(pubFileName)){
				IPublication publication = nameFileToPublication.get(pubFileName);
				Map<String, IEntityAnnotation> idInFileToAnnotation = convertFileToNERAnnotations(entitiesFile);
				nameFileToAnnotations.put(pubFileName, idInFileToAnnotation);
				getDocumentEntityAnnotations().put(publication.getId(), new AnnotatedDocumentImpl(publication,null, null,new ArrayList<>(idInFileToAnnotation.values())));
			}
		}
	}

	private void extractEventAnnotations(File directory, Map<String, IPublication> nameFileToPublication,
			Map<String, Map<String, IEntityAnnotation>> nameFileToAnnotations)
					throws FileNotFoundException, IOException, ANoteException {
		for(File eventsFile : getEventFiles(directory)){
			String pubFileName = eventsFile.getName().replaceAll(".a2", "");
			if(nameFileToPublication.containsKey(pubFileName) && nameFileToAnnotations.containsKey(pubFileName)){
				List<IEventAnnotation> events = new ArrayList<>();
				IPublication publication = nameFileToPublication.get(pubFileName);
				Map<String, IEntityAnnotation> idInFileToAnnotation = nameFileToAnnotations.get(pubFileName);
				idInFileToAnnotation = cloneNEREntitiesWithNewId(idInFileToAnnotation);
				BufferedReader br = new BufferedReader(new FileReader(eventsFile));
				String line;
				while((line = br.readLine()) != null){
					if(!line.trim().isEmpty()){
						String[] columns = line.split("\t");

						if(columns.length<3){
							if(columns[0].startsWith("*")){
								saveAstresticEvents(events, idInFileToAnnotation, columns);
							}else if(columns[0].startsWith("E")){
								//events from events are ignored
								saveEventIntoEventsList(events, idInFileToAnnotation, columns);
							}else if(columns[0].startsWith("R")){
								saveRelationIntoEventsList(events, idInFileToAnnotation, columns);
							}else if(columns[0].startsWith("M")){
								//to be added polarity and speculation
							}

						}else if(columns[0].startsWith("T")){
							idInFileToAnnotation.put(columns[0], convertColumnsToNEREntity(columns));
						}
					}
				}
				br.close();
				getDocumentEventAnnotations().put(publication.getId(), new AnnotatedDocumentImpl(publication,null, null,new ArrayList<>(idInFileToAnnotation.values()), events));
			}
		}
	}

	private void saveRelationIntoEventsList(List<IEventAnnotation> events,
			Map<String, IEntityAnnotation> idInFileToAnnotation, String[] columns) {
		String[] eventColumns = columns[1].split("\\s");
		String[] entity1values = eventColumns[0].split(":");
		String[] entity2values = eventColumns[1].split(":");
		if(idInFileToAnnotation.containsKey(entity1values[1]) && idInFileToAnnotation.containsKey(entity2values[1])){
			IEntityAnnotation entity1 = idInFileToAnnotation.get(entity1values[1]);
			IEntityAnnotation entity2 = idInFileToAnnotation.get(entity2values[1]);
			saveEntitiesIntoEvent(columns[1], entity1, entity2, events);
		}
	}

	private void saveEventIntoEventsList(List<IEventAnnotation> events,
			Map<String, IEntityAnnotation> idInFileToAnnotation, String[] columns) {
		String[] eventColumns = columns[1].split("\\s");
		String[] trigguervalues = eventColumns[0].split(":");
		String[] rolevalues = eventColumns[1].split(":");
		if(idInFileToAnnotation.containsKey(trigguervalues[1]) && idInFileToAnnotation.containsKey(rolevalues[1])){
			IEntityAnnotation trigger = idInFileToAnnotation.get(trigguervalues[1]);
			IEntityAnnotation entity = idInFileToAnnotation.get(rolevalues[1]);
			saveEntitiesIntoEvent(rolevalues[0], trigger, entity, events);
			if(eventColumns.length>2){
				String[] role2values = eventColumns[2].split(":");
				if(idInFileToAnnotation.containsKey(role2values[1])){
					IEntityAnnotation entity2 = idInFileToAnnotation.get(role2values[1]);
					saveEntitiesIntoEvent(role2values[0], trigger, entity2, events);
				}
			}
		}
	}

	private void saveAstresticEvents(List<IEventAnnotation> events, Map<String, IEntityAnnotation> idInFileToAnnotation,
			String[] columns) {
		String[] eventColumns = columns[1].split("\\s");
		IEntityAnnotation annotation1 = null;
		IEntityAnnotation annotation2 = null;
		if(idInFileToAnnotation.containsKey(eventColumns[1])){
			annotation1 = idInFileToAnnotation.get(eventColumns[1]);
		}
		if(idInFileToAnnotation.containsKey(eventColumns[2])){
			annotation2 = idInFileToAnnotation.get(eventColumns[2]);
		}
		if(annotation1 != null && annotation2 != null){
			saveEntitiesIntoEvent(eventColumns[0], annotation1, annotation2, events);
		}else if(annotation1 != null){
			List<IEntityAnnotation> left = new ArrayList<>();
			left.add(annotation2);
			IEventProperties eventProp = new EventPropertiesImpl();
			Properties properties = new Properties();
			properties.put(eventColumns[0], eventColumns[0]);
			eventProp.setProperties(properties);
			events.add(new EventAnnotationImpl(-1,-1,AnnotationType.re.name(),left,null, null, eventProp,true));
		}else if(annotation2 != null){
			IEventProperties eventProp = new EventPropertiesImpl();
			Properties properties = new Properties();
			properties.put(eventColumns[0], eventColumns[0]);
			eventProp.setProperties(properties);
			List<IEntityAnnotation> left = new ArrayList<>();
			left.add(annotation2);
			events.add(new EventAnnotationImpl(-1,-1,AnnotationType.re.name(),left,null,null,eventProp, true));
		}
	}

	private void saveEntitiesIntoEvent(String eventClass, IEntityAnnotation annotation1, IEntityAnnotation annotation2, List<IEventAnnotation> events){
		IEventProperties eventProp = new EventPropertiesImpl();
		Properties properties = new Properties();
		properties.put(eventClass, eventClass);
		eventProp.setProperties(properties);
		if(annotation2.getStartOffset()>annotation1.getEndOffset()){
			List<IEntityAnnotation> left = new ArrayList<>();
			left.add(annotation1);
			List<IEntityAnnotation> right = new ArrayList<>();
			right.add(annotation2);

			events.add(new EventAnnotationImpl(-1,-1,AnnotationType.re.name(),left,right,null,eventProp, true));
		}else{
			List<IEntityAnnotation> left = new ArrayList<>();
			left.add(annotation2);
			List<IEntityAnnotation> right = new ArrayList<>();
			right.add(annotation1);
			events.add(new EventAnnotationImpl(-1,-1,AnnotationType.re.name(),left,right,null,eventProp, true));
		}
	}
	
}
