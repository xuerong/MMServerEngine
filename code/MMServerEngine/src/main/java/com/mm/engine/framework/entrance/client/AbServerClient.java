package com.mm.engine.framework.entrance.client;

/**
 * Created by apple on 16-8-28.
 */
public abstract class AbServerClient implements ServerClient {
    protected String host;
    protected int port;

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
