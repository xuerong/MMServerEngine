package com.mm.engine.framework.server;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.security.exception.MMException;
import org.eclipse.jetty.util.ConcurrentHashSet;

import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by a on 2016/9/14.
 * id服务，为类的对象生成唯一
 */
@Service(init = "init")
public class IdService {

    private Map<Class,IdSegment> idSegmentMap;


    public void init(){
        //TODO 从数据库中载入当前各个id状态
        idSegmentMap = new ConcurrentHashMap<>();
    }

    public int acquire(Class<?> cls){
        IdSegment idSegment = idSegmentMap.get(cls);
        if(idSegment == null){
            idSegment = new IdSegment(cls);
            idSegmentMap.putIfAbsent(cls,idSegment);
            idSegment = idSegmentMap.get(cls);
        }
        return idSegment.acquire();
    }

    public void release(Class<?> cls,int id){
        IdSegment idSegment = idSegmentMap.get(cls);
        if(idSegment == null){
            throw new MMException("idSegment is not exist,cls = "+cls.getName());
        }
        idSegment.release(id);
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
            this.idMark = new AtomicInteger(0);
        }

        public int acquire(){
            Integer id = canUseIds.poll();
            if(id == null){
                id = idMark.incrementAndGet();
                usingIds.add(id);
            }
            return id;
        }

        public void release(int id){
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

        public void setUsingIds(Set<Integer> usingIds) {
            this.usingIds = usingIds;
        }

        public Queue<Integer> getCanUseIds() {
            return canUseIds;
        }

        public void setCanUseIds(Queue<Integer> canUseIds) {
            this.canUseIds = canUseIds;
        }

        public AtomicInteger getIdMark() {
            return idMark;
        }

        public void setIdMark(AtomicInteger idMark) {
            this.idMark = idMark;
        }
    }
}
