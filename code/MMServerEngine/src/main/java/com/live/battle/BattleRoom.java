package com.live.battle;

import com.mm.engine.framework.control.room.Room;
import com.mm.engine.framework.control.room.RoomAccount;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.mm.engine.framework.security.exception.ToClientException;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import com.protocol.LiveOpcode;
import com.protocol.LivePB;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by a on 2016/10/13.
 * 战斗房间：
 * 房间中有多个玩家，包括玩家和电脑，
 */
public class BattleRoom extends Room {
    public static final int roomSizeWidth = 1000;
    public static final int roomSizeHeight = 600;

    private static final Logger log = LoggerFactory.getLogger(BattleRoom.class);
    private ConcurrentHashMap<String,BattleUser> battleUserMap;
    private AtomicInteger userCount = new AtomicInteger(0); // 真实玩家的数量，不算机器人
    private List<BattleUser> dieBattleUserList; // 有顺序的，先进入的先死
    private ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
    private int roomStatus=0;//0：未开始，1进行中，2结束

    private String creatorAccountId;

    @Override
    public void onInit() {
        battleUserMap = new ConcurrentHashMap<>();
        dieBattleUserList = new Vector<>();
        // 加一个机器人
//        doSetRobot(2);
    }
    @Override
    public RetPacket handle(Session session, int opcode, byte[] data)  throws Throwable{
        BattleUser battleUser = getBattleBySession(session);
        switch (opcode){
            case LiveOpcode.CSStart:return doCsStart(battleUser,data);
            case LiveOpcode.CSSetRobot:return doCsSetRobot(battleUser,data);
            case LiveOpcode.CSMoveTo:return doCsMoveTo(battleUser,data);
            case LiveOpcode.CSAttack:return doCsAttack(battleUser,data);
        }
        return null;
    }
    public int getMemberCount(boolean withRobot){
        if(withRobot){
            return battleUserMap.size();
        }
        return accountMap.size();
    }
    private RetPacket doCsStart(BattleUser battleUser,byte[] data) throws Throwable{
        System.out.println("doCsStart--"+battleUser.getAccountId());
        if(battleUser.getAccountId() != host.getAccountId()){
            throw new ToClientException("you are not host");
        }
        int count = battleUserMap.size();
        if(count <= 1){
            throw new ToClientException("user count is not enough,count = "+count);
        }
        long time = System.currentTimeMillis();
        // 计算并分配位置
        roomStatus = 1;
        int interval = (roomSizeWidth*2+roomSizeHeight*2)/count;
        for(BattleUser user : battleUserMap.values()){
            int dis = (count-1)*interval;
            int x=0,y=0;
            if(dis<=roomSizeWidth){
                y = 0;
                x = dis;
            }else if(dis<=roomSizeHeight+roomSizeWidth){
                x = roomSizeWidth;
                y = dis-roomSizeWidth;
            }else if(dis<=roomSizeWidth*2+roomSizeHeight){
                x=roomSizeWidth - (dis-roomSizeHeight-roomSizeWidth);
                y=roomSizeHeight;
            }else{
                x=0;
                y=roomSizeHeight-(dis-roomSizeWidth*2-roomSizeHeight);
            }
            user.start(x,y,time);
            count--;
        }
        // TODO 测试一下，房间移除之后，这些还在不在，应该不在是对的
        executor.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("scheduleAtFixedRate lost blood");
                LivePB.SCDie.Builder dieBuilder = null;
                for(BattleUser user : battleUserMap.values()){
                    if(user.isDie()){
                        continue;
                    }
                    int blood = user.lostBlood();
//                    log.info(user.getAccountId()+" blood:"+blood);
                    if(user.isDie()){
                        // 广播死亡消息
                        if(dieBuilder == null){
                            dieBuilder = LivePB.SCDie.newBuilder();
                        }
                        doDie(user);
                        dieBuilder.addAccountId(user.getAccountId());
                    }
                }
                if(dieBuilder != null){
                    Map<String,RoomAccount> map = accountMap;
                    byte[] retDate = dieBuilder.build().toByteArray();
                    for(RoomAccount roomAccount : map.values()){
                        try {
                            roomAccount.getMessageSender().sendMessage(LiveOpcode.SCDie,id, retDate);
                        }catch (Throwable e){
                            log.error("broadcast die error , accountId = "+roomAccount.getAccountId());
                        }
                    }
                }
            }
        },10,10, TimeUnit.SECONDS);
        // 推送给所有的玩家，游戏开始了
        LivePB.SCStartGame.Builder startB = LivePB.SCStartGame.newBuilder();
        for(BattleUser bu : battleUserMap.values()){
            PosInfo posInfo = bu.getPosInfo();
            LivePB.PBStatus.Builder pb = LivePB.PBStatus.newBuilder();
            pb.setDirection(posInfo.getDirection());
            pb.setX(posInfo.getX());
            pb.setY(posInfo.getY());
            pb.setAccountId(bu.getAccountId());
            pb.setBlood(bu.getBlood());
            pb.setSpeed((int)bu.getSpeed());
            startB.addStatus(pb);
        }
        byte[] startBData = startB.build().toByteArray();
        Map<String,RoomAccount> map = accountMap;
        for(RoomAccount roomAccount : map.values()){
            roomAccount.getMessageSender().sendMessage(LiveOpcode.SCStartGame,id,startBData);
        }
        //
        LivePB.SCStart.Builder builder = LivePB.SCStart.newBuilder();
        RetPacket retPacket = new RetPacketImpl(LiveOpcode.SCStart,builder.build().toByteArray());
        return retPacket;
    }

    private RetPacket doCsSetRobot(BattleUser battleUser,byte[] data) throws Throwable{
        if(roomStatus != 0){
            throw new ToClientException("游戏已经刚开始，不能进入房间");
        }
        if(battleUser.getAccountId() != host.getAccountId()){
            throw new ToClientException("you are not host");
        }
        LivePB.CSSetRobot csSetRobot = LivePB.CSSetRobot.parseFrom(data);
        int count = csSetRobot.getCount();
        doSetRobot(count);
        LivePB.SCSetRobot.Builder builder = LivePB.SCSetRobot.newBuilder();
        RetPacket retPacket = new RetPacketImpl(LiveOpcode.SCSetRobot,builder.build().toByteArray());
        return retPacket;
    }

    private void doSetRobot(int count){
        // 移除所有的机器人
        Iterator<BattleUser> it = battleUserMap.values().iterator();
        while(it.hasNext()){
            BattleUser user = it.next();
            if(user.isRobot()){
                it.remove();
            }
        }
        for(int i=0;i<count;i++){
            String accountId = "robot"+i;
            while(battleUserMap.containsKey(accountId)){
                accountId+="_";
            }
            BattleUser newUser = new BattleUser(true,accountId);
            addBattleUser(newUser);
        }
        broadcastRoomChange();
    }

    private RetPacket doCsMoveTo(BattleUser battleUser,byte[] data) throws Throwable{
        if(battleUser.isDie()){ // 如果已经死亡
            LivePB.PBStatus.Builder pb = LivePB.PBStatus.newBuilder();
            pb.setDirection(-1);
            pb.setX(0);
            pb.setY(0);
            pb.setAccountId(battleUser.getAccountId());
            pb.setBlood(battleUser.getBlood());
            pb.setSpeed(0);
            //
            LivePB.SCMoveTo.Builder builder = LivePB.SCMoveTo.newBuilder();
            builder.setStatus(pb);
            RetPacket retPacket = new RetPacketImpl(LiveOpcode.SCMoveTo,builder.build().toByteArray());
            return retPacket;
        }
        LivePB.CSMoveTo csMoveTo = LivePB.CSMoveTo.parseFrom(data);
        float direction = csMoveTo.getDirection();
        //
        PosInfo posInfo = battleUser.updateDirection(direction);
        LivePB.PBStatus.Builder pb = LivePB.PBStatus.newBuilder();
        pb.setDirection(posInfo.getDirection());
        pb.setX(posInfo.getX());
        pb.setY(posInfo.getY());
        pb.setAccountId(battleUser.getAccountId());
        pb.setBlood(battleUser.getBlood());
        pb.setSpeed((int)battleUser.getSpeed());
        //
        LivePB.SCMoveTo.Builder builder = LivePB.SCMoveTo.newBuilder();
        builder.setStatus(pb);
        // 移动推送,不推送自己
        LivePB.SCStatus.Builder scBuilder = LivePB.SCStatus.newBuilder();
        scBuilder.setStatus(pb);
        Map<String,RoomAccount> map = accountMap;
        byte[] retDate = scBuilder.build().toByteArray();
        for(RoomAccount roomAccount : map.values()){
            if(roomAccount.getAccountId()!=battleUser.getAccountId()){
                roomAccount.getMessageSender().sendMessage(LiveOpcode.SCStatus,id,retDate);
//                System.out.println("csmove--"+roomAccount.getAccountId());
            }
        }

        RetPacket retPacket = new RetPacketImpl(LiveOpcode.SCMoveTo,builder.build().toByteArray());
        return retPacket;
    }
    private RetPacket doCsAttack(BattleUser battleUser,byte[] data) throws Throwable{
        PosInfo posInfo = battleUser.getPosInfo();
        // 计算被打击的玩家:遍历所有的玩家，计算两者之间的距离，在此范围内则定位被打击
        List<BattleUser> beAttackUserList = null;
        for(BattleUser user : battleUserMap.values()){
            if(user.getAccountId()!=battleUser.getAccountId() && !user.isDie()){
                PosInfo pos = user.getPosInfo();
                int dis = (int)Math.sqrt((posInfo.getX()-pos.getX())*(posInfo.getX()-pos.getX())+
                        (posInfo.getY()-pos.getY())*(posInfo.getY()-pos.getY()));
                log.info("attack,accountId="+user.getAccountId()+",dis:"+dis);
                if(dis<=battleUser.getAttackDistance()){
                    // 被打击，掉血或死亡
                    user.beAttack(battleUser.getAttackValue());
                    if(user.isDie()){
                        doDie(user);
                    }
                    battleUser.suckBlood(); // 吸血
                    if(beAttackUserList == null){
                        beAttackUserList = new ArrayList<>();
                    }
                    beAttackUserList.add(user);
                }
            }
        }
        // 打击推送,推送自己
        LivePB.SCAttackResult.Builder scAttackBuilder = LivePB.SCAttackResult.newBuilder();
        if(beAttackUserList != null && beAttackUserList.size()>0) {
            for (BattleUser user : beAttackUserList) {
                scAttackBuilder.addBeAttack(battleUserTopPStatus(user));
            }
        }
        LivePB.PBStatus.Builder pb = LivePB.PBStatus.newBuilder();
        pb.setDirection(posInfo.getDirection());
        pb.setX(posInfo.getX());
        pb.setY(posInfo.getY());
        pb.setAccountId(battleUser.getAccountId());
        pb.setBlood(battleUser.getBlood());
        pb.setSpeed((int)battleUser.getSpeed());
        scAttackBuilder.setAttack(pb);
        Map<String,RoomAccount> map = accountMap;
        byte[] retDate = scAttackBuilder.build().toByteArray();
        for(RoomAccount roomAccount : map.values()){
            roomAccount.getMessageSender().sendMessage(LiveOpcode.SCAttackResult,id,retDate);
        }
        //
        LivePB.SCAttack.Builder builder = LivePB.SCAttack.newBuilder();
        RetPacket retPacket = new RetPacketImpl(LiveOpcode.SCAttack,builder.build().toByteArray());
        return retPacket;
    }

    private LivePB.PBStatus.Builder battleUserTopPStatus(BattleUser battleUser){
        PosInfo posInfo = battleUser.getPosInfo();
        LivePB.PBStatus.Builder pb = LivePB.PBStatus.newBuilder();
        pb.setDirection(posInfo.getDirection());
        pb.setX(posInfo.getX());
        pb.setY(posInfo.getY());
        pb.setAccountId(battleUser.getAccountId());
        pb.setBlood(battleUser.getBlood());
        pb.setSpeed((int)battleUser.getSpeed());
        return pb;
    }

    @Override
    public void onDestroy() {
        log.info("room destroy , roomId="+id);
        if(!executor.isShutdown()) {
            executor.shutdownNow();
        }
    }

    @Override
    public void beforePeopleEnterRoom(RoomAccount roomAccount) {
        if(host == null && roomAccount.getAccountId()!= creatorAccountId){
            throw new ToClientException("host has not into room");
        }
    }

    @Override
    public void afterPeopleEnterRoom(RoomAccount roomAccount) {
        BattleUser battleUser = new BattleUser(false,roomAccount.getAccountId());
        addBattleUser(battleUser);
        System.out.println("people add:"+roomAccount.getAccountId());
        if(host == null){
            System.out.println("set host:"+roomAccount.getAccountId());
            host = roomAccount;
        }
        broadcastRoomChange();
    }

    @Override
    public void beforePeopleOutRoom(RoomAccount roomAccount) {

    }

    private void addBattleUser(BattleUser battleUser){
        if(roomStatus != 0){
            throw new ToClientException("游戏已经刚开始，不能进入房间");
        }
        if(battleUserMap.containsKey(battleUser.getAccountId())) {
            throw new ToClientException("你已经在房间中");
        }
        battleUserMap.put(battleUser.getAccountId(),battleUser);
        userCount.getAndIncrement();
    }

    private void broadcastRoomChange(){
        LivePB.SCRoomChange.Builder builder = LivePB.SCRoomChange.newBuilder();
        LivePB.PBRoomInfo.Builder ri = LivePB.PBRoomInfo.newBuilder();
        ri.setRoomId(id);
        ri.setMember(getMemberCount(true));
        ri.setStatus(roomStatus);
        ri.setHost(Util.getHostAddress());
        ri.setPort(Server.getEngineConfigure().getRoomPort());
        builder.setRoomInfo(ri);
        // 推送给所有人房间的新状态
        byte[] retDate = builder.build().toByteArray();
        broadcast(LiveOpcode.SCRoomChange,retDate);
    }

    @Override
    public void afterPeopleOutRoom(RoomAccount roomAccount) {
//        if(roomStatus != 0){
//            throw new ToClientException("room has start , cant out room,roomStatus = "+roomStatus);
//        }else{
//
//        }
        System.out.println(roomAccount.getAccountId()+" outRoom");
        BattleUser battleUser = battleUserMap.remove(roomAccount.getAccountId());
        if(battleUser == null){
            throw new ToClientException("you are not in the room ,roomId = "+id);
        }
        // 如果房间里面没有别人，直接关闭房间，否则，如果是房主，关闭房间(或者更换房主)，否则，直接离开
        if(battleUserMap.size() == 0){ // 如果是最后一个人往往也是房主
            LiveService liveService = BeanHelper.getServiceBean(LiveService.class);
            liveService.closeRoom(this);
        }else if(battleUser.getAccountId() == host.getAccountId()){
            if(battleUserMap.size()>0){
                LivePB.SCRoomClose.Builder builder = LivePB.SCRoomClose.newBuilder();
                builder.setReason(1);
                broadcast(LiveOpcode.SCRoomClose,builder.build().toByteArray());
            }
            LiveService liveService = BeanHelper.getServiceBean(LiveService.class);
            liveService.closeRoom(this);
        }else{
            if(roomStatus != 0){
                doDie(battleUser);
            }else{
                broadcastRoomChange();
            }
        }
    }

    @Override
    public void onDisconnection(RoomAccount roomAccount) {
        System.out.println(roomAccount.getAccountId()+" disconnection");
        BattleUser battleUser = battleUserMap.remove(roomAccount.getAccountId());
        if(battleUserMap.size()<=0){
            // 相当于死亡
            doDie(battleUser);
        }
    }

    private void doDie(BattleUser battleUser){
        if(!dieBattleUserList.contains(battleUser)) {
            dieBattleUserList.add(battleUser);

            if(dieBattleUserList.size() == battleUserMap.size()-1){
                // 结束
                roomStatus = 2;
                StringBuilder accountIds = new StringBuilder();
                int count = dieBattleUserList.size();
                for(;count>0;count--){
                    BattleUser user = dieBattleUserList.get(count-1);
                    accountIds.append(","+user.getAccountId());
                    battleUserMap.remove(user.getAccountId());
                }
                for(BattleUser user:battleUserMap.values()){
                    accountIds.insert(0,user.getAccountId());
                }
                LivePB.SCOver.Builder builder = LivePB.SCOver.newBuilder();
                builder.setAccountIds(accountIds.toString());
                Map<String,RoomAccount> map = accountMap;
                byte[] retDate = builder.build().toByteArray();
                for(RoomAccount roomAccount : map.values()){
                    try {
                        roomAccount.getMessageSender().sendMessage(LiveOpcode.SCOver,id, retDate);
                    }catch (Throwable e){
                        log.error("send SCOver error,accountId="+roomAccount.getAccountId());
                    }
                }
                LiveService liveService = BeanHelper.getServiceBean(LiveService.class);
                liveService.closeRoom(this);
            }
        }
    }

    private BattleUser getBattleBySession(Session session){
        BattleUser battleUser = battleUserMap.get(session.getAccountId());
        if(battleUser == null){
            throw new ToClientException("you are not in room");
        }
        return battleUser;
    }

    public void setCreatorAccountId(String creatorAccountId) {
        this.creatorAccountId = creatorAccountId;
    }
    public String getCreatorAccountId() {
        return creatorAccountId;
    }

    public int getRoomStatus() {
        return roomStatus;
    }

    public void setRoomStatus(int roomStatus) {
        this.roomStatus = roomStatus;
    }
}
