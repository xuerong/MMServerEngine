package com.mm.engine.framework.server;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.netEvent.remote.RemoteCallService;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by a on 2016/9/14.
 * id服务，为类的对象生成唯一
 * TODO id服务的持久化
 * id服务提供两种就可:int和long
 * 
 * ,runOnEveryServer = false
 */
@Service(init = "init",runOnEveryServer = false)
public class IdService {

    private RemoteCallService remoteCallService;

    private ConcurrentHashMap<Class,IdSegment> intIdSegmentMap;
    private ConcurrentHashMap<Class,IdSegment> longIdSegmentMap;

    public void init(){
        remoteCallService = BeanHelper.getServiceBean(RemoteCallService.class);
        //TODO 从数据库中载入当前各个id状态
        intIdSegmentMap = new ConcurrentHashMap<>();
        longIdSegmentMap = new ConcurrentHashMap<>();
    }

    public int acquireInt(Class<?> cls){
        IdSegment IdSegment = intIdSegmentMap.get(cls);
        if(IdSegment == null){
            IdSegment = new IdSegment(cls);
            intIdSegmentMap.putIfAbsent(cls, IdSegment);
            IdSegment = intIdSegmentMap.get(cls);
        }
        return IdSegment.acquire();
    }

    public void releaseInt(Class<?> cls, int id){
        IdSegment IdSegment = intIdSegmentMap.get(cls);
        if(IdSegment == null){
            throw new MMException("IdSegment is not exist,cls = "+cls.getName());
        }
        IdSegment.release(id);
    }

    class IdSegment{
        private Class cls;
        private Set<Integer> usingIds;
        private Queue<Integer> canUseIds;
        private AtomicInteger idMark;

        public IdSegment(Class cls){
            this.cls = cls;
            this.usingIds = new ConcurrentHashSet<>();
            this.canUseIds = new ConcurrentLinkedDeque<>();
            this.idMark = new AtomicInteger();

        }

        public int acquire(){
            Integer id = canUseIds.poll();
            if(id == null){
                id = idMark.getAndIncrement();
                usingIds.add(id);
            }
            return id;
        }

        public void release(Integer id){
            usingIds.remove(id);
            canUseIds.offer(id);
        }


        public Class getCls() {
            return cls;
        }

        public void setCls(Class cls) {
            this.cls = cls;
        }

        public Set<Integer> getUsingIds() {
            return usingIds;
        }

        public Queue<Integer> getCanUseIds() {
            return canUseIds;
        }

        public Number getIdMark() {
            return idMark;
        }
    }
}
