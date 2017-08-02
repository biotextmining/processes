package com.silicolife.textmining.processes.ir.patentpipeline.components.metainfomodules.fgo.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.json.JSONArray;
import org.json.JSONObject;

import com.silicolife.textmining.core.datastructures.utils.GenericPairComparable;

public class FGOUtils {

	private final static boolean _AddSynonymsToQuery = true;
	private final static boolean _UseGoogleCustomSearchAPI = false;

	public static String getPatentTextHTML(String patentID){
		try {
			return fetch("https://www.google.com/patents/" + patentID);
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return null;
		}
	}

	public static Set<String> getPatentIDsForCompanyPatentsGivenInchi(String inchi, String company_name) throws IOException {
		// use the inchi to get all synonyms
		List<String> names = namesFromPubchem(inchi, true);
		// get all patent #s that mention these names AND the customer company_name
		Set<String> idSet = queryGoogleForPatentsOfCustomer(company_name, names);
		return idSet;
	}

	public static Set<String> getPatentIDs(String inchi) throws IOException {
		// use the inchi to get all synonyms
		List<String> names = namesFromPubchem(inchi, true);
		// get all patent #s that mention these names
		Set<String> idSet = queryGoogleForPatentIDs(null, names);

		return idSet;
	}


	private static Set<String> queryGoogleForPatentIDs(String common_name, List<String> names) throws IOException {
		if (!_AddSynonymsToQuery && common_name != null) {
			names = new ArrayList<String>();
			names.add(common_name);
		}

		// String searchPhrase = "(cerevisiae OR coli) AND (";
		String searchPhrase = "(yeast OR cerevisiae OR coli) AND (";
		searchPhrase+= "\"" + names.get(0) + "\"";
		for(int i=1; i<names.size(); i++)
			searchPhrase+= " OR \"" + names.get(i) + "\"";
		searchPhrase+= ")";

		Set<String> idSet = new HashSet<String>();
		if (_UseGoogleCustomSearchAPI)
			idSet.addAll(QueryFGOAPI.query(searchPhrase));
		idSet.addAll(QueryFGONonAPI.retrievedPatentIds(searchPhrase));
		return idSet;
	}

	private static Set<String> queryGoogleForPatentsOfCustomer(String company, List<String> names) throws IOException {

		String searchPhrase = "inassignee:\"" + company + "\" AND (";
		searchPhrase+= "\"" + names.get(0) + "\"";
		for(int i=1; i<names.size(); i++)
			searchPhrase+= " OR \"" + names.get(i) + "\"";
		searchPhrase+= ")";
		return QueryFGONonAPI.retrievedPatentIds(searchPhrase);
	}

	private static List<String> namesFromPubchem(String name, boolean inputIsInChI) throws IOException {
		List<String> out = new ArrayList<>();

		// Query pubchem for synonyms
		String jsonStr;
		if (inputIsInChI) {
			String base = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/inchi/synonyms/json";
			List<GenericPairComparable<String, String>> post_data = new ArrayList<>();
			post_data.add(new GenericPairComparable<String, String>("inchi", name));
			jsonStr = FGOUtils.fetch(base, post_data);
		} else {
			String base = "https://pubchem.ncbi.nlm.nih.gov/rest/pug/compound/name/";
			name = URLEncoder.encode(name, "UTF-8");
			String pubchem_query = base + name + "/synonyms/json";
			jsonStr = FGOUtils.fetch(pubchem_query);
		}

		JSONObject json = new JSONObject(jsonStr);
		JSONObject InformationList = json.getJSONObject("InformationList");
		JSONArray Information = InformationList.getJSONArray("Information");
		JSONObject data = Information.getJSONObject(0);
		JSONArray Synonym = data.getJSONArray("Synonym");
		for(int i=0; i < Synonym.length(); i++) {
			String syn = Synonym.getString(i);

			out.add(syn);
			if(out.size() > 5) {
				break;
			}
		}
		return out;
	}

	protected static String fetch(String link) throws IOException {
		URL url = new URL(link);
		String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2272.76 Safari/537.36";
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();

		conn.setRequestMethod("GET");
		conn.setRequestProperty("User-Agent", USER_AGENT);
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

		conn.getOutputStream().write(postDataBytes);

		int respCode = conn.getResponseCode();
		System.err.println("\nSearch Sending 'GET' request to URL : " + url);
		System.err.println("Response Code : " + respCode);

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

	public static void delay(int seconds) {
		try {
			long ms = seconds * (1000 + ((long) Math.random() * 1000));
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
		}
	}
}
