package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.pdfbox.exceptions.COSVisitorException;
import org.apache.pdfbox.util.PDFMergerUtility;

import com.silicolife.textmining.core.datastructures.utils.GenericPairImpl;
import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentImageHandler;
import com.silicolife.textmining.processes.ir.epopatent.opshandler.OPSPatentgetPDFPageHandler;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.AIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.utils.http.HTTPClient;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSPatentRetrieval extends AIRPatentRetrieval{

	private static String version = "3.1";
	private static String publicationDetails = "http://ops.epo.org/" + version + "/rest-services/published-data/publication/epodoc/";
	private static String generalURL = "http://ops.epo.org/" + version + "/rest-services/";
	private static HTTPClient client = new HTTPClient();

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
		File generatedPDF = null;
		File outDir = new File(getConfiguration().getOutputDirectory());
		String docPDFFinal = outDir +"/" + patentID + ".pdf";
		Map<String, String> headers = new HashMap<String, String>();
		if (tokenaccess != null) {
			headers.put("Authorization", "Bearer " + tokenaccess);
		}
		String urlPatentImages = publicationDetails + patentIDModified + "/images";			
		GenericPairImpl<Integer, String> pagesLink = client.get(urlPatentImages, headers, new OPSPatentImageHandler());	
		Path docPath = Paths.get(outDir+"/" + "/tmp_" + patentID);	
		if (!Files.exists(docPath))
			Files.createDirectories(docPath);

		// -- API to merge PDF
		PDFMergerUtility merger = new PDFMergerUtility();
		Integer numberPages = pagesLink.getX();		
		for (int x = 1; x <= numberPages; x++) {
			String urlpages = generalURL + pagesLink.getY() + ".pdf?" + "Range=" + x;
			File pdfOnePage = client.get(urlpages, headers, new OPSPatentgetPDFPageHandler(docPath.toString()));
			// -- Add source to merge
			merger.addSource(pdfOnePage);
			if(x%5 == 0 && tokenaccess==null)
			{
				if(tokenaccess==null)
				{
					Thread.sleep(62000);
				}
			}
		}
		merger.setDestinationFileName(docPDFFinal);
		merger.mergeDocuments();
		generatedPDF = new File(docPDFFinal);			
		recursiveDelete(docPath.toFile());

		return generatedPDF;
	}


	private boolean verifyPDFAlreadyDownloaded(String filePathway){
		File file=new File(filePathway);
		if(file.exists() && file.isFile()){
			return true;
		}
		return false;

	}

	private static void recursiveDelete(File file) {
		if (file.isDirectory()) {
			if (file.list().length == 0) {
				file.delete();
			} else {
				String files[] = file.list();
				for (String temp : files) {
					File fileDelete = new File(file, temp);
					recursiveDelete(fileDelete);
				}

				recursiveDelete(file);
			}
		} else {
			file.delete();
		}
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
