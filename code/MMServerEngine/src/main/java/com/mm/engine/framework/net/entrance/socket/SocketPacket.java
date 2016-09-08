package com.mm.engine.framework.net.entrance.socket;

import java.io.Serializable;
import java.util.concurrent.CountDownLatch;

/**
 * Created by a on 2016/8/30.
 * id是一个表示服，如果是需要返回的请求，要把这个id返回作为消息的标识
 */
public class SocketPacket implements Serializable{
    private int id;
    private Object data;
    private transient CountDownLatch latch;
    private transient Object reData;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public CountDownLatch getLatch() {
        return latch;
    }

    public void setLatch(CountDownLatch latch) {
        this.latch = latch;
    }

    public Object getReData() {
        return reData;
    }

    public void setReData(Object reData) {
        this.reData = reData;
    }
}
