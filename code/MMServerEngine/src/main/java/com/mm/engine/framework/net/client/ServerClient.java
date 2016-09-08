package com.mm.engine.framework.net.client;

/**
 * Created by apple on 16-8-14.
 * 本节点与其它节点的链接对象
 */
public interface ServerClient {
    public void start() throws Exception;
    public Object send(Object msg);
    public void sendWithoutReply(Object msg);

}
