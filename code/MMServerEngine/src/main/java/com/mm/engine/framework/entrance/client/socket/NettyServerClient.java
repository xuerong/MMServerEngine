package com.mm.engine.framework.entrance.client.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.entrance.client.AbServerClient;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.entrance.code.net.netty.NettyDeCoder;
import com.mm.engine.framework.entrance.code.net.netty.NettyEnCoder;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.server.SysConstantDefine;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by apple on 16-8-28.
 */
public class NettyServerClient extends AbServerClient {
    private Channel channel;

    @Override
    public void start() {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    Bootstrap b = new Bootstrap(); // (1)
                    b.group(workerGroup); // (2)
                    b.channel(NioSocketChannel.class); // (3)
                    b.option(ChannelOption.SO_KEEPALIVE, true); // (4)
                    b.handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(
                                    new NettyEnCoder(),
                                    new NettyDeCoder(),
                                    new NettyClientHandler()
                            );
                        }
                    });

                    // Start the client.
                    ChannelFuture f = b.connect(host, port).sync(); // (5)

                    // Wait until the connection is closed.
                    f.channel().closeFuture().sync();
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    workerGroup.shutdownGracefully();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public RetPacket send(Object msg, String controller) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(msg);
            out.flush();
            out.close();
            byte[] nb = bos.toByteArray();

            NetPacket netPacket = new NetPacketImpl(nb);
            netPacket.put(SysConstantDefine.controller,controller);
            final ChannelFuture f = channel.writeAndFlush(netPacket); // (3)

            f.addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) {
//                    assert f == future;
//                    ctx.close();
                }
            }); // (4)
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendWithoutReply(Object msg, String controller) {

    }

    class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            NetPacket netPacket = (NetPacket) msg; // (1)
            try {
                final ChannelFuture f = ctx.writeAndFlush(netPacket); // (3)
//                ctx.close();
            } finally {
//                m.release();
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
