package com.mm.engine.framework.control.room;

import com.mm.engine.framework.data.entity.account.Account;
import com.mm.engine.framework.data.entity.account.MessageSender;
import com.mm.engine.framework.data.entity.session.Session;

import java.sql.Timestamp;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by a on 2016/10/11.
 * 房间中的玩家的信息记录
 * 只需要记录account一部分信息，也包括其他需要记录的信息
 */
public class RoomAccount {

    private String accountId;
    private String name;
    private Timestamp enterTime;
    private RoomRole roomRole;
    private RoomMessageSender messageSender;

    private ConcurrentHashMap<Object,Object> attrs = new ConcurrentHashMap<>();

    public RoomAccount(Account account){
        this(account,RoomRole.Normal);
    }
    public RoomAccount(Account account,RoomRole roomRole){
        this.accountId = account.getId();
        this.name = account.getName();
        this.enterTime = new Timestamp(System.currentTimeMillis());
        this.roomRole = roomRole;
    }

    public Object getAttr(Object key){
        return attrs.get(key);
    }
    public void setAttr(Object key,Object value){
        attrs.put(key,value);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Timestamp getEnterTime() {
        return enterTime;
    }

    public void setEnterTime(Timestamp enterTime) {
        this.enterTime = enterTime;
    }

    public RoomRole getRoomRole() {
        return roomRole;
    }

    public void setRoomRole(RoomRole roomRole) {
        this.roomRole = roomRole;
    }

    public RoomMessageSender getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(RoomMessageSender messageSender) {
        this.messageSender = messageSender;
    }
}
