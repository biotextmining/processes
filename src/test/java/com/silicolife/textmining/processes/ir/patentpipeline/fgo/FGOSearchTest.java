package com.silicolife.textmining.processes.ir.patentpipeline.fgo;

import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.GoogleSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.IIRPatentIDRecoverGoogleSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.IRPatentIDRetrievalGoogleSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class FGOSearchTest {
	
	private static String accessToken = "accessToken";
	private static String customSearchID = "customSearchID";


	@Test
	public void test() throws WrongIRPatentIDRecoverConfigurationException, ANoteException {
		String query = "\"A\" AND B";
		IIRPatentIDRecoverGoogleSearchConfiguration fgoConfiguration = new IRPatentIDRetrievalGoogleSearchConfigurationImpl(accessToken, customSearchID);
		IIRPatentPipelineSearchConfiguration configuration = new IRPatentPipelineSearchConfigurationImpl(query );
		GoogleSearchPatentIDRecoverSource search = new GoogleSearchPatentIDRecoverSource(fgoConfiguration);
		Set<String> out = search.retrievalPatentIds(configuration);
		System.out.println(out.size());
		System.out.println(out.toString());

	}

}
