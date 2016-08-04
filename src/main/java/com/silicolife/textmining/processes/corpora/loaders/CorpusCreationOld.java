package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.corpora.CorpusImpl;
import com.silicolife.textmining.core.datastructures.documents.PDFtoText;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.general.GeneralDefaultSettings;
import com.silicolife.textmining.core.datastructures.init.propertiesmanager.PropertiesManager;
import com.silicolife.textmining.core.datastructures.report.corpora.CorpusCreateReportImpl;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalNames;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.corpora.ICorpusCreateConfiguration;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.document.corpus.CorpusTextType;
import com.silicolife.textmining.core.interfaces.core.document.corpus.ICorpus;
import com.silicolife.textmining.core.interfaces.core.report.corpora.ICorpusCreateReport;

public class CorpusCreationOld {

	public CorpusCreationOld()
	{

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
			for(IPublication publication:documents)
			{
				// Before connect ... insert document if not exists
				IPublication pub = getPublicationOnDatabaseByID(publication.getId());
				if(pub==null)
				{		
					Set<IPublication> documentToadd = new HashSet<>();
					documentToadd.add(publication);
					// Add Publication to system
					addPublicationToDatabase(documentToadd);
					// Test if Corpus is full text
					if(configuration.getCorpusTextType().equals(CorpusTextType.Hybrid) || 
							configuration.getCorpusTextType().equals(CorpusTextType.FullText))
					{
						// IF PDF is nt available and source URL is a file put file in directory and update Full text COntent
						if(publication.getSourceURL()!=null)
						{
							publication.addPDFFile(new File(publication.getSourceURL()));
							// update relative path
							InitConfiguration.getDataAccess().updatePublication(publication);

						}
						// update full text context
						if(publication.getFullTextContent().isEmpty())
							updatePublicationFullTextOnfDatabase(publication);
					}

				}
				else // Already Exist publication
				{
					if(configuration.getCorpusTextType().equals(CorpusTextType.Hybrid) || 
							configuration.getCorpusTextType().equals(CorpusTextType.FullText))
					{
						// IF PDF is not available and source URL is a file put file in directory and update Full text COntent
						if(publication.getSourceURL()!=null && !publication.isPDFAvailable())
						{
							publication.addPDFFile(new File(publication.getSourceURL()));
							// update relative path
							InitConfiguration.getDataAccess().updatePublication(publication);
						}
						// PDF is availbale and Full text are not available yet
						else if(publication.isPDFAvailable() && publication.getFullTextContent().isEmpty())
						{
							String saveDocDirectoty = (String) PropertiesManager.getPManager().getProperty(GeneralDefaultSettings.PDFDOCDIRECTORY);
							// Get PDF to text from PDF file
							String fullTextContent = PDFtoText.convertPDFDocument(saveDocDirectoty + "//" + publication.getRelativePath());
							publication.setFullTextContent(fullTextContent);
							updatePublicationFullTextOnfDatabase(publication);
						}
					}
				}

				InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publication);
				step++;
				if(step%10==0)
					memoryAndProgress(step,total);
			}
			ICorpusCreateReport report = new CorpusCreateReportImpl(newCorpus, configuration.getCorpusTextType(),configuration.getDocuments().size());
			return report;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
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
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

}
