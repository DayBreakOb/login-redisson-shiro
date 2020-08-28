package com.mry.controller;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;

import com.google.common.collect.Maps;
import com.mry.http.wrapper.RequestParameterWrapper;
import com.mry.http.wrapper.XssHttpServletRequestWrapper;
import com.mry.system.pojo.User;

public class BaseController {

	protected static String SUCCESS = "SUCCESS";
	
	protected static String FAILURE = "FAILURE";

	private Subject getSubject() {
		return SecurityUtils.getSubject();
	}

	protected User getCUrrentUser() {
		return (User) getSubject().getPrincipal();
	}

	protected Session getSession() {
		return getSubject().getSession();
	}

	protected Session getSession(boolean flag) {
		return getSubject().getSession(flag);
	}

	protected ServletRequest getRequest(ServletRequest request) {

		if (request instanceof ShiroHttpServletRequest) {
			return (ShiroHttpServletRequest) request;
		} else if (request instanceof RequestParameterWrapper) {
			return RequestParameterWrapper.getOrgRequest(request);
		} else if (request instanceof XssHttpServletRequestWrapper) {
			return XssHttpServletRequestWrapper.getOrgRequest(request);
		}

		return request;
	}

	protected Map<String, String> getAllParameter(ServletRequest request){
		HashMap<String, String> map = Maps.newHashMap();
		ServletRequest req = getRequest(request);
		Map<String, String[]> paramap=req.getParameterMap();
		Iterator<Entry<String, String[]>> iter = paramap.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<String, String[]> it = iter.next();
			map.put(it.getKey(), it.getValue()[0].toString());
		}
		return map;
	}
}
