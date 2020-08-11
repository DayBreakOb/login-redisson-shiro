package com.mry.util;

import java.util.concurrent.locks.Lock;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mry.redis.session.StaticSession;
import com.mry.system.pojo.User;

public class ShiroUtil {
	public static final String USER_CACHE = "userCache";
	private static final String USER_CACHE_LOGIN_CODE_ = "login_";
	private static final String USER_CACHE_USER_CODE_ = "code_";
	private static Logger logger = LoggerFactory.getLogger(ShiroUtil.class);
	public static final String CACHE_AUTH_INFO = "authInfo";
	private static final String USER_CACHE_USER_TYPE_AND_REF_CODE_ = "type_ref_";
	public static final String CACHE_MENU_LIST = "menuList";
	private static Lock userCacheLock;

	public static Subject getSubject() {

		return SecurityUtils.getSubject();
	}

	public static Session getSession() {
		// TODO Auto-generated method stub
		Subject subject = getSubject();
		Session session = subject.getSession(false);
		if (session == null) {
			session = subject.getSession();
		}
		if (session != null) {
			return session;
		}

		return StaticSession.INSTANCE;
	}

	public static User getByLoginCode(String loginCode, String corpCode) {
		return getUser(loginCode, corpCode);
	}

	private static User getUser(String loginCode, String corpCode) {
		// TODO Auto-generated method stub
		return null;
	}

	

}
