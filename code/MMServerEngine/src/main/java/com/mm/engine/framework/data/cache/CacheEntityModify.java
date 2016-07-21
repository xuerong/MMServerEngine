package com.mm.engine.framework.data.cache;

import com.mm.engine.framework.data.SynchronousLevel;

import java.util.Map;

/**
 * Created by Administrator on 2015/11/24.
 * 这个是其它CacheEntity的存储在memcached中的改动记录，
 * 每一个变动，需要存储父CacheEntity对象的：变量名，新值
 */
public class CacheEntityModify extends CacheEntity{
    //private Map<>
    public CacheEntityModify() {
        super(SynchronousLevel.Public);
    }

    @Override
    public long getCacheId() {
        return 0;
    }
}
