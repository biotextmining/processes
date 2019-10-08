package com.silicolife.textmining.processes.resources.dictionaries.loaders.kegg.webservice;

import java.io.IOException;
import java.util.List;

import org.junit.Test;

import com.silicolife.textmining.processes.resources.dictionary.loaders.kegg.webservice.KeggWebserviceAPI;

public class KeggWebserviceAPITest {

	@Test
	public void getCompoundList() throws IOException {
		String entityClass = "cpd";
		List<String> list = KeggWebserviceAPI.getEntityStream(entityClass);
		System.out.println(list.size());
		for(String item:list)
		{
			System.out.println(KeggWebserviceAPI.getEntityIDGivenEntityStream(entityClass, item) + " " + KeggWebserviceAPI.getEntityNames(item).toString());
		}
	}

	@Test
	public void getGlycanList() throws IOException {
		String entityClass = "gl";
		List<String> list = KeggWebserviceAPI.getEntityStream(entityClass);
		System.out.println(list.size());
		for(String item:list)
		{
			System.out.println(KeggWebserviceAPI.getEntityIDGivenEntityStream(entityClass, item) + " " + KeggWebserviceAPI.getEntityNames(item).toString());
		}
	}

	@Test
	public void getDrugList() throws IOException {
		String entityClass = "dr";
		List<String> list = KeggWebserviceAPI.getEntityStream(entityClass);
		System.out.println(list.size());
		for(String item:list)
		{
			System.out.println(KeggWebserviceAPI.getEntityIDGivenEntityStream(entityClass, item) + " " + KeggWebserviceAPI.getEntityNames(item).toString());
		}
	}

	@Test
	public void getEnzymesList() throws IOException {
		String entityClass = "ec";
		List<String> list = KeggWebserviceAPI.getEntityStream(entityClass);
		System.out.println(list.size());
		for(String item:list)
		{
			System.out.println(KeggWebserviceAPI.getEntityIDGivenEntityStream(entityClass, item) + " " + KeggWebserviceAPI.getEntityNames(item).toString());
		}
	}
	

	@Test
	public void getGemeOrganismList() throws IOException {
		String keggOrganism = "eco";
		List<String> list = KeggWebserviceAPI.getGenesByOrganismStream(keggOrganism);
		System.out.println(list.size());
		for(String item:list)
		{
			System.out.println(KeggWebserviceAPI.getEntityIDGivenEntityStream(keggOrganism, item) + " " + KeggWebserviceAPI.getEntityNames(item).toString());
		}
	}
	
	@Test
	public void getOrganismList() throws IOException {
		List<String> list = KeggWebserviceAPI.getAvailableOrganismStream();
		System.out.println(list.size());
		for(String item:list)
		{
			String[] splited = item.split("\\t");
			System.out.println(splited[0] + " " + splited[1] + " " + splited[2]);
		}
	}


}
