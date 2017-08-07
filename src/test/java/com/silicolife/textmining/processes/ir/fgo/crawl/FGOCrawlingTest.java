package com.silicolife.textmining.processes.ir.fgo.crawl;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class FGOCrawlingTest {

	@Test
	public void test() throws ANoteException {
		FGOCrawling fgo = new FGOCrawling();
		long pubID = 1000;
		String patentID = "US20110177564";
		String saveDocDirectoty = "src/test/resources";
		fgo.getPDFAndUpdateReportUsingPatentID(patentID, saveDocDirectoty, pubID);
	}

}
