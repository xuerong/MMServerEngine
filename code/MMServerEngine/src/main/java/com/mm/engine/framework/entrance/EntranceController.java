package com.mm.engine.framework.entrance;

import com.mm.engine.framework.entrance.code.protocol.Packet;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.data.entity.session.Session;

/**
 * Created by Administrator on 2015/11/19.
 * 所有访问的入口需要继承该接口，并添加注解：@Controller
 *
 * 访问入口其实就是一个分发器，
 * 可以在这里拦截特定的访问，并自定义分发
 *
 * opcode是系统默认的分发方案，会将协议解码出来的数据对象分发给对应的@Request注解的函数处理
 */
public interface EntranceController {
    public RetPacket control(Packet packet, Session session) throws Exception;
}
