package com.mm.engine.framework.data;

import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.data.cache.CacheEntity;
import com.mm.engine.framework.data.cache.KeyParser;
import com.mm.engine.framework.data.persistence.orm.DataSet;
import com.mm.engine.framework.data.tx.ThreadLocalTxCache;
import com.mm.engine.framework.tool.helper.BeanHelper;

import java.security.Key;
import java.util.List;

/**
 * Created by a on 2016/8/10.
 * 这个是操作数据的对外接口
 * 对于对对象的增删该查，都是对缓存和数据库的操作
 *
 * 注意：这里面锁操作的对象都必须注解：DBEntity
 * 如果仅对缓存进行操作，请用CacheCenter
 *
 * 这里仅提供这些方法
 *
 * 过程：
 * 如果处理的数据在事务进行中，则将数据交给事务线程缓存处理，
 * 否则，对缓存进行处理，对于get操作，缓存不存在要穿透到数据库进行查询，而flush操作则仅更新缓存，和异步数据库
 *
 *
 */
public class DataCenter {
    private static final CacheCenter cacheCenter;
    static {
        cacheCenter= BeanHelper.getFrameBean(CacheCenter.class);
    }
    /**
     * 查询一个对象，condition必须是主键，否则请用selectList
     *
     * 这里需要保存一下获取数据的版本，其实就是保存一下CacheEntity的引用，这样可以在update的时候cas用
     */
    public static <T> T selectObject(Class<T> entityClass, String condition, Object... params){
        // 如果在事务中，先从事务缓存中get，如果事务缓存中没有，则从数据中取，并放入事务,根据情况确定是否返回
        String key = KeyParser.parseKeyForObject(entityClass,condition,params);
        Object object = null;
        if(ThreadLocalTxCache.isInTx()){
            ThreadLocalTxCache.PrepareCachedData prepareCachedData = ThreadLocalTxCache.get(key);
            if(prepareCachedData != null){
                if(prepareCachedData.getOperType() != OperType.Delete){
                    return (T)prepareCachedData.getData();
                }else{ // 是delete说明已经被删除了
                    return null;
                }
            }
        }
        // 如果不在事务之中
        CacheEntity entity = (CacheEntity)cacheCenter.get(key);
        if(entity == null){
            object = DataSet.select(entityClass,condition,params);
            if(object != null){
                entity = new CacheEntity(object);
                cacheCenter.putIfAbsent(key,entity);
            }else{
                // 这里缓存有两种设计方案，一个是不缓存，一个是缓存一个无效值，防止总是通过查询为空来判断某一个条件，导致不断穿透到数据库
                entity = new CacheEntity(object);
                entity.setState(CacheEntity.CacheEntityState.HasNot);
                cacheCenter.putIfAbsent(key,entity);
            }
        }
        if(entity!= null && entity.getState() == CacheEntity.CacheEntityState.Normal) {
            if(ThreadLocalTxCache.isInTx()){ // 放入事务缓存，这里可以考虑不放入，下面的selectList也就可以不放入
                ThreadLocalTxCache.putWhileSelect(key,entity.getEntity());
            }
            return (T) (entity.getEntity());
        }
        return null;
    }
    /**
     * 查询一个列表
     * 这里先不做事务的缓存,我觉得还是有必要加的，不过要先想好怎么加，否则会影响效率(和缓存中一样,会有点鸡肋，要不上面的也不缓存)
     */
    public static <T> List<T> selectList(Class<T> entityClass, String condition, Object... params) {
        String key = KeyParser.parseKeyForList(entityClass,condition,params);
        CacheEntity entity = (CacheEntity)cacheCenter.get(key);
        if(entity == null){
            List<T> objectList = DataSet.selectListWithCondition(entityClass,condition,params);
            if(objectList != null){ // 0个也缓存
                entity = new CacheEntity(objectList);
                cacheCenter.putIfAbsent(key,entity);
            }
        }
        if(entity != null && entity.getState() == CacheEntity.CacheEntityState.Normal) {
            if(ThreadLocalTxCache.isInTx()){
                // 替换掉事务中新的值
            }
            return (List<T>)(entity.getEntity());
        }
        return null;
    }
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
    /**
     * 删除一个实体,condition必须是主键
     */
    public static <T> boolean delete(Class<T> entityClass, String condition, Object... params){
        return false;
    }
}
