package com.mm.engine.framework.data.cache;

import com.mm.engine.framework.data.Entity;
import com.mm.engine.framework.data.SynchronousLevel;

import java.util.HashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by Administrator on 2015/11/24.
 * 可以缓存的对象都要继承自该抽象类
 */
public abstract class CacheEntity implements Entity {
    private SynchronousLevel synchronousLevel;
    private AtomicLong version=new AtomicLong(0);
    private long cacheId;
    public CacheEntity(SynchronousLevel synchronousLevel){
        this.synchronousLevel=synchronousLevel;
        cacheId = CacheIdCreate.createId(getClass());
    }

    public SynchronousLevel getSynchronousLevel() {
        return synchronousLevel;
    }

    public AtomicLong getVersion() {
        return version;
    }

    public long getCacheId() {
        return cacheId;
    }
}
class CacheIdCreate{
    public static HashMap<Class<?>,AtomicLong> idMap = new HashMap<>();

    public static long createId(Class<?> cls){
        AtomicLong idA = idMap.get(cls);
        if(idA==null){
            synchronized (idMap){
                if(!idMap.containsKey(cls)){
                    idA=new AtomicLong(0);
                    idMap.put(cls,idA);
                }else{
                    idA = idMap.get(cls);
                }
            }
        }
        return idA.decrementAndGet();
    }
}
