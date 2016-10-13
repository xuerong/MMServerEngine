package com.live.battle;

import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.control.room.RoomService;

/**
 * Created by a on 2016/10/13.
 */
@Service
public class LiveService {
    private RoomService roomService;
    public void closeRoom(BattleRoom battleRoom){
        roomService.removeRoom(battleRoom.getId());
    }
}
