package com.silicolife.textmining.processes.ir.patentpipeline.patentrepository;

import java.io.IOException;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.patentrepository.IRPatentIDRetrievalPatentRepositorySearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.patentrepository.PatentRepositoryPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

import net.sourceforge.tess4j.TesseractException;

public class PatentRepositorySearchStepTest {

	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, WrongIRPatentIDRecoverConfigurationException, PatentPipelineException, ANoteException, IOException, TesseractException, WrongIRPatentMetaInformationRetrievalConfigurationException {

		PatentPipeline patentPipeline = new PatentPipeline();
		
		//Step 1 - Retrieved Patent IDs Information	

		String query = "bioprocess";
		String basedServerURL ="url";
		String user = "guest";
		String pwd = "guest";
		
		IIRPatentPipelineSearchConfiguration patentPipelineSearchConfiguration = new IRPatentPipelineSearchConfigurationImpl(query);
		
		IIRPatentIDRetrievalModuleConfiguration configuration = new IRPatentIDRetrievalPatentRepositorySearchConfigurationImpl(basedServerURL,user,pwd);
		IIRPatentIDRetrievalSource patentIDrecoverSourceEPO = new PatentRepositoryPatentIDRecoverSource(configuration);
		patentPipeline.addPatentIDRecoverSource(patentIDrecoverSourceEPO);
		
		Set<String> result = patentPipeline.executePatentIDSearchStep(patentPipelineSearchConfiguration);
		System.out.println(result.toString());

	}

}
