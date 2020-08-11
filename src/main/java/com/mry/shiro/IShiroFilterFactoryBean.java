package com.mry.shiro;

import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.filter.mgt.FilterChainManager;
import org.apache.shiro.web.filter.mgt.PathMatchingFilterChainResolver;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.BeanInitializationException;

import com.mry.shrio.filter.IAbstractShiroFilter;

public class IShiroFilterFactoryBean extends ShiroFilterFactoryBean {

	private Logger logger = LoggerFactory.getLogger(IShiroFilterFactoryBean.class);

	public AbstractShiroFilter getInstance() throws Exception{
		// TODO Auto-generated method stub
		try {
			return (AbstractShiroFilter) super.getObject();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public Class<?> getObjectType() {
		return IShiroFilterFactoryBean.class;
	}

	@Override
	public Object getObject() throws Exception {
		return this;
	}
	
	@Override
	protected AbstractShiroFilter createInstance() throws Exception {
		logger.debug("create the shirofilter instance ...");
		SecurityManager securityManager=this.getSecurityManager();
		if (securityManager==null) {
			throw new BeanInitializationException("the securityManager initial failure ...");
		}
		if (!(securityManager instanceof WebSecurityManager)) {
			throw new BeanInitializationException("the securityManager is not  WebSecurityManager ...");
		}
		FilterChainManager filterChainManager=this.createFilterChainManager();
		PathMatchingFilterChainResolver pathMatchingFilterChainResolver=new PathMatchingFilterChainResolver();
		pathMatchingFilterChainResolver.setFilterChainManager(filterChainManager);
		
		return new  IAbstractShiroFilter((WebSecurityManager)securityManager, pathMatchingFilterChainResolver);
		
	}
}
