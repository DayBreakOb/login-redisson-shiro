package com.mry.shrio.filter;


import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.AccessControlFilter;
import org.apache.shiro.web.util.WebUtils;

import com.mry.util.SecurityConfigUtils;
import com.mry.util.StringUtils;

public class IAccessControlFilter extends AccessControlFilter{
	
	
	
	/**
	 * case when the request has been refused is have to handle .if handled return the false 
	 * 
	 * */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		return IPermissionsAuthorizationFilter.redirectTo403Page(request, response);
	}

	
	/*
	 * check by yourself is allowed to request ,arg2 is the param of the request ,if allowed return the true
	 * **/
	@Override
	protected boolean isAccessAllowed(ServletRequest arg0, ServletResponse arg1, Object arg2) throws Exception {
		// TODO Auto-generated method stub
		boolean result = false;
		String [] prefix = (String[])arg2;
		if (prefix==null) {
			prefix= SecurityConfigUtils.ALLOW_REMOTE_ADDRESS;
		}
		if ((prefix!=null )&&(arg0 instanceof HttpServletRequest)) {
			String ip = WebUtils.toHttp(arg0).getRemoteAddr();
			for (String tempip :prefix) {
				result=StringUtils.startsWithIgnoreCase(ip, StringUtils.trim(tempip));
				if (result) {
					break;
				}
			}
		}
		return result;
	}

}
