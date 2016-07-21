package com.mm.engine.framework.data.cache;

import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.CacheManagerBuilder;
import org.ehcache.config.CacheConfigurationBuilder;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by Administrator on 2015/11/24.
 */
public class EhCacheHelper {
    private static Set<Class<? extends CacheEntity>> cacheEntityClassList=new HashSet<>();

    private static Map<Class<? extends CacheEntity>, Cache> cacheMap=new HashMap<>();


    static{
        // 根据继承自CacheEntity创建缓存组
        CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
        for(Class<? extends CacheEntity> cacheEntityClass : cacheEntityClassList){
            cacheMap.put(cacheEntityClass,cacheManager.createCache(cacheEntityClass.getName(),
                    CacheConfigurationBuilder.newCacheConfigurationBuilder().buildConfig(Long.class, CacheEntity.class)));
        }
    }
    /**
     * 添加数据，并返回相应的key
     * */
    public static boolean putNew(CacheEntity entity) {
        Cache<Long, CacheEntity> cache=cacheMap.get(entity.getClass());
        cache.put(entity.getCacheId(),entity);
        return true;
    }

    public static CacheEntity get(Long key,Class<? extends CacheEntity> cls) {
        Cache<Long, CacheEntity> cache=cacheMap.get(cls);
        return cache.get(key);
    }

    public static boolean remove(Long key,Class<? extends CacheEntity> cls) {
        Cache<Long, CacheEntity> cache=cacheMap.get(cls);
        cache.remove(key);
        return true;
    }

    public static boolean save(CacheEntity entity, CacheEntity... newChildEntitys) {

        return false;
    }
}
