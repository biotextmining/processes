package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.MalformedURLException;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.activation.DataHandler;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.wipo.pctis.ps.client.Doc;
import org.wipo.pctis.ps.client.UnknownApplicationException_Exception;
import org.wipo.pctis.ps.client.UnknownDocumentException_Exception;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help.ServiceHelper;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.help.WIPOXMLSAXPHandler;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.AIRPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
public class WIPOPatentMetaInformationRetrieval extends AIRPatentMetaInformationRetrieval {

	private static ServiceHelper serviceHelper;

	public WIPOPatentMetaInformationRetrieval(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException, MalformedURLException {
		super(configuration);
		initWipo();
	}

	private void initWipo() throws MalformedURLException {
		String username =  ((IIRWIPOPatentMetaInformationRetrievalConfiguration)getConfiguration()).getUserName();
		String password = ((IIRWIPOPatentMetaInformationRetrievalConfiguration)getConfiguration()).getPassword();
		serviceHelper = new ServiceHelper(username, password);
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		for(String patentID:mapPatentIDPublication.keySet())
		{
			long t1 = new Date().getTime();
			System.out.println("Getting metainformation for patent:"+patentID);
			IPublication publication = mapPatentIDPublication.get(patentID);
			List<Doc> retour;
			try {
				retour = serviceHelper.getStub().getAvailableDocuments(patentID);
				for (int count=0; count<retour.size(); count++) {
					Doc doc = retour.get(count);
					List<String> pagesList = null;
					pagesList = serviceHelper.getStub().getDocumentTableOfContents(doc.getDocId());
					for (int i=0; i<pagesList.size(); i++) {
						if (pagesList.get(i).endsWith(".xml")){//verify xml existence
							DataHandler page = serviceHelper.getStub().getDocumentContentPage(doc.getDocId(), pagesList.get(i));
							SAXParserFactory spf = SAXParserFactory.newInstance();//Using sax parser in order to read inputstream.
							SAXParser sp = spf.newSAXParser();
							WIPOXMLSAXPHandler parseEventsHandler = new WIPOXMLSAXPHandler(publication);
							InputStream inputstream = page.getInputStream();
							Reader reader = new InputStreamReader(inputstream,"UTF-8");//conversion to inputstream reader in order to encoding to UTF-8
							InputSource is = new InputSource(reader);
							is.setEncoding("UTF-8");

							sp.parse(is,parseEventsHandler);

							long t2 = new Date().getTime();
							float downLoadingTime = ((float)(t2 - t1))/1000;
							System.out.println("getPatentMetaInformation(" + patentID + "," + pagesList.get(i) + "): "
									+ " downloaded in " + downLoadingTime + "s");

						}
					}
				}		
			} catch (UnknownApplicationException_Exception e) {
				//e.printStackTrace();
				System.err.println("Error UnknownApplicationException_Exception getting metainformation of patentID "+patentID);
			} catch (UnknownDocumentException_Exception e) {
				//e.printStackTrace();
				System.err.println("Error UnknownDocumentException_Exception getting metainformation of patentID "+patentID);
			} catch (ParserConfigurationException e) {
				throw new ANoteException(e);
			} catch (SAXException e) {
				throw new ANoteException(e);
			} catch (IOException e) {
				throw new ANoteException(e);
			}
			catch (Exception e) {//other exceptions do not given origin to a break
//				throw new ANoteException(e);
				System.err.println(e);
			}
		}

	}


	@Override
	public String getSourceName() {
		return "WIPOPatentRetrieval";
	}

	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		if(configuration instanceof IIRWIPOPatentMetaInformationRetrievalConfiguration)
		{
			IIRWIPOPatentMetaInformationRetrievalConfiguration wipoConfiguration = (IIRWIPOPatentMetaInformationRetrievalConfiguration) configuration;
			String userName = wipoConfiguration.getUserName();
			if (userName == null || userName.isEmpty()) {
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("The username can not be null or empty");
			}

			String password = wipoConfiguration.getPassword();
			if (password == null || password.isEmpty()) {
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("The password can not be null or empty");
			}
		}
		else
			new WrongIRPatentMetaInformationRetrievalConfigurationException("Configuration is not a IIRWIPOPatentMetaInformationRetrievalConfiguration");
	}


}
