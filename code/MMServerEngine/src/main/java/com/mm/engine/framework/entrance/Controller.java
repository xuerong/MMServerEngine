package com.mm.engine.framework.entrance;

import com.mm.engine.framework.entrance.code.protocol.ProtocolDecode;
import com.mm.engine.framework.entrance.code.protocol.ProtocolEncode;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created by Administrator on 2015/11/19.
 * 系统启动要获取所有的入口，并为入口设置协议解码器
 * 每种访问入口名称是固定存储，如http放在header里
 *
 * 入口分为两种：RequestEntrance和NetEventEntrance，分开来设计
 *
 * 后面改成控制转发器比较合适:Controler
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Controller {
    // 入口名，需要在协议解码器得到，如果没有，将进入默认入口
    String name();
    Class<? extends ProtocolEncode> protocolEncode();
    Class<? extends ProtocolDecode> protocolDecode();
}
