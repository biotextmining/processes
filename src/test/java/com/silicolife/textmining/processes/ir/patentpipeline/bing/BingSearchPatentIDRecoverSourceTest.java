package com.silicolife.textmining.processes.ir.patentpipeline.bing;

import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.BingSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.IRPatentIDRetrievalBingSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class BingSearchPatentIDRecoverSourceTest {
	
	private static String accessToken = "accessToken";

	@Test
	public void test() throws WrongIRPatentIDRecoverConfigurationException, ANoteException {
		String query = "expressiontosearch";
		IIRPatentPipelineSearchConfiguration configuration = new IRPatentPipelineSearchConfigurationImpl(query);
		IIRPatentIDRetrievalModuleConfiguration bingConfiguration = new IRPatentIDRetrievalBingSearchConfigurationImpl(accessToken);
		BingSearchPatentIDRecoverSource search = new BingSearchPatentIDRecoverSource(bingConfiguration );
		Set<String> out = search.retrievalPatentIds(configuration);
		System.out.println(out.size());
	}

}
