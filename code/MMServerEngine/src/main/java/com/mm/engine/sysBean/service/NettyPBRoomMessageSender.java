package com.mm.engine.sysBean.service;

import com.mm.engine.framework.control.room.RoomMessageSender;
import com.mm.engine.framework.control.room.RoomNetData;
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
public class NettyPBRoomMessageSender implements RoomMessageSender{
    private static final Logger log = LoggerFactory.getLogger(NettyPBRoomMessageSender.class);
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

    public NettyPBRoomMessageSender(Channel channel){
        this.channel = channel;
    }

    @Override
    public void sendMessage(final int opcode, final int roomId, final byte[] data) {
        asyncExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sendMessageSync(opcode,roomId,data);
                }catch (Throwable e){
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void sendMessageSync(int opcode, int roomId, byte[] data) {
        RoomNetData roomNetData = new RoomNetData();
        roomNetData.setId(-1); // 没有的时候为-1
        roomNetData.setOpcode(opcode);
        roomNetData.setRoomId(roomId);
        roomNetData.setData(data);
        channel.writeAndFlush(roomNetData);
    }
}
