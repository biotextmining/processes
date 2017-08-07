package com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.patentrepository;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.AIRPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;
import com.silicolife.textmining.processes.ir.patentrepository.PatentRepositoryAPI;

public class PatentRepositoryPatentIDRecoverSource extends AIRPatentIDRecoverSource{

	public final static String patentrepositoryName = "Patent Repository from SilicoLife and CEB (UMinho)";
	public final static String patentrepositoryProcessID = "patentrepository.searchpatentid";
		
	public PatentRepositoryPatentIDRecoverSource(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		super(configuration);
	}

	@Override
	public Set<String> retrievalPatentIds(IIRPatentPipelineSearchConfiguration configuration) throws ANoteException {
		Set<String> out = new HashSet<>();
		IIRPatentIDRecoverPatentRepositorySearchConfiguration moduleConf = (IIRPatentIDRecoverPatentRepositorySearchConfiguration) getConfiguration();
		try {
			out = PatentRepositoryAPI.getPatentIdsGivenTextQuery(moduleConf.getPatentRepositoryServerBasedUrl(), configuration.getQuery());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return out;
	}

	@Override
	public String getSourceName() {
		return patentrepositoryName;
	}

	@Override
	public int getNumberOfResults() throws ANoteException {
		return 0;
	}

	@Override
	public void validate(IIRPatentIDRetrievalModuleConfiguration configuration)
			throws WrongIRPatentIDRecoverConfigurationException {
		if(configuration instanceof IIRPatentIDRecoverPatentRepositorySearchConfiguration)
		{
			IIRPatentIDRecoverPatentRepositorySearchConfiguration moduleConf = (IIRPatentIDRecoverPatentRepositorySearchConfiguration) configuration;
			if(moduleConf.getPatentRepositoryServerBasedUrl() == null || moduleConf.getPatentRepositoryServerBasedUrl().isEmpty())
			{
				throw new WrongIRPatentIDRecoverConfigurationException("PatentRepositoryServerBasedUrl can not be null or empty");		
			}
		}
		else
			throw new WrongIRPatentIDRecoverConfigurationException("Configuration is not a IIRPatentIDRecoverPatentRepositorySearchConfiguration");		
	}
	
//	private void login() throws IOException
//	{
//		IIRPatentIDRecoverPatentRepositorySearchConfiguration configuration = (IIRPatentIDRecoverPatentRepositorySearchConfiguration) getConfiguration();
//		URL url = new URL(configuration.getPatentRepositoryServerBasedUrl()+"/login");
//
//		Map<String, Object> params = new LinkedHashMap<>();
//		params.put("username", configuration.getUserName());
//		params.put("password", configuration.getPassword());
//
//		StringBuilder postData = new StringBuilder();
//		for (Map.Entry<String, Object> param : params.entrySet()) {
//			if (postData.length() != 0)
//				postData.append('&');
//			postData.append(URLEncoder.encode(param.getKey(), "UTF-8"));
//			postData.append('=');
//			postData.append(URLEncoder.encode(String.valueOf(param.getValue()), "UTF-8"));
//		}
//		byte[] postDataBytes = postData.toString().getBytes("UTF-8");
//
//
//
//
//		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
//		conn.setRequestMethod("POST");
//		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
//		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
//		conn.setDoOutput(true);
//		conn.getOutputStream().write(postDataBytes);
//		conn.getResponseCode();
//
//	}

}
