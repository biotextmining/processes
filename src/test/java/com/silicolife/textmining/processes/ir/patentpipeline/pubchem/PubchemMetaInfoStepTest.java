package com.silicolife.textmining.processes.ir.patentpipeline.pubchem;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.pubchem.PubchemPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

import net.sourceforge.tess4j.TesseractException;

public class PubchemMetaInfoStepTest {

	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, WrongIRPatentIDRecoverConfigurationException, PatentPipelineException, ANoteException, IOException, TesseractException, WrongIRPatentMetaInformationRetrievalConfigurationException {

	
		
		PatentPipeline patentPipeline = new PatentPipeline();
		
		//Step 2 - Retrieved Patent meta Information	
		
		IIRPatentMetainformationRetrievalSource patentIDrecoverSource = new PubchemPatentMetaInformationRetrieval();
		patentPipeline.addPatentsMetaInformationRetrieval(patentIDrecoverSource);
		
		Set<String> patentids = new HashSet<>();
		patentids.add("EP0273689");
		patentids.add("US9233115");
		patentids.add("US9139536");
		patentids.add("US3990954");
		
		IIRPatentMetaInformationRetrievalReport result = patentPipeline.executePatentRetrievalMetaInformationStep(patentids,null);
		for(IPublication pub:result.getMapPatentIDPublication().values())
		{
			System.out.println(pub.toString());
		}

	}

}
