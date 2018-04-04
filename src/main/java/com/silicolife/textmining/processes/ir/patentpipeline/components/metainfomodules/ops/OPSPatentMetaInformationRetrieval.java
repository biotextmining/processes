package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineUtils;
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

	public final static String opsName= "Open Patent Services API from EPO";

	private final static int minWaitTime=100;
	private final static int maxWaitTime=800;

	private Long lastTimeThatGetAutentication;

	private String tokenaccess;

	public OPSPatentMetaInformationRetrieval(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		super(configuration);
	}

	@Override
	public void retrievePatentsMetaInformation(Map<String, IPublication> mapPatentIDPublication) throws ANoteException {
		IIROPSPatentMetaInformationRetrievalConfiguration configuration = (IIROPSPatentMetaInformationRetrievalConfiguration) getConfiguration();
		boolean updateAbstarctWitClaimsAndDescription = configuration.isAbstarctIncludeClaimsAndDescription();
		testIfautenticationIFneeded();
		Iterator<String> iterator = mapPatentIDPublication.keySet().iterator();
		while(iterator.hasNext() && !stop)
		{
			testIfautenticationIFneeded();
			String patentID = iterator.next();
			if(configuration.isWaitingTimeBetweenSteps())
				waitARandomTime();
			List<String> possiblePatentIDs = PatentPipelineUtils.createPatentIDPossibilities(patentID);
			searchInAllPatents(mapPatentIDPublication, tokenaccess, patentID,possiblePatentIDs, updateAbstarctWitClaimsAndDescription);
		}
	}

	public void testIfautenticationIFneeded() throws ANoteException
	{
		IIROPSPatentMetaInformationRetrievalConfiguration configuration = (IIROPSPatentMetaInformationRetrievalConfiguration) getConfiguration();
		if(lastTimeThatGetAutentication==null)
		{
			lastTimeThatGetAutentication = System.currentTimeMillis();
		}
		long nowTime = System.currentTimeMillis();
		if(((float)(nowTime-lastTimeThatGetAutentication)/1000)>=900){//15min
			try {
				Thread.sleep(2000);
				String autentication = Utils.get64Base(configuration.getAccessToken());
				tokenaccess=OPSUtils.loginOPS(autentication);
				lastTimeThatGetAutentication=System.currentTimeMillis();
			} catch (InterruptedException e) {
				throw new ANoteException(e);
			}
		}
	}


	private void waitARandomTime() throws ANoteException{ 
		try {
			Random r = new Random();
			int Result = r.nextInt(maxWaitTime-minWaitTime) + minWaitTime;
			Thread.sleep(Result);
		} catch (InterruptedException e) {
			throw new ANoteException(e);
		}
	}


	private boolean searchInAllPatents(Map<String, IPublication> mapPatentIDPublication, String tokenaccess,
			String patentID, List<String> possiblePatentIDs,boolean updateAbstractWithClaimsAndDescription) {
		boolean informationdownloaded =false;
		for (String id:possiblePatentIDs){
			informationdownloaded = tryUpdatePatentMetaInformation(mapPatentIDPublication, patentID, id, tokenaccess,updateAbstractWithClaimsAndDescription);
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

	private boolean tryUpdatePatentMetaInformation(Map<String, IPublication> mapPatentIDPublication, String patentIDOriginal, String patentIDModified, String tokenaccess, boolean updateAbstractWithClaimsAndDescription){
		IPublication publiction = mapPatentIDPublication.get(patentIDOriginal);
		try {
			OPSUtils.getPatentFamily(tokenaccess, publiction, patentIDModified);
			OPSUtils.updatePatentMetaInformation(tokenaccess, publiction, patentIDModified,updateAbstractWithClaimsAndDescription);
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
		return "OPS Patent Metainformation Retrieval";
	}

	@Override
	public void validate(IIRPatentMetaInformationRetrievalConfiguration configuration)
			throws WrongIRPatentMetaInformationRetrievalConfigurationException {
		if(configuration instanceof IIROPSPatentMetaInformationRetrievalConfiguration)
		{
			IIROPSPatentMetaInformationRetrievalConfiguration opsConfiguration = (IIROPSPatentMetaInformationRetrievalConfiguration) configuration;
			String tokenAcess = opsConfiguration.getAccessToken();
			if ( tokenAcess== null || tokenAcess.isEmpty()) {
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("The OPS AccessToken can not be null or empty");
			}

			String autentication = Utils.get64Base(tokenAcess);
			try {
				OPSUtils.postAuth(autentication);
			}catch(RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
					| ResponseHandlingException e1) {
				throw new WrongIRPatentMetaInformationRetrievalConfigurationException("The given OPS AccessToken is not a valid one. Try another one!");
			}
		}
		else
			throw new WrongIRPatentMetaInformationRetrievalConfigurationException("Configuration is not a IIROPSPatentMetaInformationRetrievalConfiguration");	
	}

}
