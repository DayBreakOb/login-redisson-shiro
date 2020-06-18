package com.mry.chat.letschat.common.redis.session;

import com.google.common.collect.Lists;
import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.eis.AbstractSessionDAO;
import org.redisson.RedissonScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.redisson.api.RScript;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author root
 */
public class IRedissonSessionDao extends AbstractSessionDAO {


    private static final String SESSION_INFO_KEY_PREFIX = "session:info:";
    private static final String SESSION_ATTR_KEY_PREFIX = "session:attr:";

    private RedissonClient redissonClient;
    private Codec codec = new JsonJacksonCodec();

    private static Logger logger = LoggerFactory.getLogger(IRedissonSessionDao.class);

    @Override
    protected Serializable doCreate(Session session) {
        Serializable sessionId = generateSessionId(session);
        assignSessionId(session, sessionId);
        String infoKey = getSessionInfoKey(sessionId.toString());
        String attrKey = getSessionAttrKey(sessionId.toString());
        new IRedissonSession(this.redissonClient, this.codec, session, infoKey, attrKey);
        return sessionId;
    }

    @Override
    protected Session doReadSession(Serializable serializable) {
        String infoKey = getSessionInfoKey(serializable.toString());
        String attrKey = getSessionAttrKey(serializable.toString());
        List<Object> keys = Lists.newArrayList();
        keys.add(infoKey);
        RedissonScript script = (RedissonScript) this.redissonClient.getScript(this.codec);
        Long remainTimeToLive = script.eval(infoKey, RScript.Mode.READ_ONLY,
                IRedissonSessionScript.READ_SCRIPT,
                RScript.ReturnType.INTEGER, keys);

        if (remainTimeToLive > 0) {
            return new IRedissonSession(this.redissonClient, this.codec, serializable, infoKey, attrKey);
        } else {
            return null;
        }
    }

    @Override
    public void update(Session session) throws UnknownSessionException {
        //nothing will be to do .because the redis will handle the expired by it self
    }

    @Override
    public void delete(Session session) {
        if (session == null || session.getId() == null || "".equals(session.getId())) {
            return;
        }
        Serializable sessionId = session.getId();
        String infoKey = getSessionInfoKey(sessionId.toString());
        String attrKey = getSessionAttrKey(sessionId.toString());
        List<Object> keys = new ArrayList<>(2);
        keys.add(infoKey);
        keys.add(attrKey);
        RedissonScript script = (RedissonScript) this.redissonClient.getScript(this.codec);
        script.eval(infoKey, RScript.Mode.READ_WRITE,
                IRedissonSessionScript.DELETE_SCRIPT,
                RScript.ReturnType.VALUE, keys);
    }

    @Override
    public Collection<Session> getActiveSessions() {
        //for performance reasons, this method should not be called
        return Collections.EMPTY_LIST;
    }

    protected String getSessionInfoKey(String sessionID) {
        //stringbuffer like the stringbuilder but the builder is not safe in threads
        StringBuilder sb = new StringBuilder(SESSION_INFO_KEY_PREFIX);
        sb.append("{").append(sessionID).append("}");
        return sb.toString();
    }

    protected String getSessionAttrKey(String sessionId) {
        StringBuilder sb = new StringBuilder(SESSION_ATTR_KEY_PREFIX);
        sb.append("{").append(sessionId).append("}");
        return sb.toString();
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

}
