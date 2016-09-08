package com.mm.engine.framework.net.entrance.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventService;
import com.mm.engine.framework.net.entrance.Entrance;

import com.mm.engine.framework.net.code.net.netty.DefaultNettyDecoder;
import com.mm.engine.framework.net.code.net.netty.DefaultNettyEncoder;
import com.mm.engine.framework.security.exception.MMException;
import io.netty.channel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/8/29.
 */
public class NetEventNettyEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(NetEventNettyEntrance.class);
    Channel channel = null;
    NetEventService netEventService;
    public NetEventNettyEntrance(){}
    public NetEventNettyEntrance(String name, int port){
        super(name,port);
    }

//    @AspectMark(mark = {"EntranceStart"})
    @Override
    public void start() throws Exception {
        channel = NettyHelper.createAndStart(
                port,DefaultNettyEncoder.class,DefaultNettyDecoder.class,DiscardServerHandler.class,name);
        log.info("bind port :"+port);
        // 向mainServer取得连接 :这个通过aop和事件做了，和具体的入口隔离
//        netEventService = BeanHelper.getServiceBean(NetEventService.class);
//        netEventService.notifyConnMainServer();
    }

    public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            NetEventData netEventData = null;
            int id = -1;
            if(msg instanceof SocketPacket){
                SocketPacket socketPacket = (SocketPacket)msg;
                if(socketPacket.getData() instanceof NetEventData){
                    netEventData = (NetEventData)socketPacket.getData();
                }
                id = socketPacket.getId();
            }else if(msg instanceof NetEventData){
                netEventData = (NetEventData)msg;
            }
            if(netEventData == null){
                throw new MMException("NetEventNettyEntrance 收到包错误 ："+msg.getClass().getName());
            }
            NetEventData retPacket = netEventService.handle(netEventData);
            if(id>0){ // 需要返回的
                SocketPacket socketPacket = new SocketPacket();
                socketPacket.setId(id);
                socketPacket.setData(retPacket);
                ctx.writeAndFlush(socketPacket);
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
