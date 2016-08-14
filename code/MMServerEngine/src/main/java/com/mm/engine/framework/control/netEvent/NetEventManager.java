package com.mm.engine.framework.control.netEvent;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
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
    private static final Map<String,ServerClient> serverClientMap = new HashMap<>();
    // 这里可以考虑用多个服务器用作主服务器
    private static final ServerClient mainServer;

    private static Map<Integer,NetEventListenerHandler> handlerMap=new HashMap<Integer,NetEventListenerHandler>();

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,100,3000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),
            new RejectedExecutionHandler(){
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // 拒绝执行
                }
            });
    static {
        // 初始化所有服务器
        String serversStr = Server.getEngineConfigure().getString(SERVERSKEY);
        mainServer = new ServerClient();


        TIntObjectHashMap<Class<?>> netEventHandlerClassMap = ServiceHelper.getNetEventListenerHandlerClassMap();
        netEventHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
            @Override
            public boolean execute(int i, Class<?> aClass) {
                handlerMap.put(i, (NetEventListenerHandler)BeanHelper.getServiceBean(aClass));
                return true;
            }
        });
    }
    // 一个系统的一种NetEvent只有一个监听器(因为很多事件需要返回数据)，可以通过内部事件分发
    public static NetEventListenerHandler getHandler(int netEvent){
        return  handlerMap.get(netEvent);
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
     * */
    public static Map<Integer,Object> broadcastNetEventSyn(NetEventData netEvent){
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
     */
    public static Object fireMainServerNetEvent(NetEventData netEvent){
//        mainServer.
        return null;
    }
}
