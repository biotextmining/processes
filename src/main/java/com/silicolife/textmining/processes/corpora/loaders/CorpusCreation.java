package com.silicolife.textmining.processes.corpora.loaders;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.List;
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

public class CorpusCreation {
	
	public CorpusCreation()
	{
		
	}
	

	public ICorpusCreateReport createCorpus(ICorpusCreateConfiguration configuration) throws ANoteException
	{
		try {
		Properties properties = configuration.getProperties();
		properties.put(GlobalNames.textType, CorpusTextType.convertCorpusTetTypeToString(configuration.getCorpusTextType()));
		ICorpus newCorpus = new CorpusImpl(configuration.getCorpusName(), configuration.getCorpusNotes(), configuration.getProperties());
		InitConfiguration.getDataAccess().createCorpus(newCorpus);
		Set<IPublication> documents = configuration.getDocuments();
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		int step = 0;
		int total = documents.size();
		for(IPublication publication:documents)
		{
			// Before connect ... insert document if not exists
			if(InitConfiguration.getDataAccess().getPublication(publication.getId())==null)
			{		
				List<IPublication> documentToadd = new ArrayList<IPublication>();
				documentToadd.add(publication);
				// Add Publication to system
				InitConfiguration.getDataAccess().addPublications(documentToadd );
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
						InitConfiguration.getDataAccess().updatePublicationFullTextContent(publication);
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
						// update full text context
						InitConfiguration.getDataAccess().updatePublicationFullTextContent(publication);
					}
				}
			}
			
			InitConfiguration.getDataAccess().addCorpusPublication(newCorpus, publication);
			step++;
			if(step%10==0)
				memoryAndProgressAndTime(step,total,startTime);
		}
		ICorpusCreateReport report = new CorpusCreateReportImpl(newCorpus, configuration.getCorpusTextType(),configuration.getDocuments().size());
		return report;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}
	
	protected void memoryAndProgressAndTime(int step, int total,long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.gc();
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

}
