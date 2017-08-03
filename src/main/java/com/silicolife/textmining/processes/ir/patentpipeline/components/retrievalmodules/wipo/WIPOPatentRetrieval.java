package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import javax.activation.DataHandler;

import org.wipo.pctis.ps.client.Doc;
import org.wipo.pctis.ps.client.UnknownApplicationException_Exception;
import org.wipo.pctis.ps.client.UnknownDocumentException_Exception;

import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Image;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.RandomAccessFileOrArray;
import com.lowagie.text.pdf.codec.TiffImage;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help.ServiceHelper;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.AIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.sun.xml.ws.developer.StreamingDataHandler;


public class WIPOPatentRetrieval extends AIRPatentRetrieval{

	private ServiceHelper serviceHelper;

	public WIPOPatentRetrieval(IIRPatentRetrievalConfiguration configuration)throws WrongIRPatentRetrievalConfigurationException, MalformedURLException {
		super(configuration);
		initWipo();

	}

	@Override
	public IIRPatentRetrievalReport retrievedPatents(Set<String> patentsIds) throws ANoteException {
		IRPatentRetrievalReport report= new IRPatentRetrievalReport ();//Open the report class
		Iterator<String> iterator = patentsIds.iterator();
		while(iterator.hasNext() && !stop)
		{
			String patentID = iterator.next();
			try {
				getPatentDocumentOCRBYID(patentID);
				report.addRetrievedPatents(patentID);
			} catch (UnknownApplicationException_Exception e) {
				report.addNotRetrievedPatents(patentID);
				System.err.println("Error UnknownApplicationException_Exception getting patentID "+patentID);
			} catch (UnknownDocumentException_Exception e) {
				report.addNotRetrievedPatents(patentID);
				System.err.println("Error UnknownDocumentException_Exception getting patentID "+patentID);
			} catch (IOException e) {
				report.addNotRetrievedPatents(patentID);
				System.err.println("Error IOException getting patentID "+patentID);
			} catch (DocumentException e) {
				report.addNotRetrievedPatents(patentID);
				System.err.println("Error DocumentException getting patentID "+patentID);
			}
			catch (Exception e){
				report.addNotRetrievedPatents(patentID);
				System.err.println(e);
			}
		}
		return report;
	}


	private boolean verifyPDFAlreadyDownloaded(String filePathway){
		File file=new File(filePathway);
		if(file.exists() && file.isFile()){
			return true;
		}
		return false;

	}


	private void getPatentDocumentOCRBYID(String patentID)throws UnknownApplicationException_Exception,UnknownDocumentException_Exception, IOException, DocumentException {
		List<Doc> retour = serviceHelper.getStub().getAvailableDocuments(patentID);
		File pathStock = new File(getConfiguration().getOutputDirectory() +"/"+ patentID + ".pdf");
		if (!verifyPDFAlreadyDownloaded(pathStock.getPath())){
			for (int count=0; count<retour.size(); count++) {
				Doc doc = retour.get(count);
				List<String> pagesList = null;
				File outDir = new File(getConfiguration().getOutputDirectory());
				if ((doc.getDocType().equals("PAMPH")) && (doc.getOcrPresence() != null) && (doc.getOcrPresence().equals("yes"))) {
					StreamingDataHandler myfile = (StreamingDataHandler)serviceHelper.getStub().getDocumentOcrContent(doc.getDocId());
					if (myfile != null){
						if (!outDir.exists()) {
							outDir.mkdirs();
						}
						File receiptFile = new File(pathStock.getPath());
						myfile.moveTo(receiptFile);
						myfile.close();
					}
				}
				else{
					File temporaryPath = new File (getConfiguration().getOutputDirectory() +"/tmp_"+ patentID);
					pagesList = serviceHelper.getStub().getDocumentTableOfContents(doc.getDocId());
					for (int i=0; i<pagesList.size(); i++) {
						if (pagesList.get(i).endsWith(".tif")){//only download tif files	
							DataHandler page = serviceHelper.getStub().getDocumentContentPage(doc.getDocId(), pagesList.get(i));
							if (page!=null){
								if (!temporaryPath.exists()){
									temporaryPath.mkdirs();
								}	
								FileOutputStream tempImage = new FileOutputStream(temporaryPath+"/"+pagesList.get(i));
								page.writeTo(tempImage);
								tempImage.close();
							}
						}
					}
					transformTiffIntoPDF(pathStock, temporaryPath);
				}
			}
		}
	}



	private void transformTiffIntoPDF(File completePathToPDF,File temporaryPath) throws IOException, DocumentException{
		Rectangle pageSize = getPageSize(temporaryPath);
		Document document = new Document();
		document.setPageSize(pageSize);
		PdfWriter writer = PdfWriter.getInstance(document,  new FileOutputStream(completePathToPDF,true));
		writer.setStrictImageSequence(true);
		document.open();
		for (int file = 0; file <temporaryPath.listFiles().length; file++) {
			RandomAccessFileOrArray raOther = new RandomAccessFileOrArray(temporaryPath.listFiles()[file].getPath());
			Image otherImage = TiffImage.getTiffImage(raOther, 1);
			document.add(otherImage);
			document.newPage();
			raOther.close();
		}
		document.close();
		writer.close();
		recursiveDelete(temporaryPath);
	}


	private Rectangle getPageSize(File temporaryPathWithTiffFiles) throws IOException{
		RandomAccessFileOrArray ra = new RandomAccessFileOrArray(temporaryPathWithTiffFiles.listFiles()[0].getPath());
		Image image = TiffImage.getTiffImage(ra, 1);
		Rectangle pageSize = new Rectangle(image.getAbsoluteX(),image.getAbsoluteY(),image.getWidth(), image.getHeight());
		ra.close();
		return pageSize;
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

	private void initWipo() throws MalformedURLException {
		String username =  ((IIRWIPOPatentRetrievalConfiguration)getConfiguration()).getUserName();
		String password = ((IIRWIPOPatentRetrievalConfiguration)getConfiguration()).getPassword();
		serviceHelper = new ServiceHelper(username, password);
	}


	@Override
	public void validate(IIRPatentRetrievalConfiguration configuration) throws WrongIRPatentRetrievalConfigurationException {
		if(configuration instanceof IIRWIPOPatentRetrievalConfiguration)
		{
			IIRWIPOPatentRetrievalConfiguration wipoConfiguration = (IIRWIPOPatentRetrievalConfiguration) configuration;
			String userName = wipoConfiguration.getUserName();
			if (userName == null || userName.isEmpty()) {
				throw new WrongIRPatentRetrievalConfigurationException("The WIPO username can not be null or empty");
			}

			String password = wipoConfiguration.getPassword();
			if (password == null || password.isEmpty()) {
				throw new WrongIRPatentRetrievalConfigurationException("The WIPO password can not be null or empty");
			}
		}
		else
			new WrongIRPatentRetrievalConfigurationException("Configuration is not a IIRWIPOPatentRetrievalConfiguration");
	}

	@Override
	public String getSourceName() {
		return "WIPOPatentRetrieval";
	}

}
