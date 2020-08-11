package com.mry.cache.cachemanager;

import org.apache.shiro.ShiroException;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.apache.shiro.io.ResourceUtils;
import org.apache.shiro.util.Initializable;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.Codec;
import org.redisson.codec.JsonJacksonCodec;
import org.redisson.spring.cache.CacheConfig;

import com.mry.cache.IRedissonShiroCache;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class IRedissionShiroCacheManager implements CacheManager,Initializable{

    private boolean allowNullValues = true;
    private Codec codec = new JsonJacksonCodec();
    private RedissonClient redissonClient;
    private String configLocation;

    private Map<String, CacheConfig> configMap = new ConcurrentHashMap<>();
    private ConcurrentMap<String, Cache> instanceMap = new ConcurrentHashMap<>();

    public IRedissionShiroCacheManager() {
    }

    public IRedissionShiroCacheManager(RedissonClient redisson) {
        this(redisson, (String) null, null);
    }

    public IRedissionShiroCacheManager(RedissonClient redisson, Map<String, ? extends CacheConfig> config) {
        this(redisson, config, null);
    }

    public IRedissionShiroCacheManager(RedissonClient redisson, Map<String, ? extends CacheConfig> config, Codec codec) {
        this.redissonClient = redisson;
        this.configMap = (Map<String, CacheConfig>) config;
        if (codec != null) {
            this.codec = codec;
        }
    }

    public IRedissionShiroCacheManager(RedissonClient redisson, String configLocation) {
        this(redisson, configLocation, null);
    }

    public IRedissionShiroCacheManager(RedissonClient redisson, String configLocation, Codec codec) {
        this.redissonClient = redisson;
        this.configLocation = configLocation;
        if (codec != null) {
            this.codec = codec;
        }
    }

    protected CacheConfig DefaultConfig() {
        return new CacheConfig();
    }

    private <K, V> Cache<K, V> createMap(String name, CacheConfig config) {
        RMap<K, Object> map = getMap(name, config);
        Cache<K, V> cache = new IRedissonShiroCache<>(map, this.allowNullValues);
        Cache<K, V> oldCache = this.instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        }
        return cache;
    }

    protected <K> RMap<K, Object> getMap(String name, CacheConfig config) {
        if (this.codec != null) {
            return this.redissonClient.getMap(name, this.codec);
        }
        return this.redissonClient.getMap(name);
    }

    protected <K> RMapCache<K, Object> getMapCache(String name, CacheConfig config) {
        if (this.codec != null) {
            return this.redissonClient.getMapCache(name, this.codec);
        }
        return redissonClient.getMapCache(name);
    }

    private <K, V> Cache<K, V> createMapCache(String name, CacheConfig config) {
        RMapCache<K, Object> map = getMapCache(name, config);
        Cache<K, V> cache = new IRedissonShiroCache<>(map, config, this.allowNullValues);
        Cache<K, V> oldCache = this.instanceMap.putIfAbsent(name, cache);
        if (oldCache != null) {
            cache = oldCache;
        } else {
            map.setMaxSize(config.getMaxSize());
        }
        return cache;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String s) throws CacheException {
        Cache<K, V> cache = this.instanceMap.get(s);
        if (cache != null) {
            return cache;
        }

        CacheConfig config = this.configMap.get(s);
        if (config == null) {
            config = DefaultConfig();
            configMap.put(s, config);
        }

        if (config.getMaxIdleTime() == 0 && config.getTTL() == 0 && config.getMaxSize() == 0) {
            return createMap(s, config);
        }

        return createMapCache(s, config);
    }

    @Override
    public void init() throws ShiroException {
        if (this.configLocation == null) {
            return;
        }
        try {
            this.configMap = (Map<String, CacheConfig>) CacheConfig.fromJSON(ResourceUtils.getInputStreamForPath(this.configLocation));
        } catch (IOException e) {
            // try to read yaml
            try {
                this.configMap = (Map<String, CacheConfig>) CacheConfig.fromYAML(ResourceUtils.getInputStreamForPath(this.configLocation));
            } catch (IOException e1) {
                throw new IllegalArgumentException(
                        "Could not parse cache configuration at [" + configLocation + "]", e1);
            }
        }
    }

    public boolean isAllowNullValues() {
        return allowNullValues;
    }

    public void setAllowNullValues(boolean allowNullValues) {
        this.allowNullValues = allowNullValues;
    }

    public Codec getCodec() {
        return codec;
    }

    public void setCodec(Codec codec) {
        this.codec = codec;
    }

    public RedissonClient getRedissonClient() {
        return redissonClient;
    }

    public void setRedissonClient(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    public String getConfigLocation() {
        return configLocation;
    }

    public void setConfigLocation(String configLocation) {
        this.configLocation = configLocation;
    }

    public Map<String, CacheConfig> getConfigMap() {
        return configMap;
    }

    public void setConfigMap(Map<String, CacheConfig> configMap) {
        this.configMap = configMap;
    }

    public ConcurrentMap<String, Cache> getInstanceMap() {
        return instanceMap;
    }

    public void setInstanceMap(ConcurrentMap<String, Cache> instanceMap) {
        this.instanceMap = instanceMap;
    }
}
