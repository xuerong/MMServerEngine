package com.mm.engine.framework.entrance;

import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.entrance.code.net.http.HttpPacket;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.AttributeKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.Map;

/**
 * Created by a on 2016/8/9.
 */
public class NetFlowFire {
    private static final Logger log = LoggerFactory.getLogger(NetFlowFire.class);

    private static final AttributeKey<String> sessionKey = AttributeKey.valueOf("mmsession");

    public static void fireNetty(ChannelHandlerContext ctx, Object msg,String entranceName){
        NetPacket netPacket = (NetPacket) msg; // 由于上层用编解码器处理了,所以这里获取和发送都是NetPacket即可
        try {
            if (netPacket == null) {
                // net解码错误
                System.out.println("net解码错误");
            }
            Channel channel = ctx.channel();
            String sessionId = channel.attr(sessionKey).get();
            if(sessionId!=null) {
                netPacket.put(Session.sessionKey, sessionId);
            }
            SocketAddress remoteAddress = channel.remoteAddress();
            String ip = "ipip";
            String url = "url";
            if(remoteAddress instanceof InetSocketAddress){
                InetSocketAddress inetSocketAddress = (InetSocketAddress)remoteAddress;
                ip = inetSocketAddress.getAddress().getHostAddress();
                url = inetSocketAddress.getHostName();
            }
            String controller = (String)netPacket.get(SysConstantDefine.controller);
            System.out.println("ip:"+ip+",url:"+url+",controller:"+controller);
            NetPacket reNetPacket=ControllerDispatcher.handle(NetType.Netty,
                    netPacket,url, ip);
            channel.attr(sessionKey).set(reNetPacket.get(Session.sessionKey).toString());

            ctx.write(reNetPacket); // (1)
            ctx.flush(); // (2)
        } catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(entranceName+" Exception");
        }finally {
//                msg.
//                ReferenceCountUtil.release(msg);
        }
    }
    public static void fireHttp(HttpServletRequest request, HttpServletResponse response,String entranceName){
        try {
            log.info("do~~qqq:");
            HttpDecoder httpDecoder = BeanHelper.getFrameBean(HttpDecoder.class);
            NetPacket netPacket = httpDecoder.decode(request);
            if (netPacket == null) {
                // net解码错误
                System.out.println("net解码错误");
            }

            NetPacket reNetPacket=ControllerDispatcher.handle(NetType.Http,netPacket,request.getContextPath(), Util.getIp(request));

            if (reNetPacket == null) {
                // 协议编码错误
                System.out.println("协议编码错误");
            }
            HttpEncoder httpEncoder = BeanHelper.getFrameBean(HttpEncoder.class);
            HttpPacket reHttpPacket = httpEncoder.encode(reNetPacket);
            if (reHttpPacket == null) {
                // http编码错误
                System.out.println("http编码错误");
            }
            // 设置头
            Map<String, String> headers = reHttpPacket.getHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                // 这个地方要用setHeader，而不是addHeader
                response.setHeader(entry.getKey(), entry.getValue());
            }
            System.out.println("reHttpPacket:"+reHttpPacket.getHeaders()+","+reHttpPacket.getData().length);
            // 这个地方要+1
            response.setBufferSize(reHttpPacket.getData().length+1);
            response.setContentLength(reHttpPacket.getData().length);
            response.getOutputStream().write(reHttpPacket.getData(), 0, reHttpPacket.getData().length);
            response.getOutputStream().flush();
//            response.getOutputStream().close();
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(entranceName+" Exception");
        }
    }
}
