package com.mm.engine.framework.security;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.annotation.Updatable;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/12/30.
 * TODO 这个要好好的实现一下
 * 系统检测服务，用于对系统的所有服务运行状况进行检测
 *
 * 这里要提供如下服务：
 * 1 启动时，判断哪些启动时必须满足的条件，即启动完成的判断，如NetEventService端口尚未启动
 * 2 当某些极端事件发生，如mainServer失联，则停止服务器的正常运行状态
 * 3 关闭服务器的时候，判断所有正常关闭的条件
 * 4 平时监控并提示服务器的状态，如负载，瓶颈，网络状态等
 */
@Service(init = "init")
public class MonitorService {
    private static final Logger log = LoggerFactory.getLogger(MonitorService.class);

    private Map<String,String> conditions = new HashMap<>();

    private EventService eventService;

    public void init(){
        eventService = BeanHelper.getServiceBean(EventService.class);
    }

    @Updatable(isAsynchronous = true,cycle = 6000)
    public void monitorUpdate(int interval){
//        log.info("monitorUpdate:"+interval);
        String state = eventService.getMonitorData();
        if(!state.equals("ok")){
            log.error(state);
        }
        if(conditions.size()>0){
            for(Map.Entry<String,String> entry : conditions.entrySet()){
                log.warn(entry.getKey()+":"+entry.getValue());
            }
        }
    }

    public synchronized void addStartCondition(String key,String describe){
        conditions.put(key,describe);
    }
    public synchronized void removeStartCondition(String key){
        conditions.remove(key);
        notify();
    }

    /**
     * 等待直到服务器启动完成
     */
    public synchronized void startWait(){
        while (conditions.size() > 0){
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }



    public void stopWait(){

    }
}
