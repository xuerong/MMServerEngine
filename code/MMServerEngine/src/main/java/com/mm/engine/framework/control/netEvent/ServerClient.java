package com.mm.engine.framework.control.netEvent;

/**
 * Created by apple on 16-8-14.
 * 本节点与其它节点的链接对象
 */
public class ServerClient {
    private String serverIp;
    private String serverPort;

    public String getServerIp() {
        return serverIp;
    }

    public void setServerIp(String serverIp) {
        this.serverIp = serverIp;
    }

    public String getServerPort() {
        return serverPort;
    }

    public void setServerPort(String serverPort) {
        this.serverPort = serverPort;
    }
}
