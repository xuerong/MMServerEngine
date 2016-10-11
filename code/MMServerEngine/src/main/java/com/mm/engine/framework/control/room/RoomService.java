package com.mm.engine.framework.control.room;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.security.exception.MMException;
import com.mm.engine.framework.server.IdService;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.helper.ClassHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
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
 */
@Service(init = "init")
public class RoomService {
    private static final Logger log = LoggerFactory.getLogger(RoomService.class);
    private ConcurrentHashMap<Integer,Room> roomMap;
    private ConcurrentHashMap<Class,List<Room>> roomListMap;

    private IdService idService;

    public void init(){
        idService = BeanHelper.getServiceBean(IdService.class);
        roomMap = new ConcurrentHashMap<>();
        roomListMap = new ConcurrentHashMap<>();
        List<Class<?>> roomClassList = ClassHelper.getClassListBySuper(Room.class);
        for(Class cls: roomClassList){
            roomListMap.put(cls,new ArrayList<Room>());
        }
    }
    public Object handle(Session session,int roomId, int opcode, Object data){
        Room room = roomMap.get(roomId);
        if(room == null){
            throw new MMException("room is not exist");
        }
        Object retData = room.handle(session,opcode,data);
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
}
