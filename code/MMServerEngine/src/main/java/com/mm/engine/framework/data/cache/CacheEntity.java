package com.mm.engine.framework.data.cache;

import java.io.Serializable;

/**
 * Created by a on 2016/8/12.
 *
 * 对需要缓存的数据进行一下封装，然后缓存CacheEntity
 * 这里面可以记录一些特殊的点
 */
public class CacheEntity implements Serializable{
    private Object entity;
    private CacheEntityState state;
    private long casUnique;

    public CacheEntity(){
        this(null);
    }
    public CacheEntity(Object entity){
        this.entity = entity;
        this.state = CacheEntityState.Normal;
    }

    public Object getEntity() {
        return entity;
    }

    public void setEntity(Object entity) {
        this.entity = entity;
    }

    public CacheEntityState getState() {
        return state;
    }

    public void setState(CacheEntityState state) {
        this.state = state;
    }

    public long getCasUnique() {
        return casUnique;
    }

    public void setCasUnique(long casUnique) {
        this.casUnique = casUnique;
    }

    //
    public static enum CacheEntityState{
        Normal,
        Delete, // 这个说明该数据已经被删除
        HasNot//说明数据库中也没有，这样就不要穿透到数据库判断一个没有的数据，可以考虑用Delete？
    }
}
