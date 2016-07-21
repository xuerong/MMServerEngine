package com.mm.engine.framework.entrance.code.net.http;

import com.mm.engine.framework.entrance.code.net.NetDecode;
import com.mm.engine.framework.entrance.code.net.NetPacket;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Administrator on 2015/11/13.
 *
 * 可以解析出来的东西包括：
 * controller
 * opcode
 * session
 *
 * 用户data
 */
public interface HttpDecoder extends NetDecode{
    public NetPacket decode(HttpServletRequest request) throws IOException;
}
