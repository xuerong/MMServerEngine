package com.mm.engine.sysBean.entrance;

import com.mm.engine.framework.control.room.RoomNetData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by a on 2016/9/19.
 * pbsize-opcode-id-roomid-data
 */
public class RoomNettyPBDecoder extends ByteToMessageDecoder {
    private static final int headSize = 16; // body length + opcode
    int size = headSize;
    boolean isReadHead = false;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        int readAble =in.readableBytes();
        if (readAble < size) {
            return;
        }
        int opcode = 0,id = 0,roomId = 0;
        if(!isReadHead) {
            size = in.readInt();
            opcode = in.readInt();
            id = in.readInt();
            roomId = in.readInt();

            isReadHead = true;
            if(size>readAble - headSize){
                return;
            }
        }
        ByteBuf b = in.readBytes(size); // 这里有data
        byte[]  bbb = new byte[size];
        b.getBytes(0,bbb);
        // add之后好像in就被重置了
        RoomNetData roomNetData = new RoomNetData();
        roomNetData.setId(id);
        roomNetData.setOpcode(opcode);
        roomNetData.setRoomId(roomId);
        roomNetData.setData(bbb);
        list.add(roomNetData);
        // 清理临时变量
        size = headSize;
        isReadHead = false;
    }
}
