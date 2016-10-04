package com.mm.engine.sysBean.service;

import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.sysBean.entrance.NettyPBPacket;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by apple on 16-10-4.
 */
public class NettyPBMessageSender implements MessageSender{
    private static final Logger log = LoggerFactory.getLogger(NettyPBMessageSender.class);
    private Channel channel;

    public NettyPBMessageSender(Channel channel){
        this.channel = channel;
    }

    @Override
    public void sendMessage(int opcode, byte[] data) throws Throwable{
        NettyPBPacket nettyPBPacket = new NettyPBPacket();
        nettyPBPacket.setData(data);
        nettyPBPacket.setOpcode(opcode);
        channel.writeAndFlush(nettyPBPacket);
    }
}
