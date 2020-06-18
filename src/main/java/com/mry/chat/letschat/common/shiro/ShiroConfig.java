package com.mry.chat.letschat.common.shiro;

import com.google.common.collect.Lists;
import com.mry.chat.letschat.common.redis.session.IRedissonWebSessionManager;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.mgt.RememberMeManager;
import org.apache.shiro.mgt.SecurityManager;
import org.apache.shiro.session.SessionListener;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.apache.shiro.spring.security.interceptor.AuthorizationAttributeSourceAdvisor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author root
 */
public abstract class ShiroConfig {


    @Value("${shiro.config.sessionValidationInterval}")
    public long sessionValidationInterval;

    @Value("${shiro.config.anno_url}")
    public String anno_url;

    @Value("${shiro.session.sessiontimeOut:360000}")
    public long sessionTimeOut;

    @Value("${shiro.cookie.timeout:3600}")
    public int cookieTimeOUt;

    public static Logger logger = LoggerFactory.getLogger(ShiroConfig.class);




    public String getAnno_url() {
        return anno_url;
    }


    public void setAnno_url(String anno_url) {
        this.anno_url = anno_url;
    }

    public void setSessionValidationInterval(long sessionValidationInterval) {
        this.sessionValidationInterval = sessionValidationInterval;
    }

    public void setCookieTimeOUt(int cookieTimeOUt) {
        this.cookieTimeOUt = cookieTimeOUt;
    }


    protected abstract RememberMeManager cookieRememberMeManager();

    protected abstract CacheManager cacheManager();

    protected abstract SimpleCookie remeberMeCookie();

    /**
     * securityManager
     */

    public List<SessionListener> sessionListeners() {
        List<SessionListener> listeners = Lists.newArrayList();
        listeners.add(new ShiroSessionListener());
        return listeners;
    }


    @Bean
    public SecurityManager securityManager(IRedissonWebSessionManager sessionManager, ShiroRealm shiroRealm) {
        DefaultWebSecurityManager dsm = new DefaultWebSecurityManager();
        dsm.setRealm(shiroRealm);
        dsm.setSessionManager(sessionManager);
       // dsm.setCacheManager(cacheManager());
       // dsm.setRememberMeManager(cookieRememberMeManager());
        SecurityUtils.setSecurityManager(dsm);
        return dsm;
    }


    @Bean
    public ShiroFilterFactoryBean shiroFilterFactoryBean(SecurityManager securityManager) {
        ShiroFilterFactoryBean shiroFilterFactoryBean = new ShiroFilterFactoryBean();
        // 设置 securityManager
        shiroFilterFactoryBean.setSecurityManager(securityManager);

        /**
         * 使用shirofactorybean 管理filter这里不使用spring的 FilterRegistrationBean管理
         *
         * */
        LinkedHashMap<String, Filter> filters = new LinkedHashMap<String, Filter>();
        // filters.put("viewfilter", new ViewFilter());

        shiroFilterFactoryBean.setFilters(filters);
        // 登录的 url
        shiroFilterFactoryBean.setLoginUrl("/login");
        shiroFilterFactoryBean.setSuccessUrl("/index");
        shiroFilterFactoryBean.setUnauthorizedUrl("/unauthorized");
        // 未授权 url
        LinkedHashMap<String, String> filterChainDefinitionMap = new LinkedHashMap<>();
        // 设置免认证 url
        String[] anonUrls = StringUtils.splitByWholeSeparatorPreserveAllTokens(anno_url, ",");
        for (String url : anonUrls) {
            filterChainDefinitionMap.put(url, "anon");
        }
       /* String[] viewfilterunck2 = StringUtils.splitByWholeSeparatorPreserveAllTokens(viewfilterunck, ",");
        for (String htmlview : viewfilterunck2) {
            filterChainDefinitionMap.put(htmlview, "viewfilter");
        }*/
        // 配置退出过滤器，其中具体的退出代码 Shiro已经替我们实现了
        filterChainDefinitionMap.put("/logout", "logout");
        // 除上以外所有 url都必须认证通过才可以访问，未通过认证自动访问 LoginUrl
        //用户拦截器
        //filterChainDefinitionMap.put("/**", "user");
        //表单拦截器
       filterChainDefinitionMap.put("/**", "authc");

        shiroFilterFactoryBean.setFilterChainDefinitionMap(filterChainDefinitionMap);
        logger.info("the ShiroFilterFactoryBean has benn initial ...");
        return shiroFilterFactoryBean;
    }


    //开启注解认证
    @Bean
    public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(SecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor = new AuthorizationAttributeSourceAdvisor();
        authorizationAttributeSourceAdvisor.setSecurityManager(securityManager);
        return authorizationAttributeSourceAdvisor;
    }


    public JavaUuidSessionIdGenerator javaUuidSessionIdGenerator() {
        return new JavaUuidSessionIdGenerator();

    }


}
