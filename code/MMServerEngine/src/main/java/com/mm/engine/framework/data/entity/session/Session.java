package com.mm.engine.framework.data.entity.session;

import com.mm.engine.framework.control.room.RoomMessageSender;
import com.mm.engine.framework.data.cache.CacheEntity;
import com.mm.engine.framework.data.entity.account.MessageSender;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Administrator on 2015/11/16.
 * Session中封装了和客户端访问相关的数据，是对所有访问种类@class EntranceType的统一封装：
 * 如，访问种类，访问url，访问port，访问者ip等信息
 *
 * 每一个Request都包含一个Session，
 *
 * TODO session放缓存的时候是不是需要把它作为CacheEntity的object，而不是继承自它
 */

public class Session extends CacheEntity{
    private String url;
    // sessionid的组成包括两部分，一是前缀，用来记录和登陆相关的一些信息，二是cacheEntity的id
    private final String sessionId;
    // session所对应的客户端,这个当客户端登陆的时候赋值,以便在后续的使用中从session中找到它
//    private SessionClient sessionClient;
    private String accountId;
    private final String ip;
    private final Date createTime;
    private Date lastUpdateTime;
    private MessageSender messageSender;
    private RoomMessageSender roomMessageSender;
    private ConnectionClose connectionClose;
    // 属性
    private Map<String,Object> attrs;

    public Session(String url, String sessionIdPrefix, String ip, Date createTime){
        this.url=url;
        //"Session_"+sessionIdPrefix;
        this.sessionId="Session_"+sessionIdPrefix;
        this.ip=ip;
        this.createTime=createTime;
        this.lastUpdateTime=createTime;
        attrs = new ConcurrentHashMap<>();
    }

    public Object getAttr(String key){
        return attrs.get(key);
    }
    public void setAttr(String key,Object object){
        this.attrs.put(key,object);
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getSessionId() {
        return sessionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        this.accountId = accountId;
    }

    public String getIp() {
        return ip;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setExpired() {
        lastUpdateTime.setTime(0);
    }

    public MessageSender getMessageSender() {
        return messageSender;
    }

    public void setMessageSender(MessageSender messageSender) {
        this.messageSender = messageSender;
    }

    public RoomMessageSender getRoomMessageSender() {
        return roomMessageSender;
    }

    public void setRoomMessageSender(RoomMessageSender roomMessageSender) {
        this.roomMessageSender = roomMessageSender;
    }

    public ConnectionClose getConnectionClose() {
        return connectionClose;
    }

    public void setConnectionClose(ConnectionClose connectionClose) {
        this.connectionClose = connectionClose;
    }

    public void closeConnect(){
        if(this.connectionClose != null){
            this.connectionClose.close();
        }
    }
}
