package com.silicolife.textmining.processes.ir.pubchem;

import org.junit.Test;

public class PubchemPatentParserTest {

	@Test
	public void test() {
		String patentID = "EP0273689";
		PubchemPatentDataObject patentObject = PubchemPatentParser.retrieveMetaInformation(patentID);
		System.out.println(patentObject.toString());
	}

}
