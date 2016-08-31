package com.mm.engine.framework.entrance.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.control.netEvent.NetEventManager;
import com.mm.engine.framework.entrance.Entrance;

import com.mm.engine.framework.entrance.code.net.netty.DefaultNettyDecoder;
import com.mm.engine.framework.entrance.code.net.netty.DefaultNettyEncoder;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.exception.MMException;
import io.netty.channel.*;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by a on 2016/8/29.
 */
public class NetEventNettyEntrance extends Entrance {
    Channel channel = null;

    public NetEventNettyEntrance(String name, int port){
        super(name,port);
    }

    @Override
    public void start() throws Exception {
        channel = NettyHelper.createAndStart(
                port,new DefaultNettyEncoder(),new DefaultNettyDecoder(),new DiscardServerHandler(),name);
        // 向mainServer取得连接
        NetEventManager.notifyConnMainServer();
    }

    private void fire(ChannelHandlerContext ctx, Object msg,String entranceName){
//        NetPacket netPacket = (NetPacket) msg; // 由于上层用编解码器处理了,所以这里获取和发送都是NetPacket即可
//        try {
//            if (netPacket == null) {
//                // net解码错误
//                System.out.println("net解码错误");
//            }
//            Channel channel = ctx.channel();
//            String sessionId = channel.attr(sessionKey).get();
//            if(sessionId!=null) {
//                netPacket.put(Session.sessionKey, sessionId);
//            }
//            SocketAddress remoteAddress = channel.remoteAddress();
//            String ip = "ipip";
//            String url = "url";
//            if(remoteAddress instanceof InetSocketAddress){
//                InetSocketAddress inetSocketAddress = (InetSocketAddress)remoteAddress;
//                ip = inetSocketAddress.getAddress().getHostAddress();
//                url = inetSocketAddress.getHostName();
//            }
//            String controller = (String)netPacket.get(SysConstantDefine.controller);
//            System.out.println("ip:"+ip+",url:"+url+",controller:"+controller);
//            NetPacket reNetPacket=ControllerDispatcher.handle(NetType.Netty,
//                    netPacket,url, ip);
//            channel.attr(sessionKey).set(reNetPacket.get(Session.sessionKey).toString());
//
//            ctx.write(reNetPacket); // (1)
//            ctx.flush(); // (2)
//        } catch (Throwable e){
//            e.printStackTrace();
//            throw new RuntimeException(entranceName+" Exception");
//        }finally {
////                msg.
////                ReferenceCountUtil.release(msg);
//        }
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
            NetEventData retPacket = NetEventManager.handle(netEventData);
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
