package com.mry.chat.letschat.common.redis.session;


import org.apache.shiro.session.Session;
import org.apache.shiro.session.UnknownSessionException;
import org.apache.shiro.session.mgt.*;
import org.apache.shiro.session.mgt.eis.JavaUuidSessionIdGenerator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(Parameterized.class)
public class IRedissonSessionManagerTest {

    private IRedissonSessionManager sessionManager;

    protected static final long MILLIS_PER_SECOND = 1000;
    protected static final long MILLIS_PER_MINUTE = 60 * MILLIS_PER_SECOND;
    protected static final long MILLIS_PER_HOUR = 60 * MILLIS_PER_MINUTE;

    /**
     * Default main session timeout value, equal to {@code 30} minutes.
     */
    public static final long DEFAULT_GLOBAL_SESSION_TIMEOUT = 30 * MILLIS_PER_MINUTE;

    private long globalSessionTimeout = DEFAULT_GLOBAL_SESSION_TIMEOUT;

    @Parameterized.Parameters
    public static Collection<Object> data() {
        Config redissonCfg = new Config();
        redissonCfg.useSingleServer().setAddress("redis://127.0.0.1:6380");
        RedissonClient redisson = Redisson.create(redissonCfg);
        IRedissonSessionDao sessionDao = new IRedissonSessionDao();
        sessionDao.setRedissonClient(redisson);
        IRedissonSessionManager webSessionManager=new IRedissonSessionManager();
        webSessionManager.setSessionDAO(sessionDao);
        return Arrays.asList(new Object[]{webSessionManager});
    }

    public IRedissonSessionManagerTest( IRedissonSessionManager sessionManager) {
        this.sessionManager = sessionManager;
    }


    @Test
    public void testCreateSession() {
        final String host = "localhost";
        SessionFactory noStartTimeFactory = new SessionFactory() {
            @Override
            public Session createSession(SessionContext initData) {
                SimpleSession session;
                if (initData != null) {
                    String host = initData.getHost();
                    if (host != null) {
                        session = new SimpleSession(host);
                        session.setStartTimestamp(null);
                        return session;
                    }
                }
                session = new SimpleSession();
                session.setStartTimestamp(null);
                return session;
            }
        };
        IRedissonSessionManager cloneSessionManager = spy(this.sessionManager);
        cloneSessionManager.setSessionFactory(noStartTimeFactory);
        SessionContext sc = new DefaultSessionContext();
        sc.setHost(host);
        Session session = cloneSessionManager.start(sc);
        assertEquals(host, session.getHost());
        assertNotNull(session.getStartTimestamp());

        Session newSession = this.sessionManager.start(new DefaultSessionContext());
        assertEquals("", newSession.getHost());
        assertNotNull(newSession);
        assertEquals(DEFAULT_GLOBAL_SESSION_TIMEOUT, newSession.getTimeout());
        assertNotNull(newSession.getStartTimestamp());

        Session retrievedSession = this.sessionManager.getSession(new DefaultSessionKey(newSession.getId()));
        assertNotNull(retrievedSession);
        assertEquals(newSession.getStartTimestamp(), retrievedSession.getStartTimestamp());
    }

    @Test(expected = UnknownSessionException.class)
    public void testGetSessionByInvalidId() {
        String invalidId = "i_am_not_a_valid_session";
        this.sessionManager.getSession(new DefaultSessionKey(invalidId));
    }

    @Test
    public void testBeanSetter() {
        IRedissonSessionManager sessionManager = new IRedissonSessionManager();
        IRedissonSessionDao sessionDao = mock(IRedissonSessionDao.class);
        sessionManager.setSessionDAO(sessionDao);
        assertEquals(sessionDao, sessionManager.getSessionDAO());

        SessionFactory factory = sessionManager.getSessionFactory();
        assertTrue(factory != null && factory instanceof SimpleSessionFactory);

        final String host = "anonymous.host";
        sessionManager.setSessionFactory(new SessionFactory() {
            @Override
            public Session createSession(SessionContext initData) {
                Session session = mock(Session.class);
                when(session.getHost()).thenReturn(host);
                return session;
            }
        });
        Session session = sessionManager.createSession(new DefaultSessionContext());
        assertEquals(host, session.getHost());
    }

    @Test
    public void testDoGetSessionWithNullId() {
        IRedissonSessionManager sessionManager = new IRedissonSessionManager();
        IRedissonSessionDao sessionDao = mock(IRedissonSessionDao.class);
        sessionManager.setSessionDAO(sessionDao);
        SessionKey key = new DefaultSessionKey();
        assertNull(sessionManager.doGetSession(key));
    }

    @Test(expected = UnknownSessionException.class)
    public void testDoGetSessionWithNotExistId() {
        IRedissonSessionManager sessionManager = new IRedissonSessionManager();
        IRedissonSessionDao sessionDao = mock(IRedissonSessionDao.class);
        sessionManager.setSessionDAO(sessionDao);
        when(sessionDao.readSession(anyString())).thenReturn(null);
        SessionKey key = new DefaultSessionKey(new JavaUuidSessionIdGenerator().generateId(null));
        sessionManager.doGetSession(key);
    }

}
