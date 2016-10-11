package com.mm.engine.framework.control.room;

/**
 * Created by a on 2016/9/14.
 */
public class RoomNetData {
    private int roomId;
    private int opcode;
    private Object data;

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public int getOpcode() {
        return opcode;
    }

    public void setOpcode(int opcode) {
        this.opcode = opcode;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
