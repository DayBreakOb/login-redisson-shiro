package com.mry.http.wrapper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

public class GetHttpServletRequestWrapper extends HttpServletRequestWrapper{

	public GetHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}
	
	public GetHttpServletRequestWrapper(ServletRequest request) {
		super((HttpServletRequest)request);
		// TODO Auto-generated constructor stub
	}

}
