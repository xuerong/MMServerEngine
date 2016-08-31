package com.mm.engine.framework.control.netEvent;

import java.io.Serializable;

/**
 * Created by Administrator on 2015/12/30.
 */
public class NetEventData implements Serializable{
    private int netEvent;
    private Object param;
    public NetEventData(int netEvent,Object param){
        this.netEvent = netEvent;
        this.param = param;
    }
    public NetEventData(int netEvent){
        this.netEvent=netEvent;
    }

    public int getNetEvent(){
        return netEvent;
    }

    public void setNetEvent(int netEvent) {
        this.netEvent = netEvent;
    }

    public Object getParam() {
        return param;
    }

    public void setParam(Object param) {
        this.param = param;
    }
}
