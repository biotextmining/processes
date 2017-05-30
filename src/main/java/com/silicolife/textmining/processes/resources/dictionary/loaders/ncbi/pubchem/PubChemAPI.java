package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils.PUGRestUtils;

public class PubChemAPI {
	
	/**
	 * Method that return a list of Patent Id associated with  PubChem ID (CID)
	 * 
	 * @param pubchemID
	 * @return PatentID List according to search on PubChem by PubChem ID (CID)
	 * @throws ANoteException 
	 */
	public static List<String> getPatentIdsByPubChemID(String pubchemID) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPatentIDsUsingCID(pubchemID);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}

	/**
	 * Method that return a list of Patent Id associated with PubChem SMILES Entity
	 * 
	 * @param smiles
	 * @return PatentID List according to search on PubChem by SMILES
	 * @throws ANoteException
	 */
	public static List<String> getPatentIdsBySMILES(String smiles) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPatentIDsUsingSMILEs(smiles);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}

	/**
	 * Method that return a list of Patent Id associated with PubChem InchIKey Entity 
	 * 
	 * @param inchIKey
	 * @return PatentID List according to search on PubChem by InchIKey
	 * @throws ANoteException
	 */
	public static List<String> getPatentIdsByInchIKey(String inchIKey) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPatentIDsUsingInchiKey(inchIKey);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}

	/**
	 * Method that return a list of Patent Id associated with PubChem Compound Name
	 * Important Note that compound name could be ambiguous and the result are a combination of multiple Pubchem Entities 
	 * 
	 * @param compoundName
	 * @return PatentID List according to search on PubChem By Name
	 * @throws ANoteException
	 */
	public static List<String> getPatentIdsByCompoundName(String compoundName) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPatentIDsUsingCompoundName(compoundName);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of Pubmed Id associated with  PubChem ID (CID)
	 * 
	 * @param pubchemID
	 * @return Pubmed Id List according to search on PubChem by PubChem ID (CID)
	 * @throws ANoteException
	 */
	public static List<String> getPublicationsByPubChemID(String pubchemID) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPublicationsIDsUsingCID(pubchemID);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of Publications Id associated with PubChem SMILES Entity
	 * 
	 * @param smiles
	 * @return Publications Ids List according to search on PubChem by SMILES
	 * @throws ANoteException
	 */
	public static List<String> getPublicationsIdsBySMILES(String smiles) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPublicationsIDsUsingSMILEs(smiles);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	

	/**
	 * Method that return a list of Patent Id associated with PubChem InchIKey Entity 
	 * 
	 * @param inchIKey
	 * @return PatentID List according to search on PubChem by InchIKey
	 * @throws ANoteException
	 */
	public static List<String> getPublicationsIdsByInchIKey(String inchIKey) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPublicationsIDsUsingInchiKey(inchIKey);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of Publications Id associated with PubChem Compound Name
	 * Important Note that compound name could be ambiguous and the result are a combination of multiple Pubchem Entities 
	 * 
	 * @param compoundName
	 * @return Publication Ids List according to search on PubChem By Name
	 * @throws ANoteException
	 */
	public static List<String> getPublicationsByCompoundName(String compoundName) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getPublicationsIDsUsingCompoundName(compoundName);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of NCBI Taxonomy Id associated with PubChem ID (CID)
	 * 
	 * @param pubchemID
	 * @return NCBI Taxonomy Id List according to search on PubChem by PubChem ID (CID)
	 * @throws ANoteException
	 */
	public static List<String> getNCBITaxonomyByPubChemID(String pubchemID) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getNCBITaxonomyIDsUsingCID(pubchemID);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of NCBI Taxonomy Id associated with PubChem SMILES Entity
	 * 
	 * @param smiles
	 * @return NCBI Taxonomy List according to search on PubChem by SMILES
	 * @throws ANoteException
	 */
	public static List<String> getNCBITaxonomyIdsBySMILES(String smiles) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getNCBITaxonomyIDsUsingSMILEs(smiles);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of NCBI Taxonomy Id associated with PubChem InchIKey Entity 
	 * 
	 * @param inchIKey
	 * @return NCBI Taxonomy List according to search on PubChem by InchIKey
	 * @throws ANoteException
	 */
	public static List<String> getNCBITaxonomyIdsByInchIKey(String inchIKey) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getNCBITaxonomyIDsUsingInchiKey(inchIKey);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return a list of NCBI Taxonomy Id associated with PubChem Compound Name
	 * Important Note that compound name could be ambiguous and the result are a combination of multiple Pubchem Entities 
	 * 
	 * @param compoundName
	 * @return NCBI Taxonomy Ids List according to search on PubChem By Name
	 * @throws ANoteException
	 */
	public static List<String> getNCBITaxonomyByCompoundName(String compoundName) throws ANoteException
	{
		Map<String, Set<String>> result = PUGRestUtils.getNCBITaxonomyIDsUsingCompoundName(compoundName);
		Set<String> tmpOut = new HashSet<>();
		for(Set<String> keyresult:result.values())
		{
			tmpOut.addAll(keyresult);	
		}
		List<String> out = new ArrayList<>(tmpOut);
		return out;
	}
	
	/**
	 * Method that return the PubChem CID associated with PubChem Compound Name.
	 * 
	 * @param compoundName
	 * @return PubChem CID according to search on PubChem By Name
	 * @throws ANoteException - return if not found any entity or error occur
	 */
	public static String getPubChemCIDByCompoundName(String compoundName) throws ANoteException
	{
		String out = PUGRestUtils.getPubChemCIDByCompoundName(compoundName);
		return out;
	}
	
	/**
	 * Method that return all names associated to Pubchem CID ( Position Zero refer to prefer name)
	 * 
	 * @param cid - Pubchem CID
	 * @return Compound name list for PubChem CID
	 * @throws ANoteException throw if not found any name or error occur
	 */
	public static List<String> getPubChemNamesByCID(String cid) throws ANoteException
	{
		List<String> out = PUGRestUtils.getPubChemNamesByCID(cid);
		return out;
	}
}
