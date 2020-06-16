package com.mry.chat.letschat.common.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import net.sf.ehcache.Status;
import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.support.SimpleValueWrapper;
import org.springframework.util.Assert;

import java.net.URL;
import java.util.ResourceBundle;
import java.util.concurrent.Callable;

/**
 * @author root
 */
public class IEhCacheCache implements IEhcache  {


    private static Logger logger = LoggerFactory.getLogger(IEhCacheCache.class);

    private final Ehcache ehcache;


    public IEhCacheCache(Ehcache ehcache) {
        Assert.notNull(ehcache, "ehcache must not be null ");
        Status status = ehcache.getStatus();
        if (!Status.STATUS_ALIVE.equals(status)) {
            throw new IllegalArgumentException("An ALIVE ehcache is required -current cache status is " + status.toString());
        }
        this.ehcache = ehcache;
    }


    /**
     * Return the cache name.
     */
    @Override
    public String getName() {
        return this.ehcache.getName();
    }

    /**
     * Return the underlying native cache provider.
     */
    @Override
    public Object getNativeCache() {
        return this.ehcache;
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
        Element ele = lookup(key);
        return toValueWrapper(ele);
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
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        Element element = this.ehcache.get(key);
        Object value = (element != null ? element.getObjectValue() : null);
        if (value != null && type != null && !type.isInstance(value)) {
            throw new IllegalStateException(
                    "Cached value is not of required type [" + type.getName() + "]: " + value);
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
        Element element = lookup(key);
        if (element != null) {
            return (T) element.getObjectValue();
        } else {
            this.ehcache.acquireWriteLockOnKey(key);
            try {

                element = lookup(key);
                if (element != null) {
                    return (T) element.getObjectValue();
                } else {
                    return loadValue(key, valueLoader);
                }
            } catch (Throwable ex) {
                throw new RuntimeException(ex.getMessage(), ex);
            } finally {
                this.ehcache.releaseWriteLockOnKey(key);
            }
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
        this.ehcache.put(new Element(key, value));
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
        this.ehcache.remove(key);
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
        this.ehcache.removeAll();
    }


    private Element lookup(Object key) {
        return this.ehcache.get(key);
    }

    private ValueWrapper toValueWrapper(Element element) {
        return element != null ? new SimpleValueWrapper(element.getObjectValue()) : null;
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


}
