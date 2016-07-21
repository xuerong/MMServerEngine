package com.mm.engine.framework.entrance.code.net;

import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 *
 * 通讯解码的返回值，
 * 通过通讯解码，得到一个整包的byte[]和对应的键值对
 * 其中byte[]是和具体游戏数据传输协议设计相关的数据包，而键值对可以为空
 */
public interface NetPacket {
    public Map<String,Object> getHeaders();
    public byte[] getData();
}
