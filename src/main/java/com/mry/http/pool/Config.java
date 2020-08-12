package com.mry.http.pool;


import org.apache.http.client.config.RequestConfig;

public class Config {

	private String uuid;

	private String httpsisverify;
	private String keypath;
	private String keypass;
	private String keyinstance;

	private RequestConfig requestConfig;

	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getHttpsisverify() {
		return httpsisverify;
	}

	public void setHttpsisverify(String httpsisverify) {
		this.httpsisverify = httpsisverify;
	}

	public String getKeypath() {
		return keypath;
	}

	public void setKeypath(String keypath) {
		this.keypath = keypath;
	}

	public String getKeypass() {
		return keypass;
	}

	public void setKeypass(String keypass) {
		this.keypass = keypass;
	}

	public String getKeyinstance() {
		return keyinstance;
	}

	public void setKeyinstance(String keyinstance) {
		this.keyinstance = keyinstance;
	}

	public RequestConfig getRequestConfig() {
		return requestConfig;
	}

	public void setRequestConfig(RequestConfig requestConfig) {
		this.requestConfig = requestConfig;
	}

}
