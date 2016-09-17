package com.mm.engine.framework.net.code.netty;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by a on 2016/8/29.
 * Netty的编码器和http的解码器不同，它要求每个客户端拥有一个实例
 */
public class DefaultNettyEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf){
        try {
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(o);
            out.flush();
            out.close(); // 这个是否能重复用？
            byte[] nb = bos.toByteArray();
            byteBuf.writeInt(nb.length);
            byteBuf.writeBytes(nb);
        }catch (Throwable e){
            e.printStackTrace();
        }
    }
}
