package com.mry.shiro.token;

import java.util.Map;

import org.apache.shiro.authc.UsernamePasswordToken;

public class IUserPasswordToken extends UsernamePasswordToken {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Map<String, Object> extMap;

	private String validCode;
	
	
	

	public Map<String, Object> getExtMap() {
		return extMap;
	}




	public void setExtMap(Map<String, Object> extMap) {
		this.extMap = extMap;
	}




	public String getValidCode() {
		return validCode;
	}




	public void setValidCode(String validCode) {
		this.validCode = validCode;
	}




	public IUserPasswordToken(String username, char[] password, boolean rememberMe, String host, String validCode,
			Map<String, Object> exMap) {
		 super(username,password,rememberMe,host);
		 this.extMap = exMap;
		 this.validCode = validCode;
	}
}
