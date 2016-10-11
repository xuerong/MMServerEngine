package com.mm.engine.framework.net.entrance.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventService;
import com.mm.engine.framework.net.entrance.Entrance;

import com.mm.engine.framework.net.code.netty.DefaultNettyDecoder;
import com.mm.engine.framework.net.code.netty.DefaultNettyEncoder;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import io.netty.channel.*;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/8/29.
 */
public class NetEventNettyEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(NetEventNettyEntrance.class);
    Channel channel = null;
    static NetEventService netEventService;

    @Override
    public void start() throws Exception {
        channel = NettyHelper.createAndStart(
                port,DefaultNettyEncoder.class,DefaultNettyDecoder.class,DiscardServerHandler.class,name);
        netEventService = BeanHelper.getServiceBean(NetEventService.class);
        log.info("NetEventNettyEntrance bind port :"+port);
    }

    public static class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
//        AttributeKey<String> sessionKey = AttributeKey.newInstance("sessionKey");
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            NetEventData netEventData = null;
            int id = -1;
            if (msg instanceof SocketPacket) {
                SocketPacket socketPacket = (SocketPacket) msg;
                if (socketPacket.getData() instanceof NetEventData) {
                    netEventData = (NetEventData) socketPacket.getData();
                }
                id = socketPacket.getId();
            } else if (msg instanceof NetEventData) {
                netEventData = (NetEventData) msg;
            }
            try {
                if (netEventData == null) {
                    throw new MMException("NetEventNettyEntrance 收到包错误 ：" + msg.getClass().getName());
                }
                NetEventData retPacket = netEventService.handleNetEventData(netEventData);
                if (id > 0) { // 需要返回的
                    SocketPacket socketPacket = new SocketPacket();
                    socketPacket.setId(id);
                    socketPacket.setData(retPacket);
                    ctx.writeAndFlush(socketPacket);
                }
            }catch (Throwable e){
                if(id > 0){
                    SocketPacket socketPacket = new SocketPacket();
                    socketPacket.setId(id);
                    NetEventData retPacket = new NetEventData(SysConstantDefine.NETEVENTEXCEPTION);
                    String errMsg = "net event exception";
                    if(e instanceof MMException){
                        MMException mmException = (MMException)e;
                        errMsg = mmException.getMessage();
                    }else{
                        errMsg = e.getMessage();
                    }
                    retPacket.setParam(errMsg);
                    socketPacket.setData(retPacket);
                    ctx.writeAndFlush(socketPacket);
                }
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
