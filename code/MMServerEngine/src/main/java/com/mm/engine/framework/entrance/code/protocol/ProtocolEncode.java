package com.mm.engine.framework.entrance.code.protocol;

import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.data.entity.session.Session;

/**
 * Created by Administrator on 2015/11/16.
 */
public interface ProtocolEncode {
    public NetPacket encode(RetPacket packet, Session session);
}
