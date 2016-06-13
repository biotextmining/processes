package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.corpora.CorpusImpl;
import com.silicolife.textmining.core.datastructures.documents.PDFtoText;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;

public class CorpusCreationInBatch {

	public CorpusCreationInBatch(){
		
	}
	
	public ICorpus startCorpusCreation(ICorpusCreateConfiguration configuration) throws ANoteException{
		
		Properties properties = configuration.getProperties();
		CorpusTextType corpusType = configuration.getCorpusTextType();
		if(corpusType != null){
			properties.put(GlobalNames.textType, CorpusTextType.convertCorpusTetTypeToString(configuration.getCorpusTextType()));
		}
		
		ICorpus corpus = new CorpusImpl(configuration.getCorpusName(), configuration.getCorpusNotes(), configuration.getProperties());
		createCorpusOnDatabase(corpus);
		return corpus;
	}
	
	public void addPublications(ICorpus corpus, Set<IPublication> publications) throws ANoteException, IOException{
		
		CorpusTextType corpusType = null;
		Properties corpusProperties = corpus.getProperties();
		if(corpusProperties.containsKey(GlobalNames.textType)){
			corpusType = CorpusTextType.convertStringToCorpusType(corpusProperties.getProperty(GlobalNames.textType));
		}
		
		Map<Long, IPublication> alreadyExistentPublications = addPublicationsToDatabase(publications);
		

		for(IPublication publication:publications){
			
			if(corpusType  != null && 
					(corpusType.equals(CorpusTextType.Hybrid) || corpusType.equals(CorpusTextType.FullText)))
			{
				// IF PDF is not available and source URL is a file put file in directory and update Full text COntent
				if(publication.getSourceURL()!=null && !publication.isPDFAvailable())
				{
					publication.addPDFFile(new File(publication.getSourceURL()));
					// update relative path
					InitConfiguration.getDataAccess().updatePublication(publication);
				}
				// PDF is availbale and Full text are not available yet
				if(publication.isPDFAvailable() && publication.getFullTextContent().isEmpty())
				{
					String saveDocDirectoty = (String) PropertiesManager.getPManager().getProperty(GeneralDefaultSettings.PDFDOCDIRECTORY);
					// Get PDF to text from PDF file
					String fullTextContent = PDFtoText.convertPDFDocument(saveDocDirectoty + "//" + publication.getRelativePath());
					publication.setFullTextContent(fullTextContent);
					updatePublicationFullTextOnfDatabase(publication);
				}
				// If pub don't have fulltext and publication has a full text inserted from other system. Then it will be added
				else if(changefulltext(publication, alreadyExistentPublications)){
					updatePublicationFullTextOnfDatabase(publication);
				}
			}

			associatePublicationToCorpusOnDatabase(corpus, publication);
		}
	}


	private Map<Long, IPublication> addPublicationsToDatabase(Set<IPublication> publications) throws ANoteException {
		List<IPublication> documentToadd = new ArrayList<IPublication>();
		Map<Long, IPublication> documentsInDatabase = new HashMap<>();
		for(IPublication publication:publications){
			IPublication pub = getPublicationOnDatabaseByID(publication.getId());
			if(pub==null){
				documentToadd.add(publication);
			}else{
				documentsInDatabase.put(pub.getId(),pub);
			}
		}
		addPublicationToDatabase(documentToadd);
		documentToadd.clear();
		return documentsInDatabase;
	}
	
	private boolean changefulltext(IPublication publication, Map<Long, IPublication> alreadyExistentPublications){
		if(publication.getFullTextContent() == null || publication.getFullTextContent().isEmpty()){
			return false;
		}
		IPublication pub = null;
		if(alreadyExistentPublications.containsKey(publication.getId())){
			pub = alreadyExistentPublications.get(publication.getId());
		}
		if(pub  != null){
			if(pub.getFullTextContent() != null && !pub.getFullTextContent().isEmpty()){
				return false;
			}
		}
		return true;
	}
	
	protected void updatePublicationFullTextOnfDatabase(IPublication publication) throws ANoteException {
		InitConfiguration.getDataAccess().updatePublicationFullTextContent(publication);
	}


	protected void addPublicationToDatabase(List<IPublication> documentToadd)throws ANoteException {
		InitConfiguration.getDataAccess().addPublications(documentToadd );
	}


	protected IPublication getPublicationOnDatabaseByID(Long publictionID)throws ANoteException {
		return InitConfiguration.getDataAccess().getPublication(publictionID);
	}

	protected void createCorpusOnDatabase(ICorpus newCorpus)throws ANoteException {
		InitConfiguration.getDataAccess().createCorpus(newCorpus);
	}

	protected void associatePublicationToCorpusOnDatabase(ICorpus corpus, IPublication publication) throws ANoteException {
		InitConfiguration.getDataAccess().addCorpusPublication(corpus, publication);
	}
}
