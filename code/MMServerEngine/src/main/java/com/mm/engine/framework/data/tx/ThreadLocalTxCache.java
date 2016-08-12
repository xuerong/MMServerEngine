package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.data.OperType;
import com.mm.engine.framework.data.cache.KeyParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by a on 2016/8/12.
 * 在一个事务的service提交之前，把数据存储到这里，在提交的时候，提交到数据库
 * 方案如下：
 * insert：
 */
public class ThreadLocalTxCache {
    private static final Logger log = LoggerFactory.getLogger(ThreadLocalTxCache.class);

    // 注意，这里面的数据应该是乱序的，提交的时候应该是顺序的，所以要坐个sort
    private static ThreadLocal<Map<String, PrepareCachedData>> cacheDatas = new ThreadLocal<Map<String, PrepareCachedData>>() {
        protected Map<String, PrepareCachedData> initialValue() {
            return new HashMap<String, PrepareCachedData>();
        }
    };

    private static ThreadLocal<TxState> txStates = new ThreadLocal<TxState>(){
        protected TxState initialValue() {
            return TxState.Absent;
        }
    };

    public static boolean isInTx(){
        return txStates.get() == TxState.In;
    }


    public static PrepareCachedData get(String key){
        return cacheDatas.get().get(key);
    }

    public static <T> List<T> replaceCacheObjectToList(String listKey,List<T> objectList){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        for (String key:map.keySet()) {
            PrepareCachedData prepareCachedData = map.get(key);
            if(!KeyParser.isObjectBelongToList(prepareCachedData.getData(),listKey)){
                continue;
            }
            // 替换，或添加，或删除

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
            log.warn("older != null while insert key = "+key);
        }
        return true;
    }
    /**
     * 插入一个对象，
     */
    public static boolean insert(String key,Object entity){
        Map<String, PrepareCachedData> map = cacheDatas.get();
        PrepareCachedData prepareCachedData = new PrepareCachedData();
        prepareCachedData.setData(entity);
        prepareCachedData.setKey(key);
        prepareCachedData.setOperType(OperType.Insert);
        PrepareCachedData older = map.put(key,prepareCachedData);
        if(older!= null){
            log.warn("older !=  null , while   insert key = "+key);
        }
        return true;
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
}
