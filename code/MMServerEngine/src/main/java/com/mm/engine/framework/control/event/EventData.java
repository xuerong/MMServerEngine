package com.mm.engine.framework.control.event;

/**
 * Created by Administrator on 2015/11/18.
 */
public class EventData {
    private short event;

    public EventData(short event){
        this.event=event;
    }
    public EventData(int event){
        this.event=(short) event;
    }

    public short getEvent(){
        return event;
    }
}
