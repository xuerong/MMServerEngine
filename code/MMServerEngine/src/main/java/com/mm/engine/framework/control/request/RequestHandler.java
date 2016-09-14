package com.mm.engine.framework.control.request;

import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.data.entity.session.Session;

/**
 * Created by Administrator on 2015/11/17.
 * RequestHandler是访问的处理者，是对存在@Request注解的Service的封装，让其继承RequestHandler
 * 添加handle方法，通过swich-case结构转发请求
 */
public interface RequestHandler {
    public RetPacket handle(int opcode,Object clientData, Session session) throws Exception;
}
