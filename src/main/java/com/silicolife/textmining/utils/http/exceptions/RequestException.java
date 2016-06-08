package com.silicolife.textmining.utils.http.exceptions;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class RequestException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public RequestException(String message) {
		super(message);
	}

	protected static String readStream(InputStream response) {
		StringBuilder message = new StringBuilder();
		BufferedReader br = new BufferedReader(new InputStreamReader(response));
		String line;
		try {
			while((line = br.readLine()) != null) message.append(line);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return message.toString();
		
		
	}
	
}
