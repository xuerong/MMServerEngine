package com.mm.engine.framework.data.cache;

/**
 * Created by Administrator on 2015/11/24.
 */
public interface CacheCenter {
    /**
     * 存入新的CacheEntity，返回缓存是否成功
     * 如果缓存中已经存在，返回false
     * */
    public Object putIfAbsent(String key,Object entity);
    /**
     * 获取
     * */
    public Object get(String key);
    /**
     * 移除
     * */
    public Object remove(String key);
    /**
     *  用户保存回用这个函数
     * 要根据更新需要进行更新
     * */
    public boolean update(String key,Object entity);
}
