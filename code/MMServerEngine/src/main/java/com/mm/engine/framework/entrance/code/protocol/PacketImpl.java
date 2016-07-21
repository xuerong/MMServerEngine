package com.mm.engine.framework.entrance.code.protocol;


/**
 * Created by Administrator on 2015/11/16.
 * 玩家packet
 */
public final class PacketImpl implements Packet {
    private String sessionId;
    private int opcode;
    private Object value;

    public PacketImpl(String sessionId, int opcode, Object value){
        this.sessionId=sessionId;
        this.opcode=opcode;
        this.value=value;
    }

    @Override
    public String getSessionId() {
        return sessionId;
    }

    @Override
    public int getOpcode() {
        return opcode;
    }

    @Override
    public Object getValue() {
        return value;
    }
}
