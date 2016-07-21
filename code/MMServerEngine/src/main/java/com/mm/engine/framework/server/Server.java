package com.mm.engine.framework.server;

import com.mm.engine.framework.control.update.UpdateManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Administrator on 2015/11/16.
 *
 */
public final class Server {
    private static final Logger log = LoggerFactory.getLogger(Server.class);
    private static EngineConfigure configure;
    /**
     * 初始化配置文件
     * 初始化所有helper
     * 启动游戏主循环
     * **/
    public static void init(EngineConfigure configure){
        Server.configure=configure;
    }
    public static void init(){
        init(new EngineConfigure());
    }

    public static void start(){
        UpdateManager.start();
    }

    public static void stop(){
        UpdateManager.stop();
    }

    public static EngineConfigure getEngineConfigure(){
        if(configure==null){
            log.error("configure is not init,don't getEngineConfigure() before server start");
            throw new RuntimeException("configure is not init,don't getEngineConfigure() before server start");
        }
        return configure;
    }
}
