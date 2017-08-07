package com.silicolife.textmining.processes.ir.patentpipeline;

import java.io.IOException;

import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.IROPSPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.OPSPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.PatentRepositoryPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.IRWIPOPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.WIPOPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.fgo.FGOPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops.IROPSPatentRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.ops.OPSPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository.IRPatentRepositoryPatentRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.retrievalmodules.patentrepository.PatentRepositoryPatentRetrieval;
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
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IIRPatentRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.retrievalmodule.IRPatentRetrievalConfigurationImpl;
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
		String patentRepositoryURL = "patentRepositoryURL";
		String patentRepositoryUser = "patentRepositoryUser";
		String patentRepositoryPassword = "patentRepositoryPassword";		
		String userPassword = "patentrepositoryPwd";
		String patentRepositoryServerBasedUrl = "patentrepositoryURL";
		String userName = "patentrepositoryUser";
		
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
		IIRPatentMetainformationRetrievalSource wipoMetaInformationRetrieval = new WIPOPatentMetaInformationRetrieval(configurationWIPO);
		patentPipeline.addPatentsMetaInformationRetrieval(wipoMetaInformationRetrieval);
		
		IIRPatentMetaInformationRetrievalConfiguration configurationOPS=new IROPSPatentMetaInformationRetrievalConfigurationImpl(proxy, accessTokenOPS);
		IIRPatentMetainformationRetrievalSource opsMetaInformationretrieval = new OPSPatentMetaInformationRetrieval(configurationOPS);
		patentPipeline.addPatentsMetaInformationRetrieval(opsMetaInformationretrieval);
		
		IIRPatentRepositoryPatentMetaInformationRetrievalConfiguration configurationPatentRepository = new IRPatentRepositoryPatentMetaInformationRetrievalConfigurationImpl(proxy, patentRepositoryURL, patentRepositoryUser, patentRepositoryPassword);
		IIRPatentMetainformationRetrievalSource patentRepository = new PatentRepositoryPatentMetaInformationRetrieval(configurationPatentRepository);
		patentPipeline.addPatentsMetaInformationRetrieval(patentRepository);

		//Step 3 - Retrieved PDF

		IIRPatentRetrievalConfiguration configuration = new IRWIPOPatentRetrievalConfigurationImpl(usernameWIPO, pwdWIPO, outputDir, proxy );
		IIRPatentRetrieval WIPOpatentRetrievalProcess = new WIPOPatentRetrieval(configuration);
		patentPipeline.addPatentIDRetrieval(WIPOpatentRetrievalProcess);
		
		IIRPatentRetrievalConfiguration configuration2 = new IROPSPatentRetrievalConfigurationImpl(outputDir, proxy, accessTokenOPS);
		IIRPatentRetrieval OPSpatentRetrievalProcess = new OPSPatentRetrieval(configuration2 );
		patentPipeline.addPatentIDRetrieval(OPSpatentRetrievalProcess);

		IIRPatentRetrievalConfiguration configurationPatentRepositoryREtrieval = new IRPatentRepositoryPatentRetrievalConfigurationImpl(proxy, outputDir, patentRepositoryServerBasedUrl, userName, userPassword);
		IIRPatentRetrieval patentRetrievalProcessPatentRepository = new PatentRepositoryPatentRetrieval(configurationPatentRepositoryREtrieval );
		patentPipeline.addPatentIDRetrieval(patentRetrievalProcessPatentRepository);
		
		IIRPatentRetrievalConfiguration configurationFGOREtrieval = new IRPatentRetrievalConfigurationImpl(outputDir, proxy);
		IIRPatentRetrieval patentRetrievalProcessFGO = new FGOPatentRetrieval(configurationFGOREtrieval);
		patentPipeline.addPatentIDRetrieval(patentRetrievalProcessFGO);
		
		// Run 
		patentPipeline.runCompletePipeline(patentPipelineSearchConfiguration);	
	}

}
