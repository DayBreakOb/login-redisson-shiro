package com.mry.shiro.realm;

import org.apache.shiro.realm.AuthorizingRealm;

import com.mry.redis.session.IRedissonSessionDao;

public abstract class IAuthorizingRealm extends AuthorizingRealm{

	
	protected IRedissonSessionDao sessionDAO;


	public void setSessionDAO(IRedissonSessionDao sessionDAO) {
		this.sessionDAO = sessionDAO;
	}
	


}
