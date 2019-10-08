package com.silicolife.textmining.processes.ir.patentrepository;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.Set;

import org.junit.Test;

import com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.patentrepository.PatentEntity;

public class PatentRepositoryAPITest {
	
	private static String url = "http://localhost:8080/patentrepository";
	
	@Test
	public void getPatentIdsBYTest() throws MalformedURLException, IOException
	{
		String query = "PHBs";
		Set<String> patentIds = PatentRepositoryAPI.getPatentIdsGivenTextQuery(url, query);
		System.out.println(patentIds.toString());
	}
	
	@Test
	public void getPatentIMetaInformation() throws MalformedURLException, IOException
	{
		String patentId = "US07788725";
		PatentEntity patent = PatentRepositoryAPI.getPatentMetaInformationByID(url, patentId);
		System.out.println(patent.toString());
	}
	
	@Test
	public void getPatentFullText() throws MalformedURLException, IOException
	{
		String patentId = "US07788725";
		String fullPatent = PatentRepositoryAPI.getPatentFullText(url, patentId);
		System.out.println(fullPatent);
	}

}
