package com.mm.engine.framework.data.entity.session;

import com.mm.engine.framework.data.SynchronousLevel;
import com.mm.engine.framework.data.cache.CacheEntity;
import com.mm.engine.framework.entrance.NetType;

import java.util.Date;

/**
 * Created by Administrator on 2015/11/16.
 * Session中封装了和客户端访问相关的数据，是对所有访问种类@class EntranceType的统一封装：
 * 如，访问种类，访问url，访问port，访问者ip等信息
 *
 * 每一个Request都包含一个Session，在Protocol解码中需要解出sessionId和opcode，由系统创建session
 */

public final class Session extends CacheEntity {
    private NetType netType;
    private String url;
    // sessionid的组成包括两部分，一是前缀，用来记录和登陆相关的一些信息，二是cacheEntity的id
    private final String sessionId;
    private SessionClient sessionClient;
    private final String ip;
    private final Date createTime;
    private Date lastUpdateTime;
    // session是否过期，入登出时，设置为过期

    public Session(NetType netType, String url, String sessionIdPrefix, String ip, Date createTime){
        super(SynchronousLevel.NoSync);

        this.netType = netType;
        this.url=url;
        this.sessionId=sessionIdPrefix+"_"+ getCacheId();
        this.ip=ip;
        this.createTime=createTime;
        this.lastUpdateTime=createTime;
    }

    public String getSessionId() {
        return sessionId;
    }

    public SessionClient getSessionClient() {
        return sessionClient;
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

    public void setSessionClient(SessionClient sessionClient) {
        this.sessionClient = sessionClient;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    public void setExpired() {
        lastUpdateTime.setTime(0);
    }
}
