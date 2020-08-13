package com.mry.redis.session;

import org.apache.catalina.connector.RequestFacade;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.session.mgt.eis.SessionDAO;
import org.apache.shiro.web.servlet.ShiroHttpServletRequest;
import org.apache.shiro.web.session.mgt.WebSessionKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mry.http.wrapper.XssHttpServletRequestWrapper;

import javax.servlet.ServletRequest;
import java.io.Serializable;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author root
 */
public class IRedissonSessionManager extends AbstractNativeSessionManager {

	private final static AtomicInteger sessioncount = new AtomicInteger(0);

	private Logger logger = LoggerFactory.getLogger(IRedissonSessionManager.class);

	private SessionFactory sessionFactory;

	private SessionDAO sessionDAO;

	public IRedissonSessionManager() {
		this.sessionFactory = new SimpleSessionFactory();
	}

	@Override
	protected Session createSession(SessionContext sessionContext) throws AuthorizationException {
		Session session = createSessionInstance(sessionContext);
		if (logger.isDebugEnabled()) {
			logger.debug("create session for host {}", session.getHost());
			logger.debug("Creating new EIS record for new session instance [" + session + "]");
		}
		sessionDAO.create(session);
		sessioncount.getAndIncrement();
		return session;
	}

	@Override
	protected Session doGetSession(SessionKey sessionKey) throws InvalidSessionException {

		if (logger.isDebugEnabled()) {
			logger.debug("Attempting to retrieve session with key {}", sessionKey);
		}
		Serializable sessionid = getSessionId(sessionKey);
		if (sessionid == null) {
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to resolve session ID from SessionKey [{}].  Returning null to indicate a "
						+ "session could not be found.", sessionKey);
			}
			return null;
		}
		Session session  = null;
		try {
			 session = sessionDAO.readSession(sessionid);
		} catch (Throwable e) {
			// TODO: handle exception
			String msg = "Could not find session with ID [" + sessionid + "] in the redis ";
			logger.debug(msg);
		}

		if (session == null) {
			String msg = "Could not find session with ID [" + sessionid + "]";
			logger.debug(msg);
		}

		return session;
	}

	@Override
	protected void afterStopped(Session session) {
		this.sessionDAO.delete(session);
		if (logger.isDebugEnabled()) {
			logger.debug("the session has been delete from redis sever which id is {}", session.getId());
		}
	}

	protected Session createSessionInstance(SessionContext sessionContext) {
		return getSessionFactory().createSession(sessionContext);
	}

	protected Serializable getSessionId(SessionKey sessionKey) {

		Serializable sessionid = sessionKey.getSessionId();
		if (sessionid == null) {
			WebSessionKey websessionkey = (WebSessionKey) sessionKey;
			ServletRequest request = websessionkey.getServletRequest();
			ShiroHttpServletRequest rew = (ShiroHttpServletRequest) request;
			RequestFacade req = (RequestFacade) XssHttpServletRequestWrapper.getOrgRequest(rew.getRequest());
			sessionid = req.getRequestedSessionId();
		}
		return sessionid;
	}

	public SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public void setSessionFactory(SessionFactory sessionFactory) {
		this.sessionFactory = sessionFactory;
	}

	public SessionDAO getSessionDAO() {
		return sessionDAO;
	}

	public void setSessionDAO(SessionDAO sessionDAO) {
		this.sessionDAO = sessionDAO;
	}
}
