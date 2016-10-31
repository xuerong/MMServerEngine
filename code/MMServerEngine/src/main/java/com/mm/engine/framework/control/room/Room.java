package com.mm.engine.framework.control.room;

import com.mm.engine.framework.data.DataService;
import com.mm.engine.framework.data.entity.account.Account;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.*;

/**
 * Created by a on 2016/9/14.
 *
 * 场景是一个全缓存服务对象，
 * 客户端通过场景入口进行访问，然后通过SceneService进行分配
 * 同一个场景中的玩家需要都通过场景入口连接场景所在服务器
 *
 * 一下场合适合用场景：
 * 1、实时性要求较高的，如实时战斗
 * 2、多人实时互动，如聊天室
 *
 * 场景支持AOP，但不鼓励使用，尤其是要求效率较高的场合
 *
 *
 * 进入房间
 * 离开房间
 * 处理消息
 *
 * 广播
 */
public abstract class Room<T extends RoomAccount> {
    private static final Logger log = LoggerFactory.getLogger(Room.class);

    private static final ThreadPoolExecutor executor = new ThreadPoolExecutor(
            10,100,3000, TimeUnit.MILLISECONDS,new LinkedBlockingDeque<Runnable>(),
            new RejectedExecutionHandler(){
                @Override
                public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
                    // 拒绝执行
                }
            });

    protected int id;// 房间id
    protected String name; // 房间的名字
    protected RoomAccount host; // 房间主人的accountId
    protected ConcurrentHashMap<String,RoomAccount> accountMap; // 房间内的人

    private DataService dataService;

    protected void init(){
        dataService = BeanHelper.getServiceBean(DataService.class);
        accountMap = new ConcurrentHashMap<>();
        onInit();
    }
    protected void destroy(){
        // 比如通知所有玩家房间关闭
        onDestroy();
    }

    public RoomAccount getRoomAccount(String accountId){
        return accountMap.get(accountId);
    }
    public void enterRoom(Session session){
        Account account =dataService.selectObject(Account.class,"id="+session.getAccountId());
        if(account == null){
            throw new MMException("enterRoom account == null,id="+session.getAccountId());
        }

        RoomAccount roomAccount = new RoomAccount(account);
        roomAccount.setMessageSender(session.getRoomMessageSender());
        beforePeopleEnterRoom(roomAccount);
        RoomAccount oldRoomAccount = accountMap.putIfAbsent(roomAccount.getAccountId(),roomAccount);
        if(oldRoomAccount!=null){
            log.warn("already in the room,accountId="+account.getId()+",roomId="+this.id);
        }else{
            afterPeopleEnterRoom(roomAccount);
        }
    }


    /**
     * 如果从房间离开，返回true，如果不在房间内，返回false
     * @param session
     * @return
     */
    public boolean outRoom(Session session){
        RoomAccount roomAccount = accountMap.get(session.getAccountId());
        if(roomAccount == null){
            log.warn("outRoom account == null,id="+session.getAccountId());
            return false;
        }
        beforePeopleOutRoom(roomAccount);
        roomAccount = accountMap.remove(session.getAccountId());
        afterPeopleOutRoom(roomAccount);
        if(roomAccount == host){
            host = null;
        }
        return true;
    }

    public void disConnect(Session session){
        RoomAccount roomAccount = accountMap.remove(session.getAccountId());
        if(roomAccount == null){
            throw new MMException("outRoom account == null,id="+session.getAccountId());
        }
        onDisconnection(roomAccount);
    }

    public void broadcast(final int opcode,final byte[] data){
        executor.execute(new Runnable() {
            @Override
            public void run() {
                broadcastSyn(opcode,data);
            }
        });
    }
    public void broadcastSyn(final int opcode,final byte[] data){
        for(RoomAccount roomAccount : accountMap.values()){
            try {
                roomAccount.getMessageSender().sendMessage(opcode,id, data);
            }catch (Throwable e){
                log.error("room send message error,roomId="+this.id+",accountId="+roomAccount.getAccountId()+",cause="+e.getMessage());
            }
        }
    }

    public abstract void onInit();

    public abstract RetPacket handle(Session session, int opcode, byte[] data) throws Throwable;

    public abstract void onDestroy();
    public abstract void beforePeopleEnterRoom(RoomAccount roomAccount);
    public abstract void afterPeopleEnterRoom(RoomAccount roomAccount);
    public abstract void beforePeopleOutRoom(RoomAccount roomAccount);
    public abstract void afterPeopleOutRoom(RoomAccount roomAccount);

    public abstract void onDisconnection(RoomAccount roomAccount);


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public RoomAccount getHost() {
        return host;
    }

    public void setHost(RoomAccount host) {
        this.host = host;
    }

    public ConcurrentHashMap<String, RoomAccount> getAccountMap() {
        return accountMap;
    }

    public void setAccountMap(ConcurrentHashMap<String, RoomAccount> accountMap) {
        this.accountMap = accountMap;
    }
}
