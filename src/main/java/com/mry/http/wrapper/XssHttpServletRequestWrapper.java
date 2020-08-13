package com.mry.http.wrapper;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;

import com.mry.util.JsoupUtil;
import com.mry.util.StringUtils;
import com.mry.util.XssUtil;

public class XssHttpServletRequestWrapper extends HttpServletRequestWrapper {

	public HttpServletRequest getRequest() {
		return request;
	}
	private HttpServletRequest request;
	private boolean isIncludeRichText;
	
	public XssHttpServletRequestWrapper(HttpServletRequest request) {
		// TODO Auto-generated constructor stub
		super(request);
	}
	public XssHttpServletRequestWrapper(HttpServletRequest request,boolean isIncludeRichText) {
		this(request);
		this.request = request;
		this.isIncludeRichText = isIncludeRichText;
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
		if ("content".equals(name)||(name.endsWith("WithHtml")&&!isIncludeRichText)) {
			return XssUtil.xssFilter(super.getParameter(name));
		}
		name = JsoupUtil.clean(name);
		String value = XssUtil.xssFilter(super.getParameter(name));
		if (StringUtils.isNotBlank(value)) {
			return JsoupUtil.clean(value);
		}
		return null;
	}
	
	@Override
	public String[] getParameterValues(String name) {
		// TODO Auto-generated method stub
		String[] strs = super.getParameterValues(name);
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


}
