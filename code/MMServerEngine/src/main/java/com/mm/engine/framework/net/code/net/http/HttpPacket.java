package com.mm.engine.framework.net.code.net.http;

import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 * 从解码器返回给http入口的包
 */
public interface HttpPacket {
    public Map<String,String> getHeaders();
    public byte[] getData();
}
