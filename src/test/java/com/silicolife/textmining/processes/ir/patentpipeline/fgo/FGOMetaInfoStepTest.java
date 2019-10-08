package com.silicolife.textmining.processes.ir.patentpipeline.fgo;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.documents.PublicationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPipelineException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.FGOPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalReport;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

import net.sourceforge.tess4j.TesseractException;

public class FGOMetaInfoStepTest {
	
	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, WrongIRPatentIDRecoverConfigurationException, PatentPipelineException, ANoteException, IOException, TesseractException, WrongIRPatentMetaInformationRetrievalConfigurationException {

		
		PatentPipeline patentPipeline = new PatentPipeline();
		
		//Step 2 - Retrieved Patent meta Information	
		
		IIRPatentMetainformationRetrievalSource patentIDrecoverSource = new FGOPatentMetaInformationRetrieval();
		patentPipeline.addPatentsMetaInformationRetrieval(patentIDrecoverSource);
		
		Set<String> patentids = new HashSet<>();
		patentids.add("WO2006010252");
		patentids.add("US7157562");
		patentids.add("EP0540210");
		patentids.add("US09631023");
		patentids.add("CN103796727");
		patentids.add("CA2077921");
		patentids.add("EP2010641");
		
		IIRPatentMetaInformationRetrievalReport result = patentPipeline.executePatentRetrievalMetaInformationStep(patentids,null);
		for(IPublication pub:result.getMapPatentIDPublication().values())
		{
			System.out.println(((PublicationImpl)pub).toString2());
		}

	}

}
