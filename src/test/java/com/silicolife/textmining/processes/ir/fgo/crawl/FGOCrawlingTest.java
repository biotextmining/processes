package com.silicolife.textmining.processes.ir.fgo.crawl;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class FGOCrawlingTest {

	@Test
	public void test() throws ANoteException {
		FGOCrawling fgo = new FGOCrawling();
		long pubID =2002007850L;
		String patentID = "WO2002007850";
		String saveDocDirectoty = "src/test/resources";
		fgo.getPDFAndUpdateReportUsingPatentID(patentID, saveDocDirectoty, pubID);
	}

}
