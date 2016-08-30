package com.mm.engine.framework.control.netEvent;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.entrance.client.ServerClient;
import com.mm.engine.framework.entrance.client.socket.NettyServerClient;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.exception.MMException;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Administrator on 2015/12/30.
 */
public class NetEventManager{
    private static final Logger log = LoggerFactory.getLogger(NetEventManager.class);


    private static final String SERVERSKEY = "servers";

    private static Map<Integer,NetEventListenerHandler> handlerMap=new HashMap<Integer,NetEventListenerHandler>();

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,100,3000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),
            new RejectedExecutionHandler(){
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // 拒绝执行
                }
            });

    // mainServer client
    private static ServerClient mainServerClient;

    static {

        TIntObjectHashMap<Class<?>> netEventHandlerClassMap = ServiceHelper.getNetEventListenerHandlerClassMap();
        netEventHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
            @Override
            public boolean execute(int i, Class<?> aClass) {
                handlerMap.put(i, (NetEventListenerHandler)BeanHelper.getServiceBean(aClass));
                return true;
            }
        });
    }

    public static void notifyConnMainServer(){
        String mainServerAdd = Server.getEngineConfigure().getMainServerNetEventAdd();
        String[] items = mainServerAdd.split(":");
        if(items.length<2){
            throw new MMException("mainServerAdd error:"+mainServerAdd);
        }
        if(!items[0].equalsIgnoreCase("localhost") && !Util.isIP(items[0])){
            throw new MMException("mainServerAdd error:"+mainServerAdd);
        }
        NettyServerClient nettyServerClient = new NettyServerClient(ServerType.MAIN_SERVER,items[0],Integer.parseInt(items[1]));
        try{
            nettyServerClient.start();
        }catch (Throwable e){
            throw new MMException(e);
        }
        // 告诉mainServer 自己是谁，并且从mainServer哪里获取其它服务器，并连接之
        mainServerClient = nettyServerClient;
    }

    // 一个系统的一种NetEvent只有一个监听器(因为很多事件需要返回数据)，可以通过内部事件分发
    public static RetPacket handle(NetEventData netEventData){
        NetEventListenerHandler handler = handlerMap.get(netEventData.getNetEvent());
        if(handler == null){
            throw new MMException("netEventHandle is not exist , netEvent="+netEventData.getNetEvent());
        }
        return handler.handle(netEventData);
    }

    /**
     * 事件是异步的
     * **/
    public static void broadcastNetEvent(final NetEventData netEvent){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                broadcastNetEventSyn(netEvent);
            }
        });
    }

    /**
     * 同步触发事假，即事件完成方可返回
     * 别忘了截取一些出问题的事件
     * */
    public static NetEventData broadcastNetEventSyn(NetEventData netEvent){
        try {
            // 通过NetEvent的网络接口发出事件
            return null;
        }catch (Throwable e){
            e.printStackTrace();
            log.error("exception happened while fire netEvent :"+netEvent.getNetEvent());
        }
        return null;
    }
    /**
     * 向主服务器发送事件
     * 异步
     */
    public static void fireMainServerNetEvent(NetEventData netEvent){
//        mainServerClient.send()
    }
    /**
     * 向主服务器发送事件
     */
    public static NetEventData fireMainServerNetEventSyn(NetEventData netEvent){
//        mainServer.
        return null;
    }
    /**
     * 向异步服务器发送事件
     * 异步
     */
    public static void fireAsyncServerNetEvent(NetEventData netEvent){
//        mainServer.
    }
    /**
     * 向异步服务器发送事件
     */
    public static NetEventData fireAsyncServerNetEventSyn(NetEventData netEvent){
//        mainServer.
        return null;
    }
}
