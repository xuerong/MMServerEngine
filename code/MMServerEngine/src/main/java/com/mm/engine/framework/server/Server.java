package com.mm.engine.framework.server;

import com.mm.engine.framework.control.update.UpdateManager;
import com.mm.engine.framework.data.tx.AsyncManager;
import com.mm.engine.framework.entrance.Entrance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

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
    public static void init(String serverTypeStr){
        init(new EngineConfigure(serverTypeStr));
    }
    public static void init(){
        init(new EngineConfigure());
    }

    public static void start(){
        UpdateManager.start();
        // 启动所有入口
        List<Entrance> entranceList = configure.getEntranceList();
        for (Entrance entrance :entranceList) {
            try {
                entrance.start();
            }catch (Exception e){
                log.error("entrance start fail , entrance name = "+entrance.getName()+e.getCause());
                try{
                    entrance.stop();
                }catch (Exception e2){
                    log.error("entrance stop fail , entrance name = "+entrance.getName()+":"+e2.getStackTrace());
                }
            }
        }
        // 服务器启动完成
        log.info("服务器启动完成!");
    }

    public static void stop(){
        // 关闭更新器
        UpdateManager.stop();
        // 关闭入口
        List<Entrance> entranceList = configure.getEntranceList();
        for (Entrance entrance :entranceList) {
            try{
                entrance.stop();
            }catch (Exception e2){
                log.error("entrance stop fail , entrance name = "+entrance.getName()+":"+e2.getStackTrace());
            }
        }
        // 异步服务器关闭
        if(Server.getEngineConfigure().isAsyncServer()){
            AsyncManager.stop();
            log.info("异步服务器关闭完成!");
        }

        log.info("服务器关闭完成!");
    }

    public static EngineConfigure getEngineConfigure(){
        if(configure==null){
            log.error("configure is not init,don't getEngineConfigure() before server start");
            throw new RuntimeException("configure is not init,don't getEngineConfigure() before server start");
        }
        return configure;
    }

    /**
     * 应用程序启动，如果在容器中运行，请在容器中调用init和start方法
     * @param args
     */
    public static void main(String[] args){
        String serverTypeStr = args[0];
        Server.init(serverTypeStr);
        Server.start();
    }
}
