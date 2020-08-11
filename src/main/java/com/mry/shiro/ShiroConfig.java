package com.mry.shiro;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;


/**
 * @author root
 */
public  class ShiroConfig {

	
	
	
	public long sessionValidationInterval;

	@Value("${shiro.config.anno_url}")
	public String anno_url;

	public long sessionTimeOut;

	public int cookieTimeOUt;

	public static Logger logger = LoggerFactory.getLogger(ShiroConfig.class);

	public String getAnno_url() {
		return anno_url;
	}

	public void setAnno_url(String anno_url) {
		this.anno_url = anno_url;
	}

	public void setSessionValidationInterval(long sessionValidationInterval) {
		this.sessionValidationInterval = sessionValidationInterval;
	}

	public void setCookieTimeOUt(int cookieTimeOUt) {
		this.cookieTimeOUt = cookieTimeOUt;
	}

	



}
