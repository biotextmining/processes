package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.corpora.CorpusImpl;
import com.silicolife.textmining.core.datastructures.documents.PDFtoText;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.document.labels.IPublicationLabel;

public class CorpusCreationInBatch {

	private Map<String, Long> externalIDAlreadyExistOnDB;
	private Set<String> externalIDAlreadyExistOnCorpus;
	private boolean removepublicationlabels = true;

	public CorpusCreationInBatch(){
		externalIDAlreadyExistOnDB = new HashMap<>();
		externalIDAlreadyExistOnCorpus = new HashSet<>();
	}

	private Map<String, Long> getExternalIDAlreadyExistOnDB() {
		return externalIDAlreadyExistOnDB;
	}

	private Set<String> getExternalIDAlreadyExistOnCorpus() {
		return externalIDAlreadyExistOnCorpus;
	}

	private void setExternalIDAlreadyExistOnDB(Map<String, Long> externalIDAlreadyExistOnDB) {
		this.externalIDAlreadyExistOnDB = externalIDAlreadyExistOnDB;
	}

	private void setExternalIDAlreadyExistOnCorpus(Set<String> externalIDAlreadyExistOnCorpus) {
		this.externalIDAlreadyExistOnCorpus = externalIDAlreadyExistOnCorpus;
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
		
		if(getExternalIDAlreadyExistOnDB().isEmpty()){
			Map<String, Long> alreadyExistOnDB = getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.PUBMED.name());
			alreadyExistOnDB.putAll(getAllPublicationExternalIdFromSource(PublicationSourcesDefaultEnum.pmc.name()));
			setExternalIDAlreadyExistOnDB(alreadyExistOnDB);
		}
		
		if(getExternalIDAlreadyExistOnCorpus().isEmpty()){
			Set<String> alreadyExistOnCorpus = getAllCorpusPublicationExternalIdFromSource(corpus, PublicationSourcesDefaultEnum.PUBMED.name());
			alreadyExistOnCorpus.addAll(getAllCorpusPublicationExternalIdFromSource(corpus, PublicationSourcesDefaultEnum.pmc.name()));
			setExternalIDAlreadyExistOnCorpus(alreadyExistOnCorpus);
		}
		
		CorpusTextType corpusType = corpus.getCorpusTextType();
		Properties corpusProperties = corpus.getProperties();
		if(corpusType == null && corpusProperties.containsKey(GlobalNames.textType)){
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
				if(publication.isPDFAvailable() && getPublicationFullTextOnDatabase(publication).isEmpty())
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

			String pubPMID = PublicationImpl.getPublicationExternalIDForSource(publication,PublicationSourcesDefaultEnum.PUBMED.name());
			String pmcID = PublicationImpl.getPublicationExternalIDForSource(publication,PublicationSourcesDefaultEnum.pmc.name());
			if(!getExternalIDAlreadyExistOnCorpus().contains(pubPMID) 
					&& !getExternalIDAlreadyExistOnCorpus().contains(pmcID)){
				associatePublicationToCorpusOnDatabase(corpus, publication);
				if(pubPMID != null && !pubPMID.isEmpty()){
					getExternalIDAlreadyExistOnCorpus().add(pubPMID);
				}
				if(pmcID != null && !pmcID.isEmpty()){
					getExternalIDAlreadyExistOnCorpus().add(pmcID);
				}
			}
			
		}
	}


	private Map<Long, IPublication> addPublicationsToDatabase(Set<IPublication> publications) throws ANoteException {
		Set<IPublication> documentToadd = new HashSet<>();
		Map<Long, IPublication> documentsInDatabase = new HashMap<>();
		for(IPublication publication:publications){
			String pubPMID = PublicationImpl.getPublicationExternalIDForSource(publication,PublicationSourcesDefaultEnum.PUBMED.name());
			String pmcID =  PublicationImpl.getPublicationExternalIDForSource(publication,PublicationSourcesDefaultEnum.pmc.name());
			if(getExternalIDAlreadyExistOnDB().containsKey(pubPMID)){
				publication.setId(getExternalIDAlreadyExistOnDB().get(pubPMID));
			}else if(getExternalIDAlreadyExistOnDB().containsKey(pmcID)){
				publication.setId(getExternalIDAlreadyExistOnDB().get(pmcID));
			}else{
				if(pubPMID!= null && !pubPMID.isEmpty())
					getExternalIDAlreadyExistOnDB().put(pubPMID, publication.getId());
				if(pmcID!= null && !pmcID.isEmpty())
					getExternalIDAlreadyExistOnDB().put(pmcID, publication.getId());
			}
			

			IPublication pub = getPublicationOnDatabaseByID(publication.getId());
			if(pub==null){
				// remove publication lables
				if(removepublicationlabels )
					publication.setPublicationLabels(new ArrayList<IPublicationLabel>());
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
			String fulltext;
			try {
				fulltext = getPublicationFullTextOnDatabase(pub);
			} catch (ANoteException e) {
				fulltext = null;
			}
			if(fulltext != null && !fulltext.isEmpty()){
				return false;
			}
		}
		return true;
	}
	
	protected String getPublicationFullTextOnDatabase(IPublication publication) throws ANoteException{
		return InitConfiguration.getDataAccess().getPublicationFullText(publication);
	}
	
	protected void updatePublicationFullTextOnfDatabase(IPublication publication) throws ANoteException {
		InitConfiguration.getDataAccess().updatePublicationFullTextContent(publication);
	}


	protected void addPublicationToDatabase(Set<IPublication> documentToadd)throws ANoteException {
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
	
	protected Map<String, Long> getAllPublicationExternalIdFromSource(String source) throws ANoteException {
		return InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(source);
	}
	
	protected Set<String> getAllCorpusPublicationExternalIdFromSource(ICorpus corpus, String source) throws ANoteException {
		return InitConfiguration.getDataAccess().getCorpusPublicationsExternalIDFromSource(corpus, source);
	}
}
