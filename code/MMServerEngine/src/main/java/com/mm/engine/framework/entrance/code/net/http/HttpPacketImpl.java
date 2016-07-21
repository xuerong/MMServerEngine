package com.mm.engine.framework.entrance.code.net.http;

import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 */
public class HttpPacketImpl implements HttpPacket{
    private byte[] data;
    private Map<String,String> headers;
    public HttpPacketImpl(Map<String,String> headers,byte[] data){
        this.data=data;
        this.headers=headers;
    }
    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public Map<String, String> getHeaders() {
        return headers;
    }
}
