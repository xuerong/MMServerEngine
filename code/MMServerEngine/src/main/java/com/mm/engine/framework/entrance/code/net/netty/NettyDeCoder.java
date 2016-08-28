package com.mm.engine.framework.entrance.code.net.netty;

import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.server.SysConstantDefine;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.EmptyByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.AttributeKey;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.nio.charset.Charset;
import java.util.List;

/**
 * Created by apple on 16-8-27.
 */
public class NettyDeCoder extends ByteToMessageDecoder {
    private static final int headSize = 8;
    int size = headSize;
    int controllerSize = 0;
    boolean isReadHead = false;
    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> list) throws Exception {

        int readAble =in.readableBytes();
        System.out.println(readAble);
        if (readAble < size) {
            return;
        }
        if(!isReadHead) {
            int length1 = in.readInt();
            int length2 = in.readInt();
            size = length1+length2;
            controllerSize = length2;
            isReadHead = true;
            if(size>readAble - headSize){
                return;
            }
        }
        String controller = null;
        if(controllerSize > 0){
            controller = in.readCharSequence(controllerSize, Charset.forName(SysConstantDefine.utf_8)).toString();
        }
        ByteBuf b = in.readBytes(size - controllerSize); // 这里有data
        byte[]  bbb = new byte[size - controllerSize];
        b.getBytes(0,bbb);
//        ByteArrayInputStream bis = new ByteArrayInputStream(bbb);
//        ObjectInputStream oin = new ObjectInputStream(bis);
        NetPacket netPacket = new NetPacketImpl(bbb);
        if(controller != null){
            netPacket.put(SysConstantDefine.controller,controller);
        }
        list.add(netPacket); // add之后好像in就被重置了
        System.out.println("hahaha");
        // 清理临时变量
        size = headSize;
        controllerSize = 0;
        isReadHead = false;
    }
    public static void main(String[] args){

    }
}
