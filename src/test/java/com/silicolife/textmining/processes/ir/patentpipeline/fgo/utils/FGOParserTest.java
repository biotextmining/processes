package com.silicolife.textmining.processes.ir.patentpipeline.fgo.utils;

import java.io.IOException;

import org.junit.Test;

import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.FGOPatentDataObject;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.utils.FGOParser;

public class FGOParserTest {
	
//	@Test
	public void getpatentMeta() throws IOException {
		String patentID = "US20080268216";
		FGOPatentDataObject fgoPatentDataObject = FGOParser.retrieveMetaInformation(patentID);
		System.out.println(fgoPatentDataObject);
	}
	
	@Test
	public void getpatentFull() throws IOException {
		String patentID = "US20080268216";
		FGOPatentDataObject fgoPatentDataObject = FGOParser.retrieveFullInformation(patentID);
		System.out.println(fgoPatentDataObject);
	}

}
