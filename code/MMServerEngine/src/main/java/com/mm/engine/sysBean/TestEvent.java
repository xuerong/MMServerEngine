package com.mm.engine.sysBean;

import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.netEvent.NetEventData;

/**
 * Created by Administrator on 2015/11/20.
 */
@Service
public class TestEvent {
    @EventListener(event = 100)
    public void eventListener1(EventData eventData){
        System.out.println("eventData1:"+eventData.getEvent());
    }
    @EventListener(event = 100)
    public void eventListener2(EventData eventData){
        System.out.println("eventData2:"+eventData.getEvent());
    }
    @NetEventListener(netEvent = 100)
    public NetEventData netEventListener(NetEventData netEventData){
        return null;
    }
}
