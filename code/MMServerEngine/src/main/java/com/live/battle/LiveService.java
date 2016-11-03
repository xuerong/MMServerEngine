package com.live.battle;

import com.mm.engine.framework.control.annotation.EventListener;
import com.mm.engine.framework.control.annotation.Request;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.event.EventData;
import com.mm.engine.framework.control.room.RoomService;
import com.mm.engine.framework.data.entity.account.LogoutEventData;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.tx.LockTask;
import com.mm.engine.framework.data.tx.LockerService;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.util.Util;
import com.protocol.LiveOpcode;
import com.protocol.LivePB;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/10/13.
 */
@Service
public class LiveService {
    private RoomService roomService;
    private LockerService lockerService;
    // 记录玩家创建的房间
    private Map<String,List<Integer>> roomCreator = new ConcurrentHashMap<>();

    public void closeRoom(final BattleRoom battleRoom){
        lockerService.doLockTask(new LockTask<Object>() {
            @Override
            public Object run() {
                List<Integer> roomList = roomCreator.get(battleRoom.getCreatorAccountId());
                if(roomList != null){
                    roomList.remove(battleRoom.getId());
                    if(roomList.size()<= 0 ){
                        roomCreator.remove(battleRoom.getCreatorAccountId());
                    }
                }
                roomService.removeRoom(battleRoom.getId());
                return null;
            }
        },"account_"+battleRoom.getCreatorAccountId());
    }
    @Request(opcode = LiveOpcode.CSCreateLiveRoom)
    public RetPacket doCreateRoom(Object clientData,final Session session){

        BattleRoom room = lockerService.doLockTask(new LockTask<BattleRoom>() {
            @Override
            public BattleRoom run() {
                BattleRoom room = roomService.createRoom(BattleRoom.class);
                room.setCreatorAccountId(session.getAccountId());
                List<Integer> list = roomCreator.get(session.getAccountId());
                if(list == null){
                    list = new ArrayList<>();
                    roomCreator.put(session.getAccountId(),list);
                }
                list.add(room.getId());
                return room;
            }
        },"account_"+session.getAccountId());

        LivePB.SCCreateLiveRoom.Builder builder = LivePB.SCCreateLiveRoom.newBuilder();
        builder.setRoomId(room.getId());
        builder.setHost(Util.getHostAddress());
        builder.setPort(Server.getEngineConfigure().getRoomPort());
        RetPacket ret = new RetPacketImpl(LiveOpcode.SCCreateLiveRoom,builder.build().toByteArray());
        System.out.println("create room :"+room.getId());
        return ret;
    }
    @Request(opcode = LiveOpcode.CSGetRoomList)
    public RetPacket getRoomList(Object clientData, Session session){
        List<BattleRoom> roomList = roomService.getRoomList(BattleRoom.class);
        LivePB.SCGetRoomList.Builder builder = LivePB.SCGetRoomList.newBuilder();
        for(BattleRoom battleRoom : roomList){
            LivePB.PBRoomInfo.Builder ri = LivePB.PBRoomInfo.newBuilder();
            ri.setRoomId(battleRoom.getId());
            ri.setMember(battleRoom.getMemberCount(true));
            ri.setStatus(battleRoom.getRoomStatus());
            ri.setHost(Util.getHostAddress());
            ri.setPort(Server.getEngineConfigure().getRoomPort()); // 获取的是本机器的port，显然不应该，而是要获取
            builder.addRoomInfo(ri);
        }
        RetPacket ret = new RetPacketImpl(LiveOpcode.SCGetRoomList,builder.build().toByteArray());
        return ret;
    }

    @EventListener(event = SysConstantDefine.Event_AccountLogout)
    public void onAccountLogout(EventData data){
        // 当玩家登出的时候，要查看该玩家所创建的房间
        final LogoutEventData logoutEventData = (LogoutEventData)data.getData();

        final String accountId= logoutEventData.getSession().getAccountId();
        if(accountId == null){
            return;
        }
        lockerService.doLockTask(new LockTask<Object>() {
            @Override
            public Object run() {
                List<Integer> list = roomCreator.remove(accountId);
                System.out.println("onAccountLogout,"+accountId+","+list);
                if(list != null && list.size()>0){
                    for(Integer roomId : list){
                        BattleRoom room = (BattleRoom)roomService.getRoom(roomId);
                        roomService.outRoom(roomId,logoutEventData.getSession());
                        if(room.getMemberCount(false)<=0) { // 问题在于这里
                            roomService.removeRoom(roomId);
                        }
                    }
                }
                return null;
            }
        },"account_"+accountId);
    }
}
