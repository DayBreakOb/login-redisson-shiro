package com.mry.chat.letschat.common.redis.client;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.ClusterServersConfig;
import org.redisson.config.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;


@Configuration
public class RedissonInstance {


    private static Logger logger = LoggerFactory.getLogger(RedissonInstance.class);


    private static String host;

    private static int port;

    private static String password;

    public static int timeout;

    private static int database;

    @Value("${spring.redis.host}")
    public void setHost(String host) {
        logger.info("the host is " + host);
        RedissonInstance.host = host;
    }

    @Value("${spring.redis.port}")
    public void setPort(int port) {
        RedissonInstance.port = port;
    }

    @Value("${spring.redis.password:}")
    public void setPassword(String password) {
        RedissonInstance.password = password;
    }

    @Value("${spring.redis.timeout}")
    public void setTimeout(int timeout) {
        RedissonInstance.timeout = timeout;
    }

    @Value("${spring.redis.database:0}")
    public void setDatabase(int database) {
        RedissonInstance.database = database;
    }


    @Bean
    public RedissonClient getredissonClient() {
        logger.info("initial the redisson client is " + host + "   :port   " + port + "   passwd: " + password + "   database  :" + database);
        Config cfg = new Config();
        if (host.contains(";")) {
            //cluster scan time unit is ms
            String[] hosts = host.split(";");
            ClusterServersConfig cfgcluster = cfg.useClusterServers().setScanInterval(2000).setPassword(password);
            for (String host : hosts){
                cfgcluster.addNodeAddress(host);
            }
        } else {
            cfg.useSingleServer().setAddress(host + ":" + port).setPassword(password).setTimeout(timeout);
        }
        return Redisson.create(cfg);

    }

}
