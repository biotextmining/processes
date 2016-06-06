package com.silicolife.textmining.processes.corpora.loaders.pdf.utils;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.documents.PublicationEditableImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.datastructures.documents.PublicationSourcesDefaultEnum;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.IPublicationEditable;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;

public class PublictionMetaInfoFinder {
	
	private boolean stop = false;
	private long startTime;
	private static boolean useDatabseInformation = true;
	
	public PublictionMetaInfoFinder()
	{
		
	}

	/**
	 * Get Meta information by searching in Database ( by PMID) or searching DOI within PDF or using heuristics 
	 * 
	 * @param set
	 * @param progress
	 * @return
	 * @throws InternetConnectionProblemException
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public List<IPublicationEditable> getMetaInfoForFiles(Set<IPublication> documentSet) throws InternetConnectionProblemException
	{
		stop = false;
		startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int position = 0;
		int max = documentSet.size();
		List<IPublicationEditable> listDocuments = new ArrayList<IPublicationEditable>();
		// Find PMID and DOI in Database
		if(useDatabseInformation && !stop)
		{
			try {
				Map<String, Long> pmidDocID = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.PUBMED.name());
				Map<String, Long> doiDocID = InitConfiguration.getDataAccess().getAllPublicationsExternalIDFromSource(PublicationSourcesDefaultEnum.DOI.name());

				// Searching in database for already exist PMIDs
				List<IPublicationEditable> listDocumentPMID = searchInformationOnDatabase(documentSet,PublicationSourcesDefaultEnum.PUBMED.name(),pmidDocID,max);
				listDocuments.addAll(listDocumentPMID);
				// Searching in database for already exist DOI
				List<IPublicationEditable> listDocumentDOI= searchInformationOnDatabase(documentSet,PublicationSourcesDefaultEnum.DOI.name(),doiDocID,max);
				listDocuments.addAll(listDocumentDOI);
			} catch (ANoteException e) {
			} 
		}
		position = max - documentSet.size();
		// Find Pubmed Information for PMID or DOI
		listDocuments.addAll(searchMetaInformation(documentSet, position,max ));		
		return listDocuments;
	}


	private List<IPublicationEditable> searchMetaInformation(Set<IPublication> documentSet, int postion,int max) throws InternetConnectionProblemException {
		List<IPublicationEditable> listDocuments = new ArrayList<IPublicationEditable>();
		for(IPublication pub : documentSet)
		{
			// Don't have PMID or PMID are invalid
			IPublicationEditable edited = new PublicationEditableImpl(pub);
			// looking for DOI
			String doi = PublicationImpl.getPublicationExternalIDForSource(pub, "DOI");
			String pubmed = PublicationImpl.getPublicationExternalIDForSource(pub, "Pubmed");

			if((doi == null || doi.isEmpty()) && !stop)
			{		
				try {
					// Find Meta Information using DOI (if exists)
					PDFBoxMetaDataFinder.getPublicationMetaInformation(edited);
				} catch (IOException e) {
					
				}
			} 
			else if((pubmed!=null && !pubmed.isEmpty()) && !stop)// For PMID
			{
				PMIDSearch.getPublicationByPMID(edited);	
			}
			else
			{
				// Do nothing
			}
			if(edited.getTitle().isEmpty())
				edited.setTitle(pub.getSourceURL());
			try {
				edited.setFullTextFromURL(pub.getSourceURL());
			} catch (IOException e) {
			}
			listDocuments.add(edited);
			memoryAndProgress(this.startTime, max, postion);
			postion++;
		}
		return listDocuments;
	}

	private List<IPublicationEditable> searchInformationOnDatabase(Set<IPublication> documentSet,String source,Map<String, Long> sourceIDDocID, int max) throws ANoteException {
		List<IPublication> idsToremove = new ArrayList<IPublication>();
		List<IPublicationEditable> listDocuments = new ArrayList<IPublicationEditable>();;
		int position = 0;
		for(IPublication pub:documentSet)
		{
			String sourceID = PublicationImpl.getPublicationExternalIDForSource(pub, source);
			if(sourceID!=null && !sourceID.isEmpty() && sourceIDDocID.containsKey(sourceID))
			{
				long docID = sourceIDDocID.get(sourceID);
				IPublication pubSearchOnDatabse = InitConfiguration.getDataAccess().getPublication(docID);
				if(pubSearchOnDatabse!=null)
				{
					IPublicationEditable pubEdit = new PublicationEditableImpl(pub);
					pubEdit.setTitle(pubSearchOnDatabse.getTitle());
					pubEdit.setAbstract(pubSearchOnDatabse.getAbstractSection());
					pubEdit.setAuthors(pubSearchOnDatabse.getAuthors());
					pubEdit.setExternalLink(pubSearchOnDatabse.getExternalLink());
					pubEdit.setJournal(pubSearchOnDatabse.getJournal());
					pubEdit.setYearDate(pubSearchOnDatabse.getYeardate());
					pubEdit.setSourceURL(pub.getSourceURL());
					try {
						pubEdit.setFullTextFromURL(pub.getSourceURL());
					} catch (IOException e) {
					}
					listDocuments.add(pubEdit);
					idsToremove.add(pub);
					memoryAndProgress(this.startTime,max, position);
					position++;
				}

			}
		}
		for(IPublication docID:idsToremove)
		{
			documentSet.remove(docID);
		}
		return listDocuments;
	}

	protected void memoryAndProgress(long starttime,int total, int step) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		Runtime.getRuntime().gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	public void setstop() {
		stop = true;		
	}
}
