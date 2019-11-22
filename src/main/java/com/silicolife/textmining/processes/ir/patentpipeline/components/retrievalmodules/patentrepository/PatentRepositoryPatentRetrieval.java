package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Iterator;
import java.util.Set;

import com.lowagie.text.DocumentException;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.AIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentrepository.PatentRepositoryAPI;

public class PatentRepositoryPatentRetrieval extends AIRPatentRetrieval{

	public PatentRepositoryPatentRetrieval(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {
		super(configuration);
	}

	@Override
	public IIRPatentRetrievalReport retrievedPatents(Set<String> patentsIds) throws ANoteException {
		IRPatentRetrievalReport report= new IRPatentRetrievalReport ();//Open the report class
		IIRPatentRepositoryPatentRetrievalConfiguration configuration = (IIRPatentRepositoryPatentRetrievalConfiguration) getConfiguration();
		Iterator<String> iterator = patentsIds.iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			String docPDFFinal = getConfiguration().getOutputDirectory() +"/" + patentID + ".pdf";
			if (!verifyPDFAlreadyDownloaded(docPDFFinal)){
				File fileDownloaded = getPDFAndUpdateReportUsingPatentID(configuration.getPatentRepositoryServerBasedUrl(), patentID, getConfiguration().getOutputDirectory());
				if (fileDownloaded==null){
					report.addNotRetrievedPatents(patentID);				
				}
				else{
					report.addRetrievedPatents(patentID);
				}
			}
			else{
				report.addRetrievedPatents(patentID);
			}
		}
		return report;
	}
	
	protected File getPDFAndUpdateReportUsingPatentID(String urlServer,String patentID,String saveDocDirectoty) throws ANoteException{
		try {
			String fullTextContent = PatentRepositoryAPI.getPatentFullText(urlServer, patentID);
			if(fullTextContent!=null && !fullTextContent.isEmpty())
			{
				String filepath = saveDocDirectoty + "/" + patentID + ".pdf";
				FileHandling.createPDFFileWithText(filepath,fullTextContent);
				return new File(filepath);
			}
		} catch (IOException | DocumentException e) {
		}
		return null;
	}

	@Override
	public String getSourceName() {
		return "PatentPipelineRetrieval";
	}

	@Override
	public void validate(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {
		if(configuration instanceof IIRPatentRepositoryPatentRetrievalConfiguration)
		{
			IIRPatentRepositoryPatentRetrievalConfiguration opsConfiguration = (IIRPatentRepositoryPatentRetrievalConfiguration) configuration;
			String url = opsConfiguration.getPatentRepositoryServerBasedUrl();
			if ( url == null || url.isEmpty() || !validateURL(url)) {
				throw new WrongIRPatentRetrievalConfigurationException("The Patent Repository URL is not valid");
			}
		}
		else
			throw new WrongIRPatentRetrievalConfigurationException("Configuration is not a IIRPatentRepositoryPatentRetrievalConfiguration.");		
	}

	private boolean validateURL(String url)
	{
		try {
			new URL(url).openConnection().connect();
		} catch (IOException e) {
			return false;
		}
		return  true;
	}

}
