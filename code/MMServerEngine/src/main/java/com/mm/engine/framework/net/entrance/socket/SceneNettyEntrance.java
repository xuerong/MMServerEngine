package com.mm.engine.framework.net.entrance.socket;

import com.mm.engine.framework.control.scene.SceneNetData;
import com.mm.engine.framework.control.scene.SceneService;
import com.mm.engine.framework.net.code.netty.*;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.tool.helper.BeanHelper;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by a on 2016/9/14.
 */
public class SceneNettyEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(SceneNettyEntrance.class);
    Channel channel = null;
    static SceneService sceneService;

    @Override
    public void start() throws Exception {
        channel = NettyHelper.createAndStart(
                port,SceneNettyEncoder.class,SceneNettyDecoder.class,DiscardServerHandler.class,name);
        sceneService = BeanHelper.getServiceBean(SceneService.class);
        log.info("bind port :"+port);
    }
    public static class DiscardServerHandler extends ChannelInboundHandlerAdapter { // (1)
        @Override
        public void channelActive(final ChannelHandlerContext ctx) { // (1)

        }

        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) { // (2)
            SceneNetData sceneNetData = (SceneNetData)msg;
            Object retData = sceneService.handle(sceneNetData.getSceneId(),sceneNetData.getOpcode(),sceneNetData.getData());
            sceneNetData.setData(retData);
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
