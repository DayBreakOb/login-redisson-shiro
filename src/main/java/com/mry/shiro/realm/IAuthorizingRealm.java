package com.mry.shiro.realm;

import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.session.mgt.eis.SessionDAO;

public abstract class IAuthorizingRealm extends AuthorizingRealm{

	
	protected SessionDAO sessionDAO;


	public void setSessionDAO(SessionDAO sessionDAO) {
		this.sessionDAO = sessionDAO;
	}
	


}
