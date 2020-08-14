package com.mry.shrio.filter;

import java.util.ArrayList;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import com.google.common.collect.Lists;
import com.mry.util.ServletUtils;

public class IViewFilter {

	private static boolean isPost(HttpServletRequest request) {
		String method = request.getMethod();
		if ("POST".equals(method) || "post".equals(method)) {
			return true;
		}
		return false;
	}

	private static ArrayList<String> iviews = Lists.newArrayList();
	static {
		// iviews.add("/login.html");

	}

	protected static boolean filter(ServletRequest request, ServletResponse response, Object mappedValue,boolean isanno) {
		HttpServletRequest request1 = (HttpServletRequest) request;
		String severltPath = request1.getServletPath();
		if (!ServletUtils.isAjaxRequest(request1) && (!isPost(request1)) && (severltPath.endsWith(".html"))
				&& !iviews.contains(severltPath)) {
			IPermissionsAuthorizationFilter.forward(request1, response, severltPath,isanno);
			 return false;
		}
		if (!isanno) {
			IPermissionsAuthorizationFilter.forward(request1, response, severltPath,isanno);
		}
		 
		return isanno;
	}
}
