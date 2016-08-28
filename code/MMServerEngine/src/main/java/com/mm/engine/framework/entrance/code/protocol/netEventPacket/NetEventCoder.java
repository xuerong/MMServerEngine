package com.mm.engine.framework.entrance.code.protocol.netEventPacket;

import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.entrance.code.net.NetPacket;
import com.mm.engine.framework.entrance.code.net.NetPacketImpl;
import com.mm.engine.framework.entrance.code.protocol.*;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * Created by apple on 16-8-28.
 */
public class NetEventCoder implements ProtocolDecode,ProtocolEncode {
    @Override
    public NetPacket encode(RetPacket packet, Session session) {
        try{
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(bos);
            out.writeObject(packet.getRetData());
            out.flush();
            out.close();
            byte[] nb = bos.toByteArray();
            NetPacket netPacket = new NetPacketImpl(nb);
            if(packet.keepSession()){
                netPacket.put(Session.sessionKey,session.getSessionId());
            }
            return netPacket;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public Packet decode(NetPacket netPacket) {
        try{
            ByteArrayInputStream bis = new ByteArrayInputStream(netPacket.getData());
            ObjectInputStream oin = new ObjectInputStream(bis);
            NetEventData netEventData = (NetEventData)oin.readObject();
            Packet packet = new PacketImpl((String)netPacket.get(Session.sessionKey),
                    netEventData.getNetEvent(),netEventData);
            return packet;
        }catch (Throwable e){
            e.printStackTrace();
        }
        return null;
    }
}
