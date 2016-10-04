package com.mm.engine.sysBean.entrance;

import com.mm.engine.framework.control.request.RequestService;
import com.mm.engine.framework.data.entity.account.AccountSysService;
import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.net.entrance.socket.NettyHelper;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.sysBean.service.NettyPBMessageSender;
import com.protocol.AccountOpcode;
import com.protocol.AccountPB;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/9/19.
 */
public class RequestNettyPBEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(RequestNettyPBEntrance.class);

    Channel channel = null;
    private static AccountSysService accountSysService;
    private static SessionService sessionService;
    private static RequestService requestService;
    @Override
    public void start() throws Exception {
        accountSysService = BeanHelper.getServiceBean(AccountSysService.class);
        sessionService = BeanHelper.getServiceBean(SessionService.class);
        requestService = BeanHelper.getServiceBean(RequestService.class);

        channel = NettyHelper.createAndStart(
                port,RequestNettyPBEncoder.class,RequestNettyPBDecoder.class,RequestNettyPBHandler.class,name);
        log.info("RequestNettyPBEntrance bind port :"+port);
    }

    @Override
    public void stop() throws Exception {

    }

    public static class RequestNettyPBHandler extends ChannelInboundHandlerAdapter {
        static AttributeKey<String> sessionKey = AttributeKey.newInstance("sessionKey");
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception { // (1)
            super.channelActive(ctx);
        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            String sessionId = ctx.channel().attr(sessionKey).get();
            if(sessionId != null) {
                accountSysService.netDisconnect(sessionId);
            }else{
                log.error("channelInactive , but session = "+sessionId);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            try {
                NettyPBPacket nettyPBPacket = (NettyPBPacket) msg;
                String sessionId = ctx.channel().attr(sessionKey).get();
                if (nettyPBPacket.getOpcode() == AccountOpcode.CSLoginNode) { // 登陆消息
                    AccountPB.CSLoginNode csLoginNode = AccountPB.CSLoginNode.parseFrom(nettyPBPacket.getData());
                    sessionId = csLoginNode.getSessionId();
                    ctx.channel().attr(sessionKey).set(sessionId);
                    // 推送消息用的工具
                    Session session = checkAndGetSession(sessionId);
                    MessageSender messageSender = new NettyPBMessageSender(ctx.channel());
                    session.setMessageSender(messageSender);
                }
                Session session = checkAndGetSession(sessionId);
                RetPacket retPacket = requestService.handle(nettyPBPacket.getOpcode(),nettyPBPacket.getData(),session);
                if(retPacket == null){
                    throw new MMException("server error!");
                }
                nettyPBPacket.setOpcode(retPacket.getOpcode());
                nettyPBPacket.setData((byte[])retPacket.getRetData());
                ctx.writeAndFlush(nettyPBPacket);
            }catch (Exception e){
                if(e instanceof MMException){

                }
                throw new MMException(e);
            }
        }
        private Session checkAndGetSession(String sessionId){
            if (sessionId == null || sessionId.length() == 0){
                throw new MMException("won't get sessionId while loginNode:"+sessionId);
            }
            // 不是login，可以处理消息
            Session session = sessionService.get(sessionId);
            if(session == null){
                throw new MMException("login timeout , please login again");
            }
            return session;
        }


        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }
}
