package com.mry.chat.letschat.common.redis.session;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.mgt.DelegatingSession;
import org.apache.shiro.session.mgt.SessionContext;
import org.apache.shiro.session.mgt.SessionKey;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.servlet.ShiroHttpSession;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.apache.shiro.web.session.mgt.WebSessionManager;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;

/**
 * @author root
 */
public class IRedissonWebSessionManager extends IRedissonSessionManager implements WebSessionManager {

    private Cookie SessionIdCookie;

    private boolean sessionIdCookieEnable;

    private boolean sessionIdUrlRewritingEnable;


    private static Logger logger = LoggerFactory.getLogger(IRedissonWebSessionManager.class);


    public IRedissonWebSessionManager() {
        Cookie cookie = new SimpleCookie(ShiroHttpSession.DEFAULT_SESSION_ID_NAME);
        //cookie.setHttpOnly(true);
        this.SessionIdCookie = cookie;
        this.sessionIdCookieEnable = true;
        this.sessionIdUrlRewritingEnable = true;
    }


    private void storeSessionId(Serializable currSessionId, HttpServletRequest request, HttpServletResponse response) {
        if (currSessionId == null) {
            String msg = "the session id can't be null when keep for subject sequence requests ...";
            throw new IllegalArgumentException(msg);
        }
        Cookie template = getSessionIdCookie();
        Cookie cookie = new SimpleCookie(template);
        String sessionId = currSessionId.toString();
        cookie.setValue(sessionId);
        cookie.saveTo(request, response);
        if (logger.isDebugEnabled()) {
            logger.debug("set the session id cookie for session with id {}", sessionId);
        }
    }


    private void removeSessionIdCookie(HttpServletRequest request, HttpServletResponse response) {
        getSessionIdCookie().removeFrom(request, response);
    }

    private String getSessionIdCookieValue(ServletRequest request, ServletResponse response) {
        if (!isSessionIdCookieEnable()) {
            if (logger.isDebugEnabled()) {
                logger.debug("Session id cookie is disabled -session id will not be acquired from request cookie ...");
            }
            return null;
        }
        if (!(request instanceof HttpServletRequest)) {
            if (logger.isDebugEnabled()) {
                logger.debug("current request is not an httpserveletrequest --can't get session id Cookie returing " +
                        "null ..");
            }
            return null;
        }
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        return getSessionIdCookie().readValue(httpRequest, (HttpServletResponse) response);
    }


    private Serializable getReferenceSessionId(ServletRequest request, ServletResponse response) {
        String sessionId = getSessionIdCookieValue(request, response);
        if (sessionId == null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE, ShiroHttpServletRequest.COOKIE_SESSION_ID_SOURCE);
        } else {
            //not in a cookie, or cookie is disabled - try the request URI as a fallback (i.e. due to URL rewriting):
            //try the URI path segment parameters first:
            sessionId = getUriPathSegementParamValue(request, ShiroHttpSession.DEFAULT_SESSION_ID_NAME);
            if (sessionId == null) {
                //not a URI path segment parameter, try the query parameters:
                String name = getSessionIdName();
                sessionId = request.getParameter(name);
                if (sessionId == null) {
                    sessionId = request.getParameter(name.toLowerCase());
                }
            }
            if (sessionId != null) {
                request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE,
                        ShiroHttpServletRequest.URL_SESSION_ID_SOURCE);
            }

        }
        if (sessionId != null) {
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID, sessionId);
            //automatically mark it valid here.  If it is invalid, the
            //onUnknownSession method below will be invoked and we'll remove the attribute at that time.
            request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_IS_VALID, Boolean.TRUE);
        }
        // always set rewrite flag - SHIRO-361
        request.setAttribute(ShiroHttpServletRequest.SESSION_ID_URL_REWRITING_ENABLED, isSessionIdUrlRewritingEnable());
        return sessionId;
    }

    /**
     * SHIRO-351
     * also see http://cdivilly.wordpress.com/2011/04/22/java-servlets-uri-parameters/
     */
    private String getUriPathSegementParamValue(ServletRequest request, String paramName) {
        if (!(request instanceof HttpServletRequest)) {
            return null;
        }
        HttpServletRequest httpreq = WebUtils.toHttp(request);
        String uri = httpreq.getRequestURI();
        if (uri == null) {
            return null;
        }
        int startindex = uri.indexOf("?");
        if (startindex >= 0) {
            //get rid of the query string
            uri = uri.substring(0, startindex);
        }
        //now check for path segment parameters:
        int index = uri.indexOf(";");
        if (index < 0) {
            //no path segment params - return:
            return null;
        }
        //there are path segment params, let's get the last one that may exist:
        final String token = paramName + "=";
        //uri now contains only the path segment params
        uri = uri.substring(index + 1);
        //we only care about the last JSESSIONID param:
        index = uri.lastIndexOf(";");
        if (index < 0) {
            return null;
        }
        uri = uri.substring(index + token.length());
        //strip off any remaining segment params:
        index = uri.indexOf(";");
        if (index >= 0) {
            uri = uri.substring(0, index);
        }
        return uri;
    }

    private String getSessionIdName() {
        String name = this.SessionIdCookie != null ? this.SessionIdCookie.getName() : null;
        if (name == null) {
            name = ShiroHttpSession.DEFAULT_SESSION_ID_NAME;
        }
        return name;
    }


    @Override
    public boolean isServletContainerSessions() {
        return false;
    }

    @Override
    protected Session createExposedSession(Session session, SessionKey key) {
        if (!WebUtils.isWeb(key)) {
            return super.createExposedSession(session, key);
        }
        return doCreateSession(session, key);
    }

    @Override
    protected Session createExposedSession(Session session, SessionContext context) {
        if (!WebUtils.isWeb(context)) {
            return super.createExposedSession(session, context);
        }
        return doCreateSession(session, context);
    }

    private Session doCreateSession(Session session, Object source) {
        ServletRequest request = WebUtils.getRequest(source);
        ServletResponse response = WebUtils.getResponse(source);
        SessionKey sessionKey = new WebSessionKey(session.getId(), request, response);
        return new DelegatingSession(this, sessionKey);
    }

    /**
     * Stores the Session's ID, usually as a Cookie, to associate with future requests.
     *
     * @param session the session that was just {@link #createSession created}.
     */
    @Override
    protected void onStart(Session session, SessionContext context) {
        super.onStart(session, context);
        if (!WebUtils.isHttp(context)) {
            if (logger.isDebugEnabled()) {
                logger.debug("SessionContext argument is not HTTP compatible or does not have an HTTP request/response " +
                        "pair. No session ID cookie will be set.");
            }
            return;
        }
        HttpServletRequest request = WebUtils.getHttpRequest(context);
        HttpServletResponse response = WebUtils.getHttpResponse(context);
        if (isSessionIdCookieEnable()) {
            Serializable sessionId = session.getId();
            storeSessionId(sessionId, request, response);
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("Session ID cookie is disabled.  No cookie has been set for new session with id {}", session.getId());
            }
        }
        request.removeAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_ID_SOURCE);
        request.setAttribute(ShiroHttpServletRequest.REFERENCED_SESSION_IS_NEW, Boolean.TRUE);
    }

    @Override
    protected Serializable getSessionId(SessionKey sessionKey) {
        Serializable id =  super.getSessionId(sessionKey);
        if (id==null&&WebUtils.isWeb(sessionKey)){
            ServletRequest request = WebUtils.getRequest(sessionKey);
            ServletResponse response = WebUtils.getResponse(sessionKey);
            id = getSessionId(request,response);
        }
        return  id;
    }

    protected Serializable getSessionId(ServletRequest request, ServletResponse response) {
        return getReferenceSessionId(request, response);
    }


    @Override
    protected void onStop(Session session, SessionKey key) {
        super.onStop(session, key);
        if (WebUtils.isHttp(key)){
          HttpServletRequest request= WebUtils.getHttpRequest(key);
          HttpServletResponse response= WebUtils.getHttpResponse(key);
          if (logger.isDebugEnabled()){
              logger.debug("Session has been stopped (subject logout or explicit stop).  Removing session ID cookie.");
          }
          removeSessionIdCookie(request,response);
        }else {
            if (logger.isDebugEnabled()){
                logger.debug("SessionKey argument is not HTTP compatible or does not have an HTTP request/response " +
                        "pair. Session ID cookie will not be removed due to stopped session.");
            }
        }
    }

    public Cookie getSessionIdCookie() {
        return SessionIdCookie;
    }

    public void setSessionIdCookie(Cookie sessionIdCookie) {
        SessionIdCookie = sessionIdCookie;
    }

    public boolean isSessionIdCookieEnable() {
        return sessionIdCookieEnable;
    }

    public void setSessionIdCookieEnable(boolean sessionIdCookieEnable) {
        this.sessionIdCookieEnable = sessionIdCookieEnable;
    }

    public boolean isSessionIdUrlRewritingEnable() {
        return sessionIdUrlRewritingEnable;
    }

    public void setSessionIdUrlRewritingEnable(boolean sessionIdUrlRewritingEnable) {
        this.sessionIdUrlRewritingEnable = sessionIdUrlRewritingEnable;
    }
}
