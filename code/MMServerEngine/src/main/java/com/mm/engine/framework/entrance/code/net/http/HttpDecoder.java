package com.mm.engine.framework.entrance.code.net.http;

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
public interface HttpDecoder{
    public byte[] decode(HttpServletRequest request) throws IOException;
}
