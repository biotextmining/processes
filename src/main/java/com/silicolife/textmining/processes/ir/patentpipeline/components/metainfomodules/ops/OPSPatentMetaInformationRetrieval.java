package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.AIRPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class OPSPatentMetaInformationRetrieval extends AIRPatentMetaInformationRetrieval{
	
	public final static String opsProcessID = "ops.searchpatentmetainformation";

	public OPSPatentMetaInformationRetrieval(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(configuration);
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		long t1 = System.currentTimeMillis();
		String autentication = Utils.get64Base(((IIROPSPatentMetaInformationRetrievalConfiguration)getConfiguration()).getAccessToken());
		String tokenaccess=OPSUtils.loginOPS(autentication);
		for(String patentID:mapPatentIDPublication.keySet())
		{
			long t2 = System.currentTimeMillis();
			if(((float)(t2-t1)/1000)>=900){//15min
				try {
					Thread.sleep(5000);
					tokenaccess=OPSUtils.loginOPS(autentication);
					t1=System.currentTimeMillis();
				} catch (InterruptedException e) {
					throw new ANoteException(e);
				}
			}
			Set<String> possiblePatentIDs;
			possiblePatentIDs = OPSUtils.createPatentIDPossibilities(patentID);
			searchInAllPatents(mapPatentIDPublication, tokenaccess, patentID, possiblePatentIDs);
		}
	}



	private boolean searchInAllPatents(Map<String, IPublication> mapPatentIDPublication, String tokenaccess,
			String patentID, Set<String> possiblePatentIDs) {
		boolean informationdownloaded =false;
		for (String id:possiblePatentIDs){
			informationdownloaded = tryUpdatePatentMetaInformation(mapPatentIDPublication, patentID, id, tokenaccess);
			if (informationdownloaded){
				return true;
			}
		}
		return false;
	}

	private boolean verifyPublicationMetadataDownload(Map<String, IPublication> mapPatentIDPublication, String patentID){
		IPublication pub = mapPatentIDPublication.get(patentID);
		if((pub.getTitle()==null||pub.getTitle().isEmpty())||
				((pub.getAbstractSection()==null||pub.getAbstractSection().isEmpty())&&
				(pub.getAuthors()==null||pub.getAuthors().isEmpty())&&
				(pub.getYeardate()==null||pub.getYeardate().isEmpty()))){//empty publication (or without title)
			return false;
		}
		else{
			return true;
		}
	}

	private boolean tryUpdatePatentMetaInformation(Map<String, IPublication> mapPatentIDPublication, String patentIDOriginal, String patentIDModified, String tokenaccess){
		IPublication publiction = mapPatentIDPublication.get(patentIDOriginal);
		try {
			OPSUtils.updatePatentMetaInformation(tokenaccess, publiction, patentIDModified);
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			return false;
		}
		boolean downloadSuccess=verifyPublicationMetadataDownload(mapPatentIDPublication, patentIDOriginal);
		if (!downloadSuccess){
			return false;
		}
		return true;
	}

	@Override
	public String getSourceName() {
		return "OPSPatentRetrieval";
	}

	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		if(configuration instanceof IIROPSPatentMetaInformationRetrievalConfiguration)
		{
			IIROPSPatentMetaInformationRetrievalConfiguration opsConfiguration = (IIROPSPatentMetaInformationRetrievalConfiguration) configuration;
			String tokenAcess = opsConfiguration.getAccessToken();
			if ( tokenAcess== null || tokenAcess.isEmpty()) {
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("The AcessToken can not be null or empty");
			}
		}
		else
			new WrongIRPatentMetaInformationRetrievalConfigurationException("Configuration is not a IIROPSPatentMetaInformationRetrievalConfiguration");	
	}

}
