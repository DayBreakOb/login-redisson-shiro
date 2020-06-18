
package com.mry.chat.letschat.common.util;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.Validate;
import org.springframework.http.MediaType;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;



public class ServletUtils {

	public static final String EXT_PARAMS_PREFIX = "param_";	// 扩展参数前缀
	
	// 定义静态文件后缀；静态文件排除URI地址
	private static String[] staticFiles;
	private static String[] staticFileExcludeUri;
	
	/**
	 * 获取当前请求对象
	 * web.xml: <listener><listener-class>
	 * 	org.springframework.web.context.request.RequestContextListener
	 * 	</listener-class></listener>
	 */
	public static HttpServletRequest getRequest(){
		HttpServletRequest request = null;
		try{
			request = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getRequest();
			if (request == null){
				return null;
			}
			return request;
		}catch(Exception e){
			return null;
		}
	}
	
	/**
	 * 获取当前相应对象
	 * web.xml: <filter><filter-name>requestContextFilter</filter-name><filter-class>
	 * 	org.springframework.web.filter.RequestContextFilter</filter-class></filter><filter-mapping>
	 * 	<filter-name>requestContextFilter</filter-name><url-pattern>/*</url-pattern></filter-mapping>
	 */
	public static HttpServletResponse getResponse(){
		HttpServletResponse response = null;
		try{
			response = ((ServletRequestAttributes)RequestContextHolder.currentRequestAttributes()).getResponse();
			if (response == null){
				return null;
			}
		}catch(Exception e){
			return null;
		}
		return response;
	}
	
	/**
	 * 支持AJAX的页面跳转
	 */
	public static void redirectUrl(HttpServletRequest request, HttpServletResponse response, String url){
		try {
			if (ServletUtils.isAjaxRequest(request)){
				request.getRequestDispatcher(url).forward(request, response); // AJAX不支持Redirect改用Forward
			}else{
				response.sendRedirect(request.getContextPath() + url);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * 是否是Ajax异步请求
	 * @param request
	 */
	public static boolean isAjaxRequest(HttpServletRequest request){
		
		String accept = request.getHeader("accept");
		if (StringUtils.contains(accept, MediaType.APPLICATION_JSON_VALUE)){
			return true;
		}
		
		String xRequestedWith = request.getHeader("X-Requested-With");
		if (StringUtils.contains(xRequestedWith, "XMLHttpRequest")){
			return true;
		}
		
		String ajaxHeader = request.getHeader("__ajax");
		if (StringUtils.inStringIgnoreCase(ajaxHeader, "json", "xml")){
			return true;
		}
		
		String ajaxParameter = request.getParameter("__ajax");
		if (StringUtils.inStringIgnoreCase(ajaxParameter, "json", "xml")){
			return true;
		}
		
		String uri = request.getRequestURI();
		if (StringUtils.endsWithIgnoreCase(uri, ".json")
				|| StringUtils.endsWithIgnoreCase(uri, ".xml")){
			return true;
		}
		
		return false;
	}





	/**
	 * 获得请求参数值
	 */
	public static String getParameter(String name) {
		HttpServletRequest request = getRequest();
		if (request == null){
			return null;
		}
		return request.getParameter(name);
	}
	


	/**
	 * 取得带相同前缀的Request Parameters, copy from spring WebUtils.
	 * 返回的结果的Parameter名已去除前缀.
	 */
	@SuppressWarnings("rawtypes")
	public static Map<String, Object> getParametersStartingWith(ServletRequest request, String prefix) {
		Validate.notNull(request, "Request must not be null");
		Enumeration paramNames = request.getParameterNames();
		Map<String, Object> params = new TreeMap<String, Object>();
		String pre = prefix;
		if (pre == null) {
			pre = "";
		}
		while (paramNames != null && paramNames.hasMoreElements()) {
			String paramName = (String) paramNames.nextElement();
			if ("".equals(pre) || paramName.startsWith(pre)) {
				String unprefixed = paramName.substring(pre.length());
				String[] values = request.getParameterValues(paramName);
				if (values == null || values.length == 0) {
					values = new String[]{};
					// Do nothing, no values found at all.
				} else if (values.length > 1) {
					params.put(unprefixed, values);
				} else {
					params.put(unprefixed, values[0]);
				}
			}
		}
		return params;
	}

	/**
	 * 组合Parameters生成Query String的Parameter部分,并在paramter name上加上prefix.
	 */
	public static String encodeParameterStringWithPrefix(Map<String, Object> params, String prefix) {
		StringBuilder queryStringBuilder = new StringBuilder();
		String pre = prefix;
		if (pre == null) {
			pre = "";
		}
		Iterator<Entry<String, Object>> it = params.entrySet().iterator();
		while (it.hasNext()) {
			Entry<String, Object> entry = it.next();
			queryStringBuilder.append(pre).append(entry.getKey()).append("=").append(entry.getValue());
			if (it.hasNext()) {
				queryStringBuilder.append("&");
			}
		}
		return queryStringBuilder.toString();
	}

	/**
	 * 从请求对象中扩展参数数据，格式：JSON 或  param_ 开头的参数
	 * @param request 请求对象
	 * @return 返回Map对象
	 */
	public static Map<String, Object> getExtParams(ServletRequest request) {
//		Map<String, Object> paramMap = null;
//		String params = StringUtils.trim(request.getParameter(DEFAULT_PARAMS_PARAM));
//		if (StringUtils.isNotBlank(params) && StringUtils.startsWith(params, "{")) {
//			paramMap = JsonMapper.fromJson(params, Map.class);
//		} else {
//			paramMap = getParametersStartingWith(request, DEFAULT_PARAM_PREFIX_PARAM);
//		}
		return getParametersStartingWith(request, EXT_PARAMS_PREFIX);
	}
	



}
