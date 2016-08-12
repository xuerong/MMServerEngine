package com.mm.engine.framework.data.cache;

import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.helper.ClassHelper;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.CacheConfigurationBuilder;

import java.util.*;

/**
 * Created by Administrator on 2015/11/24.
 */
public class EhCacheHelper {
    private static final Map<String, Cache<String,Object>> cacheMap=new HashMap<>();
    private static final CacheManager cacheManager;

    static{
        // 根据继承自CacheEntity创建缓存组
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }
    /**
     * 添加数据，并返回相应的key
     * */
    public static boolean put(String key,Object entity) {
        Cache<String, Object> cache=getCache(entity);
        cache.put(key,entity);
        return true;
    }

    public static Object get(String key) {
        Cache<String, Object> cache=cacheMap.get(null);
        return cache.get(key);
    }

    public static boolean remove(String key) {
        Cache<String, Object> cache=getCache(null);
        cache.remove(key);
        return true;
    }

    public static boolean update(String key,Object entity) { // 更新时删除本地的缓存数据，防止数据不一致
//        Cache<String, Object> cache=getCache(entity);
//        cache.insert(key,entity);
        return remove(key);
    }

    private static Cache<String,Object> getCache(Object entity){
        String cacheKey = "cacheKey";//entity.getClass().getName();
        Cache<String, Object> cache=cacheMap.get(cacheKey);
        if(cache == null){
            synchronized (cacheMap){
                cache=cacheMap.get(cacheKey);
                if(cache == null){
                    cache = cacheManager.createCache(cacheKey,
                            CacheConfigurationBuilder.newCacheConfigurationBuilder().buildConfig(String.class, Object.class));
                    cacheMap.put(cacheKey,cache);
                }
            }
        }
        return cache;
    }
}
