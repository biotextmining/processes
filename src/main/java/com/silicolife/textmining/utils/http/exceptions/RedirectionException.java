package com.silicolife.textmining.utils.http.exceptions;

import java.util.HashMap;
import java.util.Map;

public class RedirectionException extends RequestException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	static Map<Integer, String> messages = new HashMap<Integer, String>();
	static {
		messages.put(300, "Multiple Choices");
		messages.put(301, "Moved Permanently");
		messages.put(302, "Found");
		messages.put(303, "See Other");
		messages.put(304, "Not Modified");
		messages.put(305, "Use Proxy");
		messages.put(307, "Temporary Redirect");
	}


	protected int status;
	protected String location;

	public RedirectionException(int status, String responseMessage) {
		super(status + ": " + responseMessage);
		this.status = status;
		this.location = responseMessage;
	}
	
	public int getStatus() {
		return status;
	}

	public String getLocation() {
		return location;
	}
}
