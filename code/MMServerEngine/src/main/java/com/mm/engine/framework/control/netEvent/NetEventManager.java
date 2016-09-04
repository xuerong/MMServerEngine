package com.mm.engine.framework.control.netEvent;

import com.mm.engine.framework.control.ServiceHelper;
import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.annotation.NetEventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.entrance.client.ServerClient;
import com.mm.engine.framework.entrance.client.socket.NettyServerClient;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.exception.MMException;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import gnu.trove.map.hash.TIntObjectHashMap;
import gnu.trove.procedure.TIntObjectProcedure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

/**
 * Created by Administrator on 2015/12/30.
 *
 * TODO 发送netEvent有两种情况：
 * 1、自己执行了，接收者如果是自己，就不用发，或者不用处理：本地缓存之间的flush通知
 * 2、自己没有执行，接受者是谁都要处理：加锁
 *
 * 广播默认不发给自己
 * 发给单个服务器的，要发给自己
 * 自己不连自己，而是直接调用
 * TODO 被忘了设定超时机制
 */
@Service(init = "init")
public class NetEventManager{
    private static final Logger log = LoggerFactory.getLogger(NetEventManager.class);


    private static final String SERVERSKEY = "servers";

//    private static Map<Integer,NetEventListenerHandler> handlerMap=new HashMap<Integer,NetEventListenerHandler>();
private static Map<Integer,NetEventListenerHandler> handlerMap=null;
    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,100,3000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),
            new RejectedExecutionHandler(){
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // 拒绝执行
                }
            });
    // 所有已经添加的服务器
    private static ConcurrentHashMap<String,ServerInfo> servers = new ConcurrentHashMap<>();
    // 所有的serverClient 不包括自己 TODO 一个server可能既是这个server又是那个server
    private static Map<String,ServerClient> serverClientMap = new ConcurrentHashMap<>();
    // mainServer 不包括自己
    private static ServerClient asyncServerClient;
    // mainServer client 不包括自己
    private static ServerClient mainServerClient;
    //
    private static String selfAdd;

    static {

//        TIntObjectHashMap<Class<?>> netEventHandlerClassMap = ServiceHelper.getNetEventListenerHandlerClassMap();
//        netEventHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>(){
//            @Override
//            public boolean execute(int i, Class<?> aClass) {
//                handlerMap.put(i, (NetEventListenerHandler)BeanHelper.getServiceBean(aClass));
//                return true;
//            }
//        });
    }

    public void init(){

        if(handlerMap == null){ //TODO 这个要变成start时候初始化，
            synchronized (NetEventManager.class) {
                if(handlerMap == null) {
                    final Map<Integer,NetEventListenerHandler> _handlerMap = new HashMap<>();
                    TIntObjectHashMap<Class<?>> netEventHandlerClassMap = ServiceHelper.getNetEventListenerHandlerClassMap();
                    netEventHandlerClassMap.forEachEntry(new TIntObjectProcedure<Class<?>>() {
                        @Override
                        public boolean execute(int i, Class<?> aClass) {
                            _handlerMap.put(i, (NetEventListenerHandler) BeanHelper.getServiceBean(aClass));
                            return true;
                        }
                    });
                    handlerMap = _handlerMap;
                }
            }
        }
        selfAdd = Util.getHostAddress()+":"+Server.getEngineConfigure().getNetEventPort();
    }

    public static void notifyConnMainServer(){
        if(ServerType.isMainServer()){
            log.info("不需要连接mainServer,本服务器即为mainServer");
            return ;
        }
        String mainServerAdd = Server.getEngineConfigure().getMainServerNetEventAdd();
        String[] items = mainServerAdd.split(":");
        if(items.length<2){
            throw new MMException("mainServerAdd error:"+mainServerAdd);
        }
        if(!items[0].equalsIgnoreCase("localhost") && !Util.isIP(items[0])){
            throw new MMException("mainServerAdd error:"+mainServerAdd);
        }
        String host = items[0];
        int port = Integer.parseInt(items[1]);
        int localPort = Server.getEngineConfigure().getNetEventPort();
        if(Util.isLocalHost(host) && port == localPort){
            log.info("本服务器被配置为mainServer，但未按照mainServer启动，请重新配置mainServer或按照mainServer启动");
            return ;
        }
        NettyServerClient nettyServerClient = new NettyServerClient(ServerType.MAIN_SERVER,host,port);
        try{
            nettyServerClient.start();
        }catch (Throwable e){
            throw new MMException(e);
        }
        serverClientMap.put(host+":"+port,nettyServerClient);
        mainServerClient = nettyServerClient;
        // 告诉mainServer 自己是谁，并且从mainServer哪里获取其它服务器，并连接之
        tellMainServer();
    }
    // 主服务器：别人请求添加，并请求返回其它服务器信息，并告诉其他服务器它的存在
    @NetEventListener(netEvent = SysConstantDefine.TellMainServerSelfInfo)
    public NetEventData registerServerToMain(NetEventData eventData){
        if(!ServerType.isMainServer()){
            throw new MMException("this server is not mainServer , serverType="+ServerType.getServerTypeName());
        }
        // 添加到serverList，返回其它server的List
        ServerInfo serverInfo = (ServerInfo)eventData.getParam();
        String add = serverInfo.getHost()+":"+serverInfo.getPort();
        ServerInfo old = servers.putIfAbsent(add,serverInfo);
        if(old == null){ // 没有添加它
            // 告诉其他服务器，它的存在
            NetEventData serverInfoData = new NetEventData(SysConstantDefine.TellServersNewInfo,eventData.getParam());
            broadcastNetEvent(serverInfoData,false); // 这个地方有可能发送给serverInfo，因为是异步发送的
            // 创建NettyServerClient，并连接
            connectServer(serverInfo);
            // 告诉它，所有其他的
            NetEventData ret = new NetEventData(eventData.getNetEvent(),servers);
            return ret;
        }
        throw new MMException("该服务器已经注册完成，是否是断线重连？");
    }
    // 其它服务器：主服务器推出其它服务器的存在：建立与其它服务器的连接
    @NetEventListener(netEvent = SysConstantDefine.TellServersNewInfo)
    public NetEventData receiveServerInfoFromMainServer(NetEventData eventData){
        ServerInfo serverInfo = (ServerInfo)eventData.getParam();
        String add = serverInfo.getHost()+":"+serverInfo.getPort();
        if(Util.isLocalHost(serverInfo.getHost()) &&
                serverInfo.getPort() == Server.getEngineConfigure().getNetEventPort()){ // 过滤掉自己
            return null;
        }
        ServerInfo old = servers.putIfAbsent(add,serverInfo);
        if(old == null){
            connectServer(serverInfo);
            return null;
        }
        throw new MMException("该服务器已经注册完成，是否是断线重连？");
    }
    // nettyServerClient断线通知:如果是mainServer，则重连，否则，从client记录去掉它，它连上mainServer自然会重新通知
    @EventListener(event = SysConstantDefine.Event_NettyServerClient)
    public void nettyServerClientDisconnect(EventData eventData){
        NettyServerClient client = (NettyServerClient)eventData.getData();
        if(mainServerClient == client){ //如果是mainServer,则重连
            try{
                client.start();
            }catch (Throwable e){
                throw new MMException(e);
            }
            tellMainServer();
        }else { //
            String add = client.getHost()+":"+client.getPort();
            servers.remove(add);
            serverClientMap.remove(add);
            if(asyncServerClient == client){
                asyncServerClient = null;
            }
        }
    }
    private static void tellMainServer(){
        // 告诉mainServer 自己是谁，并且从mainServer哪里获取其它服务器，并连接之
        NetEventData netEventData = new NetEventData(SysConstantDefine.TellMainServerSelfInfo);
        ServerInfo serverInfo = new ServerInfo();
        serverInfo.setHost(Util.getHostAddress());
        serverInfo.setPort(Server.getEngineConfigure().getNetEventPort());
        serverInfo.setType(ServerType.getServerType());
        netEventData.setParam(serverInfo);

        NetEventData ret = fireMainServerNetEventSyn(netEventData); //通知主服务器，并获取其它服务器列表

        Map<String,ServerInfo> retServers = (Map)ret.getParam();
        String localAdd = serverInfo.getHost()+":"+Server.getEngineConfigure().getNetEventPort();
        for (Map.Entry<String, ServerInfo> entry: retServers.entrySet()){
            serverInfo = entry.getValue();
            if(entry.getKey().equals(localAdd)){ // 把自己过滤出来
                continue;
            }
            // 创建NettyServerClient，并连接
            ServerInfo old = servers.putIfAbsent(entry.getKey(),entry.getValue());
            if(old == null){
                connectServer(serverInfo);
            }else{
                log.warn("mainServer reStart?");
            }
        }
    }
    private static void connectServer(ServerInfo serverInfo){
        // 创建NettyServerClient，并连接
        NettyServerClient client = new NettyServerClient(serverInfo.getType(),serverInfo.getHost(),serverInfo.getPort());
        try{
            client.start();
        }catch (Throwable e){
            throw new MMException(e);
        }
        if(ServerType.isAsyncServer(serverInfo.getType())){
            if(asyncServerClient == null) {
                asyncServerClient = client;
            }else {
                throw new MMException("asyncServer 重复");
            }
        }
        serverClientMap.put(serverInfo.getHost()+":"+serverInfo.getPort(),client);
    }
    // 一个系统的一种NetEvent只有一个监听器(因为很多事件需要返回数据)，可以通过内部事件分发
    public static NetEventData handle(NetEventData netEventData){
        if(handlerMap == null){ //TODO 这个要变成start时候初始化，
            throw new MMException("改造这些Manager");
        }
        NetEventListenerHandler handler = handlerMap.get(netEventData.getNetEvent());
        if(handler == null){
            throw new MMException("netEventHandle is not exist , netEvent="+netEventData.getNetEvent());
        }
        return handler.handle(netEventData);
    }

    /**
     * 事件是异步的
     * **/
    public static void broadcastNetEvent(final NetEventData netEvent, final boolean self){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                // 通过NetEvent的网络接口发出事件
                for(final Map.Entry<String,ServerClient> entry : serverClientMap.entrySet()){
                    try{ // TODO 这里尽量做到不捕获异常，提高效率
                        entry.getValue().sendWithoutReply(netEvent); // 这个不等待返回，所以不用多个发送
                    }finally {
                        continue;
                    }
                }
                if(self){
                    handle(netEvent);
                }
            }
        });
    }

    /**
     * 同步触发事假，即事件完成方可返回
     * 别忘了截取一些出问题的事件
     * 显然，这里每个ServerClient并不需要同步等待，
     * */
    public static Map<String,NetEventData> broadcastNetEventSyn(final NetEventData netEvent,boolean self){
        try {
            final CountDownLatch latch = new CountDownLatch(serverClientMap.size());
            final Map<String,NetEventData> result = new ConcurrentHashMap<>();
            // 通过NetEvent的网络接口发出事件
            for(final Map.Entry<String,ServerClient> entry : serverClientMap.entrySet()){
                executor.execute(new Runnable() {
                    @Override
                    public void run() {
                        try{ // TODO 这里尽量做到不捕获异常，提高效率
                            NetEventData ret = (NetEventData)entry.getValue().send(netEvent);
                            result.put(entry.getKey(),ret);
                        }finally {
                            latch.countDown();
                        }
                    }
                });
            }
            latch.await();
            if(self){
                NetEventData ret = handle(netEvent);
                result.put(Util.getHostAddress()+":"+Server.getEngineConfigure().getNetEventPort(),ret);
            }
            return result;
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
        if(ServerType.isMainServer()){
            handle(netEvent);
            return;
        }
        if(mainServerClient != null){
            mainServerClient.sendWithoutReply(netEvent);
            return;
        }
        throw new MMException("mainServerClient is null");
    }
    /**
     * 向主服务器发送事件
     */
    public static NetEventData fireMainServerNetEventSyn(NetEventData netEvent){
        if(ServerType.isMainServer()){
            return handle(netEvent);
        }
        if(mainServerClient != null){
            return (NetEventData) mainServerClient.send(netEvent);
        }
        throw new MMException("mainServerClient is null");
    }
    /**
     * 向异步服务器发送事件
     * 异步
     */
    public static void fireAsyncServerNetEvent(NetEventData netEvent){
        if(ServerType.isAsyncServer()){
            handle(netEvent);
            return;
        }
        if(asyncServerClient != null){
            asyncServerClient.sendWithoutReply(netEvent);
            return;
        }
        throw new MMException("asyncServerClient is null");
    }
    /**
     * 向异步服务器发送事件
     */
    public static NetEventData fireAsyncServerNetEventSyn(NetEventData netEvent){
        if(ServerType.isAsyncServer()){
            return handle(netEvent);
        }
        if(asyncServerClient != null){
            return (NetEventData)asyncServerClient.send(netEvent);
        }
        throw new MMException("asyncServerClient is null");
    }
    /**
     * 向某个服务器发送事件
     * 异步
     */
    public static void fireServerNetEvent(String add,NetEventData netEvent){
        if(add.equals(selfAdd)){
            handle(netEvent);
            return;
        }
        ServerClient serverClient = serverClientMap.get(add);
        if(serverClient != null){
            serverClient.sendWithoutReply(netEvent);
            return;
        }
        throw new MMException("serverClient is null");
    }
    /**
     * 向某个服务器发送事件
     * 同步
     */
    public static NetEventData fireServerNetEventSyn(String add,NetEventData netEvent){
        if(add.equals(selfAdd)){
            return handle(netEvent);
        }
        ServerClient serverClient = serverClientMap.get(add);
        if(serverClient != null){
            return (NetEventData)serverClient.send(netEvent);
        }
        throw new MMException("serverClient is null");
    }
}
