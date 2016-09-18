package com.mm.engine.framework.data.entity;

import com.mm.engine.framework.data.entity.session.SessionClient;

import java.sql.Timestamp;
import java.util.Date;

/**
 * Created by Administrator on 2016/1/4.
 * 账号的抽象类,实际使用可继承自该类,也可不继承自该类,
 * 但要实现SessionClient接口,并在客户端登陆的时候将相应的对象放入session,以便系统在传给其它服务的
 * session中存有该账号的信息
 */
public abstract class AbAccount implements SessionClient {
    //
    protected String id;
    protected String name;
    protected String icon;
    protected String clientVersion;//客户端版本号

    protected Timestamp createTime;
    protected Timestamp lastLoginTime;
    protected Timestamp lastLogoutTime;

    // 玩家注册相关信息
    protected int channelId; // 渠道
    protected String uid; // 渠道对应的id，如微信，或手机mid，或手机号


    // 玩家登陆统计信息
    protected String area; // 登录地区名称
    protected String country; // 登录国家名称
    protected String device; // 登录设备名称
    protected String deviceSystem; // 登录系统名称
    protected String networkType; // 联网类型名称
    protected String prisonBreak; // 是否越狱(0:否 1:是)
    protected String operator; // 运营商名称

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getClientVersion() {
        return clientVersion;
    }

    public void setClientVersion(String clientVersion) {
        this.clientVersion = clientVersion;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public Timestamp getLastLoginTime() {
        return lastLoginTime;
    }

    public void setLastLoginTime(Timestamp lastLoginTime) {
        this.lastLoginTime = lastLoginTime;
    }

    public Timestamp getLastLogoutTime() {
        return lastLogoutTime;
    }

    public void setLastLogoutTime(Timestamp lastLogoutTime) {
        this.lastLogoutTime = lastLogoutTime;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getArea() {
        return area;
    }

    public void setArea(String area) {
        this.area = area;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getDevice() {
        return device;
    }

    public void setDevice(String device) {
        this.device = device;
    }

    public String getDeviceSystem() {
        return deviceSystem;
    }

    public void setDeviceSystem(String deviceSystem) {
        this.deviceSystem = deviceSystem;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getPrisonBreak() {
        return prisonBreak;
    }

    public void setPrisonBreak(String prisonBreak) {
        this.prisonBreak = prisonBreak;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }
}
