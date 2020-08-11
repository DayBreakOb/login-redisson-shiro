package com.mry.shiro;

import com.google.common.collect.Lists;
import com.mry.algorithm.crypto.process.impl.AesProcess;
import com.mry.cache.cachemanager.IRedissionShiroCacheManager;
import com.mry.redis.session.IRedissonSessionDao;
import com.mry.redis.session.IRedissonWebSessionManager;
import com.mry.shrio.filter.IAccessControlFilter;
import com.mry.shrio.filter.IFormAuthenticationFilter;
import com.mry.shrio.filter.ILogoutFilter;
import com.mry.shrio.filter.IShiroViewFilter;
import com.mry.util.SecurityConfigUtils;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.servlet.Filter;

import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.mgt.WebSecurityManager;
import org.apache.shiro.web.servlet.AbstractShiroFilter;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;



@Configuration
public class RedissonShiro extends ShiroConfig {

	@Autowired
	private RedissonClient redissonClient;

	@Bean
	@Order(3000)
	@ConditionalOnMissingBean(name = "shiroFilterProxy")
	public FilterRegistrationBean<Filter> shiroFilterProxy(IShiroFilterFactoryBean shirofilter) {
		FilterRegistrationBean<Filter> bean = new FilterRegistrationBean<>();
		AbstractShiroFilter filter = null;
		try {
			filter = shirofilter.getInstance();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		bean.setFilter((Filter) filter);
		bean.addUrlPatterns("/*");
		return bean;
	}

	private IRedissonWebSessionManager SessionManager() {
		IRedissonWebSessionManager sessionManager = new IRedissonWebSessionManager();
		Collection<SessionListener> listeners=Lists.newArrayList();
		listeners.add(new ShiroSessionListener());
		sessionManager.setGlobalSessionTimeout(1800*1000L);
		sessionManager.setSessionListeners(listeners);
		IRedissonSessionDao sessionDAO=new IRedissonSessionDao();
		sessionDAO.setRedissonClient(redissonClient);
		sessionManager.setSessionDAO(sessionDAO);
		sessionManager.setSessionIdUrlRewritingEnable(false);
		return sessionManager;
	}

	
	
	private CacheManager cacheManager() {
		IRedissionShiroCacheManager cachemanager = new IRedissionShiroCacheManager();
		cachemanager.setRedissonClient(redissonClient);
		return cachemanager;
	}

	/**
	 * rememberMe cookie 效果是重开浏览器后无需重新登录
	 *
	 * @return SimpleCookie
	 */
	private CookieRememberMeManager cookieRememberMeManager() {
		CookieRememberMeManager cookieremme = new CookieRememberMeManager();
		cookieremme.setCookie(remeberMeCookie());
		String encryKey = "SHIRO_KEY";
		String remeberKey = AesProcess.AesEncrypt(encryKey, SecurityConfigUtils.AES_REMBER_KEY);
		cookieremme.setCipherKey(AesProcess.hex2Bytes(remeberKey));
		return cookieremme;
	}

	private SimpleCookie remeberMeCookie() {
		// 设置 cookie 名称，对应 login.html 页面的 <input type="checkbox" name="rememberMe"/>
		SimpleCookie cookie = new SimpleCookie("remeberMe");
		// 设置cookie 过期时间,单位为秒.默认设置为一天
		cookie.setMaxAge(3600*365*24);
		return cookie;
	}

	@Bean
	public WebSecurityManager securityManager(ShiroRealm shiroRealm) {
		DefaultWebSecurityManager dsm = new DefaultWebSecurityManager();
		dsm.setRealm(shiroRealm);
		dsm.setSessionManager(SessionManager());
		dsm.setCacheManager(cacheManager());
		dsm.setRememberMeManager(cookieRememberMeManager());
		return dsm;
	}

	@Bean
	public IShiroFilterFactoryBean shiroFilterFactoryBean(WebSecurityManager securityManager) {
		IShiroFilterFactoryBean bean = new IShiroFilterFactoryBean();
		// 设置 securityManager
		bean.setSecurityManager(securityManager);
		// 登录的 url
		// 除上以外所有 url都必须认证通过才可以访问，未通过认证自动访问 LoginUrl
		Map<String, Filter> selfFilters = bean.getFilters();
		selfFilters.put("iaccess", new IAccessControlFilter());
		selfFilters.put("ilogout", new ILogoutFilter());
		selfFilters.put("iform",new IFormAuthenticationFilter());
		selfFilters.put("iviews", new IShiroViewFilter());
		// 未授权 url
		LinkedHashMap<String, String> chains = new LinkedHashMap<>();
		// 设置免认证 url
		String[] anonUrls = StringUtils.splitByWholeSeparatorPreserveAllTokens(anno_url, ",");
		for (String url : anonUrls) {
			chains.put(url, "anon");
		}
		chains.put("/logout", "ilogout");
		chains.put("/**", "iaccess");
		chains.put("/**", "iform");
		chains.put("/*.html", "iviews");
		//filterChainDefinitionMap.put("/**", "user");
		bean.setFilterChainDefinitionMap(chains);
		logger.info("the ShiroFilterFactoryBean has benn initial ...");
		return bean;
	}

}
