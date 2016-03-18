package com.silicolife.textmining.processes.ir.springer;

public class SpringerException extends Exception{
	
	private static final long serialVersionUID = 1L;

	public SpringerException(Exception e) {
		super(e);
	}

	public SpringerException(String errorMessage, Exception e) {
		super(errorMessage, e);
	}

	public SpringerException(String errorMessage) {
		super(errorMessage);
	}	

}
