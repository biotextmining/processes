package com.silicolife.textmining.processes.ir.patentpipeline;

import org.junit.Test;

public class PatentPipelineUtilsTest {

	@Test
	public void test() {
		String patentId = "US08071587";
		System.out.println(PatentPipelineUtils.createPatentIDPossibilities(patentId));
//		System.out.println(PatentPipelineUtils.deleteInitialZeros(patentId));
	}

}
