package com.mm.engine.framework.control.room;

/**
 * Created by a on 2016/10/21.
 */
public interface RoomMessageSender {
    public void sendMessage(int opcode,int roomId,byte[] data);
    public void sendMessageSync(int opcode,int roomId,byte[] data);
}
