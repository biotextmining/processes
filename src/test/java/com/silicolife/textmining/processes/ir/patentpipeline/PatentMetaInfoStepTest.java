package com.silicolife.textmining.processes.ir.patentpipeline;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.PatentRepositoryPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

import net.sourceforge.tess4j.TesseractException;

public class PatentMetaInfoStepTest {

	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, WrongIRPatentIDRecoverConfigurationException, PatentPipelineException, ANoteException, IOException, TesseractException, WrongIRPatentMetaInformationRetrievalConfigurationException {

		
		String basedServerURL = "http://localhost:8998/patentrepository";
		String user = "";
		String pwd = "";
		
		PatentPipeline patentPipeline = new PatentPipeline();
		
		//Step 2 - Retrieved Patent meta Information	
		
		IIRPatentMetaInformationRetrievalConfiguration configuration = new IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl(null,basedServerURL,user,pwd);
		IIRPatentMetainformationRetrievalSource patentIDrecoverSource = new PatentRepositoryPatentMetaInformationRetrieval(configuration);
		patentPipeline.addPatentsMetaInformationRetrieval(patentIDrecoverSource);
		
		Set<String> patentids = new HashSet<>();
		patentids.add("US09630165");
		patentids.add("USRE046376");
		patentids.add("US09630961");
		patentids.add("US09631023");
		patentids.add("US09631057");
		patentids.add("US09629882");
		patentids.add("US09630988");
		patentids.add("US09631219");
		patentids.add("US09631189");
		patentids.add("US09631181");
		patentids.add("US09631208");
		
		IIRPatentMetaInformationRetrievalReport result = patentPipeline.executePatentRetrievalMetaInformationStep(patentids);
		for(IPublication pub:result.getMapPatentIDPublication().values())
		{
			System.out.println(pub.toString());
		}

	}

}
