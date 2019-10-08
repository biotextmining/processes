package com.silicolife.textmining.processes.corpora.merge;

import java.util.Iterator;
import java.util.List;
import java.util.Properties;

import com.silicolife.textmining.core.datastructures.corpora.CorpusImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IDocumentSet;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationExternalSourceLink;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;

public class MergeCorpusImpl {

	private List<ICorpus> corpusToMerge;
	private String description;
	private String notes;
	private Properties properties;
	private static int publicationbuffersize = 1000;

	public MergeCorpusImpl(String corpusdescription, String corpusNotes, Properties corpusProperties, List<ICorpus> corpusToMerge) {
		this.description = corpusdescription;
		this.notes = corpusNotes;
		this.properties = corpusProperties;
		this.corpusToMerge = corpusToMerge;
	}
	
	public ICorpus generateMergedCorpus() throws ANoteException {
		if(corpusToMerge.size() <1)
			throw new ANoteException("Must be 2 or more corpus to merge!");
		ICorpus outCorpus = new CorpusImpl(description, notes, properties);
		saveCorpus(outCorpus);
		
		Long publicationCount=0L;
		Iterator<ICorpus> itCorpus = corpusToMerge.iterator();
		while(itCorpus.hasNext()) {
			ICorpus tocopy = itCorpus.next();
		
			if(publicationCount == 0) 
				copyCorpus(outCorpus, tocopy);
			else 
				copyCorpusCheckingExternalSources(outCorpus, tocopy);
			publicationCount = InitConfiguration.getDataAccess().getCorpusPublicationsCount(outCorpus);
			
		}
		return outCorpus;
	}
		
	private void copyCorpus(ICorpus outCorpus, ICorpus tocopy) throws ANoteException {
		Long count = getCorpusPublicationsCount(tocopy);
		int i = 0;
		while(i<=count) {
			IDocumentSet publications = getPublications(tocopy, i);
			Iterator<IPublication> itpub = publications.iterator();
			while(itpub.hasNext()) {
				IPublication pub = itpub.next();
				addpublication(outCorpus, pub);
			}
		
			i+=publicationbuffersize;
			System.out.println(i);
		}
	}
	
	private void copyCorpusCheckingExternalSources(ICorpus outCorpus, ICorpus tocopy) throws ANoteException {
		Long count = getCorpusPublicationsCount(tocopy);
		int i = 0;
		while(i<=count) {
			IDocumentSet publications = getPublications(tocopy, i);
			Iterator<IPublication> itpub = publications.iterator();
			while(itpub.hasNext()) {
				IPublication pub = itpub.next();

				boolean found = checkIfPublicationExistsByExternalId(outCorpus, pub.getPublicationExternalIDSource());
				if(!found)
					addpublication(outCorpus, pub);
			}
			i+=publicationbuffersize;
			System.out.println(i);
		}
	}

	private boolean checkIfPublicationExistsByExternalId(ICorpus outCorpus, List<IPublicationExternalSourceLink> externalIds) throws ANoteException {
		boolean found = false;
		Iterator<IPublicationExternalSourceLink> itExternal = externalIds.iterator();
		while(itExternal.hasNext() && !found) {
			IPublicationExternalSourceLink externalId = itExternal.next();
			List<Long> pubIds = InitConfiguration.getDataAccess().getCorpusPublicationsFromExternalID(outCorpus, externalId.getSource(), externalId.getSourceInternalId());
			if(!pubIds.isEmpty()) 
				found = true;
		}
		return found;
	}
	
	private void saveCorpus(ICorpus outCorpus) throws ANoteException {
		InitConfiguration.getDataAccess().createCorpus(outCorpus);
	}
	
	private IDocumentSet getPublications(ICorpus corpus, int pubindex) throws ANoteException{
		return InitConfiguration.getDataAccess().getCorpusPublicationsPaginated(corpus, pubindex, publicationbuffersize);
	}
	
	private Long getCorpusPublicationsCount(ICorpus corpus) throws ANoteException {
		return InitConfiguration.getDataAccess().getCorpusPublicationsCount(corpus);
	}
	
	private void addpublication(ICorpus outCorpus, IPublication publication) throws ANoteException {
		InitConfiguration.getDataAccess().addCorpusPublication(outCorpus, publication);
	}

	

}
