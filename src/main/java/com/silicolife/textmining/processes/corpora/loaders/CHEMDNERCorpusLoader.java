package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;

import com.silicolife.textmining.core.datastructures.annotation.ner.EntityAnnotationImpl;
import com.silicolife.textmining.core.datastructures.documents.AnnotatedDocumentImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationExternalSourceLinkImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.general.AnoteClass;
import com.silicolife.textmining.core.datastructures.general.ClassPropertiesManagement;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.interfaces.core.annotation.IEntityAnnotation;
import com.silicolife.textmining.core.interfaces.core.corpora.loaders.ICorpusEntityLoader;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IAnnotatedDocument;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;
import com.silicolife.textmining.core.interfaces.core.document.structure.IPublicationField;
import com.silicolife.textmining.core.interfaces.core.general.classe.IAnoteClass;



public class CHEMDNERCorpusLoader implements ICorpusEntityLoader{
	
	private String sufixchemical = "chemical_";
	private List<IPublication> documents;
	private Map<Long,IAnnotatedDocument> documentswithEntities;
	public static String documentsFile = "documentfile";
	public static String annotationFile = "annotationfile";

	public CHEMDNERCorpusLoader()
	{
		this.documents = new ArrayList<>();
		this.documentswithEntities = new TreeMap<>();
	}

	@Override
	public List<IPublication> processFile(File directory, Properties properties)
			throws ANoteException, IOException {
		
		if(validateFile(directory))
		{
			String documentsPathFile = properties.getProperty(documentsFile);
			String annotationPathFile = properties.getProperty(annotationFile);
			String patentID,title,text;
			Set<String> lines = FileHandling.getFileLinesTExt(new File(directory.getAbsolutePath()+"/"+documentsPathFile));
			for(String line:lines)
			{
				String[] data = line.split("\\t");
				patentID = data[0];
				title = data[1];
				text = data[2];
				List<IPublicationExternalSourceLink> publicationExternalIDSource = new ArrayList<>();
				IPublicationExternalSourceLink externalID = new PublicationExternalSourceLinkImpl(patentID, "PATENT");
				publicationExternalIDSource.add(externalID);
				IPublication pub = new PublicationImpl(title, "", "", "", "", "", "", "", "", "", text, "", false, "", "", publicationExternalIDSource , new ArrayList<IPublicationField>(), new ArrayList<IPublicationLabel>());
				getDocuments().add(pub);

			}
			if(annotationPathFile!=null)
			{
				processEntities(directory.getAbsolutePath()+"/"+annotationPathFile);

			}
			return getDocuments();
			
		}
		else
		{
			return null;
		}
	}

	private void processEntities(String annotationPathFile) throws ANoteException, IOException{
		Map<String,IPublication> docExternalID = new HashMap<String, IPublication>();
		Map<String,List<IEntityAnnotation>> docEntities = new HashMap<String, List<IEntityAnnotation>>();
		for( IPublication doc:getDocuments())
		{
			for(IPublicationExternalSourceLink externalid : doc.getPublicationExternalIDSource()){
				docExternalID.put(externalid.getSourceInternalId(),doc);
			}
		}
		File annotationfile = new File(annotationPathFile);
		Set<String> lines = FileHandling.getFileLinesTExt(annotationfile);
		String patent,section,name,classe;
		int startoffset,endoffset;
		for(String line:lines)
		{
			String[] data = line.split("\\t");
			patent = data[0];
			section = data[1];
			startoffset = Integer.valueOf(data[2]);
			endoffset = Integer.valueOf(data[3]);
			name = data[4];
			classe = data[5];
			if(section.equals("T"))
			{
				int docSize = ((IPublication)docExternalID.get(patent)).getAbstractSection().length();
				startoffset = startoffset+docSize+1;
				endoffset = endoffset+docSize+1;
			}
			if(!docEntities.containsKey(patent))
			{
				docEntities.put(patent, new ArrayList<IEntityAnnotation>());
			}
			IAnoteClass klassToAdd = new AnoteClass(sufixchemical+classe);
			IAnoteClass klass = ClassPropertiesManagement.getClassIDOrinsertIfNotExist(klassToAdd);
			IEntityAnnotation ent = new EntityAnnotationImpl(startoffset, endoffset,klass,null, name, false,true, null);
			docEntities.get(patent).add(ent);
		}
		for(IPublication doc: docExternalID.values())
		{
			for(IPublicationExternalSourceLink externalid : doc.getPublicationExternalIDSource()){
				List<IEntityAnnotation> entities = docEntities.get(externalid.getSourceInternalId());
				if(entities==null)
					entities = new ArrayList<IEntityAnnotation>();
				IAnnotatedDocument docResult = new AnnotatedDocumentImpl(doc,null, null, entities);
				getDocumentEntityAnnotations().put(doc.getId(),docResult);
			}
		}
	}

	@Override
	public boolean validateFile(File firectory) {
		return true;
	}

	@Override
	public void stop() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Map<Long, IAnnotatedDocument> getDocumentEntityAnnotations() {
		return documentswithEntities;
	}

	public List<IPublication> getDocuments() {
		return documents;
	}
	
//	public static void main(String[] args) throws SQLException, DatabaseLoadDriverException, IOException, Exception {
//		CHEMDNERCorpusLoader chemdnerLoader = new CHEMDNERCorpusLoader();
//		Properties prop = new Properties();	
//		String corpusName = "chemdner patent sample";
//		CHEMDNERCorpusCreteConfiguration configuration = new CHEMDNERCorpusCreteConfiguration(corpusName, prop );
//		File fileOrDirectory = new File("files/biocreativev/chemdnerpatents/sampleset/chemdner_cemp_sample/chemdner_patents_sample_text.txt");
//		chemdnerLoader.processFileOrDirectory(fileOrDirectory , configuration);
//	}

}
