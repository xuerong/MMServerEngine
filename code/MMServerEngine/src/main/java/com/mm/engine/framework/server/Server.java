package com.mm.engine.framework.server;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.security.MonitorService;
import com.mm.engine.framework.server.configure.EngineConfigure;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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
        // Service的初始化
        Map<Class<?>, Object> serviceBeanMap = BeanHelper.getServiceBeans();
        Map<Class<?>, Method> initMethodMap = ServiceHelper.getInitMethodMap();

        for(Map.Entry<Class<?>, Object> entry : serviceBeanMap.entrySet()){
            Method method = initMethodMap.get(entry.getKey());
            if(method != null){
                try {
                    method.invoke(entry.getValue());
                } catch (IllegalAccessException|InvocationTargetException e) {
                    e.printStackTrace();
                }finally { // 报异常，这里是停服务器还是继续？
                    continue;
                }
            }
        }
        // 启动所有入口
//        List<Entrance> entranceList = configure.getEntranceList();
        Collection<Entrance> entranceList = BeanHelper.getEntranceBeans().values();
        for (Entrance entrance :entranceList) {
            try {
                entrance.start();
            }catch (Exception e){
//                e.printStackTrace();
                log.error("net start fail , net name = "+entrance.getName()+","+e.getMessage());
                try{
                    entrance.stop();
                }catch (Exception e2){
                    log.error("net stop fail , net name = "+entrance.getName()+":"+e2.getStackTrace());
                }
            }
        }
        // 等待启动条件完成
        MonitorService monitorService = BeanHelper.getServiceBean(MonitorService.class);
        monitorService.startWait();
        // 服务器启动完成
        EventService eventService = BeanHelper.getServiceBean(EventService.class);
        eventService.fireEventSyn(new EventData(SysConstantDefine.Event_ServerStart));

        log.info("服务器启动完成!");
    }

    public static void stop(){
        // 关闭入口
        Collection<Entrance> entranceList = BeanHelper.getEntranceBeans().values();
        for (Entrance entrance :entranceList) {
            try {
                entrance.stop();
            }catch (Exception e){
                log.error("net stop fail , net name = "+entrance.getName()+":"+e.getStackTrace());
            }
        }
        // 关闭所有的Service
        Map<Class<?>, Object> serviceBeanMap = BeanHelper.getServiceBeans();
        Map<Class<?>, Method> destroyMethodMap = ServiceHelper.getDestroyMethodMap();
        for(Map.Entry<Class<?>, Object> entry : serviceBeanMap.entrySet()){
            Method method = destroyMethodMap.get(entry.getKey());
            if(method != null){
                try {
                    method.invoke(entry.getValue());
                } catch (IllegalAccessException|InvocationTargetException e) {
                    e.printStackTrace();
                }finally { // 报异常，这里是停服务器还是继续？
                    continue;
                }
            }
        }
        // 等待关闭条件完成
        MonitorService monitorService = BeanHelper.getServiceBean(MonitorService.class);
        monitorService.stopWait();

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
        EngineConfigure configure = new EngineConfigure(serverTypeStr);
        if(args.length>1){
            System.out.println(args[1]);
            configure.changeEntrancePort(args[1]);
        }

        Server.init(configure);
        Server.start();
    }
}
