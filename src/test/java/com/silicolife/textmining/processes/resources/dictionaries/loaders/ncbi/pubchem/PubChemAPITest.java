package com.silicolife.textmining.processes.resources.dictionaries.loaders.ncbi.pubchem;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.PubChemAPI;

public class PubChemAPITest {
	
//	@Test
	public void getPatentsByPubchemID() throws ANoteException
	{
		String pubchemID = "1183";
		List<String> result = PubChemAPI.getPatentIdsByPubChemID(pubchemID);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsByPubchemIDNotFound() throws ANoteException
	{
		String pubchemID = "1183000";
		List<String> result = PubChemAPI.getPatentIdsByPubChemID(pubchemID);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsBySmiles() throws ANoteException
	{
		String smiles = "CC(=O)OC1=CC=CC=C1C(=O)O";
		List<String> result = PubChemAPI.getPatentIdsBySMILES(smiles);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsBySmilesNotFound() throws ANoteException
	{
		String smiles = "CC(=O)OC1=CC=CC=C1=O)O";
		List<String> result = PubChemAPI.getPatentIdsBySMILES(smiles);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsByInchIKey() throws ANoteException
	{
		String inchIKey = "SJWWTRQNNRNTPU-ABBNZJFMSA-N";
		List<String> result = PubChemAPI.getPatentIdsByInchIKey(inchIKey);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsByInchIKeyNotFound() throws ANoteException
	{
		String inchIKey = "SJWWTRQNNRNTPU-ABZJFMSA-N";
		List<String> result = PubChemAPI.getPatentIdsByInchIKey(inchIKey);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsByCompundName() throws ANoteException
	{
		String compoundName = "aspirin";
		List<String> result = PubChemAPI.getPatentIdsByCompoundName(compoundName);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPatentsByCompundNameNotFound() throws ANoteException
	{
		String compoundName = "aspilin";
		List<String> result = PubChemAPI.getPatentIdsByCompoundName(compoundName);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPublicationsByPubchemID() throws ANoteException
	{
		String pubchemID = "1183";
		List<String> result = PubChemAPI.getPublicationsByPubChemID(pubchemID);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPublicationsBySmiles() throws ANoteException
	{
		String smiles = "CC(=O)OC1=CC=CC=C1C(=O)O";
		List<String> result = PubChemAPI.getPublicationsIdsBySMILES(smiles);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPublicationsByInchIKey() throws ANoteException
	{
		String inchIKey = "SJWWTRQNNRNTPU-ABBNZJFMSA-N";
		List<String> result = PubChemAPI.getPublicationsIdsByInchIKey(inchIKey);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPublicatiosByCompundName() throws ANoteException
	{
		String compoundName = "aspirin";
		List<String> result = PubChemAPI.getPublicationsByCompoundName(compoundName);
		System.out.println(result.toString());
		assertTrue(true);
	}

//	@Test
	public void getNCBITaxonomyIdsByPubchemID() throws ANoteException
	{
		String pubchemID = "1183";
		List<String> result = PubChemAPI.getNCBITaxonomyByPubChemID(pubchemID);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getNCBITaxonomyIdsBySmiles() throws ANoteException
	{
		String smiles = "CC(=O)OC1=CC=CC=C1C(=O)O";
		List<String> result = PubChemAPI.getNCBITaxonomyIdsBySMILES(smiles);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getNCBITaxonomyIdsByInchIKey() throws ANoteException
	{
		String inchIKey = "SJWWTRQNNRNTPU-ABBNZJFMSA-N";
		List<String> result = PubChemAPI.getNCBITaxonomyIdsByInchIKey(inchIKey);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
	@Test
	public void getCBITaxonomyIdsByCompundName() throws ANoteException
	{
		String compoundName = "aspirin";
		List<String> result = PubChemAPI.getNCBITaxonomyByCompoundName(compoundName);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
}
