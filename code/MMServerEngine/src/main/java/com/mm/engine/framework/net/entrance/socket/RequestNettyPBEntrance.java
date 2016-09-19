package com.mm.engine.framework.net.entrance.socket;

import com.mm.engine.framework.control.request.RequestService;
import com.mm.engine.framework.data.entity.account.AccountService;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.netty.*;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
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
    private static AccountService accountService;
    private static SessionService sessionService;
    private static RequestService requestService;
    @Override
    public void start() throws Exception {
        accountService = BeanHelper.getServiceBean(AccountService.class);
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
        AttributeKey<String> sessionKey = AttributeKey.newInstance("sessionKey");
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            try {
                NettyPBPacket nettyPBPacket = (NettyPBPacket) msg;
                String sessionId = ctx.channel().attr(sessionKey).get();
                if (nettyPBPacket.getOpcode() == SysConstantDefine.loginNodeOpcode) { // 必须是登陆消息
                    Account_PB.CSLoginNode csLoginNode = Account_PB.CSLoginNode.parseFrom(nettyPBPacket.getData());
                    accountService.loginNodeServer(csLoginNode.getAccountId(),csLoginNode.getSessionId());
                    sessionId = csLoginNode.getSessionId();
                    ctx.channel().attr(sessionKey).set(sessionId);
                }else if (sessionId == null || sessionId.length() == 0){
                    throw new MMException("won't login");
                }else{
                    // 不是login，可以处理消息
                    Session session = sessionService.get(sessionId);
                    if(session == null){
                        throw new MMException("login timeout , please login again");
                    }
                    RetPacket retPacket = requestService.handle(nettyPBPacket.getOpcode(),nettyPBPacket.getData(),session);
                    if(retPacket == null){
                        throw new MMException("server error!");
                    }
                    nettyPBPacket.setOpcode(retPacket.getOpcode());
                    nettyPBPacket.setData((byte[])retPacket.getRetData());
                    ctx.writeAndFlush(nettyPBPacket);
                }
            }catch (Exception e){
                if(e instanceof MMException){

                }
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
}
