package com.mry.chat.letschat.common.cache;

import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.spring.cache.CacheConfig;
import org.redisson.spring.cache.NullValue;
import org.springframework.cache.support.SimpleValueWrapper;

import javax.cache.CacheException;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author root
 */
public class IRedissonCacheCache<K, V> implements IRedissonCache<K, V> {
    private RMapCache<K, Object> mapCache;

    private final RMap<K, Object> map;

    private CacheConfig config;

    private final boolean allowNullValues;

    private final AtomicLong hits = new AtomicLong();

    private final AtomicLong misses = new AtomicLong();


    public IRedissonCacheCache(RMapCache<K, Object> mapCache, CacheConfig config, boolean allowNullValues) {
        this.mapCache = mapCache;
        this.map = mapCache;
        this.config = config;
        this.allowNullValues = allowNullValues;
    }

    public IRedissonCacheCache(RMap<K, Object> map, boolean allowNullValues) {
        this.map = map;
        this.allowNullValues = allowNullValues;
    }


    /**
     * Return the cache name.
     */
    @Override
    public String getName() {
        if (this.mapCache != null) {
            return this.mapCache.getName();
        } else if (this.map != null) {
            return this.map.getName();
        }
        return null;
    }

    /**
     * Return the underlying native cache provider.
     */
    @Override
    public Object getNativeCache() {
        if (this.mapCache != null) {
            return this.mapCache;
        } else if (this.map != null) {
            return this.map;
        }
        return null;
    }

    /**
     * Return the value to which this cache maps the specified key.
     * <p>Returns {@code null} if the cache contains no mapping for this key;
     * otherwise, the cached value (which may be {@code null} itself) will
     * be returned in a {@link ValueWrapper}.
     *
     * @param key the key whose associated value is to be returned
     * @return the value to which this cache maps the specified key,
     * contained within a {@link ValueWrapper} which may also hold
     * a cached {@code null} value. A straight {@code null} being
     * returned means that the cache contains no mapping for this key.
     * @see #get(Object, Class)
     * @see #get(Object, Callable)
     */
    @Override
    public ValueWrapper get(Object key) {
        //because the result is obj
        Object value;
        if (this.mapCache != null) {
            value = this.mapCache.get(key);
        } else {
            value = this.map.get(key);
        }
        return null != fromStoreValue(value) ? new SimpleValueWrapper(fromStoreValue(value)) : null;
    }

    /**
     * Return the value to which this cache maps the specified key,
     * generically specifying a type that return value will be cast to.
     * <p>Note: This variant of {@code get} does not allow for differentiating
     * between a cached {@code null} value and no cache entry found at all.
     * Use the standard {@link #get(Object)} variant for that purpose instead.
     *
     * @param key  the key whose associated value is to be returned
     * @param type the required type of the returned value (may be
     *             {@code null} to bypass a type check; in case of a {@code null}
     *             value found in the cache, the specified type is irrelevant)
     * @return the value to which this cache maps the specified key
     * (which may be {@code null} itself), or also {@code null} if
     * the cache contains no mapping for this key
     * @throws IllegalStateException if a cache entry has been found
     *                               but failed to match the specified type
     * @see #get(Object)
     * @since 4.0
     */
    @Override
    public <T> T get(Object key, Class<T> type) {
        Object value;
        if (mapCache != null) {
            value = this.mapCache.get(key);
        } else {
            value = this.map.get(key);
        }
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalArgumentException("cached values is not of required type [" + type.getName() + "]: " + value);
        }
        return (T) value;
    }

    /**
     * Return the value to which this cache maps the specified key, obtaining
     * that value from {@code valueLoader} if necessary. This method provides
     * a simple substitute for the conventional "if cached, return; otherwise
     * create, cache and return" pattern.
     * <p>If possible, implementations should ensure that the loading operation
     * is synchronized so that the specified {@code valueLoader} is only called
     * once in case of concurrent access on the same key.
     * <p>If the {@code valueLoader} throws an exception, it is wrapped in
     * a {@link ValueRetrievalException}
     *
     * @param key         the key whose associated value is to be returned
     * @param valueLoader
     * @return the value to which this cache maps the specified key
     * @throws ValueRetrievalException if the {@code valueLoader} throws an exception
     * @see #get(Object)
     * @since 4.3
     */
    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        Object value;
        if (this.mapCache != null) {
            value = this.mapCache.get(key);
        } else {
            value = this.map.get(key);
        }
        if (value != null) {
            return (T) value;
        } else {
            return loadValue(key, valueLoader);
        }
    }

    /**
     * Associate the specified value with the specified key in this cache.
     * <p>If the cache previously contained a mapping for this key, the old
     * value is replaced by the specified value.
     * <p>Actual registration may be performed in an asynchronous or deferred
     * fashion, with subsequent lookups possibly not seeing the entry yet.
     * This may for example be the case with transactional cache decorators.
     * Use {@link #putIfAbsent} for guaranteed immediate registration.
     *
     * @param key   the key with which the specified value is to be associated
     * @param value the value to be associated with the specified key
     * @see #putIfAbsent(Object, Object)
     */
    @Override
    public void put(Object key, Object value) {
        Object previous;
        if (!allowNullValues && value == null) {
            if (mapCache != null) {
                previous = mapCache.remove(key);
            } else {
                previous = map.remove(key);
            }
            return;
        }
        if (value == null) {
            value = NullValue.INSTANCE;
        }
        if (mapCache != null) {
            mapCache.put((K) key, value, config.getTTL(), TimeUnit.MILLISECONDS, config.getMaxIdleTime(),
                    TimeUnit.MILLISECONDS);
        } else {
            map.put((K) key, value);
        }
    }


    /**
     * Evict the mapping for this key from this cache if it is present.
     * <p>Actual eviction may be performed in an asynchronous or deferred
     * fashion, with subsequent lookups possibly still seeing the entry.
     * This may for example be the case with transactional cache decorators.
     * Use {@link #evictIfPresent} for guaranteed immediate removal.
     *
     * @param key the key whose mapping is to be removed from the cache
     * @see #evictIfPresent(Object)
     */
    @Override
    public void evict(Object key) {
        if (mapCache != null) {
            this.mapCache.remove(key);
        } else {
            this.map.remove(key);
        }
    }

    /**
     * Clear the cache through removing all mappings.
     * <p>Actual clearing may be performed in an asynchronous or deferred
     * fashion, with subsequent lookups possibly still seeing the entries.
     * This may for example be the case with transactional cache decorators.
     * Use {@link #invalidate()} for guaranteed immediate removal of entries.
     *
     * @see #invalidate()
     */
    @Override
    public void clear() {
        if (mapCache != null) {
            this.mapCache.clear();
        } else {
            this.map.clear();
        }
    }


    protected V fromStoreValue(Object storeValue) {
        if (storeValue instanceof NullValue) {
            return null;
        }
        return (V) storeValue;
    }

    protected Object toStoreValue(V userValue) {
        if (userValue == null) {
            return NullValue.INSTANCE;
        }
        return userValue;
    }

    private <T> T loadValue(Object key, Callable<T> valueLoader) {
        T value;
        try {
            value = valueLoader.call();
        } catch (Throwable ex) {
            throw new ValueRetrievalException(key, valueLoader, ex);
        }
        put(key, value);
        return value;
    }

    public void fastPut(K key, V v) throws CacheException {
        if ((!allowNullValues && v == null)) {
            if (mapCache != null) {
                mapCache.fastRemove(key);
            } else {
                map.fastRemove(key);
            }
            return;
        }
        Object val = toStoreValue(v);
        if (mapCache != null) {
            mapCache.fastPut(key, val, config.getTTL(), TimeUnit.MILLISECONDS, config.getMaxIdleTime(),
                    TimeUnit.MILLISECONDS);
        } else {
            map.fastPut(key, v);
        }
    }

    public boolean fastPutIfAbsent(K key, V value) throws CacheException {
        if (!allowNullValues && value == null) {
            return false;
        }
        Object val = toStoreValue(value);
        if (mapCache != null) {
            return mapCache.fastPutIfAbsent(key, val, config.getTTL(), TimeUnit.MILLISECONDS, config.getMaxIdleTime(),
                    TimeUnit.MILLISECONDS);
        } else {
            return map.fastPutIfAbsent(key, val);
        }
    }

    /**
     * The number of get requests that were satisfied by the cache.
     *
     * @return the number of hits
     */
    long getCacheHits() {
        return this.hits.get();
    }


    /**
     * A miss is a get request that is not satisfied.
     *
     * @return the number of misses
     */
    long getCacheMisses() {
        return this.misses.get();
    }

    private void addCacheHit() {
        this.hits.incrementAndGet();
    }

    private void addCacheMiss() {
        this.misses.incrementAndGet();
    }
}
