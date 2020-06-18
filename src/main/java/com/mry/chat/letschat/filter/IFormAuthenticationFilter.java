package com.mry.chat.letschat.filter;

import com.google.common.collect.Maps;
import com.mry.chat.letschat.common.util.ServletUtils;
import com.mry.chat.letschat.system.pojo.User;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Enumeration;
import java.util.Map;


public class IFormAuthenticationFilter extends FormAuthenticationFilter {


    private static Logger logger = LoggerFactory.getLogger(IFormAuthenticationFilter.class);

    @Override
    protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request, ServletResponse response) throws Exception {

        User user = (User) subject.getPrincipal();
        HttpSession session = WebUtils.toHttp(request).getSession(false);

        if (logger.isDebugEnabled()) {
            Enumeration<String> attrs = session.getAttributeNames();
            while (attrs.hasMoreElements()) {
                logger.debug("the session attr name is " + attrs.nextElement() + "the values is " + session.getAttribute(attrs.nextElement()));
            }

        }
        session.setAttribute("activeUser", user);
        return super.onLoginSuccess(token, subject, request, response);
    }

    @Override
    protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request, ServletResponse response) {

        logger.info("the login is faulure ....");
        return super.onLoginFailure(token, e, request, response);
    }

    @Override
    protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {

        logger.info("the accesss is allowed ...");
        return super.isAccessAllowed(request, response, mappedValue);
    }


    public static Map<String, Object> getLoginData(HttpServletRequest request, HttpServletResponse response) {
        Map<String,Object> data=Maps.newHashMap();

        Map<String, Object> parammap = ServletUtils.getExtParams(request);
        for (Map.Entry<String,Object> entry: parammap.entrySet()){
            data.put(ServletUtils.EXT_PARAMS_PREFIX+entry.getKey(),entry.getValue());
        }

        return data;
    }
}
