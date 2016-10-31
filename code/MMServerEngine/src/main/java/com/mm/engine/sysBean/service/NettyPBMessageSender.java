package com.mm.engine.sysBean.service;

import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.sysBean.entrance.NettyPBPacket;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * Created by apple on 16-10-4.
 */
public class NettyPBMessageSender implements MessageSender{
    private static final Logger log = LoggerFactory.getLogger(NettyPBMessageSender.class);
    private static final ScheduledExecutorService asyncExecutor = new ScheduledThreadPoolExecutor(100, new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // 拒绝执行处理

        }
    }){
        protected void afterExecute(Runnable r, Throwable t) {
            // 执行后处理，注意异常的处理
        }
    };
    private Channel channel;

    public NettyPBMessageSender(Channel channel){
        this.channel = channel;
    }

    @Override
    public void sendMessage(final int opcode, final byte[] data){
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMessageSync(opcode,data);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        });
    }
    @Override
    public void sendMessageSync(int opcode,byte[] data){
        NettyPBPacket nettyPBPacket = new NettyPBPacket();
        nettyPBPacket.setId(-1); // 没有的时候为-1
        nettyPBPacket.setData(data);
        nettyPBPacket.setOpcode(opcode);
        channel.writeAndFlush(nettyPBPacket);
    }
}
