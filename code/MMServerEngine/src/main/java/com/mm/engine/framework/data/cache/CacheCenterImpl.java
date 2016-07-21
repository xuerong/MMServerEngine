package com.mm.engine.framework.data.cache;

/**
 * Created by Administrator on 2015/11/24.
 * CacheCenterImpl中的本地缓存使用ehcache，公共缓存使用memcached
 */
public class CacheCenterImpl implements CacheCenter {
    /**
     * 添加新数据时，放在本地缓存，并发送公共缓存
     *
     * 在这一层要对缓存中出现的失败进行基本的处理，如打印日志
     * */
    @Override
    public boolean putNew(CacheEntity entity) {
        if(!EhCacheHelper.putNew(entity)){
            return false;
        }
        if(!MemCachedHelper.put(entity)){
            return false;
        }
        return true;
    }

    /**
     * 从缓存中获取数据
     * 如果本地存在，返回本地，否则返回公共缓存的，否则，返回null
     * */
    @Override
    public CacheEntity get(Long id, Class<? extends CacheEntity> cls) {
        CacheEntity entity=EhCacheHelper.get(id,cls);
        if(entity!=null){
            return entity;
        }
        entity = MemCachedHelper.get(CacheEntityKey.publicCacheKey(id,cls));
        if(entity!=null){
            // 放在本地缓存
            if(!EhCacheHelper.putNew(entity)){
                // 缓存本地失败
            }
        }
        return entity;
    }
    /**
     * 从本地缓存中移除，并从公共缓存中移除
     * */
    @Override
    public boolean remove(Long id, Class<? extends CacheEntity> cls) {
        EhCacheHelper.remove(id,cls);
        MemCachedHelper.remove(CacheEntityKey.publicCacheKey(id,cls));
        return false;
    }

    @Override
    public boolean remove(CacheEntity entity) {
        remove(entity.getCacheId(),entity.getClass());
        return false;
    }

    @Override
    public boolean removeLocalCache(Long id, Class<? extends CacheEntity> cls) {
        EhCacheHelper.remove(id,cls);
        return true;
    }

    /**
     * 重新保存数据
     * 缓存中的数据不需要变动，因为是引用
     * 更新memcached中的变动数据
     * */
    @Override
    public boolean save(CacheEntity entity) {
        switch (entity.getSynchronousLevel()){
            case NoSync:
                // do nothing
                break;
            case Public:
                // 发送到memcached
                MemCachedHelper.put(entity);
                break;
            case Expired:
                if(MemCachedHelper.put(entity)){

                }
                // 广播过期
                break;
            case RealTime:
                if(MemCachedHelper.put(entity)){

                }
                // 广播给其它服务器
                break;
        }
        return false;
    }
}
