package com.mry.chat.letschat.common.shiro;

import com.mry.chat.letschat.common.cache.cachemanager.IRedissionShiroCacheManager;
import com.mry.chat.letschat.common.redis.session.IRedissonSessionDao;
import com.mry.chat.letschat.common.redis.session.IRedissonWebSessionManager;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.session.mgt.SessionValidationScheduler;
import org.apache.shiro.session.mgt.quartz.QuartzSessionValidationScheduler;
import org.apache.shiro.web.mgt.CookieRememberMeManager;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.DefaultWebSessionManager;
import org.apache.shiro.web.session.mgt.WebSessionManager;
import org.redisson.api.RedissonClient;
import org.redisson.codec.JsonJacksonCodec;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;


@Configuration
public class RedisShiroConfig extends ShiroConfig {



    @Autowired
    private  RedissonClient redissonClient;

    public SessionValidationScheduler quartzSessionValidationScheduler(DefaultWebSessionManager webSessionManager) {
        QuartzSessionValidationScheduler quartzSessionValidationScheduler = new QuartzSessionValidationScheduler();
        quartzSessionValidationScheduler.setSessionManager(webSessionManager);
        //remove the invalid session
        webSessionManager.setDeleteInvalidSessions(true);
        webSessionManager.setSessionValidationInterval(sessionValidationInterval);
        //thr scan  session thread to remove the timeout session
        webSessionManager.setSessionValidationSchedulerEnabled(true);
        //suggest quartz to remove it
        webSessionManager.setSessionValidationScheduler(quartzSessionValidationScheduler);
        logger.info("the SessionValidationScheduler has benn initial ...");
        return quartzSessionValidationScheduler;
    }


    @Bean
    public IRedissonWebSessionManager SessionManager() {
        IRedissonWebSessionManager sessionManager = new IRedissonWebSessionManager();
        //listener for the session
        //sessionManager.setSessionListeners(sessionListeners());
       // sessionManager.setGlobalSessionTimeout(sessionTimeOut);
        //sessionManager.setSessionIdUrlRewritingEnable(true);
        IRedissonSessionDao sessiondao = new IRedissonSessionDao();
        sessiondao.setRedissonClient(redissonClient);
        //sessiondao.setCodec(new JsonJacksonCodec());
        sessionManager.setSessionDAO(sessiondao);
        //SimpleCookie cookie = new SimpleCookie("MYCOOKIE");
        //cookie.setHttpOnly(true);
        //sessionManager.setSessionIdCookie(cookie);
        //sessionManager.setSessionIdCookieEnable(true);
        logger.info("the SessionManager has benn initial ...");
        return sessionManager;
    }


    @Override
    protected CacheManager cacheManager() {
        IRedissionShiroCacheManager cachemanager = new IRedissionShiroCacheManager();
        cachemanager.setRedissonClient(redissonClient);
        cachemanager.setAllowNullValues(true);
        cachemanager.setCodec(new JsonJacksonCodec());
        cachemanager.setConfigLocation("classpath:config_cache_shiro.json");
        cachemanager.init();
        return cachemanager;
    }

    /**
     * rememberMe cookie 效果是重开浏览器后无需重新登录
     *
     * @return SimpleCookie
     */
    @Override
    protected CookieRememberMeManager cookieRememberMeManager() {
        CookieRememberMeManager cookieremme = new CookieRememberMeManager();
        cookieremme.setCookie(remeberMeCookie());
        return cookieremme;
    }

    @Override
    protected SimpleCookie remeberMeCookie() {
        //设置 cookie 名称，对应 login.html 页面的 <input type="checkbox" name="rememberMe"/>
        SimpleCookie cookie = new SimpleCookie("remeberMe");
        //设置cookie 过期时间,单位为秒.默认设置为一天
        cookie.setMaxAge(cookieTimeOUt);
        return cookie;
    }


}
