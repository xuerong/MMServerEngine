package com.mm.engine.framework.data.entity.account.sendMessage;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.netEvent.remote.BroadcastRPC;
import com.mm.engine.framework.control.netEvent.remote.RemoteCallService;
import com.mm.engine.framework.data.entity.account.AccountSysService;
import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.tx.LockerService;
import com.mm.engine.framework.data.tx.Tx;
import com.mm.engine.framework.server.ServerType;
import com.mm.engine.framework.tool.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by apple on 16-10-4.
 * 推送消息有个主要解决的问题:不同账号登陆在不同的服务器上,必须由对应的服务器推送给它
 * 推送单个玩家原则:
 * 1、先推送自己服务器上的,
 * 2、如果可能存在其它服务器上的,则将其发送给mainServer
 * 3、mainServer分发给对应的服务器进行推送
 *
 * 广播和推送一个组
 *
 *
 */
@Service(init = "init",initPriority = 4)
public class SendMessageService {
    private static final Logger log = LoggerFactory.getLogger(SendMessageService.class);
    /**
     * 用于推送消息的推送器
     * 需要根据使用的协议、传输方式,在创建session的时候设置
     */
    private ConcurrentHashMap<String,MessageSender> messageSenderMap = new ConcurrentHashMap<>();
    /**
     * 组,一个组中可以存在多个accountId,accountId可以不在线
     */
    public ConcurrentHashMap<String,Set<String>> groupMap = new ConcurrentHashMap<>();

    private AccountSysService accountSysService;
    private RemoteCallService remoteCallService;
    private SendMessageGroupStorage sendMessageGroupStorage;
    private LockerService lockerService;

    public void init(){
        Map<String,Set<String>> map = sendMessageGroupStorage.getAllSendMessageGroup();
        groupMap.putAll(map);
    }
    // TODO 这个地方改成通过捕捉登陆登出事件来完成
    public void login(String accountId, Session session){
        MessageSender messageSender = session.getMessageSender();
        if(messageSender != null){
            messageSenderMap.put(accountId,messageSender);
        }
    }
    public void logout(String accountId,Session session){
        if(accountId != null) {
            messageSenderMap.remove(accountId);
        }
    }

    public void sendMessage(String accountId,int opcode,byte[] data){
        try {
            MessageSender messageSender = messageSenderMap.get(accountId);
            if(messageSender != null){
                messageSender.sendMessage(opcode, data);
            }else{
                // 发送给mainServer
                remoteCallService.remoteCallMainServerSyn(SendMessageService.class,"receiveSendMessage",accountId,opcode,data);
            }
        }catch (Throwable e){
            log.error("send message fail ,opcode = " + opcode+",e = "+e.getMessage()+",accountId+"+accountId);
        }
    }

    /**
     * 接收到其它服务器发送的sendMessage消息,要求对应账号登陆的服务器推送消息
     */
    public void receiveSendMessage(String accountId,int opcode,byte[] data){
        if(ServerType.isMainServer()){
            // mainServer查看是否在哪个服务器上面,并发送给它
            String add = accountSysService.getAccountLoginServerAdd(accountId);
            if(add != null && !Util.getLocalNetEventAdd().equals(add)){
                remoteCallService.remoteCallSyn(add,SendMessageService.class,"receiveSendMessage",accountId,opcode,data);
                return;
            }
        }
        // mainServer发送过来的消息,或者在本机上面运行的
        try {
            MessageSender messageSender = messageSenderMap.get(accountId);
            if(messageSender != null){
                messageSender.sendMessage(opcode, data);
            }else{
                log.warn("messageSender is not exist,accountId="+accountId);
            }
        }catch (Throwable e){
            log.error("send message fail ,opcode = " + opcode+",e = "+e.getMessage()+",accountId+"+accountId);
        }
    }
    @BroadcastRPC
    public void broadcastMessage(int opcode,byte[] data){
        for(Map.Entry<String,MessageSender> entry : messageSenderMap.entrySet()){
            doSendMessage(entry.getKey(),entry.getValue(),opcode,data);
        }
    }
    private void doSendMessage(String accountId,MessageSender messageSender,int opcode,byte[] data){
        try {
            messageSender.sendMessage(opcode, data);
        }catch (Throwable e){
            log.error("broadcast message fail ,opcode = " + opcode+",e = "+e.getMessage()+",accountId+"+accountId);
        }
    }
    @BroadcastRPC
    public void sendGroupMessage(String groupId,int opcode,byte[] data){
        Set<String> group = groupMap.get(groupId);
        if(group == null){
            log.warn("group is not exist ,groupId = "+groupId);
            return;
        }
        for(String accountId: group){
            MessageSender messageSender = messageSenderMap.get(accountId);
            if(messageSender != null){
                doSendMessage(accountId,messageSender,opcode,data);
            }
        }
    }
    // 对group的操作
    @Tx(lock = true,lockClass = {SendMessageGroup.class})
    public void putIntoGroup(String groupId,String accountId){
        Set<String> group = groupMap.get(groupId);
        if(group == null){
            groupMap.putIfAbsent(groupId, new HashSet<String>());
            group = groupMap.get(groupId);
            sendMessageGroupStorage.addGroup(groupId);
        }
        group.add(accountId);
        sendMessageGroupStorage.addAccount(groupId,accountId);
        remoteCallService.broadcastRemoteCallSyn(SendMessageService.class,"_putIntoGroup",groupId,accountId);
    }
    public void _putIntoGroup(String groupId,String accountId){
        Set<String> group = groupMap.get(groupId);
        if(group == null){
            groupMap.putIfAbsent(groupId, new HashSet<String>());
            group = groupMap.get(groupId);
        }
        group.add(accountId);
    }
    @Tx(lock = true,lockClass = {SendMessageGroup.class})
    public void removeOutGroup(String groupId,String accountId){
        Set<String> group = groupMap.get(groupId);
        if(group != null){
            group.remove(accountId);
            sendMessageGroupStorage.removeAccount(groupId,accountId);
        }else{
            log.warn("group is not exist while remove account ,groupId = {},accountId = {}",groupId,accountId);
        }
        remoteCallService.broadcastRemoteCallSyn(SendMessageService.class,"_removeOutGroup",groupId,accountId);
    }
    public void _removeOutGroup(String groupId,String accountId){
        Set<String> group = groupMap.get(groupId);
        if(group != null){
            group.remove(accountId);
        }else{
            log.warn("group is not exist while remove account ,groupId = {},accountId = {}",groupId,accountId);
        }
    }
    @Tx(lock = true,lockClass = {SendMessageGroup.class})
    public void removeGroup(String groupId){
        groupMap.remove(groupId);
        sendMessageGroupStorage.removeGroup(groupId);
        remoteCallService.broadcastRemoteCallSyn(SendMessageService.class,"_removeGroup",groupId);
    }
    public void _removeGroup(String groupId){
        groupMap.remove(groupId);
    }
}
