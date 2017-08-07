package com.silicolife.textmining.processes.ir.fgo;

import java.io.IOException;

import com.silicolife.textmining.processes.ir.fgo.FGOAPI;
import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.*;

import org.junit.Test;

public class FGOAOITest {

	@Test
	public void getpatent() throws IOException {
		String patentID = "US20080268216";
		String html = FGOAPI.getPatentTextHTML(patentID);
		System.out.println(html);
	}
	

}
