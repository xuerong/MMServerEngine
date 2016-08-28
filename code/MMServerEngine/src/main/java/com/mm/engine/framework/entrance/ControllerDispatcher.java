package com.mm.engine.framework.entrance;

import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.protocol.Packet;
import com.mm.engine.framework.entrance.code.protocol.ProtocolDecode;
import com.mm.engine.framework.entrance.code.protocol.ProtocolEncode;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.data.entity.session.SessionManager;
import com.mm.engine.framework.server.SysConstantDefine;
import com.mm.engine.framework.tool.helper.BeanHelper;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/20.
 */
public final class ControllerDispatcher {
    private static final Logger log = LoggerFactory.getLogger(ControllerDispatcher.class);
    private static Map<String,ControllerBean> controllerBeanMap =new HashMap<>();
    static {
        try {
            // 获取bean
            Map<String,ControllerHelper.EntranceControllerClass> requestEntranceClassMap= ControllerHelper.getEntranceControllerClassMap();
            for(Map.Entry<String,ControllerHelper.EntranceControllerClass> entry : requestEntranceClassMap.entrySet()){
                ControllerHelper.EntranceControllerClass cls=entry.getValue();
                ControllerBean bean=new ControllerBean();
                bean.controller = BeanHelper.getFrameBean(cls.getEntranceClass());
                bean.protocolDecode=BeanHelper.getFrameBean(cls.getProtocolDecodeClass());
                bean.protocolEncode=BeanHelper.getFrameBean(cls.getProtocolEncodeClass());

                controllerBeanMap.put(entry.getKey(),bean);
            }
        }catch (Throwable e){
            e.printStackTrace();

        }

    }
    /**
     * 处理   请求netpacket，并返回netpacket
     *
     * 判断是request还是netevent
     * 获取相应的controller，并用解码器解码，得到packet
     * 根据nettype获取或创建session
     * 调用相应的controller处理packet，得到retpacket
     * 用相应的编码器编码，得到netpacket
     * 返回netpacket
     * **/
    public static NetPacket handle(NetType netType,NetPacket netPacket ,String url,String ip) throws java.lang.Exception {
        ControllerBean controllerBean = getControllerBean((String)netPacket.get(SysConstantDefine.controller));
        // 获取controller，并根据controller获取相应的编解码器
        ProtocolDecode protocolDecode = controllerBean.getProtocolDecode();
        Packet packet = protocolDecode.decode(netPacket);
        if (packet == null) {
            //protocol解码错误
            log.error("protocol解码错误");
            return null;
        }
//        log.info("opcode:" + packet.getOpcode());
        // 获取session
        Session session;
        if (StringUtils.isEmpty(packet.getSessionId())) {
            session = SessionManager.create(netType,url,ip);
        } else {
            session = SessionManager.get(packet.getSessionId());
            if (session == null) {
                log.warn("session " + packet.getSessionId() + " is not exist,may expired,create new session");
                session = SessionManager.create(netType,url,ip);
            }
        }
        // 获取opcode
        int opcode = packet.getOpcode();
        RetPacket rePacket = controllerBean.getController().control(packet,session);
        if(rePacket==null){
            // 处理包失败
            log.error("处理消息错误,session:"+session.getSessionId());
            return null;
        }

        ProtocolEncode protocolEncode = controllerBean.getProtocolEncode();
        NetPacket reNetPacket = protocolEncode.encode(rePacket, session);
        if(reNetPacket == null){
            log.error("protocol编码错误:"+session.getSessionId());
            return null;
        }
        return reNetPacket;
    }
    // 获取默认的controller
    private String getDefaultControllerName(NetType netType){
        String result = "DefaultRequestController";
        switch (netType){
            case Http : result = "DefaultRequestController"; break;
            case WebSocket:result = "DefaultNetEventController"; break;
        }
        return result;
    }

    //
    private static ControllerBean getControllerBean(String name){
        if(StringUtils.isEmpty(name)){
            return controllerBeanMap.get("DefaultRequestController");
        }
        ControllerBean controllerBean = controllerBeanMap.get(name);
        if(controllerBean ==null){
            controllerBean = controllerBeanMap.get("DefaultRequestController");
        }
        return controllerBean;
    }

    private final static class ControllerBean {
        private EntranceController controller;
        private ProtocolDecode protocolDecode;
        private ProtocolEncode protocolEncode;

        public EntranceController getController(){
            return controller;
        }
        public ProtocolEncode getProtocolEncode(){
            return protocolEncode;
        }
        public ProtocolDecode getProtocolDecode(){
            return protocolDecode;
        }
    }
}
