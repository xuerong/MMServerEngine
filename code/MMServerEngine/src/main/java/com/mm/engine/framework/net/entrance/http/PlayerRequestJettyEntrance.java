package com.mm.engine.framework.net.entrance.http;

import com.google.protobuf.AbstractMessage;
import com.mm.engine.framework.control.request.RequestService;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionService;
import com.mm.engine.framework.net.code.HttpDecoder;
import com.mm.engine.framework.net.entrance.Entrance;
import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import com.mm.engine.framework.tool.util.Util;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by a on 2016/8/29.
 */
public class PlayerRequestJettyEntrance extends Entrance {
    private static final Logger log = LoggerFactory.getLogger(PlayerRequestJettyEntrance.class);

    public static final String sessionKey = "sessionKey";

    private Server server;


    public PlayerRequestJettyEntrance(){}

    private SessionService sessionService;
    private RequestService requestService;
    @Override
    public void start() throws Exception {
        sessionService = BeanHelper.getServiceBean(SessionService.class);
        requestService = BeanHelper.getServiceBean(RequestService.class);

        Handler entranceHandler = new AbstractHandler(){
            @Override
            public void handle(String target, Request baseRequest,
                               HttpServletRequest request, HttpServletResponse response) throws IOException {
                fire(request,response,"EntranceJetty");
            }
        };

        server = new Server(this.port);
        server.setHandler(entranceHandler);
        server.start();
    }

    private void fire(HttpServletRequest request, HttpServletResponse response,String entranceName){
        try {
            byte[] data = HttpDecoder.decode(request);
            // 获取controller，并根据controller获取相应的编解码器
            Object opcodeObj = request.getHeader(SysConstantDefine.opcodeKey);
            if(opcodeObj==null){
                log.warn("opcode is not exist when decode in : PlayerRequestJettyEntrance");
                return;
            }
            String opcodeStr= opcodeObj.toString();
            if(opcodeStr.length()==0 || !StringUtils.isNumeric(opcodeStr)){
                log.warn("opcode is not exist when decode in : PlayerRequestJettyEntrance");
                return;
            }
            int opcode=Integer.parseInt(opcodeStr);
            // 获取sessionId
            String sessionId=null;
            Object sessionIdObj = request.getHeader(sessionKey);
            if(sessionIdObj!=null && sessionIdObj instanceof String){
                String sessionIdStr=(String)sessionIdObj;
                if(!StringUtils.isEmpty(sessionIdStr)){
                    sessionId=sessionIdStr;
                }
            }
//        log.info("opcode:" + packet.getOpcode());
            // 获取session
            Session session = null;
            if(!StringUtils.isEmpty(sessionId) ){
                session = sessionService.get(sessionId);
                if (session == null) {
                    log.warn("session " + sessionId + " is not exist,may expired,create new session");
                }
            }
            if(session == null){
                session = sessionService.create(request.getContextPath(), Util.getIp(request));
            }

            RetPacket rePacket = requestService.handle(opcode, data, session);

            if(rePacket==null){
                // 处理包失败
                log.error("处理消息错误,session:"+session.getSessionId());
                return;
            }


            response.setHeader(SysConstantDefine.opcodeKey,""+rePacket.getOpcode());
            if(rePacket.keepSession()){
                response.setHeader(sessionKey,session.getSessionId());
            }
            AbstractMessage.Builder<?> builder=(AbstractMessage.Builder<?>)rePacket.getRetData();
            byte[] reData=builder.build().toByteArray();

            // 这个地方要+1
            response.setBufferSize(reData.length+1);
            response.setContentLength(reData.length);
            response.getOutputStream().write(reData, 0, reData.length);
            response.getOutputStream().flush();
//            response.getOutputStream().close();
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException(entranceName+" Exception");
        }
    }

    @Override
    public void stop() throws Exception {
        if(server != null){
            server.stop();
        }
    }
}
