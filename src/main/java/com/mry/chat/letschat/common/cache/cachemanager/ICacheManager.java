package com.mry.chat.letschat.common.cache.cachemanager;

import java.util.Collection;
import java.util.Map;

import com.google.common.collect.Maps;
import com.mry.chat.letschat.common.cache.ICache;
import com.mry.chat.letschat.common.cache.IEhCacheCache;
import net.sf.ehcache.CacheManager;
import org.apache.shiro.ShiroException;
import org.apache.shiro.util.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;


/**
 * @author root
 */
public class ICacheManager implements org.springframework.cache.CacheManager {


    private  static Logger logger = LoggerFactory.getLogger(ICacheManager.class);

    private  String filepath = "ehcache.xml";

    public String getFilepath() {
        return filepath;
    }

    public void setFilepath(String filepath) {
        this.filepath = filepath;
    }

    private String[] regions={"test_cache"};

    public void setRegion(String[] regions) {
        this.regions = regions;
    }

    private Map<String ,ICache> caches = Maps.newHashMap();


    public void init() throws ShiroException {
        logger.info("---------------------------------");
        CacheManager cachemanager = CacheManager.create(ICacheManager.class.getClassLoader().getResourceAsStream(filepath));
        for (String str :this.regions){
            IEhCacheCache ehcace = new IEhCacheCache(cachemanager.getEhcache(str));
           logger.info(ehcace.getName()+" ....cache name ....");
            caches.put(str,ehcace);
        }
    }

    /**
     * Get the cache associated with the given name.
     * <p>Note that the cache may be lazily created at runtime if the
     * native provider supports it.
     *
     * @param name the cache identifier (must not be {@code null})
     * @return the associated cache, or {@code null} if such a cache
     * does not exist or could be not created
     */
    @Override
    public Cache getCache(String name) {
        return this.caches.get(name);
    }

    /**
     * Get a collection of the cache names known by this manager.
     *
     * @return the names of all caches known by the cache manager
     */
    @Override
    public Collection<String> getCacheNames() {
        return this.caches.keySet();
    }
}