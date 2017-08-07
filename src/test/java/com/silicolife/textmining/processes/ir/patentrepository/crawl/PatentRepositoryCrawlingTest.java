package com.silicolife.textmining.processes.ir.patentrepository.crawl;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class PatentRepositoryCrawlingTest {
	
	@Test
	public void getPDF() throws ANoteException {
		String url = "url";
		String saveDocDirectoty = "src/test/resources";
		long pubID = 1;
		String patentID = "US07788725";
		PatentRepositoryCrawling test = new PatentRepositoryCrawling(url );
		test.getPDFAndUpdateReportUsingPatentID(patentID, saveDocDirectoty, pubID);
	}

}
