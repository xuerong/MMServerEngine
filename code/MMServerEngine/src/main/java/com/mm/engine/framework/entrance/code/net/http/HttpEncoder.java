package com.mm.engine.framework.entrance.code.net.http;

import com.mm.engine.framework.entrance.code.net.NetEncode;
import com.mm.engine.framework.entrance.code.net.NetPacket;

import java.io.IOException;

/**
 * Created by Administrator on 2015/11/13.
 * // 注意，此httpcode是net中的解析，建立的是packet中的数据和httpPacket中的对应关系
 */
public interface HttpEncoder extends NetEncode{
    public HttpPacket encode(NetPacket packet) throws IOException;
}
