package com.mry.http.pool;

import java.io.Serializable;

/**
 * @author root
 */
public class HttpResponeEntity implements Serializable{

	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private int statusCode;
	private String entityString;
	private String errorMessage;
	
	
	
	
	public HttpResponeEntity(int statusCode, String entityString, String errorMessage) {
		super();
		this.statusCode = statusCode;
		this.entityString = entityString;
		this.errorMessage = errorMessage;
	}
	public int getStatusCode() {
		return statusCode;
	}
	public String getEntityString() {
		return entityString;
	}

	public String getErrorMessage() {
		return errorMessage;
	}

	
	
	
}
