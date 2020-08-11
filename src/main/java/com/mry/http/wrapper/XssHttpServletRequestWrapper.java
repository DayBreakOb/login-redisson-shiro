package com.mry.http.wrapper;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.mry.util.XssUtil;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

	public XssHttpServletRequestWrapper(HttpServletRequest request) {
		super(request);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	public String getHeader(String name) {
		// TODO Auto-generated method stub
		return super.getHeader(name);
	}
	

	@Override
	public String getQueryString() {
		// TODO Auto-generated method stub
		return super.getQueryString();
	}

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		return XssUtil.xssFilter(super.getParameter(name));
	}
	
	@Override
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		String[] strs = super.getParameterValues(name);
		if (strs!=null) {
			String[] nstrs = new String[strs.length];
			int i=0;
			for (String x :strs) {
				nstrs[i] = XssUtil.xssFilter(x);
				i+=1;
			}
			return nstrs;
		}
		return null;
	}

}
