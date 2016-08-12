package com.mm.engine.framework.entrance;

import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.http.HttpDecoder;
import com.mm.engine.framework.entrance.code.net.http.HttpEncoder;
import com.mm.engine.framework.entrance.code.net.http.HttpPacket;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

/**
 * Created by a on 2016/8/9.
 */
public class NetFlowFire {
    private static final Logger log = LoggerFactory.getLogger(NetFlowFire.class);
    public static void fireHttp(HttpServletRequest request, HttpServletResponse response,String entranceName){
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
            response.setContentLength(reHttpPacket.getData().length);
            response.getOutputStream().write(reHttpPacket.getData(), 0, reHttpPacket.getData().length);
            response.getOutputStream().flush();
//            response.getOutputStream().close();
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(entranceName+" Exception");
        }
    }
}
