package com.mm.engine.framework.net.code.netty;

import com.mm.engine.framework.control.room.RoomNetData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * Created by a on 2016/8/29.
 * Netty的解码器和http的解码器不同，它要求每个客户端拥有一个实例
 */
public class RoomNettyDecoder extends ByteToMessageDecoder {
    private static final int headSize = 4;
    int size = headSize;
    boolean isReadHead = false;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {
        int readAble =in.readableBytes();
        if (readAble < size) {
            return;
        }
        if(!isReadHead) {
            size = in.readInt();
            isReadHead = true;
            if(size>readAble - headSize){
                return;
            }
        }
        ByteBuf b = in.readBytes(size); // 这里有data

        int sceneId =  b.readInt();
        int opcode = b.readInt();

        byte[]  bbb = new byte[size - 8];
        b.getBytes(0,bbb);

        RoomNetData roomNetData = new RoomNetData();
        roomNetData.setRoomId(sceneId);
        roomNetData.setOpcode(opcode);
        roomNetData.setData(bbb);

        // add之后好像in就被重置了
        list.add(roomNetData);
        // 清理临时变量
        size = headSize;
        isReadHead = false;
    }
}
