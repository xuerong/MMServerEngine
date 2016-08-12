package com.mm.engine.framework.entrance;

/**
 * Created by a on 2016/8/9.
 *
 * 网络访问的入口，在入口的消息到来请调用{NetFlowFire}相应的fire方法来处理消息
 */
public abstract class Entrance {

    protected String name;
    protected int port;

    public Entrance(){};
    public Entrance(String name,int port){
        this.name = name;
        this.port = port;
    }

    public abstract void start() throws Exception;

    public abstract void stop() throws Exception;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
