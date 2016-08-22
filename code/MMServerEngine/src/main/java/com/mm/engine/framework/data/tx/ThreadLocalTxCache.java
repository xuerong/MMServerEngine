package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.data.DataCenter;
import com.mm.engine.framework.data.OperType;
import com.mm.engine.framework.data.cache.CacheEntity;
import com.mm.engine.framework.data.cache.KeyParser;
import com.mm.engine.framework.exception.ExceptionHelper;
import com.mm.engine.framework.exception.ExceptionLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by a on 2016/8/12.
 * 在一个事务的service提交之前，把数据存储到这里，在提交的时候，提交到数据库
 * 方案如下：
 * insert：
 */
public class ThreadLocalTxCache {
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalTxCache.class);

    // 注意，这里面的数据应该是乱序的，提交的时候应该是顺序的，所以要坐个sort
    // 这样写默认了初始化,所以不用再赋值给线程了
    private static ThreadLocal<Map<String, PrepareCachedData>> cacheDatas = new ThreadLocal<Map<String, PrepareCachedData>>() {
        protected Map<String, PrepareCachedData> initialValue() {
            return new HashMap<String, PrepareCachedData>();
        }
    };
    // 这个默认不赋值,这样在事务开始的时候,如果需要锁再赋值,不赋值就代表不需要锁
    private static final ThreadLocal<Set<Class<?>>> lockClasses = new ThreadLocal<Set<Class<?>>>();

    private static ThreadLocal<TxState> txStates = new ThreadLocal<TxState>(){
        protected TxState initialValue() {
            return TxState.Absent;
        }
    };

    public static boolean isLockClass(Class<?> cls){
        Set<Class<?>> lockClassSet = lockClasses.get();
        return lockClassSet != null && lockClassSet.contains(cls);
    }

    public static boolean isInTx(){
        return txStates.get() == TxState.In;
    }
    public static void setTXState(TxState txState){
        txStates.set(txState);
    }

    /**
     * 事务提交
     * 若要加锁,则先加锁(main服加锁),若要验证,则加锁后验证,都加锁验证通过,然后提交
     * 将对象的key和对应的casUnique都提交给main服,其进行加锁和验证,
     */
    public static boolean commit(){
        setTXState(TxState.Committing);

        Map<String, PrepareCachedData> map = cacheDatas.get();
        if(map == null){
            return true;
        }
        // -- 加锁校验
        Set<Class<?>> lockClass = lockClasses.get();
        List<LockerManager.LockerData> lockerDataList = null;
        if(lockClass != null && lockClass.size()>0){ // 说明要加锁
            // 提取要加锁的对象
            for (Map.Entry<String, PrepareCachedData> entry:map.entrySet()){
                PrepareCachedData data = entry.getValue();
                if(data.getOperType() != OperType.Select && lockClass.contains(data.getData().getClass())){
                    LockerManager.LockerData lockerData = new LockerManager.LockerData();
                    lockerData.setKey(data.getKey());
                    lockerData.setOperType(data.getOperType());
                    long casUnique = -1;
                    CacheEntity older = DataCenter.getCacheEntity(data.getKey());
                    if(older != null){
                        casUnique = older.getCasUnique();
                    }
                    lockerData.setCasUnique(casUnique);
                    if(lockerDataList == null){
                        lockerDataList = new ArrayList<>();
                    }
                    lockerDataList.add(lockerData);
                }
            }
            if(lockerDataList!=null && lockerDataList.size()>0){
                boolean result = LockerManager.lockAndCheckKeys((LockerManager.LockerData[])lockerDataList.toArray());
                if(!result){ // 加锁校验失败,提交也就失败
                    return false;
                }
            }
        }
        // --- 提交事务,无论中间出现什么情况,都要解锁
        try{
            for(PrepareCachedData data : map.values()){
                switch (data.getOperType()){
                    case Insert:
                        DataCenter.insert(data.getData()); // 这个地方用这种方式提交,如果有需要,可以换方式
                        break;
                    case Update:
                        DataCenter.update(data.getData());
                        break;
                    case Delete:
                        DataCenter.delete(data.getData());
                        break;

                }
            }
        }finally {
            // -- 解锁
            if(lockerDataList!=null){
                int size = lockerDataList.size();
                if(size > 0){
                    String[] keys = new String[size];
                    int i=0;
                    for(LockerManager.LockerData lockerData : lockerDataList){
                        keys[i++] = lockerData.getKey();
                    }
                    LockerManager.unlockKeys(keys);
                }
            }
        }
        return true;
    }


    public static PrepareCachedData get(String key){
        return cacheDatas.get().get(key);
    }

    public static <T> List<T> replaceCacheObjectToList(String listKey,List<T> objectList){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        if(map.size() > 0){
            Map<String,Integer> keyMap = null;

            for (String key:map.keySet()) {
                PrepareCachedData prepareCachedData = map.get(key);
                if(!KeyParser.isObjectBelongToList(prepareCachedData.getData(),listKey)){
                    continue;
                }
                // 替换，或添加，或删除
                if(keyMap == null &&
                        prepareCachedData.getOperType() != OperType.Select){
                    keyMap = new HashMap<>();
                    int i = 0;
                    for (T t:objectList) {
                        keyMap.put(KeyParser.parseKey(t),i++);
                    }
                }
                // 替换和添加,如果objectMap中存在,都要替换
                Integer index = keyMap.get(key);
                if(index != null){
                    if(prepareCachedData.getOperType() == OperType.Update
                            || prepareCachedData.getOperType() == OperType.Insert){
                        objectList.remove(index);
                        objectList.add(index,(T)prepareCachedData.getData());
                        if(prepareCachedData.getOperType() == OperType.Insert){
                            log.warn("insert object in this threadLocalCache is exist in objectList ,objectKey = "
                                    +key+",listKey = "+listKey);
                        }
                    }else if(prepareCachedData.getOperType() == OperType.Delete){
                        objectList.remove(index);
                    }
                }else{
                    if(prepareCachedData.getOperType() == OperType.Insert
                            || prepareCachedData.getOperType() == OperType.Update){
                        objectList.add((T)prepareCachedData.getData());
                        if(prepareCachedData.getOperType() == OperType.Update){
                            log.warn("update object in this threadLocalCache is not exist in objectList ,objectKey = "
                                    +key+",listKey = "+listKey);
                        }
                    }
                }

            }
        }
        return objectList;
    }

    public static boolean putWhileSelect(String key,Object entity){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        PrepareCachedData prepareCachedData = new PrepareCachedData();
        prepareCachedData.setData(entity);
        prepareCachedData.setKey(key);
        prepareCachedData.setOperType(OperType.Select);
        PrepareCachedData older = map.put(key,prepareCachedData);
        if(older!= null){
            log.warn("older != null while insert key   =  "+key);
        }
        return true;
    }
    /**
     * 插入一个对象，
     */
    public static boolean insert(String key,Object entity){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        PrepareCachedData older = map.get(key);
        if(older != null && older.getOperType() != OperType.Delete){
            ExceptionHelper.handle(ExceptionLevel.Warn,"object is exist while insert object key = "+key,null);
        }

        PrepareCachedData prepareCachedData = new PrepareCachedData();
        prepareCachedData.setData(entity);
        prepareCachedData.setKey(key);
        prepareCachedData.setOperType(OperType.Insert);
        map.put(key,prepareCachedData);
        return true;
    }
    /**
     * 更新一个对象，
     */
    public static boolean update(String key,Object entity){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        PrepareCachedData older = map.get(key);
        if(older != null && older.getOperType() == OperType.Delete){
            ExceptionHelper.handle(ExceptionLevel.Warn,"object has deleted while update object key = "+key,null);
        }
        PrepareCachedData prepareCachedData = new PrepareCachedData();
        prepareCachedData.setData(entity);
        prepareCachedData.setKey(key);
        prepareCachedData.setOperType(OperType.Update);
        map.put(key,prepareCachedData);
        return true;
    }
    /**
     * 删除一个实体
     * 由于要异步删除，缓存中设置删除标志位,所以，在缓存中是update
     */
    public static boolean delete(String key,Object entity){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        PrepareCachedData prepareCachedData = new PrepareCachedData();
        prepareCachedData.setData(entity);
        prepareCachedData.setKey(key);
        prepareCachedData.setOperType(OperType.Delete);
        map.put(key,prepareCachedData);
        return true;
    }
    /**
     * 删除一个实体,condition必须是主键
     */
//    public static <T> boolean delete(Class<T> entityClass, String condition, Object... params){
//        return false;
//    }

    public static class PrepareCachedData {
        private OperType operType;
        private String key;
        private Object data;

        public OperType getOperType() {
            return operType;
        }

        public void setOperType(OperType operType) {
            this.operType = operType;
        }

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public Object getData() {
            return data;
        }

        public void setData(Object data) {
            this.data = data;
        }
    }
//    public static void main(String[] args){
//        ThreadLocal<Map<String, PrepareCachedData>> cacheDatas =
//                new ThreadLocal<Map<String, PrepareCachedData>>() {
//            protected Map<String, PrepareCachedData> initialValue() {
//                return new HashMap<String, PrepareCachedData>();
//            }
//        };
//        Map<String, PrepareCachedData> data = cacheDatas.get();
//        System.out.println(data);
//    }
}
