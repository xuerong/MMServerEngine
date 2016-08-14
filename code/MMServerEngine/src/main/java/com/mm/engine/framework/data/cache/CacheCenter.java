package com.mm.engine.framework.data.cache;

/**
 * Created by Administrator on 2015/11/24.
 *
 * 这里面的数据可以考虑不用返回(flush)而是出现错误抛出异常
 */
public interface CacheCenter {
    /**
     * 存入新的CacheEntity，返回缓存是否成功
     * 如果缓存中已经存在，返回false
     * */
    public CacheEntity putIfAbsent(String key,CacheEntity entity);
    /**
     * 获取
     * */
    public CacheEntity get(String key);
    /**
     * 移除
     * */
    public CacheEntity remove(String key);
    /**
     *  更新,不考虑版本
     *  不用cas,因为cas失败了会导致事务无法回退,用的是先所有的加锁之后校验,
     * */
    public boolean update(String key,CacheEntity entity);
}
