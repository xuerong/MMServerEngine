package com.mm.engine.framework.entrance.code.protocol;

import com.mm.engine.framework.entrance.code.net.NetPacket;

/**
 * Created by Administrator on 2015/11/16.
 *
 */
public interface ProtocolDecode {
    /**
     * 失败可返回null
     * 但建议自定义失败返回包，包括失败所用的opcode
     * **/
    public Packet decode(NetPacket netPacket);
}
