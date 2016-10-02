package com.mm.engine.framework.net.client.socket;

import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.net.client.AbServerClient;
import com.mm.engine.framework.net.code.netty.DefaultNettyDecoder;
import com.mm.engine.framework.net.code.netty.DefaultNettyEncoder;
import com.mm.engine.framework.net.entrance.socket.SocketPacket;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by apple on 16-8-28.
 * 服务器之间连接时候用的client……
 */
public class NettyServerClient extends AbServerClient {
    private static final Logger log = LoggerFactory.getLogger(NettyServerClient.class);
    private static final int timeout = 10; //10s
    private static final int reconnectionInterval = 10000;

    private LinkedBlockingQueue<Integer> idOut = new LinkedBlockingQueue<Integer>(); // 可以用的id，id池
    private Map<Integer,SocketPacket> packetMap = new ConcurrentHashMap<>(); // 正在用的id
    private AtomicInteger idCreator = new AtomicInteger(0); // 如果池中没有可用的，从这里获取

    private Channel channel;
    private volatile boolean running;

    private EventService eventService;

    public NettyServerClient(int serverType,String host,int port){

        eventService = BeanHelper.getServiceBean(EventService.class);

        this.serverType = serverType;
        this.host = host;
        this.port = port;
        running = false;
    }
    @Override
    public void start() throws Exception{
        final CountDownLatch latch = new CountDownLatch(1);
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
                                    new DefaultNettyEncoder(),
                                    new DefaultNettyDecoder(),
                                    new NettyClientHandler()
                            );
                        }
                    });
                    ChannelFuture f = null;
                    // Start the client.
                    while(true) {
                        try {
                            f = b.connect(host, port); // (5) // 这行代码要在while循环里面
                            f.sync();
                            break;
                        } catch (Exception e) {
                            if (e instanceof ConnectException) {
                                log.warn("connect "+ ServerType.getServerTypeName(serverType)+"("+(host+":"+port) +") fail ," +
                                        "reconnect after "+ reconnectionInterval/1000+" s");
                                Thread.sleep(reconnectionInterval);
                                continue;
                            }
                        }
                    }
                    channel = f.channel();
                    latch.countDown();
                    f.channel().closeFuture().sync();
                    // 客户端断线
                    log.info("disconnect server:"+"("+(host+":"+port) +")");
                    EventData eventData = new EventData(SysConstantDefine.Event_NettyServerClient);
                    eventData.setData(NettyServerClient.this);
                    eventService.fireEvent(eventData);
                }catch (Exception e){
                    e.printStackTrace();
                }finally {
                    workerGroup.shutdownGracefully();
                }
            }
        };
        Thread thread = new Thread(runnable);
        thread.setName("NettyServerClient");
        thread.start();
        latch.await();
        log.info("connect :"+ServerType.getServerTypeName(serverType)+"("+(host+":"+port) +") success");
        running = true;

    }

    @Override
    public Object send(Object msg) { // TODO 要不要考虑用多个包整合成一个包，来降低网络访问量
        try{
            SocketPacket socketPacket = new SocketPacket();
            Integer id = idOut.poll();
            if(id == null){
                id = idCreator.incrementAndGet();
            }
            socketPacket.setId(id);
            socketPacket.setData(msg);
            CountDownLatch latch = new CountDownLatch(1);
            socketPacket.setLatch(latch);
            packetMap.put(id,socketPacket);
            final ChannelFuture f = channel.writeAndFlush(socketPacket); // (3)
            if(!latch.await(timeout, TimeUnit.SECONDS)){
                throw new MMException("timeout while send packet,to "+getHost()+":"+getPort());
            }
            return socketPacket.getReData();
        }catch (Throwable e){
            if(e instanceof InterruptedException){
                throw new MMException(e);
            }
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void sendWithoutReply(Object msg) {
        try{
            final ChannelFuture f = channel.writeAndFlush(msg); // (3)
        }catch (Throwable e){
            e.printStackTrace();
        }
    }

    class NettyClientHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) {
            if(msg instanceof SocketPacket){ // 说明是某一个需要返回的值
                SocketPacket socketPacket = (SocketPacket)msg;
                SocketPacket s = packetMap.remove(socketPacket.getId());
                s.setReData(socketPacket.getData());
                s.getLatch().countDown();
                idOut.offer(s.getId());
            }else {
                throw new MMException("NettyServerClient receive data is not SocketPacket,throw away it");
            }
        }

        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }
}
