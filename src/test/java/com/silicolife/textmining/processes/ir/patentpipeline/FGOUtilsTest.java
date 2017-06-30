package com.silicolife.textmining.processes.ir.patentpipeline;

import java.io.IOException;


import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.*;
import org.junit.Test;

public class FGOUtilsTest {

	@Test
	public void getpatent() throws IOException {
		String patentID = "US20080268216";
		String html = FGOUtils.getPatentTextHTML(patentID);
		System.out.println(html);
	}
	

}
