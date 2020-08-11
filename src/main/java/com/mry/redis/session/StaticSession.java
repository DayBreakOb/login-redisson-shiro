package com.mry.redis.session;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.mgt.ValidatingSession;

import com.google.common.collect.Maps;

public class StaticSession implements ValidatingSession,Serializable{

	 public static final StaticSession INSTANCE = new StaticSession();
	    private Map<Object, Object> attributes;
	    private Serializable id = "1";
	    private Date startTimestamp;
	    private static final long serialVersionUID = 1L;

	    public Date getStartTimestamp() {
	        return this.startTimestamp;
	    }

	    public long getTimeout() throws InvalidSessionException {
	        return Long.MAX_VALUE;
	    }

	    public void stop() throws InvalidSessionException {
	    }

	    public void setTimeout(long maxIdleTimeInMillis) throws InvalidSessionException {
	    }

	    public Object removeAttribute(Object key) throws InvalidSessionException {
	        return this.attributes.remove(key);
	    }

	    public void validate() throws InvalidSessionException {
	    }

	    public boolean isValid() {
	        return true;
	    }

	    public StaticSession() {
	        StaticSession staticSession = this;
	        this.startTimestamp = new Date();
	        staticSession.attributes =Maps.newHashMap();
	    }

	    public Collection<Object> getAttributeKeys() throws InvalidSessionException {
	        return this.attributes.keySet();
	    }

	    public String getHost() {
	        return null;
	    }

	    public void touch() throws InvalidSessionException {
	    }

	    public Date getLastAccessTime() {
	        return new Date();
	    }

	    public Serializable getId() {
	        return this.id;
	    }

	    public void setAttribute(Object key, Object value) throws InvalidSessionException {
	        this.attributes.put(key, value);
	    }

	    public Object getAttribute(Object key) throws InvalidSessionException {
	        return this.attributes.get(key);
	    }

}
