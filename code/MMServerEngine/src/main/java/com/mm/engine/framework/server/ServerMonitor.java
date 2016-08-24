package com.mm.engine.framework.server;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.annotation.Updatable;
import com.mm.engine.framework.control.event.EventManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/12/30.
 *
 * 系统检测服务，用于对系统的所有服务运行状况进行检测
 */
@Service
public class ServerMonitor {
    private static final Logger log = LoggerFactory.getLogger(ServerMonitor.class);

    @Updatable(isAsynchronous = true,cycle = 6000)
    public void monitorUpdate(int interval){
//        log.info("monitorUpdate:"+interval);
        String state = EventManager.getMonitorData();
        if(!state.equals("ok")){
            log.error(state);
        }
    }
}
