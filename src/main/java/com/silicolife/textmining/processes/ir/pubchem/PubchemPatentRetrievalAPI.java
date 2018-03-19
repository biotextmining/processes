package com.silicolife.textmining.processes.ir.pubchem;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class PubchemPatentRetrievalAPI {
	
	private static String generalPatentJsonLink = "https://pubchem.ncbi.nlm.nih.gov/rest/pug_view/data/patent/";
	private static String classificationPatentJsonLink = "https://pubchem.ncbi.nlm.nih.gov/classification/cgi/classifications.fcgi?format=json&hid=";
	private static String classificationPatentJsonLinkEnd = "&search_uid=89960&search_uid_type=pid&search_max=10&search_type=tree&search_start=0";


	public static String getPatentJson(String patentID){
		try {
			String url = generalPatentJsonLink + patentID + "/JSON";
			return fetch(url);
		} catch (IOException e) {
			return null;
		}
	}
	
	public static String getPatentClassificationJson(String hid){
		try {
			return fetch(classificationPatentJsonLink + hid + classificationPatentJsonLinkEnd);
		} catch (IOException e) {
			return null;
		}
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


	public static void delay(int seconds) {
		try {
			long ms = seconds * (1000 + ((long) Math.random() * 1000));
			Thread.sleep(ms);
		} catch (InterruptedException ex) {
		}
	}
}
