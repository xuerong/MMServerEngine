package com.mm.engine.framework.control.netEvent;

import java.io.Serializable;

/**
 * Created by a on 2016/8/31.
 */
public class ServerInfo implements Serializable {
    private String host;
    private int netEventPort;
    private int requestPort;
    private int scenePort;
    private int type;

    public ServerInfo(){

    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getNetEventPort() {
        return netEventPort;
    }

    public void setNetEventPort(int netEventPort) {
        this.netEventPort = netEventPort;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getRequestPort() {
        return requestPort;
    }

    public void setRequestPort(int requestPort) {
        this.requestPort = requestPort;
    }

    public int getScenePort() {
        return scenePort;
    }

    public void setScenePort(int scenePort) {
        this.scenePort = scenePort;
    }
}
