package com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository;

import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.fgo.FGOPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;

public class FGOPatentRetrievalTest {

	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, ANoteException {
		IProxy proxy = null;
		String outputDir = "src/test/resources";
		IIRPatentRetrievalConfiguration configuration = new IRPatentRetrievalConfigurationImpl(outputDir , proxy);
		FGOPatentRetrieval pr = new FGOPatentRetrieval(configuration);
		Set<String> patentsIds = new HashSet<>();
		patentsIds.add("WO2015010097");
		patentsIds.add("EP1565584");
		patentsIds.add("CN101981600");
		patentsIds.add("DE60232726");
		pr.retrievedPatents(patentsIds);
	}

}
