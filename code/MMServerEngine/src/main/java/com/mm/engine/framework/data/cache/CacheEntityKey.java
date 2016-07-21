package com.mm.engine.framework.data.cache;

/**
 * Created by Administrator on 2015/11/24.
 */
public class CacheEntityKey {
    /**
     * memcached中存储数据的key
     * 每个CacheEntity在memcached中的缓存包括两部分：CacheEntity自身和对应的变动
     * 变动只包括CacheEntity中的其它CacheEntity，且是需要同步的，即SynchronousLevel！=NoSync，
     * 如果CacheEntity自身就不是NoSync，就不需要对应的变动数据了
     * **/
    protected static String publicCacheKey(long id,Class<? extends CacheEntity> cls){
        return cls.getName()+id;
    }
    protected static String publicModifyCacheKey(String entityKey){
        return "modify"+entityKey;
    }
    protected static String publicModifyCacheKey(long id,Class<? extends CacheEntity> cls){
        return "modify"+cls.getName()+id;
    }
    protected static String publicModifyCacheKey(CacheEntity entity){
        return publicModifyCacheKey(entity.getCacheId(),entity.getClass());
    }
}
