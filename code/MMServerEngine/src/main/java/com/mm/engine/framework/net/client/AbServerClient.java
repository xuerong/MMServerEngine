package com.mm.engine.framework.net.client;

/**
 * Created by apple on 16-8-28.
 */
public abstract class AbServerClient implements ServerClient {
    protected int serverType;

    protected String host;
    protected int port;


    public int getServerType() {
        return serverType;
    }

    public void setServerType(int serverType) {
        this.serverType = serverType;
    }
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
}
