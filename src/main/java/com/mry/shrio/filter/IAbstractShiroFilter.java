package com.mry.shrio.filter;



import org.apache.shiro.web.filter.mgt.FilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;


public class IAbstractShiroFilter extends AbstractShiroFilter {


	public IAbstractShiroFilter(WebSecurityManager securityManager, FilterChainResolver filterChainResolver) {
		if (securityManager == null) {
			throw new IllegalArgumentException(" websecuritymanager is not initial ...");
		}
		setSecurityManager(securityManager);
		if (filterChainResolver != null) {
			setFilterChainResolver(filterChainResolver);
		}
	}

	

}
