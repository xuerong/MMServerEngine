package com.mm.engine.framework.entrance.http;

import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.entrance.code.net.http.HttpPacket;
import com.mm.engine.framework.entrance.ControllerDispatcher;
import com.mm.engine.framework.entrance.NetType;
import com.mm.engine.framework.server.Server;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/13.
 *
 * 引擎的http入口类
 */
@WebServlet(name = "EntranceServlet",urlPatterns={"/http/*"} ,loadOnStartup=1)
public class EntranceServlet extends HttpServlet {
    private static final Logger log = LoggerFactory.getLogger(EntranceServlet.class);
    static{
        Server.init();
    }
    @Override
    public void init() throws ServletException{
        super.init();
        System.out.println("start------------------------");
        Server.start();

    }
    @Override
    protected void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        try {
            log.info("do~~qqq:");
            HttpDecoder httpDecoder = BeanHelper.getFrameBean(HttpDecoder.class);
            NetPacket netPacket = httpDecoder.decode(request);
            if (netPacket == null) {
                // net解码错误
                System.out.println("net解码错误");
            }

            NetPacket reNetPacket=ControllerDispatcher.handle(NetType.Http,netPacket,request.getContextPath(), Util.getIp(request));

            if (reNetPacket == null) {
                // 协议编码错误
                System.out.println("协议编码错误");
            }
            HttpEncoder httpEncoder = BeanHelper.getFrameBean(HttpEncoder.class);
            HttpPacket reHttpPacket = httpEncoder.encode(reNetPacket);
            if (reHttpPacket == null) {
                // http编码错误
                System.out.println("http编码错误");
            }
            // 设置头
            Map<String, String> headers = reHttpPacket.getHeaders();
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                // 这个地方要用setHeader，而不是addHeader
                response.setHeader(entry.getKey(), entry.getValue());
            }
            System.out.println("reHttpPacket:"+reHttpPacket.getHeaders()+","+reHttpPacket.getData().length);
            // 这个地方要+1
            response.setBufferSize(reHttpPacket.getData().length+1);
            response.getOutputStream().write(reHttpPacket.getData(), 0, reHttpPacket.getData().length);
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException("EntranceServlet.service Exception");
        }
    }
    @Override
    public void destroy() {
        System.out.println("stop-------------------");
        super.destroy();
        Server.stop();
    }
}
