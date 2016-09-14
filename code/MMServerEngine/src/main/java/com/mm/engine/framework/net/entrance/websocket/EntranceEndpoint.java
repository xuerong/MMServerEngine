package com.mm.engine.framework.net.entrance.websocket;

import com.mm.engine.framework.tool.util.Util;
import org.apache.commons.lang3.ArrayUtils;

import javax.websocket.*;
import javax.websocket.server.ServerEndpoint;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/26.
 */
@ServerEndpoint("/webSocket")
public class EntranceEndpoint {
    private Session session;
    //private static final Logger sysLogger = Logger.getLogger("sysLog");

    @OnOpen
    public void open(Session session,EndpointConfig config) {
        this.session = session;

        System.out.println("*** WebSocket opened from sessionId " + session.getId());

    }

//    @OnMessage
//    public void inMessageStr(Session session,String message,boolean isLast) {
//        //sysLogger.info("*** WebSocket Received from sessionId " + this.session.getCacheId() + ": " + message);
//        System.out.println("rece:"+message+","+(session==this.session));
//        try {
//            session.getBasicRemote().sendText("success");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
    @OnMessage
    public void inMessageByte(Session session,byte[] message,boolean isLast)  {
        if(message==null){
            return;
        }
        try {
            if(isLast){
                byte[] data=message;
                if(session.getUserProperties().containsKey("messageBefore")) {
                    byte[] messageBefore = (byte[])session.getUserProperties().get("messageBefore");
                    data = ArrayUtils.addAll(message,messageBefore);
                    session.getUserProperties().remove("messageBefore");
                }
                Map<String,List<String>> rpMap=session.getRequestParameterMap();
                String url = session.getRequestURI().getPath();
                // the socket object is hidden in WsSession,client IP is not exposed via JSR-356
                // http://stackoverflow.com/questions/22880055/jsr-356-websockets-with-tomcat-how-to-limit-connections-within-single-ip-addre
                String ip = Util.getIp(session);
//                ControllerDispatcher.handle(NetType.WebSocket,netPacket,url,ip);
            }else{
                if(session.getUserProperties().containsKey("messageBefore")) {
                    byte[] messageBefore = (byte[])session.getUserProperties().get("messageBefore");
                    byte[] data= ArrayUtils.addAll(message,messageBefore);
                    session.getUserProperties().put("messageBefore",data);
                }else{
                    session.getUserProperties().put("messageBefore",message);
                }
            }
            System.out.println("rece:"+message+","+(session==this.session));
            session.getBasicRemote().sendText("success");
        }catch (Throwable e){
            e.printStackTrace();
            throw new RuntimeException("EntranceEndpoint.inMessageByte Exception");
        }
    }

    @OnClose
    public void end(Session session) {
        //sysLogger.info("*** WebSocket closed from sessionId " + this.session.getCacheId());
    }
}
