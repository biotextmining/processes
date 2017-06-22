package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.utils.http.HTTPClient;
import com.silicolife.textmining.utils.http.exceptions.ClientErrorException;
import com.silicolife.textmining.utils.http.exceptions.ConnectionException;
import com.silicolife.textmining.utils.http.exceptions.RedirectionException;
import com.silicolife.textmining.utils.http.exceptions.ResponseHandlingException;
import com.silicolife.textmining.utils.http.exceptions.ServerErrorException;

public class PUGRestUtils {

	private static String SEPARATOR="/";
	private static String generalURL="https://pubchem.ncbi.nlm.nih.gov/rest/pug";
	private static String database= "compound";
	private static String operationPatentIDs="xrefs/patentID";
	private static String operationPUBMEDIDs="xrefs/PubMedID";
	private static String operationSynonyms="synonyms";


	private static String operationNCBITaxonomyIDs="xrefs/TaxonomyID";

	private static String outputFormat=PUGRestOutputEnum.xml.toString(); //xml,json,csv,sdf,txt,png
	private static String fastidentityString="fastidentity";

	public static Map<String, Set<String>> getPatentIDsUsingCID(String identifier) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + identifier
				+ SEPARATOR + operationPatentIDs + SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		try {
			Map<String, Set<String>> mapPubchemIDPatentIDs = client.get(urlPatentsForAID,headers, new PUGRestPatentIDSHandler());
			return mapPubchemIDPatentIDs;
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
	}

	public static Map<String, Set<String>> getPublicationsIDsUsingCID(String identifier) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String urlPublicationsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + identifier
				+ SEPARATOR + operationPUBMEDIDs + SEPARATOR + outputFormat;

		Map<String, String> headers = new HashMap<String, String>();
		try {
			Map<String, Set<String>> mapPubchemIDPublicationsIDs = client.get(urlPublicationsForAID,headers, new PUGRestPublicationIDSHandler());
			return mapPubchemIDPublicationsIDs;
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}

	}

	public static Map<String, Set<String>> getPatentIDsUsingSMILEs(String identifier) throws ANoteException{
		Map<String, Set<String>> patentIDs = new HashMap<>();		
		try {
			identifier=URLEncoder.encode(identifier,"UTF-8");

			String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
					+ fastidentityString + SEPARATOR + PUGRestInputEnum.smiles.toString()
					+ SEPARATOR + identifier
					+ SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();

			ListIterator<Long> iterator;
				iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getPatentIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}
	
	public static Map<String, Set<String>> getPublicationsIDsUsingSMILEs(String identifier) throws ANoteException{
		Map<String, Set<String>> patentIDs = new HashMap<>();		
		try {
			identifier=URLEncoder.encode(identifier,"UTF-8");

			String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
					+ fastidentityString + SEPARATOR + PUGRestInputEnum.smiles.toString()
					+ SEPARATOR + identifier
					+ SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();

			ListIterator<Long> iterator;
				iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getPublicationsIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}


	public static Map<String, Set<String>> getPatentIDsUsingInchiKey(String identifier) throws ANoteException {
		Map<String, Set<String>> patentIDs = new HashMap<>();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
				+ PUGRestInputEnum.inchikey.toString()
				+ SEPARATOR + identifier + SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();
		try{
			ListIterator<Long> iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getPatentIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}
	
	public static Map<String, Set<String>> getPublicationsIDsUsingInchiKey(String identifier) throws ANoteException {
		Map<String, Set<String>> patentIDs = new HashMap<>();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
				+ PUGRestInputEnum.inchikey.toString()
				+ SEPARATOR + identifier + SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();
		try{
			ListIterator<Long> iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getPublicationsIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}


	@SuppressWarnings("unchecked")
	private static ListIterator<Long> getJsonIteratorUsingURL(String urlPatentsForAID) throws IOException, ParseException{
		URL url = new URL(urlPatentsForAID);
		URLConnection connection = url.openConnection();
		InputStream in = connection.getInputStream();
		JSONParser parser= new JSONParser();
		JSONObject jsonObj = (JSONObject) parser.parse(new InputStreamReader(in));
		jsonObj = (JSONObject) jsonObj.get("IdentifierList");
		JSONArray cids = (JSONArray) jsonObj.get("CID");
		ListIterator<Long> iterator = cids.listIterator();
		return iterator;
	}



	public static Map<String, Set<String>> getPatentIDsUsingCompoundName (String compound) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String[] cFractions = compound.split(" ");
		if (cFractions.length>1){
			compound=buildCompoundName(cFractions);
		}
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundName.toString() + SEPARATOR + compound
				+ SEPARATOR + operationPatentIDs + SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, Set<String>> patentIDs;
		try {
			patentIDs = client.get(urlPatentsForAID,headers, new PUGRestPatentIDSHandler());
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}
	

	public static Map<String, Set<String>> getPublicationsIDsUsingCompoundName (String compound) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String[] cFractions = compound.split(" ");
		if (cFractions.length>1){
			compound=buildCompoundName(cFractions);
		}
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundName.toString() + SEPARATOR + compound
				+ SEPARATOR + operationPUBMEDIDs + SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, Set<String>> patentIDs;
		try {
			patentIDs = client.get(urlPatentsForAID,headers, new PUGRestPublicationIDSHandler());
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
		return patentIDs;

	}

	private static String buildCompoundName(String[] cFractions){
		String resultStr = new String();
		for (String frac:cFractions){
			resultStr+=frac +"%20";
		}
		return resultStr.substring(0,resultStr.length()-3);
	}
	
	public static Map<String, Set<String>> getNCBITaxonomyIDsUsingCID(String identifier) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + identifier
				+ SEPARATOR + operationNCBITaxonomyIDs + SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		try {
			Map<String, Set<String>> mapPubchemIDPatentIDs = client.get(urlPatentsForAID,headers, new PUGRestNCBITaxonomyIDSHandler());
			return mapPubchemIDPatentIDs;
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
	}
	
	public static Map<String, Set<String>> getNCBITaxonomyIDsUsingSMILEs(String identifier) throws ANoteException{
		Map<String, Set<String>> patentIDs = new HashMap<>();		
		try {
			identifier=URLEncoder.encode(identifier,"UTF-8");

			String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
					+ fastidentityString + SEPARATOR + PUGRestInputEnum.smiles.toString()
					+ SEPARATOR + identifier
					+ SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();

			ListIterator<Long> iterator;
				iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getNCBITaxonomyIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}
	
	public static Map<String, Set<String>> getNCBITaxonomyIDsUsingInchiKey(String identifier) throws ANoteException {
		Map<String, Set<String>> patentIDs = new HashMap<>();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR
				+ PUGRestInputEnum.inchikey.toString()
				+ SEPARATOR + identifier + SEPARATOR + "cids" + SEPARATOR + PUGRestOutputEnum.json.toString();
		try{
			ListIterator<Long> iterator = getJsonIteratorUsingURL(urlPatentsForAID);
			while (iterator.hasNext()){
				patentIDs.putAll(getNCBITaxonomyIDsUsingCID((iterator.next().toString())));
			}
		} catch (IOException | ParseException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}

	public static Map<String, Set<String>> getNCBITaxonomyIDsUsingCompoundName (String compound) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String[] cFractions = compound.split(" ");
		if (cFractions.length>1){
			compound=buildCompoundName(cFractions);
		}
		String urlNCBITaxonomyIdsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundName.toString() + SEPARATOR + compound
				+ SEPARATOR + operationNCBITaxonomyIDs + SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		Map<String, Set<String>> patentIDs;
		try {
			patentIDs = client.get(urlNCBITaxonomyIdsForAID,headers, new PUGRestNCBITaxonomyIDSHandler());
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
		return patentIDs;
	}
	
	public static List<String> getPubChemCIDByCompoundName(String compoundName) throws ANoteException
	{
		String[] cFractions = compoundName.split(" ");
		if (cFractions.length>1){
			compoundName=buildCompoundName(cFractions);
		}
		HTTPClient client = new HTTPClient();
		String urlPubchemForCompoundName= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundName.toString() + SEPARATOR + compoundName
				+ SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		try {
			List<String> pubchemCIDs = client.get(urlPubchemForCompoundName,headers, new PUGRestPubChemIDHandler());
			return pubchemCIDs;
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
	}
	
	public static List<String> getPubChemNamesByCID(String cid) throws ANoteException
	{
		HTTPClient client = new HTTPClient();
		String urlPubchemSynomysByCID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + cid
				+ SEPARATOR + operationSynonyms +  SEPARATOR + outputFormat;
		Map<String, String> headers = new HashMap<String, String>();
		try {
			List<String> names = client.get(urlPubchemSynomysByCID,headers, new PUGRestPubChemNamesHandler());
			return names;
		} catch (RedirectionException | ClientErrorException | ServerErrorException | ConnectionException
				| ResponseHandlingException e) {
			throw new ANoteException(e);
		}
	}
	
}
