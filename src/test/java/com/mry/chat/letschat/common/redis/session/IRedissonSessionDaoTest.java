package com.mry.chat.letschat.common.redis.session;

import org.apache.shiro.session.mgt.SimpleSession;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;

/**
 * <p>RedissonSessionDao test case.</p>
 *
 * @author streamone
 */

@RunWith(Parameterized.class)
public class IRedissonSessionDaoTest {


    private  IRedissonSessionDao sessionDao;
    @Parameterized.Parameters
    public static Collection<Object> data() {
        Config redissonCfg = new Config();
        redissonCfg.useSingleServer().setAddress("redis://127.0.0.1:6380");
        RedissonClient redisson = Redisson.create(redissonCfg);
        IRedissonSessionDao sessionDao = new IRedissonSessionDao();
        sessionDao.setRedissonClient(redisson);
        return Arrays.asList(new Object[]{sessionDao});
    }

    public  IRedissonSessionDaoTest(IRedissonSessionDao sessionDao){
        this.sessionDao = sessionDao;
    }
    @Test
    public void testDeleteNull() {
        this.sessionDao.delete(null);
        this.sessionDao.delete(new SimpleSession());
    }

    @Test
    public void testGetRedisson() {
        assertNotNull(this.sessionDao.getRedissonClient());
    }

    @Test
    public void testGetActiveSessions() {
        assertTrue(this.sessionDao.getActiveSessions().isEmpty());
    }
    
    @Test
    public void testCreateSession() {
    	
    	SimpleSession session = new SimpleSession();
    	session.setId("dsajd1saojd1nsalkmdsa");
    	session.setAttribute("sd1adsad", "231edsadada");
    	session.setExpired(true);
    	session.setTimeout(600000);
    	this.sessionDao.doCreate(session);
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(this.sessionDao.doReadSession(session.getId()));
    }
}
