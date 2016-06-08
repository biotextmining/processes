package com.silicolife.textmining.utils.http.exceptions;

import java.io.IOException;

public class ConnectionException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public ConnectionException(IOException e) {
		super(e);
	}

}
