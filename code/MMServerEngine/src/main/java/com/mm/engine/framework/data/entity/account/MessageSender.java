package com.mm.engine.framework.data.entity.account;

/**
 * Created by apple on 16-10-4.
 */
public interface MessageSender {
    public void sendMessage(int opcode,byte[] data);
    public void sendMessageSync(int opcode,byte[] data);
}
