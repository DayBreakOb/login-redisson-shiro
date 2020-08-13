package com.mry.config;

import java.util.HashMap;
import java.util.Map;

import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.mry.shrio.filter.IXssFilter;

@Configuration
public class FilterConfig {

	@Bean
	public FilterRegistrationBean<IXssFilter> xssFilter() {
		FilterRegistrationBean<IXssFilter> filterbean = new FilterRegistrationBean<>();
		filterbean.setFilter(new IXssFilter());
		filterbean.setOrder(0);
		filterbean.setEnabled(true);
		filterbean.addUrlPatterns("/*");
		filterbean.setName("xssfilter");
		Map<String, String> initParameters = new HashMap<>(2);
		initParameters.put("excludes", "/favicon.ico,/img/*,/js/*,/css/*/assets/*,/dist/*,/vendor/*,/images/*");
		initParameters.put("isIncludeRichText", "true");
		filterbean.setInitParameters(initParameters);
		return filterbean;
	}
}
