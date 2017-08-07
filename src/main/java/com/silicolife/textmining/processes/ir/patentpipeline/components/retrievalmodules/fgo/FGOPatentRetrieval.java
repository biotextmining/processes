package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.fgo;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;

import com.lowagie.text.DocumentException;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.fgo.FGOParser;
import com.silicolife.textmining.processes.ir.fgo.FGOPatentDataObject;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.AIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;

public class FGOPatentRetrieval extends AIRPatentRetrieval{

	public FGOPatentRetrieval(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {
		super(configuration);
	}

	@Override
	public IIRPatentRetrievalReport retrievedPatents(Set<String> patentsIds) throws ANoteException {
		IRPatentRetrievalReport report= new IRPatentRetrievalReport ();//Open the report class
		Iterator<String> iterator = patentsIds.iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			String docPDFFinal = getConfiguration().getOutputDirectory() +"/" + patentID + ".pdf";
			if (!verifyPDFAlreadyDownloaded(docPDFFinal)){
				File fileDownloaded = getPDFAndUpdateReportUsingPatentID(patentID, getConfiguration().getOutputDirectory());
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
	
	protected File getPDFAndUpdateReportUsingPatentID(String patentID,String saveDocDirectoty) throws ANoteException{
		try {
			FGOPatentDataObject fgoPatentDataObject = FGOParser.retrieveFullInformation(patentID);
			String fullTextContent = fgoPatentDataObject.getTextContent();
			if(fullTextContent!=null && !fullTextContent.isEmpty())
			{
				String filepath = saveDocDirectoty + "/" + patentID + ".pdf";
				FileHandling.createPDFFileWithText(filepath,fullTextContent);
				return new File(filepath);
			}
		} catch (IOException | COSVisitorException | DocumentException e) {
		}
		return null;
	}


	public String getSourceName() {
		return "FGO Patent Retrieval";
	}

	@Override
	public void validate(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {
	}

}
