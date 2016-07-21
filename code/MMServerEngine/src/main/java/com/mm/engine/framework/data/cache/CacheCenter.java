package com.mm.engine.framework.data.cache;

/**
 * Created by Administrator on 2015/11/24.
 */
public interface CacheCenter {
    /**
     * 存入新的CacheEntity，返回缓存是否成功
     * 如果缓存中已经存在，返回false
     * */
    public boolean putNew(CacheEntity entity);
    /**
     * 获取
     * */
    public CacheEntity get(Long id, Class<? extends CacheEntity> cls);
    /**
     * 移除
     * */
    public boolean remove(Long id,Class<? extends CacheEntity> cls);
    public boolean remove(CacheEntity entity);
    public boolean removeLocalCache(Long id,Class<? extends CacheEntity> cls);
    /**
     *  用户保存回用这个函数
     * 要根据更新需要进行更新
     * */
    public boolean save(CacheEntity entity);
}
