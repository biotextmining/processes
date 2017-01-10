package com.silicolife.textmining.processes.ir;

import java.net.MalformedURLException;
import java.util.Properties;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.InitConfiguration;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.configuration.IProxy;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ir.patentpipeline.PatentPiplineSearch;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.IROPSPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.ops.OPSPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.IRWIPOPatentMetaInformationRetrievalConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.wipo.WIPOPatentMetaInformationRetrieval;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.BingSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.bing.IRPatentIDRetrievalBingSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo.EPOSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.epo.IRPatentIDRetrievalEPOSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.GoogleSearchPatentIDRecoverSource;
import com.silicolife.textmining.processes.ir.patentpipeline.components.searchmodule.googlesearch.IRPatentIDRetrievalGoogleSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IIRPatentPipelineSearchStepsConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.configuration.IRPatentPipelineSearchStepsConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetaInformationRetrievalConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.IIRPatentMetainformationRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.metainfomodule.WrongIRPatentMetaInformationRetrievalConfigurationException;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalModuleConfiguration;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IIRPatentIDRetrievalSource;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.IRPatentSearchConfigurationImpl;
import com.silicolife.textmining.processes.ir.patentpipeline.core.searchmodule.WrongIRPatentIDRecoverConfigurationException;

public class PatentPipelineSearchTest {

	@Test

	/**
	 * Test IQuery maker for patents with all configurations for patentID recover source and for patent retrieval system 
	 */
	public void test1() throws ANoteException, InternetConnectionProblemException, InvalidDatabaseAccess, InvalidConfigurationException, WrongIRPatentIDRecoverConfigurationException, MalformedURLException, WrongIRPatentMetaInformationRetrievalConfigurationException{
		DatabaseConnectionInit.init("localhost", "3306", "anote2db", "root", "root");
		InitConfiguration.addProperty("General.PDFDirectoryDocuments","src/test/resources/pdf/output");
		
		String query="Escherichia coli";
		String accessTokenEPO = "accessTokenEPO";
		String accessTokenBing = "accessTokenBing";
		String accessTokenGoogle = "accessTokenGoogle";
		String customSearchIDGoogle = "customSearchIDGoogle";
		String accessTokenOPS = "accessTokenOPS";
		String usernameWIPO = "wipousername";
		String pwdWIPO = "wipousernamepwd";

		IProxy proxy = null;
		Properties prop=null;
		//Step 1 - Retrived Patents Ids
		
		IIRPatentPipelineSearchConfiguration patentPipelineSearchConfiguration = new IRPatentPipelineSearchConfigurationImpl(query);
		
		IIRPatentIDRetrievalModuleConfiguration configurationEPO = new IRPatentIDRetrievalEPOSearchConfigurationImpl(accessTokenEPO);
		IIRPatentIDRetrievalSource patentIDrecoverSourceEPO = new EPOSearchPatentIDRecoverSource(configurationEPO);
		
		IIRPatentIDRetrievalModuleConfiguration configurationBing = new IRPatentIDRetrievalBingSearchConfigurationImpl(accessTokenBing);
		IIRPatentIDRetrievalSource patentIDrecoverSourceBing = new BingSearchPatentIDRecoverSource(configurationBing);
		
		IIRPatentIDRetrievalModuleConfiguration configurationGoogle = new IRPatentIDRetrievalGoogleSearchConfigurationImpl(accessTokenGoogle, customSearchIDGoogle);
		IIRPatentIDRetrievalSource patentIDrecoverSourceGoogle = new GoogleSearchPatentIDRecoverSource(configurationGoogle);
		
		//Step 2 - Retrived Meta Information	
		
		IIRPatentMetaInformationRetrievalConfiguration configurationWIPO = new IRWIPOPatentMetaInformationRetrievalConfigurationImpl(usernameWIPO, pwdWIPO, proxy );
		IIRPatentMetainformationRetrievalSource wipoMetaInformationRetrieval = new WIPOPatentMetaInformationRetrieval(configurationWIPO);
		
		IIRPatentMetaInformationRetrievalConfiguration configurationOPS=new IROPSPatentMetaInformationRetrievalConfigurationImpl(proxy, accessTokenOPS);
		IIRPatentMetainformationRetrievalSource opsMetaInformationretrieval = new OPSPatentMetaInformationRetrieval(configurationOPS);
			
		IIRPatentPipelineSearchStepsConfiguration configurationPipeline=new IRPatentPipelineSearchStepsConfigurationImpl();
		configurationPipeline.addIRPatentIDRecoverSource(patentIDrecoverSourceEPO);
		configurationPipeline.addIRPatentIDRecoverSource(patentIDrecoverSourceBing);
		configurationPipeline.addIRPatentIDRecoverSource(patentIDrecoverSourceGoogle);
		configurationPipeline.addIRPatentRetrievalMetaInformation(wipoMetaInformationRetrieval);
		configurationPipeline.addIRPatentRetrievalMetaInformation(opsMetaInformationretrieval);
		
		IIRPatentPipelineConfiguration configuration = new IRPatentSearchConfigurationImpl(patentPipelineSearchConfiguration,"Teste23062016",prop,configurationPipeline);
		PatentPiplineSearch runnerIQueryMaker = new PatentPiplineSearch();
		runnerIQueryMaker.search(configuration);
		
	
	}

}
