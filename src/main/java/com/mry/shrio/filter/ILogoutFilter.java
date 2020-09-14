package com.mry.shrio.filter;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.filter.authc.LogoutFilter;

import com.mry.config.BaseConfig;
import com.mry.util.ServletUtils;



public class ILogoutFilter extends LogoutFilter {

	
	
	public ILogoutFilter(){
		
		setRedirectUrl(BaseConfig.loginUrl);
		
	}
	

	@Override
	protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
		// TODO Auto-generated method stub
		Subject subject = getSubject(request, response);
		String redirectUrl = getRedirectUrl(request, response, subject);
		try {
			subject.logout();
			  // 如果是Ajax请求，返回Json字符串。
	 		if (ServletUtils.isAjaxRequest((HttpServletRequest)request)){
	 			ServletUtils.renderString((HttpServletResponse)response,
	 					"success", "text/html");
	 			return false;
	 		}
			issueRedirect(request, response, redirectUrl);
		} catch (Throwable e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		return false;
	}

}
