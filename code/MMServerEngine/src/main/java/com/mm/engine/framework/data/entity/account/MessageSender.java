package com.mm.engine.framework.data.entity.account;

import com.mm.engine.framework.data.entity.session.Session;

/**
 * Created by apple on 16-10-4.
 */
public interface MessageSender {
    public void sendMessage(int opcode,byte[] data) throws Throwable;
}
