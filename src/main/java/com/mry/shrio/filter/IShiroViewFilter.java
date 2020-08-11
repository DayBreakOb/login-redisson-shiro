package com.mry.shrio.filter;

import java.util.ArrayList;
import java.util.HashMap;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.web.filter.AccessControlFilter;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mry.http.wrapper.RequestParameterWrapper;
import com.mry.util.ServletUtils;

public class IShiroViewFilter extends AccessControlFilter{

	private static ArrayList<String> iviews = Lists.newArrayList();
	static {
		//iviews.add("/login.html");

	}
	
	private boolean isPost(HttpServletRequest request) {
		String method = request.getMethod();
		if ("POST".equals(method) || "post".equals(method)) {
			return true;
		}
		return false;
	}

	@Override
	protected boolean isAccessAllowed(ServletRequest arg0, ServletResponse arg1, Object arg2) throws Exception {
		HttpServletRequest request1 = (HttpServletRequest) arg0;
		String severltPath = request1.getServletPath();
		if (!ServletUtils.isAjaxRequest(request1) && (!isPost(request1)) && (severltPath.endsWith(".html"))
				&& !iviews.contains(severltPath)) {
			HashMap<String, Object> map = Maps.newHashMap();
			map.put("viewName", severltPath);
			RequestParameterWrapper reqmapwrapper = new RequestParameterWrapper(request1, map);
			reqmapwrapper.getRequestDispatcher("/view").forward(reqmapwrapper, arg1);
			return true;
		} 
		return false;
	}

	@Override
	protected boolean onAccessDenied(ServletRequest arg0, ServletResponse arg1) throws Exception {
		return IPermissionsAuthorizationFilter.redirectTo403Page(arg0, arg1);
	}

}
