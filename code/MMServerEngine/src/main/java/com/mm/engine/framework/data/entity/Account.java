package com.mm.engine.framework.data.entity;

import com.mm.engine.framework.data.entity.session.SessionClient;

import java.util.Date;

/**
 * Created by Administrator on 2016/1/4.
 * 账号的抽象类,实际使用可继承自该类,也可不继承自该类,
 * 但要实现SessionClient接口,并在客户端登陆的时候将相应的对象放入session,以便系统在传给其它服务的
 * session中存有该账号的信息
 */
public abstract class Account implements SessionClient {
    //
    protected int id;
    protected String name="";
    protected String icon="";
    protected String clientVersion;//客户端版本号

    protected Date createTime;
    protected Date lastLoginTime;
    protected Date lastLogoutTime;

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

    @Override
    public void destroySession() {

    }
}
