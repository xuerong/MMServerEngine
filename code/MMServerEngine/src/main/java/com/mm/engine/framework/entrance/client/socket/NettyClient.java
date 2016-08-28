package com.mm.engine.framework.entrance.client.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.entrance.code.net.netty.NettyDeCoder;
import com.mm.engine.framework.entrance.code.net.netty.NettyEnCoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.AttributeKey;

/**
 * Created by apple on 16-8-27.
 */
public class NettyClient {
//    private String host = "localhost";
//    private int port = 8000;
//
//    public void start() throws Exception{
//        EventLoopGroup workerGroup = new NioEventLoopGroup();
//
//        try {
//            Bootstrap b = new Bootstrap(); // (1)
//            b.group(workerGroup); // (2)
//            b.channel(NioSocketChannel.class); // (3)
//            b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
//            b.handler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(
//                            new NettyEnCoder(),
//                            new NettyDeCoder(),
//                            new NettyClientHandler()
//                    );
//                }
//            });
//
//            // Start the client.
//            ChannelFuture f = b.connect(host, port).sync(); // (5)
//
//            // Wait until the connection is closed.
//            f.channel().closeFuture().sync();
//        } finally {
//            workerGroup.shutdownGracefully();
//        }
//    }
//
//
//    class NettyClientHandler extends ChannelInboundHandlerAdapter {
//        @Override
//        public void channelRead(ChannelHandlerContext ctx, Object msg) {
//            NetPacket netPacket = (NetPacket) msg; // (1)
//            try {
//                final ChannelFuture f = ctx.writeAndFlush(netPacket); // (3)
////                ctx.close();
//            } finally {
////                m.release();
//            }
//        }
//
//        @Override
//        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
//            cause.printStackTrace();
//            ctx.close();
//        }
//    }
}
