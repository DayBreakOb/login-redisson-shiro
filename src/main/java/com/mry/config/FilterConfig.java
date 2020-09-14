package com.mry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mry.shrio.filter.IUrlFilter;
import com.mry.shrio.filter.IXssFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<IXssFilter> xssFilter() {
		FilterRegistrationBean<IXssFilter> filterbean = new FilterRegistrationBean<>();
		filterbean.setFilter(new IXssFilter());
		filterbean.setOrder(BaseConfig.XssFilterOrder);
		filterbean.setEnabled(true);
		filterbean.addUrlPatterns("/*");
		filterbean.setName("xssfilter");
		Map<String, String> initParameters = new HashMap<>(2);
		initParameters.put("excludes", BaseConfig.xssExcludes);
		initParameters.put("isIncludeRichText", "true");
		filterbean.setInitParameters(initParameters);
		return filterbean;
	}
	

	@Bean
	public FilterRegistrationBean<IUrlFilter> urlfilter() {
		FilterRegistrationBean<IUrlFilter> filterbean = new FilterRegistrationBean<>();
		filterbean.setFilter(new IUrlFilter());
		filterbean.setOrder(BaseConfig.UrlFilterOrder);
		filterbean.setEnabled(true);
		filterbean.addUrlPatterns("/*");
		filterbean.setName("urlfilter");
		Map<String, String> initParameters = new HashMap<>(2);
		initParameters.put("excludes", BaseConfig.postNoParamRequireExcludes);
		filterbean.setInitParameters(initParameters);
		return filterbean;
	}
}
