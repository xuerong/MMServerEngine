package test.service;

import com.mm.engine.framework.net.code.RetPacket;
import com.mm.engine.framework.net.code.RetPacketImpl;
import com.mm.engine.framework.control.annotation.Request;
import com.mm.engine.framework.control.annotation.Service;
import com.mm.engine.framework.data.entity.session.Session;
import com.protocol.OpCode;

/**
 * Created by Administrator on 2015/11/19.
 */
@Service
public class TestService {
    @Request(opcode = OpCode.CSLogin)
    public RetPacket handlerLogin(Object clientData, Session session){
//        PBMessage.SCLoginRet.Builder builder= PBMessage.SCLoginRet.newBuilder();
//        builder.setHasNewVersion(1);
//        builder.setNewVersionUrl("123456");
//        return new RetPacketImpl(OpCode.SCLoginRet,builder);
        return null;
    }

}
