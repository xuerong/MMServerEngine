package com.mm.engine.framework.net.code.protocol;


/**
 * Created by Administrator on 2015/11/16.
 *
 * Packet是由NetPacket通过用户定义的数据传输协议，如pb，json，xml等解析而来，需要用户自定义
 * opcode:用来判断将数据分发给service-oper
 * value：分发的游戏逻辑数据包
 *
 * packet分为多种：玩家数据，管理员数据，第三方访问，等，又可以细分
 * 其中，又可以分为带有session的数据和不带有session的数据,session服务器保存的客户端相关的数据
 *
 * 若解码错误，返回null
 */
public interface Packet {
    public int getOpcode();
    public String getSessionId();
    public Object getValue();
}
