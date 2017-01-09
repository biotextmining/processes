package com.silicolife.textmining.processes.ir.patentpipeline;

import java.io.IOException;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.IROPSPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.OPSPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.IRWIPOPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.WIPOPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops.IROPSPatentRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops.OPSPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.IRWIPOPatentRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.wipo.WIPOPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.BingSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.IRPatentIDRetrievalBingSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo.EPOSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo.IRPatentIDRetrievalEPOSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.GoogleSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.IRPatentIDRetrievalGoogleSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.PatentPipeline;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentRetrievalMetaInformation;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.WrongIRPatentRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

import net.sourceforge.tess4j.TesseractException;

public class PatentPipelineCompleteTest {

//	@Test
	public void test() throws WrongIRPatentRetrievalConfigurationException, WrongIRPatentIDRecoverConfigurationException, PatentPipelineException, ANoteException, IOException, TesseractException, WrongIRPatentMetaInformationRetrievalConfigurationException {
		String accessTokenOPS = "accessTokenOPS";
		String accessTokenBing = "accessTokenBing";
		String accessTokenGoogle = "accessTokenGoogle";
		String customSearchID = "customSearchID";
		String usernameWIPO = "username";
		String pwdWIPO = "pwd";
		String outputDir = "src/test/resource";
		IProxy proxy = null;
		PatentPipeline patentPipeline = new PatentPipeline();
		
		//Step 1 - Retrieved Patent IDs Information	

		String query = "Define Query";
		
		IIRPatentPipelineSearchConfiguration patentPipelineSearchConfiguration = new IRPatentPipelineSearchConfigurationImpl(query);

		
		IIRPatentIDRetrievalModuleConfiguration configuration0 = new IRPatentIDRetrievalEPOSearchConfigurationImpl(accessTokenOPS);
		IIRPatentIDRetrievalSource patentIDrecoverSourceEPO = new EPOSearchPatentIDRecoverSource(configuration0);
		patentPipeline.addPatentIDRecoverSource(patentIDrecoverSourceEPO);
		
		IIRPatentIDRetrievalModuleConfiguration configuration3 = new IRPatentIDRetrievalBingSearchConfigurationImpl(accessTokenBing);
		IIRPatentIDRetrievalSource patentIDrecoverSourceBing = new BingSearchPatentIDRecoverSource(configuration3);
		patentPipeline.addPatentIDRecoverSource(patentIDrecoverSourceBing);
		
		IIRPatentIDRetrievalModuleConfiguration configuration4 = new IRPatentIDRetrievalGoogleSearchConfigurationImpl(accessTokenGoogle, customSearchID);
		IIRPatentIDRetrievalSource patentIDrecoverSourceGoogle = new GoogleSearchPatentIDRecoverSource(configuration4);
		patentPipeline.addPatentIDRecoverSource(patentIDrecoverSourceGoogle);
		
		//Step 2 - Retrieved Meta Information	
		
		IIRPatentMetaInformationRetrievalConfiguration configurationWIPO = new IRWIPOPatentMetaInformationRetrievalConfigurationImpl(usernameWIPO, pwdWIPO, proxy );
		IIRPatentRetrievalMetaInformation wipoMetaInformationRetrieval = new WIPOPatentMetaInformationRetrieval(configurationWIPO);
		patentPipeline.addPatentsMetaInformationRetrieval(wipoMetaInformationRetrieval);
		
		IIRPatentMetaInformationRetrievalConfiguration configurationOPS=new IROPSPatentMetaInformationRetrievalConfigurationImpl(proxy, accessTokenOPS);
		IIRPatentRetrievalMetaInformation opsMetaInformationretrieval = new OPSPatentMetaInformationRetrieval(configurationOPS);
		patentPipeline.addPatentsMetaInformationRetrieval(opsMetaInformationretrieval);
		
		//Step 3 - Retrieved PDF

		
		IIRPatentRetrievalConfiguration configuration = new IRWIPOPatentRetrievalConfigurationImpl(usernameWIPO, pwdWIPO, outputDir, proxy );
		IIRPatentRetrieval WIPOpatentRetrievalProcess = new WIPOPatentRetrieval(configuration);
		patentPipeline.addPatentIDRetrieval(WIPOpatentRetrievalProcess);
		
		IIRPatentRetrievalConfiguration configuration2 = new IROPSPatentRetrievalConfigurationImpl(outputDir, proxy, accessTokenOPS);
		IIRPatentRetrieval OPSpatentRetrievalProcess = new OPSPatentRetrieval(configuration2 );
		patentPipeline.addPatentIDRetrieval(OPSpatentRetrievalProcess);
		
		
		
		//String path = "D:/Desktop/ATCC 55618 OR Actinobacillus succinogenes OR CCUG 43843 OR CIP 106512 OR strain 130Z.txt";
		//String path="src/test/resources/data/teste_ids_google.txt";
//		patentPipeline.runPipelineWithpatentIdsFromFile(patentPipeline.patentIDsloaderFromFile(path));
		patentPipeline.runCompletePipeline(patentPipelineSearchConfiguration);
		
//		patentPipeline.applyOCRengine();
//		patentPipeline.saveTXTwithPatentsText("teste02_real.txt");
	}

}
