package com.silicolife.textmining.processes.resources.dictionary.loaders.chemspider;

import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.json.XML;

import com.silicolife.textmining.core.datastructures.utils.GenericPairComparable;
import com.silicolife.textmining.core.interfaces.core.dataaccess.exception.ANoteException;

public class ChemSpiderAPI {

	static String urlGetCSIDByInchi  = "http://www.chemspider.com/InChI.asmx/InChIToCSID";

	public static String getCSIDGivenInchi(String accessToken, String inchi) throws ANoteException
	{
		String xml;
		try {
			xml = apiCall(urlGetCSIDByInchi, apiInChIToCSIDData(accessToken,inchi));
			// should return something like:
			// <?xml version="1.0" encoding="utf-8"?><string xmlns="http://www.chemspider.com/">1906</string>
			JSONObject toJSON = XML.toJSONObject(xml);
			// toJSON.string.content should have the ID
			Integer id = toJSON.getJSONObject("string").getInt("content");
			return String.valueOf(id);
		} catch (IOException e) {
			throw new ANoteException(e);
		}
	}

	private static String apiCall(String endpoint, List<GenericPairComparable<String, String>> data) throws IOException {
		StringBuilder postData = new StringBuilder();
		URL url = new URL(endpoint);
		for (GenericPairComparable<String,String> param : data) {
			if (postData.length() != 0) postData.append('&');
			postData.append(URLEncoder.encode(param.getX(), "UTF-8"));
			postData.append('=');
			postData.append(URLEncoder.encode(String.valueOf(param.getY()), "UTF-8"));
		}
		byte[] postDataBytes = postData.toString().getBytes("UTF-8");

		HttpURLConnection conn = (HttpURLConnection)url.openConnection();
		conn.setRequestMethod("POST");
		conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
		conn.setRequestProperty("Content-Length", String.valueOf(postDataBytes.length));
		conn.setDoOutput(true);
		conn.getOutputStream().write(postDataBytes);
		StringWriter writer = new StringWriter();
		IOUtils.copy(conn.getInputStream(), writer);
		String theString = writer.toString();
		return theString;
	}

	private static List<GenericPairComparable<String, String>> apiInChIToCSIDData(String token,String inchi) {
		List<GenericPairComparable<String, String>> data = new ArrayList<>();
		data.add(new GenericPairComparable<String, String>("inchi", inchi));
	    data.add(new GenericPairComparable<String, String>("token", token));
		return data;
	}

}
