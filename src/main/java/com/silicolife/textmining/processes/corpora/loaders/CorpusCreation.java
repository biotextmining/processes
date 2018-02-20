package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Date;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.corpora.CorpusImpl;
import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.DataProcessStatusEnum;
import com.silicolife.textmining.core.datastructures.dataaccess.database.dataaccess.implementation.utils.ProcessStatusResourceTypesEnum;
import com.silicolife.textmining.core.datastructures.documents.PDFtoText;
import com.silicolife.textmining.core.datastructures.general.DataProcessStatusImpl;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.report.corpora.CorpusCreateReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.general.IDataProcessStatus;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;

public class CorpusCreation {

	public CorpusCreation()
	{

	}
	
	
	public ICorpusCreateReport createCorpusByIds(ICorpusCreateConfiguration configuration) throws ANoteException
	{
			Properties properties = configuration.getProperties();
			properties.put(GlobalNames.textType, CorpusTextType.convertCorpusTetTypeToString(configuration.getCorpusTextType()));
			ICorpus newCorpus = new CorpusImpl(configuration.getCorpusName(), configuration.getCorpusNotes(), configuration.getProperties());
			createCorpusOnDatabase(newCorpus);
			InitConfiguration.getDataAccess().updateCorpusStatus(newCorpus, false);
			Set<Long> documentIds = configuration.getDocumentsIDs();
			int step = 0;
			int total = documentIds.size();
			IDataProcessStatus dataprocessStatus = new DataProcessStatusImpl(newCorpus.getId(),ProcessStatusResourceTypesEnum.corpus);
			InitConfiguration.getDataAccess().addDataProcessStatus(dataprocessStatus);
			dataprocessStatus.setStatus(DataProcessStatusEnum.running);
			for(Long publicationId:documentIds) {
				IPublication publication = InitConfiguration.getDataAccess().getPublication(publicationId);
				if(publication!=null)
				{
					InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publication);
				}
				step++;
				float progress = memoryAndProgressOut(step,total);
				if(progress!=-1) {
					dataprocessStatus.setProgress(progress);
					dataprocessStatus.setUpdateDate(new Date());
					InitConfiguration.getDataAccess().updateDataProcessStatus(dataprocessStatus);
				}
				memoryAndProgress(step,total);
			}
			InitConfiguration.getDataAccess().updateCorpusStatus(newCorpus, true);
			ICorpusCreateReport report = new CorpusCreateReportImpl(newCorpus, configuration.getCorpusTextType(),configuration.getDocumentsIDs().size());
			dataprocessStatus.setStatus(DataProcessStatusEnum.finished);
			dataprocessStatus.setProgress(100);
			Date finishDate = new Date();
			dataprocessStatus.setFinishedDate(finishDate);
			dataprocessStatus.setUpdateDate(finishDate);
			dataprocessStatus.setReport("Corpus "+ newCorpus.getDescription()+" "+configuration.getDocumentsIDs().size()+ " documents added");
			InitConfiguration.getDataAccess().updateDataProcessStatus(dataprocessStatus);
			return report;
	}
	
	public ICorpusCreateReport createCorpusByLuceneSearch(ICorpusCreateConfiguration configuration) throws ANoteException
	{
	
			Properties properties = configuration.getProperties();
			properties.put(GlobalNames.textType, CorpusTextType.convertCorpusTetTypeToString(configuration.getCorpusTextType()));
			ICorpus newCorpus = new CorpusImpl(configuration.getCorpusName(), configuration.getCorpusNotes(), configuration.getProperties());
			int size = 0;
			int offset = 0;
			int paginationSize = 100;
			boolean allDocs = false;
			/*createCorpusOnDatabase(newCorpus);
			Set<Long> documentIds = configuration.getDocumentsIDs();
			int step = 0;
			int total = documentIds.size();
			
			for(Long publicationId:documentIds) {
				InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publicationId);
				step++;
				memoryAndProgress(step,total);
			}
			*/
			
			
			
			ICorpusCreateReport report = new CorpusCreateReportImpl(newCorpus, configuration.getCorpusTextType(),size);
			return report;

	}

	public ICorpusCreateReport createCorpus(ICorpusCreateConfiguration configuration) throws ANoteException
	{
		try {
			Properties properties = configuration.getProperties();
			properties.put(GlobalNames.textType, CorpusTextType.convertCorpusTetTypeToString(configuration.getCorpusTextType()));
			ICorpus newCorpus = new CorpusImpl(configuration.getCorpusName(), configuration.getCorpusNotes(), configuration.getProperties());
			createCorpusOnDatabase(newCorpus);
			Set<IPublication> documents = configuration.getDocuments();
			int step = 0;
			int total = documents.size();
			for(IPublication publication:documents) {
				// Before connect ... insert document if not exists
				IPublication pub = getPublicationOnDatabaseByID(publication.getId());
				if(pub==null) {		
					Set<IPublication> documentToadd = new HashSet<>();
					documentToadd.add(publication);
					// Add Publication to system
					addPublicationToDatabase(documentToadd);
				}
				if(configuration.getCorpusTextType().equals(CorpusTextType.Hybrid) || 
						configuration.getCorpusTextType().equals(CorpusTextType.FullText)) {
					updatePDFInformationOnPublication(publication);
					// If pub don't have fulltext and publication has a full text inserted. Then it will be added and associated to corpus
					if(changefulltext(publication, pub))
						updatePublicationFullTextOnfDatabase(publication);
					
					//associate the document if it has fulltext content on fulltext corpus or the corpus is hybrid 
					if(configuration.getCorpusTextType().equals(CorpusTextType.FullText) && !publication.getFullTextContent().isEmpty()
							|| configuration.getCorpusTextType().equals(CorpusTextType.Hybrid))
						InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publication);
				}
				else
				{
					//on non full text or hybrid corpus, the document is always associated
					InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publication);
				}
				
				step++;
				memoryAndProgress(step,total);
			}
			ICorpusCreateReport report = new CorpusCreateReportImpl(newCorpus, configuration.getCorpusTextType(),configuration.getDocuments().size());
			return report;
		} catch (IOException e) {

			throw new ANoteException(e);
		}
	}

	private void updatePDFInformationOnPublication(IPublication publication)
			throws IOException, ANoteException, FileNotFoundException {
		// IF PDF is not available and source URL is a file put file in directory and update Full text COntent
		if(publication.getSourceURL()!=null && !publication.isPDFAvailable()) {
			publication.addPDFFile(new File(publication.getSourceURL()));
			// update relative path
			InitConfiguration.getDataAccess().updatePublication(publication);
		}
		// PDF is availbale and Full text are not available yet, transform it to string and insert it in publication object
		if(publication.isPDFAvailable() && publication.getFullTextContent().isEmpty()) {
			String saveDocDirectoty = (String) InitConfiguration.getPropertyValueFromInitOrProperties(GeneralDefaultSettings.PDFDOCDIRECTORY);
			// Get PDF to text from PDF file
			String fullTextContent = PDFtoText.convertPDFDocument(saveDocDirectoty + "//" + publication.getRelativePath());
			publication.setFullTextContent(fullTextContent);
		}
	}
	
	private boolean changefulltext(IPublication publication, IPublication pub){
		if(publication.getFullTextContent() == null || publication.getFullTextContent().isEmpty())
			return false;

		if(pub != null && pub.getFullTextContent() != null && !pub.getFullTextContent().isEmpty())
			return false;
			
		return true;
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

	protected void memoryAndProgress(int step, int total) {
		if(step%10==0)
		{
			System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		}
	}
	
	protected float memoryAndProgressOut(int step, int total) {
		if(step%10==0)
		{
			return (float) ((double)step/ (double) total * 100);
		}
		else return -1;
	}

}
