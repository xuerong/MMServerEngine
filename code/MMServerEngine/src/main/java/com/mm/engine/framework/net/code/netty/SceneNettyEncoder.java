package com.mm.engine.framework.net.code.netty;

import com.mm.engine.framework.control.scene.SceneNetData;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * Created by a on 2016/8/29.
 * Netty的编码器和http的解码器不同，它要求每个客户端拥有一个实例
 */
public class SceneNettyEncoder extends MessageToByteEncoder {
    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object o, ByteBuf byteBuf) throws Exception {
        SceneNetData sceneNetData = (SceneNetData)o;
        byte[] nb = (byte[])sceneNetData.getData();
        byteBuf.writeInt(nb.length+8);
        byteBuf.writeInt(sceneNetData.getSceneId());
        byteBuf.writeInt(sceneNetData.getOpcode());
        byteBuf.writeBytes(nb);
    }
}
