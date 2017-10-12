package com.silicolife.textmining.processes.ir.epopatent;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.exceptions.process.InvalidConfigurationException;
import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.document.IPublication;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.IQuery;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ir.epopatent.configuration.IREPOSearchConfigurationImpl;

public class OPSSearchTest {
	
	private static String  authentication = "accesstoken";


	@Test
	public void test() throws InvalidDatabaseAccess, ANoteException, InvalidConfigurationException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","test3","root","admin");
		OPSSearch search = new OPSSearch();
		String keywords = "keywords";
		String organism = "";
		Integer minYear = 2010;
		Integer maxYear = 2012;
		Properties propeties = new Properties();
		Set<String> classificationIPCFilter = new HashSet<>();
		classificationIPCFilter.add("C12N");
		classificationIPCFilter.add("C12R");
		classificationIPCFilter.add("C12P");
		String queryName = "test";
		IIRSearchConfiguration configuration = new IREPOSearchConfigurationImpl(keywords, organism , queryName , authentication, minYear, maxYear  , classificationIPCFilter , propeties);
		IIRSearchProcessReport report = search.search(configuration);
		IQuery query = report.getQuery();
		List<IPublication> publications = query.getPublications();
		System.out.println(publications.size());
		for(IPublication publication:publications)
		{
			System.out.println(publication.toString());
		}
	}

}
