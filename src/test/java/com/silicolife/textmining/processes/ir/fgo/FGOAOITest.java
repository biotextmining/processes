package com.silicolife.textmining.processes.ir.fgo;

import java.io.IOException;

import org.junit.Test;

public class FGOAOITest {

	@Test
	public void getpatent() throws IOException {
		String patentID = "WO2002007850";
		String html = FGOAPI.getPatentTextHTML(patentID);
		System.out.println(html);
	}
	

}
