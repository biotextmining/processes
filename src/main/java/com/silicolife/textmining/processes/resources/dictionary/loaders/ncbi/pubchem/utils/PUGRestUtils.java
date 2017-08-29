package com.silicolife.textmining.processes.resources.dictionary.loaders.ncbi.pubchem.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import com.silicolife.textmining.core.datastructures.general.ExternalIDImpl;
import com.silicolife.textmining.core.datastructures.general.SourceImpl;
import com.silicolife.textmining.core.datastructures.utils.FileHandling;
import com.silicolife.textmining.core.datastructures.utils.GenericPairComparable;
import com.silicolife.textmining.core.datastructures.utils.GenericTriple;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;
import com.silicolife.textmining.core.interfaces.core.general.IExternalID;
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
	private static String operationInchi="property/Inchi";
	private static String operationInchiKey="property/InchiKey";
	private static String operationCanonicalSmiles="property/CanonicalSMILES";
	private static String operationXrefsSBUR="xrefs/SBURL";
	private static String operationNCBITaxonomyIDs="xrefs/TaxonomyID";

	private static String outputFormat=PUGRestOutputEnum.xml.toString(); //xml,json,csv,sdf,txt,png
	private static String outputFormatJson=PUGRestOutputEnum.json.toString(); //xml,json,csv,sdf,txt,png

	private static String fastidentityString="fastidentity";
	

	public static Map<String, Set<String>> getPatentIDsUsingCID(String identifier) throws ANoteException{
		HTTPClient client = new HTTPClient();
		String urlPatentsForAID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + identifier
				+ SEPARATOR + operationPatentIDs + SEPARATOR + outputFormat;
		System.out.println(urlPatentsForAID);
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
		connection.setConnectTimeout(50000);
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

	public static List<String> getPubChemCIDByInchi(String inchi) throws ANoteException
	{
		String urlPubchemForCompoundName= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.inchi.toString()  + SEPARATOR + outputFormatJson;
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("inchi",inchi));
		try {
			List<String> out = new ArrayList<>();
			String jsonStr = fetch(urlPubchemForCompoundName, data);
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONArray pcCompoundsArray = json.getJSONArray("PC_Compounds");
			for(int i=0;i<pcCompoundsArray.length();i++)
			{
				org.json.JSONObject compoundData = (org.json.JSONObject) pcCompoundsArray.get(i);
				org.json.JSONObject  ids = (org.json.JSONObject) compoundData.get("id");
				org.json.JSONObject  id = (org.json.JSONObject) ids.get("id");
				Integer  cid = id.getInt("cid");
				out.add(String.valueOf(cid));
			}
			return out;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}
	
	public static List<String> getPubChemCIDByInchiKey(String inchikey) throws ANoteException {
		String urlPubchemForCompoundName= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.inchikey.toString()  + SEPARATOR + outputFormatJson;
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("inchikey",inchikey));
		try {
			List<String> out = new ArrayList<>();
			String jsonStr = fetch(urlPubchemForCompoundName, data);
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONArray pcCompoundsArray = json.getJSONArray("PC_Compounds");
			for(int i=0;i<pcCompoundsArray.length();i++)
			{
				org.json.JSONObject compoundData = (org.json.JSONObject) pcCompoundsArray.get(i);
				org.json.JSONObject  ids = (org.json.JSONObject) compoundData.get("id");
				org.json.JSONObject  id = (org.json.JSONObject) ids.get("id");
				Integer  cid = id.getInt("cid");
				out.add(String.valueOf(cid));
			}
			return out;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}
	
	public static List<String> getPubChemCIDBySmiles(String smiles) throws ANoteException {
		String urlPubchemForCompoundName= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.smiles.toString()  + SEPARATOR + outputFormatJson;
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("smiles",smiles));
		try {
			List<String> out = new ArrayList<>();
			String jsonStr = fetch(urlPubchemForCompoundName, data);
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONArray pcCompoundsArray = json.getJSONArray("PC_Compounds");
			for(int i=0;i<pcCompoundsArray.length();i++)
			{
				org.json.JSONObject compoundData = (org.json.JSONObject) pcCompoundsArray.get(i);
				org.json.JSONObject  ids = (org.json.JSONObject) compoundData.get("id");
				org.json.JSONObject  id = (org.json.JSONObject) ids.get("id");
				Integer  cid = id.getInt("cid");
				out.add(String.valueOf(cid));
			}
			return out;
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}
	
	public static String getInchiByPubchemCID(String cid) throws ANoteException
	{
		String urlInchiByCID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + cid
				+ SEPARATOR + operationInchi + SEPARATOR + outputFormatJson;
		try {
			URL u = new URL(urlInchiByCID);
			String jsonStr = FileHandling.convertImputStream(u.openStream());
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONObject propertyPropertyTableJson = json.getJSONObject("PropertyTable");
			org.json.JSONArray propertyPropertiesJsonArray = propertyPropertyTableJson.getJSONArray("Properties");
			org.json.JSONObject propertyPropertiesJson  = propertyPropertiesJsonArray.getJSONObject(0);
			return propertyPropertiesJson.getString("InChI");
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
	}
	
	public static String getInchiKeyByPubchemCID(String cid) throws ANoteException
	{
		String urlInchiByCID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + cid
				+ SEPARATOR + operationInchiKey + SEPARATOR + outputFormatJson;
		try {
			URL u = new URL(urlInchiByCID);
			String jsonStr = FileHandling.convertImputStream(u.openStream());
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONObject propertyPropertyTableJson = json.getJSONObject("PropertyTable");
			org.json.JSONArray propertyPropertiesJsonArray = propertyPropertyTableJson.getJSONArray("Properties");
			org.json.JSONObject propertyPropertiesJson  = propertyPropertiesJsonArray.getJSONObject(0);
			return propertyPropertiesJson.getString("InChIKey");
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
	}
	
	public static String getCanonicalSmilesyByPubchemCID(String cid) throws ANoteException
	{
		String urlInchiByCID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + cid
				+ SEPARATOR + operationCanonicalSmiles + SEPARATOR + outputFormatJson;
		try {
			URL u = new URL(urlInchiByCID);
			String jsonStr = FileHandling.convertImputStream(u.openStream());
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONObject propertyPropertyTableJson = json.getJSONObject("PropertyTable");
			org.json.JSONArray propertyPropertiesJsonArray = propertyPropertyTableJson.getJSONArray("Properties");
			org.json.JSONObject propertyPropertiesJson  = propertyPropertiesJsonArray.getJSONObject(0);
			return propertyPropertiesJson.getString("CanonicalSMILES");
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
	}
	
	public static List<IExternalID> getExternalIdsGivenPubchemCID(String cid) throws ANoteException
	{
		List<IExternalID> out = new ArrayList<>();
		List<String> urls = getExternalURLLinks(cid);
		Map<String,GenericTriple<String, String,String>> urlmapper = getURLMap();
		for(String url:urls)
		{
			for(String startsentenceDBLinkMap:urlmapper.keySet())
			{
				if(url.startsWith(startsentenceDBLinkMap))
				{
					String database = urlmapper.get(startsentenceDBLinkMap).getZ();
					String externalID = url;
					externalID = externalID.replace(urlmapper.get(startsentenceDBLinkMap).getX(), "");
					externalID = externalID.replace(urlmapper.get(startsentenceDBLinkMap).getY(), "");
					out.add(new ExternalIDImpl(externalID, new SourceImpl(database)));
				}
			}
		}
		return out;
	}
	
	private static List<String> getExternalURLLinks(String cid) throws ANoteException
	{
		String urlInchiByCID= generalURL + SEPARATOR + database + SEPARATOR 
				+ PUGRestInputEnum.compoundIdentifier.toString() + SEPARATOR + cid
				+ SEPARATOR + operationXrefsSBUR + SEPARATOR + outputFormatJson;
		try {
			List<String> out = new ArrayList<>();
			URL u = new URL(urlInchiByCID);
			String jsonStr = FileHandling.convertImputStream(u.openStream());
			org.json.JSONObject json = new org.json.JSONObject(jsonStr);
			org.json.JSONObject propertyPropertyTableJson = json.getJSONObject("InformationList");
			org.json.JSONArray propertyPropertiesJsonArray = propertyPropertyTableJson.getJSONArray("Information");
			org.json.JSONObject propertyPropertiesJson  = propertyPropertiesJsonArray.getJSONObject(0);
			org.json.JSONArray propertySBURLJsonArray = propertyPropertiesJson.getJSONArray("SBURL");
			for(int i=0;i<propertySBURLJsonArray.length();i++)
			{
				out.add(propertySBURLJsonArray.getString(i));
			}
			return out;
			
		} catch (MalformedURLException e) {
			throw new ANoteException(e);
		} catch (IOException e) {
			throw new ANoteException(e);
		}	
	}

	private static String fetch(String link, List<GenericPairComparable<String, String>> data) throws IOException {
		StringBuilder postData = new StringBuilder();

		for (GenericPairComparable<String,String> param : data) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getX(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getY()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		URL url = new URL(link);
		String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36";
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("POST");
		conn.setRequestProperty("User-Agent", USER_AGENT);
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.setConnectTimeout(50000);
		conn.setReadTimeout(50000);

		conn.getOutputStream().write(postDataBytes);

		int respCode = conn.getResponseCode();

		if (respCode != 200) {
			throw new IOException("StatusCode = " + respCode + " - GET returned not OK.\n" + url);
		}

		BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
		StringBuffer resp = new StringBuffer();
		String inputLine;
		while ((inputLine = in.readLine()) != null)
			resp.append(inputLine);
		in.close();

		return resp.toString();
	}
	
	public static Map<String,GenericTriple<String, String,String>> getURLMap()
	{
		 Map<String,GenericTriple<String,String, String>> out = new HashMap<>();
		 out.put("http://www.ebi.ac.uk/chebi", new GenericTriple<String,String, String>("http://www.ebi.ac.uk/chebi/searchId.do?chebiId=CHEBI:","", "CHEBI"));
		 out.put("http://chembank.broadinstitute.org/chemistry", new GenericTriple<String,String, String>("http://chembank.broadinstitute.org/chemistry/viewMolecule.htm?cbid=","", "CHEMBANK"));
		 out.put("http://chemdb.niaid.nih.gov/CompoundDetails", new GenericTriple<String,String, String>("http://chemdb.niaid.nih.gov/CompoundDetails.aspx?AIDSNO=","", "CHEMDB"));
		 out.put("http://ctdbase.org/detail.go?type", new GenericTriple<String,String, String>("http://ctdbase.org/detail.go?type=chem&acc=","", "CTDBASE"));
		 out.put("http://www.cambridgechem.com/", new GenericTriple<String,String, String>("http://www.cambridgechem.com/","", "CAMBRIDGECHEM"));
		 out.put("http://www.chembase.cn/molecule-", new GenericTriple<String,String, String>("http://www.chembase.cn/molecule-",".html", "CHEMBASE"));
		 out.put("http://www.chemspider.com/Chemical-Structure.", new GenericTriple<String,String, String>("http://www.chemspider.com/Chemical-Structure.",".html", "CHEMSPIDER"));
		 out.put("http://www.drugbank.ca/drugs/", new GenericTriple<String,String, String>("http://www.drugbank.ca/drugs/","", "DRUGBANK"));
		 out.put("http://zinc.docking.org/substances/", new GenericTriple<String,String, String>("http://zinc.docking.org/substances/","/", "ZINC"));
		 out.put("https://www.ebi.ac.uk/chembldb/index.php/compound/inspect/", new GenericTriple<String,String, String>("https://www.ebi.ac.uk/chembldb/index.php/compound/inspect/CHEMBL","", "CHEMBL"));
		 out.put("https://www.molport.com/shop/molecule-link/", new GenericTriple<String,String, String>("https://www.molport.com/shop/molecule-link/","", "MOLPORT"));
		 out.put("https://chem-space.com/", new GenericTriple<String,String, String>("https://chem-space.com/",".html", "CHEMSPACE"));
		 out.put("http://www.ox-chem.com/pc", new GenericTriple<String,String, String>("http://www.ox-chem.com/pc/en/product_item.aspx?ID=","", "OXCHEM"));
		 return out;
	}
}
