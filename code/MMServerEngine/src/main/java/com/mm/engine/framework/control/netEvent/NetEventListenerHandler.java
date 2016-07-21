package com.mm.engine.framework.control.netEvent;

import com.mm.engine.framework.entrance.code.protocol.RetPacket;

/**
 * Created by Administrator on 2015/11/18.
 */
public interface NetEventListenerHandler {
    public RetPacket handle(NetEventData netEventData);
}
