package com.mm.engine.framework.data.entity.account;

import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.event.EventService;
import com.mm.engine.framework.control.netEvent.RemoteCallService;
import com.mm.engine.framework.control.netEvent.ServerInfo;
import com.mm.engine.framework.data.DataService;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.data.tx.Tx;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.security.MonitorService;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/9/18.
 * 客户端首先登陆mainServer，获取需要登陆的nodeServer
 * mainServer保留该用户的登陆nodeServer
 *
 * 抛出三个事件：创建account，登陆，登出
 * 登陆对外接口：loginMain，传出LoginSegment
 * 登出对外接口：logout
 *
 */
@Service(init = "init")
public class AccountSysService {
    private static final Logger log = LoggerFactory.getLogger(AccountSysService.class);
    //用于mainServer： nodeServer
    private Map<String,NodeServerState> nodeServerMap = new HashMap<>();
    //用于mianServer： 账号登陆的服务器mainServer上面用,accountId-nodeServerKey
    private ConcurrentHashMap<String,String> mainServerAccountLoginMap = new ConcurrentHashMap<>();
    /**
     * 用于nodeServer：
     * accountId - sessionId
     *  login的时候，用这个校验：
     *  不存在：不允许登录，
     */
    private ConcurrentHashMap<String,String> nodeServerLoginMark = new ConcurrentHashMap<>();

    private DataService dataService;
    private SessionService sessionService;
    private RemoteCallService remoteCallService;
    private MonitorService monitorService;
    private EventService eventService;

    public void init(){
        dataService = BeanHelper.getServiceBean(DataService.class);
        sessionService = BeanHelper.getServiceBean(SessionService.class);
        remoteCallService = BeanHelper.getServiceBean(RemoteCallService.class);
        eventService = BeanHelper.getServiceBean(EventService.class);
    }
    @EventListener(event = SysConstantDefine.Event_EntranceStart)
    public void entranceStart(EventData eventData){
        // 自己是否是mainServer，又是nodeServer
        if(ServerType.isMainServer() && ServerType.isNodeServer()){
            Entrance entrance = (Entrance)eventData.getData();
            if(entrance.getName().equals("request")) {
                nodeServerRegister(Util.getHostAddress(), Server.getEngineConfigure().getRequestPort(), 10);
            }
        }
    }
    @EventListener(event = SysConstantDefine.Event_ConnectNewServer)
    public void newServerConnect(EventData eventData){
        if(!ServerType.isMainServer()){
            return;
        }
        ServerInfo serverInfo = (ServerInfo)eventData.getData();
        if(ServerType.isNodeServer(serverInfo.getType())){
            nodeServerRegister(serverInfo.getHost(),serverInfo.getRequestPort(),0);
        }
    }
    @EventListener(event = SysConstantDefine.Event_DisconnectNewServer)
    public void newServerDisconnect(EventData eventData){
        if(!ServerType.isMainServer()){
            return;
        }
        ServerInfo serverInfo = (ServerInfo)eventData.getData();
        String key = serverInfo.getHost()+":"+serverInfo.getRequestPort();
        NodeServerState nodeServerState = nodeServerMap.remove(key);
        // 如何处理登陆它的玩家数据？已经断连的，所以无法通知他们登出，这样的话应该是无法再有登陆该nodeServer的了，直到它再次连接mainServer
        // 那么此时如果玩家想主动登出如何办？原来登陆的玩家数据需要再次通知mainServer？其实基本不可能出现，两个服务器都运行着，但是无法互相连接的状态
//        if(nodeServerState != null){
//            for(String account : nodeServerState.getAccountIdSet()){
//
//            }
//        }
    }

    /**
     * 注册一个nodeServer，
     * 这个最好要传入该服务器的负载能力，如两个不同性能的服务器显然不能同等分配玩家
     * 注意，这里的port是指request的port，不是netEvent的port
     * @param host
     * @param port
     */
    public synchronized void nodeServerRegister(String host,int port,int workload){
        String key = host+":"+port;
        if(nodeServerMap.containsKey(key)){
//            throw new MMException("node server has register,key = "+key);
            log.warn("node server has register,key = "+key);
            return;
        }
        NodeServerState nodeServerState = new NodeServerState();
        nodeServerState.setPort(port);
        nodeServerState.setHost(host);
        nodeServerState.setAccountCount(0);
        nodeServerState.setWorkload(workload);
        nodeServerMap.put(key,nodeServerState);
    }
    /**
     * 登陆mainServer，
     * 1、mainServer获取分配给它的nodeServer
     * 2、通知nodeServer客户端的登陆请求，后面客户端登陆nodeServer时要校验
     * 3、返回分配的nodeServer的地址和访问用的sessionId
     *
     * 如果已经登陆，则把之前的账号顶下来:要考虑多个机器同时登陆一个账号时的同步问题
     * @param id
     */
    @Tx(tx = true,lock = true,lockClass = {Account.class})
    public LoginSegment loginMain(String id,String url,String ip){
        // check id
        if(id == null || id.length() == 0){
            throw new MMException("id error, id="+id);
        }
        // get account
        Account account = dataService.selectObject(Account.class,"id=?",id);
        if(account == null){
            // 没有则创建
            account = createAccount(id);
            dataService.insert(account);
            eventService.fireEventSyn(account,SysConstantDefine.Event_AccountCreate);
//            throw new MMException("account is not exist, id="+id);
        }
        // distribute nodeServer
        NodeServerState nodeServerState  = distributeNodeServer();
        String sessionId = (String)remoteCallService.remoteCallSyn(nodeServerState.getKey(),AccountSysService.class,"applyForLogin",id,url,ip);
        if(sessionId == null || sessionId.length() == 0){
            throw new MMException("login false,see log on "+nodeServerState.getKey());
        }

        nodeServerState.addAccount(id);
        // 显然这里有则覆盖
        mainServerAccountLoginMap.put(id,nodeServerState.getKey());
        //
        LoginSegment loginSegment = new LoginSegment();
        loginSegment.setHost(nodeServerState.getHost());
        loginSegment.setPort(nodeServerState.getPort());
        loginSegment.setSessionId(sessionId);
        loginSegment.setAccount(account);
        return loginSegment;
    }
    /**
     * 登出mainServer
     * account主动登出，
     */
    public void logout(String id){
        String nodeServerKey = mainServerAccountLoginMap.get(id);
        if(nodeServerKey == null){
            throw new MMException("user is not login,why logout?");
        }
        NodeServerState nodeServerState  = nodeServerMap.get(nodeServerKey);
        if(nodeServerState == null){
            throw new MMException("nodeServer "+nodeServerKey +" has stopped");
        }
        remoteCallService.remoteCallSyn(nodeServerState.getKey(),AccountSysService.class,"applyForLogout",id);
        nodeServerState.removeAccount(id);
    }

    /**
     * 用sessionId登陆nodeServer，
     * @param sessionId
     */
    public void loginNodeServer(String id,String sessionId){
        String sId = nodeServerLoginMark.get(id);
        if(sId == null){
            sessionService.removeSession(sessionId);
            throw new MMException("login error,sId = null");
        }
        if(!sId.equals(sessionId)){
            throw new MMException("两个地方同时登陆一个账号,login error,sId = "+sId+",sessionId = "+sessionId+",accountId = "+id);
        }
        Session session = sessionService.get(sessionId);
        if(session == null){
            throw new MMException("session is not exist");
        }
        Account account = dataService.selectObject(Account.class,"id=?",id);
        if(account == null){
            throw new MMException("account is not exist , id = "+id);
        }
        session.setSessionClient(account);
    }
    /**
    * nodeServer接收，来自mainServer的一个account的login请求
    * 如果可以登录，
    * 1、如果已经登录，在这里销毁之前的session
    * 2、创建session，
    * @return 返回sessionId
     **/
    public String applyForLogin(String id,String url,String ip){
        Session session = sessionService.create(url,ip);
        String olderSessionId = nodeServerLoginMark.putIfAbsent(id,session.getSessionId());
        if(olderSessionId != null){
            // 通知下线
            Session oldSession = sessionService.get(olderSessionId);
            if(oldSession != null){
                sessionService.removeSession(oldSession);
                // 账户登出事件
                LogoutEventData logoutEventData = new LogoutEventData();
                logoutEventData.setSession(session);
                logoutEventData.setLogoutReason(LogoutReason.replaceLogout);
                eventService.fireEventSyn(logoutEventData,SysConstantDefine.Event_AccountLogout);
            }
        }
        eventService.fireEventSyn(session,SysConstantDefine.Event_AccountLogin);
        return session.getSessionId();
    }

    /**
     * mainServer向nodeServer要求登出某个玩家
     * 去掉session
     * @param id
     */
    public void applyForLogout(String id){
        String sessionId = nodeServerLoginMark.get(id);
        if(sessionId == null){
            throw new MMException("sessionId is not exist , accountId = "+id+"");
        }
        Session session = sessionService.get(sessionId);
        if(session == null){
            throw new MMException("session is not exist , sessionId = "+sessionId);
        }
        sessionService.removeSession(session);

        LogoutEventData logoutEventData = new LogoutEventData();
        logoutEventData.setSession(session);
        logoutEventData.setLogoutReason(LogoutReason.userLogout);
        eventService.fireEventSyn(logoutEventData,SysConstantDefine.Event_AccountLogout);
    }

    /**
     * 通过某种机制分配一个服务器
     * @return
     */
    private NodeServerState distributeNodeServer(){
        int serverCount = nodeServerMap.size();
        if(serverCount <= 0){
            throw new MMException("there is no nodeServer");
        }
        int num = (int)(Math.random()*serverCount);
        int i = 0;
        for (NodeServerState nodeServerState:nodeServerMap.values()) {
            if(i++ >= num){
                return nodeServerState;
            }
        }
        throw new MMException("won't happen here");
    }

    /**
     * 创建一个account
     * TODO 这个要初始化哪些数据呢？
     * @param id
     * @return
     */
    private Account createAccount(String id){
        Account account = new Account();
        account.setId(id);
        return account;
    }
}
