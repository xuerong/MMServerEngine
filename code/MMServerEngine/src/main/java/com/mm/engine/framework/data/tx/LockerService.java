package com.mm.engine.framework.data.tx;

import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventService;
import com.mm.engine.framework.data.OperType;
import com.mm.engine.framework.data.cache.CacheCenter;
import com.mm.engine.framework.data.cache.CacheEntity;
import com.mm.engine.framework.security.exception.ExceptionHelper;
import com.mm.engine.framework.security.exception.ExceptionLevel;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Serializable;
import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apple on 16-8-21.
 * 事务提交时的锁控制,运行在一个服务器中(main服务器)
 * 主要是对要提交的对象进行加锁并进行版本校验
 */
@Service(init = "init")
public class LockerService {
    private static final Logger log = LoggerFactory.getLogger(LockerService.class);
    private static final int maxTryLockTimes = 20;

    private ConcurrentHashMap<String,String> lockers = new ConcurrentHashMap<>();
    private CacheCenter cacheCenter;

    private NetEventService netEventService;

    public void init(){
        netEventService = BeanHelper.getServiceBean(NetEventService.class);
        cacheCenter= BeanHelper.getFrameBean(CacheCenter.class);
    }
    ////---------------------------外部代用
    public boolean lockAndCheckKeys(LockerData... lockerDatas){
        Arrays.sort(lockerDatas); //先做个排序 防止死锁
        NetEventData eventData = new NetEventData(SysConstantDefine.LOCKKEYSANDCHECK);
        eventData.setParam(lockerDatas);
        NetEventData ret = netEventService.fireMainServerNetEventSyn(eventData); // 需要同步发送
        Boolean result = (Boolean)ret.getParam();
        if(result == null){
            result = false;
        }
        return result;
    }
    public void unlockKeys(String... keys){
        NetEventData eventData = new NetEventData(SysConstantDefine.UNLOCKKEYS);
        eventData.setParam(keys);
        netEventService.fireMainServerNetEvent(eventData); // 异步发送解锁就可以
    }
    // TODO 如果确保每个服务在自己返回的时候，即使发生异常，也会把加的锁释放
    // TODO 有没有必要先做个排序，防止死锁
    public boolean lockKeys(String... keys){
        Arrays.sort(keys); //先做个排序 防止死锁
        NetEventData eventData = new NetEventData(SysConstantDefine.LOCKKEYS);
        eventData.setParam(keys);
        NetEventData ret = netEventService.fireMainServerNetEventSyn(eventData); // 需要同步发送
        Boolean result = (Boolean)ret.getParam();
        if(result == null){
            result = false;
        }
        return result;
    }
    //// ----------------------------------外部代用end
    @NetEventListener(netEvent = SysConstantDefine.LOCKKEYS)
    public NetEventData receiveLockKeys(NetEventData eventData){
        String[] keys = (String[])eventData.getParam();
        boolean result = true;
        String failKey = null;
        for (String key : keys) {
            if(!lock(key)){
                result = false;
                failKey = key;
                break;
            }
        }
        if(!result){ // 如果加锁失败,已经加锁的要解锁
            for (String key : keys) {
                unlock(key);
                if(key.equals(failKey)){
                    break;
                }
            }
        }
        NetEventData ret = new NetEventData(eventData.getNetEvent(),result);
        return ret;
    }
    @NetEventListener(netEvent = SysConstantDefine.LOCKKEYSANDCHECK)
    public NetEventData receiveLockRequest(NetEventData eventData){
        LockerData[] lockerDatas = (LockerData[])eventData.getParam();
        boolean result = true;
        String failKey = null;
        for (LockerData lockerData : lockerDatas) {
            if(!lockAndCheck(lockerData.getKey(),lockerData.getOperType(),lockerData.getCasUnique())){
                result = false;
                failKey = lockerData.getKey();
                break;
            }
        }
        if(!result){ // 如果加锁失败,已经加锁的要解锁
            for (LockerData lockerData : lockerDatas) {
                unlock(lockerData.getKey());
                if(lockerData.getKey().equals(failKey)){
                    break;
                }
            }
        }
        NetEventData ret = new NetEventData(eventData.getNetEvent(),result);
        return ret;
    }
    @NetEventListener(netEvent = SysConstantDefine.UNLOCKKEYS)
    public NetEventData receiveUnLockRequest(NetEventData eventData){
        String[] keys = (String[])eventData.getParam();
        for(String key : keys){
            unlock(key);
        }
        return null;
    }
    /**
     * 加锁并校验
     * 校验规则:
     * update要进行版本校验
     * insert要进行有无校验
     * delete都ok
     * @param key
     * @param operType
     * @param casUnique
     */
    private boolean lockAndCheck(String key, OperType operType, long casUnique){
        if(!lock(key)){
            return false;
        }
        if(operType == OperType.Update){
            CacheEntity cacheEntity = cacheCenter.get(key); // 如果这个是从本地获取的,显然就没有更新过,so,没问题
            if(cacheEntity!= null && cacheEntity.getState() != CacheEntity.CacheEntityState.Normal){
                return false;
            }
            if(cacheEntity.getCasUnique() != casUnique){ // 没有更新过
                return false;
            }
        }else if(operType == OperType.Insert){
            CacheEntity cacheEntity = cacheCenter.get(key);
            if(cacheEntity!= null && cacheEntity.getState() == CacheEntity.CacheEntityState.Normal){
                return false;
            }
        }
        return true;
    }
    private boolean lock(String key){
        // 加锁
        String olderKey = lockers.putIfAbsent(key,key);
        int lockTime = 0;
        while(olderKey != null){ // 加锁失败,稍等再加
            if(lockTime++>maxTryLockTimes){
                // 这个地方不能用异常,因为加锁失败,要清理之前加成功的锁:如果考虑用最终捕获异常来清理锁，也可以考虑，但还是不如这样
//                ExceptionHelper.handle(ExceptionLevel.Warn,"锁超时,key = "+key,null);
                log.warn("锁超时,key = "+key);
                return false;
            }
            try {
                Thread.sleep(10);
                olderKey = lockers.putIfAbsent(key,key);
            }catch (InterruptedException e){
                ExceptionHelper.handle(ExceptionLevel.Warn,"锁异常,key = "+key,e);
                return false;
            }
        }
        return true;
    }
    private void unlock(String key){
        lockers.remove(key);
    }

    public static class LockerData implements Serializable,Comparable<LockerData>{
        private String key;
        private OperType operType;
        private long casUnique;

        public String getKey() {
            return key;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public OperType getOperType() {
            return operType;
        }

        public void setOperType(OperType operType) {
            this.operType = operType;
        }

        public long getCasUnique() {
            return casUnique;
        }

        public void setCasUnique(long casUnique) {
            this.casUnique = casUnique;
        }

        @Override
        public int compareTo(LockerData o) {
            return key.compareTo(o.getKey());
        }
    }

}
