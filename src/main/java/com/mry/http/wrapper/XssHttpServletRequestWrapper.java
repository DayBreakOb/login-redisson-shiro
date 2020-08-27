package com.mry.http.wrapper;

import java.util.Map;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.google.common.collect.Maps;
import com.mry.util.JsoupUtil;
import com.mry.util.StringUtils;
import com.mry.util.XssUtil;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

	public HttpServletRequest getRequest() {
		return request;
	}
	private HttpServletRequest request;
	private boolean isIncludeRichText;
	private Map<String, String[]> parammap = Maps.newHashMap();
	public XssHttpServletRequestWrapper(HttpServletRequest request) {
		// TODO Auto-generated constructor stub
		super(request);
		this.parammap.putAll(request.getParameterMap());
	}
	public XssHttpServletRequestWrapper(HttpServletRequest request,boolean isIncludeRichText) {
		this(request);
		this.request = request;
		this.isIncludeRichText = isIncludeRichText;
		// TODO Auto-generated constructor stub
	}
	
	public XssHttpServletRequestWrapper(HttpServletRequest request,boolean isIncludeRichText,Map<String, String[]> map) {
		this(request);
		this.request = request;
		this.isIncludeRichText = isIncludeRichText;
		// TODO Auto-generated constructor stub
		this.parammap.putAll(map);
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
    public Map<String, String[]> getParameterMap() {
    	// TODO Auto-generated method stub
    	return this.parammap;
    }

	@Override
	public String getParameter(String name) {
		// TODO Auto-generated method stub
		if ("content".equals(name)||(name.endsWith("WithHtml")&&!isIncludeRichText)) {
			return XssUtil.xssFilter(this.getParameterSelf(name));
		}
		name = JsoupUtil.clean(name);
		String value = XssUtil.xssFilter(this.getParameterSelf(name));
		if (StringUtils.isNotBlank(value)) {
			return JsoupUtil.clean(value);
		}
		return null;
	}
	
	@Override
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		String[] strs = this.getParameterValuesSelf(name);
		if (strs!=null) {
			String[] nstrs = new String[strs.length];
			int i=0;
			for (String x :strs) {
				String tempstr = XssUtil.xssFilter(x);
				if (StringUtils.isNotBlank(tempstr)) {
					tempstr = JsoupUtil.clean(tempstr);
				}
				nstrs[i] = tempstr;
				i+=1;
			}
			return nstrs;
		}
		return null;
	}

 
    public static ServletRequest getOrgRequest(ServletRequest servletRequest) {
        if (servletRequest instanceof XssHttpServletRequestWrapper) {
            return ((XssHttpServletRequestWrapper) servletRequest).getRequest();
        }
        return servletRequest;
    }

	public String getParameterSelf(String key) {

		String[] values = parammap.get(key);
		if (values == null || values.length == 0) {
			return null;
		}
		return values[0];
	}
	
	public String[] getParameterValuesSelf(String name) {
		return parammap.get(name);
	}

}
