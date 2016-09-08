package com.mm.engine.framework.net.code.net;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/16.
 */
public class NetPacketImpl implements NetPacket {
    private Map<String,Object> headers;
    private byte[] data;
    public  NetPacketImpl(Map<String,Object> headers,byte[] data){
        this.headers=headers;
        this.data=data;
    }
    public NetPacketImpl(byte[] data){
        this.headers=null;
        this.data=data;
    }
    @Override
    public void put(String key,Object value){
        if(headers==null){
            headers=new HashMap<String,Object>();
        }
        headers.put(key,value);
    }

    @Override
    public Object get(String key) {
        if(headers == null){
            return null;
        }
        return headers.get(key);
    }

    @Override
    public Map<String,Object> getHeaders() {
        return headers;
    }

    @Override
    public byte[] getData() {
        return data;
    }
}
