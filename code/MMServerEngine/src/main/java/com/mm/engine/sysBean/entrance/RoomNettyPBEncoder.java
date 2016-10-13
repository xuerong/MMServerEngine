package com.mm.engine.sysBean.entrance;

import com.mm.engine.framework.control.room.RoomNetData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by a on 2016/9/19.
 * pbsize-opcode-id-roomid-data
 */
public class RoomNettyPBEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object object, ByteBuf byteBuf) throws Exception {
        RoomNetData roomNetData = (RoomNetData)object;
        byteBuf.writeInt(roomNetData.getData().length);
        byteBuf.writeInt(roomNetData.getOpcode());
        byteBuf.writeInt(roomNetData.getId());
        byteBuf.writeInt(roomNetData.getRoomId());
        byteBuf.writeBytes(roomNetData.getData());
    }
}
