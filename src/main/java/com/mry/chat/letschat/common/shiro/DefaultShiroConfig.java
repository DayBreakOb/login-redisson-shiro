package com.mry.chat.letschat.common.shiro;

import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.cache.ehcache.EhCacheManager;
import org.apache.shiro.session.mgt.SessionManager;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;



/**
 * @author root
 */
public class DefaultShiroConfig{




    public SessionValidationScheduler quartzSessionValidationScheduler(DefaultWebSessionManager webSessionManager) {
        QuartzSessionValidationScheduler quartzSessionValidationScheduler = new QuartzSessionValidationScheduler();
        quartzSessionValidationScheduler.setSessionManager(webSessionManager);
        //remove the invalid session
        webSessionManager.setDeleteInvalidSessions(true);
        //webSessionManager.setSessionValidationInterval(sessionValidationInterval);
        //thr scan  session thread to remove the timeout session
        webSessionManager.setSessionValidationSchedulerEnabled(true);
        //suggest quartz to remove it
        webSessionManager.setSessionValidationScheduler(quartzSessionValidationScheduler);
       // logger.info("the SessionValidationScheduler has benn initial ...");
        return quartzSessionValidationScheduler;
    }


    public SessionManager SessionManager() {
        DefaultWebSessionManager sessionManager = new DefaultWebSessionManager();
        //listener for the session
        //sessionManager.setSessionListeners(sessionListeners());
        //sessionManager.setGlobalSessionTimeout(sessionTimeOut);
        sessionManager.setSessionIdUrlRewritingEnabled(false);
        sessionManager.setSessionIdCookie(new SimpleCookie("MYCOOKIE"));
      //  logger.info("the SessionManager has benn initial ...");
        return sessionManager;
    }



    //@Override
    public CacheManager cacheManager() {
        EhCacheManager ehcachemanager = new EhCacheManager();
        ehcachemanager.init();
        return ehcachemanager;
    }
    /**
     * rememberMe cookie 效果是重开浏览器后无需重新登录
     *
     * @return SimpleCookie
     */
    //@Override
    public CookieRememberMeManager cookieRememberMeManager() {
        CookieRememberMeManager cookieremme = new CookieRememberMeManager();
        cookieremme.setCookie(remeberMeCookie());
        return cookieremme;
    }


   // @Override
    public SimpleCookie remeberMeCookie() {
        //设置 cookie 名称，对应 login.html 页面的 <input type="checkbox" name="rememberMe"/>
        SimpleCookie cookie = new SimpleCookie("remeberMe");
        //设置cookie 过期时间,单位为秒.默认设置为一天
        //cookie.setMaxAge(cookieTimeOUt);
        return cookie;
    }






}
