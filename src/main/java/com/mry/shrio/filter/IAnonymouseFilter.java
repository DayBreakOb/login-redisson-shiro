package com.mry.shrio.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authc.AnonymousFilter;

public class IAnonymouseFilter extends AnonymousFilter {

	@Override
	protected boolean onPreHandle(ServletRequest request, ServletResponse response, Object mappedValue) {
		// TODO Auto-generated method stub
		return IViewFilter.filter(request, response, mappedValue,true);
	}

}
