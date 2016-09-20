package com.mm.engine.sysBean.entrance;

/**
 * Created by a on 2016/9/19.
 */
public class NettyPBPacket {
    private int opcode;
    private byte[] data;

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
