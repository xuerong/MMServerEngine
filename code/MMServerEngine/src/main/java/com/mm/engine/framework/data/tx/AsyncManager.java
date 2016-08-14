package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.data.cache.KeyParser;

/**
 * Created by apple on 16-8-14.
 */
public class AsyncManager {
    /**
     * 插入一个对象，
     */
    public static boolean insert(Object entity){
        return false;
    }
    /**
     * 更新一个对象，
     */
    public static boolean update(Object entity){
        return false;
    }
    /**
     * 删除一个实体
     * 由于要异步删除，缓存中设置删除标志位,所以，在缓存中是update
     */
    public static boolean delete(Object entityObject){
        return false;
    }
}
