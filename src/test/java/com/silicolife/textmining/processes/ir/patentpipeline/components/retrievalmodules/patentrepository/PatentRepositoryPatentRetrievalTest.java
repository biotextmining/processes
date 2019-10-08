package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;

public class PatentRepositoryPatentRetrievalTest {
	
	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, ANoteException
	{
		IProxy proxy = null;
		String userName = "";
		String outputDir = "src/test/resources";
		String patentRepositoryServerBasedUrl = "url";
		String userPassword = "";
		IIRPatentRetrievalConfiguration configuration = new IRPatentRepositoryPatentRetrievalConfigurationImpl(proxy , outputDir , patentRepositoryServerBasedUrl , userName , userPassword );
		PatentRepositoryPatentRetrieval pr = new PatentRepositoryPatentRetrieval(configuration);
		Set<String> patentsIds = new HashSet<>();
		patentsIds.add("US06839908");
		patentsIds.add("US09687635");
		patentsIds.add("US09689937");
		patentsIds.add("US20150115954");
		pr.retrievedPatents(patentsIds);
	}

}
