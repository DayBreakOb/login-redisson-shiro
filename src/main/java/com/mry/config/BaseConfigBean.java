package com.mry.config;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javax.annotation.Resource;

import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import com.google.common.collect.Sets;

@Service
public class BaseConfigBean {
	@Resource
	private RequestMappingHandlerMapping requestMappingHandlerMapping;
	

	public Set<String> getUrlAll() {

		Map<RequestMappingInfo, HandlerMethod> hm = requestMappingHandlerMapping.getHandlerMethods();
		 Iterator<RequestMappingInfo> hmdd = hm.keySet().iterator();
		HashSet<String> urls = Sets.newHashSet();
		while (hmdd.hasNext()) {
			RequestMappingInfo cc = hmdd.next();
			urls.add(cc.getPatternsCondition().getPatterns().toString());
		}
		return urls;
	}
}
