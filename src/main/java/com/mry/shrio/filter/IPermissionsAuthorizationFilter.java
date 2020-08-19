package com.mry.shrio.filter;

import java.io.IOException;
import java.util.HashMap;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authz.PermissionsAuthorizationFilter;
import org.apache.shiro.web.util.WebUtils;

import com.google.common.collect.Maps;
import com.mry.config.BaseConfig;
import com.mry.config.PropertyUtil;
import com.mry.http.wrapper.GetHttpServletRequestWrapper;
import com.mry.http.wrapper.RequestParameterWrapper;
import com.mry.util.SecurityConfigUtils;
import com.mry.util.ServletUtils;
import com.mry.util.StringUtils;

/**
 * 
 * auth character filter ...
 */
public class IPermissionsAuthorizationFilter extends PermissionsAuthorizationFilter {

	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		redirectToDefaultPage(request, response);
	}

	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		return redirectTo403Page(request, response);
	}

	/**
	 * auth to the login page ;to the defaultPath(first page)
	 */
	public static void redirectToDefaultPage(ServletRequest request, ServletResponse response) {
		String loginUrl = PropertyUtil.getProperty("shiro.defaultPath",BaseConfig.loginUrl);
		HttpServletRequest req = (HttpServletRequest) request;
		if (StringUtils.equals(req.getContextPath() + loginUrl, req.getRequestURI())) {
			loginUrl = PropertyUtil.getProperty("shiro.loginUrl");
		}

		if (ServletUtils.isAjaxRequest(req)) {
			try {
				request.getRequestDispatcher("/").forward(new GetHttpServletRequestWrapper(request), response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();

			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			try {
				WebUtils.issueRedirect(request, response, "/"+loginUrl);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	public static boolean redirectTo403Page(ServletRequest request, ServletResponse response) {

		Subject subject = SecurityUtils.getSubject();

		if (subject.getPrincipal() == null) {
			redirectToDefaultPage(request, response);
		} else {
			try {
				request.getRequestDispatcher("404.html").forward(request, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return false;
	}

	public static void forward(ServletRequest request, ServletResponse response, String url, boolean isanno) {

		Subject subject = SecurityUtils.getSubject();

		if (subject.getPrincipal() == null && !isanno) {
			redirectToDefaultPage(request, response);
		} else {
			try {
				HashMap<String, Object> map = Maps.newHashMap();
				map.put("viewName", url);
				RequestParameterWrapper requestwrapper = new RequestParameterWrapper((HttpServletRequest) request, map);
				request.getRequestDispatcher("view").forward(requestwrapper, response);
			} catch (ServletException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
