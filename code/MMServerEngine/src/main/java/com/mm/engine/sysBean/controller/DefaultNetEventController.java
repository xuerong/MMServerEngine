package com.mm.engine.sysBean.controller;

import com.mm.engine.framework.control.netEvent.NetEventManager;
import com.mm.engine.framework.entrance.code.protocol.Packet;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.entrance.code.protocol.netEventPacket.NetEventCoder;
import com.mm.engine.framework.entrance.code.protocol.playerPacket.PlayerPacketCoder;
import com.mm.engine.framework.control.netEvent.NetEventListenerHandler;
import com.mm.engine.framework.control.netEvent.NetEventData;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.entrance.Controller;
import com.mm.engine.framework.entrance.EntranceController;

/**
 * Created by Administrator on 2015/11/25.
 */
@Controller(
        name="DefaultNetEventController",
        protocolEncode = NetEventCoder.class,
        protocolDecode = NetEventCoder.class
)
public class DefaultNetEventController implements EntranceController{

    @Override
    public RetPacket control(Packet packet, Session session) throws Exception {
        // 用controler根据netEvent进行导航，并将session和packet.getValue()作为参数导入
        NetEventListenerHandler netEventHandler = NetEventManager.getHandler(packet.getOpcode());
        if (netEventHandler == null) {
            // handler不存在
            System.out.println("netEventHandler不存在,netEvent:"+packet.getOpcode());
            return null;
        }
        NetEventData netEventData = new NetEventData(packet.getOpcode());
        netEventData.setParam(packet.getValue());
        RetPacket rePacket = netEventHandler.handle(netEventData);
        return rePacket;
    }
}
