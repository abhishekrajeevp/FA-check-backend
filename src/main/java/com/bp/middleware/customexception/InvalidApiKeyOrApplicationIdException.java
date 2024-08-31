package com.bp.middleware.customexception;

public class InvalidApiKeyOrApplicationIdException extends Exception{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private int statusCode;
	
	public InvalidApiKeyOrApplicationIdException() {
		
		super();
	}
	
	
	public InvalidApiKeyOrApplicationIdException(String message,int statusCode) {
		
		super(message);
		
		this.statusCode=statusCode;
	}


	public int getStatusCode() {
		return statusCode;
	}

}
