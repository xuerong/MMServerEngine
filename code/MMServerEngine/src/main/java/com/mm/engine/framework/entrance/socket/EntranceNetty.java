package com.mm.engine.framework.entrance.socket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.entrance.code.net.netty.NettyDeCoder;
import com.mm.engine.framework.entrance.code.net.netty.NettyEnCoder;
import com.mm.engine.framework.entrance.Entrance;
import com.mm.engine.framework.entrance.NetFlowFire;
import com.mm.engine.framework.server.SysConstantDefine;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.ObjectOutputStream;

/**
 * Created by apple on 16-8-27.
 */
public class EntranceNetty extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(EntranceNetty.class);
    public EntranceNetty(String name, int port){
        super(name,port);
    }

    @Override
    public void start() throws Exception {
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                log.info("netty start begin");
                EventLoopGroup bossGroup = new NioEventLoopGroup(); // (1)
                EventLoopGroup workerGroup = new NioEventLoopGroup();
                try {
                    ServerBootstrap b = new ServerBootstrap(); // (2)
                    b.group(bossGroup, workerGroup)
                            .channel(NioServerSocketChannel.class) // (3)
                            .childHandler(new ChannelInitializer<SocketChannel>() { // (4)
                                @Override
                                public void initChannel(SocketChannel ch) throws Exception {
                                    ch.pipeline().addLast(
                                            new NettyDeCoder(), // 解码器
                                            new NettyEnCoder(), // 编码器
                                            new DiscardServerHandler() //处理器
                                    );
                                }
                            })
                            .option(ChannelOption.SO_BACKLOG, 128)          // (5)
                            .childOption(ChannelOption.SO_KEEPALIVE, true); // (6)

                    // Bind and start to accept incoming connections.
                    ChannelFuture f = b.bind(port).sync(); // (7)
                    // Wait until the server socket is closed.
                    // In this example, this does not happen, but you can do that to gracefully
                    // shut down your server.
                    f.channel().closeFuture().sync();
                    log.info("netty stop ");
                }catch (InterruptedException e){
                    e.printStackTrace();
                }finally {
                    workerGroup.shutdownGracefully();
                    bossGroup.shutdownGracefully();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.start();
    }

    @Override
    public void stop() throws Exception {

    }

    /**
     * Handles a server-side channel.
     */
    public class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)
            try{
                NetEventData netEventData = new NetEventData(222);
                netEventData.setParam("jjsjsjsjs");
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                ObjectOutputStream out = new ObjectOutputStream(bos);
                out.writeObject(netEventData);
                out.flush();
                out.close();
                byte[] nb = bos.toByteArray();

                NetPacket netPacket = new NetPacketImpl(nb);
                netPacket.put(SysConstantDefine.controller,"DefaultNetEventController");
                final ChannelFuture f = ctx.writeAndFlush(netPacket); // (3)
                System.out.println("zheli");
//            f.addListener(new ChannelFutureListener() {
//                @Override
//                public void operationComplete(ChannelFuture future) {
//                    assert f == future;
//                    ctx.close();
//                }
//            }); // (4)
            }catch (Throwable e){
                e.printStackTrace();
            }

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            // Discard the received data silently.
            NetFlowFire.fireNetty(ctx,msg,"EntranceNetty");
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
            // Close the connection when an exception is raised.
            cause.printStackTrace();
            ctx.close();
        }
    }
}
