package com.mm.engine.framework.entrance.code.net.netty;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.server.SysConstantDefine;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;

/**
 * Created by apple on 16-8-27.
 */
public class NettyEnCoder extends MessageToByteEncoder<NetPacket> {
    @Override
    protected void encode(ChannelHandlerContext ctx, NetPacket netPacket, ByteBuf byteBuf) throws Exception {
        byte[] nb = netPacket.getData();
        byteBuf.writeInt(nb.length);
        byteBuf.writeInt(0);
        String controller = (String)netPacket.get(SysConstantDefine.controller);
        int controllerLength = 0;
        if(controller != null && controller.length() > 0){
            controllerLength = byteBuf.writeCharSequence(controller, Charset.forName(SysConstantDefine.utf_8));
        }
        byteBuf.writeBytes(nb);
        if(controllerLength > 0){
            byteBuf.writerIndex(4);
            byteBuf.writeInt(controllerLength);
            byteBuf.writerIndex(8+controllerLength+nb.length); // 最后还要定位回去
        }
    }
}
