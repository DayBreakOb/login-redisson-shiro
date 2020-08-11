package com.mry.shiro;

import org.apache.shiro.session.Session;
import org.apache.shiro.session.SessionListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author root
 */

public class ShiroSessionListener implements SessionListener {



    private  static Logger logger = LoggerFactory.getLogger(ShiroSessionListener.class);
    private final AtomicInteger sessionCount = new AtomicInteger(0);

    @Override
    public void onStart(Session session) {

        logger.info("the session has benn stasrt ...");
        sessionCount.incrementAndGet();
    }

    @Override
    public void onStop(Session session) {
        logger.info("the session has benn onStop ...");
        sessionCount.decrementAndGet();
    }

    @Override
    public void onExpiration(Session session) {
        logger.info("the session has benn onExpiration ...");
        sessionCount.decrementAndGet();
    }
}
