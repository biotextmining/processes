package com.silicolife.textmining.processes.resources.dictionaries.loaders.ncbi.pubchem;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
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
	
//	@Test
	public void getCBITaxonomyIdsByCompundName() throws ANoteException
	{
		String compoundName = "aspirin";
		List<String> result = PubChemAPI.getNCBITaxonomyByCompoundName(compoundName);
		System.out.println(result.toString());
		System.out.println(result.size());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemCIDByCompundName() throws ANoteException
	{
		String compoundName = "2-amino-3-oxo-3h-phenoxazine-1,9-dicarboxylic acid";
		List<String> result = PubChemAPI.getPubChemCIDsByCompoundName(compoundName);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemCIDByCompundNameNotFound() throws ANoteException
	{
		String compoundName = "2-amino-3-oxo4-3h-phenoxazine-1,9-dicarboxylic acid";
		List<String> result = PubChemAPI.getPubChemCIDsByCompoundName(compoundName);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemNamesByCID() throws ANoteException
	{
		String cid = "2000";
		List<String> names = PubChemAPI.getPubChemNamesByCID(cid);
		System.out.println(names.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemNamesByCIDNotFound() throws ANoteException
	{
		String cid = "118367700005";
		List<String> names = PubChemAPI.getPubChemNamesByCID(cid);
		System.out.println(names.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemCIDsByInchi() throws ANoteException
	{
		String inchi = "InChI=1S/C8H8O3/c1-11-8-4-6(5-9)2-3-7(8)10/h2-5,10H,1H3";
		List<String> result = PubChemAPI.getPubChemCIDsByInchi(inchi);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getPubChemCIDsByInchiNotFound() throws ANoteException
	{
		String inchi = "InChI=1S/C8H8O3/c1-11-8-4-6(5-9)2-3-7(8)10/h2-5,10H,1H";
		List<String> result = PubChemAPI.getPubChemCIDsByInchi(inchi);
		System.out.println(result.toString());
		assertTrue(true);
	}
	
//	@Test
	public void getInchiByPubChemCID() throws ANoteException
	{
		String cid = "1183";
		String result = PubChemAPI.getInchiByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getInchiByPubChemCIDNotFound() throws ANoteException
	{
		String cid = "118367700005";
		String result = PubChemAPI.getInchiByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getInchiKeyByPubChemCID() throws ANoteException
	{
		String cid = "1183";
		String result = PubChemAPI.getInchiKeyByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getInchiKeyByPubChemCIDNotFound() throws ANoteException
	{
		String cid = "118367700005";
		String result = PubChemAPI.getInchiKeyByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getCanonicalSmilesByPubChemCID() throws ANoteException
	{
		String cid = "1183";
		String result = PubChemAPI.getCanonicalSmilesByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getCanonicalSmilesByPubChemCIDNotFound() throws ANoteException
	{
		String cid = "118367700005";
		String result = PubChemAPI.getCanonicalSmilesByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
	@Test
	public void getExternalIdsByPubChemCID() throws ANoteException
	{
		String cid = "15342072";
		List<IExternalID> result = PubChemAPI.getExternalIdsByPubchemID(cid);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsByInhi() throws ANoteException
	{
		String inchi = "InChI=1S/C8H8O3/c1-11-8-4-6(5-9)2-3-7(8)10/h2-5,10H,1H3";
		List<String> result = PubChemAPI.getPubChemCIDsByInchi(inchi);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsByInhiNotFound() throws ANoteException
	{
		String inchi = "InChI=1S/C8H8O3/c1-11-8-4-6(5-9)2-3-7(8)10/h2-5,10H,1H";
		List<String> result = PubChemAPI.getPubChemCIDsByInchi(inchi);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsByInchiKey() throws ANoteException
	{
		String inchikey = "MWOOGOJBHIARFG-UHFFFAOYSA-N";
		List<String> result = PubChemAPI.getPubChemCIDsByInchiKey(inchikey);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsByInchiKeyNotFound() throws ANoteException
	{
		String inchikey = "MWOOGOJBHIARFG-UHFFFAOYSA-N23";
		List<String> result = PubChemAPI.getPubChemCIDsByInchiKey(inchikey);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsBySmiles() throws ANoteException
	{
		String smiles = "COC1=C(C=CC(=C1)C=O)O";
		List<String> result = PubChemAPI.getPubChemCIDsBySmiles(smiles);
		System.out.println(result);
		assertTrue(true);
	}
	
//	@Test
	public void getPubchemIdsBySmilesNotFound() throws ANoteException
	{
		String smiles = "COC1=C(C=CC(=C1)C=O)O1";
		List<String> result = PubChemAPI.getPubChemCIDsBySmiles(smiles);
		System.out.println(result);
		assertTrue(true);
	}
}
