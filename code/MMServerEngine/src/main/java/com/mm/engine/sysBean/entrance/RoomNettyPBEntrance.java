package com.mm.engine.sysBean.entrance;

import com.mm.engine.framework.control.room.Room;
import com.mm.engine.framework.control.room.RoomMessageSender;
import com.mm.engine.framework.control.room.RoomNetData;
import com.mm.engine.framework.control.room.RoomService;
import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.net.entrance.socket.NettyHelper;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.security.exception.ToClientException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import com.mm.engine.sysBean.service.NettyPBMessageSender;
import com.mm.engine.sysBean.service.NettyPBRoomMessageSender;
import com.protocol.*;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/10/12.
 */
public class RoomNettyPBEntrance  extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(RequestNettyPBEntrance.class);

    Channel channel = null;
    static RoomService roomService;
    private static SessionService sessionService;
    @Override
    public void start() throws Exception {
        roomService = BeanHelper.getServiceBean(RoomService.class);
        sessionService = BeanHelper.getServiceBean(SessionService.class);

        channel = NettyHelper.createAndStart(
                port,RoomNettyPBEncoder.class,RoomNettyPBDecoder.class,RoomNettyPBHandler.class,name);
        log.info("RequestNettyPBEntrance bind port :"+port);
    }

    @Override
    public void stop() throws Exception {

    }

    public static class RoomNettyPBHandler extends ChannelInboundHandlerAdapter {
        static AttributeKey<String> sessionKey = AttributeKey.newInstance("roomSessionKey");
        @Override
        public void channelActive(final ChannelHandlerContext ctx) throws Exception { // (1)
            super.channelActive(ctx);
        }
        @Override
        public void channelInactive(ChannelHandlerContext ctx) throws Exception {
            super.channelInactive(ctx);
            String sessionId = ctx.channel().attr(sessionKey).get();
            if(sessionId != null) {
                Session session = sessionService.get(sessionId);
                if(session == null){
                    log.error("channelInactive , but session=null, sessionId = "+sessionId);
                }else {
                    roomService.netDisconnect(session);
                }
            }else{
                log.error("channelInactive , but sessionId = "+sessionId);
            }
        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            RoomNetData roomNetData = (RoomNetData) msg;
            try {
                Room room = roomService.getRoom(roomNetData.getRoomId());
                if(room == null){
                    // 连接如何处理？ session如何处理?
                    throw new MMException("room is not exist");
                }
                String sessionId = ctx.channel().attr(sessionKey).get();
                if (roomNetData.getOpcode() == RoomOpcode.CSEnterRoom) {
                    // 进入房间不一定创建session，因为如果已经有了session
                    RoomPB.CSEnterRoom csEnterRoom = RoomPB.CSEnterRoom.parseFrom(roomNetData.getData());
                    Session session =null;
                    if(sessionId != null && sessionId.length()>0){
                        session = sessionService.get(sessionId);
                    }
                    if(session == null){ // 不存在session
                        System.out.println(ctx.channel().remoteAddress().getClass()+","+ctx.channel().remoteAddress().toString());
                        session = sessionService.create(Util.getHostAddress(),ctx.channel().remoteAddress().toString());
                        ctx.channel().attr(sessionKey).set(session.getSessionId());
                        session.setAccountId(csEnterRoom.getAccountId());
                        RoomMessageSender messageSender = new NettyPBRoomMessageSender(ctx.channel());
                        session.setRoomMessageSender(messageSender);
                    }
                    roomService.enterRoom(roomNetData.getRoomId(),session);
                    RoomPB.SCEnterRoom.Builder builder = RoomPB.SCEnterRoom.newBuilder();
                    roomNetData.setOpcode(RoomOpcode.SCEnterRoom);
                    roomNetData.setData(builder.build().toByteArray());
                    ctx.writeAndFlush(roomNetData);
                }else if(roomNetData.getOpcode() == RoomOpcode.CSOutRoom){
                    Session session =checkAndGetSession(sessionId);
                    roomService.outRoom(roomNetData.getRoomId(),session);
                    RoomPB.SCOutRoom.Builder builder = RoomPB.SCOutRoom.newBuilder();
                    roomNetData.setOpcode(RoomOpcode.SCOutRoom);
                    roomNetData.setData(builder.build().toByteArray());
                    ctx.writeAndFlush(roomNetData);
                }else{
                    Session session = checkAndGetSession(sessionId);
                    RetPacket retPacket = roomService.handle(session,roomNetData.getRoomId(),roomNetData.getOpcode(),roomNetData.getData());
                    if(retPacket == null){
                        throw new MMException("server error!");
                    }
                    roomNetData.setOpcode(retPacket.getOpcode());
                    roomNetData.setData((byte[])retPacket.getRetData());
                    ctx.writeAndFlush(roomNetData);
                }

            }catch (Throwable e){
                int errCode = -1000;
                String errMsg = "系统异常";
                if(e instanceof MMException){
                    MMException mmException = (MMException)e;
                    log.error("MMException:"+mmException.getMessage());
                }else if(e instanceof ToClientException){
                    ToClientException toClientException = (ToClientException)e;
                    errCode = toClientException.getErrCode();
                    errMsg = toClientException.getMessage();
                    log.error("ToClientException:"+toClientException.getMessage());
                }else{
                    e.printStackTrace();
                }
                BasePB.SCException.Builder scException = BasePB.SCException.newBuilder();
                scException.setErrCode(errCode);
                scException.setErrMsg(errMsg);
                roomNetData.setOpcode(BaseOpcode.SCException);
                roomNetData.setData(scException.build().toByteArray());
                ctx.writeAndFlush(roomNetData);
            }
        }
        private Session checkAndGetSession(String sessionId){
            if (sessionId == null || sessionId.length() == 0){
                throw new MMException("won't get sessionId :"+sessionId);
            }
            // 不是login，可以处理消息
            Session session = sessionService.get(sessionId);
            if(session == null){
                throw new MMException("login timeout , please login again,sessionId="+sessionId);
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
