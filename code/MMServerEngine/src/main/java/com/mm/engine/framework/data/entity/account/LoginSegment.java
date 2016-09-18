package com.mm.engine.framework.data.entity.account;

/**
 * Created by a on 2016/9/18.
 */
public class LoginSegment {
    private String host;
    private int port;
    private String sessionId;
    private Account account;

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

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }
}
