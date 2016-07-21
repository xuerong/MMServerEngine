package com.mm.engine.sysBean.controller;

import com.mm.engine.framework.entrance.code.protocol.Packet;
import com.mm.engine.framework.entrance.code.protocol.RetPacket;
import com.mm.engine.framework.entrance.code.protocol.playerPacket.PlayerPacketCoder;
import com.mm.engine.framework.control.request.RequestDispatcher;
import com.mm.engine.framework.control.request.RequestHandler;
import com.mm.engine.framework.data.entity.session.Session;
import com.mm.engine.framework.entrance.Controller;
import com.mm.engine.framework.entrance.EntranceController;

/**
 * Created by Administrator on 2015/11/19.
 */
@Controller(
        name="DefaultRequestController",
        protocolEncode = PlayerPacketCoder.class,
        protocolDecode = PlayerPacketCoder.class
)
public class DefaultRequestController implements EntranceController {
    @Override
    public RetPacket control(Packet packet, Session session)  throws Exception{
        // 用controler根据opcode进行导航，并将session和packet.getValue()作为参数导入
        RequestHandler requestHandler = RequestDispatcher.getHandler(packet.getOpcode());
        if (requestHandler == null) {
            // handler不存在
            System.out.println("handler不存在");
            return null;
        }
        RetPacket rePacket = requestHandler.handle(packet.getOpcode(), packet.getValue(), session);
        return rePacket;
    }
}
