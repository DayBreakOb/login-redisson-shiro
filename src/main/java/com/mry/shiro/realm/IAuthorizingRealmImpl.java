package com.mry.shiro.realm;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.session.Session;
import org.apache.shiro.subject.PrincipalCollection;

import com.mry.shiro.token.IUserPasswordToken;
import com.mry.util.ShiroUtil;

public abstract class IAuthorizingRealmImpl extends IAuthorizingRealm {

	
	public IAuthorizingRealmImpl() {
		this.setCachingEnabled(false);
		this.setAuthenticationTokenClass(IUserPasswordToken.class);
	}
	
	@Override
	protected AuthorizationInfo getAuthorizationInfo(PrincipalCollection principals) {
		// TODO Auto-generated method stub
		if (principals==null) {
			return null;
		}
		Session session = ShiroUtil.getSession();
		return super.getAuthorizationInfo(principals);
	}
	
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		// TODO Auto-generated method stub
		return this.iGetAuthenticationInfo(token);
	}

	
	
	

	private AuthenticationInfo iGetAuthenticationInfo(AuthenticationToken token) {
		// TODO Auto-generated method stub
		return null;
	}

	protected IUserPasswordToken getFormToken(AuthenticationToken token) {
		if (token instanceof IUserPasswordToken) {
			return (IUserPasswordToken)token;
		}
		return null;
	}

}
