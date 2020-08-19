package com.mry.shrio.filter;

import com.google.common.collect.Maps;
import com.mry.algorithm.crypto.process.impl.AesProcess;
import com.mry.algorithm.crypto.process.impl.RsaProcess;
import com.mry.config.PropertyUtil;
import com.mry.http.request.ParamHandle;
import com.mry.shiro.token.IUserPasswordToken;
import com.mry.system.pojo.User;
import com.mry.util.CookieUtils;
import com.mry.util.IpUtils;
import com.mry.util.ObjectUtils;
import com.mry.util.SecurityConfigUtils;
import com.mry.util.ServletUtils;
import com.mry.util.ShiroUtil;
import com.mry.util.StringUtils;
import com.mry.util.XssUtil;

import java.io.IOException;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.IncorrectCredentialsException;
import org.apache.shiro.authc.UnknownAccountException;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.FormAuthenticationFilter;
import org.apache.shiro.web.servlet.Cookie;
import org.apache.shiro.web.servlet.SimpleCookie;
import org.apache.shiro.web.util.WebUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IFormAuthenticationFilter extends FormAuthenticationFilter {

	private static final String VALID_CODE = "validCode";
	private static final String LOGIN_RES_MESSAGE = "resMessage";// login response msg
	private static final String REMEMBER_ME_USER = "remeberMeUser";
	private static final String EXCEPTION_NAME = "exception"; // exceptionName

	private Cookie rememberMeCookie;

	public IFormAuthenticationFilter() {
		super();
		rememberMeCookie = new SimpleCookie();
		rememberMeCookie.setHttpOnly(true);
		rememberMeCookie.setMaxAge(Cookie.ONE_YEAR);
		setLoginUrl("/login");
		setSuccessUrl("/index.html");
	}

	private static Logger logger = LoggerFactory.getLogger(IFormAuthenticationFilter.class);

	// create the authtoken
	@Override
	protected AuthenticationToken createToken(ServletRequest request, ServletResponse response) {
		// TODO Auto-generated method stub
		String username = getUsername(request, response);
		String passwd = getPassword(request);
		boolean remeberMe = isRememberMe(request);
		String host = getHost(request);
		String validcode = getValicode(request);
		Map<String, Object> extmap = ServletUtils.getExtParams(request);
		return this.createToken(username, passwd.toCharArray(), remeberMe, host, "", extmap);
	}

	private AuthenticationToken createToken(String username, char[] password, boolean rememberMe, String host,
			String validCode, Map<String, Object> exMap) {
		// TODO Auto-generated method stub
		return new IUserPasswordToken(username, password, rememberMe, host, validCode, exMap);
	}

	private String getValicode(ServletRequest request) {
		// TODO Auto-generated method stub
		String validCode = WebUtils.getCleanParam(request, VALID_CODE);
		if (StringUtils.isBlank(validCode)) {
			validCode = StringUtils.toString(request.getAttribute(VALID_CODE), StringUtils.EMPTY);
		}
		if (StringUtils.isBlank(validCode)) {
			return null;
		}
		String secretKey = SecurityConfigUtils.AES_VALI_CODE;
		if (StringUtils.isNotBlank(secretKey)) {
		}
		return validCode;
	}

	protected String getUsername(ServletRequest request, ServletResponse response) {
		// TODO Auto-generated method stub
		String username = super.getUsername(request);
		if (StringUtils.isBlank(username)) {
			username = ObjectUtils.toString((request.getAttribute(getUsernameParam())), StringUtils.EMPTY);
		}
		String loginsk = SecurityConfigUtils.AES_LOGIN_SK;
		if (StringUtils.isNotBlank(loginsk)) {
			if (StringUtils.isBlank(username)) {
				logger.info("the login username is null or the decode the username is wrong ...");
			}
		}
		if (WebUtils.isTrue(request, REMEMBER_ME_USER)) {
			rememberMeCookie.setValue(XssUtil.encodeUrl(XssUtil.xssFilter(username)));
			rememberMeCookie.saveTo(WebUtils.toHttp(request), WebUtils.toHttp(response));
		} else {

			try {
				rememberMeCookie.removeFrom(WebUtils.toHttp(request), WebUtils.toHttp(response));
			} catch (Throwable e) {
			}

		}
		return username;
	}

	@Override
	protected String getPassword(ServletRequest request) {
		// TODO Auto-generated method stub
		String password = super.getPassword(request);
		if (StringUtils.isBlank(password)) {
			password = ObjectUtils.toString(request.getAttribute(getPasswordParam()), StringUtils.EMPTY);
		}
		String secretKey = SecurityConfigUtils.AES_PASSWD_SK;
		if (StringUtils.isNotBlank(secretKey)) {
			if (StringUtils.isBlank(password)) {
				logger.info("the password is null or the password decode wrong ...");
				throw new AuthenticationException("the password is null or the password decode wrong ...");
			}
		}
		return password;
	}

	@Override
	protected boolean isRememberMe(ServletRequest request) {
		// TODO Auto-generated method stub
		String isRememberMe = WebUtils.getCleanParam(request, getRememberMeParam());
		if (StringUtils.isBlank(isRememberMe)) {
			isRememberMe = ObjectUtils.toString(request.getAttribute(getRememberMeParam()), StringUtils.EMPTY);
		}
		return ObjectUtils.toBoolean(isRememberMe);
	}

	@Override
	protected String getHost(ServletRequest request) {
		// TODO Auto-generated method stub
		return IpUtils.getRemoteAddr((HttpServletRequest) request);
	}

	/**
	 * 多次调用登录接口，允许改变登录身份，无需退出再登录
	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
		// TODO Auto-generated method stub
		boolean cc = super.isAccessAllowed(request, response, mappedValue);
		if (cc) {
			return IViewFilter.filter(request, response, null, false);
		}
		return cc;
	}

	/**
	 * 跳转登录页时，跳转到默认首页
	 */
	@Override
	protected void redirectToLogin(ServletRequest request, ServletResponse response) throws IOException {
		// TODO Auto-generated method stub
		IPermissionsAuthorizationFilter.redirectToDefaultPage(request, response);
	}

	/**
	 * 地址访问接入验证
	 */
	@Override
	protected boolean onAccessDenied(ServletRequest request, ServletResponse response) throws Exception {
		if (isLoginRequest(request, response)) {
			if (isLoginSubmission(request, response)) {
				if (logger.isTraceEnabled()) {
					logger.trace("Login submission detected.  Attempting to execute login.");
				}
				return executeLogin(request, response);
			} else {
				if (logger.isTraceEnabled()) {
					logger.trace("Login page view.");
				}
				// allow them to see the login page ;)
				return true;
			}
		} else {
			if (!super.isAccessAllowed(request, response, null)) {
				// redirectToLogin(request, response); // 此过滤器优先级较高，未登录，则跳转登录页，方便 CAS 登录
				return IViewFilter.filter(request, response, null, false);
			}
			// return IViewFilter.filter(request, response, null, false);
			return false;
		}
	}

	/**
	 * 是否为登录操作（支持GET或CAS登录时传递__login=true参数）
	 */
	@Override
	protected boolean isLoginRequest(ServletRequest request, ServletResponse response) {
		boolean isLogin = WebUtils.isTrue(request, "__login");
		boolean xx = super.isLoginRequest(request, response);
		if (xx) {
			HttpServletRequest req = (HttpServletRequest) request;
			String method = req.getMethod();
			if (!("POST".equalsIgnoreCase(method))) {
				return false;
			}
		}
		return xx || isLogin;
	}

	/**
	 * 是否为登录操作（支持GET或CAS登录时传递__login=true参数）
	 */
	@Override
	protected boolean isLoginSubmission(ServletRequest request, ServletResponse response) {
		boolean isLogin = WebUtils.isTrue(request, "__login");
		return super.isLoginSubmission(request, response) || isLogin;
	}

	/**
	 * 执行登录方法
	 */
	@Override
	protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
		// 是否在登录后生成新的Session（默认false）
		if (SecurityConfigUtils.isGenerateNewSessionAfterLogin) {
			ShiroUtil.getSubject().logout();
		}
		return super.executeLogin(request, response);
	}

	@Override
	protected boolean onLoginSuccess(AuthenticationToken token, Subject subject, ServletRequest request,
			ServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		if (ServletUtils.isAjaxRequest(WebUtils.toHttp(request))) {
			ServletUtils.renderString((HttpServletResponse) response, "success", "text/html");
			// WebUtils.issueRedirect(request, response, getSuccessUrl());
			return false;
		}
		return super.onLoginSuccess(token, subject, request, response);
	}

	/**
	 * 登录失败调用事件
	 */
	@Override
	protected boolean onLoginFailure(AuthenticationToken token, AuthenticationException e, ServletRequest request,
			ServletResponse response) {
		String message = StringUtils.EMPTY;
		if (e instanceof IncorrectCredentialsException || e instanceof UnknownAccountException) {
			message = SecurityConfigUtils.LOGIN_FAILURE;
		} else if (e.getMessage() != null && StringUtils.startsWith(e.getMessage(), "msg:")) {
			message = StringUtils.replace(e.getMessage(), "msg:", "");
		} else {
			message = SecurityConfigUtils.LOGIN_ERROR;
			logger.error(message, e); // 输出到日志文件
		}
		request.setAttribute(EXCEPTION_NAME, e);
		request.setAttribute(LOGIN_RES_MESSAGE, message);

		// 登录操作如果是Ajax操作，直接返回登录信息字符串。
		if (ServletUtils.isAjaxRequest(((HttpServletRequest) request))) {
			Map<String, Object> data = getLoginFailureData(((HttpServletRequest) request),
					((HttpServletResponse) response));
			ServletUtils.renderResult(((HttpServletResponse) response), SecurityConfigUtils.TRUE, message, data);
			return false;
		}

		return true;
	}

	public static Map<String, Object> getLoginData(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> data = Maps.newHashMap();

		// 获取登录参数
		Map<String, Object> paramMap = ServletUtils.getExtParams(request);
		for (Entry<String, Object> entry : paramMap.entrySet()) {
			data.put(ServletUtils.EXT_PARAMS_PREFIX + entry.getKey(), entry.getValue());
		}

		// 如果已登录，再次访问主页，则退出原账号。
		if (!SecurityConfigUtils.isAllowRefreshIndex) {
			CookieUtils.setCookie(response, "LOGINED", "false");
		}

		// 是否显示验证码
		data.put("isValidCodeLogin",
				Integer.parseInt(PropertyUtil.getProperty("sys.login.failedNumAfterValidCode", "200")) == 0);

		// 获取当前会话对象
		Session session = ShiroUtil.getSession();
		data.put("sessionid", (String) session.getId());

		// 如果登录设置了语言，则切换语言
		/*
		 * if (paramMap.get("lang") != null) { Global.setLang((String)
		 * paramMap.get("lang"), request, response); }
		 */

		data.put("result", "login");
		return data;
	}

	public static Map<String, Object> getLoginFailureData(HttpServletRequest request, HttpServletResponse response) {
		Map<String, Object> data = Maps.newHashMap();

		String username = WebUtils.getCleanParam(request, DEFAULT_USERNAME_PARAM);
		boolean rememberMe = WebUtils.isTrue(request, DEFAULT_REMEMBER_ME_PARAM);
		boolean rememberUserCode = WebUtils.isTrue(request, REMEMBER_ME_USER);
		Exception exception = (Exception) request.getAttribute(EXCEPTION_NAME);
		String message = (String) request.getAttribute(LOGIN_RES_MESSAGE);

		String secretKey = SecurityConfigUtils.AES_LOGIN_SK;
		if (StringUtils.isNotBlank(secretKey)) {
		}

		data.put(DEFAULT_USERNAME_PARAM, username);
		data.put(DEFAULT_REMEMBER_ME_PARAM, rememberMe);
		data.put(REMEMBER_ME_USER, rememberUserCode);
		Map<String, Object> paramMap = ServletUtils.getExtParams(request);
		for (Entry<String, Object> entry : paramMap.entrySet()) {
			data.put(ServletUtils.EXT_PARAMS_PREFIX + entry.getKey(), entry.getValue());
		}
		data.put(LOGIN_RES_MESSAGE, message);

		// 记录用户登录失败日志
		String corpCode = (String) paramMap.get("corpCode");
		User user = ShiroUtil.getByLoginCode(username, corpCode);
		// LogUtils.saveLog(user, request, "登录失败", Log.TYPE_LOGIN_LOGOUT);

		// 获取当前会话对象
		Session session = ShiroUtil.getSession();
		data.put("sessionid", (String) session.getId());

		data.put("result", SecurityConfigUtils.FALSE);
		return data;
	}

}
