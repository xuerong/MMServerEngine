package com.mm.engine.framework.entrance.code.protocol.playerPacket;

import com.google.protobuf.AbstractMessage.Builder;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.entrance.code.protocol.*;
import com.mm.engine.framework.data.entity.session.Session;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2015/11/16.
 * // 这个事针对Player的解码器，要求必须有opcode，对应后面的RequestEntrance：gameclient
 */
public final class PlayerPacketCoder implements ProtocolDecode,ProtocolEncode{
    private static final Logger log = LoggerFactory.getLogger(PlayerPacketCoder.class);

    private static final String opcodeKey="opcode";

    @Override
    public Packet decode(NetPacket netPacket) {
        Map<String,Object> headers=netPacket.getHeaders();
        Object opcodeObj = headers.get(opcodeKey);
        if(opcodeObj==null){
            log.warn("opcode is not exist when decode in : PlayerPacketCoder");
            return null;
        }
        String opcodeStr= opcodeObj.toString();
        if(opcodeStr.length()==0 || !StringUtils.isNumeric(opcodeStr)){
            log.warn("opcode is not exist when decode in : PlayerPacketCoder");
            return null;
        }
        int opcode=Integer.parseInt(opcodeStr);
        // 获取sessionId
        String sessionId=null;
        Object sessionIdObj = headers.get(Session.sessionKey);
        if(sessionIdObj!=null && sessionIdObj instanceof String){
            String sessionIdStr=(String)sessionIdObj;
            if(!StringUtils.isEmpty(sessionIdStr)){
                sessionId=sessionIdStr;
            }
        }
        return new PacketImpl(sessionId,opcode,netPacket.getData());
    }

    @Override
    public NetPacket encode(RetPacket packet,Session session) {
        Map<String,Object> headers=new HashMap<>();
        headers.put(opcodeKey,""+packet.getOpcode());
        if(packet.keepSession()){
            headers.put(Session.sessionKey,session.getSessionId());
        }
        Builder<?> builder=(Builder<?>)packet.getRetData();
        byte[] reData=builder.build().toByteArray();
        NetPacket netPacket=new NetPacketImpl(headers,reData);
        return netPacket;
    }
}
