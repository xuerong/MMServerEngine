package com.mm.engine.framework.net.entrance.socket;

import com.mm.engine.framework.control.room.RoomNetData;
import com.mm.engine.framework.control.room.RoomService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.netty.*;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/9/14.
 */
public class RoomNettyEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(RoomNettyEntrance.class);
    Channel channel = null;
    static RoomService roomService;

    @Override
    public void start() throws Exception {
        channel = NettyHelper.createAndStart(
                port,RoomNettyEncoder.class,RoomNettyDecoder.class,DiscardServerHandler.class,name);
        roomService = BeanHelper.getServiceBean(RoomService.class);
        log.info("bind port :"+port);
    }
    public static class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            try {
                RoomNetData roomNetData = (RoomNetData) msg;
                RetPacket retPacket = roomService.handle(null, roomNetData.getRoomId(), roomNetData.getOpcode(), roomNetData.getData());
                roomNetData.setData((byte[]) retPacket.getRetData());
            }catch (Throwable e){
                throw new MMException(e);
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }
    @Override
    public void stop() throws Exception {

    }
}
