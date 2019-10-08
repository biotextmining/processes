package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.AIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSPatentRetrieval extends AIRPatentRetrieval{

	public OPSPatentRetrieval(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {
		super(configuration);

	}

	@Override
	public IIRPatentRetrievalReport retrievedPatents(Set<String> patentsIds) throws ANoteException {
		IRPatentRetrievalReport report= new IRPatentRetrievalReport ();//Open the report class
		String autentication = Utils.get64Base(((IIROPSPatentRetrievalConfiguration)getConfiguration()).getAccessToken());
		String tokenaccess=null;
		tokenaccess=OPSUtils.loginOPS(autentication);
		long starttime = System.currentTimeMillis();
		Iterator<String> iterator = patentsIds.iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			long actualtime=System.currentTimeMillis();
			String docPDFFinal = getConfiguration().getOutputDirectory() +"/" + patentID + ".pdf";
			if (!verifyPDFAlreadyDownloaded(docPDFFinal)){
				List<String> possiblePatentIDs;
				if(((float)(actualtime-starttime)/1000)>=900){//15min
					try {
						Thread.sleep(5000);
						tokenaccess=OPSUtils.loginOPS(autentication);
						starttime=System.currentTimeMillis();
					} catch (InterruptedException e) {
						throw new ANoteException(e);
					}
				}
				possiblePatentIDs = PatentPipelineUtils.createPatentIDPossibilities(patentID);
				File fileDownloaded = searchInAllPatents(tokenaccess, patentID, possiblePatentIDs);
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

	private File searchInAllPatents(String tokenaccess, String patentID, List<String> possiblePatentIDs) throws ANoteException {
		File fileDownloaded=null;
		for (String id:possiblePatentIDs){
			try{
				fileDownloaded=getPatentDocumentOCRBYID(patentID,id,tokenaccess);
			} catch (RedirectionException  | ServerErrorException | ConnectionException | ClientErrorException | ResponseHandlingException | COSVisitorException | InterruptedException e){
			}//dont do anything - pass to next
			catch (IOException e){
				throw new ANoteException(e);//file errors (file downloaded but some error happens)
			}
		}
		return fileDownloaded;
	}


	private File getPatentDocumentOCRBYID(String patentID,String patentIDModified,String tokenaccess) throws ANoteException, RedirectionException, ServerErrorException, ConnectionException, ResponseHandlingException, InterruptedException, IOException, COSVisitorException, ClientErrorException {
		File outDir = new File(getConfiguration().getOutputDirectory());
		File generatedPDF = OPSUtils.getPatentFullTextPDFUsingPatentID(tokenaccess, patentID ,outDir);
		return generatedPDF;
	}

	@Override
	public void validate(IIRPatentRetrievalConfiguration configuration)
			throws WrongIRPatentRetrievalConfigurationException {

		if(configuration instanceof IIROPSPatentRetrievalConfiguration)
		{
			IIROPSPatentRetrievalConfiguration opsConfiguration = (IIROPSPatentRetrievalConfiguration) configuration;
			String tokenAcess = opsConfiguration.getAccessToken();
			if ( tokenAcess== null || tokenAcess.isEmpty()) {
				throw new WrongIRPatentRetrievalConfigurationException("The OPS AccessToken can not be null or empty!");
			}

			String autentication = Utils.get64Base(tokenAcess);
			try {
				OPSUtils.postAuth(autentication);
			}catch(RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
					| ResponseHandlingException e1) {
				throw new WrongIRPatentRetrievalConfigurationException("The given OPS AccessToken is not a valid one. Try another one!");
			}
		}
		else
			throw new WrongIRPatentRetrievalConfigurationException("Configuration is not a IIROPSPatentRetrievalConfiguration.");
	}

	@Override
	public String getSourceName() {
		return "OPSPatentRetrieval";
	}		
}
