package com.mry.chat.letschat.common.redis.session;

import com.google.common.collect.Lists;
import org.apache.shiro.session.ExpiredSessionException;
import org.apache.shiro.session.InvalidSessionException;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.StoppedSessionException;
import org.apache.shiro.session.mgt.AbstractSessionManager;
import org.redisson.RedissonScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.RedisException;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.api.RScript;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class IRedissonSession extends IRedissonSessionScript implements Session {

	private RedissonClient redisson;
	private Codec infoCodec = new JsonJacksonCodec();
	private Codec codec = infoCodec;
	private String infoKey;
	private String attrKey;
	private Serializable id;

	public IRedissonSession(RedissonClient client, Codec codec, Serializable id, String infoKey, String attrKey) {
		if (client == null || infoKey == null || attrKey == null || id == null) {
			throw new IllegalArgumentException("Arguments must not be null!");
		}
		this.redisson = client;
		if (codec != null) {
			this.codec = codec;
		}
		this.infoKey = infoKey;
		this.attrKey = attrKey;
		this.id = id;

	}

	public IRedissonSession(RedissonClient client, Codec codec, Session session, String infoKey, String attrKey) {
		if (client == null || infoKey == null || attrKey == null || session == null) {
			throw new IllegalArgumentException("Arguments must not be null!");
		}
		this.redisson = client;
		if (codec != null) {
			this.codec = codec;
		}
		this.infoKey = infoKey;
		this.attrKey = attrKey;
		this.id = session.getId();
		start(session);

	}

	private void start(Session session) {
		final long timeout = session.getTimeout() > 0 ? session.getTimeout()
				: AbstractSessionManager.DEFAULT_GLOBAL_SESSION_TIMEOUT;
		Date startTimeStamp = session.getStartTimestamp();
		startTimeStamp = startTimeStamp != null ? startTimeStamp : new Date();
		String host = session.getHost();
		host = host != null ? host : "";
		List<Object> keys = Lists.newArrayList();
		keys.add(this.infoKey);
		RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
		script.eval(this.infoKey, RScript.Mode.READ_WRITE, INIT_SCRIPT, RScript.ReturnType.VALUE, keys, session.getId(),
				timeout, startTimeStamp, host);
	}

	@Override
	public Serializable getId() {
		return this.id;
	}

	@Override
	public Date getStartTimestamp() {
		List<Object> keys = Lists.newArrayList();
		keys.add(this.infoKey);
		RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
		Date res = null;
		try {
			res = script.eval(this.infoKey, RScript.Mode.READ_ONLY, GET_START_SCRIPT, RScript.ReturnType.MAPVALUE,
					keys);
		} catch (Throwable e) {
			convertException(e);
		}
		if (res == null) {
			throw new InvalidSessionException();
		}
		return res;
	}

	@Override
	public Date getLastAccessTime() {
		List<Object> keys = Lists.newArrayList(1);
		keys.add(infoKey);
		RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
		Date res = null;
		try {
			res = script.eval(this.infoKey, RScript.Mode.READ_ONLY, GET_LAST_SCRIPT, RScript.ReturnType.MAPVALUE, keys);
		} catch (Throwable e) {
			convertException(e);
		}
		if (res == null) {
			throw new InvalidSessionException();
		}
		return res;
	}

	@Override
	public long getTimeout() throws InvalidSessionException {
		List<Object> keys = new ArrayList<>(1);
		keys.add(this.infoKey);

		RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
		Long res = null;
		try {
			res = script.eval(this.infoKey, RScript.Mode.READ_ONLY, GET_TIMEOUT_SCRIPT, RScript.ReturnType.MAPVALUE,
					keys);
		} catch (RedisException e) {
			convertException(e);
		}

		if (res == null) {
			throw new InvalidSessionException();
		} else {
			return res;
		}
	}

	@Override
	public void setTimeout(long l) throws InvalidSessionException {
		List<Object> keys = new ArrayList<>(1);
        keys.add(this.infoKey);
        keys.add(this.attrKey);

        RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
        try {
            script.eval(this.infoKey, RScript.Mode.READ_WRITE,
                SET_TIMEOUT_SCRIPT,
                RScript.ReturnType.VALUE, keys, l);
        } catch (RedisException e) {
            convertException(e);
        }
	}

	@Override
	public String getHost() {
		 List<Object> keys = new ArrayList<>(1);
	        keys.add(this.infoKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
	        String res = null;
	        try {
	            res = script.eval(this.infoKey, RScript.Mode.READ_ONLY,
	                GET_HOST_SCRIPT,
	                RScript.ReturnType.MAPVALUE, keys);
	        } catch (RedisException e) {
	            convertException(e);
	        }

	        if (res == null) {
	            throw new InvalidSessionException();
	        } else {
	            return res;
	        }
	}

	@Override
	public void touch() throws InvalidSessionException {
		   List<Object> keys = new ArrayList<>(2);
	        keys.add(this.infoKey);
	        keys.add(this.attrKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
	        try {
	            script.eval(this.infoKey, RScript.Mode.READ_WRITE,
	                TOUCH_SCRIPT, RScript.ReturnType.VALUE, keys, new Date());
	        } catch (RedisException e) {
	            convertException(e);
	        }
	}

	@Override
	public void stop() throws InvalidSessionException {
		 List<Object> keys = new ArrayList<>(1);
	        keys.add(this.infoKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.infoCodec);
	        try {
	            script.eval(this.infoKey, RScript.Mode.READ_WRITE,
	                STOP_SCRIPT, RScript.ReturnType.VALUE, keys, new Date());
	        } catch (RedisException e) {
	            convertException(e);
	        }
	}

	@Override
	public Collection<Object> getAttributeKeys() throws InvalidSessionException {
		List<Object> keys = new ArrayList<>(2);
        keys.add(this.infoKey);
        keys.add(this.attrKey);

        RedissonScript script = (RedissonScript) this.redisson.getScript(this.codec);
        Collection<Object> res = null;
        try {
            res = script.eval(this.infoKey, RScript.Mode.READ_ONLY,
                GET_ATTRKEYS_SCRIPT, RScript.ReturnType.MAPVALUELIST, keys);
        } catch (RedisException e) {
            convertException(e);
        }

        if (res == null) {
            throw new InvalidSessionException();
        } else {
            return res;
        }
	}

	@Override
	public Object getAttribute(Object key) throws InvalidSessionException {
		 List<Object> keys = new ArrayList<>(2);
	        keys.add(this.infoKey);
	        keys.add(this.attrKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.codec);
	        Object res = null;
	        try {
	            res = script.eval(this.infoKey, RScript.Mode.READ_ONLY,
	                GET_ATTR_SCRIPT, RScript.ReturnType.MAPVALUE, keys, key);
	        } catch (RedisException e) {
	            convertException(e);
	        }

	        return res;
	}

	@Override
	public void setAttribute(Object key, Object value) throws InvalidSessionException {
		 List<Object> keys = new ArrayList<>(2);
	        keys.add(this.infoKey);
	        keys.add(this.attrKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.codec);
	        try {
	            script.eval(this.infoKey, RScript.Mode.READ_WRITE,
	                SET_ATTR_SCRIPT, RScript.ReturnType.VALUE, keys, key, value);
	        } catch (RedisException e) {
	            convertException(e);
	        }
	}

	@Override
	public Object removeAttribute(Object key) throws InvalidSessionException {
		  List<Object> keys = new ArrayList<>(2);
	        keys.add(this.infoKey);
	        keys.add(this.attrKey);

	        RedissonScript script = (RedissonScript) this.redisson.getScript(this.codec);
	        Object res = null;
	        try {
	            res = script.eval(this.infoKey, RScript.Mode.READ_WRITE,
	                REMOVE_ATTR_SCRIPT, RScript.ReturnType.MAPVALUE, keys, key);
	        } catch (RedisException e) {
	            convertException(e);
	        }

	        return res;
	}

	public void setCodec(Codec codec) {
		this.codec = codec;
	}

	public void setInfoKey(String infoKey) {
		this.infoKey = infoKey;
	}

	public void setAttrKey(String attrKey) {
		this.attrKey = attrKey;
	}

	public void setRedisson(RedissonClient redisson) {
		this.redisson = redisson;
	}

	public void setInfoCodec(Codec infoCodec) {
		this.infoCodec = infoCodec;
	}

	public void setId(Serializable id) {
		this.id = id;
	}

	private void checkIllegal(Object obj) {
		if (obj == null) {
			String msg = "the " + obj.getClass().getName() + "  must not  be null to initial RedissonSession ...";
			throw new IllegalArgumentException(msg);
		}
	}

	private void convertException(Throwable e) {
		String emsg = e.getMessage();
		switch (emsg) {
		case RETURN_CODE_EXPIRED:
			throw new ExpiredSessionException(emsg);
		case RETURN_CODE_STOPPED:
			throw new StoppedSessionException(emsg);
		case RETURN_CODE_INVALID:
			throw new InvalidSessionException(emsg);
		default:
			throw new RuntimeException(emsg);
		}
	}

}
