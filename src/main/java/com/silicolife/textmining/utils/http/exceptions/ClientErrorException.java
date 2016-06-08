package com.silicolife.textmining.utils.http.exceptions;

import java.util.HashMap;
import java.util.Map;

public class ClientErrorException extends RequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	static Map<Integer, String> messages = new HashMap<Integer, String>();
	static {
		messages.put(400, "Bad Request");
		messages.put(401, "Unauthorized");
		messages.put(402, "Payment Required");
		messages.put(403, "Forbidden");
		messages.put(404, "Not Found");
		messages.put(405, "Method Not Allowed");
		messages.put(406, "Not Acceptable");
		messages.put(407, "Proxy Authentication Required");
		messages.put(408, "Request Timeout");
		messages.put(409, "Conflict");
		messages.put(410, "Gone");
		messages.put(411, "Length Required");
		messages.put(412, "Precondition Failed");
		messages.put(413, "Request Entity Too Large");
		messages.put(414, "Request-URI Too Long");
		messages.put(415, "Unsupported Media Type");
		messages.put(416, "Requested Range Not Satisfiable");
		messages.put(417, "Expectation Failed");
	}
	
	public ClientErrorException(int status, String responseMessage) {
		super(status + ":" + responseMessage + "\n");
	}

}
