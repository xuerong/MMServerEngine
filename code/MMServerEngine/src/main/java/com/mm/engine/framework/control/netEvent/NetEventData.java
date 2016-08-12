package com.mm.engine.framework.control.netEvent;

/**
 * Created by Administrator on 2015/12/30.
 */
public class NetEventData {
    private int netEvent;
    private Object param;

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
