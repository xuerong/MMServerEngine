package com.mm.engine.framework.control.netEvent;

import java.io.Serializable;

/**
 * Created by a on 2016/8/31.
 */
public class ServerInfo implements Serializable {
    private String host;
    private int port;
    private int type;

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }
}
