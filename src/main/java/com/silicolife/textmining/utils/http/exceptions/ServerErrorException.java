package com.silicolife.textmining.utils.http.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ServerErrorException extends RequestException {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Map<Integer, String> messages = new HashMap<Integer, String>();
	static {
		messages.put(500, "Internal Server Error");
		messages.put(501, "Not Implemented");
		messages.put(502, "Bad Gateway");
		messages.put(503, "Service Unavailable");
		messages.put(504, "Gateway Timeout");
		messages.put(505, "HTTP Version Not Supported");
	}
	
	public ServerErrorException(int status, String responseMessage) {
		super(status + ": " + responseMessage + "\n");
	}
	

}
