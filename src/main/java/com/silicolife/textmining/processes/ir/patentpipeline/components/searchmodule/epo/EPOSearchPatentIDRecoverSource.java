package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo;

import java.util.GregorianCalendar;
import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.datastructures.utils.Utils;
import com.silicolife.textmining.core.datastructures.utils.conf.GlobalOptions;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.ir.epopatent.OPSUtils;
import com.silicolife.textmining.processes.ir.epopatent.configuration.OPSConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.AIRPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class EPOSearchPatentIDRecoverSource extends AIRPatentIDRecoverSource{
	
	public final static String opsProccessID = "ops.searchpatentid";
	public final static String opsName= "Open Patent Services API from EPO"; 

	
	public EPOSearchPatentIDRecoverSource(IIRPatentIDRetrievalModuleConfiguration configuration) throws WrongIRPatentIDRecoverConfigurationException {
		super(configuration);
	}

	@Override
	public Set<String> retrievalPatentIds(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException{

		int results;
		String query = OPSUtils.queryBuilder(configuration.getQuery());
		String autentication = null;
		try {
			String tokenaccess = Utils.get64Base(((IIRPatentIDRecoverEPOSearchConfiguration)getConfiguration()).getAccessToken());
			autentication = OPSUtils.postAuth(tokenaccess);
			results = OPSUtils.getSearchResults(query);
		} catch (RedirectionException | ClientErrorException| ServerErrorException | ConnectionException| ResponseHandlingException e) {
			throw new ANoteException(new InternetConnectionProblemException(e));
		}
		if(results > OPSConfiguration.MAX_RESULTS)
			results = OPSConfiguration.MAX_RESULTS;
		long startTime = GregorianCalendar.getInstance().getTimeInMillis();
		Set<String> patentIDs = new HashSet<>();
		for(int step=1;step<=results && !stop;step = step + OPSConfiguration.STEP)
		{
			// Step Document retrieved
			Set<String> patentIDsReturned;
			try {
				patentIDsReturned = OPSUtils.getSearchPatentIds(autentication,query,step);
			} catch (RedirectionException | ClientErrorException
					| ServerErrorException | ConnectionException
					| ResponseHandlingException e) {
				break;//query limits reached (verification is made previously)
			}
			patentIDs.addAll(patentIDsReturned);
			memoryAndProgressAndTime(step + OPSConfiguration.STEP,results+1,startTime);
		}
		return patentIDs;
	}
	
	
	public void memoryAndProgressAndTime(int step, int total, long startTime) {
		System.out.println((GlobalOptions.decimalformat.format((double)step/ (double) total * 100)) + " %...");
		System.out.println((Runtime.getRuntime().totalMemory()- Runtime.getRuntime().freeMemory())/(1024*1024) + " MB ");
	}

	@Override
	public String getSourceName() {
		return OPSConfiguration.opssearch;
	}

	@Override
	public void validate(IIRPatentIDRetrievalModuleConfiguration configuration) throws WrongIRPatentIDRecoverConfigurationException {
		if(configuration instanceof IIRPatentIDRecoverEPOSearchConfiguration)
		{
			IIRPatentIDRecoverEPOSearchConfiguration configurationEPOSearch = (IIRPatentIDRecoverEPOSearchConfiguration) configuration;
			if(configurationEPOSearch.getAccessToken()==null || configurationEPOSearch.getAccessToken().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("OPS Acess Token can not be null or empty");
			}
			if(!configurationEPOSearch.getAccessToken().contains(":") && configurationEPOSearch.getAccessToken().length() < 20)
			{
				throw new WrongIRPatentIDRecoverConfigurationException("Invalid OPS access token");
			}
		}
		else
			throw new WrongIRPatentIDRecoverConfigurationException("Configuration is not a IIRPatentIDRecoverEPOSearchConfiguration");
	}

	@Override
	public int getNumberOfResults() throws ANoteException {
		return 0;
	}
}
