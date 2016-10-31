package com.mm.engine.framework.control.room;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.IdService;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.helper.ClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/9/14.
 * 场景服务
 *
 * 创建房间
 * 销毁房间
 *
 * 获取房间列表
 *
 * 加入房间
 * 离开房间
 *
 * 房间消息
 *
 *
 * room但服务器，多服务器的问题需要考虑呀
 */
@Service(init = "init")
public class RoomService {
    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private ConcurrentHashMap<Integer,Room> roomMap;
    private ConcurrentHashMap<Class,List<Room>> roomListMap;
    private ConcurrentHashMap<String,ConcurrentHashMap<Integer,Room>> accountRoomListMap; // 玩家所在的房间

    private IdService idService;

    public void init(){
        idService = BeanHelper.getServiceBean(IdService.class);
        roomMap = new ConcurrentHashMap<>();
        roomListMap = new ConcurrentHashMap<>();
        accountRoomListMap = new ConcurrentHashMap<>();
        List<Class<?>> roomClassList = ClassHelper.getClassListBySuper(Room.class);
        for(Class cls: roomClassList){
            roomListMap.put(cls,new ArrayList<Room>());
        }
    }
    public RetPacket handle(Session session, int roomId, int opcode, byte[] data) throws Throwable{
        Room room = roomMap.get(roomId);
        if(room == null){
            throw new MMException("room is not exist");
        }
        RetPacket retData = room.handle(session,opcode,data);
        return retData;
    }
    //----
    public <T> T createRoom(Class<T> cls){
        List<Room> roomList = roomListMap.get(cls);
        if(roomList == null){
            throw new MMException("create room error,"+cls.getName()+"is not Room");
        }
        Room room = (Room)BeanHelper.newAopInstance(cls);
        room.setId(idService.acquireInt(Room.class));
        room.init();
        roomMap.put(room.getId(), room);
        roomList.add(room);
        return (T) room;
    }

    /**
     * 是否进入成功，如果之前就在房间中返回false
     * @param roomId
     * @param session
     * @return
     */
    public boolean enterRoom(int roomId,Session session){
        Room room = getRoom(roomId);
        if(room == null){
            throw new MMException("room is not exist");
        }
        RoomAccount roomAccount = room.getRoomAccount(session.getAccountId());
        if(roomAccount != null){
            return false;
        }
        ConcurrentHashMap<Integer,Room> roomMap = accountRoomListMap.get(session.getAccountId());
        if(roomMap == null){
            ConcurrentHashMap<Integer,Room> _roomMap = new ConcurrentHashMap<>();
            accountRoomListMap.putIfAbsent(session.getAccountId(),_roomMap);
            roomMap = accountRoomListMap.get(session.getAccountId());
        }
        Room oldRoom = roomMap.putIfAbsent(room.getId(),room);
        if(oldRoom != null){
            log.warn("account is already in room,roomId="+roomId);
            return false;
        }
        room.enterRoom(session);
        return true;
    }

    /**
     * 返回是否还在房间中，即还存在某个房间，玩家在里面
     * @param roomId
     * @param session
     * @return
     */
    public boolean outRoom(int roomId,Session session){
        Room room = getRoom(roomId);
        if(room == null){
            throw new MMException("room is not exist");
        }
        ConcurrentHashMap<Integer,Room> roomMap = accountRoomListMap.get(session.getAccountId());
        if(roomMap != null){
            roomMap.remove(roomId);
        }else{
            log.warn("roomMap == null while outRoom,roomId="+roomId+",accountId="+session.getAccountId()+",sessionId="+session.getSessionId());
            return false;
        }
        room.outRoom(session);
        int size = roomMap.size();
        if(size == 0){
            accountRoomListMap.remove(session.getAccountId());
        }
        return size>0;
    }
    public Room getRoom(int id){
        return roomMap.get(id);
    }
    public <T> List<T> getRoomList(Class<T> cls){
        List roomList = roomListMap.get(cls);
        if(roomList == null){
            throw new MMException("getRoomList error,"+cls.getName()+"is not Room");
        }
        return roomList;
    }

    public Room removeRoom(int id){
        Room room = roomMap.remove(id);
        if(room == null){
            log.warn("room is not exist while removeRoom,roomId = "+id);
        }else {
            List<Room> roomList = roomListMap.get(room.getClass());
            roomList.remove(room);
            room.destroy();
            idService.releaseInt(Room.class, room.getId());
        }
        return room;
    }

    public void netDisconnect(Session session){
        // 所咋的room都要通知，但不一定是离开房间
        ConcurrentHashMap<Integer,Room> roomMap = accountRoomListMap.remove(session.getAccountId());
        if(roomMap!=null && roomMap.size()>0){
            for(Room room:roomMap.values()){
                room.disConnect(session);
            }
        }
    }
}
