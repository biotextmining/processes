package com.silicolife.textmining.processes.ir.fgo;

import java.io.IOException;

import org.junit.Test;

public class FGOParserTest {
	
//	@Test
	public void getpatentMeta() throws IOException {
		String patentID = "CN105061174";
		FGOPatentDataObject fgoPatentDataObject = FGOParser.retrieveMetaInformation(patentID);
		System.out.println(fgoPatentDataObject);
	}
	
	@Test
	public void getpatentFull() throws IOException {
		String patentID = "WO2002007850";
		FGOPatentDataObject fgoPatentDataObject = FGOParser.retrieveFullInformation(patentID);
		System.out.println(fgoPatentDataObject);
	}

}
