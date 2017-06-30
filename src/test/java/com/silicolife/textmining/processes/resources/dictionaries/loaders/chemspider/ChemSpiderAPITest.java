package com.silicolife.textmining.processes.resources.dictionaries.loaders.chemspider;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.resources.dictionary.loaders.chemspider.ChemSpiderAPI;

public class ChemSpiderAPITest {

	@Test
	public void test() throws ANoteException {
		String inchi = "InChI=1S/C8H8O3/c1-11-8-4-6(5-9)2-3-7(8)10/h2-5,10H,1H3";
		String accessToken = "b3d12bfe-1bcd-4960-a30f-ba876fe7a0fb";
		String result = ChemSpiderAPI.getCSIDGivenInchi(accessToken, inchi);
		System.out.println(result);
	}

}
