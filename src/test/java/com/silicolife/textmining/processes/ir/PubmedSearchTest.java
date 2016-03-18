package com.silicolife.textmining.processes.ir;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.Properties;

import org.junit.Test;

import com.silicolife.textmining.core.datastructures.init.exception.InvalidDatabaseAccess;
import com.silicolife.textmining.core.datastructures.process.ir.configuration.IRSearchConfigurationImpl;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchProcessReport;
import com.silicolife.textmining.core.interfaces.core.report.processes.ir.IIRSearchUpdateReport;
import com.silicolife.textmining.core.interfaces.process.IR.IIRSearchConfiguration;
import com.silicolife.textmining.core.interfaces.process.IR.IQuery;
import com.silicolife.textmining.core.interfaces.process.IR.exception.InternetConnectionProblemException;
import com.silicolife.textmining.processes.DatabaseConnectionInit;
import com.silicolife.textmining.processes.ir.pubmed.PubMedSearch;

public class PubmedSearchTest {

	@Test
	public void simplePubmedSearch() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		PubMedSearch pubmedSearch = new PubMedSearch();
		Properties propeties = new Properties();
		// The query name resulted
		String queryName = "Escherichia coli AND Stringent response";
		// Organism
		String organism = "Escherichia coli";
		// Keywords
		String keywords = "Stringent response";
		IIRSearchConfiguration searchConfiguration = new IRSearchConfigurationImpl(keywords , organism , queryName, propeties );
		IIRSearchProcessReport report = pubmedSearch.search(searchConfiguration);
		assertTrue(report.isFinishing());
	}
	

	@Test
	public void advancedPubmedSearch() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IIRSearchProcessReport report = createQuery();
		assertTrue(report.isFinishing());
	}
	
	@Test
	public void pubmedSearchUsingPMIDsList() throws InvalidDatabaseAccess, ANoteException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		fail("Not yet implemented");
	}
	
	@Test
	public void updateQueryUsingPubmedSearch() throws InvalidDatabaseAccess, ANoteException, InternetConnectionProblemException {
		DatabaseConnectionInit.init("localhost","3306","createdatest","root","admin");
		IIRSearchProcessReport report = createQuery();
		IQuery query = report.getQuery();
		PubMedSearch pubmedSearch = new PubMedSearch();
		IIRSearchUpdateReport reportupdate = pubmedSearch.updateQuery(query);
		assertTrue(reportupdate.isFinishing());
	}


	public static IIRSearchProcessReport createQuery() throws InvalidDatabaseAccess,
			ANoteException, InternetConnectionProblemException {
		System.out.println("Create Query");
		PubMedSearch pubmedSearch = new PubMedSearch();
		// Properties
		Properties propeties = new Properties();
		// The query name resulted
		String queryName = "Escherichia coli AND Stringent response Advanced";
		// Organism
		String organism = "Escherichia coli";
		// Keywords
		String keywords = "Stringent response";
		// Add Author Filter
//		propeties.put("authors", "");
		// Add Journal Filter
//		propeties.put("journal", "");
		// Add Data Range
		//// From Date
		propeties.put("fromDate", "2008");
		//// To Date
		propeties.put("toDate", "2014");
		// Article Details Content
		//// Abstract Available
//		propeties.put("articleDetails", "abstract");
		//// Free full text
		propeties.put("articleDetails", "freefulltext");
		//// Full Text available
//		propeties.put("articleDetails", "fulltextavailable");
		// Article Source
		//// Medline Only
//		propeties.put("ArticleSource", "med");
		//// Pubmed Central Only
		propeties.put("ArticleSource", "pmc");
		//// Both
//		propeties.put("ArticleSource", "medpmc");
		// Article Type
//		propeties.put("articletype", "Revision");

		IIRSearchConfiguration searchConfiguration = new IRSearchConfigurationImpl(keywords , organism , queryName, propeties );
		IIRSearchProcessReport report = pubmedSearch.search(searchConfiguration);
		return report;
	}
	
	public static IIRSearchProcessReport createQuery2() throws InvalidDatabaseAccess,
	ANoteException, InternetConnectionProblemException {
		PubMedSearch pubmedSearch = new PubMedSearch();
		// Properties
		Properties propeties = new Properties();
		// The query name resulted
		String queryName = "Aquaporin channels";
		// Organism
		String organism = "";
		// Keywords
		String keywords = "Aquaporin channels";
		// Add Author Filter
		propeties.put("authors", "Peter Agre");
		// Add Journal Filter
		//propeties.put("journal", "");
		// Add Data Range
		//// From Date
		propeties.put("fromDate", "2008");
		//// To Date
		propeties.put("toDate", "2014");
		// Article Details Content
		//// Abstract Available
		//propeties.put("articleDetails", "abstract");
		//// Free full text
//		propeties.put("articleDetails", "freefulltext");
		//// Full Text available
		//propeties.put("articleDetails", "fulltextavailable");
		// Article Source
		//// Medline Only
		//propeties.put("ArticleSource", "med");
		//// Pubmed Central Only
//		propeties.put("ArticleSource", "pmc");
		//// Both
		//propeties.put("ArticleSource", "medpmc");
		// Article Type
		//propeties.put("articletype", "Revision");

		IIRSearchConfiguration searchConfiguration = new IRSearchConfigurationImpl(keywords , organism , queryName, propeties );
		IIRSearchProcessReport report = pubmedSearch.search(searchConfiguration);
		return report;
	}
	
	

}
